package com.echobound.faction;

public enum FactionType {
    SPARK_KEEPERS("Spark Keepers", "Guardians of ancient resonance towers and primary spark relics"),
    CLOCKWORK_GUILD("Clockwork Guild", "Master artificers, cogwrights, and automation engineers"),
    DEEP_SEEKERS("Deep Seekers", "Subterranean miners, geomancers, and cavern surveyors"),
    SKY_NOMADS("Sky Nomads", "Aerial gliders, cliff scouts, and wind whisperers"),
    TIDE_SHAMANS("Tide Shamans", "Aquatic sages, deep divers, and moisture manipulators"),
    THORN_WARDENS("Thorn Wardens", "Ancient forest guardians, botanical alchemists, and seedkeepers"),
    VOID_OUTCASTS("Void Outcasts", "Shadow scavengers and relics investigators living beyond the spires");

    public final String displayName;
    public final String loreDescription;

    FactionType(String displayName, String loreDescription) {
        this.displayName = displayName;
        this.loreDescription = loreDescription;
    }
}
