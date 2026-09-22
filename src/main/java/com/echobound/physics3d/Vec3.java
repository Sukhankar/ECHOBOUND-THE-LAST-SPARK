package com.echobound.physics3d;

public class Vec3 {
    public float x;
    public float y;
    public float z;

    public Vec3() {
        this(0, 0, 0);
    }

    public Vec3(float x, float y, float z) {
        set(x, y, z);
    }

    public Vec3(Vec3 other) {
        set(other.x, other.y, other.z);
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vec3 other) {
        set(other.x, other.y, other.z);
    }

    public void add(float dx, float dy, float dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }

    public void add(Vec3 other) {
        add(other.x, other.y, other.z);
    }

    public void sub(Vec3 other) {
        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
    }

    public Vec3 subtract(Vec3 other) {
        return new Vec3(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vec3 normalized() {
        Vec3 v = new Vec3(this);
        v.normalize();
        return v;
    }

    public void mul(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
    }

    public float lengthSq() {
        return x * x + y * y + z * z;
    }

    public float length() {
        return (float) Math.sqrt(lengthSq());
    }

    public float length2DSq() {
        return x * x + y * y;
    }

    public float length2D() {
        return (float) Math.sqrt(length2DSq());
    }

    public void normalize() {
        float len = length();
        if (len > 0.00001f) {
            mul(1.0f / len);
        }
    }

    public void normalize2D() {
        float len = length2D();
        if (len > 0.00001f) {
            this.x /= len;
            this.y /= len;
        }
    }

    public float distanceTo(Vec3 other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        float dz = this.z - other.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float distance2D(Vec3 other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static Vec3 lerp(Vec3 a, Vec3 b, float t) {
        return new Vec3(
            a.x + (b.x - a.x) * t,
            a.y + (b.y - a.y) * t,
            a.z + (b.z - a.z) * t
        );
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}
