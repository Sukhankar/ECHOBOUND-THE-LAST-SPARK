package com.echobound.companion;

import java.util.EnumSet;
import java.util.Set;

public class MountManager {
    private final Set<MountType> unlockedMounts = EnumSet.noneOf(MountType.class);
    private MountType activeMount = null;
    private boolean isMounted = false;

    public void unlockMount(MountType mount) {
        if (mount != null) {
            unlockedMounts.add(mount);
            if (activeMount == null) {
                activeMount = mount;
            }
        }
    }

    public boolean isUnlocked(MountType mount) {
        return unlockedMounts.contains(mount);
    }

    public void setActiveMount(MountType mount) {
        if (unlockedMounts.contains(mount)) {
            this.activeMount = mount;
        }
    }

    public MountType getActiveMount() {
        return activeMount;
    }

    public void mount() {
        if (activeMount != null) {
            isMounted = true;
        }
    }

    public void dismount() {
        isMounted = false;
    }

    public void toggleMount() {
        if (isMounted) {
            dismount();
        } else if (activeMount != null) {
            mount();
        }
    }

    public boolean isMounted() {
        return isMounted;
    }

    public float getActiveSpeed(float playerBaseSpeed) {
        if (isMounted && activeMount != null) {
            return activeMount.speed;
        }
        return playerBaseSpeed;
    }

    public int getUnlockedCount() {
        return unlockedMounts.size();
    }
}
