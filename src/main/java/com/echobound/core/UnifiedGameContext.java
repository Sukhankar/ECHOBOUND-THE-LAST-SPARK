package com.echobound.core;

import com.echobound.alchemy.AlchemyManager;
import com.echobound.arcade.ArcadeCabinet;
import com.echobound.arcade.PostGameEngine;
import com.echobound.audio.SoundEngine;
import com.echobound.audio.SoundType;
import com.echobound.boss.CorruptionSwarmManager;
import com.echobound.building.BuildingStructureType;
import com.echobound.building.StructureManager;
import com.echobound.combat.ModdedWeapon;
import com.echobound.companion.MountManager;
import com.echobound.companion.PetManager;
import com.echobound.cooking.CookingManager;
import com.echobound.crafting.CraftingEngine;
import com.echobound.crafting.CraftingRecipe;
import com.echobound.entity.mob.MobEntity;
import com.echobound.settings.Difficulty;
import com.echobound.entity.mob.MobManager;
import com.echobound.entity.mob.MobType;
import com.echobound.faction.FactionManager;
import com.echobound.farming.CropType;
import com.echobound.farming.FarmingManager;
import com.echobound.items.EquipmentManager;
import com.echobound.items.ItemRegistry;
import com.echobound.magic.MagicSchool;
import com.echobound.magic.RelicManager;
import com.echobound.magic.Spell;
import com.echobound.magic.SpellCombiner;
import com.echobound.museum.MuseumManager;
import com.echobound.npc.NPCManager;
import com.echobound.physics3d.AABB3D;
import com.echobound.physics3d.Vec3;
import com.echobound.pool.ObjectPool;
import com.echobound.pool.SpellProjectile;
import com.echobound.quest.QuestManager;
import com.echobound.sandbox.DayNightCycle;
import com.echobound.sandbox.PlayerSandboxEntity;
import com.echobound.sandbox.SandboxWorld;
import com.echobound.sandbox.WorldChunk;
import com.echobound.story.StoryProgressionEngine;
import com.echobound.fishing.FishingEngine;
import com.echobound.treasure.TreasureManager;
import com.echobound.puzzle.PuzzleManager;

import java.util.HashMap;
import java.util.Map;

public class UnifiedGameContext {
    public final SandboxWorld world;
    public final PlayerSandboxEntity player;
    public final DayNightCycle dayNightCycle;
    public final SoundEngine soundEngine;

    // Subsystem Managers
    public final MobManager mobManager;
    public final CookingManager cookingManager;
    public final AlchemyManager alchemyManager;
    public final FarmingManager farmingManager;
    public final PetManager petManager;
    public final MountManager mountManager;
    public final EquipmentManager equipmentManager;
    public final RelicManager relicManager;
    public final FactionManager factionManager;
    public final NPCManager npcManager;
    public final QuestManager questManager;
    public final StoryProgressionEngine storyEngine;
    public final StructureManager structureManager;
    public final CorruptionSwarmManager swarmManager;
    public final MuseumManager museumManager;
    public final ArcadeCabinet arcadeCabinet;
    public final PostGameEngine postGameEngine;
    public final FishingEngine fishingEngine;
    public final TreasureManager treasureManager;
    public final PuzzleManager puzzleManager;

    // FX & Windows
    public final com.echobound.fx.ParticleFXManager particleFXManager;
    public final com.echobound.fx.FloatingTextManager floatingTextManager;
    public com.echobound.items.BackpackTier backpackTier = com.echobound.items.BackpackTier.STARTER;
    public ModdedWeapon activeWeapon;

    // Projectile Object Pool
    public final ObjectPool<SpellProjectile> spellPool;
    public final Map<Integer, Integer> playerInventory = new HashMap<>();

