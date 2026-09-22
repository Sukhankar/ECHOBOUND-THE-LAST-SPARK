package com.echobound.magic;

public enum RelicType {
    GRAVITY("Relic of Gravity", "Complete control of vertical fall velocity and hover capability"),
    ECHOES("Relic of Echoes", "Awakens dual Echo clones simultaneously"),
    TIDES("Relic of Tides", "Walk freely on shallow and open water surfaces"),
    BEAST("Relic of the Beast", "Pacifies wild beasts and unlocks creature riding"),
    TIME("Relic of Time", "Temporarily slows surrounding time for 3 seconds"),
    DOORS("Relic of Doors", "Reveals hidden passages and illusory walls");

    public final String displayName;
    public final String powerDescription;

    RelicType(String displayName, String powerDescription) {
        this.displayName = displayName;
        this.powerDescription = powerDescription;
    }
}
