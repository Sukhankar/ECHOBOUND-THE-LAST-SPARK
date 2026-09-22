package com.echobound.magic;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SpellCombiner {
    private static final Map<String, Spell> combinations = new HashMap<>();

    // Base Spells
    public static final Spell EMBER_BOLT = new Spell(1, "Ember Bolt", MagicSchool.EMBER, null, 12, 0.4f, 25, 260, "Launches a piercing fire projectile", new Color(255, 75, 30));
    public static final Spell TIDE_LANCE = new Spell(2, "Tide Lance", MagicSchool.TIDE, null, 14, 0.5f, 22, 240, "Launches a freezing water spear", new Color(40, 160, 240));
    public static final Spell GALE_STEP = new Spell(3, "Gale Step", MagicSchool.GALE, null, 10, 0.3f, 10, 380, "High-velocity wind dash through enemies", new Color(180, 240, 200));
    public static final Spell STONE_WALL = new Spell(4, "Stone Wall", MagicSchool.TERRA, null, 18, 1.2f, 15, 0, "Erects a temporary protective earthen wall", new Color(170, 120, 70));
    public static final Spell VOLT_CHAIN = new Spell(5, "Volt Chain", MagicSchool.VOLT, null, 16, 0.6f, 30, 280, "Chains electrical discharge across targets", new Color(255, 230, 40));
    public static final Spell BLOOM_HEAL = new Spell(6, "Bloom Heal", MagicSchool.BLOOM, null, 25, 2.0f, -4, 0, "Restores 2 full hearts via organic pollen", new Color(70, 210, 90));
    public static final Spell VOID_BLINK = new Spell(7, "Void Blink", MagicSchool.VOID, null, 20, 1.0f, 0, 0, "Instantaneous short-range dimensional blink", new Color(170, 50, 230));
    public static final Spell ECHO_CLONE = new Spell(8, "Echo Clone", MagicSchool.ECHO, null, 30, 3.0f, 20, 0, "Spawns a duplicate phantom that repeats attacks", new Color(0, 240, 255));

    static {
        // Combined Spells
        registerCombo(MagicSchool.EMBER, MagicSchool.GALE,
            new Spell(101, "Fire Tornado", MagicSchool.EMBER, MagicSchool.GALE, 28, 1.2f, 55, 180,
                      "Swirling vortex of wind and flame dealing massive AoE", new Color(255, 130, 20)));

        registerCombo(MagicSchool.TIDE, MagicSchool.VOLT,
            new Spell(102, "Storm Burst", MagicSchool.TIDE, MagicSchool.VOLT, 32, 1.4f, 60, 250,
                      "Electrified torrential blast freezing and shocking foes", new Color(100, 200, 255)));

        registerCombo(MagicSchool.TERRA, MagicSchool.BLOOM,
            new Spell(103, "Thorn Fortress", MagicSchool.TERRA, MagicSchool.BLOOM, 30, 2.0f, 40, 0,
                      "Barricade of dense stone and poisonous thorns", new Color(110, 180, 70)));

        registerCombo(MagicSchool.VOID, MagicSchool.ECHO,
            new Spell(104, "Phantom Copy", MagicSchool.VOID, MagicSchool.ECHO, 35, 2.5f, 50, 0,
                      "Dimensional shadow duplicate that mimics all magic casts", new Color(120, 160, 255)));

        registerCombo(MagicSchool.EMBER, MagicSchool.TERRA,
            new Spell(105, "Magma Hammer", MagicSchool.EMBER, MagicSchool.TERRA, 34, 1.6f, 65, 140,
                      "Crushing seismic magma fissure creating molten earth", new Color(220, 80, 20)));

        registerCombo(MagicSchool.TIDE, MagicSchool.GALE,
            new Spell(106, "Blizzard Dash", MagicSchool.TIDE, MagicSchool.GALE, 26, 0.8f, 45, 360,
                      "Sub-zero frosty rush freezing all enemies in wake", new Color(180, 225, 255)));
    }

    private static String getComboKey(MagicSchool s1, MagicSchool s2) {
        if (s1.ordinal() < s2.ordinal()) {
            return s1.name() + "+" + s2.name();
        } else {
            return s2.name() + "+" + s1.name();
        }
    }

    private static void registerCombo(MagicSchool s1, MagicSchool s2, Spell spell) {
        combinations.put(getComboKey(s1, s2), spell);
    }

    public static Spell combine(MagicSchool s1, MagicSchool s2) {
        if (s1 == null || s2 == null) return null;
        if (s1 == s2) return getBaseSpellForSchool(s1);

        String key = getComboKey(s1, s2);
        return combinations.get(key);
    }

    public static Spell getBaseSpellForSchool(MagicSchool school) {
        return switch (school) {
            case EMBER -> EMBER_BOLT;
            case TIDE -> TIDE_LANCE;
            case GALE -> GALE_STEP;
            case TERRA -> STONE_WALL;
            case VOLT -> VOLT_CHAIN;
            case BLOOM -> BLOOM_HEAL;
            case VOID -> VOID_BLINK;
            case ECHO -> ECHO_CLONE;
        };
    }
}