    // Ambient wildlife spawning — see updateWildlifeSpawning(). Before this, MobManager.spawnMob
    // was never called anywhere in the live game (only from tests): the entire creature/combat
    // system existed but nothing ever populated the world with it during normal play.
    private static final float WILDLIFE_SPAWN_INTERVAL = 6.0f;
    private static final int MAX_AMBIENT_MOBS = 10;
    private static final float SPAWN_MIN_DIST = 90.0f;
    private static final float SPAWN_MAX_DIST = 230.0f;
    private static final MobType[] GRASSLAND_POOL = {
        MobType.WOODLAND_FOX, MobType.SKY_CLOUDBIRD, MobType.EMBER_CAT,
        MobType.FIELD_RAT, MobType.MARSH_BEETLE, MobType.SHADOW_CREEPER
    };
    private static final MobType[] DESERT_POOL = {
        MobType.FIELD_RAT, MobType.CORRUPTED_DRONE, MobType.MAGMA_GOLEM
    };
    private static final MobType[] TUNDRA_POOL = {
        MobType.CAVE_GLOWBAT, MobType.MOSS_TURTLE_CREATURE, MobType.VOID_STALKER
    };
    private final java.util.Random wildlifeRng = new java.util.Random();
    private float wildlifeSpawnTimer = 2.0f; // first attempt shortly after the world loads

    // Mythical wildlife — a separate, much rarer roll from ordinary ambient spawning above,
    // so a dragon (or a phoenix/unicorn worth taming) stays a genuine event instead of just
    // another entry in the regular animal rotation. At most one legendary creature is ever
    // alive in the world at a time.
    private static final float LEGENDARY_CHECK_INTERVAL = 45.0f;
    private static final float LEGENDARY_SPAWN_CHANCE = 0.12f; // per check, ~once every ~6 minutes on average
    private static final MobType[] LEGENDARY_POOL = {
        MobType.DRAGON, MobType.PHOENIX_CREATURE, MobType.UNICORN_CREATURE
    };
    private float legendarySpawnTimer = 20.0f;

    public UnifiedGameContext() {
        this.world = new SandboxWorld(42L);
        this.player = new PlayerSandboxEntity(0, 0, 10);
        this.dayNightCycle = new DayNightCycle();
        this.soundEngine = new SoundEngine();

        this.particleFXManager = new com.echobound.fx.ParticleFXManager();
        this.floatingTextManager = new com.echobound.fx.FloatingTextManager();
        this.activeWeapon = new ModdedWeapon(ItemRegistry.WEAPON_IRON_SWORD, 25);

        this.mobManager = new MobManager();
        this.cookingManager = new CookingManager();
        this.alchemyManager = new AlchemyManager();
        this.farmingManager = new FarmingManager();
        this.petManager = new PetManager();
        this.mountManager = new MountManager();
        this.equipmentManager = new EquipmentManager();
        this.relicManager = new RelicManager();
        this.factionManager = new FactionManager();
        this.npcManager = new NPCManager();
        this.questManager = new QuestManager();
        this.storyEngine = new StoryProgressionEngine();
        this.structureManager = new StructureManager();
        this.swarmManager = new CorruptionSwarmManager();
        this.museumManager = new MuseumManager();
        this.arcadeCabinet = new ArcadeCabinet("Spark Runner Classic");
        this.postGameEngine = new PostGameEngine();
        this.fishingEngine = new FishingEngine();
        this.treasureManager = new TreasureManager();
        this.puzzleManager = new PuzzleManager();

        this.spellPool = new ObjectPool<>(64, SpellProjectile::new);

        // Starting purse for the NPC shop system.
        playerInventory.put(ItemRegistry.CURRENCY_SPARK_COIN, 150);
    }

