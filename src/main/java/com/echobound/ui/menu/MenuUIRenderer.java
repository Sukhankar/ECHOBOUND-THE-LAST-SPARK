package com.echobound.ui.menu;

import com.echobound.save.SaveData;
import com.echobound.save.SaveManager;
import com.echobound.settings.GameSettings;
import com.echobound.settings.SettingsManager;

import java.awt.*;
import java.util.List;

public class MenuUIRenderer {
    private static final Color BG_DARK = new Color(12, 14, 24);
    private static final Color GOLD = new Color(255, 215, 0);
    private static final Color CYAN_ACCENT = new Color(0, 240, 255);
    private static final Color TEXT_MUTED = new Color(140, 150, 180);
    private static final Color CARD_BG = new Color(24, 28, 44);

    public static void render(Graphics2D g, TitleMenuController controller,
                              SaveManager saveManager, SettingsManager settingsManager,
                              int width, int height) {
        g.setColor(BG_DARK);
        g.fillRect(0, 0, width, height);

        GameState state = controller.getCurrentState();
        switch (state) {
            case LOADING -> renderLoadingScreen(g, controller.getLoadingScreen(), width, height);
            case TITLE_MENU -> renderTitleMenu(g, controller, saveManager, width, height);
            case OPTIONS_MENU -> renderOptionsMenu(g, controller, settingsManager.getSettings(), width, height);
            case SAVE_SELECT_MENU -> renderSaveSelectMenu(g, controller, saveManager, width, height);
            default -> {}
        }
    }

