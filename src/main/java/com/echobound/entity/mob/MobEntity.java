package com.echobound.entity.mob;

import com.echobound.physics3d.AABB3D;
import com.echobound.physics3d.Vec3;

public class MobEntity {
    public enum AIState {
        IDLE,
        WANDER,
        DETECT,
        CHASE,
        ATTACK,
        FLEE,
        RETREAT
    }

    private static final float FLEE_RADIUS = 40.0f;
    private static final float WANDER_RADIUS = 100.0f;

    // Hostile AI ranges — these used to be 2.0 / 24.0, a "meters"-like scale left over from
    // early prototyping that was never converted to match the pixel-scale world everything
    // else here uses (blocks are 16px, move speeds are ~100-200 px/sec, this session's own
    // ambient wildlife spawns land 90-230px away). At the old scale a hostile mob needed the
    // player within *2 pixels* to attack and *24 pixels* to even start chasing — in practice
    // they just stood frozen in IDLE forever, since nothing in normal play gets that close
    // before physically colliding. Rescaled to actually work in this world.
    private static final float ATTACK_RANGE = 26.0f;
    private static final float CHASE_RANGE = 200.0f;
    private static final float HALF_WIDTH = 8.0f;
    private static final float HEIGHT = 28.0f;
    private static final float KNOCKBACK_PX = 12.0f;

    public final int id;
    public final MobType type;
    public final Vec3 position = new Vec3();
    public final Vec3 velocity = new Vec3();
    public final AABB3D hitbox;
    public int currentHealth;
    public AIState state = AIState.IDLE;
    public boolean isAlive = true;
    public float attackCooldown = 0.0f;

    // Wander behavior (PASSIVE/NEUTRAL creatures): pick a direction, hold it briefly, repeat.
    private float wanderDirX = 0f, wanderDirY = 0f;
    private float wanderTimer = 0f;
    private final java.util.Random rng;
    private final float spawnX, spawnY;

    public MobEntity(int id, MobType type, float x, float y, float z) {
        this.id = id;
        this.type = type;
        this.position.set(x, y, z);
        this.currentHealth = type.maxHealth;
        this.spawnX = x;
        this.spawnY = y;
        this.rng = new java.util.Random(id * 7919L + Double.doubleToLongBits(x + y));
        // Hitbox: was 0.8x0.8x1.6 — the same stale small-unit scale as ATTACK_RANGE/CHASE_RANGE
        // above, meaning a melee swing's attack area (see UnifiedGameContext.attackWithWeapon)
        // could essentially never overlap it in the real pixel-scale world. Rescaled to ~16px
        // wide, ~28px tall — sized to actually sit under a 32px creature sprite.
        this.hitbox = new AABB3D(x - HALF_WIDTH, y - HALF_WIDTH, z, x + HALF_WIDTH, y + HALF_WIDTH, z + HEIGHT);
    }

    public void update(float dt, Vec3 playerPos, boolean isInSafeZone) {
        if (!isAlive) return;

        if (attackCooldown > 0) {
            attackCooldown -= dt;
        }

        if (type.temperament != Temperament.HOSTILE) {
            updateWildlife(dt, playerPos);
        } else {
            updateHostile(dt, playerPos, isInSafeZone);
        }

        // Apply movement
        position.x += velocity.x * dt;
        position.y += velocity.y * dt;
        position.z += velocity.z * dt;

        // Sync hitbox
        hitbox.set(position.x - HALF_WIDTH, position.y - HALF_WIDTH, position.z,
                   position.x + HALF_WIDTH, position.y + HALF_WIDTH, position.z + HEIGHT);
    }

    private void updateHostile(float dt, Vec3 playerPos, boolean isInSafeZone) {
        // If player is in safe zone, retreat
        if (isInSafeZone) {
            state = AIState.RETREAT;
            // Run away from player
            Vec3 fleeDir = position.subtract(playerPos).normalized();
            velocity.x = fleeDir.x * type.moveSpeed;
            velocity.y = fleeDir.y * type.moveSpeed;
        } else {
            float distToPlayer = (float) position.distanceTo(playerPos);
            if (distToPlayer <= ATTACK_RANGE) {
                state = AIState.ATTACK;
                velocity.x = 0;
                velocity.y = 0;
            } else if (distToPlayer <= CHASE_RANGE) {
                // Kept as a single CHASE band (not split into DETECT/CHASE sub-ranges) —
                // hostile mobs weren't what needed richer states; DETECT exists on the enum
                // for future use, e.g. by mobs that telegraph before charging.
                state = AIState.CHASE;
                Vec3 chaseDir = playerPos.subtract(position).normalized();
                velocity.x = chaseDir.x * type.moveSpeed;
                velocity.y = chaseDir.y * type.moveSpeed;
            } else {
                state = AIState.IDLE;
                velocity.x = 0;
                velocity.y = 0;
            }
        }
    }

    /** Passive/neutral wildlife: wanders near its spawn point, and passive creatures flee
     *  when the player closes within FLEE_RADIUS. Neutral creatures ignore proximity. */
    private void updateWildlife(float dt, Vec3 playerPos) {
        float distToPlayer = (float) position.distanceTo(playerPos);

        if (type.temperament == Temperament.PASSIVE && distToPlayer <= FLEE_RADIUS) {
            state = AIState.FLEE;
            Vec3 fleeDir = position.subtract(playerPos).normalized();
            velocity.x = fleeDir.x * type.moveSpeed * 1.15f; // flee a little faster than they wander
            velocity.y = fleeDir.y * type.moveSpeed * 1.15f;
            wanderTimer = 0f; // re-roll a fresh wander direction once it stops fleeing
            return;
        }

        wanderTimer -= dt;
        if (wanderTimer <= 0f) {
            wanderTimer = 1.5f + rng.nextFloat() * 2.5f;
            if (rng.nextFloat() < 0.3f
                    || Math.abs(position.x - spawnX) > WANDER_RADIUS
                    || Math.abs(position.y - spawnY) > WANDER_RADIUS) {
                // Idle pause, or wandered too far — head back toward spawn instead of drifting forever.
                if (Math.abs(position.x - spawnX) > WANDER_RADIUS || Math.abs(position.y - spawnY) > WANDER_RADIUS) {
                    Vec3 home = new Vec3(spawnX, spawnY, position.z).subtract(position).normalized();
                    wanderDirX = home.x;
                    wanderDirY = home.y;
                } else {
                    wanderDirX = 0f;
                    wanderDirY = 0f;
                }
            } else {
                float angle = rng.nextFloat() * (float) (Math.PI * 2);
                wanderDirX = (float) Math.cos(angle);
                wanderDirY = (float) Math.sin(angle);
            }
        }

        boolean moving = wanderDirX != 0f || wanderDirY != 0f;
        state = moving ? AIState.WANDER : AIState.IDLE;
        velocity.x = wanderDirX * type.moveSpeed * 0.4f; // ambient wildlife ambles, doesn't sprint
        velocity.y = wanderDirY * type.moveSpeed * 0.4f;
    }

    public void takeDamage(int damage, Vec3 knockbackSource) {
        if (!isAlive) return;

        currentHealth -= damage;
        if (knockbackSource != null) {
            Vec3 kbDir = position.subtract(knockbackSource).normalized();
            position.x += kbDir.x * KNOCKBACK_PX;
            position.y += kbDir.y * KNOCKBACK_PX;
        }

        if (currentHealth <= 0) {
            currentHealth = 0;
            isAlive = false;
        }
    }
}
