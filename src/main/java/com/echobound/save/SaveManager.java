package com.echobound.save;

import com.echobound.core.UnifiedGameContext;
import com.echobound.faction.FactionType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SaveManager {
    public static final int MAX_SLOTS = 3;
    private final Path saveDirectory;

    public SaveManager() {
        this(Paths.get("./saves"));
    }

    public SaveManager(Path saveDirectory) {
        this.saveDirectory = saveDirectory;
        try {
            Files.createDirectories(saveDirectory);
        } catch (IOException ignored) {}
    }

    private File getSlotFile(int slot) {
        return saveDirectory.resolve(String.format("save_slot_%d.dat", slot)).toFile();
    }

    public boolean save(int slot, UnifiedGameContext ctx, String profileName, float playTime) {
        if (slot < 1 || slot > MAX_SLOTS || ctx == null) return false;

        SaveData data = new SaveData(slot, profileName != null ? profileName : "Rin");
        data.timestamp = System.currentTimeMillis();
        data.playTimeSeconds = playTime;

        // Extract player state
        data.playerX = ctx.player.pos.x;
        data.playerY = ctx.player.pos.y;
        data.playerZ = ctx.player.pos.z;
        data.playerHealth = ctx.player.health;
        data.playerMaxHealth = ctx.player.maxHealth;
        data.playerSparkEnergy = ctx.player.sparkEnergy;

        // Extract story state
        data.currentChapter = ctx.storyEngine.getCurrentMajorChapter().chapterNumber;
        data.completedSubChapters = ctx.storyEngine.getCompletedSubChapterCount();

        // Extract world day / time
        data.dayCount = ctx.dayNightCycle.getDayCount();
        data.timeOfDay = ctx.dayNightCycle.getTimeOfDay();

        // Extract inventory
        data.inventory.putAll(ctx.playerInventory);

        // Extract factions
        for (FactionType faction : FactionType.values()) {
            data.factionReputations.put(faction.name(), ctx.factionManager.getReputation(faction));
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getSlotFile(slot)))) {
            oos.writeObject(data);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public SaveData loadData(int slot) {
        if (slot < 1 || slot > MAX_SLOTS) return null;
        File file = getSlotFile(slot);
        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (SaveData) ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean applySaveToContext(SaveData data, UnifiedGameContext ctx) {
        if (data == null || ctx == null) return false;

        // Restore player position & vitals
        ctx.player.pos.set(data.playerX, data.playerY, data.playerZ);
        ctx.player.health = data.playerHealth;
        ctx.player.maxHealth = data.playerMaxHealth;
        ctx.player.sparkEnergy = data.playerSparkEnergy;

        // Restore world time
        ctx.dayNightCycle.setTimeOfDay(data.timeOfDay);

        // Restore story subchapters
        for (int i = 0; i < data.completedSubChapters; i++) {
            ctx.storyEngine.advanceSubChapter();
        }

        // Restore inventory
        ctx.playerInventory.clear();
        ctx.playerInventory.putAll(data.inventory);

        // Restore factions
        for (Map.Entry<String, Integer> entry : data.factionReputations.entrySet()) {
            try {
                FactionType faction = FactionType.valueOf(entry.getKey());
                int current = ctx.factionManager.getReputation(faction);
                ctx.factionManager.addReputation(faction, entry.getValue() - current);
            } catch (Exception ignored) {}
        }

        return true;
    }

    public boolean hasSave(int slot) {
        return getSlotFile(slot).exists();
    }

    public boolean deleteSlot(int slot) {
        File file = getSlotFile(slot);
        return file.exists() && file.delete();
    }

    public int getMostRecentSlot() {
        int bestSlot = -1;
        long newestTimestamp = -1;

        for (int slot = 1; slot <= MAX_SLOTS; slot++) {
            SaveData data = loadData(slot);
            if (data != null && data.timestamp > newestTimestamp) {
                newestTimestamp = data.timestamp;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    public List<SaveData> listAllSaves() {
        List<SaveData> list = new ArrayList<>();
        for (int slot = 1; slot <= MAX_SLOTS; slot++) {
            SaveData data = loadData(slot);
            list.add(data); // null indicates empty slot
        }
        return list;
    }
}
