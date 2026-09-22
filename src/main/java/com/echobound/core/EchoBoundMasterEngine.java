package com.echobound.core;

import com.echobound.audio.SoundType;
import com.echobound.companion.PetType;
import com.echobound.entity.mob.MobEntity;
import com.echobound.magic.MagicSchool;
import com.echobound.npc.NPCDefinition;
import com.echobound.physics3d.Vec3;
import com.echobound.pool.SpellProjectile;
import com.echobound.sandbox.EchoSandboxClone;
import com.echobound.sandbox.PixelSandboxRenderer;
import com.echobound.sandbox.SandboxHUD;
import com.echobound.sandbox.WorldChunk;
import com.echobound.save.SaveManager;
import com.echobound.settings.SettingsManager;
import com.echobound.ui.menu.GameState;
import com.echobound.ui.menu.MenuUIRenderer;
import com.echobound.ui.menu.TitleMenuController;
import com.echobound.ui.windows.WindowManager;

import java.awt.*;
import java.awt.event.*;
import com.echobound.core.Quality;

public class EchoBoundMasterEngine implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private static final double FIXED_DT = 1.0 / 60.0;
    private static final long TIME_STEP_NANOS = (long) (FIXED_DT * 1_000_000_000.0);

    private final Window window;
    private final UnifiedGameContext ctx;
    private final SaveManager saveManager;
    private final SettingsManager settingsManager;
    private final TitleMenuController menuController;
    private final WindowManager windowManager;
    private final PixelSandboxRenderer renderer;
    private final SandboxHUD hud;
    private final EchoSandboxClone echo;

    private boolean running = false;
    private Thread gameThread;

    // Camera
    private float camX = 0;
    private float camY = 0;

    // Keys state
    private final boolean[] keys = new boolean[512];
    private boolean jumpJustPressed = false;
    private boolean dashJustPressed = false;
    private boolean leftClickHeld = false;
    private boolean rightClickJustPressed = false;

    // Active Profile & Session
    private int currentSaveSlot = 1;
    private float sessionPlayTime = 0.0f;
    private NPCDefinition activeDialogueNPC = null;
    private int pauseMenuCursor = 0;

    public EchoBoundMasterEngine(Window window) {
        this.window = window;
        this.ctx = new UnifiedGameContext();
        this.saveManager = new SaveManager();
        this.settingsManager = new SettingsManager();
        this.menuController = new TitleMenuController(saveManager, settingsManager);
        this.windowManager = new WindowManager();
        this.renderer = new PixelSandboxRenderer();
        this.hud = new SandboxHUD();
        this.echo = new EchoSandboxClone();

        // Spawn player on surface
        int topZ = ctx.world.getTopSolidBlockZ(8, 8);
        ctx.player.pos.set(8 * WorldChunk.BLOCK_PIXEL_SIZE, 8 * WorldChunk.BLOCK_PIXEL_SIZE, (topZ + 1) * WorldChunk.BLOCK_PIXEL_SIZE);

        this.camX = ctx.player.pos.x - Window.INTERNAL_WIDTH * 0.5f;
        this.camY = ctx.player.pos.y - Window.INTERNAL_HEIGHT * 0.5f;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "EchoBound-MasterEngine");
        gameThread.start();
    }

    public synchronized void stop() {
        running = false;
    }

    @Override
    public void run() {
        long prevTime = System.nanoTime();
        double accumulator = 0.0;

        while (running) {
            long now = System.nanoTime();
            long elapsed = now - prevTime;
            prevTime = now;

            if (elapsed > 250_000_000L) {
                elapsed = 250_000_000L;
            }

            accumulator += elapsed / 1_000_000_000.0;

            while (accumulator >= FIXED_DT) {
                tick((float) FIXED_DT);
                accumulator -= FIXED_DT;
            }

            render();

            long sleepNanos = TIME_STEP_NANOS - (System.nanoTime() - now);
            if (sleepNanos > 0) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void tick(float dt) {
        GameState state = menuController.getCurrentState();

        if (state == GameState.LOADING || state == GameState.TITLE_MENU ||
            state == GameState.OPTIONS_MENU || state == GameState.SAVE_SELECT_MENU) {
            menuController.update(dt);
            return;
        }

        if (state == GameState.PAUSED) {
            return;
        }

        if (state == GameState.PLAYING) {
            sessionPlayTime += dt;
            windowManager.update(dt);

            if (windowManager.hasActiveWindow()) {
                jumpJustPressed = false;
                dashJustPressed = false;
                rightClickJustPressed = false;
                ctx.update(dt);
                return;
            }

            // Player Traversal Input
            boolean inLeft = keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT];
            boolean inRight = keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT];
            boolean inUp = keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP];
            boolean inDown = keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN];
            boolean jumpHeld = keys[KeyEvent.VK_SPACE];

            ctx.player.update(ctx.world, inLeft, inRight, inUp, inDown,
                              jumpJustPressed, jumpHeld, dashJustPressed,
                              leftClickHeld, rightClickJustPressed, dt);

            if (jumpJustPressed) {
                ctx.soundEngine.play(ctx.player.isGrounded() ? SoundType.JUMP : SoundType.DOUBLE_JUMP);
            }
            if (dashJustPressed) {
                ctx.soundEngine.play(SoundType.DASH);
            }

            jumpJustPressed = false;
            dashJustPressed = false;
            rightClickJustPressed = false;

            // Record frame if recording
            if (echo.isRecording) {
                echo.recordFrame(inLeft, inRight, inUp, inDown,
                                 jumpJustPressed, jumpHeld, dashJustPressed,
                                 leftClickHeld, rightClickJustPressed);
            }
            // Echo Replay
            echo.update(ctx.world, dt);

            // Update Master Game Context
            ctx.update(dt);

            // Camera Smooth Follow
            float targetCamX = ctx.player.pos.x - Window.INTERNAL_WIDTH * 0.5f;
            float targetCamY = ctx.player.pos.y - Window.INTERNAL_HEIGHT * 0.5f;
            camX += (targetCamX - camX) * 0.12f;
            camY += (targetCamY - camY) * 0.12f;
        }
    }

    private void render() {
        Graphics2D g = window.getBufferGraphics();
        GameState state = menuController.getCurrentState();

        if (state == GameState.LOADING || state == GameState.TITLE_MENU ||
            state == GameState.OPTIONS_MENU || state == GameState.SAVE_SELECT_MENU) {
            MenuUIRenderer.render(g, menuController, saveManager, settingsManager,
                                  Window.INTERNAL_WIDTH, Window.INTERNAL_HEIGHT);
            window.present();
            return;
        }

        // Render Voxel World & Traversal
        renderer.render(g, ctx.world, ctx.player, echo, ctx.dayNightCycle,
                        camX, camY, Window.INTERNAL_WIDTH, Window.INTERNAL_HEIGHT, Quality.HIGH);

        // Render Active Mobs
        for (MobEntity mob : ctx.mobManager.getActiveMobs()) {
            if (!mob.isAlive) continue;
            int sx = (int) (mob.position.x - camX);
            int sy = (int) (mob.position.y - mob.position.z * 0.75f - camY);

            // Draw mob sprite representation
            g.setColor(new Color(180, 40, 50));
            g.fillRect(sx - 5, sy - 10, 10, 10);

            // Health bar
            g.setColor(Color.RED);
            g.fillRect(sx - 8, sy - 14, 16, 2);
            g.setColor(Color.GREEN);
            int hpW = (int) (16.0f * ((float) mob.currentHealth / mob.type.maxHealth));
            g.fillRect(sx - 8, sy - 14, hpW, 2);
        }

        // Render Projectiles
        for (int i = 0; i < ctx.spellPool.getCapacity(); i++) {
            SpellProjectile p = ctx.spellPool.get(i);
            if (p != null && p.active) {
                p.render(g, camX, camY, 12.0f);
            }
        }

        // Render Particles & Floating Damage Numbers
        ctx.particleFXManager.render(g, camX, camY);
        ctx.floatingTextManager.render(g, camX, camY);

        // Render HUD
        hud.render(g, ctx.player, echo, ctx.dayNightCycle,
                   Window.INTERNAL_WIDTH, Window.INTERNAL_HEIGHT);

        // Render In-Game Modal Windows (Inventory, Crafting, Quest Log)
        if (windowManager.hasActiveWindow()) {
            windowManager.render(g, ctx, Window.INTERNAL_WIDTH, Window.INTERNAL_HEIGHT);
        }

        // Render NPC Dialogue prompt if near NPC
        if (activeDialogueNPC != null) {
            renderDialogueBox(g, activeDialogueNPC);
        }

        // Render Pause Menu if PAUSED
        if (state == GameState.PAUSED) {
            renderPauseMenu(g);
        }

        window.present();
    }

    private void renderDialogueBox(Graphics2D g, NPCDefinition npc) {
        int boxW = 440;
        int boxH = 60;
        int boxX = (Window.INTERNAL_WIDTH - boxW) / 2;
        int boxY = Window.INTERNAL_HEIGHT - 80;

        g.setColor(new Color(15, 20, 35, 220));
        g.fillRect(boxX, boxY, boxW, boxH);
        g.setColor(new Color(0, 240, 255));
        g.drawRect(boxX, boxY, boxW, boxH);

        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.setColor(new Color(255, 215, 0));
        g.drawString(npc.name + " (" + npc.faction.displayName + ")", boxX + 12, boxY + 18);

        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(Color.WHITE);
        g.drawString(npc.getDialogue("GREETING"), boxX + 12, boxY + 36);

        g.setFont(new Font("Monospaced", Font.ITALIC, 9));
        g.setColor(new Color(150, 160, 190));
        g.drawString("[F / ESC] Close Dialogue", boxX + 12, boxY + 52);
    }

    private void renderPauseMenu(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, 0, Window.INTERNAL_WIDTH, Window.INTERNAL_HEIGHT);

        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        String title = "GAME PAUSED";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (Window.INTERNAL_WIDTH - tw) / 2, 80);

        String[] options = {"Resume Game", "Save Game", "Return to Title"};
        g.setFont(new Font("Monospaced", Font.BOLD, 13));

        for (int i = 0; i < options.length; i++) {
            boolean selected = (i == pauseMenuCursor);
            g.setColor(selected ? new Color(0, 240, 255) : Color.WHITE);
            String text = (selected ? "> " : "  ") + options[i];
            int ow = g.getFontMetrics().stringWidth(text);
            g.drawString(text, (Window.INTERNAL_WIDTH - ow) / 2, 130 + i * 26);
        }
    }

    // Input Handlers
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) {
            keys[code] = true;
        }

        GameState state = menuController.getCurrentState();

        if (state == GameState.TITLE_MENU || state == GameState.OPTIONS_MENU || state == GameState.SAVE_SELECT_MENU) {
            if (code == KeyEvent.VK_UP) menuController.moveCursorUp();
            if (code == KeyEvent.VK_DOWN) menuController.moveCursorDown();
            if (code == KeyEvent.VK_LEFT) menuController.adjustOptionLeft();
            if (code == KeyEvent.VK_RIGHT) menuController.adjustOptionRight();
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                boolean valid = menuController.selectCurrent();
                if (valid && menuController.getCurrentState() == GameState.PLAYING) {
                    // Start or resume game
                    int slot = saveManager.getMostRecentSlot();
                    if (slot > 0) {
                        currentSaveSlot = slot;
                        saveManager.applySaveToContext(saveManager.loadData(slot), ctx);
                    }
                }
            }
            if (code == KeyEvent.VK_ESCAPE) {
                if (state == GameState.OPTIONS_MENU || state == GameState.SAVE_SELECT_MENU) {
                    menuController.setState(GameState.TITLE_MENU);
                }
            }
            return;
        }

        if (state == GameState.PAUSED) {
            if (code == KeyEvent.VK_UP) pauseMenuCursor = (pauseMenuCursor - 1 + 3) % 3;
            if (code == KeyEvent.VK_DOWN) pauseMenuCursor = (pauseMenuCursor + 1) % 3;
            if (code == KeyEvent.VK_ENTER) {
                if (pauseMenuCursor == 0) {
                    menuController.setState(GameState.PLAYING);
                } else if (pauseMenuCursor == 1) {
                    saveManager.save(currentSaveSlot, ctx, "Spark Runner", sessionPlayTime);
                    menuController.setState(GameState.PLAYING);
                } else if (pauseMenuCursor == 2) {
                    menuController.setState(GameState.TITLE_MENU);
                }
            }
            if (code == KeyEvent.VK_ESCAPE) {
                menuController.setState(GameState.PLAYING);
            }
            return;
        }

        if (state == GameState.PLAYING) {
            // First let active modal window or window hotkeys handle the key
            if (windowManager.handleKeyPress(code, ctx)) {
                return;
            }

            if (code == KeyEvent.VK_SPACE) jumpJustPressed = true;
            if (code == KeyEvent.VK_SHIFT) dashJustPressed = true;
            if (code == KeyEvent.VK_ESCAPE) {
                if (activeDialogueNPC != null) {
                    activeDialogueNPC = null;
                } else {
                    menuController.setState(GameState.PAUSED);
                    pauseMenuCursor = 0;
                }
            }
            // Mount Toggle
            if (code == KeyEvent.VK_M) {
                ctx.mountManager.toggleMount();
            }
            // Pet Cycle
            if (code == KeyEvent.VK_P) {
                PetType[] pets = PetType.values();
                int next = (ctx.petManager.getActivePet() == null) ? 0 :
                    (ctx.petManager.getActivePet().ordinal() + 1) % pets.length;
                ctx.petManager.tamePet(pets[next]);
                ctx.petManager.setActivePet(pets[next]);
            }
            // Weather Cycle
            if (code == KeyEvent.VK_T) {
                ctx.dayNightCycle.toggleWeather();
            }
            // Echo Loop Toggle
            if (code == KeyEvent.VK_X) {
                if (echo.isRecording) echo.stopRecording();
                else if (echo.isActive) echo.stopPlayback();
                else if (echo.hasRecordedData()) echo.startPlayback();
                else echo.startRecording(ctx.player);
            }
            // Cast Spells
            if (code == KeyEvent.VK_Q) {
                ctx.castDualSpell(MagicSchool.EMBER, MagicSchool.GALE, new Vec3(ctx.player.facingDirX, ctx.player.facingDirY, 0));
            }
            if (code == KeyEvent.VK_E) {
                ctx.castDualSpell(MagicSchool.TIDE, MagicSchool.VOLT, new Vec3(ctx.player.facingDirX, ctx.player.facingDirY, 0));
            }
            // Talk to NPC
            if (code == KeyEvent.VK_F) {
                if (activeDialogueNPC != null) {
                    activeDialogueNPC = null;
                } else {
                    for (NPCDefinition npc : ctx.npcManager.getAll()) {
                        float dist = (float) Math.hypot(npc.x - ctx.player.pos.x, npc.y - ctx.player.pos.y);
                        if (dist < 48.0f) {
                            activeDialogueNPC = npc;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) {
            keys[code] = false;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) leftClickHeld = true;
        if (e.getButton() == MouseEvent.BUTTON3) rightClickJustPressed = true;
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) leftClickHeld = false;
    }
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseWheelMoved(MouseWheelEvent e) {
        ctx.player.inventory.cycleSlot(e.getWheelRotation());
    }

    public UnifiedGameContext getContext() {
        return ctx;
    }

    public TitleMenuController getMenuController() {
        return menuController;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }
}
