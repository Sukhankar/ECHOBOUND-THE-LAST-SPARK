package com.echobound.story;

import java.util.ArrayList;
import java.util.List;

public class StoryProgressionEngine {
    private static final String[] MAJOR_CHAPTER_TITLES = {
        "The Silent Spire",
        "Echoes of the Undergrowth",
        "The Clockwork Valley",
        "Foundry of the Ancients",
        "Tides of the Forgotten Sea",
        "The Sunken Conservatory",
        "Gale Peaks and Skylands",
        "The Nomad Eyrie",
        "Caverns of the Deep Geode",
        "The Magma Chasm",
        "The Frost Hollows",
        "Sanctuary of the Whispering Roots",
        "The Shattered Monolith",
        "The Mirror Mire",
        "The Obsidian Labyrinth",
        "The Resonance Core Outpost",
        "The Void Horizon",
        "Ascent to the Heavenly Spire",
        "Confrontation with The Conductor",
        "The Eternal Harmonic Resonance"
    };

    private static final String[] MAJOR_CHAPTER_REGIONS = {
        "Awakening Plains", "Bramble Wilds", "Cog Haven", "Iron Foundry",
        "Tide Coast", "Sunken Ruins", "Howling Cliffs", "Cloud Platform",
        "Crystal Depths", "Molten Trench", "Glacial Peaks", "Verdant Heart",
        "Ruined Plateau", "Reflective Swamp", "Basalt Catacombs", "Tower Perimeter",
        "Void Boundary", "Upper Spire Axis", "The Conductor's Chamber", "Resonance Zenith"
    };

    private static final String[] MAJOR_CHAPTER_CLIMAX = {
        "Corrupted Sentry Drone", "Thorn Goliath", "Automaton Warden", "Magma Golem",
        "Tide Leviathan", "Ancient Archivist AI", "Sky Ray Alpha", "Gale Sovereign",
        "Crystal Burrower", "Infernal Core", "Blizzard Wraith", "Elder Arbiter",
        "Shattered Colossus", "Mirage Phantom", "Obsidian Sentinel", "Resonance Guardian",
        "Void Stalker Prime", "Ascendant Sentry Array", "The Conductor Phase 1 & 2", "Harmonic Unification"
    };

    private final List<StoryChapter> chapters = new ArrayList<>();
    private final List<SubChapter> allSubChapters = new ArrayList<>();
    private int currentGlobalSubChapterIndex = 1; // 1 to 500

    public StoryProgressionEngine() {
        buildAllChapters();
    }

    private void buildAllChapters() {
        int globalIdx = 1;
        for (int ch = 1; ch <= 20; ch++) {
            String title = MAJOR_CHAPTER_TITLES[ch - 1];
            String region = MAJOR_CHAPTER_REGIONS[ch - 1];
            String climax = MAJOR_CHAPTER_CLIMAX[ch - 1];

            StoryChapter chapter = new StoryChapter(ch, title, region, climax);

            for (int sub = 1; sub <= 25; sub++) {
                String subTitle = String.format("Act %d.%02d: %s", ch, sub, getSubChapterTheme(ch, sub));
                String narrative = String.format("Rin and Pip progress through %s towards %s (Step %d/25).",
                    region, climax, sub);

                SubChapter subChapter = new SubChapter(globalIdx, ch, sub, subTitle, narrative);
                chapter.addSubChapter(subChapter);
                allSubChapters.add(subChapter);
                globalIdx++;
            }
            chapters.add(chapter);
        }
    }

    private String getSubChapterTheme(int ch, int sub) {
        if (sub == 1) return "Entering New Frontier";
        if (sub == 5) return "First Echo Beacon Located";
        if (sub == 10) return "Encounter with Local Enclave";
        if (sub == 15) return "Unlocking the Energy Matrix";
        if (sub == 20) return "Breaching the Inner Threshold";
        if (sub == 25) return "Climactic Confrontation";
        return "Unraveling the Resonance Thread " + sub;
    }

    public int getTotalChapterCount() {
        return chapters.size();
    }

    public int getTotalSubChapterCount() {
        return allSubChapters.size();
    }

    public StoryChapter getChapter(int chapterNumber) {
        if (chapterNumber >= 1 && chapterNumber <= chapters.size()) {
            return chapters.get(chapterNumber - 1);
        }
        return null;
    }

    public SubChapter getSubChapter(int globalIndex) {
        if (globalIndex >= 1 && globalIndex <= allSubChapters.size()) {
            return allSubChapters.get(globalIndex - 1);
        }
        return null;
    }

    public int getCurrentSubChapterIndex() {
        return currentGlobalSubChapterIndex;
    }

    public StoryChapter getCurrentMajorChapter() {
        SubChapter current = getSubChapter(currentGlobalSubChapterIndex);
        if (current != null) {
            return getChapter(current.majorChapter);
        }
        return getChapter(20);
    }

    public boolean advanceSubChapter() {
        if (currentGlobalSubChapterIndex <= allSubChapters.size()) {
            SubChapter sc = allSubChapters.get(currentGlobalSubChapterIndex - 1);
            sc.completed = true;
            if (currentGlobalSubChapterIndex < allSubChapters.size()) {
                currentGlobalSubChapterIndex++;
                return true;
            }
        }
        return false;
    }

    public int getCompletedSubChapterCount() {
        int count = 0;
        for (SubChapter sc : allSubChapters) {
            if (sc.completed) count++;
        }
        return count;
    }

    public float getOverallProgressPercentage() {
        return (getCompletedSubChapterCount() / (float) allSubChapters.size()) * 100.0f;
    }
}
