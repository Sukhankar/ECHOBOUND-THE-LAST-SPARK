package com.echobound.core;

import com.echobound.entity.Runner;
import com.echobound.fx.Camera;
import com.echobound.fx.ParticlePool;
import com.echobound.fx.ScreenShake;
import com.echobound.physics.PhysicsConfig;
import com.echobound.ui.DebugOverlay;
import com.echobound.world.LevelData;
import com.echobound.world.TileMap;

import java.awt.*;

public class GameEngine implements Runnable {
    private static final double TIME_STEP = PhysicsConfig.FIXED_DT; // 1/60th second
    private static final long TIME_STEP_NANOS = (long) (TIME_STEP * 1_000_000_000.0);
    private static final int MAX_CATCH_UP_TICKS = 5; // Spiral of death prevention

    private final Window window;
    private final Input input;
    private final PlayerInputSource playerInputSource;

    private boolean running = false;
    private Thread gameThread;

    // Simulation components
    private final TileMap currentMap;
    private final Runner player;
    private final Camera camera;
    private final ScreenShake screenShake;
    private final ParticlePool particlePool;
    private final DebugOverlay debugOverlay;

    // Settings
    private Quality quality = Quality.HIGH;

    // Performance metrics
    private int currentFps = 0;
    private int currentUps = 0;
    private long tickCounter = 0;

