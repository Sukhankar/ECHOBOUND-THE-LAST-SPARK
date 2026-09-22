package com.echobound.test;

import com.echobound.core.UnifiedGameContext;
import com.echobound.fishing.FishSpecies;
import com.echobound.fishing.FishingEngine;
import com.echobound.items.ItemRegistry;
import com.echobound.physics3d.Vec3;
import com.echobound.puzzle.PuzzleManager;
import com.echobound.puzzle.PuzzleMechanism;
import com.echobound.puzzle.PuzzleType;
import com.echobound.treasure.BuriedTreasure;
import com.echobound.treasure.TreasureChestType;
import com.echobound.treasure.TreasureManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Part9FishingTreasurePuzzlesVerification {
    public static void main(String[] args) {
        System.out.println("=== RUNNING PART 9 FISHING, TREASURE & PUZZLES VERIFICATION SUITE ===");

        testFishingEngineAndSpecies();
        testTreasureDowsingAndExcavation();
        testEchoEnvironmentalPuzzles();
        testUnifiedContextIntegration();

        System.out.println(">>> ALL PART 9 TESTS PASSED PERFECTLY! <<<");
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

    private static void assertObjectEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(String.format("%s: expected %s but got %s", message, expected, actual));
        }
    }

    private static void testFishingEngineAndSpecies() {
        assertEquals(6, FishSpecies.values().length, "Must have 6 fish species");

        FishingEngine fishing = new FishingEngine();
        assertObjectEquals(FishingEngine.FishingState.IDLE, fishing.getState(), "Initial state is IDLE");

        // Cast line without water -> fails
        boolean castDry = fishing.castLine(false);
        assertTrue(!castDry, "Cannot cast fishing rod away from water");

        // Cast line near water -> succeeds
        boolean castWater = fishing.castLine(true);
        assertTrue(castWater, "Casting near water must succeed");
        assertObjectEquals(FishingEngine.FishingState.WAITING_FOR_BITE, fishing.getState(), "State is WAITING_FOR_BITE");

        // Advance 6 seconds to ensure bite alert occurs
        for (int i = 0; i < 60; i++) {
            fishing.update(0.1f);
            if (fishing.getState() == FishingEngine.FishingState.BITE_ALERT) {
                break;
            }
        }
        assertObjectEquals(FishingEngine.FishingState.BITE_ALERT, fishing.getState(), "Must reach BITE_ALERT");

        // Hook bite into reeling minigame
        boolean hooked = fishing.hookBite();
        assertTrue(hooked, "Hooking bite must succeed");
        assertObjectEquals(FishingEngine.FishingState.REELING_MINIGAME, fishing.getState(), "State is REELING_MINIGAME");

        // Advance minigame while keeping tension in sweet spot (0.5)
        for (int i = 0; i < 20; i++) {
            fishing.update(0.1f);
            if (fishing.getState() == FishingEngine.FishingState.CAUGHT) {
                break;
            }
        }
        assertObjectEquals(FishingEngine.FishingState.CAUGHT, fishing.getState(), "Fish must be CAUGHT");

        // Claim catch into inventory
        Map<String, Integer> inv = new HashMap<>();
        FishSpecies caught = fishing.claimCatch(inv);
        assertTrue(caught != null, "Caught fish must not be null");
        assertTrue(inv.containsKey(caught.name()), "Inventory must contain caught fish species");
        assertEquals(1, inv.get(caught.name()), "Fish count should be 1");
        assertObjectEquals(FishingEngine.FishingState.IDLE, fishing.getState(), "State returns to IDLE after catch");

        System.out.println("  [PASS] Fishing System, 6 Species & Reeling Minigame verified");
    }

    private static void testTreasureDowsingAndExcavation() {
        assertEquals(4, TreasureChestType.values().length, "Must have 4 Treasure Chest types");

        TreasureManager tm = new TreasureManager();
        assertEquals(0, tm.getUnopenedCount(), "Initial treasures must be 0");

        // Register an Ancient Vault at (50, 50, 0)
        tm.registerTreasure(TreasureChestType.ANCIENT_VAULT, 50.0f, 50.0f, 0.0f);
        assertEquals(1, tm.getUnopenedCount(), "Unopened count should be 1");

        Vec3 playerPos = new Vec3(100.0f, 100.0f, 0.0f); // dist ~70m
        assertObjectEquals(TreasureManager.SignalStrength.NONE, tm.getDowsingSignal(playerPos), "Distance 70m -> Signal NONE");

        playerPos.set(70.0f, 50.0f, 0.0f); // dist 20m
        assertObjectEquals(TreasureManager.SignalStrength.FAINT, tm.getDowsingSignal(playerPos), "Distance 20m -> Signal FAINT");

        playerPos.set(60.0f, 50.0f, 0.0f); // dist 10m
        assertObjectEquals(TreasureManager.SignalStrength.MEDIUM, tm.getDowsingSignal(playerPos), "Distance 10m -> Signal MEDIUM");

        playerPos.set(54.0f, 50.0f, 0.0f); // dist 4m
        assertObjectEquals(TreasureManager.SignalStrength.STRONG, tm.getDowsingSignal(playerPos), "Distance 4m -> Signal STRONG");

        // Open chest when within 2.5m
        Map<Integer, Integer> inv = new HashMap<>();
        playerPos.set(51.0f, 50.0f, 0.0f); // dist 1m
        BuriedTreasure opened = tm.openNearbyTreasure(playerPos, inv);
        assertTrue(opened != null, "Opening nearby treasure must succeed");
        assertTrue(opened.isOpened, "Treasure must be marked opened");
        assertEquals(0, tm.getUnopenedCount(), "No unopened treasures remaining");
        assertEquals(3, inv.get(ItemRegistry.MAT_ANCIENT_CHIP), "Loot awards 3 Ancient Chips");

        System.out.println("  [PASS] Spark Dowsing Radar & Subterranean Treasure Excavation verified");
    }

    private static void testEchoEnvironmentalPuzzles() {
        assertEquals(3, PuzzleType.values().length, "Must have 3 Puzzle types");

        PuzzleManager pm = new PuzzleManager();
        // Create Dual Pressure Plate puzzle: Plate A at (10, 0, 0), Plate B at (25, 0, 0)
        PuzzleMechanism puzzle = pm.createPressurePlatePuzzle(new Vec3(10, 0, 0), new Vec3(25, 0, 0));
        assertEquals(1, pm.getTotalPuzzleCount(), "Total puzzles must be 1");
        assertEquals(0, pm.getSolvedCount(), "Solved count must be 0");

        // Rin steps on Plate A alone
        Vec3 rinPos = new Vec3(10.0f, 0.0f, 0.0f);
        pm.update(rinPos, null);
        assertTrue(puzzle.plateAOccupied, "Plate A must be occupied");
        assertTrue(!puzzle.plateBOccupied, "Plate B must be empty");
        assertTrue(!puzzle.isSolved, "Puzzle requires both plates depressed simultaneously");

        // Rin on Plate A and Echo Clone on Plate B
        Vec3 echoPos = new Vec3(25.0f, 0.0f, 0.0f);
        pm.update(rinPos, echoPos);
        assertTrue(puzzle.plateAOccupied && puzzle.plateBOccupied, "Both plates depressed");
        assertTrue(puzzle.isSolved, "Puzzle must be SOLVED with Echo Clone collaboration");
        assertEquals(1, pm.getSolvedCount(), "Solved puzzles should be 1");

        // Test Elemental Brazier puzzle
        PuzzleMechanism brazier = pm.createBrazierPuzzle(new Vec3(50, 50, 0));
        assertTrue(!brazier.isSolved, "Brazier puzzle initially unsolved");

        pm.onElementalHit(new Vec3(50.5f, 50.2f, 0.0f));
        assertTrue(brazier.isSolved, "Brazier must be ignited by elemental spell hit");
        assertEquals(2, pm.getSolvedCount(), "Total solved should now be 2");

        System.out.println("  [PASS] Echo Clone Pressure Plates & Elemental Puzzles verified");
    }

    private static void testUnifiedContextIntegration() {
        UnifiedGameContext ctx = new UnifiedGameContext();
        assertTrue(ctx.fishingEngine != null, "FishingEngine must be in context");
        assertTrue(ctx.treasureManager != null, "TreasureManager must be in context");
        assertTrue(ctx.puzzleManager != null, "PuzzleManager must be in context");

        // Update tick runs cleanly
        ctx.update(0.016f);
        ctx.soundEngine.shutdown();
        System.out.println("  [PASS] Unified Game Context Fishing, Treasure & Puzzles Loop verified");
    }
}
