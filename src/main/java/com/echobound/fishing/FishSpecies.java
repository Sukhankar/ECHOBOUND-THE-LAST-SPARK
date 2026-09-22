package com.echobound.fishing;

import com.echobound.items.ItemRegistry;

public enum FishSpecies {
    TIDE_MINNOW("Tide Minnow", 1, 0.40f, "Common shimmering coastal fish used in soups"),
    SHIMMERING_EEL("Shimmering Eel", 2, 0.25f, "Conductive river eel rich in spark minerals"),
    ABYSSAL_GUPPY("Abyssal Guppy", 2, 0.20f, "Bioluminescent cavern fish found in deep aquifers"),
    THUNDER_BASS("Thunder Bass", 3, 0.10f, "Electrified predator required for Thunder Fish Curry"),
    GOLDEN_KOI("Golden Koi", 4, 0.04f, "Legendary fortune fish with high museum donation value"),
    VOID_RAY("Void Ray", 5, 0.01f, "Mystical creature swimming through subterranean void streams");

    public final String displayName;
    public final int tier;
    public final float catchProbability;
    public final String description;

    FishSpecies(String displayName, int tier, float catchProbability, String description) {
        this.displayName = displayName;
        this.tier = tier;
        this.catchProbability = catchProbability;
        this.description = description;
    }
}
