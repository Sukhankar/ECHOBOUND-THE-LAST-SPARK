package com.echobound.sandbox;

import com.echobound.animation.AnimationController;
import com.echobound.animation.AnimationState;
import com.echobound.animation.LayeredCharacterRenderer;
import com.echobound.assets.AssetManager;
import com.echobound.combat.ModdedWeapon;
import com.echobound.core.Quality;
import com.echobound.items.EquipmentManager;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PixelSandboxRenderer {
    private static final Color FRONT_FACE_SHADE = new Color(0, 0, 0, 85);
    public static final float Z_ELEVATION_PX = 8.0f; // Pixels lifted per Z layer

    private final AssetManager assetManager;
    private final AnimationController rinAnimController;
    private final AnimationController echoAnimController;
    private final AnimationController pipAnimController;

    private EquipmentManager currentEquip = null;
    private ModdedWeapon currentWeapon = null;

    public PixelSandboxRenderer() {
        this(new AssetManager());
    }

    public PixelSandboxRenderer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.rinAnimController = assetManager.createRinAnimationController();
        this.echoAnimController = assetManager.createRinAnimationController();
        this.pipAnimController = assetManager.createPipAnimationController();
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public AnimationController getRinAnimController() {
        return rinAnimController;
    }

    public AnimationController getPipAnimController() {
        return pipAnimController;
    }

    public void setPlayerEquipment(EquipmentManager equip, ModdedWeapon weapon) {
        this.currentEquip = equip;
        this.currentWeapon = weapon;
    }

    public void render(Graphics2D g, SandboxWorld world, PlayerSandboxEntity player,
                       EchoSandboxClone echo, DayNightCycle dayNight,
                       float camX, float camY, int viewW, int viewH, Quality quality) {
        render(g, world, player, echo, dayNight, camX, camY, viewW, viewH, quality, currentEquip, currentWeapon);
    }

    public void render(Graphics2D g, SandboxWorld world, PlayerSandboxEntity player,
                       EchoSandboxClone echo, DayNightCycle dayNight,
                       float camX, float camY, int viewW, int viewH, Quality quality,
                       EquipmentManager equip, ModdedWeapon weapon) {
        this.currentEquip = equip;
        this.currentWeapon = weapon;

        int startBlockX = (int) Math.floor(camX / WorldChunk.BLOCK_PIXEL_SIZE) - 2;
        int endBlockX = (int) Math.ceil((camX + viewW) / WorldChunk.BLOCK_PIXEL_SIZE) + 2;
        int startBlockY = (int) Math.floor(camY / WorldChunk.BLOCK_PIXEL_SIZE) - 2;
        int endBlockY = (int) Math.ceil((camY + viewH + WorldChunk.CHUNK_SIZE_Z * Z_ELEVATION_PX) / WorldChunk.BLOCK_PIXEL_SIZE) + 2;

        // 1. Render Blocks from Low Z to High Z, and North to South (Y-Sort + Z-Sort)
        for (int z = 0; z < WorldChunk.CHUNK_SIZE_Z; z++) {
            for (int y = startBlockY; y <= endBlockY; y++) {
                for (int x = startBlockX; x <= endBlockX; x++) {
                    BlockType b = world.getBlock(x, y, z);
                    if (b != BlockType.AIR) {
                        renderBlock(g, b, x, y, z, world, camX, camY);
                    }
                }
            }
        }

        // 2. Render Mining Crack Overlay
        if (player.currentTarget != null && player.miningProgress > 0.0f) {
            renderMiningProgress(g, player.currentTarget, player.miningProgress, camX, camY);
        }

        // 3. Render Target Reticle Outline
        if (player.currentTarget != null) {
            renderTargetReticle(g, player.currentTarget, camX, camY);
        }

        // 4. Render Echo Clone (if active)
        if (echo != null && echo.isActive && echo.getGhostEntity() != null) {
            renderPlayerEntity(g, echo.getGhostEntity(), camX, camY, true);
        }

        // 5. Render Player Entity (Rin)
        renderPlayerEntity(g, player, camX, camY, false);

        // 6. Day / Night Darkness & Dynamic Torch Lighting Overlay
        renderAmbientLighting(g, player, world, dayNight, viewW, viewH, camX, camY);
    }

    private void renderBlock(Graphics2D g, BlockType b, int bx, int by, int bz,
                             SandboxWorld world, float camX, float camY) {
        int bs = WorldChunk.BLOCK_PIXEL_SIZE;
        int sx = bx * bs - (int) camX;
        int sy = by * bs - (int) (bz * Z_ELEVATION_PX) - (int) camY;

        int variant = Math.abs(bx * 31 + by * 17 + bz * 7);
        BufferedImage tileImg = assetManager.getTileTexture(b, variant);

        // Top face with pixel art texture
        if (tileImg != null) {
            g.drawImage(tileImg, sx, sy, bs, bs, null);
        } else {
            g.setColor(b.baseColor);
            g.fillRect(sx, sy, bs, bs);
        }

        // Front face (if block to the south is empty or lower)
        BlockType southNeighbor = world.getBlock(bx, by + 1, bz);
        if (!southNeighbor.solid) {
            if (tileImg != null) {
                // Render front edge drop with shaded slice of texture
                g.drawImage(tileImg, sx, sy + bs, sx + bs, sy + bs + (int) Z_ELEVATION_PX,
                            0, Math.max(0, tileImg.getHeight() - (int) Z_ELEVATION_PX),
                            tileImg.getWidth(), tileImg.getHeight(), null);
                g.setColor(FRONT_FACE_SHADE);
                g.fillRect(sx, sy + bs, bs, (int) Z_ELEVATION_PX);
            } else {
                g.setColor(b.shadowColor);
                g.fillRect(sx, sy + bs, bs, (int) Z_ELEVATION_PX);
            }
        }
    }

    private void renderTargetReticle(Graphics2D g, SandboxWorld.BlockHit hit, float camX, float camY) {
        int bs = WorldChunk.BLOCK_PIXEL_SIZE;
        int sx = hit.blockX * bs - (int) camX;
        int sy = hit.blockY * bs - (int) (hit.blockZ * Z_ELEVATION_PX) - (int) camY;

        g.setColor(new Color(0, 240, 255, 200));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect(sx, sy, bs, bs);
        g.setStroke(new BasicStroke(1.0f));
    }

    private void renderMiningProgress(Graphics2D g, SandboxWorld.BlockHit hit, float progress,
                                      float camX, float camY) {
        float maxH = hit.blockType.maxHardness;
        if (maxH <= 0) return;
        float ratio = Math.min(1.0f, progress / maxH);

        int bs = WorldChunk.BLOCK_PIXEL_SIZE;
        int sx = hit.blockX * bs - (int) camX;
        int sy = hit.blockY * bs - (int) (hit.blockZ * Z_ELEVATION_PX) - (int) camY;

        // Progressive crack lines
        g.setColor(new Color(20, 20, 20, 220));
        g.drawLine(sx + 3, sy + 3, sx + 8, sy + 8);
        if (ratio > 0.33f) {
            g.drawLine(sx + 8, sy + 8, sx + 13, sy + 5);
        }
        if (ratio > 0.66f) {
            g.drawLine(sx + 8, sy + 8, sx + 7, sy + 14);
            g.drawLine(sx + 4, sy + 11, sx + 12, sy + 11);
        }
    }

    private void renderPlayerEntity(Graphics2D g, PlayerSandboxEntity p, float camX, float camY, boolean isEcho) {
        // Screen position elevated by Z — every other elevated thing this renderer draws
        // (blocks, mobs, NPCs) lifts its shadow by this same amount so it stays under the
        // sprite's feet; the player's shadow previously didn't, and since pos.z is almost
        // never 0 here (it's absolute world height — terrain alone sits at z=2..6, not a
        // jump-height offset), the shadow was permanently anchored several pixels below
        // the sprite. That's what made the character look like it was levitating.
        int px = (int) (p.pos.x - camX);
        int py = (int) (p.pos.y - p.pos.z * (Z_ELEVATION_PX / WorldChunk.BLOCK_PIXEL_SIZE) - camY);

        // Ground drop shadow — anchored to the sprite's own lifted position, not pos.y alone.
        g.setColor(new Color(0, 0, 0, 90));
        g.fillOval(px - 6, py - 3, 12, 6);

        if (isEcho) {
            // Echo Clone: Spectral cyan animation duplicate
            echoAnimController.update(0.016f);
            LayeredCharacterRenderer.render(g, echoAnimController, null, null, px, py + 2, 1.0f, true);
            return;
        }

        // Update Rin animation state machine
        updateRinAnimation(p);

        // Render Rin with dynamic equipment layers & weapon
        LayeredCharacterRenderer.render(g, rinAnimController, currentEquip, currentWeapon, px, py + 2, 1.0f, false);

        // Render Pip companion hovering near Rin
        renderPipCompanion(g, p, camX, camY);
    }

    private void updateRinAnimation(PlayerSandboxEntity p) {
        float speed = (float) Math.hypot(p.vel.x, p.vel.y);
        if (p.isDashing) {
            rinAnimController.setState(AnimationState.DASH);
        } else if (p.isGliding) {
            rinAnimController.setState(AnimationState.GLIDE);
        } else if (!p.onGround) {
            if (p.vel.z > 0) rinAnimController.setState(AnimationState.JUMP);
            else rinAnimController.setState(AnimationState.FALL);
        } else if (p.miningProgress > 0) {
            rinAnimController.setState(AnimationState.MINE);
        } else if (speed > 10.0f) {
            if (speed > 180.0f) rinAnimController.setState(AnimationState.RUN);
            else rinAnimController.setState(AnimationState.WALK);
        } else {
            rinAnimController.setState(AnimationState.IDLE);
        }
        rinAnimController.setFacingLeft(p.facingDirX < -0.1f);
        rinAnimController.update(0.016f);
    }

    private void renderPipCompanion(Graphics2D g, PlayerSandboxEntity p, float camX, float camY) {
        pipAnimController.update(0.016f);
        BufferedImage pipImg = pipAnimController.getCurrentFrame();
        if (pipImg == null) return;

        long time = System.currentTimeMillis();
        float hoverY = (float) Math.sin(time * 0.005) * 3.0f;
        int pipX = (int) (p.pos.x - camX) + (p.facingDirX < 0 ? 12 : -12);
        int pipY = (int) (p.pos.y - p.pos.z * (Z_ELEVATION_PX / WorldChunk.BLOCK_PIXEL_SIZE) - camY) - 18 + (int) hoverY;

        // Spark glow aura
        g.setColor(new Color(255, 235, 100, 45));
        g.fillOval(pipX - 2, pipY - 2, 20, 20);

        // Pip sprite
        g.drawImage(pipImg, pipX, pipY, null);
    }

    private void renderAmbientLighting(Graphics2D g, PlayerSandboxEntity player, SandboxWorld world,
                                       DayNightCycle dayNight, int viewW, int viewH, float camX, float camY) {
        // Seasonal color wash — applied regardless of time/weather (unlike the darkness
        // overlay below, which only kicks in at night or in bad weather) so Autumn/Winter/
        // Spring read as an atmosphere shift even in broad clear daylight.
        Color wash = dayNight.getSeason().ambientWash;
        if (wash.getAlpha() > 0) {
            g.setColor(wash);
            g.fillRect(0, 0, viewW, viewH);
        }

        if (!dayNight.isNight() && dayNight.getWeather() == WeatherType.CLEAR) {
            return;
        }

        Color darkColor = dayNight.getAmbientDarknessColor();
        if (darkColor.getAlpha() <= 0) return;

        g.setColor(darkColor);
        g.fillRect(0, 0, viewW, viewH);

        int plScreenX = (int) (player.pos.x - camX);
        int plScreenY = (int) (player.pos.y - player.pos.z * (Z_ELEVATION_PX / WorldChunk.BLOCK_PIXEL_SIZE) - camY);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.DstOut);
        RadialGradientPaint lightHole = new RadialGradientPaint(
            plScreenX, plScreenY, 64.0f,
            new float[]{0.0f, 0.7f, 1.0f},
            new Color[]{new Color(0, 0, 0, 220), new Color(0, 0, 0, 100), new Color(0, 0, 0, 0)}
        );
        g2.setPaint(lightHole);
        g2.fillOval(plScreenX - 64, plScreenY - 64, 128, 128);
        g2.dispose();
    }
}
