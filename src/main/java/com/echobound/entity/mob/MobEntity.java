package com.echobound.entity.mob;

import com.echobound.physics3d.AABB3D;
import com.echobound.physics3d.Vec3;

public class MobEntity {
    public enum AIState {
        IDLE,
        CHASE,
        ATTACK,
        RETREAT
    }

    public final int id;
    public final MobType type;
    public final Vec3 position = new Vec3();
    public final Vec3 velocity = new Vec3();
    public final AABB3D hitbox;
    public int currentHealth;
    public AIState state = AIState.IDLE;
    public boolean isAlive = true;
    public float attackCooldown = 0.0f;

    public MobEntity(int id, MobType type, float x, float y, float z) {
        this.id = id;
        this.type = type;
        this.position.set(x, y, z);
        this.currentHealth = type.maxHealth;
        // Hitbox: 0.8 width, 0.8 depth, 1.6 height
        this.hitbox = new AABB3D(x - 0.4f, y - 0.4f, z, x + 0.4f, y + 0.4f, z + 1.6f);
    }

    public void update(float dt, Vec3 playerPos, boolean isInSafeZone) {
        if (!isAlive) return;

        if (attackCooldown > 0) {
            attackCooldown -= dt;
        }

        // If player is in safe zone, retreat
        if (isInSafeZone) {
            state = AIState.RETREAT;
            // Run away from player
            Vec3 fleeDir = position.subtract(playerPos).normalized();
            velocity.x = fleeDir.x * type.moveSpeed;
            velocity.y = fleeDir.y * type.moveSpeed;
        } else {
            float distToPlayer = (float) position.distanceTo(playerPos);
            if (distToPlayer <= 2.0f) {
                state = AIState.ATTACK;
                velocity.x = 0;
                velocity.y = 0;
            } else if (distToPlayer <= 24.0f) {
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

        // Apply movement
        position.x += velocity.x * dt;
        position.y += velocity.y * dt;
        position.z += velocity.z * dt;

        // Sync hitbox
        hitbox.set(position.x - 0.4f, position.y - 0.4f, position.z,
                   position.x + 0.4f, position.y + 0.4f, position.z + 1.6f);
    }

    public void takeDamage(int damage, Vec3 knockbackSource) {
        if (!isAlive) return;

        currentHealth -= damage;
        if (knockbackSource != null) {
            Vec3 kbDir = position.subtract(knockbackSource).normalized();
            position.x += kbDir.x * 0.8f;
            position.y += kbDir.y * 0.8f;
        }

        if (currentHealth <= 0) {
            currentHealth = 0;
            isAlive = false;
        }
    }
}
