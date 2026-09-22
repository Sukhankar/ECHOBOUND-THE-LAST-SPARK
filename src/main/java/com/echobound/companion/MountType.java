package com.echobound.companion;

public enum MountType {
    FOREST_ELK("Forest Elk", 280.0f, true, false, false, "High ground velocity and graceful bounds"),
    STONE_RHINO("Stone Rhino", 220.0f, true, false, false, "Heavy armor; charges through destructible stone"),
    SKY_RAY("Sky Ray", 260.0f, false, true, false, "Soars across open sky, deep gorges, and cloud summits"),
    RIVER_SERPENT("River Serpent", 300.0f, false, false, true, "Rapid traverse across rivers, lakes, and oceans");

    public final String displayName;
    public final float speed;
    public final boolean isLand;
    public final boolean isFlying;
    public final boolean isAquatic;
    public final String abilityDescription;

    MountType(String displayName, float speed, boolean isLand, boolean isFlying, boolean isAquatic,
              String abilityDescription) {
        this.displayName = displayName;
        this.speed = speed;
        this.isLand = isLand;
        this.isFlying = isFlying;
        this.isAquatic = isAquatic;
        this.abilityDescription = abilityDescription;
    }
}
