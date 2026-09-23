package com.echobound.test;

import com.echobound.assets.AssetManager;
import com.echobound.core.UnifiedGameContext;
import com.echobound.settings.GameSettings;
import com.echobound.settings.SettingsManager;
import com.echobound.tutorial.TutorialManager;
import com.echobound.tutorial.TutorialStep;
import com.echobound.ui.windows.InGameWindowType;
import com.echobound.ui.windows.TutorialControlsWindow;
import com.echobound.ui.windows.WindowManager;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class Part11TutorialAndAssetsVerification {

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertObjectEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== RUNNING PART 11 ASSETS, TUTORIAL & CONTROLS VERIFICATION SUITE ===");

        testAssetsFolderAndSprites();
        testFirstTimeUserSettingsPersistence();
        testTutorialStepProgression();
        testTutorialControlsWindowAndHotkeys();

        System.out.println(">>> ALL PART 11 TESTS PASSED PERFECTLY! <<<");
    }

    private static void testAssetsFolderAndSprites() {
        File assetsDir = new File("./assets");
        AssetManager am = new AssetManager(assetsDir);

        // 1. Verify subdirectories exist
        assertTrue(new File(assetsDir, "characters").exists(), "characters/ asset folder must exist");
        assertTrue(new File(assetsDir, "mobs").exists(), "mobs/ asset folder must exist");
        assertTrue(new File(assetsDir, "tiles").exists(), "tiles/ asset folder must exist");
        assertTrue(new File(assetsDir, "maps").exists(), "maps/ asset folder must exist");
        assertTrue(new File(assetsDir, "ui").exists(), "ui/ asset folder must exist");

        // 2. Verify Character Sprites
        BufferedImage rin = am.getSprite("characters/rin_idle.png");
        assertTrue(rin != null, "Rin sprite must be loaded and non-null");
        assertEquals(16, rin.getWidth(), "Rin sprite width must be 16");

        BufferedImage pip = am.getSprite("characters/pip_companion.png");
        assertTrue(pip != null, "Pip companion sprite must be non-null");

        BufferedImage echo = am.getSprite("characters/echo_clone.png");
        assertTrue(echo != null, "Echo clone sprite must be non-null");

        // 3. Verify Mob Sprites
        BufferedImage drone = am.getSprite("mobs/corrupted_drone.png");
        assertTrue(drone != null, "Corrupted drone sprite must be non-null");

        // 4. Verify Tiles & Maps
        BufferedImage grass = am.getSprite("tiles/grass_block.png");
        assertTrue(grass != null, "Grass block tile must be non-null");

        BufferedImage map = am.getSprite("maps/world_overworld_preview.png");
        assertTrue(map != null, "World map preview must be non-null");

        // 5. Verify UI Diagram
        BufferedImage controls = am.getSprite("ui/controls_diagram.png");
        assertTrue(controls != null, "Controls diagram must be non-null");

        assertTrue(am.getCachedSpriteCount() > 0, "Sprite cache must be populated");
        System.out.println("  [PASS] Assets Directory Maintenance (characters, mobs, tiles, maps, UI) verified");
    }

    private static void testFirstTimeUserSettingsPersistence() {
        try {
            Path tmpConfig = Files.createTempFile("echobound_test_settings", ".properties");
            SettingsManager sm = new SettingsManager(tmpConfig);

            // Default must be true
            assertTrue(sm.getSettings().firstTimeUser, "New user settings must default firstTimeUser to true");

            // Toggle to false and save
            sm.getSettings().firstTimeUser = false;
            boolean saved = sm.save();
            assertTrue(saved, "Settings save must return true");

            // Reload in fresh SettingsManager instance
            SettingsManager sm2 = new SettingsManager(tmpConfig);
            assertTrue(!sm2.getSettings().firstTimeUser, "Reloaded settings must persist firstTimeUser = false");

            Files.deleteIfExists(tmpConfig);
            System.out.println("  [PASS] GameSettings firstTimeUser Persistence verified");
        } catch (Exception e) {
            throw new RuntimeException("Settings persistence test failed", e);
        }
    }

    private static void testTutorialStepProgression() {
        TutorialManager tm = new TutorialManager();

        assertEquals(6, tm.getTotalSteps(), "Tutorial must comprise 6 core steps");
        assertObjectEquals(TutorialStep.MOVEMENT, tm.getCurrentStep(), "Initial step must be MOVEMENT");
        assertTrue(!tm.isAllCompleted(), "Initially not all steps should be completed");

        // Step 1: Complete Movement
        tm.completeStep(TutorialStep.MOVEMENT);
        assertTrue(tm.isStepCompleted(TutorialStep.MOVEMENT), "MOVEMENT step must be marked complete");
        assertObjectEquals(TutorialStep.MINING, tm.getCurrentStep(), "Current step must advance to MINING");

        // Step 2: Complete Mining
        tm.completeStep(TutorialStep.MINING);
        assertTrue(tm.isStepCompleted(TutorialStep.MINING), "MINING step must be marked complete");
        assertObjectEquals(TutorialStep.CRAFTING, tm.getCurrentStep(), "Current step must advance to CRAFTING");

        // Complete remaining steps
        tm.completeStep(TutorialStep.CRAFTING);
        tm.completeStep(TutorialStep.MAGIC);
        tm.completeStep(TutorialStep.ECHO);
        tm.completeStep(TutorialStep.TAMING);

        assertTrue(tm.isAllCompleted(), "All tutorial steps must be completed");
        assertEquals(6, tm.getCompletedCount(), "Completed step count must be 6");

        System.out.println("  [PASS] Beginner Academy 5-Step Walkthrough Progression verified");
    }

    private static void testTutorialControlsWindowAndHotkeys() {
        UnifiedGameContext ctx = new UnifiedGameContext();
        SettingsManager sm = new SettingsManager();
        WindowManager wm = new WindowManager();

        // 1. Initial State
        assertTrue(!wm.hasActiveWindow(), "Initially no window active");

        // 2. Open Tutorial & Controls with [H]
        boolean handledH = wm.handleKeyPress(KeyEvent.VK_H, ctx, sm);
        assertTrue(handledH, "VK_H must open tutorial window");
        assertObjectEquals(InGameWindowType.TUTORIAL_CONTROLS, wm.getActiveWindow(), "Active window must be TUTORIAL_CONTROLS");

        // 3. Tab switching inside window
        TutorialControlsWindow tw = wm.tutorialWindow;
        assertEquals(0, tw.getCurrentTab(), "Default tab must be Controls (0)");
        tw.handleKeyPress(KeyEvent.VK_2, ctx, sm);
        assertEquals(1, tw.getCurrentTab(), "Tab must switch to Tutorial (1)");
        tw.handleKeyPress(KeyEvent.VK_1, ctx, sm);
        assertEquals(0, tw.getCurrentTab(), "Tab must switch back to Controls (0)");

        // 4. Toggle Don't Show Again
        assertTrue(!tw.isDontShowAgainChecked(), "Initially unchecked");
        tw.handleKeyPress(KeyEvent.VK_D, ctx, sm);
        assertTrue(tw.isDontShowAgainChecked(), "VK_D must toggle checkmark to true");
        assertTrue(!sm.getSettings().firstTimeUser, "firstTimeUser setting must update to false");

        // 5. Close with [ESC]
        boolean handledEsc = wm.handleKeyPress(KeyEvent.VK_ESCAPE, ctx, sm);
        assertTrue(handledEsc, "VK_ESCAPE must close window");
        assertObjectEquals(InGameWindowType.NONE, wm.getActiveWindow(), "Window must return to NONE");

        // 6. Test [F1] hotkey toggle
        boolean handledF1 = wm.handleKeyPress(KeyEvent.VK_F1, ctx, sm);
        assertTrue(handledF1, "VK_F1 must open tutorial window");
        assertObjectEquals(InGameWindowType.TUTORIAL_CONTROLS, wm.getActiveWindow(), "Active window must be TUTORIAL_CONTROLS");

        System.out.println("  [PASS] TutorialControlsWindow Navigation & [H]/[F1] Hotkeys verified");
    }
}
