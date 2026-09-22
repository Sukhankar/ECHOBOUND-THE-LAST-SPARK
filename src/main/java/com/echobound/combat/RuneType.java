package com.echobound.combat;

public enum RuneType {
    SHARP("Sharp", 1.30f, 0, 0, false, "Physical damage boost"),
    FLAME("Flame", 1.10f, 12, 0, false, "Burns target over 3s"),
    FROST("Frost", 1.05f, 0, 10, false, "Slows enemy movement by 40%"),
    VOLT("Volt", 1.15f, 0, 0, false, "Chains lightning to 2 nearby enemies"),
    ECHO("Echo", 1.20f, 0, 0, true, "Phantom clone repeats attack"),
    VOID("Void", 1.25f, 0, 0, false, "Ignores 50% enemy armor");

    public final String prefix;
    public final float damageMult;
    public final int fireDamage;
    public final int iceSlowDuration;
    public final boolean duplicateAttack;
    public final String description;

    RuneType(String prefix, float damageMult, int fireDamage,
             int iceSlowDuration, boolean duplicateAttack, String description) {
        this.prefix = prefix;
        this.damageMult = damageMult;
        this.fireDamage = fireDamage;
        this.iceSlowDuration = iceSlowDuration;
        this.duplicateAttack = duplicateAttack;
        this.description = description;
    }
}
