package com.echobound.items;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    private static final Map<Integer, ItemDefinition> registry = new HashMap<>();

    // Weapons (100s)
    public static final int WEAPON_WOOD_SWORD = 101;
    public static final int WEAPON_IRON_SWORD = 102;
    public static final int WEAPON_SPARK_PISTOL = 103;
    public static final int WEAPON_ARC_BOW = 104;
    public static final int WEAPON_HAMMER_CANNON = 105;
    public static final int WEAPON_BOLT_STAFF = 106;

    // Runes (200s)
    public static final int RUNE_SHARP = 201;
    public static final int RUNE_FLAME = 202;
    public static final int RUNE_FROST = 203;
    public static final int RUNE_VOLT = 204;
    public static final int RUNE_ECHO = 205;
    public static final int RUNE_VOID = 206;

    // Equipment (300s)
    public static final int EQUIP_MINER_HELM = 301;
    public static final int EQUIP_FOREST_CLOAK = 302;
    public static final int EQUIP_MINER_GLOVES = 303;
    public static final int EQUIP_STORM_BOOTS = 304;
    public static final int EQUIP_MOON_CHARM = 305;
    public static final int EQUIP_EMBER_CORE = 306;

    // Relics (400s)
    public static final int RELIC_GRAVITY = 401;
    public static final int RELIC_ECHOES = 402;
    public static final int RELIC_TIDES = 403;
    public static final int RELIC_BEAST = 404;
    public static final int RELIC_TIME = 405;
    public static final int RELIC_DOORS = 406;

    // Materials (500s)
    public static final int MAT_WOOD = 501;
    public static final int MAT_STONE = 502;
    public static final int MAT_SPARK_SHARD = 503;
    public static final int MAT_CRYSTAL_SHARD = 504;
    public static final int MAT_ANCIENT_CHIP = 505;
    public static final int MAT_DRAGON_SCALE = 506;

    // Food & Potions (600s)
    public static final int FOOD_MUSHROOM_SOUP = 601;
    public static final int POTION_HEALTH = 602;
    public static final int POTION_SPARK = 603;

    // Currency (900s) — backs the NPC shop system (see NPCDefinition.shopItems / ShopWindow).
    // FactionManager.getStoreDiscount() has existed since Part 4 with nothing to discount;
    // this is what actually lets a purchase happen.
    public static final int CURRENCY_SPARK_COIN = 901;

    static {
        // Register Weapons
        register(new ItemDefinition(WEAPON_WOOD_SWORD, "Wooden Sword", ItemCategory.WEAPONS, 1, 10, "Simple carved timber blade.", new Color(170, 115, 65)));
        register(new ItemDefinition(WEAPON_IRON_SWORD, "Iron Sword", ItemCategory.WEAPONS, 1, 50, "Sturdy steel blade with 3 rune slots.", new Color(200, 210, 225)));
        register(new ItemDefinition(WEAPON_SPARK_PISTOL, "Spark Pistol", ItemCategory.WEAPONS, 1, 80, "Fires concentrated pulses of Spark energy.", new Color(0, 240, 255)));
        register(new ItemDefinition(WEAPON_ARC_BOW, "Arc Bow", ItemCategory.WEAPONS, 1, 95, "Long-range kinetic arc launcher.", new Color(255, 190, 60)));
        register(new ItemDefinition(WEAPON_HAMMER_CANNON, "Hammer Cannon", ItemCategory.WEAPONS, 1, 140, "Heavy industrial concussive basher.", new Color(130, 80, 50)));
        register(new ItemDefinition(WEAPON_BOLT_STAFF, "Bolt Staff", ItemCategory.WEAPONS, 1, 120, "Focuses arcane Spark currents.", new Color(200, 100, 255)));

        // Register Runes
        register(new ItemDefinition(RUNE_SHARP, "Sharp Rune", ItemCategory.MAGIC, 16, 30, "+30% physical strike damage.", new Color(230, 230, 230)));
        register(new ItemDefinition(RUNE_FLAME, "Flame Rune", ItemCategory.MAGIC, 16, 45, "Enchants attacks with burning Ember fire.", new Color(255, 90, 40)));
        register(new ItemDefinition(RUNE_FROST, "Frost Rune", ItemCategory.MAGIC, 16, 45, "Inflicts chilling Tide slow-down.", new Color(80, 200, 255)));
        register(new ItemDefinition(RUNE_VOLT, "Volt Rune", ItemCategory.MAGIC, 16, 45, "Chains lightning sparks between targets.", new Color(255, 230, 50)));
        register(new ItemDefinition(RUNE_ECHO, "Echo Rune", ItemCategory.MAGIC, 16, 60, "Duplicate attack executed by phantom clone.", new Color(0, 240, 255)));
        register(new ItemDefinition(RUNE_VOID, "Void Rune", ItemCategory.MAGIC, 16, 60, "Phases through armor plating.", new Color(160, 50, 220)));

        // Register Equipment
        register(new ItemDefinition(EQUIP_MINER_HELM, "Miner Helm", ItemCategory.ARMOR, 1, 60, "Headpiece with illuminated crystal beacon.", new Color(240, 190, 40)));
        register(new ItemDefinition(EQUIP_FOREST_CLOAK, "Forest Cloak", ItemCategory.ARMOR, 1, 75, "Camouflages movement among foliage.", new Color(45, 110, 55)));
        register(new ItemDefinition(EQUIP_MINER_GLOVES, "Miner Gloves", ItemCategory.ARMOR, 1, 80, "Reinforced palms: +50% digging speed.", new Color(160, 95, 50)));
        register(new ItemDefinition(EQUIP_STORM_BOOTS, "Storm Boots", ItemCategory.ARMOR, 1, 100, "Gale-infused treads: +30% dash distance.", new Color(0, 180, 240)));
        register(new ItemDefinition(EQUIP_MOON_CHARM, "Moon Charm", ItemCategory.ARMOR, 1, 110, "Enhances visibility and luck during night.", new Color(210, 225, 255)));
        register(new ItemDefinition(EQUIP_EMBER_CORE, "Ember Core", ItemCategory.ARMOR, 1, 130, "Radiates protective burning warmth.", new Color(255, 60, 30)));

        // Register Relics
        register(new ItemDefinition(RELIC_GRAVITY, "Relic of Gravity", ItemCategory.RELICS, 1, 500, "Allows mastery over fall velocity.", new Color(130, 60, 240)));
        register(new ItemDefinition(RELIC_ECHOES, "Relic of Echoes", ItemCategory.RELICS, 1, 500, "Awakens dual Echo clones at once.", new Color(0, 255, 230)));
        register(new ItemDefinition(RELIC_TIDES, "Relic of Tides", ItemCategory.RELICS, 1, 500, "Walk freely across liquid surfaces.", new Color(30, 150, 255)));
        register(new ItemDefinition(RELIC_BEAST, "Relic of the Beast", ItemCategory.RELICS, 1, 500, "Calms wild creatures and unlocks mounts.", new Color(90, 180, 70)));
        register(new ItemDefinition(RELIC_TIME, "Relic of Time", ItemCategory.RELICS, 1, 500, "Slows ambient time for 3 seconds.", new Color(255, 215, 0)));
        register(new ItemDefinition(RELIC_DOORS, "Relic of Doors", ItemCategory.RELICS, 1, 500, "Reveals illusory walls and sealed ruins.", new Color(255, 100, 160)));

        // Register Materials
        register(new ItemDefinition(MAT_WOOD, "Elder Wood", ItemCategory.MATERIALS, 999, 1, "Raw timber chopped from ancient trees.", new Color(140, 85, 45)));
        register(new ItemDefinition(MAT_STONE, "Granite Stone", ItemCategory.MATERIALS, 999, 1, "Dense quarried stone.", new Color(110, 115, 125)));
        register(new ItemDefinition(MAT_SPARK_SHARD, "Spark Shard", ItemCategory.MATERIALS, 999, 5, "Crystallized fragment of the First Spark.", new Color(0, 240, 255)));
        register(new ItemDefinition(MAT_CRYSTAL_SHARD, "Resonance Crystal", ItemCategory.MATERIALS, 999, 10, "Harmonic crystal node.", new Color(220, 90, 255)));
        register(new ItemDefinition(MAT_ANCIENT_CHIP, "Ancient Chip", ItemCategory.MATERIALS, 999, 25, "Salvaged memory data wafer.", new Color(255, 180, 50)));
        register(new ItemDefinition(MAT_DRAGON_SCALE, "Dragon Scale", ItemCategory.MATERIALS, 999, 80, "Iridescent scale shed by a legendary dragon.", new Color(200, 40, 60)));

        // Register Food & Potions
        register(new ItemDefinition(FOOD_MUSHROOM_SOUP, "Mushroom Soup", ItemCategory.FOOD, 16, 12, "Regenerates 2 hearts over 8 seconds.", new Color(180, 140, 90)));
        register(new ItemDefinition(POTION_HEALTH, "Health Elixir", ItemCategory.POTIONS, 16, 20, "Instantly restores 4 hearts.", new Color(230, 40, 60)));
        register(new ItemDefinition(POTION_SPARK, "Spark Tonic", ItemCategory.POTIONS, 16, 25, "Restores 50 Spark Energy.", new Color(0, 220, 255)));

        // Register Currency
        register(new ItemDefinition(CURRENCY_SPARK_COIN, "Spark Coin", ItemCategory.CURRENCY, 999, 1, "Minted Spark residue, accepted by every faction's traders.", new Color(255, 215, 0)));
    }

    private static void register(ItemDefinition item) {
        registry.put(item.id, item);
    }

    public static ItemDefinition get(int id) {
        return registry.get(id);
    }

    public static boolean exists(int id) {
        return registry.containsKey(id);
    }

    public static int getCount() {
        return registry.size();
    }
}
