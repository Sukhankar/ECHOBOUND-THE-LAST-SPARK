package com.echobound.alchemy;

import java.util.EnumMap;
import java.util.Map;

public class AlchemyManager {
    private final Map<PotionType, Float> activePotions = new EnumMap<>(PotionType.class);

    public void drinkPotion(PotionType potion) {
        if (potion != null && potion.duration > 0) {
            activePotions.put(potion, potion.duration);
        }
    }

    public void update(float dt) {
        activePotions.entrySet().removeIf(entry -> {
            float remaining = entry.getValue() - dt;
            if (remaining <= 0) {
                return true;
            }
            entry.setValue(remaining);
            return false;
        });
    }

    public boolean hasEffect(PotionType potion) {
        return activePotions.containsKey(potion);
    }

    public float getRemainingTime(PotionType potion) {
        return activePotions.getOrDefault(potion, 0.0f);
    }

    public boolean isTreasureScentActive() {
        return hasEffect(PotionType.TREASURE_SCENT);
    }

    public boolean isInvisible() {
        return hasEffect(PotionType.INVISIBILITY_PHIAL);
    }
}
