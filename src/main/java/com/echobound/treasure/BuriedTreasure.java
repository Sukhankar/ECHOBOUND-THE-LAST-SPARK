package com.echobound.treasure;

import com.echobound.physics3d.Vec3;

public class BuriedTreasure {
    public final int id;
    public final TreasureChestType type;
    public final Vec3 position = new Vec3();
    public boolean isDiscovered = false;
    public boolean isOpened = false;

    public BuriedTreasure(int id, TreasureChestType type, float x, float y, float z) {
        this.id = id;
        this.type = type;
        this.position.set(x, y, z);
    }
}
