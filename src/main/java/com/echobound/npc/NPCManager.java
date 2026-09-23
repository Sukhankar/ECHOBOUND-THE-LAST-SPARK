package com.echobound.npc;

import com.echobound.faction.FactionType;
import com.echobound.items.ItemRegistry;
import com.echobound.sandbox.DayNightCycle;
import com.echobound.sandbox.WeatherType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCManager {
    private final Map<Integer, NPCDefinition> npcs = new HashMap<>();

    public NPCManager() {
        populateDefaultNPCs();
    }

    private void populateDefaultNPCs() {
        // 1. Master Blacksmith Kael (Clockwork Guild)
        NPCDefinition kael = new NPCDefinition(1, "Master Blacksmith Kael", FactionType.CLOCKWORK_GUILD, 100, 100, 4);
        kael.setDialogue("GREETING", "Keep your sparks steady, traveler. The anvil waits for no one.");
        kael.setDialogue("WEATHER_RAIN", "The storm dampens the forge coals, but the gears keep turning.");
        // Schedule: Day = Forge (100, 100, 4), Night = Bunkhouse (110, 100, 4), Storm = Workshop (105, 100, 4)
        kael.addScheduleRule(new NPCScheduleRule(null, WeatherType.STORM, 105, 100, 4, "Tending indoor forge in storm"));
        kael.addScheduleRule(new NPCScheduleRule(TimePeriod.DAY, null, 100, 100, 4, "Forging resonant weapons"));
        kael.addScheduleRule(new NPCScheduleRule(TimePeriod.NIGHT, null, 110, 100, 4, "Sleeping in bunkhouse"));
        // Blacksmith's forge: weapons, armor, and combat runes.
        kael.addShopItem(ItemRegistry.WEAPON_IRON_SWORD, 55);
        kael.addShopItem(ItemRegistry.WEAPON_ARC_BOW, 100);
        kael.addShopItem(ItemRegistry.EQUIP_MINER_HELM, 65);
        kael.addShopItem(ItemRegistry.EQUIP_STORM_BOOTS, 105);
        kael.addShopItem(ItemRegistry.RUNE_SHARP, 32);
        kael.addShopItem(ItemRegistry.RUNE_FLAME, 48);
        register(kael);

        // 2. Elder Lyra (Spark Keepers)
        NPCDefinition lyra = new NPCDefinition(2, "Elder Lyra", FactionType.SPARK_KEEPERS, 250, 300, 8);
        lyra.setDialogue("GREETING", "Pip carries a heavy destiny. May the First Spark illuminate your path.");
        lyra.addScheduleRule(new NPCScheduleRule(null, WeatherType.RAIN, 250, 305, 8, "Praying under spire awning"));
        lyra.addScheduleRule(new NPCScheduleRule(TimePeriod.DAY, null, 250, 300, 8, "Chanting at the Spire altar"));
        lyra.addScheduleRule(new NPCScheduleRule(TimePeriod.NIGHT, null, 255, 300, 8, "Studying celestial starcharts"));
        // Spire Keeper: arcane runes and, at a steep price, an ancient relic.
        lyra.addShopItem(ItemRegistry.RUNE_ECHO, 62);
        lyra.addShopItem(ItemRegistry.RUNE_VOID, 62);
        lyra.addShopItem(ItemRegistry.RELIC_GRAVITY, 520);
        register(lyra);

        // 3. Botanist Rowan (Thorn Wardens)
        NPCDefinition rowan = new NPCDefinition(3, "Botanist Rowan", FactionType.THORN_WARDENS, 40, 80, 3);
        rowan.setDialogue("GREETING", "Listen to the soil. Seeds speak if you are quiet enough.");
        rowan.addScheduleRule(new NPCScheduleRule(TimePeriod.DAWN, null, 45, 80, 3, "Gathering morning dew"));
        rowan.addScheduleRule(new NPCScheduleRule(TimePeriod.DAY, null, 40, 80, 3, "Tending botanical greenhouse"));
        rowan.addScheduleRule(new NPCScheduleRule(TimePeriod.NIGHT, null, 35, 80, 3, "Resting in hollow oak cabin"));
        // Botanist's stall: food, tonics, and raw materials.
        rowan.addShopItem(ItemRegistry.FOOD_MUSHROOM_SOUP, 14);
        rowan.addShopItem(ItemRegistry.POTION_HEALTH, 22);
        rowan.addShopItem(ItemRegistry.POTION_SPARK, 27);
        rowan.addShopItem(ItemRegistry.MAT_WOOD, 2);
        register(rowan);
    }

    public void register(NPCDefinition npc) {
        npcs.put(npc.id, npc);
    }

    public NPCDefinition get(int id) {
        return npcs.get(id);
    }

    public List<NPCDefinition> getAll() {
        return new ArrayList<>(npcs.values());
    }

    public void update(DayNightCycle dayNightCycle) {
        if (dayNightCycle == null) return;
        TimePeriod period = TimePeriod.fromHour(dayNightCycle.getTimeOfDay());
        WeatherType weather = dayNightCycle.getWeather();

        for (NPCDefinition npc : npcs.values()) {
            npc.updateSchedule(period, weather);
        }
    }
}
