package com.echobound.settings;

import com.echobound.core.ResolutionProfile;

import java.io.Serializable;

public class GameSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    public float masterVolume = 1.0f;
    public float sfxVolume = 1.0f;
    public ResolutionProfile resolutionProfile = ResolutionProfile.PIXEL_STANDARD;
    public boolean cameraShakeEnabled = true;
    public boolean showDebugOverlay = false;
    public boolean vsync = true;
    public boolean firstTimeUser = true;

    public void cycleResolution() {
        if (resolutionProfile == ResolutionProfile.PIXEL_SAVER) {
            resolutionProfile = ResolutionProfile.PIXEL_STANDARD;
        } else if (resolutionProfile == ResolutionProfile.PIXEL_STANDARD) {
            resolutionProfile = ResolutionProfile.PIXEL_PLUS;
        } else {
            resolutionProfile = ResolutionProfile.PIXEL_SAVER;
        }
    }

    public void adjustMasterVolume(float delta) {
        masterVolume = Math.max(0.0f, Math.min(1.0f, masterVolume + delta));
    }

    public void toggleCameraShake() {
        cameraShakeEnabled = !cameraShakeEnabled;
    }

    public void toggleDebugOverlay() {
        showDebugOverlay = !showDebugOverlay;
    }
}
