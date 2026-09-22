package com.echobound.core;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

public class Input implements KeyListener {
    private static final int KEY_COUNT = 512;

    // Raw keyboard state updated by AWT thread
    private final boolean[] rawKeys = new boolean[KEY_COUNT];

    // Synchronized state for the fixed 60Hz tick
    private final boolean[] currentKeys = new boolean[KEY_COUNT];
    private final boolean[] prevKeys = new boolean[KEY_COUNT];
    private final boolean[] justPressedKeys = new boolean[KEY_COUNT];

    // Function triggers (consumed when polled)
    private volatile boolean f1Triggered = false;
    private volatile boolean f2Triggered = false;
    private volatile boolean f3Triggered = false;
    private volatile boolean f11Triggered = false;
    private volatile boolean retryTriggered = false;

    public synchronized void tick() {
        // Copy raw keys to tick buffer
        System.arraycopy(rawKeys, 0, currentKeys, 0, KEY_COUNT);
        for (int i = 0; i < KEY_COUNT; i++) {
            justPressedKeys[i] = currentKeys[i] && !prevKeys[i];
        }
        System.arraycopy(currentKeys, 0, prevKeys, 0, KEY_COUNT);
    }

    public synchronized boolean isHeld(int keyCode) {
        if (keyCode >= 0 && keyCode < KEY_COUNT) {
            return currentKeys[keyCode];
        }
        return false;
    }

    public synchronized boolean isJustPressed(int keyCode) {
        if (keyCode >= 0 && keyCode < KEY_COUNT) {
            return justPressedKeys[keyCode];
        }
        return false;
    }

    // Function key queries
    public boolean pollF1() {
        boolean val = f1Triggered;
        f1Triggered = false;
        return val;
    }

    public boolean pollF2() {
        boolean val = f2Triggered;
        f2Triggered = false;
        return val;
    }

    public boolean pollF3() {
        boolean val = f3Triggered;
        f3Triggered = false;
        return val;
    }

    public boolean pollF11() {
        boolean val = f11Triggered;
        f11Triggered = false;
        return val;
    }

    public boolean pollRetry() {
        boolean val = retryTriggered;
        retryTriggered = false;
        return val;
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < KEY_COUNT) {
            rawKeys[code] = true;
        }
        if (code == KeyEvent.VK_F1) f1Triggered = true;
        if (code == KeyEvent.VK_F2) f2Triggered = true;
        if (code == KeyEvent.VK_F3) f3Triggered = true;
        if (code == KeyEvent.VK_F11) f11Triggered = true;
        if (code == KeyEvent.VK_R) retryTriggered = true;
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < KEY_COUNT) {
            rawKeys[code] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public synchronized void reset() {
        Arrays.fill(rawKeys, false);
        Arrays.fill(currentKeys, false);
        Arrays.fill(prevKeys, false);
        Arrays.fill(justPressedKeys, false);
    }
}
