package com.echobound.boss;

public enum WorldBossType {
    CORRUPTED_SENTRY("Corrupted Sentry Drone", 500, 2, "Chapter 1 Awakening Plains"),
    THORN_GOLIATH("Thorn Goliath", 1200, 3, "Chapter 2 Bramble Wilds"),
    AUTOMATON_WARDEN("Automaton Warden", 2500, 3, "Chapter 3 Cog Haven"),
    VOID_STALKER_PRIME("Void Stalker Prime", 5000, 3, "Chapter 17 Void Boundary"),
    THE_CONDUCTOR("The Conductor", 10000, 4, "Chapter 19 Conductor's Chamber");

    public final String displayName;
    public final int maxHealth;
    public final int phaseCount;
    public final String location;

    WorldBossType(String displayName, int maxHealth, int phaseCount, String location) {
        this.displayName = displayName;
        this.maxHealth = maxHealth;
        this.phaseCount = phaseCount;
        this.location = location;
    }
}
