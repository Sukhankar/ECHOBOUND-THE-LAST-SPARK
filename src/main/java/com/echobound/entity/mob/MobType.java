package com.echobound.entity.mob;

import com.echobound.companion.PetType;
import com.echobound.items.ItemRegistry;

public enum MobType {
    // ── Hostile (unchanged from before — original 4 monster types) ───────────
    CORRUPTED_DRONE("Corrupted Drone", 60, 140.0f, 12, true, ItemRegistry.MAT_SPARK_SHARD, 2, Temperament.HOSTILE, null),
    SHADOW_CREEPER("Shadow Creeper", 45, 180.0f, 15, false, ItemRegistry.MAT_SPARK_SHARD, 1, Temperament.HOSTILE, null),
    MAGMA_GOLEM("Magma Golem", 220, 70.0f, 28, false, ItemRegistry.MAT_CRYSTAL_SHARD, 3, Temperament.HOSTILE, null),
    VOID_STALKER("Void Stalker", 120, 200.0f, 22, false, ItemRegistry.MAT_ANCIENT_CHIP, 1, Temperament.HOSTILE, null),

    // ── Passive, tameable wildlife — one per existing PetType, so taming one in the
    //    world connects straight into the pre-existing (previously unreachable) perk
    //    system in PetManager instead of adding a second, parallel pet mechanic. ──
    WOODLAND_FOX("Woodland Fox", 12, 130.0f, 0, false, 0, 0, Temperament.PASSIVE, PetType.FOX),
    CAVE_GLOWBAT("Cave Glowbat", 8, 110.0f, 0, true, 0, 0, Temperament.PASSIVE, PetType.GLOWBAT),
    EMBER_CAT("Ember Cat", 14, 120.0f, 0, false, 0, 0, Temperament.NEUTRAL, PetType.SPARK_CAT),
    MOSS_TURTLE_CREATURE("Moss Turtle", 30, 40.0f, 0, false, 0, 0, Temperament.PASSIVE, PetType.MOSS_TURTLE),
    SKY_CLOUDBIRD("Cloudback Bird", 10, 100.0f, 0, true, 0, 0, Temperament.PASSIVE, PetType.CLOUD_BIRD),

    // ── Small ambient pests (§4) — harmless, decorative, not tameable. ────────
    FIELD_RAT("Field Rat", 4, 150.0f, 0, false, 0, 0, Temperament.PASSIVE, null),
    MARSH_BEETLE("Marsh Beetle", 3, 60.0f, 0, false, 0, 0, Temperament.NEUTRAL, null),

    // ── Mythical wildlife — exceedingly rare (see UnifiedGameContext.LEGENDARY_POOL /
    // updateLegendarySpawning, a separate much-lower-odds roll from ordinary ambient
    // wildlife) so encountering one actually feels like an event. Dragon is a genuine
    // threat; Phoenix and Unicorn are legendary tames feeding the same PetManager perk
    // system as the mundane wildlife above, not a separate mechanic. ─────────────────
    DRAGON("Elder Wyrm", 400, 90.0f, 35, true, ItemRegistry.MAT_DRAGON_SCALE, 2, Temperament.HOSTILE, null),
    PHOENIX_CREATURE("Phoenix", 60, 140.0f, 0, true, 0, 0, Temperament.PASSIVE, PetType.PHOENIX),
    UNICORN_CREATURE("Unicorn", 40, 150.0f, 0, false, 0, 0, Temperament.PASSIVE, PetType.UNICORN);

    public final String displayName;
    public final int maxHealth;
    public final float moveSpeed;
    public final int contactDamage;
    public final boolean isFlying;
    public final int dropItemId;
    public final int dropCount;
    public final Temperament temperament;
    /** Non-null only for wildlife the player can tame into a PetManager companion. */
    public final PetType tameableAs;

    MobType(String displayName, int maxHealth, float moveSpeed, int contactDamage,
            boolean isFlying, int dropItemId, int dropCount,
            Temperament temperament, PetType tameableAs) {
        this.displayName = displayName;
        this.maxHealth = maxHealth;
        this.moveSpeed = moveSpeed;
        this.contactDamage = contactDamage;
        this.isFlying = isFlying;
        this.dropItemId = dropItemId;
        this.dropCount = dropCount;
        this.temperament = temperament;
        this.tameableAs = tameableAs;
    }

    public boolean isTameable() {
        return tameableAs != null;
    }
}
