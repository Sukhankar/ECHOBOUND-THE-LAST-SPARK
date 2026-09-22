package com.echobound.entity.mob;

import com.echobound.building.StructureManager;
import com.echobound.physics3d.AABB3D;
import com.echobound.physics3d.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MobManager {
    private final List<MobEntity> activeMobs = new ArrayList<>();
    private int nextMobId = 1;

    public MobEntity spawnMob(MobType type, float x, float y, float z) {
        MobEntity mob = new MobEntity(nextMobId++, type, x, y, z);
        activeMobs.add(mob);
        return mob;
    }

    public void update(float dt, Vec3 playerPos, StructureManager structureManager) {
        boolean playerInSafeZone = structureManager != null &&
            structureManager.isSafeFromCorruption(playerPos.x, playerPos.y, playerPos.z);

        for (MobEntity mob : activeMobs) {
            boolean mobInSafeZone = structureManager != null &&
                structureManager.isSafeFromCorruption(mob.position.x, mob.position.y, mob.position.z);

            mob.update(dt, playerPos, playerInSafeZone || mobInSafeZone);
        }
    }

    public int applyDamageArea(AABB3D attackArea, int damage, Vec3 attackSource, Map<Integer, Integer> inventory) {
        int hitCount = 0;
        Iterator<MobEntity> iter = activeMobs.iterator();

        while (iter.hasNext()) {
            MobEntity mob = iter.next();
            if (mob.isAlive && mob.hitbox.overlaps(attackArea)) {
                mob.takeDamage(damage, attackSource);
                hitCount++;

                if (!mob.isAlive) {
                    // Reward loot drops
                    if (inventory != null && mob.type.dropItemId > 0) {
                        int cur = inventory.getOrDefault(mob.type.dropItemId, 0);
                        inventory.put(mob.type.dropItemId, cur + mob.type.dropCount);
                    }
                    iter.remove();
                }
            }
        }
        return hitCount;
    }

    public int getActiveMobCount() {
        return activeMobs.size();
    }

    public List<MobEntity> getActiveMobs() {
        return activeMobs;
    }

    public void clear() {
        activeMobs.clear();
    }
}
