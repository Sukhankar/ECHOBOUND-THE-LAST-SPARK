package com.echobound.items;

public enum BackpackTier {
    STARTER("Starter Bag", 16),
    EXPLORER("Explorer Pack", 24),
    ADVENTURER("Adventurer Pack", 32),
    SKY("Sky Pack", 40),
    ECHO_VAULT("Echo Vault", 48);

    public final String displayName;
    public final int capacity;

    BackpackTier(String displayName, int capacity) {
        this.displayName = displayName;
        this.capacity = capacity;
    }

    public BackpackTier next() {
        BackpackTier[] tiers = values();
        int nextIdx = Math.min(tiers.length - 1, ordinal() + 1);
        return tiers[nextIdx];
    }
}
