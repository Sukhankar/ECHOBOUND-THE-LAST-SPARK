package com.echobound.faction;

import java.util.EnumMap;
import java.util.Map;

public class FactionManager {
    private final Map<FactionType, Integer> reputationScores = new EnumMap<>(FactionType.class);

    public FactionManager() {
        for (FactionType faction : FactionType.values()) {
            reputationScores.put(faction, 0); // Start at Neutral (0)
        }
    }

    public int getReputation(FactionType faction) {
        return reputationScores.getOrDefault(faction, 0);
    }

    public void addReputation(FactionType faction, int delta) {
        int current = getReputation(faction);
        int updated = Math.max(-100, Math.min(100, current + delta));
        reputationScores.put(faction, updated);
    }

    public ReputationStanding getStanding(FactionType faction) {
        return ReputationStanding.fromScore(getReputation(faction));
    }

    public float getStoreDiscount(FactionType faction) {
        ReputationStanding standing = getStanding(faction);
        return switch (standing) {
            case REVERED -> 0.30f;   // 30% discount
            case HONORED -> 0.20f;   // 20% discount
            case FRIENDLY -> 0.10f;  // 10% discount
            case SUSPICIOUS -> -0.15f; // 15% price penalty
            case HOSTILE -> -0.50f;   // Refusal or 50% penalty
            default -> 0.0f;
        };
    }

    public boolean hasAccessToSanctuary(FactionType faction) {
        return getReputation(faction) >= 25; // Friendly or above
    }
}
