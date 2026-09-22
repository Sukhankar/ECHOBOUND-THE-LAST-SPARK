package com.echobound.ui.windows;

import com.echobound.core.UnifiedGameContext;

import java.awt.*;
import java.awt.event.KeyEvent;

public class WindowManager {
    private InGameWindowType activeWindow = InGameWindowType.NONE;

    public final InventoryEquipmentWindow inventoryWindow = new InventoryEquipmentWindow();
    public final CraftingWindow craftingWindow = new CraftingWindow();
    public final QuestLogWindow questLogWindow = new QuestLogWindow();

    public InGameWindowType getActiveWindow() {
        return activeWindow;
    }

    public void setActiveWindow(InGameWindowType type) {
        this.activeWindow = (type != null) ? type : InGameWindowType.NONE;
    }

    public boolean hasActiveWindow() {
        return activeWindow != InGameWindowType.NONE;
    }

    public void toggleWindow(InGameWindowType type) {
        if (activeWindow == type) {
            activeWindow = InGameWindowType.NONE;
        } else {
            activeWindow = type;
        }
    }

    public void closeAllWindows() {
        activeWindow = InGameWindowType.NONE;
    }

    public boolean handleKeyPress(int keyCode, UnifiedGameContext ctx) {
        if (!hasActiveWindow()) {
            // Check window toggles
            if (keyCode == KeyEvent.VK_I || keyCode == KeyEvent.VK_TAB) {
                toggleWindow(InGameWindowType.INVENTORY_EQUIPMENT);
                return true;
            } else if (keyCode == KeyEvent.VK_C) {
                toggleWindow(InGameWindowType.CRAFTING);
                return true;
            } else if (keyCode == KeyEvent.VK_J) {
                toggleWindow(InGameWindowType.QUEST_LOG);
                return true;
            }
            return false;
        }

        // Window is currently open
        if (keyCode == KeyEvent.VK_ESCAPE) {
            closeAllWindows();
            return true;
        }

        // Toggle key again closes it
        if ((keyCode == KeyEvent.VK_I || keyCode == KeyEvent.VK_TAB) && activeWindow == InGameWindowType.INVENTORY_EQUIPMENT) {
            closeAllWindows();
            return true;
        }
        if (keyCode == KeyEvent.VK_C && activeWindow == InGameWindowType.CRAFTING) {
            closeAllWindows();
            return true;
        }
        if (keyCode == KeyEvent.VK_J && activeWindow == InGameWindowType.QUEST_LOG) {
            closeAllWindows();
            return true;
        }

        // Forward to specific active window
        switch (activeWindow) {
            case INVENTORY_EQUIPMENT:
                inventoryWindow.handleKeyPress(keyCode, ctx);
                return true;
            case CRAFTING:
                craftingWindow.handleKeyPress(keyCode, ctx);
                return true;
            case QUEST_LOG:
                questLogWindow.handleKeyPress(keyCode, ctx);
                return true;
            default:
                return false;
        }
    }

    public void update(float dt) {
        if (activeWindow == InGameWindowType.CRAFTING) {
            craftingWindow.update(dt);
        }
    }

    public void render(Graphics2D g, UnifiedGameContext ctx, int width, int height) {
        if (!hasActiveWindow()) return;

        // Semi-transparent dark background tint over world
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, width, height);

        switch (activeWindow) {
            case INVENTORY_EQUIPMENT:
                inventoryWindow.render(g, ctx, width, height);
                break;
            case CRAFTING:
                craftingWindow.render(g, ctx, width, height);
                break;
            case QUEST_LOG:
                questLogWindow.render(g, ctx, width, height);
                break;
            default:
                break;
        }
    }
}
