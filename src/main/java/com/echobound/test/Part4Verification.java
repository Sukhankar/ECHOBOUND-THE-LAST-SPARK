package com.echobound.test;

import com.echobound.faction.FactionManager;
import com.echobound.faction.FactionType;
import com.echobound.faction.ReputationStanding;
import com.echobound.items.ItemRegistry;
import com.echobound.npc.NPCDefinition;
import com.echobound.npc.NPCManager;
import com.echobound.npc.TimePeriod;
import com.echobound.quest.QuestDefinition;
import com.echobound.quest.QuestManager;
import com.echobound.quest.QuestStatus;
import com.echobound.quest.QuestTier;
import com.echobound.sandbox.DayNightCycle;
import com.echobound.sandbox.WeatherType;
import com.echobound.story.StoryChapter;
import com.echobound.story.StoryProgressionEngine;
import com.echobound.story.SubChapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Part4Verification {
    public static void main(String[] args) {
        System.out.println("=== RUNNING PART 4 VERIFICATION SUITE ===");

        testSevenFactionsAndReputation();
        testLivingNPCSchedulesAndWeatherReactions();
        testFourTierQuestArchitecture();
        testTwentyChapterFiveHundredSubchapterEngine();

        System.out.println(">>> ALL PART 4 TESTS PASSED PERFECTLY! <<<");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("FAILED: " + message);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(String.format("%s: expected %d but got %d", message, expected, actual));
        }
    }

    private static void assertEquals(float expected, float actual, float epsilon, String message) {
        if (Math.abs(expected - actual) > epsilon) {
            throw new AssertionError(String.format("%s: expected %.2f but got %.2f", message, expected, actual));
        }
    }

    private static void assertObjectEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(String.format("%s: expected %s but got %s", message, expected, actual));
        }
    }

    private static void testSevenFactionsAndReputation() {
        assertEquals(7, FactionType.values().length, "Must have exactly 7 factions");

        FactionManager fm = new FactionManager();
        for (FactionType f : FactionType.values()) {
            assertEquals(0, fm.getReputation(f), "Initial faction reputation must be 0 (Neutral)");
            assertObjectEquals(ReputationStanding.NEUTRAL, fm.getStanding(f), "Initial standing must be NEUTRAL");
        }

        // Test Clockwork Guild reputation increase
        fm.addReputation(FactionType.CLOCKWORK_GUILD, 30);
        assertEquals(30, fm.getReputation(FactionType.CLOCKWORK_GUILD), "Reputation must be 30");
        assertObjectEquals(ReputationStanding.FRIENDLY, fm.getStanding(FactionType.CLOCKWORK_GUILD), "Standing must be FRIENDLY");
        assertTrue(fm.hasAccessToSanctuary(FactionType.CLOCKWORK_GUILD), "Friendly faction must grant sanctuary access");
        assertEquals(0.10f, fm.getStoreDiscount(FactionType.CLOCKWORK_GUILD), 0.01f, "Friendly faction gives 10% discount");

        // Increase to Revered
        fm.addReputation(FactionType.CLOCKWORK_GUILD, 70);
        assertEquals(100, fm.getReputation(FactionType.CLOCKWORK_GUILD), "Reputation capped at 100");
        assertObjectEquals(ReputationStanding.REVERED, fm.getStanding(FactionType.CLOCKWORK_GUILD), "Standing must be REVERED");
        assertEquals(0.30f, fm.getStoreDiscount(FactionType.CLOCKWORK_GUILD), 0.01f, "Revered faction gives 30% discount");

        // Decrease Void Outcasts to Hostile
        fm.addReputation(FactionType.VOID_OUTCASTS, -75);
        assertEquals(-75, fm.getReputation(FactionType.VOID_OUTCASTS), "Reputation must be -75");
        assertObjectEquals(ReputationStanding.HOSTILE, fm.getStanding(FactionType.VOID_OUTCASTS), "Standing must be HOSTILE");
        assertTrue(!fm.hasAccessToSanctuary(FactionType.VOID_OUTCASTS), "Hostile faction denies sanctuary access");

        System.out.println("  [PASS] 7 Factions, Alignment Bounds & Merchant Discounts verified");
    }

    private static void testLivingNPCSchedulesAndWeatherReactions() {
        assertObjectEquals(TimePeriod.DAWN, TimePeriod.fromHour(6.5f), "6:30 is DAWN");
        assertObjectEquals(TimePeriod.DAY, TimePeriod.fromHour(12.0f), "12:00 is DAY");
        assertObjectEquals(TimePeriod.DUSK, TimePeriod.fromHour(18.5f), "18:30 is DUSK");
        assertObjectEquals(TimePeriod.NIGHT, TimePeriod.fromHour(23.0f), "23:00 is NIGHT");

        NPCManager npcManager = new NPCManager();
        NPCDefinition kael = npcManager.get(1);
        assertTrue(kael != null, "Master Blacksmith Kael must be registered");
        assertObjectEquals(FactionType.CLOCKWORK_GUILD, kael.faction, "Kael belongs to Clockwork Guild");

        // Test normal Day schedule
        DayNightCycle cycle = new DayNightCycle();
        cycle.setTimeOfDay(12.0f); // Daytime (noon)
        cycle.setWeather(WeatherType.CLEAR);
        npcManager.update(cycle);

        assertEquals(100.0f, kael.x, 0.01f, "Kael must be at forge X=100 during clear day");
        assertTrue(kael.currentActivity.contains("Forging"), "Kael activity must be forging");

        // Test Storm shelter override
        cycle.setWeather(WeatherType.STORM);
        npcManager.update(cycle);
        assertEquals(105.0f, kael.x, 0.01f, "Kael must move to indoor workshop X=105 during STORM");
        assertTrue(kael.currentActivity.contains("indoor forge"), "Kael activity must reflect storm shelter");

        // Test Night schedule
        cycle.setTimeOfDay(22.0f); // Nighttime
        cycle.setWeather(WeatherType.CLEAR);
        npcManager.update(cycle);
        assertEquals(110.0f, kael.x, 0.01f, "Kael must move to bunkhouse X=110 at night");
        assertTrue(kael.currentActivity.contains("Sleeping"), "Kael activity must be sleeping");

        // Test Elder Lyra rain response
        NPCDefinition lyra = npcManager.get(2);
        cycle.setTimeOfDay(10.0f); // Day
        cycle.setWeather(WeatherType.RAIN);
        npcManager.update(cycle);
        assertEquals(305.0f, lyra.y, 0.01f, "Elder Lyra moves under awning Y=305 during rain");

        System.out.println("  [PASS] Living NPC Routines, 4 Time Periods & Weather Sheltering verified");
    }

    private static void testFourTierQuestArchitecture() {
        assertEquals(4, QuestTier.values().length, "Must have exactly 4 Quest Tiers");

        QuestManager qm = new QuestManager();
        assertTrue(qm.getTotalQuestCount() >= 4, "Must have at least 4 default quests");

        List<QuestDefinition> visible = qm.getQuestsByTier(QuestTier.VISIBLE);
        List<QuestDefinition> discoverable = qm.getQuestsByTier(QuestTier.DISCOVERABLE);
        List<QuestDefinition> hidden = qm.getQuestsByTier(QuestTier.HIDDEN);
        List<QuestDefinition> secretChain = qm.getQuestsByTier(QuestTier.SECRET_CHAIN);

        assertTrue(!visible.isEmpty(), "Must have Visible story quests");
        assertTrue(!discoverable.isEmpty(), "Must have Discoverable side quests");
        assertTrue(!hidden.isEmpty(), "Must have Hidden exploration quests");
        assertTrue(!secretChain.isEmpty(), "Must have Secret Chain quests");

        // Test Quest execution, advancement and reward distribution
        FactionManager fm = new FactionManager();
        Map<Integer, Integer> inv = new HashMap<>();

        QuestDefinition storyQ = visible.get(0);
        assertObjectEquals(QuestStatus.LOCKED, storyQ.status, "Quest initially locked");

        boolean started = qm.startQuest(storyQ.id);
        assertTrue(started, "Starting quest must succeed");
        assertObjectEquals(QuestStatus.IN_PROGRESS, storyQ.status, "Quest now in progress");

        // Advance 2/5 progress
        qm.advanceQuest(storyQ.id, 2, fm, inv);
        assertEquals(2, storyQ.currentProgress, "Progress must be 2");
        assertTrue(!storyQ.isComplete(), "Quest not complete yet");

        // Advance remaining 3/5 progress (total 5/5 -> complete!)
        qm.advanceQuest(storyQ.id, 3, fm, inv);
        assertTrue(storyQ.isComplete(), "Quest must be complete");
        assertObjectEquals(QuestStatus.COMPLETED, storyQ.status, "Status is COMPLETED");

        // Verify rewards awarded
        assertEquals(25, fm.getReputation(FactionType.SPARK_KEEPERS), "Spark Keepers reputation must be +25");
        assertEquals(1, inv.get(ItemRegistry.RUNE_SHARP), "Inventory must receive 1 Sharp Rune");

        System.out.println("  [PASS] 4-Tier Quest Architecture, Progress & Dynamic Rewards verified");
    }

    private static void testTwentyChapterFiveHundredSubchapterEngine() {
        StoryProgressionEngine engine = new StoryProgressionEngine();
        assertEquals(20, engine.getTotalChapterCount(), "Must have exactly 20 major story chapters");
        assertEquals(500, engine.getTotalSubChapterCount(), "Must have exactly 500 subchapters (20 x 25)");

        // Check Chapter 1 and Chapter 20 titles
        StoryChapter ch1 = engine.getChapter(1);
        assertTrue(ch1 != null && "The Silent Spire".equals(ch1.title), "Chapter 1 title mismatch: " + ch1.title);
        assertEquals(25, ch1.subChapters.size(), "Chapter 1 must have 25 subchapters");

        StoryChapter ch20 = engine.getChapter(20);
        assertTrue(ch20 != null && "The Eternal Harmonic Resonance".equals(ch20.title), "Chapter 20 title mismatch: " + ch20.title);
        assertEquals(25, ch20.subChapters.size(), "Chapter 20 must have 25 subchapters");

        // Initial progress checks
        assertEquals(1, engine.getCurrentSubChapterIndex(), "Initial subchapter index must be 1");
        assertEquals(0, engine.getCompletedSubChapterCount(), "Initial completed subchapters must be 0");
        assertEquals(0.0f, engine.getOverallProgressPercentage(), 0.01f, "Initial progress must be 0%");
        assertObjectEquals(ch1, engine.getCurrentMajorChapter(), "Current chapter should be Chapter 1");

        // Advance through Chapter 1 (25 subchapters)
        for (int i = 1; i <= 25; i++) {
            boolean advanced = engine.advanceSubChapter();
            assertTrue(advanced, "Subchapter advancement must succeed for step " + i);
        }

        assertTrue(ch1.isAllCompleted(), "Chapter 1 must be fully completed");
        assertEquals(25, ch1.getCompletedCount(), "Chapter 1 completed count must be 25");
        assertEquals(26, engine.getCurrentSubChapterIndex(), "Current subchapter index should now be 26 (Chapter 2, Step 1)");
        assertEquals(25, engine.getCompletedSubChapterCount(), "Completed count must be 25");
        assertEquals(5.0f, engine.getOverallProgressPercentage(), 0.01f, "Progress must be exactly 5.0% (25 / 500)");

        StoryChapter curCh = engine.getCurrentMajorChapter();
        assertEquals(2, curCh.chapterNumber, "Current major chapter should now be Chapter 2");
        assertTrue("Echoes of the Undergrowth".equals(curCh.title), "Chapter 2 title must be Echoes of the Undergrowth");

        System.out.println("  [PASS] 20 Major Chapters & 500 Subchapters Progression Engine verified");
    }
}
