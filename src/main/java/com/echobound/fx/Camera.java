package com.echobound.fx;

public class Camera {
    public static final int VIEW_WIDTH = 640;
    public static final int VIEW_HEIGHT = 360;

    private float x;
    private float y;
    private float targetX;
    private float targetY;

    // Lookahead
    private float lookaheadOffset = 0.0f;
    private static final float MAX_LOOKAHEAD = 36.0f;

    // Level bounds
    private float minX = 0;
    private float maxX = 1000;
    private float minY = 0;
    private float maxY = 600;

    private final ScreenShake screenShake;

    public Camera(ScreenShake screenShake) {
        this.screenShake = screenShake;
    }

    public void setBounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void snapTo(float focusX, float focusY) {
        this.targetX = focusX - VIEW_WIDTH * 0.5f;
        this.targetY = focusY - VIEW_HEIGHT * 0.5f;
        clampTarget();
        this.x = targetX;
        this.y = targetY;
    }

    public void update(float targetFocusX, float targetFocusY, int facing, float dt) {
        // Target lookahead based on facing
        float desiredLookahead = facing * MAX_LOOKAHEAD;
        lookaheadOffset += (desiredLookahead - lookaheadOffset) * Math.min(1.0f, dt * 5.0f);

        float desiredTargetX = targetFocusX + lookaheadOffset - VIEW_WIDTH * 0.5f;

        // Vertical dead-zone: only track Y if outside deadzone of 24 pixels
        float camCenterY = y + VIEW_HEIGHT * 0.5f;
        float dy = targetFocusY - camCenterY;
        float deadzone = 24.0f;
        if (Math.abs(dy) > deadzone) {
            targetY = (dy > 0) ? (targetFocusY - deadzone - VIEW_HEIGHT * 0.5f)
                              : (targetFocusY + deadzone - VIEW_HEIGHT * 0.5f);
        }

        targetX = desiredTargetX;
        clampTarget();

        // Smooth follow lerp
        float lerpSpeed = 8.0f;
        x += (targetX - x) * Math.min(1.0f, dt * lerpSpeed);
        y += (targetY - y) * Math.min(1.0f, dt * lerpSpeed);

        clampCamera();
    }

    private void clampTarget() {
        float effectiveMaxX = Math.max(minX, maxX - VIEW_WIDTH);
        float effectiveMaxY = Math.max(minY, maxY - VIEW_HEIGHT);
        if (targetX < minX) targetX = minX;
        if (targetX > effectiveMaxX) targetX = effectiveMaxX;
        if (targetY < minY) targetY = minY;
        if (targetY > effectiveMaxY) targetY = effectiveMaxY;
    }

    private void clampCamera() {
        float effectiveMaxX = Math.max(minX, maxX - VIEW_WIDTH);
        float effectiveMaxY = Math.max(minY, maxY - VIEW_HEIGHT);
        if (x < minX) x = minX;
        if (x > effectiveMaxX) x = effectiveMaxX;
        if (y < minY) y = minY;
        if (y > effectiveMaxY) y = effectiveMaxY;
    }

    public float getRenderX() {
        return x + screenShake.getOffsetX();
    }

    public float getRenderY() {
        return y + screenShake.getOffsetY();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
