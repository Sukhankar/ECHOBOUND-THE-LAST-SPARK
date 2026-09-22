package com.echobound.fx;

import java.awt.*;

public class PixelParticle {
    public float x, y, z;
    public float vx, vy, vz;
    public float gravity = -180.0f;
    public int size = 2;
    public Color color = Color.WHITE;
    public float life = 0.0f;
    public float maxLife = 0.6f;
    public boolean active = false;

    public void spawn(float x, float y, float z, float vx, float vy, float vz,
                      Color color, int size, float maxLife, float gravity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.color = color;
        this.size = size;
        this.maxLife = maxLife;
        this.life = 0.0f;
        this.gravity = gravity;
        this.active = true;
    }

    public void update(float dt) {
        if (!active) return;
        life += dt;
        if (life >= maxLife) {
            active = false;
            return;
        }
        vz += gravity * dt;
        x += vx * dt;
        y += vy * dt;
        z += vz * dt;
    }
}
