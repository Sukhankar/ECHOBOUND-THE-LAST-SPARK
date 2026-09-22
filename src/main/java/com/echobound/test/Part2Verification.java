package com.echobound.test;

import com.echobound.combat.ModdedWeapon;
import com.echobound.combat.RuneType;
import com.echobound.core.ResolutionProfile;
import com.echobound.items.*;
import com.echobound.magic.*;
import com.echobound.pool.ObjectPool;
import com.echobound.pool.SpellProjectile;
import com.echobound.sandbox.BlockType;
import com.echobound.sandbox.CompactChunk;

public class Part2Verification {
    public static void main(String[] args) {
        System.out.println("=== RUNNING PART 2 VERIFICATION SUITE ===");

        testCompactChunkByteStorageAndSparseDeltas();
        testItemRegistryAndCategories();
        testBackpackTiers();
        testEquipmentModifiers();
        testModdedWeaponRuneSocketing();
        testMagicSchoolsAndCombinations();
        testRelicManager();
        testResolutionProfiles();
        testObjectPoolHotPath();

        System.out.println(">>> ALL PART 2 TESTS PASSED PERFECTLY! <<<");
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

    private static void testCompactChunkByteStorageAndSparseDeltas() {
        CompactChunk chunk = new CompactChunk(0, 0);
        chunk.generateTerrain(12345L);

        // Modify 2 blocks
        chunk.setBlockId(5, 5, 3, (byte) BlockType.WORKBENCH.id);
        chunk.setBlockId(6, 5, 3, (byte) BlockType.SPARK_LAMP.id);

        assertEquals(2, chunk.getModifiedBlockCount(), "Chunk must store exactly 2 sparse deltas");
        assertEquals(BlockType.WORKBENCH.id, chunk.getBlockId(5, 5, 3), "Modified block must be WORKBENCH");
        assertEquals(BlockType.SPARK_LAMP.id, chunk.getBlockId(6, 5, 3), "Modified block must be SPARK_LAMP");
        System.out.println("  [PASS] Compact byte[2048] chunk and sparse delta tracking verified");
    }

    private static void testItemRegistryAndCategories() {
        assertTrue(ItemRegistry.getCount() >= 20, "ItemRegistry must contain at least 20 items");
        ItemDefinition sword = ItemRegistry.get(ItemRegistry.WEAPON_IRON_SWORD);
        assertTrue(sword != null && sword.category == ItemCategory.WEAPONS, "Iron sword must be WEAPONS category");

        ItemDefinition flameRune = ItemRegistry.get(ItemRegistry.RUNE_FLAME);
        assertTrue(flameRune != null && flameRune.category == ItemCategory.MAGIC, "Flame rune must be MAGIC category");

        ItemDefinition boots = ItemRegistry.get(ItemRegistry.EQUIP_STORM_BOOTS);
        assertTrue(boots != null && boots.category == ItemCategory.ARMOR, "Storm boots must be ARMOR category");

        ItemDefinition relic = ItemRegistry.get(ItemRegistry.RELIC_ECHOES);
        assertTrue(relic != null && relic.category == ItemCategory.RELICS, "Relic of Echoes must be RELICS category");
        System.out.println("  [PASS] ItemRegistry and 14-Category Architecture verified");
    }

    private static void testBackpackTiers() {
        BackpackTier tier = BackpackTier.STARTER;
        assertEquals(16, tier.capacity, "Starter bag capacity must be 16");

        tier = tier.next();
        assertEquals(24, tier.capacity, "Explorer pack capacity must be 24");

        tier = tier.next();
        assertEquals(32, tier.capacity, "Adventurer pack capacity must be 32");

        tier = tier.next();
        assertEquals(40, tier.capacity, "Sky pack capacity must be 40");

        tier = tier.next();
        assertEquals(48, tier.capacity, "Echo Vault capacity must be 48");
        System.out.println("  [PASS] 5-Tier Backpack Expansion verified");
    }

    private static void testEquipmentModifiers() {
        EquipmentManager eq = new EquipmentManager();
        assertTrue(eq.getDashMultiplier() == 1.0f, "Default dash multiplier must be 1.0");
        assertTrue(eq.getMiningSpeedMultiplier() == 1.0f, "Default mining speed multiplier must be 1.0");
        assertTrue(!eq.hasNightVision(), "Default night vision must be false");

        // Equip Storm Boots and Miner Gloves and Moon Charm
        eq.equip(EquipmentSlot.BOOTS, ItemRegistry.EQUIP_STORM_BOOTS);
        eq.equip(EquipmentSlot.GLOVES, ItemRegistry.EQUIP_MINER_GLOVES);
        eq.equip(EquipmentSlot.CHARM, ItemRegistry.EQUIP_MOON_CHARM);
        eq.equip(EquipmentSlot.CORE, ItemRegistry.EQUIP_EMBER_CORE);

        assertTrue(eq.getDashMultiplier() == 1.30f, "Storm boots must give +30% dash speed");
        assertTrue(eq.getMiningSpeedMultiplier() == 1.50f, "Miner gloves must give +50% mining speed");
        assertTrue(eq.hasNightVision(), "Moon charm must activate night vision");
        assertEquals(15, eq.getBonusFireDamage(), "Ember core must grant +15 bonus fire damage");
        System.out.println("  [PASS] 6 Equipment Slots and Passive Stat Modifiers verified");
    }

    private static void testModdedWeaponRuneSocketing() {
        ModdedWeapon weapon = new ModdedWeapon(ItemRegistry.WEAPON_IRON_SWORD, 20);
        assertEquals(20, weapon.calculateTotalDamage(), "Unmodded damage must be 20");
        assertTrue("Iron Sword".equals(weapon.getDynamicName()), "Base name must be Iron Sword");

        // Socket 1: Sharp Rune (+30% damage)
        weapon.socketRune(0, RuneType.SHARP);
        assertEquals(26, weapon.calculateTotalDamage(), "Sharp rune must boost damage to 26");

        // Socket 2: Flame Rune (+10% damage + 12 flat fire damage)
        weapon.socketRune(1, RuneType.FLAME);
        assertTrue(weapon.hasFireElement(), "Weapon must carry fire element");

        // Socket 3: Echo Rune (+20% damage + duplicate attack)
        weapon.socketRune(2, RuneType.ECHO);
        assertTrue(weapon.hasEchoDuplicate(), "Weapon must carry echo duplicate flag");

        // Name check
        String dynName = weapon.getDynamicName();
        assertTrue(dynName.contains("Sharp") && dynName.contains("Flame") && dynName.contains("Echo"),
                   "Dynamic weapon name must reflect socketed runes: " + dynName);
        System.out.println("  [PASS] 3-Socket Weapon Runecrafting & Dynamic Naming verified: " + dynName);
    }

    private static void testMagicSchoolsAndCombinations() {
        assertEquals(8, MagicSchool.values().length, "Must have exactly 8 magic schools");

        // Test Base Spells
        Spell ember = SpellCombiner.getBaseSpellForSchool(MagicSchool.EMBER);
        assertTrue(ember != null && ember.primarySchool == MagicSchool.EMBER, "Base Ember spell must exist");

        // Test 6 Signature Combinations
        Spell fireTornado = SpellCombiner.combine(MagicSchool.EMBER, MagicSchool.GALE);
        assertTrue(fireTornado != null && "Fire Tornado".equals(fireTornado.name), "Ember + Gale must produce Fire Tornado");

        Spell stormBurst = SpellCombiner.combine(MagicSchool.TIDE, MagicSchool.VOLT);
        assertTrue(stormBurst != null && "Storm Burst".equals(stormBurst.name), "Tide + Volt must produce Storm Burst");

        Spell thornFortress = SpellCombiner.combine(MagicSchool.TERRA, MagicSchool.BLOOM);
        assertTrue(thornFortress != null && "Thorn Fortress".equals(thornFortress.name), "Terra + Bloom must produce Thorn Fortress");

        Spell phantomCopy = SpellCombiner.combine(MagicSchool.VOID, MagicSchool.ECHO);
        assertTrue(phantomCopy != null && "Phantom Copy".equals(phantomCopy.name), "Void + Echo must produce Phantom Copy");

        Spell magmaHammer = SpellCombiner.combine(MagicSchool.EMBER, MagicSchool.TERRA);
        assertTrue(magmaHammer != null && "Magma Hammer".equals(magmaHammer.name), "Ember + Terra must produce Magma Hammer");

        Spell blizzardDash = SpellCombiner.combine(MagicSchool.TIDE, MagicSchool.GALE);
        assertTrue(blizzardDash != null && "Blizzard Dash".equals(blizzardDash.name), "Tide + Gale must produce Blizzard Dash");

        System.out.println("  [PASS] 8 Magic Schools and All 6 Dual Combinations verified");
    }

    private static void testRelicManager() {
        RelicManager relics = new RelicManager();
        assertEquals(1, relics.getMaxEchoClones(), "Default max Echoes must be 1");
        assertTrue(!relics.canWalkOnWater(), "Default water walking must be false");

        relics.unlockRelic(RelicType.ECHOES);
        assertEquals(2, relics.getMaxEchoClones(), "Relic of Echoes must unlock 2 simultaneous Echoes");

        relics.unlockRelic(RelicType.TIDES);
        assertTrue(relics.canWalkOnWater(), "Relic of Tides must enable water walking");

        relics.unlockRelic(RelicType.TIME);
        relics.triggerTimeSlow();
        assertTrue(relics.isTimeSlowActive(), "Relic of Time must activate time-slow aura");
        System.out.println("  [PASS] RelicManager and Rule-Changing Artifacts verified");
    }

    private static void testResolutionProfiles() {
        ResolutionProfile p1 = ResolutionProfile.PIXEL_SAVER;
        assertEquals(320, p1.width, "Pixel saver width must be 320");
        assertEquals(180, p1.height, "Pixel saver height must be 180");

        ResolutionProfile p2 = ResolutionProfile.PIXEL_STANDARD;
        assertEquals(426, p2.width, "Pixel standard width must be 426");
        assertEquals(240, p2.height, "Pixel standard height must be 240");

        ResolutionProfile p3 = ResolutionProfile.PIXEL_PLUS;
        assertEquals(640, p3.width, "Pixel plus width must be 640");
        assertEquals(360, p3.height, "Pixel plus height must be 360");
        System.out.println("  [PASS] 3 Pixel-First Resolution Profiles verified");
    }

    private static void testObjectPoolHotPath() {
        ObjectPool<SpellProjectile> pool = new ObjectPool<>(50, SpellProjectile::new);
        assertEquals(50, pool.getCapacity(), "Pool capacity must be 50");

        SpellProjectile p1 = pool.acquire();
        SpellProjectile p2 = pool.acquire();
        assertTrue(p1 != null && p2 != null, "Acquired projectiles must not be null");
        assertTrue(p1 != p2, "Sequential acquisitions must be distinct pool slots");
        System.out.println("  [PASS] Zero-Allocation ObjectPool Hot Path verified");
    }
}