    public void update(float dt) {
        // 1. Day / Night Cycle
        dayNightCycle.update(dt);

        // 2. Traversal speed modifications from Mount, Cooking, and Equipment
        float baseSpeed = 160.0f * cookingManager.getSpeedMultiplier() * equipmentManager.getDashMultiplier()
                * petManager.getSpeedMultiplier();
        float effectiveSpeed = mountManager.getActiveSpeed(baseSpeed);
        player.setMaxSpeed(effectiveSpeed);

        // 3. Player physics/input is advanced once by EchoBoundMasterEngine.tick(), which
        // calls the real ctx.player.update(world, inLeft, ..., mineHeld, placePressed, dt)
        // BEFORE calling this method. A second call used to happen right here via the
        // now-removed zero-input update(dt, world) overload — since that overload passed
        // mineHeld=false and placePressed=false unconditionally, it silently reset
        // miningProgress back to 0 (and re-zeroed velocity) at the end of every single tick,
        // completely breaking mining and block placement in the shipped game. It also
        // double-applied gravity for one extra dt each tick. Found by driving the real
        // engine tick loop end-to-end and checking mining actually removed a block —
        // it didn't, until this redundant call was removed.

        // 4. Update Living NPCs with weather/time schedule
        npcManager.update(dayNightCycle);

        // 5. Update Life-sim: Cooking, Alchemy, Farming
        cookingManager.update(dt);
        alchemyManager.update(dt);
        farmingManager.update(dt, false, false);

        // 6. Base Building Safe Zones & Nighttime Corruption Swarms
        int beaconCount = structureManager.getStructureCount(BuildingStructureType.SPARK_BEACON);
        swarmManager.update(dayNightCycle, beaconCount);

        // 7. Update Mobs AI relative to player position and safe zones
        mobManager.update(dt, player.getPosition(), structureManager);
        updateCombat(dt);
        updateWildlifeSpawning(dt);
        updateLegendarySpawning(dt);

        // 8. Story Progression & Post-Game unlocks
        postGameEngine.checkStoryProgression(storyEngine);

        // 9. Fishing & Puzzles
        fishingEngine.update(dt);
        puzzleManager.update(player.getPosition(), null);

        // 10. FX Updates
        particleFXManager.update(dt);
        particleFXManager.updateWeather(dt, player.pos.x, player.pos.y - 60.0f, dayNightCycle.getWeather());
        floatingTextManager.update(dt);
    }

    /** Periodically spawns one ambient creature at a random ring around the player, picked
     *  from a pool matching the biome at that spot (see SandboxWorld.getBiomeAt / the
     *  WorldChunk biome system) — capped so the world doesn't fill up with wildlife forever. */
    private void updateWildlifeSpawning(float dt) {
        wildlifeSpawnTimer -= dt;
        if (wildlifeSpawnTimer > 0) return;
        wildlifeSpawnTimer = WILDLIFE_SPAWN_INTERVAL;

        if (mobManager.getActiveMobCount() >= MAX_AMBIENT_MOBS) return;

        float angle = wildlifeRng.nextFloat() * (float) (Math.PI * 2.0);
        float dist = SPAWN_MIN_DIST + wildlifeRng.nextFloat() * (SPAWN_MAX_DIST - SPAWN_MIN_DIST);
        float spawnX = player.pos.x + (float) Math.cos(angle) * dist;
        float spawnY = player.pos.y + (float) Math.sin(angle) * dist;

        int bx = (int) Math.floor(spawnX / WorldChunk.BLOCK_PIXEL_SIZE);
        int by = (int) Math.floor(spawnY / WorldChunk.BLOCK_PIXEL_SIZE);
        int topZ = world.getTopSolidBlockZ(bx, by);
        float spawnZ = (topZ + 1) * WorldChunk.BLOCK_PIXEL_SIZE;

        WorldChunk.Biome biome = world.getBiomeAt(bx, by);
        MobType[] pool = switch (biome) {
            case DESERT -> DESERT_POOL;
            case TUNDRA -> TUNDRA_POOL;
            default -> GRASSLAND_POOL;
        };
        MobType type = pool[wildlifeRng.nextInt(pool.length)];
        mobManager.spawnMob(type,
                bx * WorldChunk.BLOCK_PIXEL_SIZE + 8, by * WorldChunk.BLOCK_PIXEL_SIZE + 8, spawnZ);
    }

    private boolean hasLegendaryActive() {
        for (MobEntity mob : mobManager.getActiveMobs()) {
            for (MobType legendary : LEGENDARY_POOL) {
                if (mob.type == legendary) return true;
            }
        }
        return false;
    }

    private void updateLegendarySpawning(float dt) {
        legendarySpawnTimer -= dt;
        if (legendarySpawnTimer > 0) return;
        legendarySpawnTimer = LEGENDARY_CHECK_INTERVAL;

        if (hasLegendaryActive()) return; // at most one at a time
        if (wildlifeRng.nextFloat() > LEGENDARY_SPAWN_CHANCE) return;

        float angle = wildlifeRng.nextFloat() * (float) (Math.PI * 2.0);
        float dist = SPAWN_MAX_DIST * 0.8f + wildlifeRng.nextFloat() * SPAWN_MAX_DIST; // further out than ordinary wildlife
        float spawnX = player.pos.x + (float) Math.cos(angle) * dist;
        float spawnY = player.pos.y + (float) Math.sin(angle) * dist;

        int bx = (int) Math.floor(spawnX / WorldChunk.BLOCK_PIXEL_SIZE);
        int by = (int) Math.floor(spawnY / WorldChunk.BLOCK_PIXEL_SIZE);
        int topZ = world.getTopSolidBlockZ(bx, by);
        float spawnZ = (topZ + 1) * WorldChunk.BLOCK_PIXEL_SIZE;

        MobType type = LEGENDARY_POOL[wildlifeRng.nextInt(LEGENDARY_POOL.length)];
        mobManager.spawnMob(type,
                bx * WorldChunk.BLOCK_PIXEL_SIZE + 8, by * WorldChunk.BLOCK_PIXEL_SIZE + 8, spawnZ);
    }

