package com.echobound.core;

public enum ResolutionProfile {
    PIXEL_SAVER("Pixel Saver (Low-End)", 320, 180, 20),
    PIXEL_STANDARD("Pixel Standard (Balanced)", 426, 240, 60),
    PIXEL_PLUS("Pixel Plus (Enhanced)", 640, 360, 150);

    public final String label;
    public final int width;
    public final int height;
    public final int maxParticles;

    ResolutionProfile(String label, int width, int height, int maxParticles) {
        this.label = label;
        this.width = width;
        this.height = height;
        this.maxParticles = maxParticles;
    }

    public ResolutionProfile next() {
        ResolutionProfile[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }
}
