package com.echobound.entity.mob;

import com.echobound.items.ItemRegistry;

public enum MobType {
    CORRUPTED_DRONE("Corrupted Drone", 60, 140.0f, 12, true, ItemRegistry.MAT_SPARK_SHARD, 2),
    SHADOW_CREEPER("Shadow Creeper", 45, 180.0f, 15, false, ItemRegistry.MAT_SPARK_SHARD, 1),
    MAGMA_GOLEM("Magma Golem", 220, 70.0f, 28, false, ItemRegistry.MAT_CRYSTAL_SHARD, 3),
    VOID_STALKER("Void Stalker", 120, 200.0f, 22, false, ItemRegistry.MAT_ANCIENT_CHIP, 1);

    public final String displayName;
    public final int maxHealth;
    public final float moveSpeed;
    public final int contactDamage;
    public final boolean isFlying;
    public final int dropItemId;
    public final int dropCount;

    MobType(String displayName, int maxHealth, float moveSpeed, int contactDamage,
            boolean isFlying, int dropItemId, int dropCount) {
        this.displayName = displayName;
        this.maxHealth = maxHealth;
        this.moveSpeed = moveSpeed;
        this.contactDamage = contactDamage;
        this.isFlying = isFlying;
        this.dropItemId = dropItemId;
        this.dropCount = dropCount;
    }
}
