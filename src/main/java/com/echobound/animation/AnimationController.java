package com.echobound.animation;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

public class AnimationController {
    private final Map<AnimationState, Animation> animations = new EnumMap<>(AnimationState.class);
    private AnimationState currentState = AnimationState.IDLE;
    private float stateTime = 0.0f;
    private boolean facingLeft = false;
    private boolean isActionLocked = false;

    public void registerAnimation(AnimationState state, Animation animation) {
        animations.put(state, animation);
    }

    public Animation getAnimation(AnimationState state) {
        return animations.get(state);
    }

    public void setState(AnimationState newState) {
        setState(newState, false);
    }

    public void setState(AnimationState newState, boolean forceReset) {
        if (isActionLocked && newState != AnimationState.DEAD) {
            Animation curAnim = animations.get(currentState);
            if (curAnim != null && stateTime < curAnim.getTotalDuration()) {
                // Keep action locked until duration finishes
                return;
            }
            isActionLocked = false;
        }

        if (currentState != newState || forceReset) {
            currentState = newState;
            stateTime = 0.0f;
        }
    }

    public void triggerAction(AnimationState actionState) {
        if (animations.containsKey(actionState)) {
            currentState = actionState;
            stateTime = 0.0f;
            isActionLocked = true;
        }
    }

    public void update(float dt) {
        stateTime += dt;
        Animation curAnim = animations.get(currentState);
        if (isActionLocked && curAnim != null) {
            if (stateTime >= curAnim.getTotalDuration()) {
                isActionLocked = false;
                currentState = AnimationState.IDLE;
                stateTime = 0.0f;
            }
        }
    }

    public BufferedImage getCurrentFrame() {
        Animation anim = animations.get(currentState);
        if (anim == null) {
            anim = animations.get(AnimationState.IDLE);
        }
        if (anim == null) {
            return null;
        }
        return anim.getFrame(stateTime);
    }

    public AnimationState getCurrentState() {
        return currentState;
    }

    public float getStateTime() {
        return stateTime;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public void setFacingLeft(boolean facingLeft) {
        this.facingLeft = facingLeft;
    }

    public boolean isActionLocked() {
        return isActionLocked;
    }
}
