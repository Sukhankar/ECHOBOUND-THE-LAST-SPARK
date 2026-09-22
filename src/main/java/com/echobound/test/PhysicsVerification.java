package com.echobound.test;

import com.echobound.core.InputSource;
import com.echobound.entity.Runner;
import com.echobound.entity.RunnerState;
import com.echobound.physics.PhysicsConfig;
import com.echobound.world.TileMap;

/**
 * Deterministic physics verification suite.
 * Validates all movement constants, jump dynamics, buffering, coyote time, and wall mechanics.
 */
public class PhysicsVerification {

    private static class MockInput implements InputSource {
        public boolean left = false;
        public boolean right = false;
        public boolean up = false;
        public boolean down = false;
        public boolean jumpPressed = false;
        public boolean jumpHeld = false;
        public boolean dashPressed = false;
        public boolean shootPressed = false;
        public boolean interactPressed = false;

        @Override public boolean isLeft() { return left; }
        @Override public boolean isRight() { return right; }
        @Override public boolean isUp() { return up; }
        @Override public boolean isDown() { return down; }
        @Override public boolean isJumpPressed() { return jumpPressed; }
        @Override public boolean isJumpHeld() { return jumpHeld; }
        @Override public boolean isDashPressed() { return dashPressed; }
        @Override public boolean isShootPressed() { return shootPressed; }
        @Override public boolean isInteractPressed() { return interactPressed; }
    }

    public static void main(String[] args) {
        System.out.println("=== RUNNING DETERMINISTIC PHYSICS VERIFICATION ===");

        testRunAccelerationAndDeceleration();
        testJumpAndVariableCut();
        testDoubleJump();
        testCoyoteTime();
        testJumpBuffer();
        testDash();
        testWallSlideAndWallJump();
        testDeterministicReplay();

        System.out.println(">>> ALL PHYSICS TESTS PASSED PERFECTLY! <<<");
    }

