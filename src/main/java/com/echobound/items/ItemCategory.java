package com.echobound.items;

public enum ItemCategory {
    WEAPONS("Weapons"),
    TOOLS("Tools"),
    ARMOR("Armor"),
    MAGIC("Magic & Runes"),
    FOOD("Food"),
    POTIONS("Potions"),
    MATERIALS("Materials"),
    QUEST_ITEMS("Quest Items"),
    RELICS("Ancient Relics"),
    TREASURE("Treasure"),
    BUILDING_ITEMS("Building Items"),
    KEYS("Keys"),
    MAPS("Maps"),
    COMPANION_ITEMS("Companion Items"),
    CURRENCY("Currency");

    public final String displayName;

    ItemCategory(String displayName) {
        this.displayName = displayName;
    }
}
