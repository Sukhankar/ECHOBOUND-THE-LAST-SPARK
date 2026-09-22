package com.echobound.fx;

public class ScreenShake {
    private float trauma = 0.0f; // Range [0, 1]
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private boolean enabled = true;

    public void addTrauma(float amount) {
        if (!enabled) return;
        trauma = Math.min(1.0f, trauma + amount);
    }

    public void update(float dt) {
        if (!enabled || trauma <= 0.0f) {
            trauma = 0.0f;
            offsetX = 0.0f;
            offsetY = 0.0f;
            return;
        }

        // Nonlinear shake power
        float shake = trauma * trauma;
        float maxOffset = 10.0f;
        offsetX = (float) ((Math.random() * 2.0 - 1.0) * maxOffset * shake);
        offsetY = (float) ((Math.random() * 2.0 - 1.0) * maxOffset * shake);

        // Decay trauma over time
        trauma = Math.max(0.0f, trauma - dt * 2.5f);
    }

    public float getOffsetX() {
        return enabled ? offsetX : 0.0f;
    }

    public float getOffsetY() {
        return enabled ? offsetY : 0.0f;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            offsetX = 0;
            offsetY = 0;
            trauma = 0;
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }
}
