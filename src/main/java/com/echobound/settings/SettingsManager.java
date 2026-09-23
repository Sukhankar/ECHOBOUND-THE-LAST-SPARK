package com.echobound.settings;

import com.echobound.audio.SoundEngine;
import com.echobound.core.ResolutionProfile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class SettingsManager {
    private final Path configPath;
    private final GameSettings settings = new GameSettings();

    public SettingsManager() {
        this(Paths.get("./saves/config.properties"));
    }

    public SettingsManager(Path configPath) {
        this.configPath = configPath;
        load();
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void load() {
        if (!Files.exists(configPath)) return;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(configPath)) {
            props.load(in);
            settings.masterVolume = Float.parseFloat(props.getProperty("masterVolume", "1.0"));
            settings.sfxVolume = Float.parseFloat(props.getProperty("sfxVolume", "1.0"));
            settings.cameraShakeEnabled = Boolean.parseBoolean(props.getProperty("cameraShakeEnabled", "true"));
            settings.showDebugOverlay = Boolean.parseBoolean(props.getProperty("showDebugOverlay", "false"));
            settings.firstTimeUser = Boolean.parseBoolean(props.getProperty("firstTimeUser", "true"));

            String res = props.getProperty("resolutionProfile", "PIXEL_STANDARD");
            try {
                settings.resolutionProfile = ResolutionProfile.valueOf(res);
            } catch (Exception e) {
                settings.resolutionProfile = ResolutionProfile.PIXEL_STANDARD;
            }
        } catch (Exception ignored) {}
    }

    public boolean save() {
        try {
            if (configPath.getParent() != null) {
                Files.createDirectories(configPath.getParent());
            }
            Properties props = new Properties();
            props.setProperty("masterVolume", String.valueOf(settings.masterVolume));
            props.setProperty("sfxVolume", String.valueOf(settings.sfxVolume));
            props.setProperty("cameraShakeEnabled", String.valueOf(settings.cameraShakeEnabled));
            props.setProperty("showDebugOverlay", String.valueOf(settings.showDebugOverlay));
            props.setProperty("firstTimeUser", String.valueOf(settings.firstTimeUser));
            props.setProperty("resolutionProfile", settings.resolutionProfile.name());

            try (OutputStream out = Files.newOutputStream(configPath)) {
                props.store(out, "ECHOBOUND Game Settings");
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    public void applySettings(SoundEngine soundEngine) {
        if (soundEngine != null) {
            soundEngine.setMasterVolume(settings.masterVolume);
            soundEngine.setMusicVolume(settings.masterVolume);
            soundEngine.setSoundEnabled(settings.masterVolume > 0.001f);
        }
    }
}
