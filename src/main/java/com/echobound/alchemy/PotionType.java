package com.echobound.alchemy;

public enum PotionType {
    HEALING_DRAUGHT("Healing Draught", 0.0f, "Restores 4 hearts instantly"),
    SPARK_TONIC("Spark Tonic", 0.0f, "Restores 50 Spark Energy instantly"),
    SWIFTNESS_ELIXIR("Swiftness Elixir", 30.0f, "+50% movement speed"),
    FROST_WARD("Frost Ward", 45.0f, "Resistance against extreme cold and Tide magic"),
    INVISIBILITY_PHIAL("Invisibility Phial", 15.0f, "Become invisible to standard enemies"),
    TREASURE_SCENT("Treasure Scent", 45.0f, "Reveals subterranean ore veins and ancient chips");

    public final String displayName;
    public final float duration;
    public final String effectDescription;

    PotionType(String displayName, float duration, String effectDescription) {
        this.displayName = displayName;
        this.duration = duration;
        this.effectDescription = effectDescription;
    }
}
