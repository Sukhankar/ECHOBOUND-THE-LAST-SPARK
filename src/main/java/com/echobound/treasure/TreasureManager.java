package com.echobound.treasure;

import com.echobound.physics3d.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreasureManager {
    public enum SignalStrength {
        NONE,
        FAINT,
        MEDIUM,
        STRONG
    }

    private final List<BuriedTreasure> treasures = new ArrayList<>();
    private int nextId = 1;

    public BuriedTreasure registerTreasure(TreasureChestType type, float x, float y, float z) {
        BuriedTreasure t = new BuriedTreasure(nextId++, type, x, y, z);
        treasures.add(t);
        return t;
    }

    public SignalStrength getDowsingSignal(Vec3 playerPos) {
        float closestDist = Float.MAX_VALUE;

        for (BuriedTreasure t : treasures) {
            if (!t.isOpened) {
                float dist = (float) playerPos.distanceTo(t.position);
                if (dist < closestDist) {
                    closestDist = dist;
                }
            }
        }

        if (closestDist <= 6.0f) return SignalStrength.STRONG;
        if (closestDist <= 16.0f) return SignalStrength.MEDIUM;
        if (closestDist <= 32.0f) return SignalStrength.FAINT;
        return SignalStrength.NONE;
    }

    public BuriedTreasure openNearbyTreasure(Vec3 playerPos, Map<Integer, Integer> inventory) {
        for (BuriedTreasure t : treasures) {
            if (!t.isOpened && playerPos.distanceTo(t.position) <= 2.5f) {
                t.isOpened = true;
                t.isDiscovered = true;
                if (inventory != null && t.type.rewardItemId > 0) {
                    int cur = inventory.getOrDefault(t.type.rewardItemId, 0);
                    inventory.put(t.type.rewardItemId, cur + t.type.rewardCount);
                }
                return t;
            }
        }
        return null;
    }

    public int getUnopenedCount() {
        int count = 0;
        for (BuriedTreasure t : treasures) {
            if (!t.isOpened) count++;
        }
        return count;
    }

    public List<BuriedTreasure> getAllTreasures() {
        return treasures;
    }
}
