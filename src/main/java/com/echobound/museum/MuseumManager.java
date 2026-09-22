package com.echobound.museum;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MuseumManager {
    private final Map<MuseumWing, Set<String>> donationsByWing = new EnumMap<>(MuseumWing.class);

    public MuseumManager() {
        for (MuseumWing wing : MuseumWing.values()) {
            donationsByWing.put(wing, new HashSet<>());
        }
    }

    public boolean donate(MuseumWing wing, String itemId) {
        if (wing == null || itemId == null) return false;
        Set<String> donated = donationsByWing.get(wing);
        if (donated.size() >= wing.maxItems) return false; // Wing full
        return donated.add(itemId);
    }

    public boolean isDonated(MuseumWing wing, String itemId) {
        return donationsByWing.get(wing).contains(itemId);
    }

    public int getWingDonationCount(MuseumWing wing) {
        return donationsByWing.get(wing).size();
    }

    public int getTotalDonations() {
        int total = 0;
        for (Set<String> set : donationsByWing.values()) {
            total += set.size();
        }
        return total;
    }

    public int getMaxPossibleDonations() {
        int max = 0;
        for (MuseumWing wing : MuseumWing.values()) {
            max += wing.maxItems;
        }
        return max;
    }

    public float getCompletionPercentage() {
        return (getTotalDonations() / (float) getMaxPossibleDonations()) * 100.0f;
    }

    public float getBonusSparkCapacityMultiplier() {
        // Unlocked at 25% museum completion: +10% max spark capacity
        return (getCompletionPercentage() >= 25.0f) ? 1.10f : 1.0f;
    }

    public float getBonusMovementSpeedMultiplier() {
        // Unlocked at 50% museum completion: +15% movement speed
        return (getCompletionPercentage() >= 50.0f) ? 1.15f : 1.0f;
    }

    public float getBonusDamageMultiplier() {
        // Unlocked at 75% museum completion: +20% damage
        return (getCompletionPercentage() >= 75.0f) ? 1.20f : 1.0f;
    }

    public boolean hasMasterArchivistCrown() {
        // Unlocked at 100% completion
        return getCompletionPercentage() >= 100.0f;
    }
}
