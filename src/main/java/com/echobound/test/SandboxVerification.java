package com.echobound.test;

import com.echobound.physics3d.AABB3D;
import com.echobound.physics3d.Vec3;
import com.echobound.sandbox.*;

public class SandboxVerification {
    public static void main(String[] args) {
        System.out.println("=== RUNNING SANDBOX PART 1 VERIFICATION SUITE ===");

        testVec3Math();
        testAABB3DCollision();
        testWorldChunkAndVoxelEditing();
        testMiningAndInventoryGathering();
        testBlockPlacement();
        testPlayer3DPhysicsAndGlide();
        testEchoSandboxRecordingAndPlayback();
        testDayNightAndWeatherCycle();

        System.out.println(">>> ALL PART 1 SANDBOX TESTS PASSED PERFECTLY! <<<");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("FAILED: " + message);
        }
    }

    private static void assertEquals(float expected, float actual, float tolerance, String message) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(String.format("%s: expected %.4f but got %.4f", message, expected, actual));
        }
    }

    private static void testVec3Math() {
        Vec3 v1 = new Vec3(3, 4, 0);
        assertEquals(5.0f, v1.length(), 0.001f, "Vector length must be 5.0");

        v1.normalize();
        assertEquals(1.0f, v1.length(), 0.001f, "Normalized vector length must be 1.0");

        Vec3 v2 = new Vec3(0, 0, 10);
        assertEquals(10.0f, new Vec3(0, 0, 0).distanceTo(v2), 0.001f, "Distance along Z must match");
        System.out.println("  [PASS] Vec3 3D Spatial Math verified");
    }

    private static void testAABB3DCollision() {
        AABB3D box1 = new AABB3D(0, 0, 0, 16, 16, 16);
        AABB3D box2 = new AABB3D(10, 10, 10, 26, 26, 26);
        AABB3D box3 = new AABB3D(30, 30, 30, 40, 40, 40);

        assertTrue(box1.overlaps(box2), "Box 1 and Box 2 must overlap in 3D");
        assertTrue(!box1.overlaps(box3), "Box 1 and Box 3 must not overlap");
        assertTrue(box1.contains(8, 8, 8), "Box 1 must contain center point");
        System.out.println("  [PASS] AABB3D Spatial Overlap verified");
    }

    private static void testWorldChunkAndVoxelEditing() {
        SandboxWorld world = new SandboxWorld(999L);
        BlockType b = world.getBlock(5, 5, 0);
        assertEquals(BlockType.STONE.id, b.id, 0.0f, "Layer 0 bedrock must be STONE");

        world.setBlock(10, 10, 4, BlockType.SPARK_LAMP);
        BlockType placed = world.getBlock(10, 10, 4);
        assertTrue(placed == BlockType.SPARK_LAMP, "Set block must persist in chunk");
        System.out.println("  [PASS] WorldChunk & Voxel Editing verified");
    }

    private static void testMiningAndInventoryGathering() {
        SandboxWorld world = new SandboxWorld(1234L);
        PlayerSandboxEntity player = new PlayerSandboxEntity(16, 16, 48);

        // Place floor under player and test wood block directly adjacent in front of player
        world.setBlock(1, 1, 2, BlockType.STONE);
        world.setBlock(2, 1, 3, BlockType.WOOD_LOG);
        player.facingDirX = 1.0f;
        player.facingDirY = 0.0f;

        int initialWood = player.inventory.getMaterialCount("WOOD");

        // Simulate mining for full duration
        float mineTime = BlockType.WOOD_LOG.maxHardness + 0.1f;
        int steps = (int) (mineTime / 0.016f);
        for (int i = 0; i < steps; i++) {
            player.update(world, false, false, false, false, false, false, false, true, false, 0.016f);
        }

        // Block must be broken and inventory wood increased
        BlockType afterBreak = world.getBlock(2, 1, 3);
        assertTrue(afterBreak == BlockType.AIR, "Mined block must turn to AIR");
        assertTrue(player.inventory.getMaterialCount("WOOD") > initialWood, "Inventory WOOD must increase");
        System.out.println("  [PASS] Block Mining & Inventory Drops verified");
    }

    private static void testBlockPlacement() {
        SandboxWorld world = new SandboxWorld(1234L);
        PlayerSandboxEntity player = new PlayerSandboxEntity(0, 16, 48);

        // Create solid floor for player, empty slot at (1, 1, 3), and target at (2, 1, 3)
        world.setBlock(0, 1, 2, BlockType.STONE);
        world.setBlock(2, 1, 3, BlockType.STONE);
        player.facingDirX = 1.0f;
        player.facingDirY = 0.0f;
        player.inventory.setSelectedSlot(0); // Wood Planks
        int initialPlankCount = player.inventory.getSelectedItem().count;

        // Press place
        player.update(world, false, false, false, false, false, false, false, false, true, 0.016f);

        assertTrue(player.inventory.getSelectedItem().count < initialPlankCount, "Placing must consume quickslot item");
        System.out.println("  [PASS] Quickslot Block Placement verified");
    }

    private static void testPlayer3DPhysicsAndGlide() {
        SandboxWorld world = new SandboxWorld(1234L);
        PlayerSandboxEntity player = new PlayerSandboxEntity(32, 32, 120);

        // Normal free-fall acceleration
        player.update(world, false, false, false, false, false, false, false, false, false, 0.05f);
        float normalFallVel = player.vel.z;
        assertTrue(normalFallVel < 0, "Player must fall under gravity");

        // Reset and test glide (holding jump while falling)
        player.pos.set(32, 32, 120);
        player.vel.set(0, 0, -50);
        player.update(world, false, false, false, false, false, true, false, false, false, 0.05f);
        assertTrue(player.isGliding, "Player should be gliding when holding jump while falling");
        assertTrue(Math.abs(player.vel.z) < Math.abs(normalFallVel * 3.0f), "Glide gravity must be drastically reduced");
        System.out.println("  [PASS] 3D Jump, Gravity, and Scarf Glide verified");
    }

    private static void testEchoSandboxRecordingAndPlayback() {
        SandboxWorld world = new SandboxWorld(1234L);
        // Clear a 3-block runway
        for (int x = 2; x <= 5; x++) {
            world.setBlock(x, 2, 3, BlockType.STONE);
            world.setBlock(x, 2, 4, BlockType.AIR);
            world.setBlock(x, 2, 5, BlockType.AIR);
        }
        PlayerSandboxEntity player = new PlayerSandboxEntity(32, 32, 64);
        EchoSandboxClone echo = new EchoSandboxClone();

        echo.startRecording(player);
        assertTrue(echo.isRecording, "Echo must be in recording state");

        // Record 10 ticks of moving right
        for (int i = 0; i < 10; i++) {
            echo.recordFrame(false, true, false, false, false, false, false, false, false);
            player.update(world, false, true, false, false, false, false, false, false, false, 0.016f);
        }
        echo.stopRecording();
        echo.deploy();

        assertTrue(echo.isActive, "Echo must be active after deploy");
        assertTrue(echo.getGhostEntity() != null, "Echo ghost entity must be spawned");

        // Tick echo and verify it moves right
        float startGhostX = echo.getGhostEntity().pos.x;
        for (int i = 0; i < 5; i++) {
            echo.update(world, 0.016f);
        }
        assertTrue(echo.getGhostEntity().pos.x > startGhostX, "Echo clone must replay movement actions");
        System.out.println("  [PASS] Echo Sandbox Recording & Playback verified");
    }

    private static void testDayNightAndWeatherCycle() {
        DayNightCycle cycle = new DayNightCycle();
        assertEquals(8.0f, cycle.getTimeOfDay(), 0.01f, "Initial time should be 8:00 AM");
        assertTrue(!cycle.isNight(), "8:00 AM should be Day");

        // Advance 14 hours -> 22:00 (Night)
        cycle.update(140.0f);
        assertTrue(cycle.isNight(), "22:00 must be Night");
        assertTrue(cycle.getDaylightFactor() < 0.3f, "Night daylight factor must be low");

        // Test weather toggle
        WeatherType initialWeather = cycle.getWeather();
        cycle.toggleWeather();
        assertTrue(cycle.getWeather() != initialWeather, "Weather must cycle on toggle");
        System.out.println("  [PASS] Day/Night 24-Hour Cycle & Weather verified");
    }
}
