package com.echobound.fx;

import java.awt.*;

public class Particle {
    public boolean active = false;
    public float x;
    public float y;
    public float vx;
    public float vy;
    public float life;
    public float maxLife;
    public float size;
    public Color color;

    public void init(float x, float y, float vx, float vy, float life, float size, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        this.maxLife = life;
        this.size = size;
        this.color = color;
        this.active = true;
    }

    public void update(float dt) {
        if (!active) return;
        x += vx * dt;
        y += vy * dt;
        life -= dt;
        if (life <= 0) {
            active = false;
        }
    }

    public void render(Graphics2D g, float camX, float camY) {
        if (!active) return;
        float progress = Math.max(0.0f, life / maxLife);
        int alpha = (int) (progress * 255);
        if (alpha <= 0) return;

        int px = (int) (x - camX - size * 0.5f);
        int py = (int) (y - camY - size * 0.5f);
        int s = Math.max(1, (int) (size * progress));

        Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        g.setColor(c);
        g.fillRect(px, py, s, s);
    }
}
