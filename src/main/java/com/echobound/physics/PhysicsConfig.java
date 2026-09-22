package com.echobound.physics;

/**
 * Single source of truth for all movement, jump, and collision constants.
 * Tile size = 16 px.
 */
public final class PhysicsConfig {
    private PhysicsConfig() {}

    // World & Unit constants
    public static final int TILE_SIZE = 16;
    public static final float FIXED_DT = 1.0f / 60.0f; // 60 Hz deterministic simulation

    // Runner Hitbox (forgiving 10x14)
    public static final float HITBOX_WIDTH = 10.0f;
    public static final float HITBOX_HEIGHT = 14.0f;

    // Movement speeds & accelerations (in px/s and px/s^2)
    public static final float RUN_SPEED = 150.0f;
    public static final float GROUND_ACCEL = 1400.0f;
    public static final float DECEL = 1800.0f;
    public static final float AIR_ACCEL = 1000.0f;

    // Gravity & Fall
    public static final float GRAVITY = 1300.0f;
    public static final float MAX_FALL = 420.0f;
    public static final float APEX_THRESHOLD = 40.0f; // px/s
    public static final float APEX_GRAVITY_MULT = 0.5f;

    // Jumps
    public static final float JUMP_SPEED = 390.0f;
    public static final float JUMP_RELEASE_MULT = 0.45f;
    public static final float DOUBLE_JUMP_SPEED = 330.0f;
    public static final float COYOTE_TIME = 0.10f; // 100 ms (~6 ticks)
    public static final float JUMP_BUFFER = 0.12f; // 120 ms (~7 ticks)

    // Dash (8-directional, gravity off, 1 air dash)
    public static final float DASH_SPEED = 380.0f;
    public static final float DASH_DURATION = 0.14f; // ~8.4 ticks
    public static final float DASH_COOLDOWN = 0.40f; // 24 ticks

    // Wall interactions
    public static final float WALL_SLIDE_MAX = 90.0f;
    public static final float WALL_JUMP_X = 200.0f;
    public static final float WALL_JUMP_Y = 370.0f;
    public static final float WALL_JUMP_LOCK_TIME = 0.10f; // 6 ticks lock horizontal input
}
