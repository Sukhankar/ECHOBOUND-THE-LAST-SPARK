package com.echobound.test;

import com.echobound.audio.SoundEngine;
import com.echobound.audio.SoundSynthesizer;
import com.echobound.audio.SoundType;
import com.echobound.building.BuildingStructureType;
import com.echobound.combat.ModdedWeapon;
import com.echobound.combat.RuneType;
import com.echobound.companion.MountType;
import com.echobound.companion.PetType;
import com.echobound.core.UnifiedGameContext;
import com.echobound.crafting.CraftingEngine;
import com.echobound.crafting.CraftingRecipe;
import com.echobound.crafting.CraftingStationType;
import com.echobound.entity.mob.MobEntity;
import com.echobound.entity.mob.MobManager;
import com.echobound.entity.mob.MobType;
import com.echobound.items.ItemRegistry;
import com.echobound.magic.MagicSchool;
import com.echobound.physics3d.AABB3D;
import com.echobound.physics3d.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Part6UnifiedEngineVerification {
    public static void main(String[] args) {
        System.out.println("=== RUNNING PART 6 UNIFIED ENGINE & COMBAT VERIFICATION SUITE ===");

        testProceduralSoundSynthesisAndEngine();
        testMobArchetypesAndAIStateMachine();
        testMobCombatKnockbackAndLootDrops();
        testUnifiedGameContextIntegration();

        System.out.println(">>> ALL PART 6 TESTS PASSED PERFECTLY! <<<");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("FAILED: " + message);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(String.format("%s: expected %d but got %d", message, expected, actual));
        }
    }

    private static void assertEquals(float expected, float actual, float epsilon, String message) {
        if (Math.abs(expected - actual) > epsilon) {
            throw new AssertionError(String.format("%s: expected %.2f but got %.2f", message, expected, actual));
        }
    }

    private static void assertObjectEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(String.format("%s: expected %s but got %s", message, expected, actual));
        }
    }

    private static void testProceduralSoundSynthesisAndEngine() {
        assertEquals(16, SoundType.values().length, "Must have 16 procedural sound types (14 original + player-hurt and enemy-hit)");

        // Synthesize and check buffers
        for (SoundType type : SoundType.values()) {
            byte[] pcm = SoundSynthesizer.generateSoundBytes(type);
            assertTrue(pcm != null && pcm.length > 0, "Synthesized audio for " + type + " must not be empty");
        }

        // Test SoundEngine cache and controls
        SoundEngine engine = new SoundEngine();
        byte[] jumpBuffer = engine.getCachedBuffer(SoundType.JUMP);
        assertTrue(jumpBuffer != null && jumpBuffer.length > 0, "Engine must precache JUMP sound");

        assertEquals(0, engine.getTotalSoundsPlayed(), "Initial sounds played must be 0");
        engine.play(SoundType.JUMP);
        assertEquals(1, engine.getTotalSoundsPlayed(), "Total sounds played should increment to 1");

        engine.setMasterVolume(0.5f);
        assertEquals(0.5f, engine.getMasterVolume(), 0.01f, "Master volume must be 0.5");

        engine.setSoundEnabled(false);
        engine.play(SoundType.DASH);
        assertEquals(1, engine.getTotalSoundsPlayed(), "Disabled sound engine must ignore play requests");

        engine.shutdown();
        System.out.println("  [PASS] Procedural Audio Synthesis & SoundEngine verified");
    }

    private static void testMobArchetypesAndAIStateMachine() {
        int hostileCount = 0;
        for (MobType t : MobType.values()) {
            if (t.temperament == com.echobound.entity.mob.Temperament.HOSTILE) hostileCount++;
        }
        assertEquals(5, hostileCount, "Must have 5 hostile mob archetypes (original 4 + legendary Dragon)");

        MobManager mm = new MobManager();
        assertEquals(0, mm.getActiveMobCount(), "Initial mob count must be 0");

        // Ranges are pixel-scale (see MobEntity.ATTACK_RANGE/CHASE_RANGE = 26/200) — this test
        // used to spawn at (30,0,0) expecting a 24-unit chase range, a "meters"-like scale
        // that was never actually connected to the real pixel-scale world (blocks are 16px,
        // move speeds ~100-200 px/sec): a hostile mob needed the player within 2 *pixels* to
        // attack. Values below are scaled up to actually exercise the fixed, pixel-scale AI.
        //
        // Spawn Shadow Creeper at (300, 0, 0) while player is at (0, 0, 0).
        // Distance is 300px > 200px CHASE_RANGE -> IDLE
        MobEntity creeper = mm.spawnMob(MobType.SHADOW_CREEPER, 300.0f, 0.0f, 0.0f);
        Vec3 playerPos = new Vec3(0, 0, 0);
        mm.update(0.1f, playerPos, null);

        assertObjectEquals(MobEntity.AIState.IDLE, creeper.state, "Mob at 300px distance must be IDLE");
        assertEquals(0.0f, creeper.velocity.x, 0.01f, "Idle velocity must be 0");

        // Move player to (150, 0, 0). Distance is 150px <= 200px CHASE_RANGE -> CHASE towards player (-X)
        playerPos.set(150.0f, 0.0f, 0.0f);
        mm.update(0.1f, playerPos, null);

        assertObjectEquals(MobEntity.AIState.CHASE, creeper.state, "Mob within 200px must CHASE player");
        assertTrue(creeper.velocity.x < 0, "Chasing velocity must move toward player (-X)");

        // Move player to within ATTACK_RANGE (26px) of the mob.
        creeper.position.set(300.0f, 0.0f, 0.0f);
        playerPos.set(280.0f, 0.0f, 0.0f);
        mm.update(0.1f, playerPos, null);

        assertObjectEquals(MobEntity.AIState.ATTACK, creeper.state, "Mob within 26px must switch to ATTACK");

        System.out.println("  [PASS] Mob Archetypes & AI State Machine Transitions verified");
    }

    private static void testMobCombatKnockbackAndLootDrops() {
        MobManager mm = new MobManager();
        // Spawn Magma Golem with 220 HP at (5, 5, 0)
        MobEntity golem = mm.spawnMob(MobType.MAGMA_GOLEM, 5.0f, 5.0f, 0.0f);
        assertEquals(220, golem.currentHealth, "Initial Golem health must be 220");

        Map<Integer, Integer> inv = new HashMap<>();
        Vec3 attackSource = new Vec3(4.0f, 5.0f, 0.0f);
        AABB3D attackArea = new AABB3D(4.0f, 4.0f, -1.0f, 6.0f, 6.0f, 3.0f);

        // Strike for 100 damage
        int hits = mm.applyDamageArea(attackArea, 100, attackSource, inv);
        assertEquals(1, hits, "Hitbox overlap must register 1 hit");
        assertEquals(120, golem.currentHealth, "Golem health should be 120 (220 - 100)");
        assertTrue(golem.position.x > 5.0f, "Knockback should displace golem along +X direction");
        assertTrue(golem.isAlive, "Golem must still be alive");

        // Fatal strike for 150 damage (120 - 150 <= 0 -> Slain!)
        mm.applyDamageArea(attackArea, 150, attackSource, inv);
        assertEquals(0, mm.getActiveMobCount(), "Slain golem should be removed from active mobs");
        assertEquals(3, inv.get(ItemRegistry.MAT_CRYSTAL_SHARD), "Loot drops must award 3 Crystal Shards");

        System.out.println("  [PASS] Mob Combat Damage, Knockback & Loot Drops verified");
    }

    private static void testUnifiedGameContextIntegration() {
        UnifiedGameContext ctx = new UnifiedGameContext();

        // 1. Verify subsystem presence
        assertTrue(ctx.world != null, "World must be initialized");
        assertTrue(ctx.player != null, "Player must be initialized");
        assertTrue(ctx.soundEngine != null, "SoundEngine must be initialized");
        assertTrue(ctx.mobManager != null, "MobManager must be initialized");
        assertTrue(ctx.storyEngine != null, "StoryEngine must be initialized");

        // 2. Traversal & Mount speed integration
        ctx.mountManager.unlockMount(MountType.FOREST_ELK);
        ctx.mountManager.mount();
        ctx.update(0.016f); // 60Hz tick
        assertEquals(280.0f, ctx.player.getMaxSpeed(), 0.01f, "Player max speed must scale to Forest Elk (280.0)");

        // 3. Jump and Dash action triggers
        ctx.player.onGround = true;
        boolean jumped = ctx.performJump();
        assertTrue(jumped, "Unified jump must succeed");

        boolean dashed = ctx.performDash(1.0f, 0.0f);
        assertTrue(dashed, "Unified dash must succeed");

        // 4. Dual Spell casting with Object Pool and Mob collision
        // Spawn Corrupted Drone in front of player
        ctx.player.pos.set(0, 0, 0);
        MobEntity drone = ctx.mobManager.spawnMob(MobType.CORRUPTED_DRONE, 1.0f, 0.0f, 1.0f);
        assertEquals(1, ctx.mobManager.getActiveMobCount(), "Drone must be spawned");

        // Cast Fire Tornado (Ember + Gale) forward
        boolean spellCast = ctx.castDualSpell(MagicSchool.EMBER, MagicSchool.GALE, new Vec3(1, 0, 0));
        assertTrue(spellCast, "Dual spell casting must succeed");
        assertTrue(drone.currentHealth < MobType.CORRUPTED_DRONE.maxHealth, "Fire Tornado must damage drone");

        // 5. Crafting integration
        List<CraftingRecipe> campfireRecipes = CraftingEngine.getRecipesForStation(CraftingStationType.CAMPFIRE);
        CraftingRecipe torchRecipe = campfireRecipes.get(0);
        ctx.playerInventory.put(ItemRegistry.MAT_WOOD, 1);
        ctx.playerInventory.put(ItemRegistry.MAT_SPARK_SHARD, 1);

        boolean crafted = ctx.craftRecipe(torchRecipe);
        assertTrue(crafted, "Unified crafting must succeed");
        assertEquals(0, ctx.playerInventory.get(ItemRegistry.MAT_WOOD), "Remaining wood must be 0");
        assertEquals(4, ctx.playerInventory.get(ItemRegistry.MAT_SPARK_SHARD), "Should yield 4 Spark Torches");

        // 6. Base Building Safe Zone repellent
        ctx.structureManager.placeStructure(BuildingStructureType.SPARK_BEACON, 0, 0, 0);
        ctx.dayNightCycle.setTimeOfDay(22.0f); // Nighttime
        ctx.update(0.016f);

        // With Spark Beacon at (0,0,0), player at (0,0,0) is in safe zone
        assertTrue(ctx.structureManager.isSafeFromCorruption(0, 0, 0), "Player must be in Spark Beacon safe zone");

        ctx.soundEngine.shutdown();
        System.out.println("  [PASS] Unified Game Context Coordinator & Deterministic Master Loop verified");
    }
}
