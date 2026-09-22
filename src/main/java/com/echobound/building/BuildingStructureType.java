package com.echobound.building;

public enum BuildingStructureType {
    SPARK_BEACON("Spark Beacon", 24.0f, "Emits bright resonance wave that repels nighttime corruption"),
    RESONANCE_PYLON("Resonance Pylon", 16.0f, "Automated defense tower discharging lightning at foes"),
    CHEST_VAULT("Chest Vault", 0.0f, "High-capacity persistent storage container for materials"),
    FARMING_GREENHOUSE("Farming Greenhouse", 12.0f, "Climatized enclosure boosting crop growth by +50%"),
    TELEPORT_WAYPOINT("Teleport Waypoint", 0.0f, "Enables instantaneous teleportation across world regions");

    public final String displayName;
    public final float auraRadius;
    public final String functionDescription;

    BuildingStructureType(String displayName, float auraRadius, String functionDescription) {
        this.displayName = displayName;
        this.auraRadius = auraRadius;
        this.functionDescription = functionDescription;
    }
}
