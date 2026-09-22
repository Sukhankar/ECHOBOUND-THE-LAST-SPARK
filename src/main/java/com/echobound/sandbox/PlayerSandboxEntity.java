package com.echobound.sandbox;

import com.echobound.physics3d.AABB3D;
import com.echobound.physics3d.Vec3;

import java.awt.*;
import java.util.List;

public class PlayerSandboxEntity {
    // 3D coordinates & velocity
    public final Vec3 pos = new Vec3();
    public final Vec3 prevPos = new Vec3();
    public final Vec3 vel = new Vec3();

    // Hitbox dimensions (10x10 footprint, 14 height)
    public static final float HITBOX_W = 10.0f;
    public static final float HITBOX_D = 10.0f;
    public static final float HITBOX_H = 14.0f;
    private final AABB3D hitbox = new AABB3D();

    // Movement speeds
    public static final float MOVE_SPEED = 140.0f;
    public static final float SPRINT_SPEED = 240.0f;
    public static final float JUMP_Z_SPEED = 240.0f;
    public static final float GRAVITY_Z = 680.0f;
    public static final float GLIDE_GRAVITY_Z = 160.0f;

    // Traversal status
    public boolean onGround = false;
    public boolean canDoubleJump = false;
    public boolean isGliding = false;
    public boolean isDashing = false;
    public float dashTimer = 0.0f;
    public float dashCooldown = 0.0f;

    // Aiming & Facing
    public float facingAngle = 0.0f; // radians
    public float facingDirX = 1.0f;
    public float facingDirY = 0.0f;

    // Mining status
    public float miningProgress = 0.0f;
    public SandboxWorld.BlockHit currentTarget = null;

    // Vitals
    public int health = 12; // 6 hearts
    public int maxHealth = 12;
    public float sparkEnergy = 100.0f;
    public float maxSparkEnergy = 100.0f;

    // Inventory
    public final Inventory inventory = new Inventory();

    public PlayerSandboxEntity(float startX, float startY, float startZ) {
        pos.set(startX, startY, startZ);
        prevPos.set(startX, startY, startZ);
    }

    public AABB3D getHitbox() {
        hitbox.setFromPositionSize(pos.x - HITBOX_W * 0.5f,
                                   pos.y - HITBOX_D * 0.5f,
                                   pos.z,
                                   HITBOX_W, HITBOX_D, HITBOX_H);
        return hitbox;
    }