    public boolean performJump() {
        boolean jumped = player.jump();
        if (jumped) {
            soundEngine.play(player.isGrounded() ? SoundType.JUMP : SoundType.DOUBLE_JUMP);
        }
        return jumped;
    }

    public boolean performDash(float dx, float dy) {
        boolean dashed = player.dash(dx, dy);
        if (dashed) {
            soundEngine.play(SoundType.DASH);
        }
        return dashed;
    }

    public boolean castDualSpell(MagicSchool s1, MagicSchool s2, Vec3 direction) {
        Spell combined = SpellCombiner.combine(s1, s2);
        if (combined == null) return false;

        SpellProjectile proj = spellPool.acquire();
        Vec3 spawnPos = player.getPosition();
        proj.spawn(spawnPos.x, spawnPos.y, spawnPos.z + 1.0f,
                   direction.x * 240.0f, direction.y * 240.0f, direction.z * 240.0f,
                   combined.damage, 3.0f);

        // Trigger appropriate sound effect
        if ("Fire Tornado".equals(combined.name)) {
            soundEngine.play(SoundType.FIRE_TORNADO);
        } else if ("Storm Burst".equals(combined.name)) {
            soundEngine.play(SoundType.STORM_BURST);
        } else {
            soundEngine.play(SoundType.CAST_SPELL);
        }

        // Elemental particle burst
        particleFXManager.spawnElementalBurst(spawnPos.x, spawnPos.y, spawnPos.z + 1.0f, s1, 12);
        particleFXManager.spawnElementalBurst(spawnPos.x, spawnPos.y, spawnPos.z + 1.0f, s2, 12);
        floatingTextManager.spawnMessage(spawnPos.x, spawnPos.y, spawnPos.z + 14.0f, combined.name, combined.spellColor);

        // Damage mobs in line of fire
        AABB3D spellHitArea = new AABB3D(
            spawnPos.x - 2.0f, spawnPos.y - 2.0f, spawnPos.z - 1.0f,
            spawnPos.x + 2.0f, spawnPos.y + 2.0f, spawnPos.z + 3.0f
        );
        mobManager.applyDamageArea(spellHitArea, combined.damage, spawnPos, playerInventory);

        return true;
    }

    public boolean craftRecipe(CraftingRecipe recipe) {
        boolean success = CraftingEngine.craft(recipe, playerInventory);
        if (success) {
            soundEngine.play(SoundType.CRAFT_SUCCESS);
        }
        return success;
    }

    public void attackWithWeapon(ModdedWeapon weapon, Vec3 attackCenter) {
        if (weapon == null) return;
        int damage = weapon.calculateTotalDamage();
        // Boost with Pet damage if active
        if (weapon.hasLightningElement()) {
            damage = (int) (damage * petManager.getLightningDamageMultiplier());
        }
        if (weapon.hasFireElement()) {
            damage = (int) (damage * petManager.getFireDamageMultiplier());
        }

        // Was +/-1.5 xy, +/-1-2 z — the same stale small-unit scale MobEntity's hitbox used
        // to be (see the comment there). A melee swing that size could essentially never
        // overlap a mob's real, pixel-scale hitbox; nothing in the live game ever called this
        // method at all, so the mismatch went unnoticed until wiring an actual attack input.
        AABB3D attackArea = new AABB3D(
            attackCenter.x - 22.0f, attackCenter.y - 22.0f, attackCenter.z - 16.0f,
            attackCenter.x + 22.0f, attackCenter.y + 22.0f, attackCenter.z + 24.0f
        );
        int hits = mobManager.applyDamageArea(attackArea, damage, player.getPosition(), playerInventory);
        if (hits > 0) {
            // Impact feedback only on an actual hit — a damage number used to pop up on every
            // swing, including ones that hit nothing, so damage looked meaningless.
            soundEngine.play(SoundType.ENEMY_HIT);
            particleFXManager.spawnBurst(attackCenter.x, attackCenter.y, attackCenter.z, new java.awt.Color(255, 215, 60), 10, 45.0f);
            floatingTextManager.spawnDamage(attackCenter.x, attackCenter.y, attackCenter.z + 10.0f, damage, weapon.hasEchoDuplicate());
        } else {
            soundEngine.play(SoundType.MINE_BLOCK); // swing whoosh
        }
    }

