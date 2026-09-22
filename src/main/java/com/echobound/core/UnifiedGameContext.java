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
import com.echobound.entity.mob.MobManager;
import com.echobound.faction.FactionManager;
import com.echobound.farming.CropType;
import com.echobound.farming.FarmingManager;
import com.echobound.items.EquipmentManager;
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

    // Projectile Object Pool
    public final ObjectPool<SpellProjectile> spellPool;
    public final Map<Integer, Integer> playerInventory = new HashMap<>();

    public UnifiedGameContext() {
        this.world = new SandboxWorld(42L);
        this.player = new PlayerSandboxEntity(0, 0, 10);
        this.dayNightCycle = new DayNightCycle();
        this.soundEngine = new SoundEngine();

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
    }

    public void update(float dt) {
        // 1. Day / Night Cycle
        dayNightCycle.update(dt);

        // 2. Traversal speed modifications from Mount, Cooking, and Equipment
        float baseSpeed = 160.0f * cookingManager.getSpeedMultiplier() * equipmentManager.getDashMultiplier();
        float effectiveSpeed = mountManager.getActiveSpeed(baseSpeed);
        player.setMaxSpeed(effectiveSpeed);

        // 3. Update Player & World
        player.update(dt, world);

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

        // 8. Story Progression & Post-Game unlocks
        postGameEngine.checkStoryProgression(storyEngine);

        // 9. Fishing & Puzzles
        fishingEngine.update(dt);
        puzzleManager.update(player.getPosition(), null);
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

        AABB3D attackArea = new AABB3D(
            attackCenter.x - 1.5f, attackCenter.y - 1.5f, attackCenter.z - 1.0f,
            attackCenter.x + 1.5f, attackCenter.y + 1.5f, attackCenter.z + 2.0f
        );
        mobManager.applyDamageArea(attackArea, damage, player.getPosition(), playerInventory);
        soundEngine.play(SoundType.MINE_BLOCK);
    }
}
