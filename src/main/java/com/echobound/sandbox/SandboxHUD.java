package com.echobound.sandbox;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SandboxHUD {
    private final Font hudFont = new Font(Font.MONOSPACED, Font.BOLD, 10);
    private final Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 11);

    // Bottom-of-screen layout is stacked from these two reserved bands, bottom-up, so the
    // hotbar and the control-hints strip can never overlap regardless of platform font metrics:
    // [ active-item label ] [ hotbar slots ] [ control hints ]  <- bottom edge of screen
    private static final int HINT_BAR_HEIGHT = 13;
    private static final int SLOT_SIZE = 22;

    // Real CC0 heart art (see RealPixelAssetPipeline.buildHeartIcon()) — optional so this
    // class still works, drawing the old procedural heart shape, for any caller that
    // doesn't have an AssetManager on hand (e.g. the legacy SandboxGameEngine path).
    private final BufferedImage heartIcon;

    public SandboxHUD() {
        this(null);
    }

    public SandboxHUD(BufferedImage heartIcon) {
        this.heartIcon = heartIcon;
    }

    public void render(Graphics2D g, PlayerSandboxEntity player, EchoSandboxClone echo,
                       DayNightCycle dayNight, int viewW, int viewH) {
        g.setFont(hudFont);

        // 1. Health Hearts (Top Left)
        renderHearts(g, player.health, player.maxHealth, 10, 10);

        // 2. Spark Energy Bar (Below Hearts)
        renderSparkBar(g, player.sparkEnergy, player.maxSparkEnergy, 10, 26);

        // 3. Day / Night & Weather (Top Right)
        renderTimeAndWeather(g, dayNight, viewW - 170, 10);

        // 4. Echo Shift Status Banner (Top Center)
        renderEchoStatus(g, echo, viewW);

        // 5. 8 Quick Slots (Bottom Center)
        renderQuickSlots(g, player.inventory, viewW, viewH);

        // 6. Action Control Hints (Bottom Strip)
        renderControlHints(g, viewW, viewH);
    }

    private void renderHearts(Graphics2D g, int health, int maxHealth, int x, int y) {
        int hearts = maxHealth / 2;
        for (int i = 0; i < hearts; i++) {
            int hx = x + i * 14;
            int hpForHeart = health - i * 2;

            if (heartIcon != null) {
                renderHeartIcon(g, hpForHeart, hx, y);
            } else {
                if (hpForHeart >= 2) {
                    g.setColor(new Color(235, 45, 75));
                } else if (hpForHeart == 1) {
                    g.setColor(new Color(245, 130, 80));
                } else {
                    g.setColor(new Color(60, 60, 70));
                }
                g.fillOval(hx, y, 5, 5);
                g.fillOval(hx + 4, y, 5, 5);
                int[] px = {hx, hx + 9, hx + 4};
                int[] py = {y + 3, y + 3, y + 9};
                g.fillPolygon(px, py, 3);
            }
        }
    }

    /** Draws one real heart-icon frame: full/half/empty are the same base art with a tint
     *  wash (half = darkened, empty = desaturated to grey) — same technique used throughout
     *  this asset pass for synthesizing state variants from a single real source image. */
    private void renderHeartIcon(Graphics2D g, int hpForHeart, int hx, int y) {
        int size = 11;
        g.drawImage(heartIcon, hx, y, size, size, null);
        if (hpForHeart == 1) {
            g.setColor(new Color(20, 20, 25, 130));
            g.fillRect(hx + size / 2, y, size - size / 2, size);
        } else if (hpForHeart < 1) {
            g.setColor(new Color(40, 40, 45, 190));
            g.fillRect(hx, y, size, size);
        }
    }

    private void renderSparkBar(Graphics2D g, float spark, float maxSpark, int x, int y) {
        int barW = 70;
        int barH = 5;
        g.setColor(new Color(15, 20, 30, 200));
        g.fillRect(x, y, barW, barH);

        float ratio = Math.max(0.0f, Math.min(1.0f, spark / maxSpark));
        g.setColor(new Color(0, 240, 255));
        g.fillRect(x, y, (int) (barW * ratio), barH);

        g.setColor(new Color(0, 180, 200));
        g.drawRect(x, y, barW, barH);
    }

    private void renderTimeAndWeather(Graphics2D g, DayNightCycle dayNight, int x, int y) {
        g.setColor(new Color(12, 16, 24, 210));
        g.fillRoundRect(x, y, 160, 38, 4, 4);
        g.setColor(new Color(0, 240, 255, 140));
        g.drawRoundRect(x, y, 160, 38, 4, 4);

        g.setColor(Color.WHITE);
        g.drawString(dayNight.getFormattedTime(), x + 6, y + 12);
        g.setColor(new Color(255, 210, 80));
        g.drawString("Weather: " + dayNight.getWeather().displayName, x + 6, y + 24);
        g.setColor(new Color(150, 230, 170));
        g.drawString("Season: " + dayNight.getSeason().displayName, x + 6, y + 36);
    }

    private void renderEchoStatus(Graphics2D g, EchoSandboxClone echo, int viewW) {
        String statusText;
        Color textColor;
        Color borderColor;

        if (echo != null && echo.isRecording) {
            statusText = "ECHO RECORDING [E: STOP]";
            textColor = new Color(255, 80, 200);
            borderColor = new Color(255, 80, 200, 180);
        } else if (echo != null && echo.isActive) {
            statusText = "ECHO CLONE ACTIVE";
            textColor = Color.WHITE;
            borderColor = new Color(255, 255, 255, 180);
        } else {
            statusText = "ECHO READY [E: RECORD | Q: REPLAY]";
            textColor = new Color(0, 240, 255);
            borderColor = new Color(0, 240, 255, 140);
        }

        int textW = g.getFontMetrics().stringWidth(statusText);
        int sx = (viewW - textW) / 2;
        int sy = 10;

        g.setColor(new Color(10, 14, 22, 210));
        g.fillRoundRect(sx - 8, sy, textW + 16, 16, 4, 4);
        g.setColor(borderColor);
        g.drawRoundRect(sx - 8, sy, textW + 16, 16, 4, 4);
        g.setColor(textColor);
        g.drawString(statusText, sx, sy + 12);
    }

    private void renderQuickSlots(Graphics2D g, Inventory inv, int viewW, int viewH) {
        int slotSize = SLOT_SIZE;
        int spacing = 3;
        int totalW = Inventory.QUICK_SLOT_COUNT * slotSize + (Inventory.QUICK_SLOT_COUNT - 1) * spacing;
        int startX = (viewW - totalW) / 2;
        // Sit directly above the reserved control-hints band, with a couple px of breathing room.
        int startY = viewH - HINT_BAR_HEIGHT - slotSize - 2;

        for (int i = 0; i < Inventory.QUICK_SLOT_COUNT; i++) {
            int sx = startX + i * (slotSize + spacing);
            Inventory.SlotItem item = inv.getSlot(i);
            boolean isSelected = (i == inv.getSelectedSlot());

            // Slot Background
            g.setColor(isSelected ? new Color(30, 48, 70, 240) : new Color(15, 20, 28, 200));
            g.fillRect(sx, startY, slotSize, slotSize);

            // Slot Border
            g.setColor(isSelected ? new Color(255, 215, 60) : new Color(60, 75, 95));
            g.drawRect(sx, startY, slotSize - 1, slotSize - 1);

            // Number shortcut indicator
            g.setColor(new Color(160, 170, 185));
            g.drawString(String.valueOf(i + 1), sx + 2, startY + 8);

            // Item representation
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

                // Item count
                if (item.count > 1) {
                    g.setColor(Color.WHITE);
                    String countStr = String.valueOf(item.count);
                    g.drawString(countStr, sx + slotSize - g.getFontMetrics().stringWidth(countStr) - 2, startY + slotSize - 2);
                }
            }
        }

        // Active Item Label
        Inventory.SlotItem selected = inv.getSelectedItem();
        if (selected != null) {
            String label = selected.name + " (" + selected.count + ")";
            int lw = g.getFontMetrics().stringWidth(label);
            g.setColor(Color.WHITE);
            g.drawString(label, (viewW - lw) / 2, startY - 4);
        }
    }

    private void renderControlHints(Graphics2D g, int viewW, int viewH) {
        String hints = "[WASD] Move  [SPACE] Jump/Glide  [C] Dash  [F] Mine  [G] Place  [1-8] Quick Slot  [T] Weather";

        // The full hint string is comfortably wider than the 640px internal canvas at a
        // normal HUD font size, so pick the largest size that still fits rather than letting
        // it overflow past the screen edges (which used to visually bleed into the hotbar).
        int margin = 8;
        int fontSize = 9;
        FontMetrics fm;
        int hw;
        do {
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
            fm = g.getFontMetrics();
            hw = fm.stringWidth(hints);
            fontSize--;
        } while (hw > viewW - margin && fontSize >= 5);

        int sx = (viewW - hw) / 2;
        // Baseline sits inside the reserved bottom band, below the hotbar it never touches.
        int sy = viewH - (HINT_BAR_HEIGHT - fm.getAscent()) - 1;

        g.setColor(new Color(140, 160, 185));
        g.drawString(hints, sx, sy);
    }
}
