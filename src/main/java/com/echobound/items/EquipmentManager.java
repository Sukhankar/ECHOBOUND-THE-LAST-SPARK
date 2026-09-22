package com.echobound.items;

import java.util.EnumMap;
import java.util.Map;

public class EquipmentManager {
    private final Map<EquipmentSlot, Integer> equippedItems = new EnumMap<>(EquipmentSlot.class);

    public EquipmentManager() {
        // Start empty or with defaults
    }

    public void equip(EquipmentSlot slot, int itemId) {
        if (itemId <= 0) {
            equippedItems.remove(slot);
        } else {
            equippedItems.put(slot, itemId);
        }
    }

    public int getEquipped(EquipmentSlot slot) {
        return equippedItems.getOrDefault(slot, 0);
    }

    public boolean isEquipped(int itemId) {
        return equippedItems.containsValue(itemId);
    }

    public float getDashMultiplier() {
        return (getEquipped(EquipmentSlot.BOOTS) == ItemRegistry.EQUIP_STORM_BOOTS) ? 1.30f : 1.0f;
    }

    public float getMiningSpeedMultiplier() {
        return (getEquipped(EquipmentSlot.GLOVES) == ItemRegistry.EQUIP_MINER_GLOVES) ? 1.50f : 1.0f;
    }

    public float getStealthMultiplier() {
        return (getEquipped(EquipmentSlot.BODY) == ItemRegistry.EQUIP_FOREST_CLOAK) ? 1.40f : 1.0f;
    }

    public boolean hasNightVision() {
        return getEquipped(EquipmentSlot.HEAD) == ItemRegistry.EQUIP_MINER_HELM ||
               getEquipped(EquipmentSlot.CHARM) == ItemRegistry.EQUIP_MOON_CHARM;
    }

    public int getBonusFireDamage() {
        return (getEquipped(EquipmentSlot.CORE) == ItemRegistry.EQUIP_EMBER_CORE) ? 15 : 0;
    }
}
