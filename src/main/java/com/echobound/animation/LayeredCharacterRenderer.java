package com.echobound.animation;

import com.echobound.combat.ModdedWeapon;
import com.echobound.items.EquipmentManager;
import com.echobound.items.EquipmentSlot;
import com.echobound.items.ItemRegistry;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class LayeredCharacterRenderer {

    public static void render(Graphics2D g, AnimationController anim, EquipmentManager equip,
                              ModdedWeapon weapon, float x, float y, float scale, boolean isEcho) {
        BufferedImage bodyFrame = anim != null ? anim.getCurrentFrame() : null;
        if (bodyFrame == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int frameW = bodyFrame.getWidth();
        int frameH = bodyFrame.getHeight();
        int drawW = Math.round(frameW * scale);
        int drawH = Math.round(frameH * scale);
        int drawX = Math.round(x - drawW * 0.5f);
        int drawY = Math.round(y - drawH);

        boolean facingLeft = anim != null && anim.isFacingLeft();

        AffineTransform prevTx = g2.getTransform();
        if (facingLeft) {
            // Mirror horizontally across sprite center
            g2.translate(drawX + drawW, drawY);
            g2.scale(-1.0, 1.0);
            drawX = 0;
            drawY = 0;
        }

        if (isEcho) {
            // Echo Clone: Translucent cyan tint + waveform chromatic ghost
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2.drawImage(bodyFrame, drawX - 2, drawY, drawW, drawH, null); // Ghost trail
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
            g2.drawImage(bodyFrame, drawX, drawY, drawW, drawH, null);
            // Cyan spectral overlay
            g2.setColor(new Color(0, 240, 255, 90));
            g2.fillRect(drawX, drawY, drawW, drawH);
            g2.dispose();
            return;
        }

        // 1. Render Base Body
        g2.drawImage(bodyFrame, drawX, drawY, drawW, drawH, null);

        // 2. Layered Equipment Overlays
        if (equip != null) {
            renderEquipmentLayers(g2, equip, drawX, drawY, drawW, drawH, anim != null ? anim.getCurrentState() : AnimationState.IDLE);
        }

        // 3. Render Weapon in Hand
        if (weapon != null) {
            renderWeaponLayer(g2, weapon, drawX, drawY, drawW, drawH, anim);
        }

        g2.setTransform(prevTx);
        g2.dispose();
    }

    private static void renderEquipmentLayers(Graphics2D g, EquipmentManager equip,
                                             int dx, int dy, int dw, int dh, AnimationState state) {
        // HEAD: Miner Helm
        if (equip.getEquipped(EquipmentSlot.HEAD) == ItemRegistry.EQUIP_MINER_HELM) {
            g.setColor(new Color(210, 160, 40));
            g.fillRect(dx + (dw * 5) / 16, dy + (dh * 2) / 20, (dw * 6) / 16, (dh * 3) / 20);
            // Headlamp beam
            g.setColor(Color.WHITE);
            g.fillRect(dx + (dw * 9) / 16, dy + (dh * 3) / 20, (dw * 2) / 16, (dh * 2) / 20);
            g.setColor(new Color(255, 255, 180, 100));
            g.fillPolygon(new int[]{dx + (dw * 11) / 16, dx + dw + 8, dx + dw + 8},
                          new int[]{dy + (dh * 4) / 20, dy - 2, dy + (dh * 8) / 20}, 3);
        }

        // BODY: Forest Cloak
        if (equip.getEquipped(EquipmentSlot.BODY) == ItemRegistry.EQUIP_FOREST_CLOAK) {
            g.setColor(new Color(45, 120, 60));
            g.fillRect(dx + (dw * 4) / 16, dy + (dh * 8) / 20, (dw * 8) / 16, (dh * 7) / 20);
            // Hood
            g.setColor(new Color(35, 100, 50));
            g.fillRect(dx + (dw * 4) / 16, dy + (dh * 5) / 20, (dw * 2) / 16, (dh * 4) / 20);
        }

        // GLOVES: Miner Gloves
        if (equip.getEquipped(EquipmentSlot.GLOVES) == ItemRegistry.EQUIP_MINER_GLOVES) {
            g.setColor(new Color(140, 95, 50));
            g.fillRect(dx + (dw * 11) / 16, dy + (dh * 10) / 20, (dw * 3) / 16, (dh * 4) / 20);
        }

        // BOOTS: Storm Boots
        if (equip.getEquipped(EquipmentSlot.BOOTS) == ItemRegistry.EQUIP_STORM_BOOTS) {
            g.setColor(new Color(30, 80, 160));
            g.fillRect(dx + (dw * 4) / 16, dy + (dh * 15) / 20, (dw * 3) / 16, (dh * 5) / 20);
            g.fillRect(dx + (dw * 9) / 16, dy + (dh * 15) / 20, (dw * 3) / 16, (dh * 5) / 20);
            // Winged lightning sparks on heels
            g.setColor(new Color(0, 240, 255));
            g.fillRect(dx + (dw * 2) / 16, dy + (dh * 16) / 20, (dw * 2) / 16, (dh * 2) / 20);
        }

        // CORE: Ember Core
        if (equip.getEquipped(EquipmentSlot.CORE) == ItemRegistry.EQUIP_EMBER_CORE) {
            g.setColor(new Color(255, 90, 20));
            g.fillOval(dx + (dw * 7) / 16, dy + (dh * 9) / 20, (dw * 3) / 16, (dh * 3) / 20);
            g.setColor(new Color(255, 220, 100));
            g.fillOval(dx + (dw * 8) / 16, dy + (dh * 10) / 20, (dw * 1) / 16, (dh * 1) / 20);
        }
    }

    private static void renderWeaponLayer(Graphics2D g, ModdedWeapon weapon,
                                         int dx, int dy, int dw, int dh, AnimationController anim) {
        int handX = dx + (dw * 11) / 16;
        int handY = dy + (dh * 11) / 20;

        AnimationState state = anim != null ? anim.getCurrentState() : AnimationState.IDLE;
        float stateTime = anim != null ? anim.getStateTime() : 0.0f;

        Graphics2D wg = (Graphics2D) g.create();

        if (state == AnimationState.ATTACK || state == AnimationState.MINE) {
            // Animated weapon swing arc
            float progress = Math.min(1.0f, stateTime / 0.35f);
            double angle = Math.toRadians(-45.0 + progress * 135.0); // Swing from -45 to +90 degrees

            wg.rotate(angle, handX, handY);

            // Draw Weapon Blade / Head
            drawWeaponSprite(wg, weapon.baseWeaponId, handX, handY);

            // Slashing trail arc
            wg.setColor(new Color(255, 255, 255, (int) (180 * (1.0f - progress))));
            wg.setStroke(new BasicStroke(2.0f));
            wg.drawArc(handX - 8, handY - 14, 20, 20, -30, 90);
        } else {
            // Idle / Running stance: weapon held at ready
            wg.rotate(Math.toRadians(25.0), handX, handY);
            drawWeaponSprite(wg, weapon.baseWeaponId, handX, handY);
        }

        wg.dispose();
    }

    private static void drawWeaponSprite(Graphics2D g, int weaponId, int hx, int hy) {
        if (weaponId == ItemRegistry.WEAPON_IRON_SWORD || weaponId == ItemRegistry.WEAPON_WOOD_SWORD) {
            // Blade
            Color bladeColor = (weaponId == ItemRegistry.WEAPON_IRON_SWORD) ? new Color(200, 215, 230) : new Color(170, 115, 65);
            g.setColor(bladeColor);
            g.fillRect(hx + 2, hy - 12, 3, 12);
            // Crossguard & Hilt
            g.setColor(new Color(210, 170, 50));
            g.fillRect(hx - 1, hy, 7, 2);
            g.setColor(new Color(80, 50, 30));
            g.fillRect(hx + 2, hy + 2, 2, 4);
        } else if (weaponId == ItemRegistry.WEAPON_SPARK_PISTOL) {
            // Futuristic Spark Pistol
            g.setColor(new Color(40, 50, 70));
            g.fillRect(hx, hy - 4, 8, 4);
            g.setColor(new Color(0, 240, 255));
            g.fillRect(hx + 4, hy - 3, 4, 2);
            g.setColor(new Color(20, 25, 35));
            g.fillRect(hx, hy, 3, 4);
        } else if (weaponId == ItemRegistry.WEAPON_HAMMER_CANNON) {
            // Heavy Hammer
            g.setColor(new Color(110, 75, 45));
            g.fillRect(hx + 1, hy - 14, 3, 16);
            g.setColor(new Color(80, 85, 100));
            g.fillRect(hx - 3, hy - 18, 11, 6);
        } else {
            // Generic weapon blade
            g.setColor(new Color(190, 200, 215));
            g.fillRect(hx + 1, hy - 10, 3, 10);
        }
    }
}
