package com.echobound.ui.windows;

import com.echobound.combat.ModdedWeapon;
import com.echobound.combat.RuneType;
import com.echobound.core.UnifiedGameContext;
import com.echobound.items.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryEquipmentWindow {
    private int cursorIndex = 0;
    private int selectedEquipSlot = -1; // -1 none, 0..5 for HEAD..CORE

    public void handleKeyPress(int keyCode, UnifiedGameContext ctx) {
        int capacity = ctx.backpackTier.capacity;
        int cols = 8;
        int rows = (capacity + cols - 1) / cols;

        if (keyCode == KeyEvent.VK_RIGHT) {
            cursorIndex = (cursorIndex + 1) % capacity;
        } else if (keyCode == KeyEvent.VK_LEFT) {
            cursorIndex = (cursorIndex - 1 + capacity) % capacity;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            if (cursorIndex + cols < capacity) {
                cursorIndex += cols;
            }
        } else if (keyCode == KeyEvent.VK_UP) {
            if (cursorIndex - cols >= 0) {
                cursorIndex -= cols;
            }
        } else if (keyCode == KeyEvent.VK_E || keyCode == KeyEvent.VK_ENTER) {
            // Try equipping selected item
            equipSelectedItem(ctx);
        } else if (keyCode == KeyEvent.VK_U) {
            // Unequip item in selected equipment slot
            unequipSlot(ctx);
        } else if (keyCode == KeyEvent.VK_1 || keyCode == KeyEvent.VK_2 || keyCode == KeyEvent.VK_3) {
            // Socket rune into active weapon socket 0, 1, 2
            int socketIdx = keyCode - KeyEvent.VK_1;
            socketRuneIntoWeapon(ctx, socketIdx);
        }
    }

    public List<Integer> getItemList(UnifiedGameContext ctx) {
        List<Integer> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : ctx.playerInventory.entrySet()) {
            if (entry.getValue() > 0) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    public int getSelectedItem(UnifiedGameContext ctx) {
        List<Integer> list = getItemList(ctx);
        if (cursorIndex >= 0 && cursorIndex < list.size()) {
            return list.get(cursorIndex);
        }
        return -1;
    }

    public boolean equipSelectedItem(UnifiedGameContext ctx) {
        int itemId = getSelectedItem(ctx);
        if (itemId <= 0) return false;

        ItemDefinition def = ItemRegistry.get(itemId);
        if (def == null) return false;

        EquipmentSlot targetSlot = null;
        if (itemId == ItemRegistry.EQUIP_MINER_HELM) targetSlot = EquipmentSlot.HEAD;
        else if (itemId == ItemRegistry.EQUIP_FOREST_CLOAK) targetSlot = EquipmentSlot.BODY;
        else if (itemId == ItemRegistry.EQUIP_MINER_GLOVES) targetSlot = EquipmentSlot.GLOVES;
        else if (itemId == ItemRegistry.EQUIP_STORM_BOOTS) targetSlot = EquipmentSlot.BOOTS;
        else if (itemId == ItemRegistry.EQUIP_MOON_CHARM) targetSlot = EquipmentSlot.CHARM;
        else if (itemId == ItemRegistry.EQUIP_EMBER_CORE) targetSlot = EquipmentSlot.CORE;

        if (targetSlot != null) {
            // Unequip previous if any
            int prev = ctx.equipmentManager.getEquipped(targetSlot);
            if (prev > 0) {
                ctx.playerInventory.put(prev, ctx.playerInventory.getOrDefault(prev, 0) + 1);
            }

            // Deduct from inventory
            int count = ctx.playerInventory.getOrDefault(itemId, 0);
            if (count <= 1) {
                ctx.playerInventory.remove(itemId);
            } else {
                ctx.playerInventory.put(itemId, count - 1);
            }

            // Equip
            ctx.equipmentManager.equip(targetSlot, itemId);
            return true;
        }
        return false;
    }

    public boolean unequipSlot(UnifiedGameContext ctx) {
        EquipmentSlot[] slots = EquipmentSlot.values();
        if (selectedEquipSlot >= 0 && selectedEquipSlot < slots.length) {
            EquipmentSlot slot = slots[selectedEquipSlot];
            int equippedId = ctx.equipmentManager.getEquipped(slot);
            if (equippedId > 0) {
                ctx.equipmentManager.equip(slot, 0);
                ctx.playerInventory.put(equippedId, ctx.playerInventory.getOrDefault(equippedId, 0) + 1);
                return true;
            }
        }
        return false;
    }

    public boolean socketRuneIntoWeapon(UnifiedGameContext ctx, int socketIdx) {
        int itemId = getSelectedItem(ctx);
        if (itemId <= 0) return false;

        RuneType rune = null;
        if (itemId == ItemRegistry.RUNE_SHARP) rune = RuneType.SHARP;
        else if (itemId == ItemRegistry.RUNE_FLAME) rune = RuneType.FLAME;
        else if (itemId == ItemRegistry.RUNE_FROST) rune = RuneType.FROST;
        else if (itemId == ItemRegistry.RUNE_VOLT) rune = RuneType.VOLT;
        else if (itemId == ItemRegistry.RUNE_ECHO) rune = RuneType.ECHO;
        else if (itemId == ItemRegistry.RUNE_VOID) rune = RuneType.VOID;

        if (rune != null && ctx.activeWeapon != null) {
            boolean success = ctx.activeWeapon.socketRune(socketIdx, rune);
            if (success) {
                int count = ctx.playerInventory.getOrDefault(itemId, 0);
                if (count <= 1) ctx.playerInventory.remove(itemId);
                else ctx.playerInventory.put(itemId, count - 1);
                return true;
            }
        }
        return false;
    }

    public void render(Graphics2D g, UnifiedGameContext ctx, int width, int height) {
        // Overlay backdrop
        g.setColor(new Color(10, 14, 24, 235));
        int winW = 280;
        int winH = 160;
        int startX = (width - winW) / 2;
        int startY = (height - winH) / 2;

        g.fillRoundRect(startX, startY, winW, winH, 8, 8);
        g.setColor(new Color(0, 230, 255));
        g.drawRoundRect(startX, startY, winW, winH, 8, 8);

        // Header Title
        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        g.setColor(new Color(255, 215, 0));
        g.drawString("INVENTORY & EQUIPMENT [" + ctx.backpackTier.displayName + "]", startX + 10, startY + 14);

        // Left Panel: Backpack Grid (8 cols)
        List<Integer> items = getItemList(ctx);
        int cols = 8;
        int slotSize = 14;
        int gap = 2;
        int gridStartX = startX + 10;
        int gridStartY = startY + 22;

        int capacity = ctx.backpackTier.capacity;
        for (int i = 0; i < capacity; i++) {
            int r = i / cols;
            int c = i % cols;
            int sx = gridStartX + c * (slotSize + gap);
            int sy = gridStartY + r * (slotSize + gap);

            // Slot background
            boolean isCursor = (i == cursorIndex);
            g.setColor(isCursor ? new Color(60, 100, 150) : new Color(20, 25, 40));
            g.fillRect(sx, sy, slotSize, slotSize);
            g.setColor(isCursor ? new Color(0, 255, 255) : new Color(50, 70, 90));
            g.drawRect(sx, sy, slotSize, slotSize);

            // Item Icon if present
            if (i < items.size()) {
                int itemId = items.get(i);
                ItemDefinition def = ItemRegistry.get(itemId);
                if (def != null) {
                    g.setColor(def.iconColor);
                    g.fillRect(sx + 2, sy + 2, slotSize - 4, slotSize - 4);

                    // Stack Count
                    int count = ctx.playerInventory.getOrDefault(itemId, 0);
                    if (count > 1) {
                        g.setFont(new Font("Monospaced", Font.PLAIN, 7));
                        g.setColor(Color.WHITE);
                        g.drawString(String.valueOf(count), sx + slotSize - 6, sy + slotSize - 1);
                    }
                }
            }
        }

        // Right Panel: Equipment Slots
        int eqStartX = startX + 148;
        int eqStartY = startY + 22;
        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        g.setColor(new Color(180, 200, 220));
        g.drawString("EQUIPMENT", eqStartX, eqStartY);

        EquipmentSlot[] eqSlots = EquipmentSlot.values();
        for (int i = 0; i < eqSlots.length; i++) {
            int sx = eqStartX + (i % 2) * 58;
            int sy = eqStartY + 6 + (i / 2) * 16;

            int equippedId = ctx.equipmentManager.getEquipped(eqSlots[i]);
            g.setColor(new Color(25, 30, 45));
            g.fillRect(sx, sy, 54, 14);
            g.setColor(new Color(60, 80, 100));
            g.drawRect(sx, sy, 54, 14);

            g.setFont(new Font("Monospaced", Font.PLAIN, 7));
            g.setColor(equippedId > 0 ? new Color(100, 255, 160) : new Color(120, 130, 140));
            String label = eqSlots[i].label + ": " + (equippedId > 0 ? ItemRegistry.get(equippedId).name : "Empty");
            if (label.length() > 12) label = label.substring(0, 12);
            g.drawString(label, sx + 2, sy + 10);
        }

        // Weapon Runes Display
        int runeY = eqStartY + 60;
        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        g.setColor(new Color(255, 180, 60));
        g.drawString("WEAPON: " + (ctx.activeWeapon != null ? ctx.activeWeapon.getDynamicName() : "None"), eqStartX, runeY);

        for (int s = 0; s < ModdedWeapon.MAX_SOCKETS; s++) {
            int rx = eqStartX + s * 38;
            int ry = runeY + 5;
            g.setColor(new Color(20, 25, 40));
            g.fillRect(rx, ry, 34, 12);
            g.setColor(new Color(80, 90, 120));
            g.drawRect(rx, ry, 34, 12);

            RuneType r = ctx.activeWeapon != null ? ctx.activeWeapon.getRune(s) : null;
            g.setFont(new Font("Monospaced", Font.PLAIN, 7));
            g.setColor(r != null ? new Color(255, 215, 0) : Color.GRAY);
            g.drawString("[" + (s + 1) + "] " + (r != null ? r.prefix : "-"), rx + 2, ry + 9);
        }

        // Bottom Stats & Controls Bar
        int statsY = startY + 120;
        g.setColor(new Color(20, 30, 48));
        g.fillRect(startX + 8, statsY, winW - 16, 32);
        g.setColor(new Color(40, 60, 90));
        g.drawRect(startX + 8, statsY, winW - 16, 32);

        int curItem = getSelectedItem(ctx);
        ItemDefinition selDef = curItem > 0 ? ItemRegistry.get(curItem) : null;
        g.setFont(new Font("Monospaced", Font.PLAIN, 7));
        if (selDef != null) {
            g.setColor(new Color(255, 230, 100));
            g.drawString(selDef.name + " (" + selDef.category + ") - " + selDef.description, startX + 12, statsY + 10);
        } else {
            g.setColor(Color.GRAY);
            g.drawString("No item selected.", startX + 12, statsY + 10);
        }

        // Stats summary
        int totalAtk = ctx.activeWeapon != null ? ctx.activeWeapon.calculateTotalDamage() : 10;
        float speed = ctx.player.getMaxSpeed();
        g.setColor(new Color(150, 220, 255));
        g.drawString(String.format("ATK: %d | SPD: %.0f | HP: %d/%d | Controls: [E] Equip | [1-3] Socket Rune | [TAB/ESC] Exit",
                totalAtk, speed, ctx.player.health, ctx.player.maxHealth), startX + 12, statsY + 24);
    }

    public int getCursorIndex() {
        return cursorIndex;
    }

    public void setCursorIndex(int idx) {
        this.cursorIndex = idx;
    }
}
