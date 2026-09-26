package com.echobound.fx;

import com.echobound.magic.MagicSchool;
import com.echobound.sandbox.WeatherType;

import java.awt.*;
import java.util.Random;

public class ParticleFXManager {
    public static final int MAX_PARTICLES = 256;
    private final PixelParticle[] pool = new PixelParticle[MAX_PARTICLES];
    private final Random random = new Random(42);
    private float weatherSpawnAccumulator = 0.0f;

    // ResolutionProfile.maxParticles (Pixel Saver=20 / Standard=60 / Plus=150) was defined
    // but nothing ever read it — the Options-menu "Resolution Scale" setting had zero effect
    // on anything. This is the one real, safe knob that setting can pull: a lower-end profile
    // now genuinely caps simultaneous on-screen particles instead of always maxing out at 256.
    private int activeLimit = MAX_PARTICLES;

    public void setActiveLimit(int limit) {
        this.activeLimit = Math.max(1, Math.min(MAX_PARTICLES, limit));
    }

    public ParticleFXManager() {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            pool[i] = new PixelParticle();
        }
    }

    private PixelParticle findFreeParticle() {
        int active = 0;
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (pool[i].active) active++;
        }
        if (active < activeLimit) {
            for (int i = 0; i < MAX_PARTICLES; i++) {
                if (!pool[i].active) return pool[i];
            }
        }
        return pool[0]; // At the configured cap, or pool exhausted — recycle oldest.
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

    /**
     * Ambient weather previously only dimmed the screen (see DayNightCycle.brightnessMult) —
     * it had no actual visible precipitation. This spawns real falling particles around the
     * player using the same pool/physics every other effect in this class already uses, so
     * rain/storm/snow finally read as weather instead of just a lighting multiplier.
     */
    public void updateWeather(float dt, float centerX, float centerY, WeatherType weather) {
        boolean rain = (weather == WeatherType.RAIN || weather == WeatherType.STORM);
        boolean snow = (weather == WeatherType.SNOW);
        if (!rain && !snow) {
            weatherSpawnAccumulator = 0.0f;
            return;
        }

        float spawnInterval = (weather == WeatherType.STORM) ? 0.012f : (rain ? 0.028f : 0.05f);
        weatherSpawnAccumulator += dt;
        int guard = 0;
        while (weatherSpawnAccumulator >= spawnInterval && guard++ < 40) {
            weatherSpawnAccumulator -= spawnInterval;
            float ox = centerX + (random.nextFloat() - 0.5f) * 380.0f;
            float oy = centerY + (random.nextFloat() - 0.5f) * 240.0f;
            if (rain) {
                spawnRaindrop(ox, oy, weather == WeatherType.STORM);
            } else {
                spawnSnowflake(ox, oy);
            }
        }
    }

    // Fall height (z=130) and each gravity/life pair are tuned so z reaches ~0 (ground) right
    // around maxLife — z(t) = z0 + 0.5*gravity*t^2 with vz0=0 — instead of the particle fading
    // out mid-air or sinking visibly below the ground line before it expires.
    private void spawnRaindrop(float x, float y, boolean storm) {
        PixelParticle p = findFreeParticle();
        float vx = storm ? -60.0f : -25.0f;
        p.spawn(x, y, 130.0f, vx, 0.0f, 0.0f,
                new Color(150, 190, 255, 190), 1, storm ? 0.53f : 0.62f, storm ? -950.0f : -700.0f);
    }

    private void spawnSnowflake(float x, float y) {
        PixelParticle p = findFreeParticle();
        float vx = (random.nextFloat() - 0.5f) * 14.0f;
        p.spawn(x, y, 130.0f, vx, 0.0f, 0.0f,
                new Color(255, 255, 255, 220), 1, 2.15f, -55.0f);
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
