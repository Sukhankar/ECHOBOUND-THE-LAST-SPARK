package com.echobound.save;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    public int slotId = 1;
    public String profileName = "Spark Runner";
    public long timestamp = System.currentTimeMillis();
    public float playTimeSeconds = 0.0f;

    // Player State
    public float playerX = 0.0f;
    public float playerY = 0.0f;
    public float playerZ = 10.0f;
    public int playerHealth = 12;
    public int playerMaxHealth = 12;
    public float playerSparkEnergy = 100.0f;

    // Story Progression
    public int currentChapter = 1;
    public int completedSubChapters = 0;

    // World State
    public int dayCount = 1;
    public float timeOfDay = 8.0f;

    // Inventory & Factions
    public Map<Integer, Integer> inventory = new HashMap<>();
    public Map<String, Integer> factionReputations = new HashMap<>();

    public SaveData() {}

    public SaveData(int slotId, String profileName) {
        this.slotId = slotId;
        this.profileName = profileName;
    }

    public String getFormattedSummary() {
        int hours = (int) (playTimeSeconds / 3600);
        int mins = (int) ((playTimeSeconds % 3600) / 60);
        return String.format("%s | Ch. %d (%d/500) | Day %d | %02dh %02dm",
            profileName, currentChapter, completedSubChapters, dayCount, hours, mins);
    }
}
