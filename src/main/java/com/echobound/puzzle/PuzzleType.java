package com.echobound.puzzle;

public enum PuzzleType {
    DUAL_PRESSURE_PLATES("Dual Pressure Plates", "Requires Echo Clone on plate A while Rin activates plate B"),
    ELEMENTAL_BRAZIER("Elemental Brazier", "Must be ignited by an Ember spell projectile"),
    RESONANCE_PILLAR("Resonance Pillar", "Harmonizes when an Echo frequency wave hits the core");

    public final String displayName;
    public final String hint;

    PuzzleType(String displayName, String hint) {
        this.displayName = displayName;
        this.hint = hint;
    }
}
