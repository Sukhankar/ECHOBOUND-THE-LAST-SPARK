package com.echobound.entity;

import com.echobound.core.InputSource;
import com.echobound.core.Quality;
import com.echobound.fx.ParticlePool;
import com.echobound.physics.AABB;
import com.echobound.physics.PhysicsConfig;
import com.echobound.world.TileMap;
import com.echobound.world.TileType;

import java.awt.*;
import java.util.List;

/**
 * Shared deterministic physics and rendering entity for Rin and Echo replays.
 * Driven strictly through the swappable InputSource interface.
 */
public class Runner {
    // Current simulation position
    public float x;
    public float y;

    // Previous tick position (for render interpolation)
    public float prevX;
    public float prevY;

    // Velocities
    public float vx;
    public float vy;

    // Facing direction (-1 left, 1 right)
    public int facing = 1;

    // Collision box
    private final AABB hitbox = new AABB(0, 0, PhysicsConfig.HITBOX_WIDTH, PhysicsConfig.HITBOX_HEIGHT);

    // Ground & Wall status
    public boolean onGround = false;
    private boolean wasOnGround = false;
    public boolean onWallLeft = false;
    public boolean onWallRight = false;

    // Timers & counters
    public float coyoteTimer = 0.0f;
    public float jumpBufferTimer = 0.0f;
    public float dashTimer = 0.0f;
    public float dashCooldownTimer = 0.0f;
    public float wallLockTimer = 0.0f;
    public boolean canDoubleJump = false;
    public boolean canAirDash = false;
    public boolean jumpRising = false;
    public boolean isDashing = false;

    // Dash directional vector
    private float dashDirX = 0.0f;
    private float dashDirY = 0.0f;

    // State
    public RunnerState state = RunnerState.IDLE;

    // Swappable input provider
    private InputSource input;

    // Visual squash & stretch
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    // Procedural energy scarf nodes (5 joints)
    private static final int SCARF_SEGMENTS = 5;
    private final float[] scarfX = new float[SCARF_SEGMENTS];
    private final float[] scarfY = new float[SCARF_SEGMENTS];

    // Reference to particle pool
    private ParticlePool particlePool;

    // Color theme for procedural rendering (e.g. Rin cyan, Echo translucent)
    public Color emblemColor = new Color(0, 240, 255); // Cyan (Echo ready)
    public boolean isEchoGhost = false;

    public Runner(float spawnX, float spawnY, InputSource input, ParticlePool particlePool) {
        this.input = input;
        this.particlePool = particlePool;
        reset(spawnX, spawnY);
    }

    public void setInputSource(InputSource input) {
        this.input = input;
    }

    public InputSource getInputSource() {
        return input;
    }

    public void reset(float spawnX, float spawnY) {
        this.x = spawnX;
        this.y = spawnY;
        this.prevX = spawnX;
        this.prevY = spawnY;
        this.vx = 0;
        this.vy = 0;
        this.facing = 1;
        this.onGround = false;
        this.wasOnGround = false;
        this.onWallLeft = false;
        this.onWallRight = false;
        this.coyoteTimer = 0;
        this.jumpBufferTimer = 0;
        this.dashTimer = 0;
        this.dashCooldownTimer = 0;
        this.wallLockTimer = 0;
        this.canDoubleJump = true;
        this.canAirDash = true;
        this.jumpRising = false;
        this.isDashing = false;
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;

        for (int i = 0; i < SCARF_SEGMENTS; i++) {
            scarfX[i] = spawnX;
            scarfY[i] = spawnY + 4;
        }
    }

    public AABB getHitbox() {
        hitbox.set(x, y, PhysicsConfig.HITBOX_WIDTH, PhysicsConfig.HITBOX_HEIGHT);
        return hitbox;
    }

