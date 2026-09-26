package com.echobound.sandbox;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * In-game HUD. Layout follows a polished action-RPG frame — portrait with HP/Spark bars top-left,
 * quest tracker under it, coin pill and minimap top-right, framed hotbar bottom-centre, key
 * shortcut badges bottom-right and a controls panel bottom-left — drawn with ornate bronze-edged
 * panels. Only data the game genuinely has is shown (there is no player level/XP system, so no
 * fake level or XP bar).
 */
public class SandboxHUD {

    /** Extra live data the richer HUD panels need; filled in by the engine each frame. */
    public static class HudInfo {
        public int coins;
        public int questCount;
        public final String[] questTitles = new String[3];
        public final String[] questProgress = new String[3];
        public String biomeName = "";
        public BufferedImage portrait;
        public BufferedImage[] navIcons = new BufferedImage[3]; // bag, craft, quests
        public SandboxWorld world;
    }

    private static final Font FONT_BOLD_9 = new Font(Font.MONOSPACED, Font.BOLD, 9);
    private static final Font FONT_BOLD_8 = new Font(Font.MONOSPACED, Font.BOLD, 8);
    private static final Font FONT_PLAIN_7 = new Font(Font.MONOSPACED, Font.PLAIN, 7);
    private static final Font FONT_BOLD_7 = new Font(Font.MONOSPACED, Font.BOLD, 7);

    private static final Color PANEL_FILL = new Color(16, 20, 30, 225);
    private static final Color EDGE_DARK = new Color(8, 8, 12);
    private static final Color EDGE_BRONZE = new Color(140, 100, 50);
    private static final Color EDGE_GOLD = new Color(214, 174, 92);
    private static final Color TEXT_GOLD = new Color(255, 214, 110);
    private static final Color TEXT_LIGHT = new Color(235, 235, 240);
    private static final Color TEXT_DIM = new Color(150, 158, 172);
    private static final Color TEXT_SHADOW = new Color(0, 0, 0, 200);
    private static final Color BAR_BACK = new Color(10, 10, 16);

    private static final int SLOT_SIZE = 22;
    private static final int MARGIN = 6;

    private static final int MAP_BLOCKS_W = 39, MAP_BLOCKS_H = 30, MAP_SCALE = 2;
    private static final long MAP_REFRESH_NANOS = 400_000_000L;

    // Real CC0 heart art (see RealPixelAssetPipeline.buildHeartIcon()). Optional so the class
    // still works, drawing a plain heart shape, for callers with no AssetManager on hand.
    private final BufferedImage heartIcon;

    private BufferedImage miniMap;
    private long miniMapStamp;

    public SandboxHUD() {
        this(null);
    }

    public SandboxHUD(BufferedImage heartIcon) {
        this.heartIcon = heartIcon;
    }

    public void render(Graphics2D g, PlayerSandboxEntity player, EchoSandboxClone echo,
                       DayNightCycle dayNight, int viewW, int viewH) {
        render(g, player, echo, dayNight, viewW, viewH, null);
    }

    public void render(Graphics2D g, PlayerSandboxEntity player, EchoSandboxClone echo,
                       DayNightCycle dayNight, int viewW, int viewH, HudInfo info) {
        renderPlayerFrame(g, player, info);
        if (info != null) {
            renderQuestTracker(g, info);
            renderCoinPill(g, info, viewW);
        }
        renderEchoStatus(g, echo, viewW);
        if (info != null && info.world != null) {
            renderMinimapPanel(g, player, dayNight, info, viewW);
        } else {
            renderTimeAndWeather(g, dayNight, viewW - 170, 10);
        }
        renderQuickSlots(g, player.inventory, viewW, viewH);
        renderNavButtons(g, info, viewW, viewH);
        renderControlsPanel(g, viewH);
    }

    // ── Panel + text helpers ──────────────────────────────────────────────────────────

