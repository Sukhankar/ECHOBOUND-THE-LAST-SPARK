package com.echobound.fx;

import java.awt.*;

public class FloatingText {
    public String text = "";
    public float x, y, z;
    public float vz = 25.0f;
    public Color color = Color.WHITE;
    public boolean isCrit = false;
    public float lifetime = 0.0f;
    public float maxLifetime = 0.8f;
    public boolean active = false;

    public void spawn(String text, float x, float y, float z, Color color, boolean isCrit, float maxLifetime) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.z = z;
        this.vz = isCrit ? 35.0f : 25.0f;
        this.color = color;
        this.isCrit = isCrit;
        this.maxLifetime = maxLifetime;
        this.lifetime = 0.0f;
        this.active = true;
    }

    public void update(float dt) {
        if (!active) return;
        lifetime += dt;
        if (lifetime >= maxLifetime) {
            active = false;
            return;
        }
        z += vz * dt;
        vz *= (1.0f - dt * 1.5f); // Gentle upward decelerate
    }
}
