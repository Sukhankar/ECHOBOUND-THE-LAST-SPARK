package com.echobound.sandbox;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
    public static final int QUICK_SLOT_COUNT = 8;

    public static class SlotItem {
        public BlockType blockType;
        public String name;
        public int count;

        public SlotItem(BlockType blockType, String name, int count) {
            this.blockType = blockType;
            this.name = name;
            this.count = count;
        }
    }

    private final SlotItem[] quickSlots = new SlotItem[QUICK_SLOT_COUNT];
    private final Map<String, Integer> materials = new HashMap<>();
    private int selectedSlot = 0;

    public Inventory() {
        // Starter quick slots setup
        quickSlots[0] = new SlotItem(BlockType.WOOD_PLANKS, "Wood Planks", 32);
        quickSlots[1] = new SlotItem(BlockType.STONE_BRICK, "Stone Bricks", 24);
        quickSlots[2] = new SlotItem(BlockType.SPARK_LAMP, "Spark Lantern", 8);
        quickSlots[3] = new SlotItem(BlockType.BARRICADE, "Barricade", 10);
        quickSlots[4] = new SlotItem(BlockType.WORKBENCH, "Workbench", 2);
        quickSlots[5] = new SlotItem(null, "Spark Gauntlet", 1);
        quickSlots[6] = new SlotItem(null, "Health Potion", 3);
        quickSlots[7] = new SlotItem(null, "Echo Whistle", 1);

        // Populate materials
        materials.put("WOOD", 50);
        materials.put("STONE", 40);
        materials.put("SPARK_SHARD", 15);
        materials.put("CRYSTAL_SHARD", 8);
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(int index) {
        if (index >= 0 && index < QUICK_SLOT_COUNT) {
            this.selectedSlot = index;
        }
    }

    public void selectNext() {
        this.selectedSlot = (this.selectedSlot + 1) % QUICK_SLOT_COUNT;
    }

    public void selectPrev() {
        this.selectedSlot = (this.selectedSlot - 1 + QUICK_SLOT_COUNT) % QUICK_SLOT_COUNT;
    }

    public SlotItem getSelectedItem() {
        return quickSlots[selectedSlot];
    }

    public SlotItem getSlot(int index) {
        if (index >= 0 && index < QUICK_SLOT_COUNT) {
            return quickSlots[index];
        }
        return null;
    }

    public void addMaterial(String itemKey, int amount) {
        if (itemKey == null) return;
        materials.put(itemKey, materials.getOrDefault(itemKey, 0) + amount);

        // Also add to any matching quickslot
        for (SlotItem slot : quickSlots) {
            if (slot != null && slot.blockType != null && itemKey.equals(slot.blockType.dropItem)) {
                slot.count += amount;
                return;
            }
        }
    }

    public boolean consumeSelectedItem() {
        SlotItem item = getSelectedItem();
        if (item != null && item.count > 0) {
            item.count--;
            return true;
        }
        return false;
    }

    public int getMaterialCount(String key) {
        return materials.getOrDefault(key, 0);
    }

    public void cycleSlot(int direction) {
        int dir = (direction > 0) ? 1 : (direction < 0 ? -1 : 0);
        selectedSlot = (selectedSlot + dir + QUICK_SLOT_COUNT) % QUICK_SLOT_COUNT;
    }
}
