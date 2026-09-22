package com.echobound.sandbox;

import com.echobound.physics3d.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EchoSandboxClone {
    public static class ActionFrame {
        public boolean inLeft;
        public boolean inRight;
        public boolean inUp;
        public boolean inDown;
        public boolean jumpPressed;
        public boolean jumpHeld;
        public boolean dashPressed;
        public boolean mineHeld;
        public boolean placePressed;

        public ActionFrame(boolean inLeft, boolean inRight, boolean inUp, boolean inDown,
                           boolean jumpPressed, boolean jumpHeld, boolean dashPressed,
                           boolean mineHeld, boolean placePressed) {
            this.inLeft = inLeft;
            this.inRight = inRight;
            this.inUp = inUp;
            this.inDown = inDown;
            this.jumpPressed = jumpPressed;
            this.jumpHeld = jumpHeld;
            this.dashPressed = dashPressed;
            this.mineHeld = mineHeld;
            this.placePressed = placePressed;
        }
    }

    private final List<ActionFrame> recordedFrames = new ArrayList<>();
    private final Vec3 startPos = new Vec3();
    private PlayerSandboxEntity ghostEntity;

    public boolean isRecording = false;
    public boolean isActive = false;
    private int playbackIndex = 0;
    private float holdTimer = 0.0f;

    public void startRecording(PlayerSandboxEntity player) {
        recordedFrames.clear();
        startPos.set(player.pos);
        isRecording = true;
        isActive = false;
    }

    public void recordFrame(boolean inLeft, boolean inRight, boolean inUp, boolean inDown,
                            boolean jumpPressed, boolean jumpHeld, boolean dashPressed,
                            boolean mineHeld, boolean placePressed) {
        if (!isRecording) return;
        if (recordedFrames.size() < 360) { // Max 6 seconds
            recordedFrames.add(new ActionFrame(inLeft, inRight, inUp, inDown,
                                               jumpPressed, jumpHeld, dashPressed,
                                               mineHeld, placePressed));
        } else {
            isRecording = false;
        }
    }

    public void stopRecording() {
        isRecording = false;
    }

    public void deploy() {
        if (recordedFrames.isEmpty()) return;
        ghostEntity = new PlayerSandboxEntity(startPos.x, startPos.y, startPos.z);
        playbackIndex = 0;
        holdTimer = 3.0f;
        isActive = true;
        isRecording = false;
    }

    public void update(SandboxWorld world, float dt) {
        if (!isActive || ghostEntity == null) return;

        if (playbackIndex < recordedFrames.size()) {
            ActionFrame frame = recordedFrames.get(playbackIndex);
            ghostEntity.update(world, frame.inLeft, frame.inRight, frame.inUp, frame.inDown,
                               frame.jumpPressed, frame.jumpHeld, frame.dashPressed,
                               frame.mineHeld, frame.placePressed, dt);
            playbackIndex++;
        } else {
            // Hold pose for 3 seconds, then dissolve
            holdTimer -= dt;
            if (holdTimer <= 0) {
                isActive = false;
            }
        }
    }

    public PlayerSandboxEntity getGhostEntity() {
        return ghostEntity;
    }
}