    public void update(SandboxWorld world, boolean inLeft, boolean inRight, boolean inUp, boolean inDown,
                       boolean jumpPressed, boolean jumpHeld, boolean dashPressed,
                       boolean mineHeld, boolean placePressed, float dt) {
        prevPos.set(pos);

        // Regenerate Spark Energy slowly
        sparkEnergy = Math.min(maxSparkEnergy, sparkEnergy + dt * 6.0f);

        // Timers
        if (dashCooldown > 0) dashCooldown -= dt;
        if (dashTimer > 0) {
            dashTimer -= dt;
            if (dashTimer <= 0) isDashing = false;
        }

        // Horizontal input vector
        float moveX = 0;
        float moveY = 0;
        if (inLeft) moveX -= 1;
        if (inRight) moveX += 1;
        if (inUp) moveY -= 1;
        if (inDown) moveY += 1;

        float moveLen = (float) Math.hypot(moveX, moveY);
        if (moveLen > 0.001f) {
            moveX /= moveLen;
            moveY /= moveLen;
            facingDirX = moveX;
            facingDirY = moveY;
            facingAngle = (float) Math.atan2(moveY, moveX);
        }

        // Dash trigger
        if (dashPressed && dashCooldown <= 0 && !isDashing && sparkEnergy >= 15.0f) {
            isDashing = true;
            dashTimer = 0.18f;
            dashCooldown = 0.60f;
            sparkEnergy -= 15.0f;
            vel.x = facingDirX * 360.0f;
            vel.y = facingDirY * 360.0f;
        }

        if (!isDashing) {
            float speed = MOVE_SPEED;
            vel.x = moveX * speed;
            vel.y = moveY * speed;

            // Z-axis Jump & Double Jump
            if (jumpPressed) {
                if (onGround) {
                    vel.z = JUMP_Z_SPEED;
                    onGround = false;
                    canDoubleJump = true;
                } else if (canDoubleJump) {
                    vel.z = JUMP_Z_SPEED * 0.9f;
                    canDoubleJump = false;
                }
            }

            // Scarf Glide (hold jump while falling)
            isGliding = !onGround && jumpHeld && vel.z < -20.0f;
            float currentGravity = isGliding ? GLIDE_GRAVITY_Z : GRAVITY_Z;

            vel.z -= currentGravity * dt;
            if (vel.z < -340.0f) {
                vel.z = -340.0f;
            }
        }

        // 3D Axis-Separated Collision Movement
        // 1. Move X
        pos.x += vel.x * dt;
        resolveCollisionX(world);

        // 2. Move Y
        pos.y += vel.y * dt;
        resolveCollisionY(world);

        // 3. Move Z
        onGround = false;
        pos.z += vel.z * dt;
        resolveCollisionZ(world);

        // Prevent falling into void beneath chunk
        if (pos.z < 0) {
            pos.z = 0;
            vel.z = 0;
            onGround = true;
        }

        // Update Target Reticle for Mining / Building
        currentTarget = world.raycast(pos.x, pos.y, pos.z + 8.0f,
                                      facingDirX, facingDirY, 0.0f, 48.0f);

        // Mining logic
        if (mineHeld && currentTarget != null && currentTarget.blockType != BlockType.AIR) {
            miningProgress += dt;
            if (miningProgress >= currentTarget.blockType.maxHardness) {
                // Break block!
                world.setBlock(currentTarget.blockX, currentTarget.blockY, currentTarget.blockZ, BlockType.AIR);
                if (currentTarget.blockType.dropItem != null) {
                    inventory.addMaterial(currentTarget.blockType.dropItem, 1);
                }
                miningProgress = 0.0f;
            }
        } else {
            miningProgress = 0.0f;
        }

        // Building logic
        if (placePressed && currentTarget != null) {
            Inventory.SlotItem item = inventory.getSelectedItem();
            if (item != null && item.blockType != null && item.count > 0) {
                int placeX = currentTarget.blockX + currentTarget.faceNormalX;
                int placeY = currentTarget.blockY + currentTarget.faceNormalY;
                int placeZ = currentTarget.blockZ + currentTarget.faceNormalZ;

                // Ensure placed block doesn't overlap player body
                AABB3D candidateBox = new AABB3D(
                    placeX * WorldChunk.BLOCK_PIXEL_SIZE,
                    placeY * WorldChunk.BLOCK_PIXEL_SIZE,
                    placeZ * WorldChunk.BLOCK_PIXEL_SIZE,
                    (placeX + 1) * WorldChunk.BLOCK_PIXEL_SIZE,
                    (placeY + 1) * WorldChunk.BLOCK_PIXEL_SIZE,
                    (placeZ + 1) * WorldChunk.BLOCK_PIXEL_SIZE
                );

                if (!candidateBox.overlaps(getHitbox())) {
                    world.setBlock(placeX, placeY, placeZ, item.blockType);
                    inventory.consumeSelectedItem();
                }
            }
        }
    }

    private void resolveCollisionX(SandboxWorld world) {
        List<AABB3D> blocks = world.getCollidingBlocks(getHitbox());
        for (AABB3D b : blocks) {
            if (vel.x > 0) {
                pos.x = b.minX - HITBOX_W * 0.5f;
                vel.x = 0;
            } else if (vel.x < 0) {
                pos.x = b.maxX + HITBOX_W * 0.5f;
                vel.x = 0;
            }
        }
    }

    private void resolveCollisionY(SandboxWorld world) {
        List<AABB3D> blocks = world.getCollidingBlocks(getHitbox());
        for (AABB3D b : blocks) {
            if (vel.y > 0) {
                pos.y = b.minY - HITBOX_D * 0.5f;
                vel.y = 0;
            } else if (vel.y < 0) {
                pos.y = b.maxY + HITBOX_D * 0.5f;
                vel.y = 0;
            }
        }
    }

    private void resolveCollisionZ(SandboxWorld world) {
        List<AABB3D> blocks = world.getCollidingBlocks(getHitbox());
        for (AABB3D b : blocks) {
            if (vel.z > 0) {
                pos.z = b.minZ - HITBOX_H;
                vel.z = 0;
            } else if (vel.z < 0) {
                pos.z = b.maxZ;
                vel.z = 0;
                onGround = true;
                canDoubleJump = true;
            }
        }
    }
}
