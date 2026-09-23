package com.echobound.companion;

public enum PetType {
    FOX("Swift Fox", "Detects subterranean chests and buried relics"),
    GLOWBAT("Glowbat", "Emits a luminous radial lantern halo in darkness"),
    SPARK_CAT("Spark Cat", "Amplifies Volt lightning spells by +35% damage"),
    MOSS_TURTLE("Moss Turtle", "Grants +25% armor defense and suppresses knockback"),
    CLOUD_BIRD("Cloud Bird", "Chirps near secret illusion walls and hidden NPC paths"),
    // Legendary tames — see MobType.PHOENIX_CREATURE / UNICORN_CREATURE, both exceedingly
    // rare ambient spawns (see UnifiedGameContext.updateLegendarySpawning).
    PHOENIX("Phoenix", "Amplifies Ember fire spells and attacks by +35% damage"),
    UNICORN("Unicorn", "Grants +20% movement speed from its blessed grace");

    public final String displayName;
    public final String perkDescription;

    PetType(String displayName, String perkDescription) {
        this.displayName = displayName;
        this.perkDescription = perkDescription;
    }
}
