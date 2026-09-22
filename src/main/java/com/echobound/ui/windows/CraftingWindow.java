package com.echobound.ui.windows;

import com.echobound.crafting.CraftingEngine;
import com.echobound.crafting.CraftingRecipe;
import com.echobound.crafting.CraftingStationType;
import com.echobound.core.UnifiedGameContext;
import com.echobound.items.ItemDefinition;
import com.echobound.items.ItemRegistry;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

public class CraftingWindow {
    private final CraftingStationType[] stations = CraftingStationType.values();
    private int currentStationIndex = 0;
    private int recipeCursor = 0;
    private String statusMessage = "";
    private float statusTimer = 0.0f;

    public void handleKeyPress(int keyCode, UnifiedGameContext ctx) {
        List<CraftingRecipe> recipes = getRecipesForCurrentStation();

        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            currentStationIndex = (currentStationIndex - 1 + stations.length) % stations.length;
            recipeCursor = 0;
        } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            currentStationIndex = (currentStationIndex + 1) % stations.length;
            recipeCursor = 0;
        } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            if (!recipes.isEmpty()) {
                recipeCursor = (recipeCursor - 1 + recipes.size()) % recipes.size();
            }
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            if (!recipes.isEmpty()) {
                recipeCursor = (recipeCursor + 1) % recipes.size();
            }
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            craftSelected(ctx);
        }
    }

    public List<CraftingRecipe> getRecipesForCurrentStation() {
        return CraftingEngine.getRecipesForStation(stations[currentStationIndex]);
    }

    public CraftingRecipe getSelectedRecipe() {
        List<CraftingRecipe> list = getRecipesForCurrentStation();
        if (recipeCursor >= 0 && recipeCursor < list.size()) {
            return list.get(recipeCursor);
        }
        return null;
    }

    public boolean craftSelected(UnifiedGameContext ctx) {
        CraftingRecipe recipe = getSelectedRecipe();
        if (recipe == null) return false;

        boolean success = ctx.craftRecipe(recipe);
        if (success) {
            statusMessage = "Crafted " + recipe.resultCount + "x " + recipe.recipeName + "!";
            statusTimer = 2.0f;
            // Spawn FX
            ctx.particleFXManager.spawnLootSparkles(ctx.player.pos.x, ctx.player.pos.y, ctx.player.pos.z);
            ctx.floatingTextManager.spawnLoot(ctx.player.pos.x, ctx.player.pos.y, ctx.player.pos.z + 16.0f,
                    recipe.recipeName, recipe.resultCount);
            return true;
        } else {
            statusMessage = "Missing required ingredients!";
            statusTimer = 2.0f;
            return false;
        }
    }

    public void update(float dt) {
        if (statusTimer > 0) {
            statusTimer -= dt;
            if (statusTimer <= 0) {
                statusMessage = "";
            }
        }
    }

    public void render(Graphics2D g, UnifiedGameContext ctx, int width, int height) {
        // Overlay backdrop
        g.setColor(new Color(12, 16, 26, 240));
        int winW = 280;
        int winH = 160;
        int startX = (width - winW) / 2;
        int startY = (height - winH) / 2;

        g.fillRoundRect(startX, startY, winW, winH, 8, 8);
        g.setColor(new Color(255, 180, 50));
        g.drawRoundRect(startX, startY, winW, winH, 8, 8);

        // Title & Station Tab bar
        g.setFont(new Font("Monospaced", Font.BOLD, 9));
        g.setColor(new Color(255, 215, 0));
        g.drawString("CRAFTING FOUNDRY", startX + 10, startY + 12);

        // Station Tabs (Left / Right indicators)
        String stationName = stations[currentStationIndex].displayName.toUpperCase();
        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        g.setColor(new Color(100, 230, 255));
        g.drawString("< [" + (currentStationIndex + 1) + "/" + stations.length + "] " + stationName + " >", startX + 130, startY + 12);

        // Left Panel: Recipe List
        int listStartX = startX + 10;
        int listStartY = startY + 22;
        int listW = 120;
        int listH = 105;

        g.setColor(new Color(18, 22, 34));
        g.fillRect(listStartX, listStartY, listW, listH);
        g.setColor(new Color(45, 60, 80));
        g.drawRect(listStartX, listStartY, listW, listH);

        List<CraftingRecipe> recipes = getRecipesForCurrentStation();
        if (recipes.isEmpty()) {
            g.setFont(new Font("Monospaced", Font.ITALIC, 7));
            g.setColor(Color.GRAY);
            g.drawString("No recipes for station.", listStartX + 8, listStartY + 20);
        } else {
            for (int i = 0; i < recipes.size(); i++) {
                CraftingRecipe r = recipes.get(i);
                int ry = listStartY + 4 + i * 14;
                boolean isCursor = (i == recipeCursor);
                boolean canCraft = CraftingEngine.canCraft(r, ctx.playerInventory);

                if (isCursor) {
                    g.setColor(new Color(40, 80, 120));
                    g.fillRect(listStartX + 2, ry - 1, listW - 4, 13);
                }

                g.setFont(new Font("Monospaced", isCursor ? Font.BOLD : Font.PLAIN, 7));
                g.setColor(canCraft ? new Color(100, 255, 160) : (isCursor ? Color.WHITE : Color.LIGHT_GRAY));
                String label = (isCursor ? "> " : "  ") + r.recipeName;
                if (label.length() > 17) label = label.substring(0, 17);
                g.drawString(label, listStartX + 4, ry + 9);
            }
        }

        // Right Panel: Selected Recipe Inspector & Ingredients
        int inspStartX = startX + 138;
        int inspStartY = startY + 22;
        int inspW = 132;
        int inspH = 105;

        g.setColor(new Color(18, 22, 34));
        g.fillRect(inspStartX, inspStartY, inspW, inspH);
        g.setColor(new Color(45, 60, 80));
        g.drawRect(inspStartX, inspStartY, inspW, inspH);

        CraftingRecipe selected = getSelectedRecipe();
        if (selected != null) {
            ItemDefinition resDef = ItemRegistry.get(selected.resultItemId);
            g.setFont(new Font("Monospaced", Font.BOLD, 8));
            g.setColor(new Color(255, 220, 90));
            g.drawString(selected.recipeName, inspStartX + 6, inspStartY + 12);

            g.setFont(new Font("Monospaced", Font.PLAIN, 7));
            g.setColor(Color.WHITE);
            g.drawString("Yield: " + selected.resultCount + "x " + (resDef != null ? resDef.name : ""), inspStartX + 6, inspStartY + 24);

            // Ingredients checklist
            g.setFont(new Font("Monospaced", Font.BOLD, 7));
            g.setColor(new Color(180, 200, 220));
            g.drawString("REQUIRED MATERIALS:", inspStartX + 6, inspStartY + 38);

            int ingY = inspStartY + 48;
            for (Map.Entry<Integer, Integer> ing : selected.ingredients.entrySet()) {
                int itemId = ing.getKey();
                int required = ing.getValue();
                int owned = ctx.playerInventory.getOrDefault(itemId, 0);
                boolean enough = owned >= required;

                ItemDefinition ingDef = ItemRegistry.get(itemId);
                String ingName = (ingDef != null) ? ingDef.name : ("Item #" + itemId);

                g.setFont(new Font("Monospaced", Font.PLAIN, 7));
                g.setColor(enough ? new Color(80, 255, 120) : new Color(255, 100, 100));
                g.drawString(String.format(" %s: %d/%d %s", ingName, owned, required, enough ? "[OK]" : "[NEED]"), inspStartX + 6, ingY);
                ingY += 10;
            }

            // Craft button / status
            boolean canCraft = CraftingEngine.canCraft(selected, ctx.playerInventory);
            g.setColor(canCraft ? new Color(30, 120, 60) : new Color(60, 60, 60));
            g.fillRect(inspStartX + 12, inspStartY + 82, inspW - 24, 16);
            g.setColor(canCraft ? new Color(80, 255, 140) : Color.GRAY);
            g.drawRect(inspStartX + 12, inspStartY + 82, inspW - 24, 16);

            g.setFont(new Font("Monospaced", Font.BOLD, 8));
            g.setColor(Color.WHITE);
            g.drawString(canCraft ? "[ENTER] CRAFT" : "LOCKED", inspStartX + 34, inspStartY + 93);
        }

        // Bottom Controls / Status
        int botY = startY + 132;
        g.setColor(new Color(20, 28, 44));
        g.fillRect(startX + 8, botY, winW - 16, 20);
        g.setColor(new Color(50, 70, 95));
        g.drawRect(startX + 8, botY, winW - 16, 20);

        g.setFont(new Font("Monospaced", Font.PLAIN, 7));
        if (!statusMessage.isEmpty()) {
            g.setColor(new Color(255, 230, 80));
            g.drawString(statusMessage, startX + 14, botY + 12);
        } else {
            g.setColor(new Color(160, 200, 230));
            g.drawString("Controls: [A/D] Station | [W/S] Recipe | [ENTER] Craft | [C/ESC] Close", startX + 14, botY + 12);
        }
    }

    public int getCurrentStationIndex() {
        return currentStationIndex;
    }

    public void setCurrentStationIndex(int idx) {
        this.currentStationIndex = idx % stations.length;
    }

    public int getRecipeCursor() {
        return recipeCursor;
    }

    public void setRecipeCursor(int cursor) {
        this.recipeCursor = cursor;
    }
}
