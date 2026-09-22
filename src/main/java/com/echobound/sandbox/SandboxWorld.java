package com.echobound.sandbox;

import com.echobound.physics3d.AABB3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SandboxWorld {
    private final Map<Long, WorldChunk> chunks = new HashMap<>();
    private final long seed;

    public SandboxWorld(long seed) {
        this.seed = seed;
    }

    private long getChunkKey(int cx, int cy) {
        return (((long) cx) << 32) | (cy & 0xFFFFFFFFL);
    }

    public WorldChunk getOrCreateChunk(int cx, int cy) {
        long key = getChunkKey(cx, cy);
        WorldChunk chunk = chunks.get(key);
        if (chunk == null) {
            chunk = new WorldChunk(cx, cy);
            chunk.generateTerrain(seed);
            chunks.put(key, chunk);
        }
        return chunk;
    }

    public int getLoadedChunkCount() {
        return chunks.size();
    }

    public BlockType getBlock(int wx, int wy, int wz) {
        if (wz < 0 || wz >= WorldChunk.CHUNK_SIZE_Z) {
            return BlockType.AIR;
        }
        int cx = Math.floorDiv(wx, WorldChunk.CHUNK_SIZE_X);
        int cy = Math.floorDiv(wy, WorldChunk.CHUNK_SIZE_Y);
        int lx = wx - cx * WorldChunk.CHUNK_SIZE_X;
        int ly = wy - cy * WorldChunk.CHUNK_SIZE_Y;

        WorldChunk chunk = getOrCreateChunk(cx, cy);
        return chunk.getBlock(lx, ly, wz);
    }

    public void setBlock(int wx, int wy, int wz, BlockType type) {
        if (wz < 0 || wz >= WorldChunk.CHUNK_SIZE_Z) {
            return;
        }
        int cx = Math.floorDiv(wx, WorldChunk.CHUNK_SIZE_X);
        int cy = Math.floorDiv(wy, WorldChunk.CHUNK_SIZE_Y);
        int lx = wx - cx * WorldChunk.CHUNK_SIZE_X;
        int ly = wy - cy * WorldChunk.CHUNK_SIZE_Y;

        WorldChunk chunk = getOrCreateChunk(cx, cy);
        chunk.setBlock(lx, ly, wz, type);
    }

    public int getTopSolidBlockZ(int wx, int wy) {
        for (int z = WorldChunk.CHUNK_SIZE_Z - 1; z >= 0; z--) {
            BlockType b = getBlock(wx, wy, z);
            if (b.solid) {
                return z;
            }
        }
        return 0;
    }

    public List<AABB3D> getCollidingBlocks(AABB3D box) {
        List<AABB3D> colliding = new ArrayList<>(8);
        int startX = (int) Math.floor(box.minX / WorldChunk.BLOCK_PIXEL_SIZE);
        int endX = (int) Math.floor((box.maxX - 0.001f) / WorldChunk.BLOCK_PIXEL_SIZE);
        int startY = (int) Math.floor(box.minY / WorldChunk.BLOCK_PIXEL_SIZE);
        int endY = (int) Math.floor((box.maxY - 0.001f) / WorldChunk.BLOCK_PIXEL_SIZE);
        int startZ = Math.max(0, (int) Math.floor(box.minZ / WorldChunk.BLOCK_PIXEL_SIZE));
        int endZ = Math.min(WorldChunk.CHUNK_SIZE_Z - 1, (int) Math.floor((box.maxZ - 0.001f) / WorldChunk.BLOCK_PIXEL_SIZE));

        for (int z = startZ; z <= endZ; z++) {
            for (int y = startY; y <= endY; y++) {
                for (int x = startX; x <= endX; x++) {
                    BlockType b = getBlock(x, y, z);
                    if (b.solid) {
                        float bs = WorldChunk.BLOCK_PIXEL_SIZE;
                        colliding.add(new AABB3D(x * bs, y * bs, z * bs,
                                                (x + 1) * bs, (y + 1) * bs, (z + 1) * bs));
                    }
                }
            }
        }
        return colliding;
    }

    public static class BlockHit {
        public final int blockX;
        public final int blockY;
        public final int blockZ;
        public final int faceNormalX;
        public final int faceNormalY;
        public final int faceNormalZ;
        public final BlockType blockType;

        public BlockHit(int bx, int by, int bz, int nx, int ny, int nz, BlockType bt) {
            this.blockX = bx;
            this.blockY = by;
            this.blockZ = bz;
            this.faceNormalX = nx;
            this.faceNormalY = ny;
            this.faceNormalZ = nz;
            this.blockType = bt;
        }
    }

    /**
     * Pick block at target ray.
     */
    public BlockHit raycast(float startX, float startY, float startZ,
                            float dirX, float dirY, float dirZ, float maxDist) {
        float stepSize = 4.0f;
        int steps = (int) (maxDist / stepSize);

        float curX = startX;
        float curY = startY;
        float curZ = startZ;

        int prevBx = -999999, prevBy = -999999, prevBz = -999999;

        for (int i = 0; i < steps; i++) {
            curX += dirX * stepSize;
            curY += dirY * stepSize;
            curZ += dirZ * stepSize;

            int bx = (int) Math.floor(curX / WorldChunk.BLOCK_PIXEL_SIZE);
            int by = (int) Math.floor(curY / WorldChunk.BLOCK_PIXEL_SIZE);
            int bz = (int) Math.floor(curZ / WorldChunk.BLOCK_PIXEL_SIZE);

            if (bz >= 0 && bz < WorldChunk.CHUNK_SIZE_Z) {
                BlockType b = getBlock(bx, by, bz);
                if (b.solid) {
                    int nx = 0, ny = 0, nz = 0;
                    if (prevBx != -999999) {
                        nx = prevBx - bx;
                        ny = prevBy - by;
                        nz = prevBz - bz;
                    } else {
                        nz = 1;
                    }
                    return new BlockHit(bx, by, bz, nx, ny, nz, b);
                }
            }
            prevBx = bx;
            prevBy = by;
            prevBz = bz;
        }
        return null;
    }
}
