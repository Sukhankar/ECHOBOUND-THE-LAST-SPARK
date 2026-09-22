package com.echobound.pool;

import com.echobound.magic.Spell;

import java.awt.*;

public class SpellProjectile {
    public boolean active = false;
    public float x;
    public float y;
    public float z;
    public float vx;
    public float vy;
    public float vz;
    public float life;
    public float maxLife;
    public int damage;
    public Spell spell;
    public Color color;

    public void init(float x, float y, float z, float vx, float vy, float vz,
                     float life, int damage, Spell spell, Color color) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.life = life;
        this.maxLife = life;
        this.damage = damage;
        this.spell = spell;
        this.color = color;
        this.active = true;
    }

    public void update(float dt) {
        if (!active) return;
        x += vx * dt;
        y += vy * dt;
        z += vz * dt;
        life -= dt;
        if (life <= 0) {
            active = false;
        }
    }

    public void render(Graphics2D g, float camX, float camY, float elevationPx) {
        if (!active) return;
        int sx = (int) (x - camX);
        int sy = (int) (y - z * (elevationPx / 16.0f) - camY);

        g.setColor(color);
        g.fillOval(sx - 3, sy - 3, 6, 6);
        g.setColor(Color.WHITE);
        g.fillOval(sx - 1, sy - 1, 2, 2);
    }
}
