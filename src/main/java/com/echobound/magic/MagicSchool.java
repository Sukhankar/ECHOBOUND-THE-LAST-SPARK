package com.echobound.magic;

import java.awt.*;

public enum MagicSchool {
    EMBER("Ember", "Fire and intense heat", new Color(255, 75, 30)),
    TIDE("Tide", "Water and freezing ice", new Color(40, 160, 240)),
    GALE("Gale", "Wind, speed and movement", new Color(180, 240, 200)),
    TERRA("Terra", "Stone, seismic earth and stability", new Color(170, 120, 70)),
    VOLT("Volt", "Lightning, energy and currents", new Color(255, 230, 40)),
    BLOOM("Bloom", "Nature, organic healing and flora", new Color(70, 210, 90)),
    VOID("Void", "Dimensional shadow, phase and rifts", new Color(170, 50, 230)),
    ECHO("Echo", "Sound, duplication and resonance", new Color(0, 240, 255));

    public final String displayName;
    public final String domain;
    public final Color schoolColor;

    MagicSchool(String displayName, String domain, Color schoolColor) {
        this.displayName = displayName;
        this.domain = domain;
        this.schoolColor = schoolColor;
    }
}
