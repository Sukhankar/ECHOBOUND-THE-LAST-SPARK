package com.echobound.quest;

import com.echobound.faction.FactionManager;
import com.echobound.faction.FactionType;
import com.echobound.items.ItemRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestManager {
    private final Map<String, QuestDefinition> quests = new HashMap<>();

    public QuestManager() {
        populateDefaultQuests();
    }

    private void populateDefaultQuests() {
        // 1. Tier 1: Visible (Story)
        register(new QuestDefinition(
            "QUEST_STORY_01", "Awaken the Spire",
            "Collect 5 Spark Shards and reignite the local resonance tower beacon",
            QuestTier.VISIBLE, FactionType.SPARK_KEEPERS, 25,
            ItemRegistry.RUNE_SHARP, 1, 5
        ));

        // 2. Tier 2: Discoverable (Side)
        register(new QuestDefinition(
            "QUEST_SIDE_FORGE", "Apprentice's First Ingot",
            "Bring 10 Stone blocks to Master Blacksmith Kael to fuel the foundry",
            QuestTier.DISCOVERABLE, FactionType.CLOCKWORK_GUILD, 20,
            ItemRegistry.WEAPON_IRON_SWORD, 1, 10
        ));

        // 3. Tier 3: Hidden (Triggered by exploration)
        register(new QuestDefinition(
            "QUEST_HIDDEN_SHADOW_CAVE", "Whispers in the Dark",
            "Discover the submerged obsidian cavern located beneath the bedrock layer",
            QuestTier.HIDDEN, FactionType.VOID_OUTCASTS, 35,
            ItemRegistry.POTION_SPARK, 2, 1
        ));

        // 4. Tier 4: Secret Chain (Cryptic multi-stage lore quest)
        register(new QuestDefinition(
            "QUEST_SECRET_FIRST_ECHO", "The Harmonist's Legacy",
            "Uncover 3 Harmonic Tablets hidden within ruins throughout the world",
            QuestTier.SECRET_CHAIN, FactionType.SPARK_KEEPERS, 50,
            ItemRegistry.RELIC_ECHOES, 1, 3
        ));
    }

    public void register(QuestDefinition quest) {
        quests.put(quest.id, quest);
    }

    public QuestDefinition get(String id) {
        return quests.get(id);
    }

    public List<QuestDefinition> getQuestsByTier(QuestTier tier) {
        List<QuestDefinition> result = new ArrayList<>();
        for (QuestDefinition q : quests.values()) {
            if (q.tier == tier) {
                result.add(q);
            }
        }
        return result;
    }

    public boolean startQuest(String id) {
        QuestDefinition q = quests.get(id);
        if (q != null && (q.status == QuestStatus.LOCKED || q.status == QuestStatus.AVAILABLE)) {
            q.status = QuestStatus.IN_PROGRESS;
            return true;
        }
        return false;
    }

    public boolean advanceQuest(String id, int amount, FactionManager factionManager, Map<Integer, Integer> inventory) {
        QuestDefinition q = quests.get(id);
        if (q != null && q.status == QuestStatus.IN_PROGRESS) {
            q.advance(amount);
            if (q.isComplete()) {
                // Award faction reputation
                if (factionManager != null && q.faction != null) {
                    factionManager.addReputation(q.faction, q.reputationReward);
                }
                // Award items
                if (inventory != null && q.rewardItemId > 0 && q.rewardItemCount > 0) {
                    int cur = inventory.getOrDefault(q.rewardItemId, 0);
                    inventory.put(q.rewardItemId, cur + q.rewardItemCount);
                }
                return true;
            }
        }
        return false;
    }

    public int getTotalQuestCount() {
        return quests.size();
    }
}
