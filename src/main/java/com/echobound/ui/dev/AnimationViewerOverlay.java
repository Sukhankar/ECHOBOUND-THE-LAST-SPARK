package com.echobound.ui.dev;

import com.echobound.animation.Animation;
import com.echobound.animation.AnimationController;
import com.echobound.animation.AnimationState;
import com.echobound.assets.AssetManager;
import com.echobound.entity.mob.MobType;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class AnimationViewerOverlay {

    public enum CharacterType {
        RIN("Rin (Protagonist)"),
        PIP("Pip (Companion)"),
        KAEL("Master Blacksmith Kael"),
        SYLVAN("Botanist Sylvan"),
        CORRUPTED_DRONE("Corrupted Drone"),
        SHADOW_CREEPER("Shadow Creeper"),
        MAGMA_GOLEM("Magma Golem"),
        VOID_STALKER("Void Stalker");

        public final String displayName;
        CharacterType(String displayName) {
            this.displayName = displayName;
        }
    }

    private final AssetManager assetManager;
    private CharacterType selectedCharacter = CharacterType.RIN;
    private AnimationController currentController;
    private AnimationState selectedState = AnimationState.IDLE;
    private final List<AnimationState> availableStates = new ArrayList<>();
    private int stateIndex = 0;

    private boolean paused = false;
    private float targetFps = 12.0f;
    private boolean showHitbox = true;
    private boolean showSpriteBounds = true;
    private boolean loop = true;
    private boolean facingLeft = false;

    public AnimationViewerOverlay(AssetManager assetManager) {
        this.assetManager = assetManager;
        selectCharacter(CharacterType.RIN);
    }

    public void selectCharacter(CharacterType type) {
        this.selectedCharacter = type;
        availableStates.clear();

        if (type == CharacterType.RIN) {
            currentController = assetManager.createRinAnimationController();
            availableStates.add(AnimationState.IDLE);
            availableStates.add(AnimationState.WALK);
            availableStates.add(AnimationState.RUN);
            availableStates.add(AnimationState.JUMP);
            availableStates.add(AnimationState.FALL);
            availableStates.add(AnimationState.DOUBLE_JUMP);
            availableStates.add(AnimationState.DASH);
            availableStates.add(AnimationState.GLIDE);
            availableStates.add(AnimationState.ATTACK);
            availableStates.add(AnimationState.MINE);
            availableStates.add(AnimationState.CAST_MAGIC);
            availableStates.add(AnimationState.HURT);
            availableStates.add(AnimationState.DEAD);
        } else if (type == CharacterType.PIP) {
            currentController = assetManager.createPipAnimationController();
            availableStates.add(AnimationState.IDLE);
            availableStates.add(AnimationState.RUN);
            availableStates.add(AnimationState.HURT);
        } else if (type == CharacterType.CORRUPTED_DRONE) {
            currentController = assetManager.createMobAnimationController(MobType.CORRUPTED_DRONE);
            availableStates.add(AnimationState.IDLE);
            availableStates.add(AnimationState.ATTACK);
            availableStates.add(AnimationState.DEAD);
        } else if (type == CharacterType.SHADOW_CREEPER) {
            currentController = assetManager.createMobAnimationController(MobType.SHADOW_CREEPER);
            availableStates.add(AnimationState.IDLE);
            availableStates.add(AnimationState.ATTACK);
            availableStates.add(AnimationState.DEAD);
        } else if (type == CharacterType.MAGMA_GOLEM) {
            currentController = assetManager.createMobAnimationController(MobType.MAGMA_GOLEM);
            availableStates.add(AnimationState.IDLE);
            availableStates.add(AnimationState.ATTACK);
            availableStates.add(AnimationState.DEAD);
        } else if (type == CharacterType.VOID_STALKER) {
            currentController = assetManager.createMobAnimationController(MobType.VOID_STALKER);
            availableStates.add(AnimationState.IDLE);
            availableStates.add(AnimationState.ATTACK);
            availableStates.add(AnimationState.DEAD);
        } else {
            // NPC
            currentController = assetManager.createRinAnimationController();
            availableStates.add(AnimationState.IDLE);
            availableStates.add(AnimationState.WORK);
            availableStates.add(AnimationState.SIT);
            availableStates.add(AnimationState.SLEEP);
        }

        stateIndex = 0;
        selectedState = availableStates.get(0);
        currentController.setState(selectedState);
    }

    public void update(float dt) {
        if (!paused && currentController != null) {
            currentController.setFacingLeft(facingLeft);
            currentController.update(dt * (targetFps / 12.0f));
        }
    }

    public boolean handleKeyPress(int keyCode) {
        // Character Switching: 1-8
        if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_8) {
            int idx = keyCode - KeyEvent.VK_1;
            CharacterType[] all = CharacterType.values();
            if (idx < all.length) {
                selectCharacter(all[idx]);
                return true;
            }
        }

        // Animation State: UP / DOWN
        if (keyCode == KeyEvent.VK_UP) {
            if (!availableStates.isEmpty()) {
                stateIndex = (stateIndex - 1 + availableStates.size()) % availableStates.size();
                selectedState = availableStates.get(stateIndex);
                currentController.setState(selectedState);
            }
            return true;
        }
        if (keyCode == KeyEvent.VK_DOWN) {
            if (!availableStates.isEmpty()) {
                stateIndex = (stateIndex + 1) % availableStates.size();
                selectedState = availableStates.get(stateIndex);
                currentController.setState(selectedState);
            }
            return true;
        }

        // Pause / Play: SPACE
        if (keyCode == KeyEvent.VK_SPACE) {
            paused = !paused;
            return true;
        }

        // Frame Stepping: LEFT / RIGHT (when paused)
        if (keyCode == KeyEvent.VK_RIGHT) {
            if (currentController != null) {
                currentController.update(1.0f / targetFps);
            }
            return true;
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            if (currentController != null) {
                // Restart state or step
                currentController.setState(selectedState);
            }
            return true;
        }

        // FPS Adjustment: PLUS / EQUALS / MINUS
        if (keyCode == KeyEvent.VK_PLUS || keyCode == KeyEvent.VK_EQUALS) {
            targetFps = Math.min(60.0f, targetFps + 2.0f);
            return true;
        }
        if (keyCode == KeyEvent.VK_MINUS) {
            targetFps = Math.max(2.0f, targetFps - 2.0f);
            return true;
        }

        // Hitbox: H
        if (keyCode == KeyEvent.VK_H) {
            showHitbox = !showHitbox;
            return true;
        }

        // Bounds: B
        if (keyCode == KeyEvent.VK_B) {
            showSpriteBounds = !showSpriteBounds;
            return true;
        }

        // Loop: L
        if (keyCode == KeyEvent.VK_L) {
            loop = !loop;
            return true;
        }

        // Direction Flip: D
        if (keyCode == KeyEvent.VK_D) {
            facingLeft = !facingLeft;
            currentController.setFacingLeft(facingLeft);
            return true;
        }

        return false;
    }

    public void render(Graphics2D g, int viewW, int viewH) {
        int panelW = 460;
        int panelH = 280;
        int panelX = (viewW - panelW) / 2;
        int panelY = (viewH - panelH) / 2;

        // Dark modal background with gold/cyan border
        g.setColor(new Color(15, 20, 32, 245));
        g.fillRect(panelX, panelY, panelW, panelH);
        g.setColor(new Color(0, 240, 255));
        g.drawRect(panelX, panelY, panelW, panelH);
        g.setColor(new Color(255, 215, 0));
        g.drawRect(panelX + 2, panelY + 2, panelW - 4, panelH - 4);

        // Header
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.setColor(new Color(255, 215, 0));
        g.drawString("[F9] PIXEL-ART ANIMATION VIEWER", panelX + 12, panelY + 20);

        // Left Column: Character & Animation list
        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        g.setColor(new Color(0, 240, 255));
        g.drawString("CHARACTERS [1-8]:", panelX + 12, panelY + 40);

        g.setFont(new Font("Monospaced", Font.PLAIN, 9));
        CharacterType[] chars = CharacterType.values();
        for (int i = 0; i < Math.min(8, chars.length); i++) {
            boolean active = chars[i] == selectedCharacter;
            g.setColor(active ? new Color(255, 220, 100) : new Color(170, 180, 200));
            g.drawString(String.format("%d. %s", i + 1, chars[i].displayName), panelX + 12, panelY + 54 + i * 11);
        }

        int animY = panelY + 150;
        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        g.setColor(new Color(0, 240, 255));
        g.drawString("ANIMATIONS [UP/DOWN]:", panelX + 12, animY);

        g.setFont(new Font("Monospaced", Font.PLAIN, 9));
        for (int i = 0; i < availableStates.size(); i++) {
            boolean active = i == stateIndex;
            g.setColor(active ? new Color(255, 220, 100) : new Color(160, 170, 190));
            g.drawString((active ? " > " : "   ") + availableStates.get(i).name(), panelX + 12, animY + 14 + i * 11);
        }

        // Center Preview Box (Enlarged 4x)
        int previewBoxX = panelX + 210;
        int previewBoxY = panelY + 40;
        int previewBoxW = 150;
        int previewBoxH = 150;

        g.setColor(new Color(25, 32, 48));
        g.fillRect(previewBoxX, previewBoxY, previewBoxW, previewBoxH);
        g.setColor(new Color(60, 75, 105));
        g.drawRect(previewBoxX, previewBoxY, previewBoxW, previewBoxH);

        // Draw Checkerboard ground inside preview
        g.setColor(new Color(35, 45, 65));
        for (int x = 0; x < previewBoxW; x += 16) {
            for (int y = 0; y < previewBoxH; y += 16) {
                if ((x / 16 + y / 16) % 2 == 0) {
                    g.fillRect(previewBoxX + x, previewBoxY + y, 16, 16);
                }
            }
        }

        // Draw animated sprite frame
        if (currentController != null) {
            BufferedImage frame = currentController.getCurrentFrame();
            if (frame != null) {
                int scale = 4;
                int fw = frame.getWidth() * scale;
                int fh = frame.getHeight() * scale;
                int fx = previewBoxX + (previewBoxW - fw) / 2;
                int fy = previewBoxY + previewBoxH - fh - 10;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                if (facingLeft) {
                    g2.translate(fx + fw, fy);
                    g2.scale(-1.0, 1.0);
                    g2.drawImage(frame, 0, 0, fw, fh, null);
                } else {
                    g2.drawImage(frame, fx, fy, fw, fh, null);
                }
                g2.dispose();

                // Bounds
                if (showSpriteBounds) {
                    g.setColor(new Color(0, 240, 255, 160));
                    g.drawRect(fx, fy, fw, fh);
                }
                // Hitbox
                if (showHitbox) {
                    g.setColor(new Color(255, 60, 60, 180));
                    int hw = (int) (fw * 0.5f);
                    int hh = (int) (fh * 0.7f);
                    int hx = fx + (fw - hw) / 2;
                    int hy = fy + fh - hh;
                    g.drawRect(hx, hy, hw, hh);
                }
            }
        }

        // Right Column: Controls & Information
        int infoX = panelX + 370;
        int infoY = panelY + 44;
        g.setFont(new Font("Monospaced", Font.BOLD, 9));
        g.setColor(new Color(0, 240, 255));
        g.drawString("STATUS & CONTROLS", infoX, infoY);

        g.setFont(new Font("Monospaced", Font.PLAIN, 8));
        g.setColor(Color.WHITE);
        g.drawString(String.format("State: %s", selectedState.name()), infoX, infoY + 16);
        g.drawString(String.format("Speed: %.0f FPS [+/-]", targetFps), infoX, infoY + 28);
        g.drawString(String.format("Loop:  %s [L]", loop ? "ON" : "OFF"), infoX, infoY + 40);
        g.drawString(String.format("Dir:   %s [D]", facingLeft ? "LEFT" : "RIGHT"), infoX, infoY + 52);
        g.drawString(String.format("Box:   %s [H]", showHitbox ? "ON" : "OFF"), infoX, infoY + 64);
        g.drawString(String.format("Bound: %s [B]", showSpriteBounds ? "ON" : "OFF"), infoX, infoY + 76);
        g.drawString(String.format("Pause: %s [SPACE]", paused ? "YES" : "NO"), infoX, infoY + 88);
        g.drawString("Step:  [LEFT/RIGHT]", infoX, infoY + 100);

        // Footer
        g.setFont(new Font("Monospaced", Font.ITALIC, 9));
        g.setColor(new Color(160, 180, 210));
        g.drawString("[F9 / ESC] Close Animation Viewer", panelX + 12, panelY + panelH - 12);
    }
}
