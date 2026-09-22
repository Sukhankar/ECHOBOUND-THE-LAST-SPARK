package com.echobound.ui;

import com.echobound.core.Quality;
import com.echobound.entity.Runner;
import com.echobound.fx.ScreenShake;
import com.echobound.physics.AABB;
import com.echobound.world.TileMap;

import java.awt.*;
import java.util.List;

public class DebugOverlay {
    private boolean enabled = false;
    private final Font debugFont = new Font(Font.MONOSPACED, Font.BOLD, 10);

    public void toggle() {
        enabled = !enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void render(Graphics2D g, Runner player, TileMap map, float camX, float camY,
                       int fps, int ups, long tickCount, Quality quality, ScreenShake shake) {
        if (!enabled) return;

        // 1. Render World Hitboxes
        renderHitboxes(g, player, map, camX, camY);

        // 2. Render HUD metrics panel
        renderMetricsPanel(g, player, fps, ups, tickCount, quality, shake);
    }

    private void renderHitboxes(Graphics2D g, Runner player, TileMap map, float camX, float camY) {
        // Runner Hitbox (Green = ground, Yellow = air, Cyan = dash)
        AABB box = player.getHitbox();
        int px = (int) (box.x - camX);
        int py = (int) (box.y - camY);
        int pw = (int) box.width;
        int ph = (int) box.height;

        Color hitColor = player.isDashing ? Color.CYAN : (player.onGround ? Color.GREEN : Color.YELLOW);
        g.setColor(new Color(hitColor.getRed(), hitColor.getGreen(), hitColor.getBlue(), 60));
        g.fillRect(px, py, pw, ph);
        g.setColor(hitColor);
        g.drawRect(px, py, pw, ph);

        // Surrounding Solid Tiles Hitboxes in view
        List<AABB> solids = map.getCollidingSolidTiles(new AABB(box.x - 32, box.y - 32, box.width + 64, box.height + 64));
        g.setColor(new Color(255, 0, 0, 140));
        for (AABB s : solids) {
            g.drawRect((int) (s.x - camX), (int) (s.y - camY), (int) s.width, (int) s.height);
        }
    }

    private void renderMetricsPanel(Graphics2D g, Runner player, int fps, int ups,
                                   long tickCount, Quality quality, ScreenShake shake) {
        g.setFont(debugFont);
        FontMetrics fm = g.getFontMetrics();

        String[] lines = {
            String.format("FPS: %3d | UPS: %2d | TICK: %d", fps, ups, tickCount),
            String.format("POS: (%.1f, %.1f) | VEL: (%.1f, %.1f)", player.x, player.y, player.vx, player.vy),
            String.format("STATE: %-11s | FACING: %s", player.state, player.facing == 1 ? "RIGHT" : "LEFT"),
            String.format("GROUND: %-5b | WALL: L=%-5b R=%-5b", player.onGround, player.onWallLeft, player.onWallRight),
            String.format("COYOTE: %.2fs | JUMP_BUF: %.2fs", Math.max(0, player.coyoteTimer), Math.max(0, player.jumpBufferTimer)),
            String.format("DASH_T: %.2fs | DASH_CD: %.2fs", Math.max(0, player.dashTimer), Math.max(0, player.dashCooldownTimer)),
            String.format("DOUBLE_JUMP: %-5b | AIR_DASH: %-5b", player.canDoubleJump, player.canAirDash),
            String.format("QUALITY: %-8s | SHAKE: %s (F3)", quality.getLabel(), shake.isEnabled() ? "ON" : "OFF"),
            "[F1: Toggle Debug | F2: Quality | F11: Fullscreen | R: Respawn]"
        };

        int panelW = 380;
        int panelH = lines.length * 13 + 8;
        int panelX = 6;
        int panelY = 6;

        g.setColor(new Color(10, 14, 20, 210));
        g.fillRoundRect(panelX, panelY, panelW, panelH, 6, 6);
        g.setColor(new Color(0, 240, 255, 180));
        g.drawRoundRect(panelX, panelY, panelW, panelH, 6, 6);

        int textY = panelY + 12;
        for (String line : lines) {
            if (line.startsWith("FPS")) {
                g.setColor(new Color(0, 255, 180));
            } else if (line.startsWith("[")) {
                g.setColor(new Color(180, 180, 180));
            } else {
                g.setColor(Color.WHITE);
            }
            g.drawString(line, panelX + 8, textY);
            textY += 13;
        }
    }
}
