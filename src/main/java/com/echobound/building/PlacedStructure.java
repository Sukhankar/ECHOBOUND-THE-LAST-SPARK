package com.echobound.building;

public class PlacedStructure {
    public final int id;
    public final BuildingStructureType type;
    public final float x;
    public final float y;
    public final float z;
    public boolean active = true;

    public PlacedStructure(int id, BuildingStructureType type, float x, float y, float z) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isInRange(float targetX, float targetY, float targetZ) {
        if (type.auraRadius <= 0) return false;
        float dx = targetX - x;
        float dy = targetY - y;
        float dz = targetZ - z;
        return (dx * dx + dy * dy + dz * dz) <= (type.auraRadius * type.auraRadius);
    }
}