    public void tick(TileMap map) {
        prevX = x;
        prevY = y;
        wasOnGround = onGround;

        float dt = PhysicsConfig.FIXED_DT;

        // Timers tick down
        if (coyoteTimer > 0) coyoteTimer -= dt;
        if (jumpBufferTimer > 0) jumpBufferTimer -= dt;
        if (dashCooldownTimer > 0) dashCooldownTimer -= dt;
        if (wallLockTimer > 0) wallLockTimer -= dt;

        // Read input actions
        boolean inLeft = input.isLeft();
        boolean inRight = input.isRight();
        boolean inUp = input.isUp();
        boolean inDown = input.isDown();
        boolean jumpPressed = input.isJumpPressed();
        boolean jumpHeld = input.isJumpHeld();
        boolean dashPressed = input.isDashPressed();

        // Register jump buffer
        if (jumpPressed) {
            jumpBufferTimer = PhysicsConfig.JUMP_BUFFER;
        }

        // Handle Dash initialization
        if (dashPressed && dashCooldownTimer <= 0 && !isDashing) {
            if (onGround || canAirDash) {
                if (!onGround) canAirDash = false;

                // 8-directional dash vector
                int dx = 0;
                int dy = 0;
                if (inLeft) dx -= 1;
                if (inRight) dx += 1;
                if (inUp) dy -= 1;
                if (inDown) dy += 1;

                if (dx == 0 && dy == 0) {
                    dx = facing; // Default horizontal forward dash
                }

                float len = (float) Math.hypot(dx, dy);
                dashDirX = dx / len;
                dashDirY = dy / len;

                isDashing = true;
                dashTimer = PhysicsConfig.DASH_DURATION;
                dashCooldownTimer = PhysicsConfig.DASH_COOLDOWN;

                // Visual squash
                scaleX = 1.4f;
                scaleY = 0.7f;

                if (particlePool != null) {
                    particlePool.spawnBurst(x + 5, y + 7, 8, 80.0f, 0.2f, 3.0f, new Color(0, 240, 255));
                }
            }
        }

        // Process Dash Movement OR Normal Movement
        if (isDashing) {
            dashTimer -= dt;
            vx = dashDirX * PhysicsConfig.DASH_SPEED;
            vy = dashDirY * PhysicsConfig.DASH_SPEED;

            if (dashTimer <= 0) {
                isDashing = false;
                // Preserve remaining horizontal speed capped to run speed
                vx = Math.signum(vx) * Math.min(Math.abs(vx), PhysicsConfig.RUN_SPEED);
                if (vy < 0) vy *= 0.5f;
            }
        } else {
            // Horizontal Acceleration & Deceleration
            if (wallLockTimer <= 0) {
                float targetVx = 0.0f;
                if (inLeft && !inRight) {
                    targetVx = -PhysicsConfig.RUN_SPEED;
                    facing = -1;
                } else if (inRight && !inLeft) {
                    targetVx = PhysicsConfig.RUN_SPEED;
                    facing = 1;
                }

                float accelRate = onGround ? PhysicsConfig.GROUND_ACCEL : PhysicsConfig.AIR_ACCEL;
                if (targetVx != 0) {
                    // Accelerating in movement direction
                    if (vx < targetVx) {
                        vx = Math.min(targetVx, vx + accelRate * dt);
                    } else if (vx > targetVx) {
                        vx = Math.max(targetVx, vx - accelRate * dt);
                    }
                } else {
                    // Decelerating to stop
                    float decelRate = PhysicsConfig.DECEL * dt;
                    if (vx > 0) {
                        vx = Math.max(0, vx - decelRate);
                    } else if (vx < 0) {
                        vx = Math.min(0, vx + decelRate);
                    }
                }
            }

            // Wall Slide check
            boolean canWallSlide = (onWallLeft && inLeft) || (onWallRight && inRight);
            boolean wallSliding = canWallSlide && vy > 0 && !onGround;

            // Gravity & Apex Hang
            float currentGravity = PhysicsConfig.GRAVITY;
            if (Math.abs(vy) < PhysicsConfig.APEX_THRESHOLD && jumpHeld) {
                currentGravity *= PhysicsConfig.APEX_GRAVITY_MULT; // Apex float
            }

            vy += currentGravity * dt;
            if (vy > PhysicsConfig.MAX_FALL) {
                vy = PhysicsConfig.MAX_FALL;
            }

            if (wallSliding && vy > PhysicsConfig.WALL_SLIDE_MAX) {
                vy = PhysicsConfig.WALL_SLIDE_MAX;
            }

            // Variable Jump Height: release while rising multiplies vy by 0.45
            if (jumpRising && vy < 0 && !jumpHeld) {
                vy *= PhysicsConfig.JUMP_RELEASE_MULT;
                jumpRising = false;
            }
            if (vy >= 0) {
                jumpRising = false;
            }

            // Jump Execution
            if (jumpBufferTimer > 0) {
                if (onGround || coyoteTimer > 0) {
                    // Ground Jump
                    vy = -PhysicsConfig.JUMP_SPEED;
                    coyoteTimer = 0;
                    jumpBufferTimer = 0;
                    jumpRising = true;
                    onGround = false;
                    scaleX = 0.8f;
                    scaleY = 1.3f;
                    if (particlePool != null) {
                        particlePool.spawnDust(x + 5, y + 14, 4, new Color(200, 220, 240));
                    }
                } else if (onWallLeft) {
                    // Wall Jump Right
                    vx = PhysicsConfig.WALL_JUMP_X;
                    vy = -PhysicsConfig.WALL_JUMP_Y;
                    wallLockTimer = PhysicsConfig.WALL_JUMP_LOCK_TIME;
                    facing = 1;
                    jumpBufferTimer = 0;
                    jumpRising = true;
                    canAirDash = true;
                    scaleX = 0.8f;
                    scaleY = 1.3f;
                    if (particlePool != null) {
                        particlePool.spawnDust(x, y + 7, 4, new Color(0, 240, 255));
                    }
                } else if (onWallRight) {
                    // Wall Jump Left
                    vx = -PhysicsConfig.WALL_JUMP_X;
                    vy = -PhysicsConfig.WALL_JUMP_Y;
                    wallLockTimer = PhysicsConfig.WALL_JUMP_LOCK_TIME;
                    facing = -1;
                    jumpBufferTimer = 0;
                    jumpRising = true;
                    canAirDash = true;
                    scaleX = 0.8f;
                    scaleY = 1.3f;
                    if (particlePool != null) {
                        particlePool.spawnDust(x + 10, y + 7, 4, new Color(0, 240, 255));
                    }
                } else if (canDoubleJump) {
                    // Double Jump
                    vy = -PhysicsConfig.DOUBLE_JUMP_SPEED;
                    canDoubleJump = false;
                    jumpBufferTimer = 0;
                    jumpRising = true;
                    scaleX = 0.85f;
                    scaleY = 1.25f;
                    if (particlePool != null) {
                        particlePool.spawnBurst(x + 5, y + 12, 6, 60.0f, 0.2f, 2.5f, emblemColor);
                    }
                }
            }
        }

        // Axis-Separated Tile Collision Resolution
        onWallLeft = false;
        onWallRight = false;

        // 1. Horizontal Move & Collide
        x += vx * dt;
        hitbox.set(x, y, PhysicsConfig.HITBOX_WIDTH, PhysicsConfig.HITBOX_HEIGHT);
        List<AABB> solidTiles = map.getCollidingSolidTiles(hitbox);
        for (AABB tile : solidTiles) {
            if (vx > 0) {
                x = tile.getLeft() - PhysicsConfig.HITBOX_WIDTH;
                onWallRight = true;
                vx = 0;
            } else if (vx < 0) {
                x = tile.getRight();
                onWallLeft = true;
                vx = 0;
            }
            hitbox.set(x, y, PhysicsConfig.HITBOX_WIDTH, PhysicsConfig.HITBOX_HEIGHT);
        }

        // 2. Vertical Move & Collide
        boolean droppingThroughOneWay = inDown && jumpPressed;
        onGround = false;
        y += vy * dt;
        hitbox.set(x, y, PhysicsConfig.HITBOX_WIDTH, PhysicsConfig.HITBOX_HEIGHT);

        solidTiles = map.getCollidingSolidTiles(hitbox);
        for (AABB tile : solidTiles) {
            if (vy > 0) {
                y = tile.getTop() - PhysicsConfig.HITBOX_HEIGHT;
                onGround = true;
                vy = 0;
            } else if (vy < 0) {
                y = tile.getBottom();
                vy = 0;
            }
            hitbox.set(x, y, PhysicsConfig.HITBOX_WIDTH, PhysicsConfig.HITBOX_HEIGHT);
        }

        // One-Way Platform Collision
        if (!droppingThroughOneWay && vy >= 0) {
            List<AABB> oneWays = map.getOneWayTiles(hitbox);
            for (AABB plat : oneWays) {
                // Check if player's bottom was above the platform top on the previous tick
                float prevBottom = prevY + PhysicsConfig.HITBOX_HEIGHT;
                if (prevBottom <= plat.getTop() + 3.0f && (y + PhysicsConfig.HITBOX_HEIGHT) >= plat.getTop()) {
                    y = plat.getTop() - PhysicsConfig.HITBOX_HEIGHT;
                    onGround = true;
                    vy = 0;
                    hitbox.set(x, y, PhysicsConfig.HITBOX_WIDTH, PhysicsConfig.HITBOX_HEIGHT);
                    break;
                }
            }
        }

        // Hazard Collision Check
        if (checkHazardCollision(map)) {
            // Respawn in under half a second
            reset(map.getSpawnX(), map.getSpawnY());
            return;
        }

        // Refresh Ground Abilities & Landing Effects
        if (onGround) {
            coyoteTimer = PhysicsConfig.COYOTE_TIME;
            canDoubleJump = true;
            canAirDash = true;

            // Landing squash and dust
            if (!wasOnGround) {
                scaleX = 1.3f;
                scaleY = 0.75f;
                if (particlePool != null) {
                    particlePool.spawnDust(x + 5, y + 14, 5, new Color(180, 200, 220));
                }
            }
        }

        if (onWallLeft || onWallRight) {
            canAirDash = true; // Wall contact refreshes air dash
        }

        // Spring-damped squash/stretch return to (1.0, 1.0)
        scaleX += (1.0f - scaleX) * 0.25f;
        scaleY += (1.0f - scaleY) * 0.25f;

        // Update Animation State
        if (isDashing) {
            state = RunnerState.DASH;
        } else if (onGround) {
            state = (Math.abs(vx) > 10.0f) ? RunnerState.RUN : RunnerState.IDLE;
        } else if ((onWallLeft && inLeft) || (onWallRight && inRight)) {
            state = RunnerState.WALL_SLIDE;
        } else if (vy < -PhysicsConfig.APEX_THRESHOLD) {
            state = RunnerState.JUMP_RISING;
        } else if (Math.abs(vy) <= PhysicsConfig.APEX_THRESHOLD) {
            state = RunnerState.JUMP_APEX;
        } else {
            state = RunnerState.FALL;
        }

        // Update procedural scarf trail
        updateScarfTrail();
    }

