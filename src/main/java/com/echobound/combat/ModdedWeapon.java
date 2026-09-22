package com.echobound.combat;

import com.echobound.items.ItemDefinition;
import com.echobound.items.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

public class ModdedWeapon {
    public static final int MAX_SOCKETS = 3;

    public final int baseWeaponId;
    public final int baseDamage;
    private final RuneType[] sockets = new RuneType[MAX_SOCKETS];

    public ModdedWeapon(int baseWeaponId, int baseDamage) {
        this.baseWeaponId = baseWeaponId;
        this.baseDamage = baseDamage;
    }

    public boolean socketRune(int slotIndex, RuneType rune) {
        if (slotIndex >= 0 && slotIndex < MAX_SOCKETS) {
            sockets[slotIndex] = rune;
            return true;
        }
        return false;
    }

    public RuneType getRune(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < MAX_SOCKETS) {
            return sockets[slotIndex];
        }
        return null;
    }

    public void clearSocket(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < MAX_SOCKETS) {
            sockets[slotIndex] = null;
        }
    }

    public int calculateTotalDamage() {
        float multiplier = 1.0f;
        int flatDamage = 0;
        for (RuneType r : sockets) {
            if (r != null) {
                multiplier *= r.damageMult;
                flatDamage += r.fireDamage;
            }
        }
        return Math.round(baseDamage * multiplier) + flatDamage;
    }

    public boolean hasEchoDuplicate() {
        for (RuneType r : sockets) {
            if (r == RuneType.ECHO) return true;
        }
        return false;
    }

    public boolean hasFireElement() {
        for (RuneType r : sockets) {
            if (r == RuneType.FLAME) return true;
        }
        return false;
    }

    public boolean hasIceElement() {
        for (RuneType r : sockets) {
            if (r == RuneType.FROST) return true;
        }
        return false;
    }

    public boolean hasLightningElement() {
        for (RuneType r : sockets) {
            if (r == RuneType.VOLT) return true;
        }
        return false;
    }

    public String getDynamicName() {
        ItemDefinition baseDef = ItemRegistry.get(baseWeaponId);
        String baseName = (baseDef != null) ? baseDef.name : "Weapon";

        List<String> prefixes = new ArrayList<>();
        for (RuneType r : sockets) {
            if (r != null && !prefixes.contains(r.prefix)) {
                prefixes.add(r.prefix);
            }
        }

        if (prefixes.isEmpty()) {
            return baseName;
        }

        StringBuilder sb = new StringBuilder();
        for (String p : prefixes) {
            sb.append(p).append(" ");
        }
        sb.append(baseName);
        return sb.toString();
    }
}
