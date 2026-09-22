package com.echobound.sandbox;

import java.awt.*;

public class DayNightCycle {
    // 1 real second = 0.1 game hours (full 24h cycle in 240 seconds = 4 minutes)
    private static final float DEFAULT_TIME_SCALE = 0.1f;

    private float timeOfDay = 8.0f; // Start at 8:00 AM
    private int dayCount = 1;
    private float timeScale = DEFAULT_TIME_SCALE;
    private WeatherType weather = WeatherType.CLEAR;

    public void update(float dt) {
        timeOfDay += dt * timeScale;
        if (timeOfDay >= 24.0f) {
            timeOfDay -= 24.0f;
            dayCount++;
        }
    }

    public float getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(float timeOfDay) {
        this.timeOfDay = (timeOfDay % 24.0f + 24.0f) % 24.0f;
    }

    public int getDayCount() {
        return dayCount;
    }

    public WeatherType getWeather() {
        return weather;
    }

    public void setWeather(WeatherType weather) {
        this.weather = weather;
    }

    public void toggleWeather() {
        this.weather = this.weather.next();
    }

    public boolean isNight() {
        return timeOfDay < 5.0f || timeOfDay >= 20.0f;
    }

    /**
     * Daylight factor from 0.10 (dead of night) to 1.0 (bright noon).
     */
    public float getDaylightFactor() {
        float factor;
        if (timeOfDay >= 6.0f && timeOfDay <= 18.0f) {
            // Day curve
            double angle = (timeOfDay - 6.0f) / 12.0f * Math.PI;
            factor = (float) (0.25f + 0.75f * Math.sin(angle));
        } else {
            // Night curve
            factor = 0.18f;
        }
        return Math.max(0.12f, factor * weather.brightnessMult);
    }

    public Color getAmbientDarknessColor() {
        float light = getDaylightFactor();
        int alpha = (int) ((1.0f - light) * 205);
        if (weather == WeatherType.ECLIPSE) {
            return new Color(30, 0, 50, Math.min(235, alpha + 50));
        }
        return new Color(8, 12, 26, Math.max(0, Math.min(255, alpha)));
    }

    public String getFormattedTime() {
        int hours = (int) timeOfDay;
        int minutes = (int) ((timeOfDay - hours) * 60);
        return String.format("Day %d | %02d:%02d (%s)", dayCount, hours, minutes,
                             isNight() ? "Night" : "Day");
    }
}