    // ── Incoming damage ───────────────────────────────────────────────────────────────

    /** Set from the Options menu (see EchoBoundMasterEngine.applyLiveSettings). */
    public Difficulty difficulty = Difficulty.NORMAL;

    private static final float HIT_INVULNERABILITY = 0.8f;
    private static final float RESPAWN_INVULNERABILITY = 3.0f;
    private static final float REGEN_INTERVAL = 3.0f;
    private float invulnerabilityTimer = 0f;
    private float timeSinceHurt = 999f;
    private float regenTimer = 0f;
    private boolean playerHurtThisTick = false;

    /** True once per damaging hit, so the engine can play the hurt animation and screen shake. */
    public boolean consumePlayerHurt() {
        boolean hurt = playerHurtThisTick;
        playerHurtThisTick = false;
        return hurt;
    }

    /** Applies enemy damage, scaled by difficulty, with brief invulnerability after each hit. */
    public void damagePlayer(int baseDamage) {
        if (invulnerabilityTimer > 0f || player.health <= 0) return;
        int dmg = Math.max(1, Math.round(baseDamage * difficulty.damageMultiplier));
        player.health = Math.max(0, player.health - dmg);
        invulnerabilityTimer = HIT_INVULNERABILITY;
        timeSinceHurt = 0f;
        regenTimer = 0f;
        playerHurtThisTick = true;
        soundEngine.play(SoundType.PLAYER_HURT);
        particleFXManager.spawnBurst(player.pos.x, player.pos.y, player.pos.z + 10.0f, new java.awt.Color(220, 40, 50), 8, 40.0f);
        floatingTextManager.spawnDamage(player.pos.x, player.pos.y, player.pos.z + 24.0f, dmg, false);
        if (player.health <= 0) respawnPlayer();
    }

    private void respawnPlayer() {
        int topZ = world.getTopSolidBlockZ(8, 8);
        player.pos.set(8 * WorldChunk.BLOCK_PIXEL_SIZE, 8 * WorldChunk.BLOCK_PIXEL_SIZE,
                       (topZ + 1) * WorldChunk.BLOCK_PIXEL_SIZE);
        player.vel.set(0, 0, 0);
        player.health = player.maxHealth;
        invulnerabilityTimer = RESPAWN_INVULNERABILITY;
        floatingTextManager.spawnMessage(player.pos.x, player.pos.y, player.pos.z + 32.0f,
                                         "Defeated - respawned", new java.awt.Color(255, 170, 60));
    }

    private void updateCombat(float dt) {
        invulnerabilityTimer = Math.max(0f, invulnerabilityTimer - dt);
        timeSinceHurt += dt;

        for (MobEntity mob : mobManager.getActiveMobs()) {
            if (!mob.strikeReady) continue;
            mob.strikeReady = false;
            if (mob.isAlive) {
                // MobType.contactDamage is on the mob-HP scale (12-35); the player has 12 health
                // points total, so it's scaled down to roughly 1-4 per hit.
                damagePlayer(Math.max(1, Math.round(mob.type.contactDamage / 10.0f)));
            }
        }

        if (player.health > 0 && player.health < player.maxHealth
                && timeSinceHurt >= difficulty.regenDelaySeconds) {
            regenTimer += dt;
            if (regenTimer >= REGEN_INTERVAL) {
                regenTimer = 0f;
                player.health++;
                floatingTextManager.spawnHeal(player.pos.x, player.pos.y, player.pos.z + 24.0f, 1);
            }
        }
    }
}
