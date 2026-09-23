package com.echobound.test;

import com.echobound.core.ResolutionProfile;
import com.echobound.core.UnifiedGameContext;
import com.echobound.faction.FactionType;
import com.echobound.items.ItemRegistry;
import com.echobound.save.SaveData;
import com.echobound.save.SaveManager;
import com.echobound.settings.GameSettings;
import com.echobound.settings.SettingsManager;
import com.echobound.ui.menu.GameState;
import com.echobound.ui.menu.LoadingScreen;
import com.echobound.ui.menu.TitleMenuController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Part7MenusAndSavesVerification {
    public static void main(String[] args) throws IOException {
        System.out.println("=== RUNNING PART 7 MENUS, SAVES & OPTIONS VERIFICATION SUITE ===");

        testSaveDataAndManagerPersistence();
        testGameSettingsAndPersistence();
        testLoadingScreenProgression();
        testTitleMenuControllerAndStateTransitions();

        System.out.println(">>> ALL PART 7 TESTS PASSED PERFECTLY! <<<");
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

    private static void testSaveDataAndManagerPersistence() throws IOException {
        Path tempDir = Files.createTempDirectory("echobound_test_saves");
        SaveManager saveManager = new SaveManager(tempDir);

        UnifiedGameContext ctx = new UnifiedGameContext();
        ctx.player.pos.set(120.0f, 45.0f, 6.0f);
        ctx.player.health = 8;
        ctx.dayNightCycle.setTimeOfDay(16.5f);
        ctx.playerInventory.put(ItemRegistry.WEAPON_IRON_SWORD, 1);
        ctx.playerInventory.put(ItemRegistry.MAT_SPARK_SHARD, 15);
        ctx.factionManager.addReputation(FactionType.SPARK_KEEPERS, 40);

        // Advance 10 subchapters
        for (int i = 0; i < 10; i++) {
            ctx.storyEngine.advanceSubChapter();
        }

        // Save to Slot 1
        boolean saved = saveManager.save(1, ctx, "Hero Rin", 1850.0f);
        assertTrue(saved, "Save to slot 1 must succeed");
        assertTrue(saveManager.hasSave(1), "SaveManager must report slot 1 exists");
        assertEquals(1, saveManager.getMostRecentSlot(), "Most recent slot must be 1");

        // Load data back and inspect
        SaveData data = saveManager.loadData(1);
        assertTrue(data != null, "Loaded save data must not be null");
        assertObjectEquals("Hero Rin", data.profileName, "Profile name must match");
        assertEquals(120.0f, data.playerX, 0.01f, "Player X must be 120");
        assertEquals(45.0f, data.playerY, 0.01f, "Player Y must be 45");
        assertEquals(8, data.playerHealth, "Player health must be 8");
        assertEquals(10, data.completedSubChapters, "Completed subchapters must be 10");
        assertEquals(15, data.inventory.get(ItemRegistry.MAT_SPARK_SHARD), "Inventory must have 15 shards");
        assertEquals(40, data.factionReputations.get(FactionType.SPARK_KEEPERS.name()), "Faction rep must be 40");

        // Restore into a fresh context
        UnifiedGameContext freshCtx = new UnifiedGameContext();
        boolean restored = saveManager.applySaveToContext(data, freshCtx);
        assertTrue(restored, "Restoring context must succeed");
        assertEquals(120.0f, freshCtx.player.pos.x, 0.01f, "Restored player X mismatch");
        assertEquals(8, freshCtx.player.health, "Restored player health mismatch");
        assertEquals(10, freshCtx.storyEngine.getCompletedSubChapterCount(), "Restored story progress mismatch");
        assertEquals(15, freshCtx.playerInventory.get(ItemRegistry.MAT_SPARK_SHARD), "Restored inventory mismatch");
        assertEquals(40, freshCtx.factionManager.getReputation(FactionType.SPARK_KEEPERS), "Restored faction rep mismatch");

        // Delete slot
        boolean deleted = saveManager.deleteSlot(1);
        assertTrue(deleted, "Delete slot must succeed");
        assertTrue(!saveManager.hasSave(1), "Slot 1 must no longer exist after delete");

        ctx.soundEngine.shutdown();
        freshCtx.soundEngine.shutdown();
        System.out.println("  [PASS] SaveData Serialization, Slot Management & Context Restoration verified");
    }

    private static void testGameSettingsAndPersistence() throws IOException {
        Path tempConfig = Files.createTempFile("echobound_config", ".properties");
        SettingsManager sm = new SettingsManager(tempConfig);

        GameSettings settings = sm.getSettings();
        assertEquals(1.0f, settings.masterVolume, 0.01f, "Default master volume is 1.0");
        assertObjectEquals(ResolutionProfile.PIXEL_STANDARD, settings.resolutionProfile, "Default profile is PIXEL_STANDARD");
        assertTrue(settings.cameraShakeEnabled, "Camera shake default is true");

        // Modify settings
        settings.adjustMasterVolume(-0.3f);
        settings.cycleResolution(); // PIXEL_STANDARD -> PIXEL_PLUS
        settings.toggleCameraShake(); // true -> false
        settings.toggleDebugOverlay(); // false -> true

        assertEquals(0.7f, settings.masterVolume, 0.01f, "Volume should be 0.7");
        assertObjectEquals(ResolutionProfile.PIXEL_PLUS, settings.resolutionProfile, "Resolution should be PIXEL_PLUS");
        assertTrue(!settings.cameraShakeEnabled, "Camera shake should be false");
        assertTrue(settings.showDebugOverlay, "Debug overlay should be true");

        // Save & reload
        sm.save();
        SettingsManager loadedSm = new SettingsManager(tempConfig);
        GameSettings loaded = loadedSm.getSettings();
        assertEquals(0.7f, loaded.masterVolume, 0.01f, "Loaded master volume should be 0.7");
        assertObjectEquals(ResolutionProfile.PIXEL_PLUS, loaded.resolutionProfile, "Loaded resolution should be PIXEL_PLUS");
        assertTrue(!loaded.cameraShakeEnabled, "Loaded camera shake should be false");
        assertTrue(loaded.showDebugOverlay, "Loaded debug overlay should be true");

        System.out.println("  [PASS] GameSettings & Properties Persistence verified");
    }

    private static void testLoadingScreenProgression() {
        LoadingScreen screen = new LoadingScreen();
        assertEquals(0.0f, screen.getProgress(), 0.01f, "Initial progress must be 0");
        assertTrue(!screen.isFinished(), "Loading screen should not be finished initially");

        // Update partially
        screen.update(0.5f);
        assertTrue(screen.getProgress() > 0.3f, "Progress should advance with dt");
        assertTrue(screen.getCurrentHint() != null, "Hint text must be present");

        // Finish loading
        screen.update(1.0f);
        assertEquals(1.0f, screen.getProgress(), 0.01f, "Progress should cap at 1.0");
        assertTrue(screen.isFinished(), "Screen must be marked finished");

        System.out.println("  [PASS] Animated LoadingScreen & Phase Progression verified");
    }

    private static void testTitleMenuControllerAndStateTransitions() throws IOException {
        Path tempDir = Files.createTempDirectory("echobound_menu_test");
        Path tempConfig = Files.createTempFile("echobound_menu_config", ".properties");

        SaveManager sm = new SaveManager(tempDir);
        SettingsManager setm = new SettingsManager(tempConfig);

        TitleMenuController controller = new TitleMenuController(sm, setm);
        assertObjectEquals(GameState.LOADING, controller.getCurrentState(), "Initial state is LOADING");

        // Update until loading completes — this now leads into the opening story cinematic
        // rather than straight to the title menu.
        controller.update(1.5f);
        assertObjectEquals(GameState.INTRO_CINEMATIC, controller.getCurrentState(), "State should transition to INTRO_CINEMATIC after loading");

        // Cinematic is skippable (any key/click) — verify that path reaches TITLE_MENU too.
        controller.skipCinematic();
        assertObjectEquals(GameState.TITLE_MENU, controller.getCurrentState(), "Skipping the cinematic should transition to TITLE_MENU");
        assertTrue(controller.getCinematicIntro().wasSkipped(), "CinematicIntro must record that it was skipped");

        // Navigate menu
        controller.moveCursorDown();
        controller.moveCursorUp();

        // Navigate to Options (index 3)
        while (controller.getTitleCursor() != 3) {
            controller.moveCursorDown();
        }
        controller.selectCurrent();
        assertObjectEquals(GameState.OPTIONS_MENU, controller.getCurrentState(), "State should transition to OPTIONS_MENU");

        // Test adjusting volume right and left
        controller.adjustOptionRight();
        controller.adjustOptionLeft();

        // Navigate to Back (index 4)
        while (controller.getOptionsCursor() != 4) {
            controller.moveCursorDown();
        }
        controller.selectCurrent();
        assertObjectEquals(GameState.TITLE_MENU, controller.getCurrentState(), "State should return to TITLE_MENU");

        // Navigate to Saved Games (index 2)
        while (controller.getTitleCursor() != 2) {
            controller.moveCursorDown();
        }
        controller.selectCurrent();
        assertObjectEquals(GameState.SAVE_SELECT_MENU, controller.getCurrentState(), "State should transition to SAVE_SELECT_MENU");

        System.out.println("  [PASS] Title Menu Controller & GameState Navigation verified");
    }
}
