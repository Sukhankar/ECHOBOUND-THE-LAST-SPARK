package com.echobound.fx;

import com.echobound.magic.MagicSchool;

import java.awt.*;
import java.util.Random;

public class ParticleFXManager {
    public static final int MAX_PARTICLES = 256;
    private final PixelParticle[] pool = new PixelParticle[MAX_PARTICLES];
    private final Random random = new Random(42);

    public ParticleFXManager() {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            pool[i] = new PixelParticle();
        }
    }

    private PixelParticle findFreeParticle() {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (!pool[i].active) {
                return pool[i];
            }
        }
        return pool[0]; // Recycle oldest if full
    }

    public void spawnBurst(float x, float y, float z, Color color, int count, float speed) {
        for (int i = 0; i < count; i++) {
            PixelParticle p = findFreeParticle();
            float angle = (float) (random.nextDouble() * Math.PI * 2.0);
            float vel = speed * (0.5f + random.nextFloat() * 0.8f);
            float vx = (float) Math.cos(angle) * vel;
            float vy = (float) Math.sin(angle) * vel;
            float vz = 30.0f + random.nextFloat() * 80.0f;
            int size = 1 + random.nextInt(2);
            float life = 0.3f + random.nextFloat() * 0.4f;
            p.spawn(x, y, z, vx, vy, vz, color, size, life, -120.0f);
        }
    }

    public void spawnElementalBurst(float x, float y, float z, MagicSchool school, int count) {
        Color c;
        switch (school) {
            case EMBER: c = new Color(255, 90, 20); break;
            case TIDE:  c = new Color(50, 160, 255); break;
            case GALE:  c = new Color(200, 250, 220); break;
            case TERRA: c = new Color(160, 110, 60); break;
            case VOLT:  c = new Color(255, 240, 50); break;
            case BLOOM: c = new Color(70, 220, 90); break;
            case VOID:  c = new Color(160, 40, 220); break;
            case ECHO:  c = new Color(0, 240, 255); break;
            default:    c = Color.YELLOW; break;
        }
        spawnBurst(x, y, z, c, count, 90.0f);
    }

    public void spawnBlockDebris(float x, float y, float z, Color blockColor, int count) {
        spawnBurst(x, y, z, blockColor, count, 60.0f);
    }

    public void spawnFishingRipples(float x, float y, float z) {
        spawnBurst(x, y, z, new Color(120, 210, 255), 8, 35.0f);
    }

    public void spawnLootSparkles(float x, float y, float z) {
        spawnBurst(x, y, z, new Color(255, 220, 60), 16, 75.0f);
    }

    public void update(float dt) {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (pool[i].active) {
                pool[i].update(dt);
            }
        }
    }

    public void render(Graphics2D g, float camX, float camY) {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            PixelParticle p = pool[i];
            if (!p.active) continue;

            int screenX = Math.round(p.x - camX);
            int screenY = Math.round(p.y - camY - p.z * 0.5f); // 3D projection

            float alpha = Math.max(0.0f, Math.min(1.0f, 1.0f - (p.life / p.maxLife)));
            Color c = new Color(
                p.color.getRed() / 255.0f,
                p.color.getGreen() / 255.0f,
                p.color.getBlue() / 255.0f,
                alpha
            );
            g.setColor(c);
            g.fillRect(screenX, screenY, p.size, p.size);
        }
    }

    public int getActiveCount() {
        int count = 0;
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (pool[i].active) count++;
        }
        return count;
    }

    public void clear() {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            pool[i].active = false;
        }
    }
}
