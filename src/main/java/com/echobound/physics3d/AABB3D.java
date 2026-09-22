package com.echobound.physics3d;

public class AABB3D {
    public float minX;
    public float minY;
    public float minZ;
    public float maxX;
    public float maxY;
    public float maxZ;

    public AABB3D() {
        this(0, 0, 0, 0, 0, 0);
    }

    public AABB3D(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        set(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void set(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public void setFromPositionSize(float x, float y, float z, float widthX, float widthY, float heightZ) {
        this.minX = x;
        this.minY = y;
        this.minZ = z;
        this.maxX = x + widthX;
        this.maxY = y + widthY;
        this.maxZ = z + heightZ;
    }

    public boolean overlaps(AABB3D other) {
        return this.minX < other.maxX && this.maxX > other.minX &&
               this.minY < other.maxY && this.maxY > other.minY &&
               this.minZ < other.maxZ && this.maxZ > other.minZ;
    }

    public boolean contains(float px, float py, float pz) {
        return px >= minX && px <= maxX &&
               py >= minY && py <= maxY &&
               pz >= minZ && pz <= maxZ;
    }

    public float getWidthX() {
        return maxX - minX;
    }

    public float getWidthY() {
        return maxY - minY;
    }

    public float getHeightZ() {
        return maxZ - minZ;
    }

    public float getCenterX() {
        return (minX + maxX) * 0.5f;
    }

    public float getCenterY() {
        return (minY + maxY) * 0.5f;
    }

    public float getCenterZ() {
        return (minZ + maxZ) * 0.5f;
    }
}
