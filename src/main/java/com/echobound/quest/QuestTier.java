package com.echobound.quest;

public enum QuestTier {
    VISIBLE("Visible", "Prominent story markers and primary milestones"),
    DISCOVERABLE("Discoverable", "Side quests offered through NPC dialogues, town boards, and rumors"),
    HIDDEN("Hidden", "Triggered dynamically by interacting with secret structures, ruins, or specific biomes"),
    SECRET_CHAIN("Secret Chain", "Multi-stage esoteric investigations unlocking ancient secrets and ultimate powers");

    public final String displayName;
    public final String description;

    QuestTier(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
