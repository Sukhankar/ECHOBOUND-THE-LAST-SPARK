package com.echobound.ui.menu;

import com.echobound.save.SaveData;
import com.echobound.save.SaveManager;
import com.echobound.settings.SettingsManager;

import java.util.List;

public class TitleMenuController {
    public enum TitleOption {
        CONTINUE("Continue"),
        NEW_GAME("New Game"),
        LOAD_GAME("Saved Games"),
        OPTIONS("Options"),
        EXIT("Exit to Desktop");

        public final String label;
        TitleOption(String label) { this.label = label; }
    }

    public enum OptionsItem {
        MASTER_VOLUME("Master Volume"),
        RESOLUTION("Resolution Scale"),
        CAMERA_SHAKE("Camera Shake"),
        DEBUG_OVERLAY("Debug Info"),
        BACK("Back to Title");

        public final String label;
        OptionsItem(String label) { this.label = label; }
    }

    private GameState currentState = GameState.LOADING;
    private int titleCursor = 0;
    private int optionsCursor = 0;
    private int saveSlotCursor = 0;

    private final SaveManager saveManager;
    private final SettingsManager settingsManager;
    private final LoadingScreen loadingScreen = new LoadingScreen();

    public TitleMenuController(SaveManager saveManager, SettingsManager settingsManager) {
        this.saveManager = saveManager;
        this.settingsManager = settingsManager;
    }

    public void update(float dt) {
        if (currentState == GameState.LOADING) {
            loadingScreen.update(dt);
            if (loadingScreen.isFinished()) {
                currentState = GameState.TITLE_MENU;
                // Default cursor to CONTINUE if save exists, else NEW GAME
                titleCursor = (saveManager.getMostRecentSlot() > 0) ? 0 : 1;
            }
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setState(GameState state) {
        this.currentState = state;
    }

    public LoadingScreen getLoadingScreen() {
        return loadingScreen;
    }

    public int getTitleCursor() {
        return titleCursor;
    }

    public int getOptionsCursor() {
        return optionsCursor;
    }

    public int getSaveSlotCursor() {
        return saveSlotCursor;
    }

    public void moveCursorUp() {
        if (currentState == GameState.TITLE_MENU) {
            titleCursor = (titleCursor - 1 + TitleOption.values().length) % TitleOption.values().length;
            // Skip CONTINUE if no save exists
            if (titleCursor == 0 && saveManager.getMostRecentSlot() <= 0) {
                titleCursor = TitleOption.values().length - 1;
            }
        } else if (currentState == GameState.OPTIONS_MENU) {
            optionsCursor = (optionsCursor - 1 + OptionsItem.values().length) % OptionsItem.values().length;
        } else if (currentState == GameState.SAVE_SELECT_MENU) {
            saveSlotCursor = (saveSlotCursor - 1 + (SaveManager.MAX_SLOTS + 1)) % (SaveManager.MAX_SLOTS + 1);
        }
    }

    public void moveCursorDown() {
        if (currentState == GameState.TITLE_MENU) {
            titleCursor = (titleCursor + 1) % TitleOption.values().length;
            // Skip CONTINUE if no save exists
            if (titleCursor == 0 && saveManager.getMostRecentSlot() <= 0) {
                titleCursor = 1;
            }
        } else if (currentState == GameState.OPTIONS_MENU) {
            optionsCursor = (optionsCursor + 1) % OptionsItem.values().length;
        } else if (currentState == GameState.SAVE_SELECT_MENU) {
            saveSlotCursor = (saveSlotCursor + 1) % (SaveManager.MAX_SLOTS + 1);
        }
    }

    public void adjustOptionLeft() {
        if (currentState == GameState.OPTIONS_MENU) {
            OptionsItem item = OptionsItem.values()[optionsCursor];
            switch (item) {
                case MASTER_VOLUME -> settingsManager.getSettings().adjustMasterVolume(-0.1f);
                case RESOLUTION -> settingsManager.getSettings().cycleResolution();
                case CAMERA_SHAKE -> settingsManager.getSettings().toggleCameraShake();
                case DEBUG_OVERLAY -> settingsManager.getSettings().toggleDebugOverlay();
                case BACK -> {}
            }
            settingsManager.save();
        }
    }

    public void adjustOptionRight() {
        if (currentState == GameState.OPTIONS_MENU) {
            OptionsItem item = OptionsItem.values()[optionsCursor];
            switch (item) {
                case MASTER_VOLUME -> settingsManager.getSettings().adjustMasterVolume(+0.1f);
                case RESOLUTION -> settingsManager.getSettings().cycleResolution();
                case CAMERA_SHAKE -> settingsManager.getSettings().toggleCameraShake();
                case DEBUG_OVERLAY -> settingsManager.getSettings().toggleDebugOverlay();
                case BACK -> {}
            }
            settingsManager.save();
        }
    }

    public boolean selectCurrent() {
        if (currentState == GameState.TITLE_MENU) {
            TitleOption option = TitleOption.values()[titleCursor];
            switch (option) {
                case CONTINUE -> {
                    int slot = saveManager.getMostRecentSlot();
                    if (slot > 0) {
                        currentState = GameState.PLAYING;
                        return true;
                    }
                }
                case NEW_GAME -> {
                    currentState = GameState.PLAYING;
                    return true;
                }
                case LOAD_GAME -> {
                    currentState = GameState.SAVE_SELECT_MENU;
                    saveSlotCursor = 0;
                }
                case OPTIONS -> {
                    currentState = GameState.OPTIONS_MENU;
                    optionsCursor = 0;
                }
                case EXIT -> {
                    return false;
                }
            }
        } else if (currentState == GameState.OPTIONS_MENU) {
            if (optionsCursor == OptionsItem.BACK.ordinal()) {
                currentState = GameState.TITLE_MENU;
            } else {
                adjustOptionRight();
            }
        } else if (currentState == GameState.SAVE_SELECT_MENU) {
            if (saveSlotCursor >= SaveManager.MAX_SLOTS) {
                // Back button
                currentState = GameState.TITLE_MENU;
            } else {
                int slot = saveSlotCursor + 1;
                if (saveManager.hasSave(slot)) {
                    currentState = GameState.PLAYING;
                    return true;
                }
            }
        }
        return true;
    }
}
