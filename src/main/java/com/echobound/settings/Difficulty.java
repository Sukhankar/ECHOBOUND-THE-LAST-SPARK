package com.echobound.settings;

public enum Difficulty {
    EASY("Easy", 0.5f, 3.0f),
    NORMAL("Normal", 1.0f, 6.0f),
    HARD("Hard", 1.5f, 10.0f);

    public final String label;
    /** Scales the damage enemies deal to the player. */
    public final float damageMultiplier;
    /** Seconds without taking damage before the player starts to heal. */
    public final float regenDelaySeconds;

    Difficulty(String label, float damageMultiplier, float regenDelaySeconds) {
        this.label = label;
        this.damageMultiplier = damageMultiplier;
        this.regenDelaySeconds = regenDelaySeconds;
    }

    public Difficulty cycle(int direction) {
        Difficulty[] all = values();
        return all[(ordinal() + direction + all.length) % all.length];
    }
}
