package com.echobound.audio;

public enum SoundType {
    JUMP(120, "Rising square wave chirp"),
    DOUBLE_JUMP(150, "Harmonic high-frequency leap"),
    DASH(100, "White noise friction whoosh"),
    MINE_BLOCK(80, "Percussive block strike"),
    PLACE_BLOCK(70, "Solid voxel placement thud"),
    CAST_SPELL(180, "Elemental frequency-modulated blast"),
    FIRE_TORNADO(350, "Rumbling molten vortex"),
    STORM_BURST(250, "Lightning crackle and thunder snap"),
    CRAFT_SUCCESS(300, "Upward 4-note melodic arpeggio"),
    QUEST_COMPLETE(450, "Triumphant fanfare chord"),
    CORRUPTION_ALARM(500, "Ominous descending nocturnal warning"),
    BOSS_ROAR(600, "Heavy sub-bass resonant growl"),
    ECHO_RECORD(100, "Spark synchronization ping"),
    ECHO_REPLAY(120, "Temporal rewind shimmer"),
    PLAYER_HURT(220, "Descending pained buzz"),
    ENEMY_HIT(90, "Sharp impact crack");

    public final int durationMs;
    public final String description;

    SoundType(int durationMs, String description) {
        this.durationMs = durationMs;
        this.description = description;
    }
}