    private boolean checkHazardCollision(TileMap map) {
        int startX = (int) Math.floor(hitbox.getLeft() / PhysicsConfig.TILE_SIZE);
        int endX = (int) Math.floor((hitbox.getRight() - 0.001f) / PhysicsConfig.TILE_SIZE);
        int startY = (int) Math.floor(hitbox.getTop() / PhysicsConfig.TILE_SIZE);
        int endY = (int) Math.floor((hitbox.getBottom() - 0.001f) / PhysicsConfig.TILE_SIZE);

        for (int ty = startY; ty <= endY; ty++) {
            for (int tx = startX; tx <= endX; tx++) {
                if (map.getTile(tx, ty).isHazard()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateScarfTrail() {
        float neckX = x + (facing == 1 ? 2.0f : 8.0f);
        float neckY = y + 4.0f;
        scarfX[0] = neckX;
        scarfY[0] = neckY;

        for (int i = 1; i < SCARF_SEGMENTS; i++) {
            float targetX = scarfX[i - 1] - facing * (i * 2.5f) - (vx * 0.04f);
            float targetY = scarfY[i - 1] + 1.0f - (vy * 0.02f);
            scarfX[i] += (targetX - scarfX[i]) * 0.45f;
            scarfY[i] += (targetY - scarfY[i]) * 0.45f;
        }
    }

    /**
     * Render the Runner with render interpolation, run lean, squash-and-stretch,
     * glowing Spark Gauntlet, energy scarf, boots, goggles, and jacket emblem.
     */
    public void render(Graphics2D g, float camX, float camY, float alpha, Quality quality) {
        // Linear render interpolation between prevX/prevY and x/y
        float renderX = prevX + (x - prevX) * alpha;
        float renderY = prevY + (y - prevY) * alpha;

        int drawX = (int) (renderX - camX);
        int drawY = (int) (renderY - camY);

        Graphics2D g2 = (Graphics2D) g.create();

        // Center origin for squash and stretch
        float centerX = drawX + PhysicsConfig.HITBOX_WIDTH * 0.5f;
        float centerY = drawY + PhysicsConfig.HITBOX_HEIGHT;

        g2.translate(centerX, centerY);

        // Run lean tilt angle
        if (state == RunnerState.RUN) {
            double leanAngle = Math.toRadians((vx / PhysicsConfig.RUN_SPEED) * 8.0);
            g2.rotate(leanAngle);
        } else if (state == RunnerState.WALL_SLIDE) {
            g2.rotate(Math.toRadians(facing * -6.0));
        }

        g2.scale(scaleX, scaleY);
        g2.translate(-centerX, -centerY);

        // 1. Render Energy Scarf trailing behind
        renderScarf(g2, camX, camY, quality);

        // 2. Render Procedural Silhouette (Rin or Echo Ghost)
        renderSilhouette(g2, drawX, drawY, quality);

        g2.dispose();
    }

    private void renderScarf(Graphics2D g, float camX, float camY, Quality quality) {
        for (int i = 0; i < SCARF_SEGMENTS - 1; i++) {
            int x1 = (int) (scarfX[i] - camX);
            int y1 = (int) (scarfY[i] - camY);
            int x2 = (int) (scarfX[i + 1] - camX);
            int y2 = (int) (scarfY[i + 1] - camY);

            int alpha = Math.max(40, 240 - i * 45);
            if (isEchoGhost) alpha = (int) (alpha * 0.5f);

            // Scarf core energy line
            g.setColor(new Color(emblemColor.getRed(), emblemColor.getGreen(), emblemColor.getBlue(), alpha));
            g.setStroke(new BasicStroke(Math.max(1.0f, 3.5f - i * 0.6f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x1, y1, x2, y2);

            // Glow on HIGH quality
            if (quality == Quality.HIGH && !isEchoGhost) {
                g.setColor(new Color(emblemColor.getRed(), emblemColor.getGreen(), emblemColor.getBlue(), alpha / 4));
                g.setStroke(new BasicStroke(6.0f - i * 0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(x1, y1, x2, y2);
            }
        }
        g.setStroke(new BasicStroke(1.0f));
    }

    private void renderSilhouette(Graphics2D g, int dx, int dy, Quality quality) {
        int w = (int) PhysicsConfig.HITBOX_WIDTH;
        int h = (int) PhysicsConfig.HITBOX_HEIGHT;

        if (isEchoGhost) {
            // Translucent cyan ghost for Echo
            g.setColor(new Color(0, 240, 255, 110));
            g.fillRoundRect(dx, dy, w, h, 4, 4);
            g.setColor(new Color(200, 255, 255, 180));
            g.drawRoundRect(dx, dy, w, h, 4, 4);
            return;
        }

        // RIN PROCEDURAL ARTWORK:
        // Backpack
        int bpX = (facing == 1) ? dx - 2 : dx + w - 2;
        g.setColor(new Color(50, 40, 35));
        g.fillRect(bpX, dy + 4, 4, 6);

        // Short Jacket (Dark navy/teal runner jacket)
        g.setColor(new Color(28, 38, 56));
        g.fillRect(dx + 1, dy + 3, w - 2, 6);

        // Glowing Jacket Emblem (Cyan = ready)
        g.setColor(emblemColor);
        g.fillRect(dx + (facing == 1 ? 5 : 3), dy + 5, 2, 2);
        if (quality != Quality.LOW) {
            g.setColor(new Color(emblemColor.getRed(), emblemColor.getGreen(), emblemColor.getBlue(), 80));
            g.drawRect(dx + (facing == 1 ? 4 : 2), dy + 4, 4, 4);
        }

        // Head & Hair
        g.setColor(new Color(245, 185, 90)); // Warm hair
        g.fillRect(dx + 2, dy, 6, 4);

        // Goggles on forehead
        g.setColor(new Color(255, 210, 40));
        g.fillRect(dx + (facing == 1 ? 4 : 2), dy + 1, 4, 2);

        // Glowing Spark Gauntlet on leading hand
        int gauntletX = (facing == 1) ? dx + w - 2 : dx - 1;
        g.setColor(new Color(0, 240, 255));
        g.fillRect(gauntletX, dy + 6, 3, 3);
        if (quality == Quality.HIGH) {
            g.setColor(new Color(0, 240, 255, 90));
            g.drawOval(gauntletX - 1, dy + 5, 5, 5);
        }

        // Big Boots (Rust/Orange Spark Runner boots)
        g.setColor(new Color(210, 80, 40));
        g.fillRect(dx + 1, dy + 9, w - 2, 5);
        // Boot treads
        g.setColor(new Color(40, 30, 30));
        g.fillRect(dx, dy + 12, w, 2);
    }
}
