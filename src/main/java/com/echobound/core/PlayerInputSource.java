package com.echobound.core;

import java.awt.event.KeyEvent;

public class PlayerInputSource implements InputSource {
    private final Input input;

    public PlayerInputSource(Input input) {
        this.input = input;
    }

    @Override
    public boolean isLeft() {
        return input.isHeld(KeyEvent.VK_LEFT) || input.isHeld(KeyEvent.VK_A);
    }

    @Override
    public boolean isRight() {
        return input.isHeld(KeyEvent.VK_RIGHT) || input.isHeld(KeyEvent.VK_D);
    }

    @Override
    public boolean isUp() {
        return input.isHeld(KeyEvent.VK_UP) || input.isHeld(KeyEvent.VK_W);
    }

    @Override
    public boolean isDown() {
        return input.isHeld(KeyEvent.VK_DOWN) || input.isHeld(KeyEvent.VK_S);
    }

    @Override
    public boolean isJumpPressed() {
        return input.isJustPressed(KeyEvent.VK_SPACE) || input.isJustPressed(KeyEvent.VK_Z);
    }

    @Override
    public boolean isJumpHeld() {
        return input.isHeld(KeyEvent.VK_SPACE) || input.isHeld(KeyEvent.VK_Z);
    }

    @Override
    public boolean isDashPressed() {
        return input.isJustPressed(KeyEvent.VK_C) || input.isJustPressed(KeyEvent.VK_SHIFT);
    }

    @Override
    public boolean isShootPressed() {
        return input.isJustPressed(KeyEvent.VK_X) || input.isJustPressed(KeyEvent.VK_J);
    }

    @Override
    public boolean isInteractPressed() {
        return input.isJustPressed(KeyEvent.VK_ENTER) || input.isJustPressed(KeyEvent.VK_UP);
    }
}
