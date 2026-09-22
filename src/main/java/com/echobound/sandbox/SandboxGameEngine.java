package com.echobound.sandbox;

import com.echobound.core.Quality;
import com.echobound.core.Window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SandboxGameEngine implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private static final double FIXED_DT = 1.0 / 60.0;
    private static final long TIME_STEP_NANOS = (long) (FIXED_DT * 1_000_000_000.0);

    private final Window window;
    private final SandboxWorld world;
    private final PlayerSandboxEntity player;
    private final EchoSandboxClone echo;
    private final DayNightCycle dayNight;
    private final PixelSandboxRenderer renderer;
    private final SandboxHUD hud;

    private boolean running = false;
    private Thread gameThread;

    // Camera
    private float camX = 0;
    private float camY = 0;

    // Raw input tracking
    private final boolean[] keys = new boolean[512];
    private boolean jumpJustPressed = false;
    private boolean dashJustPressed = false;
    private boolean placeJustPressed = false;
    private boolean mineHeld = false;
    private boolean leftClickHeld = false;
    private boolean rightClickJustPressed = false;

    public SandboxGameEngine(Window window) {
        this.window = window;
        this.world = new SandboxWorld(133742L);

        // Spawn player in center of chunk 0 at top solid ground
        int spawnBlockX = 8;
        int spawnBlockY = 8;
        int topZ = world.getTopSolidBlockZ(spawnBlockX, spawnBlockY);
        float spawnPixelX = spawnBlockX * WorldChunk.BLOCK_PIXEL_SIZE;
        float spawnPixelY = spawnBlockY * WorldChunk.BLOCK_PIXEL_SIZE;
        float spawnPixelZ = (topZ + 1) * WorldChunk.BLOCK_PIXEL_SIZE;

        this.player = new PlayerSandboxEntity(spawnPixelX, spawnPixelY, spawnPixelZ);
        this.echo = new EchoSandboxClone();
        this.dayNight = new DayNightCycle();
        this.renderer = new PixelSandboxRenderer();
        this.hud = new SandboxHUD();

        this.camX = player.pos.x - Window.INTERNAL_WIDTH * 0.5f;
        this.camY = player.pos.y - Window.INTERNAL_HEIGHT * 0.5f;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "EchoBound-SandboxEngine");
        gameThread.start();
    }

    public synchronized void stop() {
        running = false;
    }

    @Override
    public void run() {
        long prevTime = System.nanoTime();
        long accumulator = 0;

        while (running) {
            long now = System.nanoTime();
            long elapsed = now - prevTime;
            prevTime = now;
            if (elapsed > 250_000_000L) elapsed = 250_000_000L;
            accumulator += elapsed;

            while (accumulator >= TIME_STEP_NANOS) {
                tickSimulation();
                accumulator -= TIME_STEP_NANOS;
            }

            render();
            window.present();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private synchronized void tickSimulation() {
        float dt = (float) FIXED_DT;

        boolean inLeft = keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT];
        boolean inRight = keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT];
        boolean inUp = keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP];
        boolean inDown = keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN];
        boolean jumpHeld = keys[KeyEvent.VK_SPACE];
        boolean isMining = mineHeld || leftClickHeld || keys[KeyEvent.VK_F];
        boolean isPlacing = placeJustPressed || rightClickJustPressed || keys[KeyEvent.VK_G];

        // Consume one-shot triggers
        boolean doJump = jumpJustPressed;
        boolean doDash = dashJustPressed;
        boolean doPlace = isPlacing;
        jumpJustPressed = false;
        dashJustPressed = false;
        placeJustPressed = false;
        rightClickJustPressed = false;

        // Record for Echo if recording
        if (echo.isRecording) {
            echo.recordFrame(inLeft, inRight, inUp, inDown, doJump, jumpHeld, doDash, isMining, doPlace);
        }

        // Update player
        player.update(world, inLeft, inRight, inUp, inDown, doJump, jumpHeld, doDash, isMining, doPlace, dt);

        // Update Echo clone replay
        echo.update(world, dt);

        // Update Day / Night cycle
        dayNight.update(dt);

        // Smooth camera follow
        float targetCamX = player.pos.x - Window.INTERNAL_WIDTH * 0.5f;
        float targetCamY = player.pos.y - (player.pos.z * (PixelSandboxRenderer.Z_ELEVATION_PX / WorldChunk.BLOCK_PIXEL_SIZE)) - Window.INTERNAL_HEIGHT * 0.5f;
        camX += (targetCamX - camX) * Math.min(1.0f, dt * 6.0f);
        camY += (targetCamY - camY) * Math.min(1.0f, dt * 6.0f);
    }

    private void render() {
        Graphics2D g = window.getBufferGraphics();
        int viewW = Window.INTERNAL_WIDTH;
        int viewH = Window.INTERNAL_HEIGHT;

        // Background void color
        g.setColor(new Color(18, 22, 34));
        g.fillRect(0, 0, viewW, viewH);

        // World, Entities, Lighting
        renderer.render(g, world, player, echo, dayNight, camX, camY, viewW, viewH, Quality.HIGH);

        // Arcade HUD & Hotbar
        hud.render(g, player, echo, dayNight, viewW, viewH);
    }

    // Input handlers
    @Override
    public synchronized void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) keys[code] = true;

        if (code == KeyEvent.VK_SPACE) jumpJustPressed = true;
        if (code == KeyEvent.VK_C || code == KeyEvent.VK_SHIFT) dashJustPressed = true;
        if (code == KeyEvent.VK_G) placeJustPressed = true;
        if (code == KeyEvent.VK_F) mineHeld = true;

        // Hotbar selection 1..8
        if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_8) {
            player.inventory.setSelectedSlot(code - KeyEvent.VK_1);
        }

        // Echo Shift controls
        if (code == KeyEvent.VK_E) {
            if (echo.isRecording) {
                echo.stopRecording();
            } else {
                echo.startRecording(player);
            }
        }
        if (code == KeyEvent.VK_Q) {
            echo.deploy();
        }

        // Weather toggle
        if (code == KeyEvent.VK_T) {
            dayNight.toggleWeather();
        }

        // Fullscreen
        if (code == KeyEvent.VK_F11) {
            window.toggleFullscreen();
        }
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) keys[code] = false;
        if (code == KeyEvent.VK_F) mineHeld = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public synchronized void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            leftClickHeld = true;
        } else if (SwingUtilities.isRightMouseButton(e)) {
            rightClickJustPressed = true;
        }
    }

    @Override
    public synchronized void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            leftClickHeld = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseDragged(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public synchronized void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() > 0) {
            player.inventory.selectNext();
        } else if (e.getWheelRotation() < 0) {
            player.inventory.selectPrev();
        }
    }
}
