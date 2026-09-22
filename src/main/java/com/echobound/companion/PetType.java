package com.echobound.companion;

public enum PetType {
    FOX("Swift Fox", "Detects subterranean chests and buried relics"),
    GLOWBAT("Glowbat", "Emits a luminous radial lantern halo in darkness"),
    SPARK_CAT("Spark Cat", "Amplifies Volt lightning spells by +35% damage"),
    MOSS_TURTLE("Moss Turtle", "Grants +25% armor defense and suppresses knockback"),
    CLOUD_BIRD("Cloud Bird", "Chirps near secret illusion walls and hidden NPC paths");

    public final String displayName;
    public final String perkDescription;

    PetType(String displayName, String perkDescription) {
        this.displayName = displayName;
        this.perkDescription = perkDescription;
    }
}
