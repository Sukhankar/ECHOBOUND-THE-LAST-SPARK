package com.echobound.core;

/**
 * Swappable input provider for the shared Runner physics entity.
 * Can be driven by Player keyboard input or by Echo recorded input frames.
 */
public interface InputSource {
    boolean isLeft();
    boolean isRight();
    boolean isUp();
    boolean isDown();
    boolean isJumpPressed();
    boolean isJumpHeld();
    boolean isDashPressed();
    boolean isShootPressed();
    boolean isInteractPressed();
}
