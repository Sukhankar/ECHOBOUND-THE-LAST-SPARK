package com.echobound.assets;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private static final String DEFAULT_ASSETS_DIR = "assets";
    private final File baseDir;
    private final Map<String, BufferedImage> spriteCache = new HashMap<>();

    public AssetManager() {
        this(new File(DEFAULT_ASSETS_DIR));
    }

    public AssetManager(File baseDir) {
        this.baseDir = baseDir;
        ensureDefaultAssetsExist();
        preloadAll();
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void ensureDefaultAssetsExist() {
        try {
            File charDir = new File(baseDir, "characters");
            File mobDir = new File(baseDir, "mobs");
            File tileDir = new File(baseDir, "tiles");
            File mapDir = new File(baseDir, "maps");
            File uiDir = new File(baseDir, "ui");

            charDir.mkdirs();
            mobDir.mkdirs();
            tileDir.mkdirs();
            mapDir.mkdirs();
            uiDir.mkdirs();

            // Characters
            createAssetIfMissing(new File(charDir, "rin_idle.png"), generateRinSprite(false));
            createAssetIfMissing(new File(charDir, "rin_run.png"), generateRinSprite(true));
            createAssetIfMissing(new File(charDir, "pip_companion.png"), generatePipSprite());
            createAssetIfMissing(new File(charDir, "echo_clone.png"), generateEchoCloneSprite());
            createAssetIfMissing(new File(charDir, "npc_kael.png"), generateNPCSprite(new Color(170, 90, 40), "K"));
            createAssetIfMissing(new File(charDir, "npc_sylvan.png"), generateNPCSprite(new Color(60, 160, 80), "S"));

            // Mobs
            createAssetIfMissing(new File(mobDir, "corrupted_drone.png"), generateMobSprite(new Color(180, 30, 40), 16, 12));
            createAssetIfMissing(new File(mobDir, "shadow_creeper.png"), generateMobSprite(new Color(110, 40, 160), 14, 16));
            createAssetIfMissing(new File(mobDir, "magma_golem.png"), generateMobSprite(new Color(220, 80, 20), 20, 20));
            createAssetIfMissing(new File(mobDir, "void_stalker.png"), generateMobSprite(new Color(60, 20, 100), 16, 18));

            // Tiles
            createAssetIfMissing(new File(tileDir, "grass_block.png"), generateTileSprite(new Color(80, 180, 60), new Color(110, 80, 40)));
            createAssetIfMissing(new File(tileDir, "stone_block.png"), generateTileSprite(new Color(120, 125, 135), new Color(80, 85, 95)));
            createAssetIfMissing(new File(tileDir, "spark_ore.png"), generateTileSprite(new Color(100, 105, 115), new Color(0, 240, 255)));
            createAssetIfMissing(new File(tileDir, "water_block.png"), generateTileSprite(new Color(40, 130, 230), new Color(80, 180, 255)));
            createAssetIfMissing(new File(tileDir, "ancient_brick.png"), generateTileSprite(new Color(75, 70, 90), new Color(140, 120, 180)));

            // Maps
            createAssetIfMissing(new File(mapDir, "world_overworld_preview.png"), generateWorldMapPreview());
            createAssetIfMissing(new File(mapDir, "minimap_legend.png"), generateMinimapLegend());
            createAssetIfMissing(new File(mapDir, "biomes_map.png"), generateBiomesMap());

            // UI
            createAssetIfMissing(new File(uiDir, "controls_diagram.png"), generateControlsDiagram());
            createAssetIfMissing(new File(uiDir, "spark_icon.png"), generateSparkIcon());
            createAssetIfMissing(new File(uiDir, "tutorial_banner.png"), generateTutorialBanner());
        } catch (Exception ignored) {
            // Silently permit in headless environments without disk access
        }
    }

    private void createAssetIfMissing(File file, BufferedImage img) {
        if (!file.exists()) {
            try {
                ImageIO.write(img, "png", file);
            } catch (IOException ignored) {}
        }
    }

    public void preloadAll() {
        // Load into cache
        loadSprite("characters/rin_idle.png");
        loadSprite("characters/rin_run.png");
        loadSprite("characters/pip_companion.png");
        loadSprite("characters/echo_clone.png");
        loadSprite("characters/npc_kael.png");
        loadSprite("characters/npc_sylvan.png");

        loadSprite("mobs/corrupted_drone.png");
        loadSprite("mobs/shadow_creeper.png");
        loadSprite("mobs/magma_golem.png");
        loadSprite("mobs/void_stalker.png");

        loadSprite("tiles/grass_block.png");
        loadSprite("tiles/stone_block.png");
        loadSprite("tiles/spark_ore.png");
        loadSprite("tiles/water_block.png");
        loadSprite("tiles/ancient_brick.png");

        loadSprite("maps/world_overworld_preview.png");
        loadSprite("maps/minimap_legend.png");
        loadSprite("maps/biomes_map.png");

        loadSprite("ui/controls_diagram.png");
        loadSprite("ui/spark_icon.png");
        loadSprite("ui/tutorial_banner.png");
    }

    public BufferedImage getSprite(String relativePath) {
        BufferedImage img = spriteCache.get(relativePath);
        if (img == null) {
            img = loadSprite(relativePath);
        }
        return img;
    }

    private BufferedImage loadSprite(String relativePath) {
        File file = new File(baseDir, relativePath);
        if (file.exists()) {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    spriteCache.put(relativePath, img);
                    return img;
                }
            } catch (IOException ignored) {}
        }
        // Generate on demand if missing
        BufferedImage fallback = generateFallback(relativePath);
        spriteCache.put(relativePath, fallback);
        return fallback;
    }

    private BufferedImage generateFallback(String path) {
        if (path.contains("rin")) return generateRinSprite(path.contains("run"));
        if (path.contains("pip")) return generatePipSprite();
        if (path.contains("echo")) return generateEchoCloneSprite();
        if (path.contains("mob")) return generateMobSprite(Color.RED, 16, 16);
        if (path.contains("tile")) return generateTileSprite(Color.DARK_GRAY, Color.CYAN);
        if (path.contains("map")) return generateWorldMapPreview();
        return generateSparkIcon();
    }

    // --- Procedural Pixel Art Generators ---

    private BufferedImage generateRinSprite(boolean running) {
        BufferedImage img = new BufferedImage(16, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        // Hair & Head
        g.setColor(new Color(60, 40, 25));
        g.fillRect(5, 1, 6, 4);
        g.setColor(new Color(240, 200, 160));
        g.fillRect(5, 4, 6, 4);
        // Eyes
        g.setColor(new Color(0, 220, 255));
        g.fillRect(8, 5, 2, 1);
        // Cyan Scarf
        g.setColor(new Color(0, 240, 255));
        g.fillRect(4, 8, 8, 2);
        if (running) g.fillRect(2, 9, 3, 2); // Fluttering scarf
        // Tunic / Body
        g.setColor(new Color(40, 70, 140));
        g.fillRect(5, 10, 6, 5);
        // Spark Gauntlet
        g.setColor(new Color(255, 215, 0));
        g.fillRect(11, 11, 3, 3);
        // Legs
        g.setColor(new Color(30, 40, 60));
        if (running) {
            g.fillRect(4, 15, 3, 4);
            g.fillRect(9, 14, 3, 4);
        } else {
            g.fillRect(5, 15, 2, 5);
            g.fillRect(9, 15, 2, 5);
        }
        g.dispose();
        return img;
    }

    private BufferedImage generatePipSprite() {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        // Spark fairy aura
        g.setColor(new Color(255, 255, 150, 180));
        g.fillOval(1, 1, 8, 8);
        g.setColor(new Color(255, 230, 40));
        g.fillOval(3, 3, 4, 4);
        g.setColor(Color.WHITE);
        g.fillRect(4, 4, 2, 2);
        g.dispose();
        return img;
    }

    private BufferedImage generateEchoCloneSprite() {
        BufferedImage img = generateRinSprite(false);
        BufferedImage clone = new BufferedImage(16, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = clone.createGraphics();
        // Render tinted cyan phantom
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g.drawImage(img, 0, 0, null);
        g.setColor(new Color(0, 240, 255, 90));
        g.fillRect(0, 0, 16, 20);
        g.dispose();
        return clone;
    }

    private BufferedImage generateNPCSprite(Color cloakColor, String initial) {
        BufferedImage img = new BufferedImage(16, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(50, 45, 40));
        g.fillRect(5, 1, 6, 4); // Hair
        g.setColor(new Color(230, 190, 150));
        g.fillRect(5, 4, 6, 4); // Face
        g.setColor(cloakColor);
        g.fillRect(4, 8, 8, 7); // Cloak
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 7));
        g.drawString(initial, 6, 14);
        g.setColor(new Color(40, 40, 45));
        g.fillRect(5, 15, 2, 5);
        g.fillRect(9, 15, 2, 5);
        g.dispose();
        return img;
    }

    private BufferedImage generateMobSprite(Color color, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRoundRect(2, 2, w - 4, h - 4, 4, 4);
        // Glowing red eyes
        g.setColor(new Color(255, 50, 50));
        g.fillRect(w / 4, h / 3, 2, 2);
        g.fillRect(3 * w / 4 - 2, h / 3, 2, 2);
        g.dispose();
        return img;
    }

    private BufferedImage generateTileSprite(Color baseColor, Color detailColor) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(baseColor);
        g.fillRect(0, 0, 16, 16);
        g.setColor(detailColor);
        g.fillRect(2, 2, 4, 4);
        g.fillRect(10, 8, 4, 4);
        g.setColor(new Color(0, 0, 0, 40));
        g.drawRect(0, 0, 15, 15);
        g.dispose();
        return img;
    }

    private BufferedImage generateWorldMapPreview() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        // Plains
        g.setColor(new Color(65, 130, 55));
        g.fillRect(0, 0, 64, 64);
        // Water/Ocean coast
        g.setColor(new Color(30, 80, 180));
        g.fillRect(48, 0, 16, 64);
        // Mountain Peaks
        g.setColor(new Color(140, 145, 160));
        g.fillPolygon(new int[]{10, 24, 38}, new int[]{40, 12, 40}, 3);
        // Ancient Ruins
        g.setColor(new Color(180, 140, 70));
        g.fillRect(26, 42, 12, 10);
        // Spark Spire
        g.setColor(new Color(0, 240, 255));
        g.fillRect(31, 28, 3, 14);
        g.dispose();
        return img;
    }

    private BufferedImage generateMinimapLegend() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(20, 25, 35));
        g.fillRect(0, 0, 32, 32);
        g.setColor(Color.GREEN);
        g.fillRect(4, 4, 4, 4);
        g.setColor(Color.BLUE);
        g.fillRect(4, 12, 4, 4);
        g.setColor(Color.YELLOW);
        g.fillRect(4, 20, 4, 4);
        g.setColor(Color.CYAN);
        g.fillRect(4, 28, 4, 4);
        g.dispose();
        return img;
    }

    private BufferedImage generateBiomesMap() {
        BufferedImage img = new BufferedImage(48, 48, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(70, 150, 70)); // Forest
        g.fillRect(0, 0, 24, 24);
        g.setColor(new Color(210, 180, 90)); // Desert
        g.fillRect(24, 0, 24, 24);
        g.setColor(new Color(40, 100, 200)); // Ocean
        g.fillRect(0, 24, 24, 24);
        g.setColor(new Color(160, 70, 180)); // Void
        g.fillRect(24, 24, 24, 24);
        g.dispose();
        return img;
    }

    private BufferedImage generateControlsDiagram() {
        BufferedImage img = new BufferedImage(64, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(25, 30, 45));
        g.fillRect(0, 0, 64, 32);
        // WASD key outlines
        g.setColor(new Color(0, 240, 255));
        g.drawRect(18, 4, 8, 8); // W
        g.drawRect(8, 14, 8, 8); // A
        g.drawRect(18, 14, 8, 8); // S
        g.drawRect(28, 14, 8, 8); // D
        // Spacebar
        g.drawRect(10, 24, 24, 5);
        g.dispose();
        return img;
    }

    private BufferedImage generateSparkIcon() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0, 240, 255));
        g.fillPolygon(new int[]{8, 14, 8, 2}, new int[]{1, 8, 15, 8}, 4);
        g.setColor(Color.WHITE);
        g.fillOval(6, 6, 4, 4);
        g.dispose();
        return img;
    }

    private BufferedImage generateTutorialBanner() {
        BufferedImage img = new BufferedImage(128, 24, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(15, 20, 35));
        g.fillRect(0, 0, 128, 24);
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        g.drawString("ECHOBOUND ACADEMY", 10, 16);
        g.dispose();
        return img;
    }

    public int getCachedSpriteCount() {
        return spriteCache.size();
    }
}
