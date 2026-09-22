package com.echobound.npc;

import com.echobound.faction.FactionType;
import com.echobound.sandbox.WeatherType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCDefinition {
    public final int id;
    public final String name;
    public final FactionType faction;
    public float x;
    public float y;
    public float z;
    public String currentActivity = "Idling";

    private final List<NPCScheduleRule> scheduleRules = new ArrayList<>();
    private final Map<String, String> dialogue = new HashMap<>();

    public NPCDefinition(int id, String name, FactionType faction, float x, float y, float z) {
        this.id = id;
        this.name = name;
        this.faction = faction;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void addScheduleRule(NPCScheduleRule rule) {
        scheduleRules.add(rule);
    }

    public void setDialogue(String key, String text) {
        dialogue.put(key, text);
    }

    public String getDialogue(String key) {
        return dialogue.getOrDefault(key, "...");
    }

    public void updateSchedule(TimePeriod period, WeatherType weather) {
        // Priority 1: Specific weather overrides (e.g. storm shelter)
        for (NPCScheduleRule rule : scheduleRules) {
            if (rule.requiredWeather == weather && rule.matches(period, weather)) {
                applyRule(rule);
                return;
            }
        }
        // Priority 2: General time period rules
        for (NPCScheduleRule rule : scheduleRules) {
            if (rule.requiredWeather == null && rule.matches(period, weather)) {
                applyRule(rule);
                return;
            }
        }
    }

    private void applyRule(NPCScheduleRule rule) {
        this.x = rule.targetX;
        this.y = rule.targetY;
        this.z = rule.targetZ;
        this.currentActivity = rule.activityDescription;
    }
}
