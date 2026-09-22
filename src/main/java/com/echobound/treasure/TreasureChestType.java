package com.echobound.treasure;

import com.echobound.items.ItemRegistry;

public enum TreasureChestType {
    WOODEN_CRATE("Wooden Supply Crate", ItemRegistry.MAT_SPARK_SHARD, 5, "Surface ruinstash with explorer supplies"),
    IRON_STRONGBOX("Iron Strongbox", ItemRegistry.RUNE_SHARP, 1, "Reinforced subterranean chest with refined weapons and runes"),
    ANCIENT_VAULT("Ancient Relic Vault", ItemRegistry.MAT_ANCIENT_CHIP, 3, "Sealed pre-calamity vault housing relic chips"),
    CELESTIAL_GEODE("Celestial Geode", ItemRegistry.MAT_CRYSTAL_SHARD, 8, "Pristine crystallized node fallen from the upper spires");

    public final String displayName;
    public final int rewardItemId;
    public final int rewardCount;
    public final String lore;

    TreasureChestType(String displayName, int rewardItemId, int rewardCount, String lore) {
        this.displayName = displayName;
        this.rewardItemId = rewardItemId;
        this.rewardCount = rewardCount;
        this.lore = lore;
    }
}
