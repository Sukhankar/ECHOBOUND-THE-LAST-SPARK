package com.echobound.sandbox;

import java.util.HashMap;
import java.util.Map;

/**
 * Ultra-compact 16x16x8 voxel chunk storing block IDs in a flat byte[2048] array.
 * Tracks player modifications as a sparse delta map to minimize disk and memory footprint.
 */
public class CompactChunk {
    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 16;
    public static final int CHUNK_SIZE_Z = 8;
    public static final int TOTAL_BLOCKS = CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z; // 2048

    public final int chunkX;
    public final int chunkY;
    private final byte[] blocks = new byte[TOTAL_BLOCKS]; // Exactly 2 KB

    // Sparse player modification storage (only stores changed blocks)
    private final Map<Short, Byte> playerDeltas = new HashMap<>();

    public CompactChunk(int chunkX, int chunkY) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
    }

    private static int getIndex(int x, int y, int z) {
        return (z * CHUNK_SIZE_X * CHUNK_SIZE_Y) + (y * CHUNK_SIZE_X) + x;
    }

    public byte getBlockId(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE_X || y < 0 || y >= CHUNK_SIZE_Y || z < 0 || z >= CHUNK_SIZE_Z) {
            return 0; // AIR
        }
        return blocks[getIndex(x, y, z)];
    }

    public void setBlockId(int x, int y, int z, byte blockId) {
        if (x < 0 || x >= CHUNK_SIZE_X || y < 0 || y >= CHUNK_SIZE_Y || z < 0 || z >= CHUNK_SIZE_Z) {
            return;
        }
        short idx = (short) getIndex(x, y, z);
        blocks[idx] = blockId;
        playerDeltas.put(idx, blockId); // Save only sparse delta
    }

    public int getModifiedBlockCount() {
        return playerDeltas.size();
    }

    public Map<Short, Byte> getPlayerDeltas() {
        return playerDeltas;
    }

    public void applyPlayerDeltas(Map<Short, Byte> deltas) {
        if (deltas == null) return;
        for (Map.Entry<Short, Byte> entry : deltas.entrySet()) {
            short idx = entry.getKey();
            byte val = entry.getValue();
            if (idx >= 0 && idx < TOTAL_BLOCKS) {
                blocks[idx] = val;
                playerDeltas.put(idx, val);
            }
        }
    }

    public void generateTerrain(long seed) {
        for (int lx = 0; lx < CHUNK_SIZE_X; lx++) {
            for (int ly = 0; ly < CHUNK_SIZE_Y; ly++) {
                int worldX = chunkX * CHUNK_SIZE_X + lx;
                int worldY = chunkY * CHUNK_SIZE_Y + ly;

                double n1 = Math.sin(worldX * 0.08 + seed * 0.1) * Math.cos(worldY * 0.08);
                double n2 = Math.sin(worldX * 0.23) * Math.cos(worldY * 0.23) * 0.5;
                double elevation = n1 + n2;

                int surfaceZ = 3;
                if (elevation > 0.4) surfaceZ = 4;
                if (elevation > 0.9) surfaceZ = 5;
                if (elevation < -0.7) surfaceZ = 2;

                // Layer 0: Bedrock
                blocks[getIndex(lx, ly, 0)] = (byte) BlockType.STONE.id;

                // Subsurface
                for (int z = 1; z < surfaceZ; z++) {
                    double ore = Math.sin(worldX * 0.35 + z * 1.5) * Math.cos(worldY * 0.35 + z);
                    if (ore > 0.65) {
                        blocks[getIndex(lx, ly, z)] = (byte) BlockType.SPARK_ORE.id;
                    } else if (ore < -0.75) {
                        blocks[getIndex(lx, ly, z)] = (byte) BlockType.CRYSTAL_NODE.id;
                    } else {
                        blocks[getIndex(lx, ly, z)] = (byte) ((z == surfaceZ - 1) ? BlockType.DIRT.id : BlockType.STONE.id);
                    }
                }

                // Surface
                blocks[getIndex(lx, ly, surfaceZ)] = (byte) ((surfaceZ == 2) ? BlockType.WATER.id : BlockType.GRASS.id);

                // Trees
                if (surfaceZ >= 3 && surfaceZ < 6) {
                    double treeChance = Math.sin(worldX * 1.7) * Math.cos(worldY * 1.7);
                    if (treeChance > 0.82 && lx > 1 && lx < CHUNK_SIZE_X - 2 && ly > 1 && ly < CHUNK_SIZE_Y - 2) {
                        blocks[getIndex(lx, ly, surfaceZ + 1)] = (byte) BlockType.WOOD_LOG.id;
                        if (surfaceZ + 2 < CHUNK_SIZE_Z) {
                            blocks[getIndex(lx, ly, surfaceZ + 2)] = (byte) BlockType.WOOD_LOG.id;
                            blocks[getIndex(lx, ly, surfaceZ + 3)] = (byte) BlockType.LEAVES.id;
                            if (lx + 1 < CHUNK_SIZE_X) blocks[getIndex(lx + 1, ly, surfaceZ + 2)] = (byte) BlockType.LEAVES.id;
                            if (lx - 1 >= 0) blocks[getIndex(lx - 1, ly, surfaceZ + 2)] = (byte) BlockType.LEAVES.id;
                            if (ly + 1 < CHUNK_SIZE_Y) blocks[getIndex(lx, ly + 1, surfaceZ + 2)] = (byte) BlockType.LEAVES.id;
                            if (ly - 1 >= 0) blocks[getIndex(lx, ly - 1, surfaceZ + 2)] = (byte) BlockType.LEAVES.id;
                        }
                    }
                }
            }
        }

        // Re-apply any existing player deltas on top of generated terrain
        for (Map.Entry<Short, Byte> entry : playerDeltas.entrySet()) {
            blocks[entry.getKey()] = entry.getValue();
        }
    }
}
