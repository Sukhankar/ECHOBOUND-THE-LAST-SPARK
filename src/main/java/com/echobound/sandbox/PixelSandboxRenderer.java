package com.echobound.sandbox;

import com.echobound.core.Quality;

import java.awt.*;

public class PixelSandboxRenderer {
    public static final float Z_ELEVATION_PX = 8.0f; // Pixels lifted per Z layer

    public void render(Graphics2D g, SandboxWorld world, PlayerSandboxEntity player,
                       EchoSandboxClone echo, DayNightCycle dayNight,
                       float camX, float camY, int viewW, int viewH, Quality quality) {

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

        // Front face (if block to the south is empty or lower)
        BlockType southNeighbor = world.getBlock(bx, by + 1, bz);
        if (!southNeighbor.solid) {
            g.setColor(b.shadowColor);
            g.fillRect(sx, sy + bs, bs, (int) Z_ELEVATION_PX);
            // Brick texture lines
            if (b == BlockType.STONE_BRICK || b == BlockType.STONE) {
                g.setColor(new Color(30, 32, 40, 180));
                g.drawRect(sx, sy + bs, bs - 1, (int) Z_ELEVATION_PX - 1);
            }
        }

        // Top face
        g.setColor(b.baseColor);
        g.fillRect(sx, sy, bs, bs);

        // Top beveled highlight & shadow
        g.setColor(b.highlightColor);
        g.drawLine(sx, sy, sx + bs - 1, sy); // North highlight
        g.drawLine(sx, sy, sx, sy + bs - 1); // West highlight

        g.setColor(b.shadowColor);
        g.drawLine(sx + bs - 1, sy, sx + bs - 1, sy + bs - 1); // East shadow
        g.drawLine(sx, sy + bs - 1, sx + bs - 1, sy + bs - 1); // South shadow

        // Specialized texture details
        if (b == BlockType.SPARK_LAMP) {
            g.setColor(Color.WHITE);
            g.fillOval(sx + 5, sy + 5, 6, 6);
        } else if (b == BlockType.SPARK_ORE) {
            g.setColor(new Color(0, 240, 255));
            g.fillRect(sx + 4, sy + 4, 3, 3);
            g.fillRect(sx + 9, sy + 8, 3, 3);
        } else if (b == BlockType.CRYSTAL_NODE) {
            g.setColor(new Color(255, 120, 255));
            int[] px = {sx + 8, sx + 12, sx + 4};
            int[] py = {sy + 3, sy + 13, sy + 13};
            g.fillPolygon(px, py, 3);
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

        // Draw progressive crack lines
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
        // Ground drop shadow
        int groundScreenY = (int) (p.pos.y - p.pos.z * (Z_ELEVATION_PX / WorldChunk.BLOCK_PIXEL_SIZE) - camY);
        int shadowX = (int) (p.pos.x - camX);
        int shadowY = (int) (p.pos.y - camY);

        g.setColor(new Color(0, 0, 0, 90));
        g.fillOval(shadowX - 6, shadowY - 3, 12, 6);

        // Player screen position elevated by Z
        int px = (int) (p.pos.x - camX);
        int py = (int) (p.pos.y - p.pos.z * (Z_ELEVATION_PX / WorldChunk.BLOCK_PIXEL_SIZE) - camY);

        if (isEcho) {
            // Echo translucent cyan phantom
            g.setColor(new Color(0, 240, 255, 120));
            g.fillRoundRect(px - 5, py - 14, 10, 14, 4, 4);
            g.setColor(new Color(200, 255, 255, 190));
            g.drawRoundRect(px - 5, py - 14, 10, 14, 4, 4);
            return;
        }

        // RIN 16-BIT RETRO SILHOUETTE
        // Energy Scarf trailing
        g.setColor(new Color(0, 240, 255, 210));
        g.setStroke(new BasicStroke(2.5f));
        int scarfBackX = px - (int) (p.facingDirX * 8);
        int scarfBackY = py - 7 - (int) (p.facingDirY * 8);
        g.drawLine(px, py - 7, scarfBackX, scarfBackY);
        g.setStroke(new BasicStroke(1.0f));

        // Runner Jacket (Warm Sunroot Orange #F2803A)
        g.setColor(new Color(242, 128, 58));
        g.fillRect(px - 4, py - 10, 8, 6);

        // Cyan Core Emblem
        g.setColor(new Color(0, 240, 255));
        g.fillRect(px - 1, py - 8, 2, 2);

        // Head & Goggles
        g.setColor(new Color(245, 185, 90));
        g.fillRect(px - 3, py - 14, 6, 4);
        g.setColor(new Color(255, 210, 40));
        g.fillRect(px - 4, py - 14, 8, 2);

        // Heavy Boots
        g.setColor(new Color(60, 42, 42));
        g.fillRect(px - 4, py - 4, 3, 4);
        g.fillRect(px + 1, py - 4, 3, 4);

        // Spark Gauntlet
        int gx = px + (int) (p.facingDirX * 6);
        int gy = py - 7 + (int) (p.facingDirY * 6);
        g.setColor(new Color(0, 240, 255));
        g.fillRect(gx - 2, gy - 2, 4, 4);
    }

    private void renderAmbientLighting(Graphics2D g, PlayerSandboxEntity player, SandboxWorld world,
                                      DayNightCycle dayNight, int viewW, int viewH, float camX, float camY) {
        if (!dayNight.isNight() && dayNight.getWeather() == WeatherType.CLEAR) {
            return;
        }

        // Draw ambient darkness overlay
        Color darkColor = dayNight.getAmbientDarknessColor();
        if (darkColor.getAlpha() <= 0) return;

        g.setColor(darkColor);
        g.fillRect(0, 0, viewW, viewH);

        // Player's Spark Gauntlet light punch-through
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
