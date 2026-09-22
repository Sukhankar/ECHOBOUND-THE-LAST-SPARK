package com.echobound.crafting;

import com.echobound.items.ItemRegistry;

import java.util.*;

public class CraftingEngine {
    private static final List<CraftingRecipe> recipes = new ArrayList<>();

    static {
        // 1. Campfire recipes
        register(new CraftingRecipe("RECIPE_TORCH", "Spark Torch", CraftingStationType.CAMPFIRE,
            ItemRegistry.MAT_SPARK_SHARD, 4,
            Map.of(ItemRegistry.MAT_WOOD, 1, ItemRegistry.MAT_SPARK_SHARD, 1)));

        // 2. Workbench recipes
        register(new CraftingRecipe("RECIPE_WOOD_SWORD", "Wooden Sword", CraftingStationType.WORKBENCH,
            ItemRegistry.WEAPON_WOOD_SWORD, 1,
            Map.of(ItemRegistry.MAT_WOOD, 5)));

        register(new CraftingRecipe("RECIPE_MINER_GLOVES", "Miner Gloves", CraftingStationType.WORKBENCH,
            ItemRegistry.EQUIP_MINER_GLOVES, 1,
            Map.of(ItemRegistry.MAT_WOOD, 4, ItemRegistry.MAT_STONE, 6)));

        register(new CraftingRecipe("RECIPE_STORM_BOOTS", "Storm Boots", CraftingStationType.WORKBENCH,
            ItemRegistry.EQUIP_STORM_BOOTS, 1,
            Map.of(ItemRegistry.MAT_WOOD, 8, ItemRegistry.MAT_SPARK_SHARD, 6)));

        // 3. Forge recipes
        register(new CraftingRecipe("RECIPE_IRON_SWORD", "Iron Sword", CraftingStationType.FORGE,
            ItemRegistry.WEAPON_IRON_SWORD, 1,
            Map.of(ItemRegistry.MAT_WOOD, 2, ItemRegistry.MAT_STONE, 10, ItemRegistry.MAT_SPARK_SHARD, 4)));

        register(new CraftingRecipe("RECIPE_MINER_HELM", "Miner Helm", CraftingStationType.FORGE,
            ItemRegistry.EQUIP_MINER_HELM, 1,
            Map.of(ItemRegistry.MAT_STONE, 8, ItemRegistry.MAT_CRYSTAL_SHARD, 3)));

        // 4. Rune Table recipes
        register(new CraftingRecipe("RECIPE_SHARP_RUNE", "Sharp Rune", CraftingStationType.RUNE_TABLE,
            ItemRegistry.RUNE_SHARP, 1,
            Map.of(ItemRegistry.MAT_STONE, 5, ItemRegistry.MAT_SPARK_SHARD, 3)));

        register(new CraftingRecipe("RECIPE_FLAME_RUNE", "Flame Rune", CraftingStationType.RUNE_TABLE,
            ItemRegistry.RUNE_FLAME, 1,
            Map.of(ItemRegistry.MAT_SPARK_SHARD, 5, ItemRegistry.MAT_CRYSTAL_SHARD, 2)));

        register(new CraftingRecipe("RECIPE_ECHO_RUNE", "Echo Rune", CraftingStationType.RUNE_TABLE,
            ItemRegistry.RUNE_ECHO, 1,
            Map.of(ItemRegistry.MAT_SPARK_SHARD, 8, ItemRegistry.MAT_CRYSTAL_SHARD, 4)));

        // 5. Alchemy Table recipes
        register(new CraftingRecipe("RECIPE_HEALTH_POTION", "Health Elixir", CraftingStationType.ALCHEMY_TABLE,
            ItemRegistry.POTION_HEALTH, 1,
            Map.of(ItemRegistry.FOOD_MUSHROOM_SOUP, 1, ItemRegistry.MAT_SPARK_SHARD, 2)));

        register(new CraftingRecipe("RECIPE_SPARK_TONIC", "Spark Tonic", CraftingStationType.ALCHEMY_TABLE,
            ItemRegistry.POTION_SPARK, 1,
            Map.of(ItemRegistry.MAT_SPARK_SHARD, 4, ItemRegistry.MAT_CRYSTAL_SHARD, 1)));

        // 6. Ancient Forge recipes
        register(new CraftingRecipe("RECIPE_HAMMER_CANNON", "Hammer Cannon", CraftingStationType.ANCIENT_FORGE,
            ItemRegistry.WEAPON_HAMMER_CANNON, 1,
            Map.of(ItemRegistry.MAT_STONE, 12, ItemRegistry.MAT_SPARK_SHARD, 10, ItemRegistry.MAT_ANCIENT_CHIP, 3)));
    }

    public static void register(CraftingRecipe recipe) {
        recipes.add(recipe);
    }

    public static List<CraftingRecipe> getRecipesForStation(CraftingStationType station) {
        List<CraftingRecipe> result = new ArrayList<>();
        for (CraftingRecipe r : recipes) {
            if (r.requiredStation == station) {
                result.add(r);
            }
        }
        return result;
    }

    public static boolean canCraft(CraftingRecipe r, Map<Integer, Integer> inventoryMaterials) {
        if (r == null || inventoryMaterials == null) return false;
        for (Map.Entry<Integer, Integer> entry : r.ingredients.entrySet()) {
            int itemId = entry.getKey();
            int required = entry.getValue();
            int current = inventoryMaterials.getOrDefault(itemId, 0);
            if (current < required) {
                return false;
            }
        }
        return true;
    }

    public static boolean craft(CraftingRecipe r, Map<Integer, Integer> inventoryMaterials) {
        if (!canCraft(r, inventoryMaterials)) {
            return false;
        }
        // Deduct ingredients
        for (Map.Entry<Integer, Integer> entry : r.ingredients.entrySet()) {
            int itemId = entry.getKey();
            int required = entry.getValue();
            int current = inventoryMaterials.getOrDefault(itemId, 0);
            inventoryMaterials.put(itemId, current - required);
        }
        // Add result item
        int curResult = inventoryMaterials.getOrDefault(r.resultItemId, 0);
        inventoryMaterials.put(r.resultItemId, curResult + r.resultCount);
        return true;
    }

    public static int getTotalRecipeCount() {
        return recipes.size();
    }
}
