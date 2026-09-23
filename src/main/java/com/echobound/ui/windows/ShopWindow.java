package com.echobound.ui.windows;

import com.echobound.audio.SoundType;
import com.echobound.core.UnifiedGameContext;
import com.echobound.items.ItemDefinition;
import com.echobound.items.ItemRegistry;
import com.echobound.npc.NPCDefinition;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A merchant's wares — opened via the [B] key while talking to an NPC whose
 * NPCDefinition.isMerchant() is true (see NPCManager for who sells what). Applies
 * FactionManager.getStoreDiscount(), which has existed since Part 4 with nothing in the
 * live game to ever apply it to.
 */
public class ShopWindow {
    private NPCDefinition shopNpc;
    private int cursor = 0;
    private String statusMessage = "";
    private float statusTimer = 0.0f;

    public void open(NPCDefinition npc) {
        this.shopNpc = npc;
        this.cursor = 0;
        this.statusMessage = "";
        this.statusTimer = 0.0f;
    }

    public NPCDefinition getShopNpc() {
        return shopNpc;
    }

    private List<Integer> itemIds() {
        if (shopNpc == null) return List.of();
        return new ArrayList<>(shopNpc.getShopItems().keySet());
    }

    public void handleKeyPress(int keyCode, UnifiedGameContext ctx) {
        List<Integer> ids = itemIds();
        if (ids.isEmpty()) return;

        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            cursor = (cursor - 1 + ids.size()) % ids.size();
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            cursor = (cursor + 1) % ids.size();
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            buySelected(ctx);
        }
    }

    /** Base price with the buyer's faction-reputation discount/markup applied and floored at 1. */
    public int getPriceFor(int itemId, UnifiedGameContext ctx) {
        Integer base = shopNpc.getShopItems().get(itemId);
        if (base == null) return 0;
        float discount = ctx.factionManager.getStoreDiscount(shopNpc.faction);
        return Math.max(1, Math.round(base * (1.0f - discount)));
    }

    public boolean buySelected(UnifiedGameContext ctx) {
        List<Integer> ids = itemIds();
        if (cursor < 0 || cursor >= ids.size()) return false;

        int itemId = ids.get(cursor);
        int price = getPriceFor(itemId, ctx);
        int gold = ctx.playerInventory.getOrDefault(ItemRegistry.CURRENCY_SPARK_COIN, 0);

        if (gold < price) {
            statusMessage = "Not enough Spark Coins!";
            statusTimer = 2.0f;
            return false;
        }

        ctx.playerInventory.put(ItemRegistry.CURRENCY_SPARK_COIN, gold - price);
        ctx.playerInventory.merge(itemId, 1, Integer::sum);

        ItemDefinition def = ItemRegistry.get(itemId);
        statusMessage = "Bought " + (def != null ? def.name : "item") + "!";
        statusTimer = 2.0f;
        ctx.soundEngine.play(SoundType.CRAFT_SUCCESS);
        ctx.floatingTextManager.spawnLoot(ctx.player.pos.x, ctx.player.pos.y, ctx.player.pos.z + 16.0f,
                def != null ? def.name : "Item", 1);
        return true;
    }

    public void update(float dt) {
        if (statusTimer > 0) {
            statusTimer -= dt;
            if (statusTimer <= 0) statusMessage = "";
        }
    }

    public void render(Graphics2D g, UnifiedGameContext ctx, int width, int height) {
        int winW = 260, winH = 170;
        int startX = (width - winW) / 2;
        int startY = (height - winH) / 2;

        g.setColor(new Color(12, 16, 26, 245));
        g.fillRoundRect(startX, startY, winW, winH, 8, 8);
        g.setColor(new Color(255, 215, 0));
        g.drawRoundRect(startX, startY, winW, winH, 8, 8);

        String shopTitle = (shopNpc != null ? shopNpc.name.toUpperCase() + "'S WARES" : "SHOP");
        g.setFont(new Font("Monospaced", Font.BOLD, 9));
        g.setColor(new Color(255, 215, 0));
        g.drawString(shopTitle, startX + 10, startY + 14);

        int gold = ctx.playerInventory.getOrDefault(ItemRegistry.CURRENCY_SPARK_COIN, 0);
        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        g.setColor(new Color(255, 230, 120));
        String goldStr = gold + " Spark Coins";
        int gw = g.getFontMetrics().stringWidth(goldStr);
        g.drawString(goldStr, startX + winW - gw - 10, startY + 14);

        int listX = startX + 10, listY = startY + 24, listW = winW - 20, listH = 110;
        g.setColor(new Color(18, 22, 34));
        g.fillRect(listX, listY, listW, listH);
        g.setColor(new Color(45, 60, 80));
        g.drawRect(listX, listY, listW, listH);

        List<Integer> ids = itemIds();
        if (ids.isEmpty()) {
            g.setFont(new Font("Monospaced", Font.ITALIC, 8));
            g.setColor(Color.GRAY);
            g.drawString("Nothing for sale.", listX + 8, listY + 20);
        } else {
            float discount = shopNpc != null ? ctx.factionManager.getStoreDiscount(shopNpc.faction) : 0f;
            for (int i = 0; i < ids.size(); i++) {
                int itemId = ids.get(i);
                ItemDefinition def = ItemRegistry.get(itemId);
                String name = def != null ? def.name : ("Item #" + itemId);
                int price = getPriceFor(itemId, ctx);
                boolean isCursor = (i == cursor);
                boolean canAfford = gold >= price;
                int ry = listY + 4 + i * 14;

                if (isCursor) {
                    g.setColor(new Color(40, 80, 120));
                    g.fillRect(listX + 2, ry - 1, listW - 4, 13);
                }

                g.setFont(new Font("Monospaced", isCursor ? Font.BOLD : Font.PLAIN, 8));
                g.setColor(canAfford ? (isCursor ? Color.WHITE : new Color(210, 220, 235)) : new Color(180, 90, 90));
                String label = (isCursor ? "> " : "  ") + name;
                g.drawString(label, listX + 4, ry + 9);

                String priceStr = price + "g";
                if (discount > 0f) priceStr += " (-" + Math.round(discount * 100) + "%)";
                else if (discount < 0f) priceStr += " (+" + Math.round(-discount * 100) + "%)";
                int pw = g.getFontMetrics().stringWidth(priceStr);
                g.setColor(new Color(255, 215, 100));
                g.drawString(priceStr, listX + listW - pw - 6, ry + 9);
            }
        }

        int botY = startY + winH - 32;
        g.setColor(new Color(20, 28, 44));
        g.fillRect(startX + 8, botY, winW - 16, 24);
        g.setColor(new Color(50, 70, 95));
        g.drawRect(startX + 8, botY, winW - 16, 24);

        g.setFont(new Font("Monospaced", Font.PLAIN, 7));
        if (!statusMessage.isEmpty()) {
            g.setColor(new Color(255, 230, 80));
            g.drawString(statusMessage, startX + 14, botY + 14);
        } else {
            g.setColor(new Color(160, 200, 230));
            g.drawString("[W/S] Select  [ENTER] Buy  [ESC] Leave", startX + 14, botY + 14);
        }
    }
}
