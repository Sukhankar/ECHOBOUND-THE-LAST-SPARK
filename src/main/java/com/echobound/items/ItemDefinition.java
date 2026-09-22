package com.echobound.items;

import java.awt.*;

public class ItemDefinition {
    public final int id;
    public final String name;
    public final ItemCategory category;
    public final int maxStack;
    public final int baseValue;
    public final String description;
    public final Color iconColor;

    public ItemDefinition(int id, String name, ItemCategory category, int maxStack,
                          int baseValue, String description, Color iconColor) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.maxStack = maxStack;
        this.baseValue = baseValue;
        this.description = description;
        this.iconColor = iconColor;
    }
}
