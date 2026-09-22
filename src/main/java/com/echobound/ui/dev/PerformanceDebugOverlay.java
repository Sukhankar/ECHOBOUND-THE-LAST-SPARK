package com.echobound.ui.dev;

import com.echobound.assets.AssetManager;
import com.echobound.core.UnifiedGameContext;

import java.awt.*;

public class PerformanceDebugOverlay {

    public static void render(Graphics2D g, UnifiedGameContext ctx, AssetManager assetManager,
                              float fps, float frameTimeMs, float physicsTimeMs, float renderTimeMs,
                              int windowWidth) {
        int hudW = 210;
        int hudH = 145;
        int hudX = windowWidth - hudW - 10;
        int hudY = 10;

        // Dark Translucent Panel with cyan border
        g.setColor(new Color(10, 15, 25, 210));
        g.fillRect(hudX, hudY, hudW, hudH);
        g.setColor(new Color(0, 240, 255, 180));
        g.drawRect(hudX, hudY, hudW, hudH);

        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        g.setColor(new Color(0, 240, 255));
        g.drawString("[F10] PERFORMANCE DEBUG HUD", hudX + 8, hudY + 14);

        g.setFont(new Font("Monospaced", Font.PLAIN, 9));
        g.setColor(new Color(230, 240, 255));

        int y = hudY + 28;
        int spacing = 11;

        // FPS & Timings
        g.drawString(String.format("FPS:           %.1f", fps), hudX + 8, y);
        y += spacing;
        g.drawString(String.format("Frame Time:    %.2f ms", frameTimeMs), hudX + 8, y);
        y += spacing;
        g.drawString(String.format("Physics Time:  %.2f ms", physicsTimeMs), hudX + 8, y);
        y += spacing;
        g.drawString(String.format("Render Time:   %.2f ms", renderTimeMs), hudX + 8, y);
        y += spacing;

        // Chunks & Entities
        int loadedChunks = (ctx != null && ctx.world != null) ? ctx.world.getLoadedChunkCount() : 0;
        int activeMobs = (ctx != null && ctx.mobManager != null) ? ctx.mobManager.getActiveMobs().size() : 0;
        int activeNpcs = (ctx != null && ctx.npcManager != null) ? ctx.npcManager.getAll().size() : 0;
        int totalEntities = activeMobs + activeNpcs + 1; // +1 for player

        g.drawString(String.format("Loaded Chunks: %d", loadedChunks), hudX + 8, y);
        y += spacing;
        g.drawString(String.format("Active Ents:   %d (Mobs:%d, NPCs:%d)", totalEntities, activeMobs, activeNpcs), hudX + 8, y);
        y += spacing;

        // Particles & Sprites
        int particles = (ctx != null && ctx.particleFXManager != null) ? ctx.particleFXManager.getActiveCount() : 0;
        int cachedSprites = (assetManager != null) ? assetManager.getCachedSpriteCount() : 0;
        g.drawString(String.format("Particle Count:%d", particles), hudX + 8, y);
        y += spacing;
        g.drawString(String.format("Sprite Count:  %d", cachedSprites), hudX + 8, y);
        y += spacing;

        // Memory & Heap
        long heapUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        long heapMax = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        long estimatedTexMemKb = cachedSprites * 32L; // estimated ~32KB per cached atlas/subimage
        g.drawString(String.format("Texture Mem:   ~%d KB", estimatedTexMemKb), hudX + 8, y);
        y += spacing;
        g.drawString(String.format("Heap Usage:    %d MB / %d MB", heapUsed, heapMax), hudX + 8, y);
    }
}
