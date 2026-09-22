package com.echobound.test;

import com.echobound.arcade.ArcadeCabinet;
import com.echobound.arcade.PostGameEngine;
import com.echobound.boss.CorruptionSwarmManager;
import com.echobound.boss.WorldBoss;
import com.echobound.boss.WorldBossType;
import com.echobound.building.BuildingStructureType;
import com.echobound.building.PlacedStructure;
import com.echobound.building.StructureManager;
import com.echobound.museum.MuseumManager;
import com.echobound.museum.MuseumWing;
import com.echobound.sandbox.DayNightCycle;
import com.echobound.story.StoryProgressionEngine;

import java.util.Objects;

public class Part5Verification {
    public static void main(String[] args) {
        System.out.println("=== RUNNING PART 5 VERIFICATION SUITE ===");

        testWorldBossMechanicsAndPhases();
        testCorruptionSwarmNighttimeScaling();
        testBaseBuildingAndSafeZoneRadius();
        testMuseumWingsAndPassiveMilestones();
        testArcadeCabinetAndPostGameEngine();

        System.out.println(">>> ALL PART 5 TESTS PASSED PERFECTLY! <<<");
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

    private static void testWorldBossMechanicsAndPhases() {
        assertEquals(5, WorldBossType.values().length, "Must have 5 World Boss types");

        WorldBoss sentry = new WorldBoss(WorldBossType.CORRUPTED_SENTRY);
        assertEquals(500, sentry.currentHealth, "Initial health must be 500");
        assertEquals(1, sentry.currentPhase, "Initial phase must be 1");
        assertTrue(!sentry.isShielded, "Boss should not be shielded initially");

        // Damage boss by 260 -> health 240 (48% of 500 <= 50%), triggering Phase 2 transition & shield!
        sentry.takeDamage(260);
        assertEquals(240, sentry.currentHealth, "Health should be 240");
        assertEquals(2, sentry.currentPhase, "Should have transitioned to Phase 2");
        assertTrue(sentry.isShielded, "Phase 2 transition grants protective shield");

        // Attack while shielded: 100 damage reduced to 25% (25 damage)
        sentry.takeDamage(100);
        assertEquals(215, sentry.currentHealth, "Shield must reduce 100 damage to 25 damage (240 - 25 = 215)");

        // Break shield and test attack telegraph
        sentry.breakShield();
        assertTrue(!sentry.isShielded, "Shield should be broken");

        sentry.startAttackTelegraph("Resonance Laser Sweep", 2.0f);
        assertTrue(sentry.isTelegraphing, "Boss should be telegraphing attack");
        assertObjectEquals("Resonance Laser Sweep", sentry.currentAttackTelegraph, "Telegraph name check");

        // Update 1.0s -> still telegraphing
        sentry.update(1.0f);
        assertTrue(sentry.isTelegraphing, "Telegraph should persist at 1.0s");

        // Update 1.5s -> total 2.5s > 2.0s -> telegraph completes
        sentry.update(1.5f);
        assertTrue(!sentry.isTelegraphing, "Telegraph should clear after duration");

        // Kill boss
        sentry.takeDamage(300);
        assertEquals(0, sentry.currentHealth, "Health should be 0");
        assertTrue(sentry.isDefeated, "Boss must be marked defeated");

        System.out.println("  [PASS] World Boss Multi-Phase Transitions & Telegraphs verified");
    }

    private static void testCorruptionSwarmNighttimeScaling() {
        DayNightCycle cycle = new DayNightCycle();
        CorruptionSwarmManager swarm = new CorruptionSwarmManager();

        // Daytime (noon)
        cycle.setTimeOfDay(12.0f);
        swarm.update(cycle, 0);
        assertTrue(!swarm.isSwarmActive(), "Swarm must be inactive at noon");
        assertEquals(0, swarm.getActiveCorruptedEnemies(), "No corrupted enemies in daytime");

        // Nighttime (22:00) on Day 1 with 0 beacons
        cycle.setTimeOfDay(22.0f);
        swarm.update(cycle, 0);
        assertTrue(swarm.isSwarmActive(), "Swarm must be active at night");
        assertEquals(12, swarm.getActiveCorruptedEnemies(), "Day 1 baseline spawn count must be 12");

        // Nighttime with 2 player Spark Beacons protecting area
        swarm.update(cycle, 2);
        assertEquals(4, swarm.getActiveCorruptedEnemies(), "2 Spark Beacons must reduce spawn count by 8 (12 - 8 = 4)");

        System.out.println("  [PASS] Nighttime Corruption Swarms & Spark Beacon Mitigation verified");
    }

    private static void testBaseBuildingAndSafeZoneRadius() {
        assertEquals(5, BuildingStructureType.values().length, "Must have 5 structure types");

        StructureManager sm = new StructureManager();
        assertEquals(0, sm.getTotalStructures(), "Initial structure count must be 0");

        // Place a Spark Beacon at (100, 100, 0)
        PlacedStructure beacon = sm.placeStructure(BuildingStructureType.SPARK_BEACON, 100, 100, 0);
        assertEquals(1, sm.getTotalStructures(), "Total structures must be 1");
        assertEquals(1, sm.getStructureCount(BuildingStructureType.SPARK_BEACON), "Spark Beacon count must be 1");

        // Test safe zone (aura radius is 24.0m)
        // Position (110, 100, 0) is distance 10m -> inside safe zone
        assertTrue(sm.isSafeFromCorruption(110, 100, 0), "Point at 10m distance must be safe from corruption");

        // Position (130, 100, 0) is distance 30m -> outside safe zone
        assertTrue(!sm.isSafeFromCorruption(130, 100, 0), "Point at 30m distance must be outside safe zone");

        // Place an automated defense Pylon
        sm.placeStructure(BuildingStructureType.RESONANCE_PYLON, 105, 100, 0);
        assertEquals(2, sm.getTotalStructures(), "Total structures must be 2");
        assertEquals(1, sm.getStructureCount(BuildingStructureType.RESONANCE_PYLON), "Pylon count must be 1");

        System.out.println("  [PASS] Base Building Structures & 24m Safe Zone Boundary verified");
    }

    private static void testMuseumWingsAndPassiveMilestones() {
        assertEquals(4, MuseumWing.values().length, "Must have 4 Museum Wings");

        MuseumManager museum = new MuseumManager();
        assertEquals(0, museum.getTotalDonations(), "Initial donations must be 0");
        assertEquals(40, museum.getMaxPossibleDonations(), "Max donations must be 40 (4 wings x 10)");
        assertEquals(0.0f, museum.getCompletionPercentage(), 0.01f, "Initial completion is 0%");

        // Donate 10 relics to complete Relics wing (10/40 = 25%)
        for (int i = 1; i <= 10; i++) {
            boolean added = museum.donate(MuseumWing.RELICS, "RELIC_" + i);
            assertTrue(added, "Relic donation must succeed for item " + i);
        }

        assertEquals(10, museum.getWingDonationCount(MuseumWing.RELICS), "Relics wing must have 10 donations");
        assertEquals(25.0f, museum.getCompletionPercentage(), 0.01f, "Completion should be 25%");
        assertEquals(1.10f, museum.getBonusSparkCapacityMultiplier(), 0.01f, "25% milestone grants +10% Spark Capacity");
        assertEquals(1.0f, museum.getBonusMovementSpeedMultiplier(), 0.01f, "50% milestone not yet reached");

        // Donate 10 fossils (total 20/40 = 50%)
        for (int i = 1; i <= 10; i++) {
            museum.donate(MuseumWing.FOSSILS, "FOSSIL_" + i);
        }
        assertEquals(50.0f, museum.getCompletionPercentage(), 0.01f, "Completion should be 50%");
        assertEquals(1.15f, museum.getBonusMovementSpeedMultiplier(), 0.01f, "50% milestone grants +15% Speed");

        // Donate 10 minerals (total 30/40 = 75%)
        for (int i = 1; i <= 10; i++) {
            museum.donate(MuseumWing.MINERALS, "MINERAL_" + i);
        }
        assertEquals(75.0f, museum.getCompletionPercentage(), 0.01f, "Completion should be 75%");
        assertEquals(1.20f, museum.getBonusDamageMultiplier(), 0.01f, "75% milestone grants +20% Damage");
        assertTrue(!museum.hasMasterArchivistCrown(), "Crown requires 100%");

        // Donate 10 flora (total 40/40 = 100%)
        for (int i = 1; i <= 10; i++) {
            museum.donate(MuseumWing.FLORA, "FLORA_" + i);
        }
        assertEquals(100.0f, museum.getCompletionPercentage(), 0.01f, "Completion should be 100%");
        assertTrue(museum.hasMasterArchivistCrown(), "100% completion unlocks Master Archivist Crown");

        System.out.println("  [PASS] Museum 4 Wings, 40 Exhibit Exhibits & Passive Buff Tiers verified");
    }

    private static void testArcadeCabinetAndPostGameEngine() {
        ArcadeCabinet arcade = new ArcadeCabinet("Spark Runner Classic");
        assertEquals(0, arcade.getHighScore(), "Initial high score must be 0");
        assertEquals(0, arcade.getTotalPlays(), "Initial plays must be 0");

        // Play game 1: score 1250 -> 12 tokens won, high score 1250
        int tokens1 = arcade.submitGameScore(1250);
        assertEquals(12, tokens1, "1250 score earns 12 tokens");
        assertEquals(1250, arcade.getHighScore(), "High score should be 1250");

        // Play game 2: score 800 -> 8 tokens won, high score stays 1250
        int tokens2 = arcade.submitGameScore(800);
        assertEquals(8, tokens2, "800 score earns 8 tokens");
        assertEquals(1250, arcade.getHighScore(), "High score should remain 1250");
        assertEquals(20, arcade.getLifetimeTokensEarned(), "Total tokens earned should be 20");

        // Post-Game Engine
        StoryProgressionEngine storyEngine = new StoryProgressionEngine();
        PostGameEngine postGame = new PostGameEngine();
        assertTrue(!postGame.isPostGameUnlocked(), "Post-game should be locked initially");

        // Complete all 500 subchapters
        for (int i = 1; i <= 500; i++) {
            storyEngine.advanceSubChapter();
        }
        postGame.checkStoryProgression(storyEngine);

        assertTrue(postGame.isPostGameUnlocked(), "Post-game should unlock upon completing 500 subchapters");
        assertTrue(postGame.isCreativeSandboxUnlocked(), "Creative sandbox mode must be unlocked");
        assertTrue(postGame.isMasterDifficultyUnlocked(), "Master difficulty must be unlocked");

        postGame.advanceRiftFloor();
        assertEquals(1, postGame.getCompletedRiftFloor(), "Completed rift floor should be 1");

        System.out.println("  [PASS] Arcade Mini-Game Cabinet & Post-Game Endless Mode verified");
    }
}
