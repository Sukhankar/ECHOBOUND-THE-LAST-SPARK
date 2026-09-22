package com.echobound.cooking;

public enum DishType {
    MUSHROOM_SOUP("Mushroom Soup", 16.0f, "Health regeneration (+1 HP every 2s)"),
    FIRE_PEPPER_STEW("Fire Pepper Stew", 60.0f, "Immunity to intense fire and heat"),
    CLOUDBERRY_CAKE("Cloudberry Cake", 45.0f, "Jump boost (+25% Z-velocity) and apex float"),
    THUNDER_FISH_CURRY("Thunder Fish Curry", 45.0f, "Melee strikes trigger chain lightning sparks"),
    EXPLORER_BREAD("Explorer Bread", 60.0f, "+30% movement speed"),
    ECHO_NOODLES("Echo Noodles", 60.0f, "+50% Spark energy regeneration rate");

    public final String displayName;
    public final float duration;
    public final String buffDescription;

    DishType(String displayName, float duration, String buffDescription) {
        this.displayName = displayName;
        this.duration = duration;
        this.buffDescription = buffDescription;
    }
}
