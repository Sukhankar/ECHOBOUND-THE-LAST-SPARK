package com.echobound.animation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Animation {
    public final String name;
    private final List<AnimationFrame> frames = new ArrayList<>();
    public final boolean looping;
    private float totalDuration = 0.0f;

    public Animation(String name, boolean looping) {
        this.name = name;
        this.looping = looping;
    }

    public void addFrame(BufferedImage image, float duration) {
        AnimationFrame frame = new AnimationFrame(image, duration);
        frames.add(frame);
        totalDuration += frame.duration;
    }

    public int getFrameCount() {
        return frames.size();
    }

    public float getTotalDuration() {
        return totalDuration;
    }

    public BufferedImage getFrame(float time) {
        if (frames.isEmpty()) {
            return null;
        }
        if (frames.size() == 1 || totalDuration <= 0.0f) {
            return frames.get(0).image;
        }

        float effectiveTime = time;
        if (looping) {
            effectiveTime = time % totalDuration;
        } else if (time >= totalDuration) {
            return frames.get(frames.size() - 1).image;
        }

        float accum = 0.0f;
        for (AnimationFrame frame : frames) {
            accum += frame.duration;
            if (effectiveTime < accum) {
                return frame.image;
            }
        }
        return frames.get(frames.size() - 1).image;
    }

    public int getFrameIndex(float time) {
        if (frames.isEmpty()) return 0;
        float effectiveTime = looping ? (time % Math.max(0.001f, totalDuration)) : Math.min(time, totalDuration - 0.001f);
        float accum = 0.0f;
        for (int i = 0; i < frames.size(); i++) {
            accum += frames.get(i).duration;
            if (effectiveTime < accum) {
                return i;
            }
        }
        return frames.size() - 1;
    }
}
