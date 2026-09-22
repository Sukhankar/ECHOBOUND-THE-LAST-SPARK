package com.echobound.audio;

import java.util.Random;

public class SoundSynthesizer {
    public static final int SAMPLE_RATE = 22050; // 22.05 kHz retro mono

    public static byte[] generateSoundBytes(SoundType type) {
        int numSamples = (int) ((type.durationMs / 1000.0) * SAMPLE_RATE);
        byte[] buffer = new byte[numSamples];
        Random rng = new Random(42);

        for (int i = 0; i < numSamples; i++) {
            float t = (float) i / SAMPLE_RATE;
            float progress = (float) i / numSamples;
            float sample = 0.0f;

            switch (type) {
                case JUMP -> {
                    // Rising pitch from 250Hz to 600Hz
                    float freq = 250.0f + 350.0f * progress;
                    sample = squareWave(t, freq) * (1.0f - progress * 0.8f);
                }
                case DOUBLE_JUMP -> {
                    // Quick upward arpeggio
                    float freq = 450.0f + 500.0f * progress;
                    sample = (squareWave(t, freq) * 0.7f + sineWave(t, freq * 1.5f) * 0.3f) * (1.0f - progress);
                }
                case DASH -> {
                    // High-pass filtered noise
                    sample = (rng.nextFloat() * 2.0f - 1.0f) * (1.0f - progress);
                }
                case MINE_BLOCK -> {
                    // Short percussive click + pitch decay
                    float freq = 180.0f * (1.0f - progress);
                    sample = (triangleWave(t, freq) * 0.6f + (rng.nextFloat() * 2.0f - 1.0f) * 0.4f)
                             * (1.0f - progress);
                }
                case PLACE_BLOCK -> {
                    // Deep low pitch thud
                    float freq = 120.0f * (1.0f - progress * 0.5f);
                    sample = sineWave(t, freq) * (1.0f - progress);
                }
                case CAST_SPELL -> {
                    // Modulated FM wave
                    float mod = sineWave(t, 40.0f) * 100.0f;
                    sample = sineWave(t, 500.0f + mod) * (1.0f - progress * 0.7f);
                }
                case FIRE_TORNADO -> {
                    // Heavy rumbling noise + sub-bass
                    float noise = (rng.nextFloat() * 2.0f - 1.0f) * 0.5f;
                    float rumble = sineWave(t, 70.0f + 20.0f * (float) Math.sin(t * 30.0f)) * 0.5f;
                    sample = (noise + rumble) * (1.0f - progress * 0.5f);
                }
                case STORM_BURST -> {
                    // Lightning snap + decaying static
                    float snap = (progress < 0.1f) ? squareWave(t, 800.0f) : (rng.nextFloat() * 2.0f - 1.0f);
                    sample = snap * (1.0f - progress);
                }
                case CRAFT_SUCCESS -> {
                    // 4-step arpeggio: C5 (523), E5 (659), G5 (784), C6 (1046)
                    float[] notes = {523.25f, 659.25f, 783.99f, 1046.50f};
                    int noteIdx = Math.min(3, (int) (progress * 4));
                    sample = triangleWave(t, notes[noteIdx]) * (1.0f - (progress % 0.25f) * 3.0f);
                }
                case QUEST_COMPLETE -> {
                    // Major chord progression
                    float[] notes = {440.0f, 554.37f, 659.25f, 880.0f};
                    int noteIdx = Math.min(3, (int) (progress * 4));
                    sample = squareWave(t, notes[noteIdx]) * 0.6f + sineWave(t, notes[noteIdx] * 0.5f) * 0.4f;
                }
                case CORRUPTION_ALARM -> {
                    // Descending ominous low saw
                    float freq = 200.0f - 120.0f * progress;
                    sample = sawWave(t, freq) * (1.0f - progress * 0.3f);
                }
                case BOSS_ROAR -> {
                    // Sub-bass growl with frequency modulation
                    float freq = 55.0f + 30.0f * (float) Math.sin(t * 15.0f);
                    sample = (sawWave(t, freq) * 0.7f + (rng.nextFloat() * 2.0f - 1.0f) * 0.3f) * (1.0f - progress * 0.2f);
                }
                case ECHO_RECORD -> {
                    // Crystal sine beep
                    sample = sineWave(t, 880.0f) * (1.0f - progress);
                }
                case ECHO_REPLAY -> {
                    // Reverse sweep
                    float freq = 400.0f + 600.0f * progress;
                    sample = sineWave(t, freq) * (1.0f - progress * 0.5f);
                }
            }

            // Convert float [-1.0, 1.0] to signed 8-bit byte [-128, 127]
            int b = (int) (Math.max(-1.0f, Math.min(1.0f, sample)) * 127.0f);
            buffer[i] = (byte) b;
        }
        return buffer;
    }

    private static float sineWave(float t, float freq) {
        return (float) Math.sin(2.0 * Math.PI * freq * t);
    }

    private static float squareWave(float t, float freq) {
        return ((t * freq) % 1.0f < 0.5f) ? 1.0f : -1.0f;
    }

    private static float triangleWave(float t, float freq) {
        float phase = (t * freq) % 1.0f;
        return (phase < 0.5f) ? (phase * 4.0f - 1.0f) : (3.0f - phase * 4.0f);
    }

    private static float sawWave(float t, float freq) {
        return ((t * freq) % 1.0f) * 2.0f - 1.0f;
    }
}
