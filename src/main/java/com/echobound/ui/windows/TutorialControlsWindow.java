package com.echobound.ui.windows;

import com.echobound.core.UnifiedGameContext;
import com.echobound.settings.SettingsManager;
import com.echobound.tutorial.TutorialManager;
import com.echobound.tutorial.TutorialStep;

import java.awt.*;
import java.awt.event.KeyEvent;

public class TutorialControlsWindow {
    public final TutorialManager tutorialManager = new TutorialManager();
    private int currentTab = 0; // 0 = Controls Guide, 1 = Tutorial Steps
    private boolean dontShowAgainChecked = false;

    public void handleKeyPress(int keyCode, UnifiedGameContext ctx, SettingsManager settingsManager) {
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_TAB) {
            currentTab = (currentTab == 0) ? 1 : 0;
        } else if (keyCode == KeyEvent.VK_1) {
            currentTab = 0;
        } else if (keyCode == KeyEvent.VK_2) {
            currentTab = 1;
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            // Confirm / Close
            if (settingsManager != null && dontShowAgainChecked) {
                settingsManager.getSettings().firstTimeUser = false;
                settingsManager.save();
            }
        } else if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_X) {
            // Toggle dont show again
            toggleDontShowAgain(settingsManager);
        }
    }

    public void toggleDontShowAgain(SettingsManager settingsManager) {
        dontShowAgainChecked = !dontShowAgainChecked;
        if (settingsManager != null) {
            settingsManager.getSettings().firstTimeUser = !dontShowAgainChecked;
            settingsManager.save();
        }
    }

    public boolean isDontShowAgainChecked() {
        return dontShowAgainChecked;
    }

    public void setDontShowAgainChecked(boolean checked) {
        this.dontShowAgainChecked = checked;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(int tab) {
        this.currentTab = tab % 2;
    }

    public void render(Graphics2D g, UnifiedGameContext ctx, int width, int height) {
        int winW = 300;
        int winH = 168;
        int startX = (width - winW) / 2;
        int startY = (height - winH) / 2;

        // Window background
        g.setColor(new Color(12, 16, 26, 245));
        g.fillRoundRect(startX, startY, winW, winH, 8, 8);
        g.setColor(new Color(0, 240, 255));
        g.drawRoundRect(startX, startY, winW, winH, 8, 8);

        // Header Title & Tabs
        g.setFont(new Font("Monospaced", Font.BOLD, 9));
        g.setColor(new Color(255, 215, 0));
        g.drawString("ECHOBOUND ADVENTURER GUIDE", startX + 10, startY + 12);

        // Tab Buttons
        int tabY = startY + 5;
        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        // Tab 0: Controls
        g.setColor(currentTab == 0 ? new Color(0, 200, 255) : new Color(60, 75, 95));
        g.fillRect(startX + 165, tabY, 58, 12);
        g.setColor(Color.WHITE);
        g.drawString("[1] CONTROLS", startX + 168, tabY + 9);

        // Tab 1: Academy
        g.setColor(currentTab == 1 ? new Color(0, 200, 255) : new Color(60, 75, 95));
        g.fillRect(startX + 228, tabY, 60, 12);
        g.setColor(Color.WHITE);
        g.drawString("[2] TUTORIAL", startX + 231, tabY + 9);

        if (currentTab == 0) {
            renderControlsTab(g, startX, startY, winW, winH);
        } else {
            renderTutorialTab(g, startX, startY, winW, winH);
        }

        // Bottom status & Dismiss bar
        int botY = startY + winH - 22;
        g.setColor(new Color(18, 24, 38));
        g.fillRect(startX + 6, botY, winW - 12, 18);
        g.setColor(new Color(40, 60, 85));
        g.drawRect(startX + 6, botY, winW - 12, 18);

        g.setFont(new Font("Monospaced", Font.PLAIN, 7));
        g.setColor(dontShowAgainChecked ? new Color(100, 255, 140) : Color.LIGHT_GRAY);
        String chk = dontShowAgainChecked ? "[X]" : "[ ]";
        g.drawString(chk + " Don't show on start [D] | [TAB] Switch Tab | [ENTER/ESC] Play", startX + 12, botY + 12);
    }

    private void renderControlsTab(Graphics2D g, int startX, int startY, int winW, int winH) {
        int leftColX = startX + 10;
        int rightColX = startX + 152;
        int curY = startY + 24;

        // Left Column: Traversal & Combat
        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        g.setColor(new Color(255, 200, 80));
        g.drawString("TRAVERSAL & COMBAT", leftColX, curY);
        curY += 11;

        g.setFont(new Font("Monospaced", Font.PLAIN, 7));
        g.setColor(Color.WHITE);
        g.drawString("[W / A / S / D]  : 3D Movement", leftColX, curY); curY += 9;
        g.drawString("[SPACE]          : Jump / Double-Jump", leftColX, curY); curY += 9;
        g.drawString("[SHIFT]          : 8-Directional Dash", leftColX, curY); curY += 9;
        g.drawString("[S + Air]        : Scarf Gliding Descent", leftColX, curY); curY += 9;
        g.drawString("[Left Click]     : Attack / Mine Voxel", leftColX, curY); curY += 9;
        g.drawString("[Right Click]    : Place Selected Block", leftColX, curY); curY += 9;
        g.drawString("[Mouse Wheel]    : Cycle Quickslot Item", leftColX, curY); curY += 13;

        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        g.setColor(new Color(255, 130, 40));
        g.drawString("MAGIC RESONANCE", leftColX, curY);
        curY += 11;
        g.setFont(new Font("Monospaced", Font.PLAIN, 7));
        g.setColor(Color.WHITE);
        g.drawString("[Q] : Fire Tornado (Ember + Gale)", leftColX, curY); curY += 9;
        g.drawString("[E] : Storm Burst (Tide + Volt)", leftColX, curY);

        // Right Column: World & Subsystems
        curY = startY + 24;
        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        g.setColor(new Color(100, 230, 255));
        g.drawString("SYSTEMS & INTERACTION", rightColX, curY);
        curY += 11;

        g.setFont(new Font("Monospaced", Font.PLAIN, 7));
        g.setColor(Color.WHITE);
        g.drawString("[I] / [TAB] : Inventory & Gear & Runes", rightColX, curY); curY += 9;
        g.drawString("[C]         : 8-Station Crafting Foundry", rightColX, curY); curY += 9;
        g.drawString("[J]         : Quest Journal (500 Chaps)", rightColX, curY); curY += 9;
        g.drawString("[X]         : Echo Clone Loop / Replay", rightColX, curY); curY += 9;
        g.drawString("[F]         : Talk to Living NPCs", rightColX, curY); curY += 9;
        g.drawString("[M]         : Mount / Dismount Steed", rightColX, curY); curY += 9;
        g.drawString("[P]         : Cycle Tamed Companions", rightColX, curY); curY += 9;
        g.drawString("[T]         : Dynamic Weather Cycle", rightColX, curY); curY += 9;
        g.drawString("[H] / [F1]  : Open this Help Guide", rightColX, curY); curY += 9;
        g.drawString("[ESC]       : Pause Menu / Back", rightColX, curY);
    }

    private void renderTutorialTab(Graphics2D g, int startX, int startY, int winW, int winH) {
        int listX = startX + 10;
        int curY = startY + 24;

        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        g.setColor(new Color(100, 255, 180));
        g.drawString("BEGINNER ACADEMY WALKTHROUGH (" + tutorialManager.getCompletedCount() + "/" + tutorialManager.getTotalSteps() + " Completed)", listX, curY);
        curY += 12;

        TutorialStep[] steps = TutorialStep.values();
        for (int i = 0; i < steps.length; i++) {
            TutorialStep step = steps[i];
            boolean done = tutorialManager.isStepCompleted(step);
            boolean current = (tutorialManager.getCurrentStep() == step);

            g.setColor(current ? new Color(35, 55, 80) : new Color(20, 26, 38));
            g.fillRect(listX, curY - 2, winW - 20, 18);
            g.setColor(current ? new Color(0, 240, 255) : new Color(45, 60, 80));
            g.drawRect(listX, curY - 2, winW - 20, 18);

            // Checkmark
            g.setFont(new Font("Monospaced", Font.BOLD, 8));
            g.setColor(done ? new Color(80, 255, 120) : (current ? new Color(255, 215, 0) : Color.GRAY));
            g.drawString(done ? "[DONE]" : (current ? "[ACTV]" : "[----]"), listX + 4, curY + 9);

            // Title and instruction
            g.setFont(new Font("Monospaced", Font.BOLD, 7));
            g.setColor(done ? new Color(180, 255, 200) : Color.WHITE);
            g.drawString(step.title + ": " + step.instruction, listX + 44, curY + 9);

            curY += 20;
        }

        // Tips bar
        curY += 2;
        g.setFont(new Font("Monospaced", Font.ITALIC, 7));
        g.setColor(new Color(255, 230, 100));
        g.drawString("Tip: Press [X] to duplicate your character with the Echo clone to solve puzzles!", listX, curY);
    }
}
