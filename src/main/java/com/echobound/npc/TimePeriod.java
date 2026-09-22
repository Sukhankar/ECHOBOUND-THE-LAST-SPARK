package com.echobound.npc;

public enum TimePeriod {
    DAWN(5.0f, 8.0f),
    DAY(8.0f, 17.0f),
    DUSK(17.0f, 20.0f),
    NIGHT(20.0f, 5.0f);

    public final float startHour;
    public final float endHour;

    TimePeriod(float startHour, float endHour) {
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public static TimePeriod fromHour(float hour) {
        float h = (hour % 24.0f + 24.0f) % 24.0f;
        if (h >= 5.0f && h < 8.0f) return DAWN;
        if (h >= 8.0f && h < 17.0f) return DAY;
        if (h >= 17.0f && h < 20.0f) return DUSK;
        return NIGHT;
    }
}
