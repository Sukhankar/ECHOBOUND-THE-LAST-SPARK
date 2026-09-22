package com.echobound.sandbox;

public enum WeatherType {
    CLEAR("Clear Sun", 1.0f),
    RAIN("Rainfall", 0.75f),
    SNOW("Gentle Snow", 0.85f),
    STORM("Thunderstorm", 0.45f),
    ECLIPSE("Magical Eclipse", 0.25f);

    public final String displayName;
    public final float brightnessMult;

    WeatherType(String displayName, float brightnessMult) {
        this.displayName = displayName;
        this.brightnessMult = brightnessMult;
    }

    public WeatherType next() {
        WeatherType[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }
}
