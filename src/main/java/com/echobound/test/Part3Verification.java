package com.echobound.test;

import com.echobound.alchemy.AlchemyManager;
import com.echobound.alchemy.PotionType;
import com.echobound.companion.*;
import com.echobound.cooking.CookingManager;
import com.echobound.cooking.DishType;
import com.echobound.crafting.CraftingEngine;
import com.echobound.crafting.CraftingRecipe;
import com.echobound.crafting.CraftingStationType;
import com.echobound.farming.CropType;
import com.echobound.farming.FarmingManager;
import com.echobound.items.ItemRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Part3Verification {
    public static void main(String[] args) {
        System.out.println("=== RUNNING PART 3 VERIFICATION SUITE ===");

        testCraftingEngineAndStations();
        testCookingManagerAndDishBuffs();
        testAlchemyManagerAndPotions();
        testFarmingManagerAndClimateMutations();
        testPetManagerAndCompanionPerks();
        testMountManagerAndSpeedOverrides();

        System.out.println(">>> ALL PART 3 TESTS PASSED PERFECTLY! <<<");
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

    private static void assertEquals(float expected, float actual, float epsilon, String message) {
        if (Math.abs(expected - actual) > epsilon) {
            throw new AssertionError(String.format("%s: expected %.2f but got %.2f", message, expected, actual));
        }
    }

    private static void testCraftingEngineAndStations() {
        assertEquals(8, CraftingStationType.values().length, "Must have exactly 8 Crafting Station types");
        assertTrue(CraftingEngine.getTotalRecipeCount() >= 10, "CraftingEngine must have at least 10 registered recipes");

        List<CraftingRecipe> campfireRecipes = CraftingEngine.getRecipesForStation(CraftingStationType.CAMPFIRE);
        assertTrue(!campfireRecipes.isEmpty(), "Campfire must have recipes");

        List<CraftingRecipe> workbenchRecipes = CraftingEngine.getRecipesForStation(CraftingStationType.WORKBENCH);
        assertTrue(workbenchRecipes.size() >= 3, "Workbench must have at least 3 recipes");

        // Test craft execution and deduction
        CraftingRecipe swordRecipe = workbenchRecipes.stream()
                .filter(r -> r.resultItemId == ItemRegistry.WEAPON_WOOD_SWORD)
                .findFirst()
                .orElseThrow();

        Map<Integer, Integer> inv = new HashMap<>();
        inv.put(ItemRegistry.MAT_WOOD, 4);
        assertTrue(!CraftingEngine.canCraft(swordRecipe, inv), "Cannot craft wood sword with only 4 wood (requires 5)");

        inv.put(ItemRegistry.MAT_WOOD, 10);
        assertTrue(CraftingEngine.canCraft(swordRecipe, inv), "Can craft wood sword with 10 wood");

        boolean crafted = CraftingEngine.craft(swordRecipe, inv);
        assertTrue(crafted, "Crafting must succeed");
        assertEquals(5, inv.get(ItemRegistry.MAT_WOOD), "Remaining wood must be 5 (10 - 5)");
        assertEquals(1, inv.get(ItemRegistry.WEAPON_WOOD_SWORD), "Result inventory must have 1 Wooden Sword");

        System.out.println("  [PASS] Crafting Engine, 8 Stations & Ingredient Deduction verified");
    }

    private static void testCookingManagerAndDishBuffs() {
        assertEquals(6, DishType.values().length, "Must have 6 gourmet dishes");

        CookingManager cooking = new CookingManager();
        assertEquals(0, cooking.getActiveBuffCount(), "Initial active buff count must be 0");
        assertEquals(1.0f, cooking.getSpeedMultiplier(), 0.01f, "Default speed multiplier must be 1.0");
        assertEquals(1.0f, cooking.getJumpMultiplier(), 0.01f, "Default jump multiplier must be 1.0");
        assertEquals(1.0f, cooking.getSparkRegenMultiplier(), 0.01f, "Default spark regen multiplier must be 1.0");

        // Consume Explorer Bread (+30% speed) and Cloudberry Cake (+25% jump)
        cooking.consumeDish(DishType.EXPLORER_BREAD);
        cooking.consumeDish(DishType.CLOUDBERRY_CAKE);
        cooking.consumeDish(DishType.FIRE_PEPPER_STEW);

        assertEquals(3, cooking.getActiveBuffCount(), "Buff count must be 3");
        assertEquals(1.30f, cooking.getSpeedMultiplier(), 0.01f, "Explorer bread must grant +30% speed");
        assertEquals(1.25f, cooking.getJumpMultiplier(), 0.01f, "Cloudberry cake must grant +25% jump");
        assertTrue(cooking.hasFireImmunity(), "Fire pepper stew must grant fire immunity");

        // Update elapsed time (e.g. 50 seconds)
        cooking.update(50.0f);
        assertTrue(cooking.hasBuff(DishType.EXPLORER_BREAD), "Explorer bread lasts 180s, must still be active at 50s");

        // Advance past duration (e.g. +200 seconds -> total 250s, exceeding bread's 180s)
        cooking.update(200.0f);
        assertTrue(!cooking.hasBuff(DishType.EXPLORER_BREAD), "Explorer bread must expire after 180s");
        assertEquals(1.0f, cooking.getSpeedMultiplier(), 0.01f, "Speed must return to 1.0 after buff expiry");

        System.out.println("  [PASS] Cooking Manager, 6 Gourmet Dishes & Timed Buff Decay verified");
    }

    private static void testAlchemyManagerAndPotions() {
        assertEquals(6, PotionType.values().length, "Must have 6 alchemy potion types");

        AlchemyManager alchemy = new AlchemyManager();
        assertTrue(!alchemy.isTreasureScentActive(), "Treasure scent must be false initially");
        assertTrue(!alchemy.isInvisible(), "Invisibility must be false initially");

        alchemy.drinkPotion(PotionType.TREASURE_SCENT);
        alchemy.drinkPotion(PotionType.INVISIBILITY_PHIAL);

        assertTrue(alchemy.isTreasureScentActive(), "Treasure scent must be active");
        assertTrue(alchemy.isInvisible(), "Invisibility must be active");

        // Decay time: 20s
        alchemy.update(20.0f);
        assertTrue(alchemy.isTreasureScentActive(), "Treasure scent (45s) must still be active at 20s");
        assertTrue(!alchemy.isInvisible(), "Invisibility phial (15s) must have expired after 20s");

        // Decay remaining time: +30s (total 50s)
        alchemy.update(30.0f);
        assertTrue(!alchemy.isTreasureScentActive(), "Treasure scent must expire after 50s total");

        System.out.println("  [PASS] Alchemy Manager, Potions & Timed Effects verified");
    }

    private static void testFarmingManagerAndClimateMutations() {
        assertEquals(5, CropType.values().length, "Must have 5 primary crop types");

        FarmingManager farming = new FarmingManager();
        assertEquals(0, farming.getActivePlotCount(), "Initial crop plots must be 0");

        // Plant Ember Seed at (10, 5, 2)
        boolean planted = farming.plantCrop(10, 5, 2, CropType.EMBER_SEED);
        assertTrue(planted, "Planting crop must succeed");
        assertEquals(1, farming.getActivePlotCount(), "Active plots must be 1");

        FarmingManager.CropPlot plot = farming.getPlot(10, 5, 2);
        assertTrue(plot != null, "Plot must not be null");
        assertEquals(0, plot.currentStage, "Plot must start at stage 0");

        // Water plot
        farming.waterPlot(10, 5, 2);
        assertTrue(plot.isWatered, "Plot must be marked watered");

        // Update with heat source (triggers climate mutation into Flameflower)
        // Ember Seed growth time per stage is 45s. With watering, rate is 2.0x, so 23s dt completes 1 stage.
        farming.update(23.0f, true, false);
        assertTrue(plot.isMutated, "Ember seed must mutate near heat source");
        assertEquals(1, plot.currentStage, "Watered plot should advance to stage 1 after 23s");
        assertTrue("Flameflower".equals(plot.mutatedName), "Mutated name must be Flameflower");

        // Finish remaining stages: Ember Seed maxStages is 4. We are at stage 1.
        farming.waterPlot(10, 5, 2);
        farming.update(23.0f, true, false); // Stage 2
        farming.waterPlot(10, 5, 2);
        farming.update(23.0f, true, false); // Stage 3
        farming.waterPlot(10, 5, 2);
        farming.update(23.0f, true, false); // Stage 4 (max)

        assertEquals(4, plot.currentStage, "Plot should reach stage 4 (fully mature)");

        // Harvest
        String harvestProduct = farming.harvest(10, 5, 2);
        assertEquals(0, farming.getActivePlotCount(), "Plot should be removed upon harvest");
        assertTrue("Flameflower".equals(harvestProduct), "Harvest product must be Flameflower: " + harvestProduct);

        System.out.println("  [PASS] Farming Manager, Crop Hydration & Climate Mutations verified");
    }

    private static void testPetManagerAndCompanionPerks() {
        assertEquals(5, PetType.values().length, "Must have 5 pet types");

        PetManager pets = new PetManager();
        assertEquals(0, pets.getTamedCount(), "Initial tamed count must be 0");
        assertTrue(pets.getActivePet() == null, "Active pet must initially be null");

        pets.tamePet(PetType.SPARK_CAT);
        assertEquals(1, pets.getTamedCount(), "Tamed count must be 1");
        assertObjectEquals(PetType.SPARK_CAT, pets.getActivePet(), "First tamed pet should automatically become active");
        assertEquals(1.35f, pets.getLightningDamageMultiplier(), 0.01f, "Spark Cat must grant +35% lightning damage");

        pets.tamePet(PetType.MOSS_TURTLE);
        assertEquals(2, pets.getTamedCount(), "Tamed count must be 2");

        // Switch active pet to Moss Turtle
        pets.setActivePet(PetType.MOSS_TURTLE);
        assertObjectEquals(PetType.MOSS_TURTLE, pets.getActivePet(), "Active pet should now be Moss Turtle");
        assertEquals(1.25f, pets.getArmorDefenseMultiplier(), 0.01f, "Moss Turtle must grant +25% armor defense");
        assertEquals(1.0f, pets.getLightningDamageMultiplier(), 0.01f, "Lightning damage must revert to 1.0 when Spark Cat is inactive");

        // Switch to Glowbat
        pets.tamePet(PetType.GLOWBAT);
        pets.setActivePet(PetType.GLOWBAT);
        assertTrue(pets.emitsLight(), "Glowbat must emit light aura");

        System.out.println("  [PASS] Pet Manager, 5 Companions & Dynamic Perk System verified");
    }

    private static void testMountManagerAndSpeedOverrides() {
        assertEquals(4, MountType.values().length, "Must have 4 mount types");

        MountManager mounts = new MountManager();
        assertEquals(0, mounts.getUnlockedCount(), "Initial unlocked mounts must be 0");
        assertTrue(!mounts.isMounted(), "Should not be mounted initially");

        float playerSpeed = 160.0f;
        assertEquals(160.0f, mounts.getActiveSpeed(playerSpeed), 0.01f, "Speed should be player base speed when unmounted");

        mounts.unlockMount(MountType.FOREST_ELK);
        assertEquals(1, mounts.getUnlockedCount(), "Unlocked mounts must be 1");
        assertObjectEquals(MountType.FOREST_ELK, mounts.getActiveMount(), "First mount should be active");

        // Mount up
        mounts.mount();
        assertTrue(mounts.isMounted(), "Should be mounted");
        assertEquals(280.0f, mounts.getActiveSpeed(playerSpeed), 0.01f, "Forest Elk speed is 280.0");

        // Toggle mount to dismount
        mounts.toggleMount();
        assertTrue(!mounts.isMounted(), "Should be dismounted after toggle");
        assertEquals(160.0f, mounts.getActiveSpeed(playerSpeed), 0.01f, "Speed should revert to player base speed");

        System.out.println("  [PASS] Mount Manager, 4 Mounts & Speed Multiplier Overrides verified");
    }
}
