package com.echobound.quest;

import com.echobound.faction.FactionType;

public class QuestDefinition {
    public final String id;
    public final String title;
    public final String description;
    public final QuestTier tier;
    public final FactionType faction;
    public final int reputationReward;
    public final int rewardItemId;
    public final int rewardItemCount;
    public final int targetProgress;

    public int currentProgress = 0;
    public QuestStatus status = QuestStatus.LOCKED;

    public QuestDefinition(String id, String title, String description, QuestTier tier,
                           FactionType faction, int reputationReward,
                           int rewardItemId, int rewardItemCount, int targetProgress) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tier = tier;
        this.faction = faction;
        this.reputationReward = reputationReward;
        this.rewardItemId = rewardItemId;
        this.rewardItemCount = rewardItemCount;
        this.targetProgress = targetProgress;
    }

    public void advance(int amount) {
        if (status == QuestStatus.IN_PROGRESS) {
            currentProgress = Math.min(targetProgress, currentProgress + amount);
            if (currentProgress >= targetProgress) {
                status = QuestStatus.COMPLETED;
            }
        }
    }

    public boolean isComplete() {
        return status == QuestStatus.COMPLETED;
    }
}
