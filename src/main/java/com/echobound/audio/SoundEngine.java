package com.echobound.audio;

import javax.sound.sampled.*;
import java.io.File;
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

    /**
     * Real recorded CC0 samples (RPG Sound Pack, opengameart.org), keyed to the gameplay
     * event they best fit. Checked first in play(); any SoundType with no entry here — or
     * whose file is missing — falls straight back to the procedural synth below, so this is
     * a pure enhancement layer, never a hard dependency.
     */
    private static final Map<SoundType, String> SAMPLE_PATHS = new EnumMap<>(SoundType.class);
    static {
        SAMPLE_PATHS.put(SoundType.MINE_BLOCK, "audio/battle/swing.wav");
        SAMPLE_PATHS.put(SoundType.CAST_SPELL, "audio/battle/spell.wav");
        SAMPLE_PATHS.put(SoundType.CRAFT_SUCCESS, "audio/inventory/coin.wav");
        SAMPLE_PATHS.put(SoundType.STORM_BURST, "audio/battle/magic1.wav");
        SAMPLE_PATHS.put(SoundType.BOSS_ROAR, "audio/npc/boss_roar.wav");
        SAMPLE_PATHS.put(SoundType.CORRUPTION_ALARM, "audio/npc/corruption_alarm.wav");
        SAMPLE_PATHS.put(SoundType.ECHO_RECORD, "audio/interface/interface1.wav");
        SAMPLE_PATHS.put(SoundType.ECHO_REPLAY, "audio/interface/interface2.wav");
    }

    private final Map<SoundType, byte[]> soundCache = new EnumMap<>(SoundType.class);
    private final Map<SoundType, File> sampleFiles = new EnumMap<>(SoundType.class);
    private final ExecutorService soundPool = Executors.newFixedThreadPool(4);
    private boolean soundEnabled = true;
    private float masterVolume = 1.0f;
    private int totalSoundsPlayed = 0;
    private int realSamplesLoaded = 0;

    public SoundEngine() {
        this(new File("assets"));
    }

    public SoundEngine(File assetsDir) {
        precacheAllSounds();
        resolveSampleFiles(assetsDir);
    }

    private void precacheAllSounds() {
        for (SoundType type : SoundType.values()) {
            soundCache.put(type, SoundSynthesizer.generateSoundBytes(type));
        }
    }

    private void resolveSampleFiles(File assetsDir) {
        for (Map.Entry<SoundType, String> entry : SAMPLE_PATHS.entrySet()) {
            File f = new File(assetsDir, entry.getValue());
            if (f.isFile()) {
                sampleFiles.put(entry.getKey(), f);
                realSamplesLoaded++;
            }
        }
    }

    public int getRealSamplesLoaded() { return realSamplesLoaded; }

    public void play(SoundType type) {
        if (!soundEnabled || masterVolume <= 0.0f || type == null) return;
        totalSoundsPlayed++;

        File sample = sampleFiles.get(type);
        if (sample != null) {
            soundPool.submit(() -> playSampleFile(sample));
            return;
        }

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

    /** Plays a real WAV sample via javax.sound.sampled, which handles format conversion
     *  itself — unlike the procedural path, no manual PCM format matching is needed. */
    private void playSampleFile(File file) {
        try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
            Clip clip = AudioSystem.getClip();
            clip.open(in);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float clamped = Math.max(0.0001f, masterVolume);
                gain.setValue(Math.max(gain.getMinimum(),
                        Math.min(gain.getMaximum(), (float) (20 * Math.log10(clamped)))));
            }
            clip.start();
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) clip.close();
            });
        } catch (Exception ignored) {
            // Gracefully ignore audio device / codec unavailability — same policy as the
            // procedural path above, so a missing or unsupported sample never crashes play().
        }
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
