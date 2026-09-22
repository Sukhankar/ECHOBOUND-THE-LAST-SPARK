package com.echobound.fx;

import java.awt.*;

public class FloatingTextManager {
    public static final int MAX_TEXTS = 64;
    private final FloatingText[] pool = new FloatingText[MAX_TEXTS];

    public FloatingTextManager() {
        for (int i = 0; i < MAX_TEXTS; i++) {
            pool[i] = new FloatingText();
        }
    }

    private FloatingText findFreeText() {
        for (int i = 0; i < MAX_TEXTS; i++) {
            if (!pool[i].active) {
                return pool[i];
            }
        }
        return pool[0]; // Recycle if full
    }

    public void spawnDamage(float x, float y, float z, int damage, boolean isCrit) {
        FloatingText ft = findFreeText();
        String txt = isCrit ? "CRIT! -" + damage : "-" + damage;
        Color c = isCrit ? new Color(255, 215, 0) : new Color(255, 60, 60);
        ft.spawn(txt, x, y, z, c, isCrit, isCrit ? 1.0f : 0.75f);
    }

    public void spawnHeal(float x, float y, float z, int heal) {
        FloatingText ft = findFreeText();
        ft.spawn("+" + heal + " HP", x, y, z, new Color(60, 240, 80), false, 0.8f);
    }

    public void spawnMana(float x, float y, float z, int mana) {
        FloatingText ft = findFreeText();
        ft.spawn("+" + mana + " MP", x, y, z, new Color(50, 180, 255), false, 0.8f);
    }

    public void spawnLoot(float x, float y, float z, String lootName, int count) {
        FloatingText ft = findFreeText();
        ft.spawn("+" + count + " " + lootName, x, y, z, new Color(255, 230, 90), false, 1.0f);
    }

    public void spawnMessage(float x, float y, float z, String msg, Color color) {
        FloatingText ft = findFreeText();
        ft.spawn(msg, x, y, z, color, false, 0.9f);
    }

    public void update(float dt) {
        for (int i = 0; i < MAX_TEXTS; i++) {
            if (pool[i].active) {
                pool[i].update(dt);
            }
        }
    }

    public void render(Graphics2D g, float camX, float camY) {
        Font prevFont = g.getFont();
        Font regularFont = new Font("Monospaced", Font.BOLD, 10);
        Font critFont = new Font("Monospaced", Font.BOLD, 12);

        for (int i = 0; i < MAX_TEXTS; i++) {
            FloatingText ft = pool[i];
            if (!ft.active) continue;

            int screenX = Math.round(ft.x - camX);
            int screenY = Math.round(ft.y - camY - ft.z * 0.5f);

            float alpha = Math.max(0.0f, Math.min(1.0f, 1.0f - (ft.lifetime / ft.maxLifetime)));
            g.setFont(ft.isCrit ? critFont : regularFont);

            // Shadow outline
            Color shadow = new Color(0, 0, 0, alpha * 0.8f);
            g.setColor(shadow);
            g.drawString(ft.text, screenX + 1, screenY + 1);

            // Main text
            Color mainColor = new Color(
                ft.color.getRed() / 255.0f,
                ft.color.getGreen() / 255.0f,
                ft.color.getBlue() / 255.0f,
                alpha
            );
            g.setColor(mainColor);
            g.drawString(ft.text, screenX, screenY);
        }
        g.setFont(prevFont);
    }

    public int getActiveCount() {
        int count = 0;
        for (int i = 0; i < MAX_TEXTS; i++) {
            if (pool[i].active) count++;
        }
        return count;
    }

    public void clear() {
        for (int i = 0; i < MAX_TEXTS; i++) {
            pool[i].active = false;
        }
    }
}
