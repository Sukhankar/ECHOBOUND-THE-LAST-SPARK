package com.echobound.farming;

public enum CropType {
    WHEAT("Golden Wheat", 3, 20.0f, "Wheat grain for bread"),
    SUNROOT_BERRY("Sunroot Berry", 3, 30.0f, "Sweet stamina berries"),
    HEALING_HERB("Healing Herb", 3, 35.0f, "Medicinal green leaves"),
    EMBER_SEED("Ember Bulb", 4, 45.0f, "Warm fiery root; mutates into Flameflower near lava"),
    TIDE_SEED("Tide Blossom", 4, 45.0f, "Aquatic plant; mutates into Frost Orchid in snow");

    public final String displayName;
    public final int maxStages;
    public final float growthTimePerStage; // in seconds
    public final String harvestProduct;

    CropType(String displayName, int maxStages, float growthTimePerStage, String harvestProduct) {
        this.displayName = displayName;
        this.maxStages = maxStages;
        this.growthTimePerStage = growthTimePerStage;
        this.harvestProduct = harvestProduct;
    }
}
