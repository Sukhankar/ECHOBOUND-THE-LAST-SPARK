package com.echobound.sandbox;

public class WorldChunk {
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
     * Procedural terrain generator for this chunk based on coordinates.
     */
    public void generateTerrain(long seed) {
        for (int lx = 0; lx < CHUNK_SIZE_X; lx++) {
            for (int ly = 0; ly < CHUNK_SIZE_Y; ly++) {
                int worldX = chunkX * CHUNK_SIZE_X + lx;
                int worldY = chunkY * CHUNK_SIZE_Y + ly;

                // Procedural elevation noise
                double n1 = Math.sin(worldX * 0.08 + seed * 0.1) * Math.cos(worldY * 0.08);
                double n2 = Math.sin(worldX * 0.23) * Math.cos(worldY * 0.23) * 0.5;
                double elevation = n1 + n2;

                int surfaceZ = 3;
                if (elevation > 0.4) surfaceZ = 4;
                if (elevation > 0.9) surfaceZ = 5;
                if (elevation < -0.7) surfaceZ = 2; // Water / lake depression

                // Layer 0: Bedrock & stone
                blocks[lx][ly][0] = BlockType.STONE;

                // Underground stone / ores
                for (int z = 1; z < surfaceZ; z++) {
                    double oreNoise = Math.sin(worldX * 0.35 + z * 1.5) * Math.cos(worldY * 0.35 + z);
                    if (oreNoise > 0.65) {
                        blocks[lx][ly][z] = BlockType.SPARK_ORE;
                    } else if (oreNoise < -0.75) {
                        blocks[lx][ly][z] = BlockType.CRYSTAL_NODE;
                    } else {
                        blocks[lx][ly][z] = (z == surfaceZ - 1) ? BlockType.DIRT : BlockType.STONE;
                    }
                }

                // Surface layer
                if (surfaceZ == 2) {
                    blocks[lx][ly][surfaceZ] = BlockType.WATER;
                } else {
                    blocks[lx][ly][surfaceZ] = BlockType.GRASS;
                }

                // Procedural Trees & Rocks
                if (surfaceZ >= 3 && surfaceZ < 6) {
                    double treeChance = Math.sin(worldX * 1.7) * Math.cos(worldY * 1.7);
                    if (treeChance > 0.82 && lx > 1 && lx < CHUNK_SIZE_X - 2 && ly > 1 && ly < CHUNK_SIZE_Y - 2) {
                        // Place tree trunk
                        blocks[lx][ly][surfaceZ + 1] = BlockType.WOOD_LOG;
                        if (surfaceZ + 3 < CHUNK_SIZE_Z) {
                            blocks[lx][ly][surfaceZ + 2] = BlockType.WOOD_LOG;
                            blocks[lx][ly][surfaceZ + 3] = BlockType.LEAVES;
                            if (lx + 1 < CHUNK_SIZE_X) blocks[lx + 1][ly][surfaceZ + 2] = BlockType.LEAVES;
                            if (lx - 1 >= 0) blocks[lx - 1][ly][surfaceZ + 2] = BlockType.LEAVES;
                            if (ly + 1 < CHUNK_SIZE_Y) blocks[lx][ly + 1][surfaceZ + 2] = BlockType.LEAVES;
                            if (ly - 1 >= 0) blocks[lx][ly - 1][surfaceZ + 2] = BlockType.LEAVES;
                        }
                    }
                }
            }
        }
    }
}
