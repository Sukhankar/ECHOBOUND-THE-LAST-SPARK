package com.echobound.audio;

import javax.sound.sampled.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundEngine {
    private static final AudioFormat FORMAT = new AudioFormat(
        SoundSynthesizer.SAMPLE_RATE,
        8,  // 8-bit
        1,  // Mono
        true, // Signed
        false // Little-endian
    );

    private final Map<SoundType, byte[]> soundCache = new EnumMap<>(SoundType.class);
    private final ExecutorService soundPool = Executors.newFixedThreadPool(4);
    private boolean soundEnabled = true;
    private float masterVolume = 1.0f;
    private int totalSoundsPlayed = 0;

    public SoundEngine() {
        precacheAllSounds();
    }

    private void precacheAllSounds() {
        for (SoundType type : SoundType.values()) {
            soundCache.put(type, SoundSynthesizer.generateSoundBytes(type));
        }
    }

    public void play(SoundType type) {
        if (!soundEnabled || masterVolume <= 0.0f || type == null) return;
        totalSoundsPlayed++;

        byte[] rawBytes = soundCache.get(type);
        if (rawBytes == null) return;

        soundPool.submit(() -> {
            try {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, FORMAT);
                if (!AudioSystem.isLineSupported(info)) {
                    return;
                }
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(FORMAT);
                line.start();

                // Apply volume
                byte[] playBuffer = new byte[rawBytes.length];
                for (int i = 0; i < rawBytes.length; i++) {
                    playBuffer[i] = (byte) (rawBytes[i] * masterVolume);
                }

                line.write(playBuffer, 0, playBuffer.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {
                // Gracefully ignore audio device unavailability in headless / test environments
            }
        });
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public int getTotalSoundsPlayed() {
        return totalSoundsPlayed;
    }

    public byte[] getCachedBuffer(SoundType type) {
        return soundCache.get(type);
    }

    public void shutdown() {
        soundPool.shutdown();
    }
}
