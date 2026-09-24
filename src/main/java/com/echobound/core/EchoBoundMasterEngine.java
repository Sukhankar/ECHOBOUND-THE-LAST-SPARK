package com.echobound.core;

import com.echobound.animation.AnimationController;
import com.echobound.animation.AnimationState;
import com.echobound.animation.NPCVisualController;
import com.echobound.assets.AssetManager;
import com.echobound.audio.SoundType;
import com.echobound.companion.PetType;
import com.echobound.entity.mob.MobEntity;
import com.echobound.entity.mob.MobType;
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
import com.echobound.tutorial.TutorialStep;
import com.echobound.ui.dev.AnimationViewerOverlay;
import com.echobound.ui.dev.PerformanceDebugOverlay;
import com.echobound.ui.menu.GameState;
import com.echobound.ui.menu.MenuUIRenderer;
import com.echobound.ui.menu.TitleMenuController;
import com.echobound.ui.windows.InGameWindowType;
import com.echobound.ui.windows.WindowManager;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EchoBoundMasterEngine implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private static final double FIXED_DT = 1.0 / 60.0;
    private static final long TIME_STEP_NANOS = (long) (FIXED_DT * 1_000_000_000.0);

    private final Window window;
    private final UnifiedGameContext ctx;
    private final SaveManager saveManager;
    private final SettingsManager settingsManager;
    private final TitleMenuController menuController;
    private final WindowManager windowManager;
    private final AssetManager assetManager;
    private final PixelSandboxRenderer renderer;
    private final SandboxHUD hud;
    private final EchoSandboxClone echo;
    private final NPCVisualController npcVisualController;
    private final AnimationViewerOverlay animationViewerOverlay;

    // Keyed by mob instance id, not MobType — a shared-per-type controller meant every mob of
    // the same species fought over one animation state/timer, and (separately) nothing ever
    // called setState() at all, so every creature stayed frozen on its IDLE pose regardless of
    // whether it was walking, chasing, or attacking. Both are fixed together in render() below.
    private final Map<Integer, AnimationController> mobAnimControllers = new HashMap<>();
    private boolean showAnimationViewer = false;
    private boolean showPerformanceHUD = false;
    private float lastPhysicsTimeMs = 0.0f;
    private float lastRenderTimeMs = 0.0f;
    private float lastFrameTimeMs = 0.0f;
    private float currentFps = 60.0f;

    private boolean running = false;
    private Thread gameThread;
    private boolean initialTutorialShown = false;

    // Reused every frame instead of allocating a fresh Font/Color per NPC/mob per frame.
    private static final Font NPC_NAME_FONT = new Font("Monospaced", Font.BOLD, 9);
    private static final Font NPC_ACTIVITY_FONT = new Font("Monospaced", Font.PLAIN, 8);
    private static final Color NPC_NAME_COLOR = new Color(255, 230, 140);
    private static final Color NPC_ACTIVITY_COLOR = new Color(180, 210, 255);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 75);
    private static final Color MOB_HP_BACK = new Color(20, 20, 30, 210);
    private static final Color MOB_HP_FILL = new Color(220, 40, 50);

    // Camera
    private float camX = 0;
    private float camY = 0;

    // Screen shake — GameSettings.cameraShakeEnabled existed as a togglable setting with
    // nothing in the live engine that ever produced any shake at all to gate (the only
    // ScreenShake/Camera classes in the codebase belong to the unused legacy GameEngine.java).
    // This is a minimal, self-contained implementation: a brief random camera punch on the
    // player's own melee swing, added into camX/camY only for the duration of render() and
    // subtracted back out immediately after, so it never perturbs the actual tracked camera
    // position other rendering math (and the next tick's smoothing) relies on.
    private float shakeTimer = 0f;
    private float shakeMagnitude = 0f;
    private final java.util.Random shakeRandom = new java.util.Random();

    private void triggerShake(float magnitude, float duration) {
        if (!settingsManager.getSettings().cameraShakeEnabled) return;
        shakeMagnitude = magnitude;
        shakeTimer = duration;
    }

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
        // A saved masterVolume from a previous session was loaded into GameSettings above,
        // but nothing ever pushed it into the live SoundEngine — every launch silently reset
        // to full volume regardless of what was saved. Apply once here at startup.
        settingsManager.applySettings(ctx.soundEngine);
        ctx.particleFXManager.setActiveLimit(settingsManager.getSettings().resolutionProfile.maxParticles);
        this.menuController = new TitleMenuController(saveManager, settingsManager);
        this.windowManager = new WindowManager();
        this.assetManager = new AssetManager();
        this.windowManager.inventoryWindow.setAssetManager(this.assetManager);
        this.renderer = new PixelSandboxRenderer(this.assetManager);
        this.hud = new SandboxHUD(assetManager.getSprite("ui/heart_icon.png"));
        this.echo = new EchoSandboxClone();
        this.npcVisualController = assetManager.createNPCVisualController(ctx.npcManager);
        this.animationViewerOverlay = new AnimationViewerOverlay(this.assetManager);

        // Spawn player on surface
        int topZ = ctx.world.getTopSolidBlockZ(8, 8);
        ctx.player.pos.set(8 * WorldChunk.BLOCK_PIXEL_SIZE, 8 * WorldChunk.BLOCK_PIXEL_SIZE, (topZ + 1) * WorldChunk.BLOCK_PIXEL_SIZE);

        this.camX = ctx.player.pos.x - Window.INTERNAL_WIDTH * 0.5f;
        this.camY = ctx.player.pos.y - Window.INTERNAL_HEIGHT * 0.5f;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        // Only reached by the real play entry point (Main.java) — never by the test harnesses,
        // which drive tick()/render() directly without calling start() — so looping this 3-minute
        // track never runs during the automated regression suite.
        ctx.soundEngine.playBackgroundMusic();
        gameThread = new Thread(this, "EchoBound-MasterEngine");
        gameThread.start();
    }

    public synchronized void stop() {
        running = false;
        ctx.soundEngine.stopMusic();
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

            if (elapsed > 0) {
                currentFps = (float) (1_000_000_000.0 / elapsed);
            }

            accumulator += elapsed / 1_000_000_000.0;

            long tickStart = System.nanoTime();
            while (accumulator >= FIXED_DT) {
                tick((float) FIXED_DT);
                accumulator -= FIXED_DT;
            }
            lastPhysicsTimeMs = (System.nanoTime() - tickStart) / 1_000_000.0f;

            long renderStart = System.nanoTime();
            render();
            lastRenderTimeMs = (System.nanoTime() - renderStart) / 1_000_000.0f;
            lastFrameTimeMs = (System.nanoTime() - now) / 1_000_000.0f;

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

        if (shakeTimer > 0f) {
            shakeTimer = Math.max(0f, shakeTimer - dt);
        }

        if (showAnimationViewer) {
            animationViewerOverlay.update(dt);
        }

        if (state == GameState.LOADING || state == GameState.INTRO_CINEMATIC ||
            state == GameState.TITLE_MENU ||
            state == GameState.OPTIONS_MENU || state == GameState.SAVE_SELECT_MENU) {
            menuController.update(dt);
            return;
        }

        if (state == GameState.PAUSED) {
            return;
        }

        if (state == GameState.PLAYING) {
            sessionPlayTime += dt;

            // Auto-show tutorial for first-time adventurers
            if (!initialTutorialShown && settingsManager.getSettings().firstTimeUser) {
                windowManager.setActiveWindow(InGameWindowType.TUTORIAL_CONTROLS);
                initialTutorialShown = true;
            }

            windowManager.update(dt);

            // Update Living NPCs with schedule routines
            for (NPCDefinition npc : ctx.npcManager.getAll()) {
                npcVisualController.update(npc, dt);
            }

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
                windowManager.tutorialWindow.tutorialManager.completeStep(TutorialStep.MOVEMENT);
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

        if (state == GameState.LOADING || state == GameState.INTRO_CINEMATIC ||
            state == GameState.TITLE_MENU ||
            state == GameState.OPTIONS_MENU || state == GameState.SAVE_SELECT_MENU) {
            MenuUIRenderer.render(g, menuController, saveManager, settingsManager,
                                  Window.INTERNAL_WIDTH, Window.INTERNAL_HEIGHT);
            window.present();
            return;
        }

        // 0. Clear the full canvas before drawing this frame. The world/entity renderer only
        // ever draws non-AIR blocks and live entities — sky/void areas above the terrain were
        // never explicitly painted, so they simply kept whatever was already sitting in the
        // buffer. That was invisible under the old single shared BufferedImage (same buffer,
        // same stale pixels, frame after frame), but now that Window ping-pongs between two
        // real buffers (see Window.present()) to fix the render/paint tearing, those two
        // buffers can each be holding a DIFFERENT stale frame in the uncovered areas —
        // e.g. two different camera positions — so as the camera pans, the gaps alternate
        // between two different leftover images every other frame. That reads as flicker.
        g.setColor(new Color(10, 14, 24));
        g.fillRect(0, 0, Window.INTERNAL_WIDTH, Window.INTERNAL_HEIGHT);
        // Pixel art is only ever scaled nearest-neighbor; set once here so per-sprite draws
        // don't each need their own Graphics2D copy just to carry this hint.
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Apply screen shake for this frame only — camX/camY are restored to their true
        // smoothed value at the end of this method so the next tick's camera tracking math
        // is never thrown off by it.
        float shakeAppliedX = 0f, shakeAppliedY = 0f;
        if (shakeTimer > 0f) {
            shakeAppliedX = (shakeRandom.nextFloat() * 2f - 1f) * shakeMagnitude;
            shakeAppliedY = (shakeRandom.nextFloat() * 2f - 1f) * shakeMagnitude;
            camX += shakeAppliedX;
            camY += shakeAppliedY;
        }

        // 1. Render Voxel World & Traversal with Layered Equipment & Animated Weapon
        renderer.render(g, ctx.world, ctx.player, echo, ctx.dayNightCycle,
                        camX, camY, Window.INTERNAL_WIDTH, Window.INTERNAL_HEIGHT, Quality.HIGH,
                        ctx.equipmentManager, ctx.activeWeapon);

        // 1b. Render village houses (CC0 "House Sets" pack — real downloaded structure
        // sprites, drawn near the NPCs whose schedules already reference a home: Kael's
        // "bunkhouse", Rowan's "hollow oak cabin"). Uses the same world-Y-to-screen lift as
        // every other elevated thing this renderer draws, so a house sits correctly on the
        // ground at its z-height instead of floating independent of the terrain under it.
        renderVillageHouses(g, camX, camY);

        // 2. Render Living NPCs with schedule-driven animations
        // labelBounds tracks every name/activity tag already drawn this frame so nearby
        // NPCs never stack illegible text on top of one another (push conflicting tags up).
        List<Rectangle> labelBounds = new ArrayList<>();
        for (NPCDefinition npc : ctx.npcManager.getAll()) {
            int nx = (int) (npc.x - camX);
            int ny = (int) (npc.y - npc.z * 0.75f - camY);
            if (nx < -40 || nx > Window.INTERNAL_WIDTH + 40 || ny < -40 || ny > Window.INTERNAL_HEIGHT + 40) continue;

            // Ground drop shadow
            g.setColor(SHADOW_COLOR);
            g.fillOval(nx - 7, ny - 2, 14, 5);

            // Animated NPC Sprite from schedule controller
            BufferedImage npcFrame = npcVisualController != null ? npcVisualController.getCurrentFrame(npc.id) : null;
            if (npcFrame != null) {
                g.drawImage(npcFrame, nx - 16, ny - 30, 32, 32, null);
            }

            // Name Tag & Profession badge
            g.setFont(NPC_NAME_FONT);
            g.setColor(NPC_NAME_COLOR);
            FontMetrics fm = g.getFontMetrics();
            int nw = fm.stringWidth(npc.name);
            int nameY = placeLabel(labelBounds, nx - nw / 2, ny - 33, nw, fm);
            g.drawString(npc.name, nx - nw / 2, nameY);

            if (npc.currentActivity != null && !npc.currentActivity.isEmpty()) {
                g.setFont(NPC_ACTIVITY_FONT);
                g.setColor(NPC_ACTIVITY_COLOR);
                String actStr = "[" + npc.currentActivity + "]";
                FontMetrics afm = g.getFontMetrics();
                int aw = afm.stringWidth(actStr);
                // Anchor to the name tag actually drawn (which may itself have been nudged),
                // then resolve any remaining collision against every other tag on screen.
                int actY = placeLabel(labelBounds, nx - aw / 2, nameY - 9, aw, afm);
                g.drawString(actStr, nx - aw / 2, actY);
            }
        }

        // 3. Render Active Mobs with multi-frame animated pixel sprites
        for (MobEntity mob : ctx.mobManager.getActiveMobs()) {
            if (!mob.isAlive) continue;
            int sx = (int) (mob.position.x - camX);
            int sy = (int) (mob.position.y - mob.position.z * 0.75f - camY);

            // Legendary creatures render noticeably larger than ordinary wildlife/monsters —
            // a dragon drawn at the same 32x32 as a field rat wouldn't read as legendary.
            int drawSize = (mob.type == MobType.DRAGON) ? 64
                          : (mob.type == MobType.PHOENIX_CREATURE || mob.type == MobType.UNICORN_CREATURE) ? 44
                          : 32;
            int half = drawSize / 2;

            // Ground drop shadow
            g.setColor(SHADOW_COLOR);
            g.fillOval(sx - half / 2, sy - 3, half, 6);

            // Animated Mob Sprite — one controller per mob instance (not per species), state
            // driven every frame from the mob's own AI state so walking/chasing/attacking
            // actually looks different instead of every creature being locked on its IDLE pose.
            AnimationController mobAnim = mobAnimControllers.computeIfAbsent(mob.id,
                    id -> assetManager.createMobAnimationController(mob.type));
            mobAnim.setState(animationStateFor(mob.state));
            mobAnim.update(0.016f);
            BufferedImage mobFrame = mobAnim.getCurrentFrame();
            if (mobFrame != null) {
                g.drawImage(mobFrame, sx - half, sy - drawSize + 4, drawSize, drawSize, null);
            }

            // Health bar with pixel frame
            g.setColor(MOB_HP_BACK);
            g.fillRect(sx - 10, sy - drawSize - 4, 20, 4);
            g.setColor(MOB_HP_FILL);
            int hpW = Math.max(0, (int) (18.0f * ((float) mob.currentHealth / mob.type.maxHealth)));
            g.fillRect(sx - 9, sy - drawSize - 3, hpW, 2);
        }
        pruneStaleMobAnimControllers();

        // 4. Render Projectiles
        for (int i = 0; i < ctx.spellPool.getCapacity(); i++) {
            SpellProjectile p = ctx.spellPool.get(i);
            if (p != null && p.active) {
                p.render(g, camX, camY, 12.0f);
            }
        }

        // 5. Render Particles & Floating Damage Numbers
        ctx.particleFXManager.render(g, camX, camY);
        ctx.floatingTextManager.render(g, camX, camY);

        // 6. Render HUD
        hud.render(g, ctx.player, echo, ctx.dayNightCycle,
                   Window.INTERNAL_WIDTH, Window.INTERNAL_HEIGHT);

        // 7. Render In-Game Modal Windows (Inventory, Crafting, Quest Log)
        if (windowManager.hasActiveWindow()) {
            windowManager.render(g, ctx, Window.INTERNAL_WIDTH, Window.INTERNAL_HEIGHT);
        }

        // 8. Render NPC Dialogue prompt if near NPC
        if (activeDialogueNPC != null) {
            renderDialogueBox(g, activeDialogueNPC);
        }

        // 9. Render Pause Menu if PAUSED
        if (state == GameState.PAUSED) {
            renderPauseMenu(g);
        }

        // 10. Developer Overlays: Animation Viewer [F9] & Performance Debug HUD [F10]
        if (showAnimationViewer) {
            animationViewerOverlay.render(g, Window.INTERNAL_WIDTH, Window.INTERNAL_HEIGHT);
        }
        // showPerformanceHUD (F10, session-only) and the persisted "Debug Info" Options-menu
        // setting used to be two entirely disconnected flags — toggling the menu option had
        // no effect on this overlay at all. Either one now shows it.
        if (showPerformanceHUD || settingsManager.getSettings().showDebugOverlay) {
            PerformanceDebugOverlay.render(g, ctx, assetManager, currentFps,
                                           lastFrameTimeMs, lastPhysicsTimeMs, lastRenderTimeMs,
                                           Window.INTERNAL_WIDTH);
        }

        camX -= shakeAppliedX;
        camY -= shakeAppliedY;

        window.present();
    }

    /**
     * Resolves label overlap for the world-space name/activity tags drawn above NPCs.
     * Starts at the requested baseline and, while the resulting text box intersects any
     * tag already placed this frame, nudges it upward in small steps until it's clear
     * (or a sane attempt limit is hit, so a crowd of NPCs can't push a tag off-screen).
     * The chosen bounding box is recorded so later tags avoid it too.
     */
    private int placeLabel(List<Rectangle> labelBounds, int x, int baselineY, int textWidth, FontMetrics fm) {
        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        int y = baselineY;
        Rectangle box = new Rectangle(x - 2, y - ascent, textWidth + 4, ascent + descent);
        for (int attempts = 0; attempts < 6 && intersectsAny(labelBounds, box); attempts++) {
            y -= (ascent + descent) + 1;
            box.y = y - ascent;
        }
        labelBounds.add(box);
        return y;
    }

    private boolean intersectsAny(List<Rectangle> boxes, Rectangle candidate) {
        for (Rectangle b : boxes) {
            if (b.intersects(candidate)) return true;
        }
        return false;
    }

    /**
     * Taming: PetManager.tamePet(PetType) has existed since Part 3 — 5 companion types, each
     * with a real gameplay perk (light in darkness, treasure detection, etc.) — but nothing
     * in the live game ever called it except a debug-style [P] key that just free-unlocks
     * the next pet in sequence with no creature involved at all. This is the actual
     * "encounter it in the world and tame it" path the perk system never had: press the
     * interact key near a passive/neutral wild creature (§3 wildlife — Woodland Fox, Cave
     * Glowbat, Ember Cat, Moss Turtle, Cloudback Bird) and it joins PetManager for real.
     */
    private void tryTameNearestCreature() {
        MobEntity nearest = null;
        float nearestDist = 40.0f; // tame range, matches the NPC dialogue range closely enough
        for (MobEntity mob : ctx.mobManager.getActiveMobs()) {
            if (!mob.isAlive || !mob.type.isTameable()) continue;
            float dist = (float) mob.position.distance2D(ctx.player.pos);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = mob;
            }
        }
        if (nearest == null) return;

        com.echobound.companion.PetType tamed = nearest.type.tameableAs;
        ctx.petManager.tamePet(tamed);
        ctx.mobManager.removeMob(nearest);
        ctx.floatingTextManager.spawnMessage(nearest.position.x, nearest.position.y, nearest.position.z + 20,
                "Tamed: " + tamed.displayName + "!", new Color(120, 230, 160));
        ctx.soundEngine.play(SoundType.CRAFT_SUCCESS);
        windowManager.tutorialWindow.tutorialManager.completeStep(TutorialStep.TAMING);
    }

    /** name, worldX, worldY, worldZ — placed just behind/beside each NPC's own position so
     *  the house reads as "their home" without covering the spot they actually stand on. */
    private static final Object[][] VILLAGE_HOUSES = {
        {"house_gabled",  85f, 118f, 4f},  // Kael's bunkhouse, near the forge
        {"house_cabin",   22f,  95f, 3f},  // Rowan's hollow oak cabin, near the greenhouse
        {"house_cottage", 230f, 60f, 3f},  // an extra village house for atmosphere
    };

    private void renderVillageHouses(Graphics2D g, float camX, float camY) {
        for (Object[] house : VILLAGE_HOUSES) {
            String name = (String) house[0];
            float wx = (Float) house[1], wy = (Float) house[2], wz = (Float) house[3];
            BufferedImage sprite = assetManager.getStructureSprite(name);
            if (sprite == null) continue;

            int sx = (int) (wx - camX);
            int sy = (int) (wy - wz * 0.75f - camY);
            if (sx < -80 || sx > Window.INTERNAL_WIDTH + 80 || sy < -80 || sy > Window.INTERNAL_HEIGHT + 80) continue;

            int w = sprite.getWidth(), h = sprite.getHeight();
            // Ground shadow, anchored at the sprite's actual base. The sprite is drawn from
            // (sy - h) to sy below — sy IS its bottom edge — but this used sy + h/2, roughly
            // half the house's own height further down, leaving a visible gap between the
            // building and its shadow that read as the house floating above the ground.
            g.setColor(new Color(0, 0, 0, 70));
            g.fillOval(sx - w / 3, sy - 4, (w * 2) / 3, 8);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(sprite, sx - w / 2, sy - h, w, h, null);
            g2.dispose();
        }
    }

    private static AnimationState animationStateFor(MobEntity.AIState aiState) {
        return switch (aiState) {
            case IDLE -> AnimationState.IDLE;
            case WANDER, DETECT -> AnimationState.WALK;
            case CHASE, FLEE, RETREAT -> AnimationState.RUN;
            case ATTACK -> AnimationState.ATTACK;
        };
    }

    /** Mobs are removed from MobManager the instant they die (see MobManager.applyDamageArea),
     *  but their per-instance AnimationController entry would otherwise linger in this map
     *  forever — a slow leak over a long session with lots of creature turnover. */
    private void pruneStaleMobAnimControllers() {
        if (mobAnimControllers.isEmpty()) return;
        java.util.Set<Integer> liveIds = new java.util.HashSet<>();
        for (MobEntity mob : ctx.mobManager.getActiveMobs()) liveIds.add(mob.id);
        mobAnimControllers.keySet().removeIf(id -> !liveIds.contains(id));
    }

    private void renderDialogueBox(Graphics2D g, NPCDefinition npc) {
        int boxW = 460;
        int boxH = 68;
        int boxX = (Window.INTERNAL_WIDTH - boxW) / 2;
        int boxY = Window.INTERNAL_HEIGHT - 86;

        g.setColor(new Color(15, 20, 35, 240));
        g.fillRect(boxX, boxY, boxW, boxH);
        g.setColor(new Color(0, 240, 255));
        g.drawRect(boxX, boxY, boxW, boxH);
        g.setColor(new Color(255, 215, 0));
        g.drawRect(boxX + 2, boxY + 2, boxW - 4, boxH - 4);

        // NPC 32x32 pixel portrait (rendered 48x48)
        BufferedImage portrait = assetManager.getNPCPortrait(npc.name);
        int portraitX = boxX + 8;
        int portraitY = boxY + 10;
        int pSize = 48;
        g.setColor(new Color(25, 30, 45));
        g.fillRect(portraitX, portraitY, pSize, pSize);
        g.setColor(new Color(0, 240, 255, 120));
        g.drawRect(portraitX, portraitY, pSize, pSize);
        if (portrait != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(portrait, portraitX, portraitY, pSize, pSize, null);
            g2.dispose();
        }

        int textX = boxX + 66;
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.setColor(new Color(255, 215, 0));
        g.drawString(npc.name + " [" + npc.faction.displayName + "]", textX, boxY + 20);

        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(Color.WHITE);
        g.drawString("\"" + npc.getDialogue("GREETING") + "\"", textX, boxY + 38);

        g.setFont(new Font("Monospaced", Font.ITALIC, 9));
        g.setColor(new Color(150, 160, 190));
        String hint = npc.isMerchant() ? "[F / ESC] Close    [B] Browse Wares" : "[F / ESC] Close Dialogue";
        g.drawString(hint, textX, boxY + 56);
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

        // Developer Hotkeys [F9] Animation Viewer & [F10] Performance HUD
        if (code == KeyEvent.VK_F9) {
            showAnimationViewer = !showAnimationViewer;
            return;
        }
        if (code == KeyEvent.VK_F10) {
            showPerformanceHUD = !showPerformanceHUD;
            return;
        }

        if (showAnimationViewer) {
            if (code == KeyEvent.VK_ESCAPE) {
                showAnimationViewer = false;
                return;
            }
            if (animationViewerOverlay.handleKeyPress(code)) {
                return;
            }
        }

        GameState state = menuController.getCurrentState();

        if (state == GameState.INTRO_CINEMATIC) {
            menuController.skipCinematic();
            return;
        }

        if (state == GameState.TITLE_MENU || state == GameState.OPTIONS_MENU || state == GameState.SAVE_SELECT_MENU) {
            if (code == KeyEvent.VK_UP) menuController.moveCursorUp();
            if (code == KeyEvent.VK_DOWN) menuController.moveCursorDown();
            if (code == KeyEvent.VK_LEFT) {
                menuController.adjustOptionLeft();
                settingsManager.applySettings(ctx.soundEngine);
                ctx.particleFXManager.setActiveLimit(settingsManager.getSettings().resolutionProfile.maxParticles);
                settingsManager.save();
            }
            if (code == KeyEvent.VK_RIGHT) {
                menuController.adjustOptionRight();
                settingsManager.applySettings(ctx.soundEngine);
                ctx.particleFXManager.setActiveLimit(settingsManager.getSettings().resolutionProfile.maxParticles);
                settingsManager.save();
            }
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                boolean valid = menuController.selectCurrent();
                if (menuController.isExitRequested()) {
                    stop();
                    System.exit(0);
                    return;
                }
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
            if (windowManager.handleKeyPress(code, ctx, settingsManager)) {
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
                windowManager.tutorialWindow.tutorialManager.completeStep(TutorialStep.ECHO);
                if (echo.isRecording) echo.stopRecording();
                else if (echo.isActive) echo.stopPlayback();
                else if (echo.hasRecordedData()) echo.startPlayback();
                else echo.startRecording(ctx.player);
            }
            // Cast Spells
            if (code == KeyEvent.VK_Q) {
                windowManager.tutorialWindow.tutorialManager.completeStep(TutorialStep.MAGIC);
                ctx.castDualSpell(MagicSchool.EMBER, MagicSchool.GALE, new Vec3(ctx.player.facingDirX, ctx.player.facingDirY, 0));
            }
            if (code == KeyEvent.VK_E) {
                windowManager.tutorialWindow.tutorialManager.completeStep(TutorialStep.MAGIC);
                ctx.castDualSpell(MagicSchool.TIDE, MagicSchool.VOLT, new Vec3(ctx.player.facingDirX, ctx.player.facingDirY, 0));
            }
            // Talk to NPC, or — if none are close — tame a nearby wild creature instead.
            // One context-sensitive "interact" key rather than a second dedicated binding,
            // matching how [F] already works for dialogue.
            if (code == KeyEvent.VK_F) {
                if (activeDialogueNPC != null) {
                    activeDialogueNPC = null;
                } else {
                    NPCDefinition nearestNpc = null;
                    for (NPCDefinition npc : ctx.npcManager.getAll()) {
                        float dist = (float) Math.hypot(npc.x - ctx.player.pos.x, npc.y - ctx.player.pos.y);
                        if (dist < 48.0f) {
                            nearestNpc = npc;
                            break;
                        }
                    }
                    if (nearestNpc != null) {
                        activeDialogueNPC = nearestNpc;
                    } else {
                        tryTameNearestCreature();
                    }
                }
            }
            // Shop with the NPC currently in dialogue, if they sell anything — see
            // NPCManager for who's a merchant (Kael/Rowan/Lyra all are, each with a
            // themed inventory) and ShopWindow for the actual buy transaction.
            if (code == KeyEvent.VK_B && activeDialogueNPC != null && activeDialogueNPC.isMerchant()) {
                windowManager.openShop(activeDialogueNPC);
                activeDialogueNPC = null;
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
        if (menuController.getCurrentState() == GameState.INTRO_CINEMATIC) {
            menuController.skipCinematic();
            return;
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftClickHeld = true;
            // Melee attack — UnifiedGameContext.attackWithWeapon() existed but was never
            // called from anywhere in the live game (same pattern as mining/taming/shops
            // before this session): no key or click triggered it, so there was no way to
            // actually attack a mob at all. Fires once per click, separate from the
            // continuous hold-based mining that also reads leftClickHeld in
            // PlayerSandboxEntity.update — a click both mines whatever's targeted ahead and
            // swings at whatever's standing there, which is the usual feel for this genre.
            if (menuController.getCurrentState() == GameState.PLAYING
                    && !windowManager.hasActiveWindow() && activeDialogueNPC == null) {
                performMeleeAttack();
            }
        }
        if (e.getButton() == MouseEvent.BUTTON3) rightClickJustPressed = true;
    }

    private void performMeleeAttack() {
        float reach = 26.0f;
        Vec3 attackCenter = new Vec3(
            ctx.player.pos.x + ctx.player.facingDirX * reach,
            ctx.player.pos.y + ctx.player.facingDirY * reach,
            ctx.player.pos.z + 8.0f
        );
        ctx.attackWithWeapon(ctx.activeWeapon, attackCenter);
        renderer.getRinAnimController().triggerAction(AnimationState.ATTACK);
        triggerShake(1.5f, 0.12f);
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

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public AnimationViewerOverlay getAnimationViewerOverlay() {
        return animationViewerOverlay;
    }

    public NPCVisualController getNPCVisualController() {
        return npcVisualController;
    }

    public boolean isAnimationViewerOpen() {
        return showAnimationViewer;
    }

    public void toggleAnimationViewer() {
        showAnimationViewer = !showAnimationViewer;
    }

    public boolean isPerformanceHUDOpen() {
        return showPerformanceHUD;
    }

    public void togglePerformanceHUD() {
        showPerformanceHUD = !showPerformanceHUD;
    }
}