    public GameEngine(Window window, Input input) {
        this.window = window;
        this.input = input;

        // Initialize world and systems
        this.currentMap = TileMap.fromAscii(LevelData.TEST_ROOM);
        this.screenShake = new ScreenShake();
        this.camera = new Camera(screenShake);
        this.camera.setBounds(0, 0, currentMap.getPixelWidth(), currentMap.getPixelHeight());

        this.particlePool = new ParticlePool(400);
        this.playerInputSource = new PlayerInputSource(input);
        this.player = new Runner(currentMap.getSpawnX(), currentMap.getSpawnY(), playerInputSource, particlePool);
        this.camera.snapTo(player.x, player.y);

        this.debugOverlay = new DebugOverlay();
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "EchoBound-GameLoop");
        gameThread.start();
    }

    public synchronized void stop() {
        running = false;
    }

    @Override
    public void run() {
        long prevTime = System.nanoTime();
        long accumulator = 0;

        long secondTimer = System.currentTimeMillis();
        int frames = 0;
        int updates = 0;

        while (running) {
            long now = System.nanoTime();
            long elapsed = now - prevTime;
            prevTime = now;

            // Cap elapsed to avoid massive jump after pause/debug break
            if (elapsed > 250_000_000L) {
                elapsed = 250_000_000L;
            }

            accumulator += elapsed;

            // Catch-up guard
            int ticksThisFrame = 0;
            while (accumulator >= TIME_STEP_NANOS && ticksThisFrame < MAX_CATCH_UP_TICKS) {
                // Fixed deterministic simulation tick
                input.tick();
                handleGlobalHotkeys();
                tickSimulation();

                accumulator -= TIME_STEP_NANOS;
                ticksThisFrame++;
                updates++;
                tickCounter++;
            }

            // Prevent accumulator overflow if simulation falls behind
            if (accumulator > TIME_STEP_NANOS * MAX_CATCH_UP_TICKS) {
                accumulator = 0;
            }

            // Interpolation factor between ticks [0.0, 1.0]
            float alpha = (float) accumulator / TIME_STEP_NANOS;
            alpha = Math.max(0.0f, Math.min(1.0f, alpha));

            // Render interpolated frame
            render(alpha);
            window.present();
            frames++;

            // Measure FPS and UPS once every second
            long currentMillis = System.currentTimeMillis();
            if (currentMillis - secondTimer >= 1000) {
                currentFps = frames;
                currentUps = updates;
                frames = 0;
                updates = 0;
                secondTimer += 1000;
            }

            // Yield slightly to allow OS and AWT events to process smoothly
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void handleGlobalHotkeys() {
        if (input.pollF1()) {
            debugOverlay.toggle();
        }
        if (input.pollF2()) {
            quality = quality.next();
        }
        if (input.pollF3()) {
            screenShake.toggle();
        }
        if (input.pollF11()) {
            window.toggleFullscreen();
        }
        if (input.pollRetry()) {
            player.reset(currentMap.getSpawnX(), currentMap.getSpawnY());
            camera.snapTo(player.x, player.y);
            particlePool.clear();
        }
    }

    private void tickSimulation() {
        float dt = PhysicsConfig.FIXED_DT;

        // Update physics & player
        player.tick(currentMap);

        // Update particles
        particlePool.update(dt);

        // Update camera smoothly tracking player
        camera.update(player.x + PhysicsConfig.HITBOX_WIDTH * 0.5f,
                      player.y + PhysicsConfig.HITBOX_HEIGHT * 0.5f,
                      player.facing, dt);

        // Update screen shake decay
        screenShake.update(dt);
    }

    private void render(float alpha) {
        Graphics2D g = window.getBufferGraphics();

        float camX = camera.getRenderX();
        float camY = camera.getRenderY();
        int viewW = Window.INTERNAL_WIDTH;
        int viewH = Window.INTERNAL_HEIGHT;

        // 1. Procedural Sci-Fi Atmospheric Parallax Background
        renderAtmosphere(g, camX, camY, viewW, viewH);

        // 2. Tile Map rendering
        currentMap.render(g, camX, camY, viewW, viewH);

        // 3. Particles
        particlePool.render(g, camX, camY);

        // 4. Runner (Rin) with render interpolation
        player.render(g, camX, camY, alpha, quality);

        // 5. World Tutorial Hint Signs (M1 testing guide)
        renderTutorialSignposts(g, camX, camY);

        // 6. Debug Overlay
        debugOverlay.render(g, player, currentMap, camX, camY,
                            currentFps, currentUps, tickCounter, quality, screenShake);
    }

    private void renderAtmosphere(Graphics2D g, float camX, float camY, int viewW, int viewH) {
        // Sky gradient: twilight indigo to warm dusk amber horizon
        GradientPaint skyGrad = new GradientPaint(
            0, 0, new Color(12, 16, 28),
            0, viewH, new Color(24, 30, 48)
        );
        g.setPaint(skyGrad);
        g.fillRect(0, 0, viewW, viewH);

        // Parallax Layer 1: Distant geometric mountain ridges (0.1x speed)
        g.setColor(new Color(18, 22, 38));
        int baseY = viewH - 80;
        int[] ptsX = new int[7];
        int[] ptsY = new int[7];
        float px1 = (camX * 0.08f) % 200;
        for (int i = 0; i < 7; i++) {
            ptsX[i] = (int) (i * 120 - px1);
            ptsY[i] = baseY + ((i % 2 == 0) ? -45 : -20);
        }
        g.fillPolygon(new int[]{0, ptsX[0], ptsX[1], ptsX[2], ptsX[3], ptsX[4], ptsX[5], ptsX[6], viewW, viewW, 0},
                      new int[]{viewH, ptsY[0], ptsY[1], ptsY[2], ptsY[3], ptsY[4], ptsY[5], ptsY[6], ptsY[6], viewH, viewH}, 11);

        // Parallax Layer 2: Silhouetted Resonance Towers (0.25x speed)
        if (quality != Quality.LOW) {
            g.setColor(new Color(28, 36, 56));
            float px2 = (camX * 0.2f) % 300;
            for (int i = -1; i < 4; i++) {
                int tx = (int) (i * 240 - px2);
                int tw = 28;
                g.fillRect(tx, viewH - 180, tw, 180);
                // Tower top antenna
                g.fillRect(tx + 12, viewH - 210, 4, 30);
                // Tower beacon glow
                g.setColor(new Color(0, 240, 255, 90));
                g.fillOval(tx + 11, viewH - 213, 6, 6);
                g.setColor(new Color(28, 36, 56));
            }
        }
    }

    private void renderTutorialSignposts(Graphics2D g, float camX, float camY) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));

        // Sign 1: Controls at spawn
        drawSign(g, "MOVE: A/D or ARROWS | JUMP: SPACE/Z", 64, 305, camX, camY);
        drawSign(g, "HOLD JUMP = HIGH | TAP = SHORT", 64, 318, camX, camY);

        // Sign 2: Double jump
        drawSign(g, "DOUBLE JUMP IN AIR", 260, 250, camX, camY);

        // Sign 3: Dash gap
        drawSign(g, "DASH (C / SHIFT) ACROSS HAZARD", 430, 310, camX, camY);

        // Sign 4: Wall Jump Shaft
        drawSign(g, "WALL SLIDE + WALL JUMP (ALT WALLS)", 710, 220, camX, camY);

        // Sign 5: One-way platforms
        drawSign(g, "ONE-WAY: JUMP THROUGH / DOWN+JUMP TO DROP", 260, 160, camX, camY);
    }

    private void drawSign(Graphics2D g, String text, float wx, float wy, float camX, float camY) {
        int sx = (int) (wx - camX);
        int sy = (int) (wy - camY);
        if (sx < -200 || sx > Window.INTERNAL_WIDTH + 50) return;

        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);

        g.setColor(new Color(15, 20, 30, 180));
        g.fillRoundRect(sx - 4, sy - fm.getAscent() - 2, tw + 8, fm.getHeight() + 4, 4, 4);
        g.setColor(new Color(0, 240, 255, 120));
        g.drawRoundRect(sx - 4, sy - fm.getAscent() - 2, tw + 8, fm.getHeight() + 4, 4, 4);
        g.setColor(new Color(220, 240, 255));
        g.drawString(text, sx, sy);
    }
}
