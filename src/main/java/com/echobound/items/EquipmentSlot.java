package com.echobound.items;

public enum EquipmentSlot {
    HEAD("Head"),
    BODY("Body"),
    GLOVES("Gloves"),
    BOOTS("Boots"),
    CHARM("Charm"),
    CORE("Core");

    public final String label;

    EquipmentSlot(String label) {
        this.label = label;
    }
}
