package com.echobound.boss;

import com.echobound.sandbox.DayNightCycle;
import com.echobound.sandbox.WeatherType;

public class CorruptionSwarmManager {
    private int activeCorruptedEnemies = 0;
    private float swarmIntensity = 1.0f; // Multiplier scaling with days
    private boolean isSwarmActive = false;

    public void update(DayNightCycle cycle, int activeSparkBeacons) {
        if (cycle == null) return;

        boolean isNightTime = cycle.isNight();
        boolean isEclipse = cycle.getWeather() == WeatherType.ECLIPSE;

        if (isNightTime || isEclipse) {
            isSwarmActive = true;
            // Intensity scales with day count: 1.0 + (dayCount - 1) * 0.15
            swarmIntensity = 1.0f + Math.max(0, cycle.getDayCount() - 1) * 0.15f;

            // Base spawn count based on intensity, reduced by player-built spark beacons
            int targetCount = (int) (12 * swarmIntensity);
            targetCount = Math.max(2, targetCount - activeSparkBeacons * 4);
            activeCorruptedEnemies = targetCount;
        } else {
            // Daylight disperses swarm
            isSwarmActive = false;
            activeCorruptedEnemies = 0;
        }
    }

    public boolean isSwarmActive() {
        return isSwarmActive;
    }

    public int getActiveCorruptedEnemies() {
        return activeCorruptedEnemies;
    }

    public float getSwarmIntensity() {
        return swarmIntensity;
    }
}
