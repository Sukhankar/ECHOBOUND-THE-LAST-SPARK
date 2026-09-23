package com.echobound.sandbox;

import java.awt.*;

/**
 * A slow seasonal cycle layered on top of the existing day/night clock (see DayNightCycle) —
 * driven by the same dayCount that already exists, not a second parallel clock. Purely
 * atmospheric for now: a subtle color wash over the world (applied alongside the day/night
 * ambient overlay) and a label in the HUD, without touching already-generated terrain.
 */
public enum Season {
    SPRING("Spring", new Color(120, 220, 140, 12)),
    SUMMER("Summer", new Color(0, 0, 0, 0)), // baseline season — no wash
    AUTUMN("Autumn", new Color(230, 140, 40, 22)),
    WINTER("Winter", new Color(160, 200, 235, 26));

    public final String displayName;
    /** Low-alpha full-screen tint blended over the world to suggest the season. */
    public final Color ambientWash;

    Season(String displayName, Color ambientWash) {
        this.displayName = displayName;
        this.ambientWash = ambientWash;
    }
}
