package com.echobound.npc;

import com.echobound.sandbox.WeatherType;

public class NPCScheduleRule {
    public final TimePeriod period;
    public final WeatherType requiredWeather; // null if applies to any weather
    public final float targetX;
    public final float targetY;
    public final float targetZ;
    public final String activityDescription;

    public NPCScheduleRule(TimePeriod period, WeatherType requiredWeather,
                           float targetX, float targetY, float targetZ,
                           String activityDescription) {
        this.period = period;
        this.requiredWeather = requiredWeather;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.activityDescription = activityDescription;
    }

    public boolean matches(TimePeriod currentPeriod, WeatherType currentWeather) {
        if (this.requiredWeather != null && this.requiredWeather != currentWeather) {
            return false;
        }
        return this.period == null || this.period == currentPeriod;
    }
}
