package com.echobound.museum;

public enum MuseumWing {
    RELICS("Relics Wing", 10, "Ancient devices, spark cores, and resonance tablets"),
    FOSSILS("Fossils Wing", 10, "Petrified bones and primordial titan impressions"),
    MINERALS("Minerals Wing", 10, "Luminous geodes, star ores, and crystalline clusters"),
    FLORA("Flora Wing", 10, "Exotic botanicals, rare seeds, and mutated blossoms");

    public final String displayName;
    public final int maxItems;
    public final String description;

    MuseumWing(String displayName, int maxItems, String description) {
        this.displayName = displayName;
        this.maxItems = maxItems;
        this.description = description;
    }
}
