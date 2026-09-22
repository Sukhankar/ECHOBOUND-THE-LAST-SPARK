package com.echobound.animation;

import java.awt.image.BufferedImage;

public class AnimationFrame {
    public final BufferedImage image;
    public final float duration; // in seconds

    public AnimationFrame(BufferedImage image, float duration) {
        this.image = image;
        this.duration = Math.max(0.01f, duration);
    }
}
