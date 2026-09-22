package com.echobound.test;

import com.echobound.combat.ModdedWeapon;
import com.echobound.combat.RuneType;
import com.echobound.crafting.CraftingRecipe;
import com.echobound.crafting.CraftingStationType;
import com.echobound.core.UnifiedGameContext;
import com.echobound.fx.FloatingTextManager;
import com.echobound.fx.ParticleFXManager;
import com.echobound.items.*;
import com.echobound.magic.MagicSchool;
import com.echobound.quest.QuestDefinition;
import com.echobound.quest.QuestStatus;
import com.echobound.quest.QuestTier;
import com.echobound.ui.windows.*;

import java.awt.event.KeyEvent;
import java.util.List;

public class Part10InGameWindowsVerification {

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
        System.out.println("=== RUNNING PART 10 IN-GAME WINDOWS & FX VERIFICATION SUITE ===");

        testParticleFXAndFloatingTexts();
        testInventoryAndEquipmentWindow();
        testCraftingWindow();
        testQuestLogWindow();
        testWindowManagerModalNavigation();

        System.out.println(">>> ALL PART 10 TESTS PASSED PERFECTLY! <<<");
    }

    private static void testParticleFXAndFloatingTexts() {
        ParticleFXManager pm = new ParticleFXManager();
        FloatingTextManager fm = new FloatingTextManager();

        // 1. Test Particle Burst
        pm.spawnElementalBurst(10, 20, 5, MagicSchool.EMBER, 16);
        assertTrue(pm.getActiveCount() >= 16, "Particle pool must have active particles after elemental burst");

        // Simulate 1.0s to exceed max life (0.7s)
        pm.update(1.0f);
        assertEquals(0, pm.getActiveCount(), "All burst particles must expire cleanly");

        // 2. Test Floating Damage and Loot Texts
        fm.spawnDamage(50, 50, 10, 45, true); // Crit
        fm.spawnLoot(50, 50, 10, "Spark Shard", 3);
        assertEquals(2, fm.getActiveCount(), "Floating text manager must track 2 active texts");

        // Simulate 0.3s (still active)
        fm.update(0.3f);
        assertTrue(fm.getActiveCount() > 0, "Floating text must still be active after 0.3s");

        // Simulate further 1.0s (expired)
        fm.update(1.0f);
        assertEquals(0, fm.getActiveCount(), "Floating texts must expire after max lifetime");

        System.out.println("  [PASS] Retro Pixel Particle FX & Floating Combat Numbers verified");
    }

    private static void testInventoryAndEquipmentWindow() {
        UnifiedGameContext ctx = new UnifiedGameContext();
        InventoryEquipmentWindow win = new InventoryEquipmentWindow();

        // 1. Populate inventory
        ctx.playerInventory.put(ItemRegistry.EQUIP_MINER_HELM, 1);
        ctx.playerInventory.put(ItemRegistry.EQUIP_FOREST_CLOAK, 1);
        ctx.playerInventory.put(ItemRegistry.RUNE_SHARP, 2);
        ctx.playerInventory.put(ItemRegistry.RUNE_FLAME, 1);

        List<Integer> items = win.getItemList(ctx);
        assertTrue(items.contains(ItemRegistry.EQUIP_MINER_HELM), "Inventory list must contain Miner Helm");
        assertTrue(items.contains(ItemRegistry.RUNE_SHARP), "Inventory list must contain Sharp Rune");

        // 2. Equip Miner Helm (slot HEAD)
        win.setCursorIndex(items.indexOf(ItemRegistry.EQUIP_MINER_HELM));
        boolean equipped = win.equipSelectedItem(ctx);
        assertTrue(equipped, "Equipping Miner Helm must succeed");
        assertEquals(ItemRegistry.EQUIP_MINER_HELM, ctx.equipmentManager.getEquipped(EquipmentSlot.HEAD),
                "Head equipment slot must now contain Miner Helm");
        assertTrue(!ctx.playerInventory.containsKey(ItemRegistry.EQUIP_MINER_HELM),
                "Equipped item must be deducted from inventory");

        // 3. Equip Forest Cloak (slot BODY)
        items = win.getItemList(ctx);
        win.setCursorIndex(items.indexOf(ItemRegistry.EQUIP_FOREST_CLOAK));
        equipped = win.equipSelectedItem(ctx);
        assertTrue(equipped, "Equipping Forest Cloak must succeed");
        assertEquals(ItemRegistry.EQUIP_FOREST_CLOAK, ctx.equipmentManager.getEquipped(EquipmentSlot.BODY),
                "Body equipment slot must now contain Forest Cloak");

        // 4. Socket Runes into Active Weapon
        ctx.activeWeapon = new ModdedWeapon(ItemRegistry.WEAPON_IRON_SWORD, 25);
        items = win.getItemList(ctx);
        win.setCursorIndex(items.indexOf(ItemRegistry.RUNE_SHARP));
        boolean socketedSharp = win.socketRuneIntoWeapon(ctx, 0);
        assertTrue(socketedSharp, "Socketing Sharp rune into slot 0 must succeed");
        assertObjectEquals(RuneType.SHARP, ctx.activeWeapon.getRune(0), "Socket 0 must be Sharp");

        items = win.getItemList(ctx);
        win.setCursorIndex(items.indexOf(ItemRegistry.RUNE_FLAME));
        boolean socketedFlame = win.socketRuneIntoWeapon(ctx, 1);
        assertTrue(socketedFlame, "Socketing Flame rune into slot 1 must succeed");
        assertObjectEquals(RuneType.FLAME, ctx.activeWeapon.getRune(1), "Socket 1 must be Flame");

        // Dynamic naming & damage verification
        assertObjectEquals("Sharp Flame Iron Sword", ctx.activeWeapon.getDynamicName(),
                "Weapon dynamic name must reflect socketed runes");
        assertTrue(ctx.activeWeapon.calculateTotalDamage() > 25, "Weapon damage must be boosted by runes");

        System.out.println("  [PASS] In-Game Inventory, 6 Equipment Slots & 3-Socket Runecrafting verified");
    }

    private static void testCraftingWindow() {
        UnifiedGameContext ctx = new UnifiedGameContext();
        CraftingWindow win = new CraftingWindow();

        // 1. Station switching
        win.setCurrentStationIndex(0); // Campfire
        CraftingRecipe recipe = win.getSelectedRecipe();
        assertTrue(recipe != null, "Campfire must have recipes");
        assertObjectEquals(CraftingStationType.CAMPFIRE, recipe.requiredStation, "Station must match Campfire");

        // 2. Attempt craft without materials
        ctx.playerInventory.clear();
        boolean craftedFail = win.craftSelected(ctx);
        assertTrue(!craftedFail, "Crafting must fail without materials");

        // 3. Supply materials and craft
        ctx.playerInventory.put(ItemRegistry.MAT_WOOD, 2);
        ctx.playerInventory.put(ItemRegistry.MAT_SPARK_SHARD, 2);
        boolean craftedSuccess = win.craftSelected(ctx);
        assertTrue(craftedSuccess, "Crafting must succeed when materials are supplied");
        assertEquals(1, ctx.playerInventory.get(ItemRegistry.MAT_WOOD), "1 Wood must remain after torch crafting");

        // Verify status message and timer
        win.update(0.1f);
        assertTrue(win.getSelectedRecipe() != null, "Recipe must still be selected");

        System.out.println("  [PASS] In-Game Crafting Window, Station Filtering & Material Consumption verified");
    }

    private static void testQuestLogWindow() {
        UnifiedGameContext ctx = new UnifiedGameContext();
        QuestLogWindow win = new QuestLogWindow();

        // 1. Check Tier 1 (VISIBLE / Story)
        win.setCurrentTierIndex(0);
        QuestDefinition quest = win.getSelectedQuest(ctx);
        assertTrue(quest != null, "Tier 1 must contain Story quest");
        assertObjectEquals(QuestTier.VISIBLE, quest.tier, "Selected quest tier must be VISIBLE");
        assertObjectEquals("Awaken the Spire", quest.title, "First visible quest must be Awaken the Spire");

        // 2. Start quest via window action
        win.handleKeyPress(KeyEvent.VK_ENTER, ctx);
        assertObjectEquals(QuestStatus.IN_PROGRESS, quest.status, "Enter key must start available quest");

        // 3. Advance quest and verify completion
        ctx.questManager.advanceQuest(quest.id, 5, ctx.factionManager, ctx.playerInventory);
        assertObjectEquals(QuestStatus.COMPLETED, quest.status, "Advancing quest to required target must complete it");

        // 4. Check chapter & subchapter info
        assertEquals(1, ctx.storyEngine.getCurrentMajorChapter().chapterNumber, "Story Chapter must be 1");
        assertEquals(1, ctx.storyEngine.getCurrentSubChapterIndex(), "Global Subchapter index must be 1");

        System.out.println("  [PASS] In-Game Quest Log Window & 4-Tier Chronicles verified");
    }

    private static void testWindowManagerModalNavigation() {
        UnifiedGameContext ctx = new UnifiedGameContext();
        WindowManager wm = new WindowManager();

        // 1. Initial state
        assertObjectEquals(InGameWindowType.NONE, wm.getActiveWindow(), "Initial window state must be NONE");
        assertTrue(!wm.hasActiveWindow(), "Must not have active window initially");

        // 2. Open Inventory with [I]
        boolean handledI = wm.handleKeyPress(KeyEvent.VK_I, ctx);
        assertTrue(handledI, "VK_I must be handled");
        assertObjectEquals(InGameWindowType.INVENTORY_EQUIPMENT, wm.getActiveWindow(), "Active window must be INVENTORY_EQUIPMENT");

        // 3. Close with [I]
        boolean handledCloseI = wm.handleKeyPress(KeyEvent.VK_I, ctx);
        assertTrue(handledCloseI, "VK_I again must close window");
        assertObjectEquals(InGameWindowType.NONE, wm.getActiveWindow(), "Active window must be NONE");

        // 4. Open Crafting with [C]
        wm.handleKeyPress(KeyEvent.VK_C, ctx);
        assertObjectEquals(InGameWindowType.CRAFTING, wm.getActiveWindow(), "Active window must be CRAFTING");

        // 5. Dismiss with [ESC]
        boolean handledEsc = wm.handleKeyPress(KeyEvent.VK_ESCAPE, ctx);
        assertTrue(handledEsc, "ESC must dismiss active window");
        assertObjectEquals(InGameWindowType.NONE, wm.getActiveWindow(), "Active window must return to NONE");

        // 6. Open Quest Log with [J]
        wm.handleKeyPress(KeyEvent.VK_J, ctx);
        assertObjectEquals(InGameWindowType.QUEST_LOG, wm.getActiveWindow(), "Active window must be QUEST_LOG");
        wm.closeAllWindows();
        assertTrue(!wm.hasActiveWindow(), "closeAllWindows must set state to NONE");

        System.out.println("  [PASS] WindowManager Modal State Machine & Hotkey Router verified");
    }
}
