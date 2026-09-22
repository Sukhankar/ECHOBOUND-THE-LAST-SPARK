package com.echobound.arcade;

import com.echobound.story.StoryProgressionEngine;

public class PostGameEngine {
    private boolean endlessRiftsUnlocked = false;
    private boolean masterDifficultyUnlocked = false;
    private boolean creativeSandboxUnlocked = false;
    private int completedRiftFloor = 0;

    public void checkStoryProgression(StoryProgressionEngine storyEngine) {
        if (storyEngine != null && storyEngine.getCompletedSubChapterCount() >= 500) {
            unlockPostGame();
        }
    }

    public void unlockPostGame() {
        this.endlessRiftsUnlocked = true;
        this.masterDifficultyUnlocked = true;
        this.creativeSandboxUnlocked = true;
    }

    public boolean isPostGameUnlocked() {
        return endlessRiftsUnlocked;
    }

    public boolean isCreativeSandboxUnlocked() {
        return creativeSandboxUnlocked;
    }

    public boolean isMasterDifficultyUnlocked() {
        return masterDifficultyUnlocked;
    }

    public void advanceRiftFloor() {
        if (endlessRiftsUnlocked) {
            completedRiftFloor++;
        }
    }

    public int getCompletedRiftFloor() {
        return completedRiftFloor;
    }
}
