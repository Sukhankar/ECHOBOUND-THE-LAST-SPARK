package com.echobound.animation;

import com.echobound.npc.NPCDefinition;
import com.echobound.npc.TimePeriod;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class NPCVisualController {
    private final Map<Integer, AnimationController> npcAnimators = new HashMap<>();

    public void registerNPC(NPCDefinition npc, Map<AnimationState, Animation> animations) {
        if (npc == null || animations == null) return;
        AnimationController ctrl = new AnimationController();
        for (Map.Entry<AnimationState, Animation> entry : animations.entrySet()) {
            ctrl.registerAnimation(entry.getKey(), entry.getValue());
        }
        ctrl.setState(AnimationState.IDLE);
        npcAnimators.put(npc.id, ctrl);
    }

    public AnimationController getController(int npcId) {
        return npcAnimators.get(npcId);
    }

    public void update(NPCDefinition npc, float dt) {
        if (npc == null) return;
        AnimationController ctrl = npcAnimators.get(npc.id);
        if (ctrl == null) return;

        // Synchronize animation state with NPC schedule & activity
        AnimationState targetState = mapActivityToAnimation(npc.currentActivity);
        ctrl.setState(targetState);
        ctrl.update(dt);
    }

    public BufferedImage getCurrentFrame(int npcId) {
        AnimationController ctrl = npcAnimators.get(npcId);
        if (ctrl == null) return null;
        return ctrl.getCurrentFrame();
    }

    public BufferedImage getCurrentFrame(NPCDefinition npc) {
        if (npc == null) return null;
        return getCurrentFrame(npc.id);
    }

    public static AnimationState mapActivityToAnimation(String activity) {
        if (activity == null) return AnimationState.IDLE;
        String act = activity.toLowerCase();
        if (act.contains("sleep") || act.contains("rest")) {
            return AnimationState.SLEEP;
        } else if (act.contains("forg") || act.contains("tend") || act.contains("gather") ||
                   act.contains("chant") || act.contains("work") || act.contains("craft")) {
            return AnimationState.WORK;
        } else if (act.contains("stud") || act.contains("pray") || act.contains("sit") || act.contains("read")) {
            return AnimationState.SIT;
        } else if (act.contains("walk") || act.contains("patrol") || act.contains("travel")) {
            return AnimationState.WALK;
        }
        return AnimationState.IDLE;
    }
}