    private static void renderLoadingScreen(Graphics2D g, LoadingScreen loading, int width, int height) {
        // Title
        g.setColor(GOLD);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        String title = "ECHOBOUND: THE LAST SPARK";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - tw) / 2, height / 2 - 50);

        // Progress Bar
        int barW = Math.min(260, width - 60);
        int barH = 12;
        int bx = (width - barW) / 2;
        int by = height / 2 - 10;

        g.setColor(new Color(35, 40, 60));
        g.fillRect(bx, by, barW, barH);

        g.setColor(CYAN_ACCENT);
        int fillW = (int) (barW * loading.getProgress());
        g.fillRect(bx, by, fillW, barH);
        g.setColor(Color.WHITE);
        g.drawRect(bx, by, barW, barH);

        // Hint Text
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(TEXT_MUTED);
        String hint = loading.getCurrentHint();
        int hw = g.getFontMetrics().stringWidth(hint);
        g.drawString(hint, (width - hw) / 2, by + 35);

        // Percentage
        String pct = String.format("%d%%", (int) (loading.getProgress() * 100));
        int pw = g.getFontMetrics().stringWidth(pct);
        g.setColor(GOLD);
        g.drawString(pct, (width - pw) / 2, by + 55);
    }

    private static void renderTitleMenu(Graphics2D g, TitleMenuController controller,
                                       SaveManager saveManager, int width, int height) {
        // Logo & Tagline
        g.setColor(GOLD);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        String logo = "ECHOBOUND: THE LAST SPARK";
        int lw = g.getFontMetrics().stringWidth(logo);
        g.drawString(logo, (width - lw) / 2, 50);

        g.setColor(CYAN_ACCENT);
        g.setFont(new Font("Monospaced", Font.ITALIC, 11));
        String sub = "\"The world lost its Echo. We bring it back.\"";
        int sw = g.getFontMetrics().stringWidth(sub);
        g.drawString(sub, (width - sw) / 2, 70);

        // Options
        int startY = 110;
        int spacing = 22;
        int cursor = controller.getTitleCursor();
        boolean hasSave = saveManager.getMostRecentSlot() > 0;

        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        for (int i = 0; i < TitleMenuController.TitleOption.values().length; i++) {
            TitleMenuController.TitleOption opt = TitleMenuController.TitleOption.values()[i];
            boolean selected = (i == cursor);
            boolean disabled = (i == 0 && !hasSave);

            if (disabled) {
                g.setColor(new Color(70, 75, 95));
            } else if (selected) {
                g.setColor(CYAN_ACCENT);
            } else {
                g.setColor(Color.WHITE);
            }

            String text = (selected ? "> " : "  ") + opt.label;
            int ow = g.getFontMetrics().stringWidth(text);
            g.drawString(text, (width - ow) / 2, startY + i * spacing);
        }

        // Bottom Controls Hint
        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(TEXT_MUTED);
        String nav = "[UP/DOWN] Select  |  [ENTER] Confirm";
        int nw = g.getFontMetrics().stringWidth(nav);
        g.drawString(nav, (width - nw) / 2, height - 20);
    }

    private static void renderOptionsMenu(Graphics2D g, TitleMenuController controller,
                                         GameSettings settings, int width, int height) {
        g.setColor(GOLD);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        String title = "OPTIONS & SETTINGS";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - tw) / 2, 45);

        int startY = 85;
        int spacing = 24;
        int cursor = controller.getOptionsCursor();

        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        for (int i = 0; i < TitleMenuController.OptionsItem.values().length; i++) {
            TitleMenuController.OptionsItem item = TitleMenuController.OptionsItem.values()[i];
            boolean selected = (i == cursor);
            g.setColor(selected ? CYAN_ACCENT : Color.WHITE);

            String label = (selected ? "> " : "  ") + item.label + ": ";
            String valueStr = switch (item) {
                case MASTER_VOLUME -> String.format("[%d%%]", (int) (settings.masterVolume * 100));
                case RESOLUTION -> "[" + settings.resolutionProfile.label + "]";
                case CAMERA_SHAKE -> "[" + (settings.cameraShakeEnabled ? "ON" : "OFF") + "]";
                case DEBUG_OVERLAY -> "[" + (settings.showDebugOverlay ? "ON" : "OFF") + "]";
                case BACK -> "[Press Enter to Return]";
            };

            String line = label + valueStr;
            int lw = g.getFontMetrics().stringWidth(line);
            g.drawString(line, (width - lw) / 2, startY + i * spacing);
        }

        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(TEXT_MUTED);
        String nav = "[LEFT/RIGHT] Adjust Setting  |  [ENTER] Toggle / Return";
        int nw = g.getFontMetrics().stringWidth(nav);
        g.drawString(nav, (width - nw) / 2, height - 20);
    }

    private static void renderSaveSelectMenu(Graphics2D g, TitleMenuController controller,
                                            SaveManager saveManager, int width, int height) {
        g.setColor(GOLD);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        String title = "SELECT SAVE GAME SLOT";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - tw) / 2, 40);

        int cardW = Math.min(320, width - 40);
        int cardH = 40;
        int cardX = (width - cardW) / 2;
        int startY = 70;
        int spacing = 50;
        int cursor = controller.getSaveSlotCursor();

        List<SaveData> saves = saveManager.listAllSaves();

        for (int i = 0; i < SaveManager.MAX_SLOTS; i++) {
            boolean selected = (i == cursor);
            int cy = startY + i * spacing;

            g.setColor(CARD_BG);
            g.fillRect(cardX, cy, cardW, cardH);

            g.setColor(selected ? CYAN_ACCENT : new Color(60, 70, 100));
            g.drawRect(cardX, cy, cardW, cardH);

            SaveData data = (i < saves.size()) ? saves.get(i) : null;
            g.setFont(new Font("Monospaced", Font.BOLD, 11));

            if (data != null) {
                g.setColor(selected ? GOLD : Color.WHITE);
                g.drawString(String.format("Slot %d: %s", i + 1, data.profileName), cardX + 10, cy + 16);
                g.setFont(new Font("Monospaced", Font.PLAIN, 10));
                g.setColor(TEXT_MUTED);
                g.drawString(String.format("Ch. %d (%d/500) | HP: %d/%d",
                    data.currentChapter, data.completedSubChapters, data.playerHealth, data.playerMaxHealth),
                    cardX + 10, cy + 32);
            } else {
                g.setColor(new Color(110, 120, 150));
                g.drawString(String.format("Slot %d: [Empty Slot - Press Enter to Start]", i + 1),
                    cardX + 10, cy + 24);
            }
        }

        // Back button
        boolean backSelected = (cursor == SaveManager.MAX_SLOTS);
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.setColor(backSelected ? CYAN_ACCENT : TEXT_MUTED);
        String back = (backSelected ? "> " : "  ") + "Back to Title";
        int bw = g.getFontMetrics().stringWidth(back);
        g.drawString(back, (width - bw) / 2, startY + SaveManager.MAX_SLOTS * spacing + 10);
    }
}
