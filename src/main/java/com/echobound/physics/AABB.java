package com.echobound.physics;

public class AABB {
    public float x;
    public float y;
    public float width;
    public float height;

    public AABB() {
        this(0, 0, 0, 0);
    }

    public AABB(float x, float y, float width, float height) {
        set(x, y, width, height);
    }

    public void set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getLeft() {
        return x;
    }

    public float getRight() {
        return x + width;
    }

    public float getTop() {
        return y;
    }

    public float getBottom() {
        return y + height;
    }

    public float getCenterX() {
        return x + width * 0.5f;
    }

    public float getCenterY() {
        return y + height * 0.5f;
    }

    public boolean overlaps(AABB other) {
        return this.getLeft() < other.getRight() &&
               this.getRight() > other.getLeft() &&
               this.getTop() < other.getBottom() &&
               this.getBottom() > other.getTop();
    }

    public boolean overlaps(float ox, float oy, float ow, float oh) {
        return this.getLeft() < ox + ow &&
               this.getRight() > ox &&
               this.getTop() < oy + oh &&
               this.getBottom() > oy;
    }

    public boolean contains(float px, float py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}
