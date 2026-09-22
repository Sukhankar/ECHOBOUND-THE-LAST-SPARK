package com.echobound.magic;

import java.util.EnumSet;
import java.util.Set;

public class RelicManager {
    private final Set<RelicType> unlockedRelics = EnumSet.noneOf(RelicType.class);
    private float timeSlowTimer = 0.0f;

    public void unlockRelic(RelicType relic) {
        if (relic != null) {
            unlockedRelics.add(relic);
        }
    }

    public boolean hasRelic(RelicType relic) {
        return unlockedRelics.contains(relic);
    }

    public int getMaxEchoClones() {
        return hasRelic(RelicType.ECHOES) ? 2 : 1;
    }

    public boolean canWalkOnWater() {
        return hasRelic(RelicType.TIDES);
    }

    public boolean canHoverGravity() {
        return hasRelic(RelicType.GRAVITY);
    }

    public boolean canTameBeasts() {
        return hasRelic(RelicType.BEAST);
    }

    public void triggerTimeSlow() {
        if (hasRelic(RelicType.TIME)) {
            timeSlowTimer = 3.0f;
        }
    }

    public void update(float dt) {
        if (timeSlowTimer > 0) {
            timeSlowTimer -= dt;
        }
    }

    public boolean isTimeSlowActive() {
        return timeSlowTimer > 0.0f;
    }

    public int getUnlockedCount() {
        return unlockedRelics.size();
    }
}
