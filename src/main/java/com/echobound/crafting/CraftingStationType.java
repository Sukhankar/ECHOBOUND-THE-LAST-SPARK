package com.echobound.crafting;

public enum CraftingStationType {
    CAMPFIRE("Campfire", "Basic cooked food and simple wooden torches"),
    WORKBENCH("Workbench", "Essential tools, furniture, chests, and wooden barricades"),
    FORGE("Forge", "Refining metals, smithing weapons, heavy armor, and anvil repairs"),
    ALCHEMY_TABLE("Alchemy Table", "Brewing magical potions, resistance draughts, and elixirs"),
    RUNE_TABLE("Rune Table", "Carving, socketing, and transmuting elemental runes"),
    COOKING_POT("Cooking Pot", "Gourmet dishes offering enduring vitality and stat buffs"),
    ECHO_STATION("Echo Station", "Tuning resonance devices, memory wafers, and Echo upgrades"),
    ANCIENT_FORGE("Ancient Forge", "Restoring ancient relics, legendary weaponry, and spark cores");

    public final String displayName;
    public final String description;

    CraftingStationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
