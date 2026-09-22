package com.echobound.ui.menu;

public class LoadingScreen {
    private static final String[] LOADING_HINTS = {
        "Awakening Pip and calibrating Spark resonance...",
        "Generating procedural voxel heightmaps and terrain layers...",
        "Pre-caching chiptune audio waveforms into memory...",
        "Synchronizing faction alignments and trade sanctions...",
        "Checking ancient ruins and subterranean geode veins...",
        "World synchronization complete. Ready to emerge!"
    };

    private float progress = 0.0f; // 0.0 to 1.0
    private int currentHintIndex = 0;
    private boolean isFinished = false;

    public void update(float dt) {
        if (isFinished) return;

        // Progress advances smoothly
        progress += dt * 0.85f;
        if (progress >= 1.0f) {
            progress = 1.0f;
            isFinished = true;
            currentHintIndex = LOADING_HINTS.length - 1;
        } else {
            currentHintIndex = Math.min(LOADING_HINTS.length - 2,
                                        (int) (progress * (LOADING_HINTS.length - 1)));
        }
    }

    public float getProgress() {
        return progress;
    }

    public String getCurrentHint() {
        return LOADING_HINTS[currentHintIndex];
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void reset() {
        progress = 0.0f;
        currentHintIndex = 0;
        isFinished = false;
    }
}