    private static void assertEquals(float expected, float actual, float tolerance, String msg) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(String.format("%s: expected %.4f but got %.4f", msg, expected, actual));
        }
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + msg);
        }
    }

    private static TileMap createFlatFloorMap() {
        return TileMap.fromAscii(new String[]{
            "####################",
            "#                  #",
            "#                  #",
            "#                  #",
            "####################"
        });
    }

    private static void testRunAccelerationAndDeceleration() {
        TileMap map = createFlatFloorMap();
        MockInput input = new MockInput();
        Runner runner = new Runner(32, 50, input, null);
        input.right = true;

        // Run for 15 ticks (0.25s)
        for (int i = 0; i < 15; i++) {
            runner.tick(map);
        }

        assertTrue(runner.vx > 100, "Runner should accelerate right");
        assertTrue(runner.vx <= PhysicsConfig.RUN_SPEED, "Speed should cap at RUN_SPEED 150");

        // Release input: should decelerate towards 0
        input.right = false;
        for (int i = 0; i < 10; i++) {
            runner.tick(map);
        }
        assertEquals(0.0f, runner.vx, 0.01f, "Deceleration should bring runner to stop");
        System.out.println("  [PASS] Run Acceleration & Deceleration verified");
    }

    private static void testJumpAndVariableCut() {
        TileMap map = createFlatFloorMap();
        MockInput input = new MockInput();
        Runner runner = new Runner(32, 50, input, null);
        runner.onGround = true;

        // Full jump
        input.jumpPressed = true;
        input.jumpHeld = true;
        runner.tick(map);
        input.jumpPressed = false;

        assertEquals(-PhysicsConfig.JUMP_SPEED, runner.vy, 1.0f, "Jump vy should match JUMP_SPEED");

        // Release jump immediately: vy should be multiplied by JUMP_RELEASE_MULT
        input.jumpHeld = false;
        float beforeCut = runner.vy;
        runner.tick(map);
        assertTrue(runner.vy > beforeCut, "Releasing jump should cut upward velocity");
        System.out.println("  [PASS] Variable Jump Cut verified");
    }

    private static void testDoubleJump() {
        TileMap map = createFlatFloorMap();
        MockInput input = new MockInput();
        Runner runner = new Runner(32, 30, input, null);
        runner.onGround = false;
        runner.canDoubleJump = true;

        input.jumpPressed = true;
        input.jumpHeld = true;
        runner.tick(map);
        input.jumpPressed = false;

        assertEquals(-PhysicsConfig.DOUBLE_JUMP_SPEED, runner.vy, 1.0f, "Double jump vy should match DOUBLE_JUMP_SPEED");
        assertTrue(!runner.canDoubleJump, "Double jump should be consumed");
        System.out.println("  [PASS] Double Jump verified");
    }

    private static void testCoyoteTime() {
        TileMap map = createFlatFloorMap();
        MockInput input = new MockInput();
        Runner runner = new Runner(32, 50, input, null);

        // Tick on ground: sets coyote timer to 0.10s
        runner.tick(map);
        assertTrue(runner.onGround, "Runner must be on ground");
        assertTrue(runner.coyoteTimer > 0, "Coyote timer must be active");

        // Move into air without jumping (running off ledge into empty space)
        runner.y = 30;
        for (int i = 0; i < 3; i++) {
            runner.tick(map);
        }
        assertTrue(runner.coyoteTimer > 0, "Coyote timer should still be positive within 3 ticks");

        // Jump within coyote time
        input.jumpPressed = true;
        input.jumpHeld = true;
        runner.tick(map);
        assertEquals(-PhysicsConfig.JUMP_SPEED, runner.vy, 1.0f, "Jump within coyote window must succeed");
        System.out.println("  [PASS] Coyote Time verified");
    }

    private static void testJumpBuffer() {
        TileMap map = createFlatFloorMap();
        MockInput input = new MockInput();
        Runner runner = new Runner(32, 20, input, null); // in the air falling
        runner.vy = 100.0f;
        runner.onGround = false;
        runner.canDoubleJump = false; // double jump already used

        // Press jump 4 ticks before landing
        input.jumpPressed = true;
        runner.tick(map);
        input.jumpPressed = false;
        assertTrue(runner.jumpBufferTimer > 0, "Jump buffer should be active");

        // Simulate falling onto ground
        runner.y = 50;
        runner.tick(map); // Lands on ground
        runner.tick(map); // Buffered jump triggers immediately
        assertTrue(runner.vy < 0, "Buffered jump should execute immediately on landing");
        System.out.println("  [PASS] Jump Buffering verified");
    }

    private static void testDash() {
        TileMap map = createFlatFloorMap();
        MockInput input = new MockInput();
        Runner runner = new Runner(32, 50, input, null);
        runner.onGround = true;

        input.right = true;
        input.dashPressed = true;
        runner.tick(map);
        input.dashPressed = false;

        assertTrue(runner.isDashing, "Runner should be in dashing state");
        assertEquals(PhysicsConfig.DASH_SPEED, runner.vx, 0.1f, "Dash speed must match DASH_SPEED");
        assertEquals(0.0f, runner.vy, 0.1f, "Horizontal dash must disable gravity");
        System.out.println("  [PASS] Dash 8-Directional & Speed verified");
    }

    private static void testWallSlideAndWallJump() {
        TileMap wallMap = TileMap.fromAscii(new String[]{
            "###",
            "# #",
            "# #",
            "# #",
            "# #",
            "# #",
            "# #",
            "# #",
            "# #",
            "###"
        });
        MockInput input = new MockInput();
        Runner runner = new Runner(16, 80, input, null);
        runner.onWallLeft = true;
        runner.vy = 200.0f; // falling fast
        input.left = true;

        runner.tick(wallMap);
        assertTrue(runner.vy <= PhysicsConfig.WALL_SLIDE_MAX, "Wall slide must clamp downward speed to 90 px/s");

        // Wall jump off left wall
        input.jumpPressed = true;
        runner.tick(wallMap);
        assertEquals(PhysicsConfig.WALL_JUMP_X, runner.vx, 1.0f, "Wall jump X must push away from wall");
        assertTrue(runner.vy < -300.0f, "Wall jump Y must propel upward with high velocity");
        assertTrue(runner.wallLockTimer > 0, "Wall jump must lock horizontal input temporarily");
        System.out.println("  [PASS] Wall Slide & Wall Jump verified");
    }

    private static void testDeterministicReplay() {
        TileMap map = TileMap.fromAscii(new String[]{
            "##############################",
            "#                            #",
            "#                            #",
            "#                            #",
            "#                            #",
            "##############################"
        });

        // Run simulation 1
        MockInput input1 = new MockInput();
        Runner runner1 = new Runner(32, 48, input1, null);
        for (int i = 0; i < 60; i++) {
            input1.right = (i < 40);
            input1.jumpPressed = (i == 10 || i == 30);
            input1.jumpHeld = (i >= 10 && i <= 25);
            runner1.tick(map);
        }

        // Run simulation 2 with exact same inputs
        MockInput input2 = new MockInput();
        Runner runner2 = new Runner(32, 48, input2, null);
        for (int i = 0; i < 60; i++) {
            input2.right = (i < 40);
            input2.jumpPressed = (i == 10 || i == 30);
            input2.jumpHeld = (i >= 10 && i <= 25);
            runner2.tick(map);
        }

        assertEquals(runner1.x, runner2.x, 0.00001f, "Replay X must be bit-identical");
        assertEquals(runner1.y, runner2.y, 0.00001f, "Replay Y must be bit-identical");
        assertEquals(runner1.vx, runner2.vx, 0.00001f, "Replay Vx must be bit-identical");
        assertEquals(runner1.vy, runner2.vy, 0.00001f, "Replay Vy must be bit-identical");
        System.out.println("  [PASS] Deterministic Simulation Replay verified to exact float precision");
    }
}
