package com.echobound.sandbox;

import java.awt.*;

public enum BlockType {
    AIR(0, "Air", false, true, 0, 0.0f, null,
        new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 0)),

    GRASS(1, "Sunroot Grass", true, false, 0, 0.4f, "DIRT",
        new Color(58, 142, 60), new Color(34, 94, 38), new Color(86, 180, 78)),

    DIRT(2, "Rich Soil", true, false, 0, 0.5f, "DIRT",
        new Color(112, 72, 48), new Color(76, 48, 32), new Color(146, 98, 66)),

    STONE(3, "Granite Stone", true, false, 0, 1.2f, "STONE",
        new Color(90, 96, 110), new Color(58, 62, 74), new Color(126, 134, 150)),

    SPARK_ORE(4, "Spark Ore", true, false, 8, 1.6f, "SPARK_SHARD",
        new Color(40, 70, 90), new Color(24, 46, 62), new Color(0, 240, 255)),

    CRYSTAL_NODE(5, "Resonance Crystal", true, true, 10, 1.4f, "CRYSTAL_SHARD",
        new Color(180, 60, 220), new Color(110, 30, 145), new Color(235, 140, 255)),

    WOOD_LOG(6, "Elder Wood Log", true, false, 0, 0.8f, "WOOD",
        new Color(130, 82, 45), new Color(86, 52, 28), new Color(165, 108, 62)),

    LEAVES(7, "Sunroot Leaves", true, true, 0, 0.25f, "LEAF_FIBER",
        new Color(42, 120, 52, 230), new Color(26, 82, 34, 230), new Color(68, 162, 78, 230)),

    WATER(8, "Spring Water", false, true, 2, 0.0f, null,
        new Color(38, 135, 210, 180), new Color(20, 90, 150, 180), new Color(110, 200, 255, 180)),

    WOOD_PLANKS(9, "Wood Planks", true, false, 0, 0.7f, "WOOD_PLANKS",
        new Color(175, 120, 70), new Color(120, 80, 45), new Color(210, 150, 95)),

    STONE_BRICK(10, "Stone Brick", true, false, 0, 1.5f, "STONE_BRICK",
        new Color(75, 80, 95), new Color(48, 52, 64), new Color(105, 112, 130)),

    SPARK_LAMP(11, "Spark Lantern", true, true, 14, 0.5f, "SPARK_LAMP",
        new Color(0, 220, 240), new Color(0, 140, 160), new Color(200, 255, 255)),

    WORKBENCH(12, "Crafting Workbench", true, true, 2, 0.9f, "WORKBENCH",
        new Color(150, 98, 55), new Color(98, 62, 34), new Color(255, 195, 75)),

    BARRICADE(13, "Defensive Barricade", true, true, 0, 2.0f, "BARRICADE",
        new Color(140, 70, 40), new Color(90, 42, 24), new Color(190, 105, 60)),

    // ── World/Environment expansion: biome surfaces, vegetation, structures ──

    SAND(14, "Sunbaked Sand", true, false, 0, 0.35f, "SAND",
        new Color(215, 190, 130), new Color(175, 150, 95), new Color(235, 215, 165)),

    SNOW(15, "Packed Snow", true, false, 0, 0.35f, "SNOW",
        new Color(225, 235, 245), new Color(185, 200, 215), new Color(255, 255, 255)),

    TALL_GRASS(16, "Tall Grass", false, true, 0, 0.1f, "PLANT_FIBER",
        new Color(70, 150, 65, 235), new Color(45, 105, 45, 235), new Color(100, 190, 90, 235)),

    WILDFLOWER(17, "Wildflower", false, true, 0, 0.1f, "WILDFLOWER",
        new Color(90, 160, 70, 235), new Color(60, 115, 50, 235), new Color(230, 120, 160, 235)),

    BERRY_BUSH(18, "Berry Bush", true, true, 0, 0.6f, "WILD_BERRIES",
        new Color(55, 120, 60), new Color(32, 82, 40), new Color(200, 50, 70)),

    BOULDER(19, "Weathered Boulder", true, false, 0, 1.6f, "STONE",
        new Color(120, 118, 112), new Color(80, 78, 74), new Color(160, 158, 150)),

    CACTUS(20, "Spineback Cactus", true, false, 0, 0.9f, "CACTUS_FIBER",
        new Color(60, 130, 80), new Color(38, 92, 55), new Color(95, 175, 115)),

    RUINS_BRICK(21, "Ancient Ruins Brick", true, false, 0, 1.5f, "ANCIENT_RUBBLE",
        new Color(95, 90, 80), new Color(62, 58, 50), new Color(140, 132, 118));

    public final int id;
    public final String displayName;
    public final boolean solid;
    public final boolean transparent;
    public final int lightLevel; // 0 to 15
    public final float maxHardness; // Mining time in seconds
    public final String dropItem;
    public final Color baseColor;
    public final Color shadowColor;
    public final Color highlightColor;

    BlockType(int id, String displayName, boolean solid, boolean transparent,
              int lightLevel, float maxHardness, String dropItem,
              Color baseColor, Color shadowColor, Color highlightColor) {
        this.id = id;
        this.displayName = displayName;
        this.solid = solid;
        this.transparent = transparent;
        this.lightLevel = lightLevel;
        this.maxHardness = maxHardness;
        this.dropItem = dropItem;
        this.baseColor = baseColor;
        this.shadowColor = shadowColor;
        this.highlightColor = highlightColor;
    }

    public static BlockType fromId(int id) {
        for (BlockType b : values()) {
            if (b.id == id) return b;
        }
        return AIR;
    }
}
