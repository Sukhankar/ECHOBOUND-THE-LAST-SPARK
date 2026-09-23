package com.echobound.sandbox;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldChunk {
    // One PerlinNoise instance per (seed, purpose) pair, cached — chunk generation calls
    // surfaceZAt() once per block (256x per chunk), and rebuilding a 256-entry permutation
    // table that often would be wasteful. Real gradient noise (see PerlinNoise.java) replaces
    // the sin(x)*cos(y) combinations previously used for elevation/biome/vegetation: a sum of
    // a few pure sinusoids has visible periodicity and axis-aligned banding at this scale —
    // gradient noise is what terrain generators actually use to avoid exactly that.
    private static final Map<Long, PerlinNoise> NOISE_CACHE = new ConcurrentHashMap<>();
    private static final long ELEVATION_NOISE = 0, CLIMATE_NOISE = 1, ORE_NOISE = 2,
                               FEATURE_NOISE = 3, DETAIL_NOISE = 4;

    private static PerlinNoise noiseFor(long seed, long purpose) {
        return NOISE_CACHE.computeIfAbsent(seed * 31 + purpose, PerlinNoise::new);
    }

    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 16;
    public static final int CHUNK_SIZE_Z = 8;
    public static final int BLOCK_PIXEL_SIZE = 16;

    public final int chunkX;
    public final int chunkY;
    private final BlockType[][][] blocks;
    private boolean modified = false;

    public WorldChunk(int chunkX, int chunkY) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.blocks = new BlockType[CHUNK_SIZE_X][CHUNK_SIZE_Y][CHUNK_SIZE_Z];
        for (int x = 0; x < CHUNK_SIZE_X; x++) {
            for (int y = 0; y < CHUNK_SIZE_Y; y++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                    blocks[x][y][z] = BlockType.AIR;
                }
            }
        }
    }

    public BlockType getBlock(int localX, int localY, int localZ) {
        if (localX < 0 || localX >= CHUNK_SIZE_X ||
            localY < 0 || localY >= CHUNK_SIZE_Y ||
            localZ < 0 || localZ >= CHUNK_SIZE_Z) {
            return BlockType.AIR;
        }
        return blocks[localX][localY][localZ];
    }

    public void setBlock(int localX, int localY, int localZ, BlockType type) {
        if (localX < 0 || localX >= CHUNK_SIZE_X ||
            localY < 0 || localY >= CHUNK_SIZE_Y ||
            localZ < 0 || localZ >= CHUNK_SIZE_Z) {
            return;
        }
        blocks[localX][localY][localZ] = (type != null) ? type : BlockType.AIR;
        modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    /**
     * Which biome this chunk belongs to. Chosen per-chunk (not per-block) from a
     * low-frequency climate noise so biomes form coherent regions instead of a
     * per-tile speckle — the same reason Minecraft-likes pick biome at chunk grain.
     */
    public enum Biome { GRASSLAND, DESERT, TUNDRA }

    /** Chebyshev chunk radius around the origin kept as guaranteed Grassland. */
    private static final int HOME_BIOME_RADIUS = 2;

    public static Biome biomeAt(int chunkX, int chunkY, long seed) {
        // The player always spawns at (8,8) in chunk (0,0), and every fixed-position NPC
        // (Kael, Rowan, Lyra — see NPCManager) sits within a couple of chunks of it, several
        // with dialogue/activity tied to grass and greenery ("botanical greenhouse", "hollow
        // oak cabin"). Climate noise alone can and did place chunk (0,0) in Desert, which
        // would spawn the player in sand next to an NPC "tending a botanical greenhouse" —
        // so the home region is pinned to Grassland and biome variety starts a short walk out.
        if (Math.max(Math.abs(chunkX), Math.abs(chunkY)) <= HOME_BIOME_RADIUS) {
            return Biome.GRASSLAND;
        }
        double climate = noiseFor(seed, CLIMATE_NOISE).fbm(chunkX * 0.15, chunkY * 0.15, 3, 0.5);
        if (climate > 0.32) return Biome.DESERT;
        if (climate < -0.32) return Biome.TUNDRA;
        return Biome.GRASSLAND;
    }

    /**
     * Procedural terrain generator for this chunk based on coordinates.
     */
    public void generateTerrain(long seed) {
        Biome biome = biomeAt(chunkX, chunkY, seed);
        BlockType surfaceBlock = switch (biome) {
            case DESERT -> BlockType.SAND;
            case TUNDRA -> BlockType.SNOW;
            default -> BlockType.GRASS;
        };

        for (int lx = 0; lx < CHUNK_SIZE_X; lx++) {
            for (int ly = 0; ly < CHUNK_SIZE_Y; ly++) {
                int worldX = chunkX * CHUNK_SIZE_X + lx;
                int worldY = chunkY * CHUNK_SIZE_Y + ly;

                int surfaceZ = surfaceZAt(worldX, worldY, seed);

                // Layer 0: Bedrock & stone
                blocks[lx][ly][0] = BlockType.STONE;

                // Underground stone / ores
                for (int z = 1; z < surfaceZ; z++) {
                    double oreNoise = noiseFor(seed, ORE_NOISE).noise(worldX * 0.35, worldY * 0.35 + z * 1.5);
                    if (oreNoise > 0.65) {
                        blocks[lx][ly][z] = BlockType.SPARK_ORE;
                    } else if (oreNoise < -0.75) {
                        blocks[lx][ly][z] = BlockType.CRYSTAL_NODE;
                    } else {
                        blocks[lx][ly][z] = (z == surfaceZ - 1) ? BlockType.DIRT : BlockType.STONE;
                    }
                }

                // Surface layer — biome picks the ground block; lake depressions stay water
                // in every biome (an oasis reads fine in desert, a thaw pool in tundra).
                boolean isWater = (surfaceZ == 2);
                if (isWater) {
                    blocks[lx][ly][surfaceZ] = BlockType.WATER;
                } else {
                    blocks[lx][ly][surfaceZ] = surfaceBlock;
                }

                // Procedural vegetation, rocks & ruins — biome-specific, one decoration
                // layer placed directly above the surface block (never touches the tree's
                // own trunk/canopy blocks, since each branch below is mutually exclusive).
                if (!isWater && surfaceZ >= 3 && surfaceZ < 6
                        && lx > 1 && lx < CHUNK_SIZE_X - 2 && ly > 1 && ly < CHUNK_SIZE_Y - 2) {
                    // Low frequency (0.08) so trees/rocks form natural clusters — a real forest
                    // has patches, not one tree every few independently-random blocks, which
                    // is what the old per-block sin/cos featureNoise effectively produced.
                    double featureNoise = noiseFor(seed, FEATURE_NOISE).fbm(worldX * 0.08, worldY * 0.08, 2, 0.5);
                    double detailNoise = noiseFor(seed, DETAIL_NOISE).fbm(worldX * 0.15, worldY * 0.15, 2, 0.5);

                    if (biome == Biome.DESERT) {
                        if (featureNoise > 0.86) {
                            placeBoulder(lx, ly, surfaceZ);
                        } else if (featureNoise > 0.62) {
                            blocks[lx][ly][surfaceZ + 1] = BlockType.CACTUS;
                        }
                    } else if (biome == Biome.TUNDRA) {
                        if (featureNoise > 0.82) {
                            placeTree(lx, ly, surfaceZ);
                        } else if (featureNoise > 0.68) {
                            placeBoulder(lx, ly, surfaceZ);
                        }
                    } else { // GRASSLAND
                        if (featureNoise > 0.82) {
                            placeTree(lx, ly, surfaceZ);
                        } else if (featureNoise > 0.70) {
                            blocks[lx][ly][surfaceZ + 1] = BlockType.BERRY_BUSH;
                        } else if (detailNoise > 0.55) {
                            blocks[lx][ly][surfaceZ + 1] = BlockType.WILDFLOWER;
                        } else if (detailNoise < -0.4) {
                            blocks[lx][ly][surfaceZ + 1] = BlockType.TALL_GRASS;
                        }
                    }
                }
            }
        }

        // Ancient ruins: rare, biome-independent small structure — roughly 1 in 40 chunks,
        // deterministic per chunk coordinate so it doesn't shift between world loads.
        long ruinHash = hashChunk(chunkX, chunkY, seed);
        if (Math.floorMod(ruinHash, 40L) == 0) {
            placeRuin(seed);
        }
    }

    /** Ground elevation noise, shared by the main generation pass and structure placement
     *  (e.g. ruins) so a structure never has to re-derive "the surface" by scanning blocks —
     *  a scan can be fooled by vegetation (a tree's leaves sit above the true ground). */
    private static int surfaceZAt(int worldX, int worldY, long seed) {
        // 4-octave fractal Perlin noise instead of a 2-term sin/cos sum — the old formula's
        // hills and lakes fell into a visibly repeating diagonal wave pattern once you walked
        // far enough to see it; real fbm noise doesn't repeat at any scale you'd encounter.
        double elevation = noiseFor(seed, ELEVATION_NOISE).fbm(worldX * 0.05, worldY * 0.05, 4, 0.5);

        int surfaceZ = 3;
        if (elevation > 0.27) surfaceZ = 4;
        if (elevation > 0.6) surfaceZ = 5;
        if (elevation < -0.47) surfaceZ = 2; // Water / lake depression
        return surfaceZ;
    }

    private void placeTree(int lx, int ly, int surfaceZ) {
        if (surfaceZ + 3 >= CHUNK_SIZE_Z) return;
        blocks[lx][ly][surfaceZ + 1] = BlockType.WOOD_LOG;
        blocks[lx][ly][surfaceZ + 2] = BlockType.WOOD_LOG;
        blocks[lx][ly][surfaceZ + 3] = BlockType.LEAVES;
        if (lx + 1 < CHUNK_SIZE_X) blocks[lx + 1][ly][surfaceZ + 2] = BlockType.LEAVES;
        if (lx - 1 >= 0) blocks[lx - 1][ly][surfaceZ + 2] = BlockType.LEAVES;
        if (ly + 1 < CHUNK_SIZE_Y) blocks[lx][ly + 1][surfaceZ + 2] = BlockType.LEAVES;
        if (ly - 1 >= 0) blocks[lx][ly - 1][surfaceZ + 2] = BlockType.LEAVES;
    }

    private void placeBoulder(int lx, int ly, int surfaceZ) {
        blocks[lx][ly][surfaceZ + 1] = BlockType.BOULDER;
    }

    /**
     * A small, unmistakably human-made ruin: a 3×3 brick platform one layer above the
     * surface, with a two-block pillar at its center — a deliberate contrast to the
     * organic terrain noise around it, placed near the chunk's center so it never
     * straddles a chunk boundary.
     */
    private void placeRuin(long seed) {
        int cx = CHUNK_SIZE_X / 2;
        int cy = CHUNK_SIZE_Y / 2;
        int worldCx = chunkX * CHUNK_SIZE_X + cx;
        int worldCy = chunkY * CHUNK_SIZE_Y + cy;

        // Computed the same way the surface itself was generated — not by scanning the
        // existing column, which can be fooled into reading a tree's leaf canopy (placed
        // above the true ground) as "the surface" and then bailing out on the height check.
        int surfaceZ = surfaceZAt(worldCx, worldCy, seed);
        if (surfaceZ == 2 || surfaceZ + 3 >= CHUNK_SIZE_Z) return; // skip lake tiles too

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int x = cx + dx, y = cy + dy;
                if (x < 0 || x >= CHUNK_SIZE_X || y < 0 || y >= CHUNK_SIZE_Y) continue;
                blocks[x][y][surfaceZ] = BlockType.RUINS_BRICK;
                blocks[x][y][surfaceZ + 1] = BlockType.AIR; // clear any vegetation the main pass placed here
            }
        }
        blocks[cx][cy][surfaceZ + 1] = BlockType.RUINS_BRICK;
        blocks[cx][cy][surfaceZ + 2] = BlockType.RUINS_BRICK;
    }

    private static long hashChunk(int chunkX, int chunkY, long seed) {
        long h = seed;
        h = h * 31 + chunkX;
        h = h * 31 + chunkY;
        h ^= (h >>> 17);
        h *= 0x9E3779B97F4A7C15L;
        h ^= (h >>> 29);
        return h;
    }
}
