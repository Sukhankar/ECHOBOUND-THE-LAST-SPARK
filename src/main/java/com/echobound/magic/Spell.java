package com.echobound.magic;

import java.awt.*;

public class Spell {
    public final int id;
    public final String name;
    public final MagicSchool primarySchool;
    public final MagicSchool secondarySchool; // Null if single-school
    public final float sparkCost;
    public final float cooldown;
    public final int damage;
    public final float projectileSpeed;
    public final String effectDescription;
    public final Color spellColor;

    public Spell(int id, String name, MagicSchool primarySchool, MagicSchool secondarySchool,
                 float sparkCost, float cooldown, int damage, float projectileSpeed,
                 String effectDescription, Color spellColor) {
        this.id = id;
        this.name = name;
        this.primarySchool = primarySchool;
        this.secondarySchool = secondarySchool;
        this.sparkCost = sparkCost;
        this.cooldown = cooldown;
        this.damage = damage;
        this.projectileSpeed = projectileSpeed;
        this.effectDescription = effectDescription;
        this.spellColor = spellColor;
    }

    public boolean isCombined() {
        return secondarySchool != null;
    }
}
