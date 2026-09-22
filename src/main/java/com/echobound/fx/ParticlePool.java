package com.echobound.fx;

import java.awt.*;

public class ParticlePool {
    private final Particle[] pool;
    private final int capacity;
    private int nextIndex = 0;

    public ParticlePool(int capacity) {
        this.capacity = capacity;
        this.pool = new Particle[capacity];
        for (int i = 0; i < capacity; i++) {
            pool[i] = new Particle();
        }
    }

    public void spawn(float x, float y, float vx, float vy, float life, float size, Color color) {
        Particle p = pool[nextIndex];
        p.init(x, y, vx, vy, life, size, color);
        nextIndex = (nextIndex + 1) % capacity;
    }

    public void spawnBurst(float x, float y, int count, float speed, float life, float size, Color color) {
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0 * i) / count + (Math.random() * 0.2 - 0.1);
            float vx = (float) (Math.cos(angle) * speed * (0.5 + Math.random() * 0.5));
            float vy = (float) (Math.sin(angle) * speed * (0.5 + Math.random() * 0.5));
            spawn(x, y, vx, vy, life, size, color);
        }
    }

    public void spawnDust(float x, float y, int count, Color color) {
        for (int i = 0; i < count; i++) {
            float vx = (float) ((Math.random() - 0.5) * 60.0);
            float vy = (float) (-Math.random() * 35.0 - 5.0);
            float life = 0.2f + (float) (Math.random() * 0.15);
            float size = 2.0f + (float) (Math.random() * 2.0);
            spawn(x, y, vx, vy, life, size, color);
        }
    }

    public void update(float dt) {
        for (int i = 0; i < capacity; i++) {
            if (pool[i].active) {
                pool[i].update(dt);
            }
        }
    }

    public void render(Graphics2D g, float camX, float camY) {
        for (int i = 0; i < capacity; i++) {
            if (pool[i].active) {
                pool[i].render(g, camX, camY);
            }
        }
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            pool[i].active = false;
        }
    }
}