    /** Ornate framed panel: dark outline, bronze body edge, thin gold inner highlight. */
    private static void drawPanel(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(EDGE_DARK);
        g.fillRoundRect(x - 1, y - 1, w + 2, h + 2, 8, 8);
        g.setColor(EDGE_BRONZE);
        g.fillRoundRect(x, y, w, h, 7, 7);
        g.setColor(PANEL_FILL);
        g.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 5, 5);
        g.setColor(EDGE_GOLD);
        g.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 6, 6);
        g.fillRect(x + 3, y + 3, 1, 1);
        g.fillRect(x + w - 4, y + 3, 1, 1);
        g.fillRect(x + 3, y + h - 4, 1, 1);
        g.fillRect(x + w - 4, y + h - 4, 1, 1);
    }

    private static void drawText(Graphics2D g, String s, int x, int y, Color c) {
        g.setColor(TEXT_SHADOW);
        g.drawString(s, x + 1, y + 1);
        g.setColor(c);
        g.drawString(s, x, y);
    }

    private static void drawCentered(Graphics2D g, String s, int cx, int y, Color c) {
        drawText(g, s, cx - g.getFontMetrics().stringWidth(s) / 2, y, c);
    }

    private static String fit(Graphics2D g, String s, int maxW) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(s) <= maxW) return s;
        while (s.length() > 1 && fm.stringWidth(s + "..") > maxW) s = s.substring(0, s.length() - 1);
        return s + "..";
    }

    private static void drawBar(Graphics2D g, int x, int y, int w, int h, float ratio,
                                Color top, Color bottom, String label) {
        ratio = Math.max(0f, Math.min(1f, ratio));
        g.setColor(EDGE_DARK);
        g.fillRect(x - 1, y - 1, w + 2, h + 2);
        g.setColor(BAR_BACK);
        g.fillRect(x, y, w, h);
        int fw = (int) (w * ratio);
        if (fw > 0) {
            g.setPaint(new GradientPaint(0, y, top, 0, y + h, bottom));
            g.fillRect(x, y, fw, h);
            g.setColor(new Color(255, 255, 255, 60));
            g.fillRect(x, y, fw, 1);
        }
        g.setColor(EDGE_BRONZE);
        g.drawRect(x - 1, y - 1, w + 1, h + 1);
        g.setFont(FONT_BOLD_7);
        drawCentered(g, label, x + w / 2, y + h - 2, TEXT_LIGHT);
    }

    // ── Top-left: portrait + bars ─────────────────────────────────────────────────────

    private void renderPlayerFrame(Graphics2D g, PlayerSandboxEntity player, HudInfo info) {
        int x = MARGIN, y = MARGIN, w = 156, h = 46;
        drawPanel(g, x, y, w, h);

        g.setColor(EDGE_DARK);
        g.fillRect(x + 5, y + 5, 36, 36);
        g.setColor(new Color(30, 40, 60));
        g.fillRect(x + 6, y + 6, 34, 34);
        if (info != null && info.portrait != null) {
            g.drawImage(info.portrait, x + 6, y + 6, 34, 34, null);
        }
        g.setColor(EDGE_GOLD);
        g.drawRect(x + 5, y + 5, 35, 35);

        g.setFont(FONT_BOLD_9);
        drawText(g, "Rin", x + 47, y + 14, TEXT_GOLD);

        int bx = x + 60, bw = 90;
        if (heartIcon != null) {
            g.drawImage(heartIcon, x + 47, y + 17, 11, 11, null);
        } else {
            g.setColor(new Color(235, 45, 75));
            g.fillOval(x + 47, y + 18, 10, 9);
        }
        drawBar(g, bx, y + 19, bw, 9, player.maxHealth > 0 ? (float) player.health / player.maxHealth : 0f,
                new Color(235, 70, 70), new Color(140, 20, 30), player.health + " / " + player.maxHealth);

        g.setColor(new Color(0, 240, 255));
        g.fillPolygon(new int[]{x + 52, x + 56, x + 52, x + 48}, new int[]{y + 31, y + 35, y + 39, y + 35}, 4);
        drawBar(g, bx, y + 32, bw, 9, player.maxSparkEnergy > 0 ? player.sparkEnergy / player.maxSparkEnergy : 0f,
                new Color(60, 200, 255), new Color(10, 90, 170),
                (int) player.sparkEnergy + " / " + (int) player.maxSparkEnergy);
    }

    // ── Left: quest tracker ───────────────────────────────────────────────────────────

    private void renderQuestTracker(Graphics2D g, HudInfo info) {
        int x = MARGIN, y = 58, w = 132;
        int rows = Math.max(1, Math.min(3, info.questCount));
        int h = 18 + rows * 20;
        drawPanel(g, x, y, w, h);
        g.setFont(FONT_BOLD_8);
        drawText(g, "QUESTS", x + 8, y + 12, TEXT_GOLD);
        g.setColor(EDGE_BRONZE);
        g.drawLine(x + 6, y + 15, x + w - 7, y + 15);

        if (info.questCount == 0) {
            g.setFont(FONT_PLAIN_7);
            drawText(g, "No active quests.", x + 8, y + 27, TEXT_DIM);
            return;
        }
        for (int i = 0; i < rows; i++) {
            int ry = y + 17 + i * 20;
            g.setColor(TEXT_GOLD);
            g.fillPolygon(new int[]{x + 8, x + 12, x + 8, x + 4}, new int[]{ry + 3, ry + 7, ry + 11, ry + 7}, 4);
            g.setFont(FONT_BOLD_7);
            drawText(g, fit(g, info.questTitles[i], w - 22), x + 15, ry + 9, TEXT_LIGHT);
            g.setFont(FONT_PLAIN_7);
            drawText(g, info.questProgress[i], x + 15, ry + 18, TEXT_DIM);
        }
    }

    // ── Top-right: coins + minimap/time ───────────────────────────────────────────────

    private void renderCoinPill(Graphics2D g, HudInfo info, int viewW) {
        int w = 78, h = 16, x = viewW - MARGIN - 88 - 4 - w, y = MARGIN;
        drawPanel(g, x, y, w, h);
        g.setColor(EDGE_DARK);
        g.fillOval(x + 5, y + 3, 10, 10);
        g.setColor(new Color(255, 205, 60));
        g.fillOval(x + 6, y + 4, 8, 8);
        g.setColor(new Color(255, 240, 160));
        g.fillOval(x + 8, y + 5, 3, 3);
        g.setFont(FONT_BOLD_9);
        String s = String.valueOf(info.coins);
        drawText(g, s, x + w - 6 - g.getFontMetrics().stringWidth(s), y + 12, TEXT_LIGHT);
    }

    private void renderMinimapPanel(Graphics2D g, PlayerSandboxEntity player, DayNightCycle dayNight,
                                    HudInfo info, int viewW) {
        int w = 88, mapW = MAP_BLOCKS_W * MAP_SCALE, mapH = MAP_BLOCKS_H * MAP_SCALE;
        int h = 14 + mapH + 4 + 34;
        int x = viewW - MARGIN - w, y = MARGIN;
        drawPanel(g, x, y, w, h);

        g.setFont(FONT_BOLD_8);
        drawCentered(g, info.biomeName, x + w / 2, y + 11, TEXT_GOLD);

        refreshMiniMap(info.world, player);
        int mx = x + (w - mapW) / 2, my = y + 14;
        g.setColor(EDGE_DARK);
        g.fillRect(mx - 1, my - 1, mapW + 2, mapH + 2);
        if (miniMap != null) g.drawImage(miniMap, mx, my, null);
        g.setColor(EDGE_BRONZE);
        g.drawRect(mx - 1, my - 1, mapW + 1, mapH + 1);

        int pcx = mx + mapW / 2, pcy = my + mapH / 2;
        boolean blink = (System.currentTimeMillis() / 300) % 2 == 0;
        g.setColor(EDGE_DARK);
        g.fillRect(pcx - 2, pcy - 2, 5, 5);
        g.setColor(blink ? Color.WHITE : new Color(0, 240, 255));
        g.fillRect(pcx - 1, pcy - 1, 3, 3);

        String[] timeParts = dayNight.getFormattedTime().split(" \\| ");
        g.setFont(FONT_PLAIN_7);
        int ty = my + mapH + 10;
        drawText(g, timeParts[0] + (timeParts.length > 1 ? "  " + timeParts[1] : ""), x + 6, ty, TEXT_LIGHT);
        drawText(g, dayNight.getWeather().displayName, x + 6, ty + 10, new Color(255, 210, 80));
        drawText(g, dayNight.getSeason().displayName, x + 6, ty + 20, new Color(150, 230, 170));
    }

    private void refreshMiniMap(SandboxWorld world, PlayerSandboxEntity p) {
        long now = System.nanoTime();
        if (miniMap != null && now - miniMapStamp < MAP_REFRESH_NANOS) return;
        miniMapStamp = now;
        if (miniMap == null) {
            miniMap = new BufferedImage(MAP_BLOCKS_W * MAP_SCALE, MAP_BLOCKS_H * MAP_SCALE, BufferedImage.TYPE_INT_RGB);
        }
        int cbx = (int) Math.floor(p.pos.x / WorldChunk.BLOCK_PIXEL_SIZE);
        int cby = (int) Math.floor(p.pos.y / WorldChunk.BLOCK_PIXEL_SIZE);
        for (int j = 0; j < MAP_BLOCKS_H; j++) {
            for (int i = 0; i < MAP_BLOCKS_W; i++) {
                int wx = cbx - MAP_BLOCKS_W / 2 + i, wy = cby - MAP_BLOCKS_H / 2 + j;
                int rgb = 0x0A0E18;
                for (int z = WorldChunk.CHUNK_SIZE_Z - 1; z >= 0; z--) {
                    BlockType b = world.getBlock(wx, wy, z);
                    if (b != BlockType.AIR) {
                        rgb = b.baseColor.getRGB();
                        break;
                    }
                }
                for (int dy = 0; dy < MAP_SCALE; dy++) {
                    for (int dx = 0; dx < MAP_SCALE; dx++) {
                        miniMap.setRGB(i * MAP_SCALE + dx, j * MAP_SCALE + dy, rgb);
                    }
                }
            }
        }
    }

    private void renderTimeAndWeather(Graphics2D g, DayNightCycle dayNight, int x, int y) {
        drawPanel(g, x, y, 160, 38);
        g.setFont(FONT_BOLD_8);
        drawText(g, dayNight.getFormattedTime(), x + 8, y + 14, TEXT_LIGHT);
        drawText(g, "Weather: " + dayNight.getWeather().displayName, x + 8, y + 25, new Color(255, 210, 80));
        drawText(g, "Season: " + dayNight.getSeason().displayName, x + 8, y + 35, new Color(150, 230, 170));
    }

    // ── Top-centre: Echo status ───────────────────────────────────────────────────────

    private void renderEchoStatus(Graphics2D g, EchoSandboxClone echo, int viewW) {
        String statusText;
        Color textColor;
        if (echo != null && echo.isRecording) {
            statusText = "ECHO RECORDING  [X] stop";
            textColor = new Color(255, 100, 210);
        } else if (echo != null && echo.isActive) {
            statusText = "ECHO CLONE ACTIVE  [X] stop";
            textColor = Color.WHITE;
        } else if (echo != null && echo.hasRecordedData()) {
            statusText = "ECHO READY  [X] replay";
            textColor = new Color(0, 240, 255);
        } else {
            statusText = "ECHO READY  [X] record";
            textColor = new Color(0, 240, 255);
        }
        g.setFont(FONT_BOLD_8);
        int textW = g.getFontMetrics().stringWidth(statusText);
        int w = textW + 18, x = (viewW - w) / 2;
        drawPanel(g, x, MARGIN, w, 16);
        drawText(g, statusText, x + 9, MARGIN + 11, textColor);
    }

    // ── Bottom-centre: hotbar ─────────────────────────────────────────────────────────

    private void renderQuickSlots(Graphics2D g, Inventory inv, int viewW, int viewH) {
        int slotSize = SLOT_SIZE;
        int spacing = 3;
        int totalW = Inventory.QUICK_SLOT_COUNT * slotSize + (Inventory.QUICK_SLOT_COUNT - 1) * spacing;
        int startX = (viewW - totalW) / 2;
        int startY = viewH - MARGIN - slotSize - 4;

        drawPanel(g, startX - 6, startY - 5, totalW + 12, slotSize + 10);

        for (int i = 0; i < Inventory.QUICK_SLOT_COUNT; i++) {
            int sx = startX + i * (slotSize + spacing);
            Inventory.SlotItem item = inv.getSlot(i);
            boolean isSelected = (i == inv.getSelectedSlot());

            g.setColor(isSelected ? new Color(38, 58, 86) : new Color(10, 14, 22));
            g.fillRect(sx, startY, slotSize, slotSize);
            g.setColor(isSelected ? EDGE_GOLD : new Color(70, 62, 48));
            g.drawRect(sx, startY, slotSize - 1, slotSize - 1);
            if (isSelected) {
                g.setColor(new Color(255, 214, 110, 90));
                g.drawRect(sx + 1, startY + 1, slotSize - 3, slotSize - 3);
            }

            g.setFont(FONT_BOLD_7);
            drawText(g, String.valueOf(i + 1), sx + 2, startY + 8, TEXT_DIM);

            if (item != null) {
                if (item.blockType != null) {
                    g.setColor(item.blockType.baseColor);
                    g.fillRect(sx + 5, startY + 6, 11, 11);
                    g.setColor(item.blockType.highlightColor);
                    g.drawRect(sx + 5, startY + 6, 11, 11);
                } else {
                    g.setColor(new Color(0, 240, 255));
                    g.fillOval(sx + 6, startY + 6, 9, 9);
                }
                if (item.count > 1) {
                    String countStr = String.valueOf(item.count);
                    drawText(g, countStr, sx + slotSize - g.getFontMetrics().stringWidth(countStr) - 2,
                             startY + slotSize - 2, TEXT_LIGHT);
                }
            }
        }

        Inventory.SlotItem selected = inv.getSelectedItem();
        if (selected != null) {
            g.setFont(FONT_BOLD_8);
            drawCentered(g, selected.name + " (" + selected.count + ")", viewW / 2, startY - 9, TEXT_LIGHT);
        }
    }

    // ── Bottom-right: shortcut badges ─────────────────────────────────────────────────

    private void renderNavButtons(Graphics2D g, HudInfo info, int viewW, int viewH) {
        String[] keys = {"I", "C", "J", "H"};
        String[] names = {"Bag", "Craft", "Quest", "Help"};
        int bw = 28, bh = 28, gap = 3;
        int totalW = keys.length * bw + (keys.length - 1) * gap;
        int x0 = viewW - MARGIN - totalW, y0 = viewH - MARGIN - bh;
        for (int i = 0; i < keys.length; i++) {
            int x = x0 + i * (bw + gap);
            drawPanel(g, x, y0, bw, bh);
            BufferedImage icon = (info != null && i < info.navIcons.length) ? info.navIcons[i] : null;
            if (icon != null) {
                g.drawImage(icon, x + (bw - 14) / 2, y0 + 4, 14, 14, null);
            } else {
                g.setFont(FONT_BOLD_9);
                drawCentered(g, "?", x + bw / 2, y0 + 15, TEXT_GOLD);
            }
            g.setFont(FONT_BOLD_7);
            drawText(g, keys[i], x + 4, y0 + 10, TEXT_GOLD);
            g.setFont(FONT_PLAIN_7);
            drawCentered(g, names[i], x + bw / 2, y0 + bh - 5, TEXT_LIGHT);
        }
    }

    // ── Bottom-left: controls panel ───────────────────────────────────────────────────

    private void renderControlsPanel(Graphics2D g, int viewH) {
        String[] lines = {
            "WASD Move   SPACE Jump   SHIFT Dash",
            "LMB Attack/Mine  RMB Place  1-8 Slot",
            "Q/E Spell  X Echo  F Talk  ESC Menu"
        };
        int w = 190, h = 4 + lines.length * 10 + 6;
        int x = MARGIN, y = viewH - MARGIN - h;
        drawPanel(g, x, y, w, h);
        g.setFont(FONT_PLAIN_7);
        for (int i = 0; i < lines.length; i++) {
            drawText(g, lines[i], x + 8, y + 13 + i * 10, TEXT_DIM);
        }
    }
}
