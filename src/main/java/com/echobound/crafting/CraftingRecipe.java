package com.echobound.crafting;

import java.util.Collections;
import java.util.Map;

public class CraftingRecipe {
    public final String recipeId;
    public final String recipeName;
    public final CraftingStationType requiredStation; // null if hand-craftable
    public final int resultItemId;
    public final int resultCount;
    public final Map<Integer, Integer> ingredients; // itemId -> count

    public CraftingRecipe(String recipeId, String recipeName, CraftingStationType requiredStation,
                          int resultItemId, int resultCount, Map<Integer, Integer> ingredients) {
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.requiredStation = requiredStation;
        this.resultItemId = resultItemId;
        this.resultCount = resultCount;
        this.ingredients = Collections.unmodifiableMap(ingredients);
    }
}
