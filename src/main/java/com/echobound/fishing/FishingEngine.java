package com.echobound.fishing;

import java.util.Map;
import java.util.Random;

public class FishingEngine {
    public enum FishingState {
        IDLE,
        WAITING_FOR_BITE,
        BITE_ALERT,
        REELING_MINIGAME,
        CAUGHT,
        ESCAPED
    }

    private FishingState state = FishingState.IDLE;
    private float timer = 0.0f;
    private float biteWindowTimer = 0.0f;
    private float reelProgress = 0.0f; // 0.0 to 2.0s required
    private float rodTension = 0.5f;   // 0.0 to 1.0 (sweet spot 0.35 to 0.75)
    private FishSpecies hookedFish = null;
    private final Random rng = new Random();

    public boolean castLine(boolean isNearWater) {
        if (!isNearWater || state != FishingState.IDLE) {
            return false;
        }
        state = FishingState.WAITING_FOR_BITE;
        timer = 2.0f + rng.nextFloat() * 3.0f; // 2-5 seconds wait
        hookedFish = selectRandomFish();
        return true;
    }

    public void update(float dt) {
        switch (state) {
            case WAITING_FOR_BITE -> {
                timer -= dt;
                if (timer <= 0) {
                    state = FishingState.BITE_ALERT;
                    biteWindowTimer = 1.2f; // 1.2s reaction window
                }
            }
            case BITE_ALERT -> {
                biteWindowTimer -= dt;
                if (biteWindowTimer <= 0) {
                    state = FishingState.ESCAPED;
                }
            }
            case REELING_MINIGAME -> {
                // Tension drifts naturally
                rodTension += (rng.nextFloat() * 0.4f - 0.2f) * dt;
                rodTension = Math.max(0.0f, Math.min(1.0f, rodTension));

                // In sweet spot (0.35 to 0.75)?
                if (rodTension >= 0.35f && rodTension <= 0.75f) {
                    reelProgress += dt;
                    if (reelProgress >= 1.5f) {
                        state = FishingState.CAUGHT;
                    }
                } else {
                    reelProgress = Math.max(0.0f, reelProgress - dt * 0.5f);
                    // Snap line if tension reaches extreme
                    if (rodTension <= 0.05f || rodTension >= 0.95f) {
                        state = FishingState.ESCAPED;
                    }
                }
            }
            default -> {}
        }
    }

    public boolean hookBite() {
        if (state == FishingState.BITE_ALERT) {
            state = FishingState.REELING_MINIGAME;
            reelProgress = 0.0f;
            rodTension = 0.5f;
            return true;
        }
        return false;
    }

    public void reelAdjust(float delta) {
        if (state == FishingState.REELING_MINIGAME) {
            rodTension = Math.max(0.0f, Math.min(1.0f, rodTension + delta));
        }
    }

    public FishSpecies claimCatch(Map<String, Integer> inventory) {
        if (state == FishingState.CAUGHT && hookedFish != null) {
            FishSpecies caught = hookedFish;
            if (inventory != null) {
                inventory.put(caught.name(), inventory.getOrDefault(caught.name(), 0) + 1);
            }
            state = FishingState.IDLE;
            hookedFish = null;
            return caught;
        }
        return null;
    }

    public void reset() {
        state = FishingState.IDLE;
        hookedFish = null;
        timer = 0;
        reelProgress = 0;
    }

    private FishSpecies selectRandomFish() {
        float roll = rng.nextFloat();
        if (roll < 0.01f) return FishSpecies.VOID_RAY;
        if (roll < 0.05f) return FishSpecies.GOLDEN_KOI;
        if (roll < 0.15f) return FishSpecies.THUNDER_BASS;
        if (roll < 0.35f) return FishSpecies.ABYSSAL_GUPPY;
        if (roll < 0.60f) return FishSpecies.SHIMMERING_EEL;
        return FishSpecies.TIDE_MINNOW;
    }

    public FishingState getState() { return state; }
    public FishSpecies getHookedFish() { return hookedFish; }
    public float getRodTension() { return rodTension; }
    public float getReelProgress() { return reelProgress; }
}
