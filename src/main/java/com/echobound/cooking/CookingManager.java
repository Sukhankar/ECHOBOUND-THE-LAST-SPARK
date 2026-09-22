package com.echobound.cooking;

import java.util.EnumMap;
import java.util.Map;

public class CookingManager {
    private final Map<DishType, Float> activeBuffs = new EnumMap<>(DishType.class);

    public void consumeDish(DishType dish) {
        if (dish != null) {
            activeBuffs.put(dish, dish.duration);
        }
    }

    public void update(float dt) {
        activeBuffs.entrySet().removeIf(entry -> {
            float remaining = entry.getValue() - dt;
            if (remaining <= 0) {
                return true;
            }
            entry.setValue(remaining);
            return false;
        });
    }

    public boolean hasBuff(DishType dish) {
        return activeBuffs.containsKey(dish);
    }

    public float getRemainingTime(DishType dish) {
        return activeBuffs.getOrDefault(dish, 0.0f);
    }

    public float getSpeedMultiplier() {
        return hasBuff(DishType.EXPLORER_BREAD) ? 1.30f : 1.0f;
    }

    public float getJumpMultiplier() {
        return hasBuff(DishType.CLOUDBERRY_CAKE) ? 1.25f : 1.0f;
    }

    public float getSparkRegenMultiplier() {
        return hasBuff(DishType.ECHO_NOODLES) ? 1.50f : 1.0f;
    }

    public boolean hasFireImmunity() {
        return hasBuff(DishType.FIRE_PEPPER_STEW);
    }

    public boolean hasThunderStrike() {
        return hasBuff(DishType.THUNDER_FISH_CURRY);
    }

    public int getActiveBuffCount() {
        return activeBuffs.size();
    }
}
