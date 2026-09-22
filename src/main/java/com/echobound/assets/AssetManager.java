package com.echobound.assets;

import com.echobound.animation.Animation;
import com.echobound.animation.AnimationController;
import com.echobound.animation.AnimationFrame;
import com.echobound.animation.AnimationState;
import com.echobound.animation.NPCVisualController;
import com.echobound.entity.mob.MobType;
import com.echobound.graphics.SpriteSheet;
import com.echobound.items.ItemRegistry;
import com.echobound.npc.NPCDefinition;
import com.echobound.npc.NPCManager;
import com.echobound.sandbox.BlockType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
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
            File npcDir = new File(baseDir, "npcs");
            File portraitDir = new File(baseDir, "portraits");
            File mobDir = new File(baseDir, "mobs");
            File weaponDir = new File(baseDir, "weapons");
            File toolDir = new File(baseDir, "tools");
            File itemDir = new File(baseDir, "items");
            File tileDir = new File(baseDir, "tiles");
            File effectDir = new File(baseDir, "effects");
            File mapDir = new File(baseDir, "maps");
            File uiDir = new File(baseDir, "ui");

            charDir.mkdirs();
            npcDir.mkdirs();
            portraitDir.mkdirs();
            mobDir.mkdirs();
            weaponDir.mkdirs();
            toolDir.mkdirs();
            itemDir.mkdirs();
            tileDir.mkdirs();
            effectDir.mkdirs();
            mapDir.mkdirs();
            uiDir.mkdirs();

            // 1. Character Sheets
            createAssetIfMissing(new File(charDir, "rin_sheet.png"), generateRinSheet());
            createAssetIfMissing(new File(charDir, "pip_sheet.png"), generatePipSheet());
            createAssetIfMissing(new File(charDir, "echo_sheet.png"), generateEchoSheet());
            createAssetIfMissing(new File(charDir, "rin_idle.png"), generateRinSingle(false));
            createAssetIfMissing(new File(charDir, "rin_run.png"), generateRinSingle(true));
            createAssetIfMissing(new File(charDir, "pip_companion.png"), generatePipSingle());
            createAssetIfMissing(new File(charDir, "echo_clone.png"), generateEchoSingle());

            // 2. NPC Sheets & Portraits
            createAssetIfMissing(new File(npcDir, "npc_sheet.png"), generateNPCSheet());
            createAssetIfMissing(new File(charDir, "npc_kael.png"), generateNPCSingle(new Color(170, 90, 40), "K"));
            createAssetIfMissing(new File(charDir, "npc_sylvan.png"), generateNPCSingle(new Color(60, 160, 80), "S"));

            createAssetIfMissing(new File(portraitDir, "kael_portrait.png"), generatePortrait(new Color(170, 90, 40), "Kael"));
            createAssetIfMissing(new File(portraitDir, "sylvan_portrait.png"), generatePortrait(new Color(60, 160, 80), "Sylvan"));
            createAssetIfMissing(new File(portraitDir, "engineer_portrait.png"), generatePortrait(new Color(210, 140, 40), "Engineer"));
            createAssetIfMissing(new File(portraitDir, "nomad_portrait.png"), generatePortrait(new Color(200, 180, 100), "Nomad"));
            createAssetIfMissing(new File(portraitDir, "pilot_portrait.png"), generatePortrait(new Color(80, 160, 220), "Pilot"));
            createAssetIfMissing(new File(portraitDir, "miner_portrait.png"), generatePortrait(new Color(130, 135, 150), "Miner"));
            createAssetIfMissing(new File(portraitDir, "researcher_portrait.png"), generatePortrait(new Color(180, 220, 240), "Researcher"));
            createAssetIfMissing(new File(portraitDir, "mechanic_portrait.png"), generatePortrait(new Color(240, 60, 180), "Mechanic"));
            createAssetIfMissing(new File(portraitDir, "mage_portrait.png"), generatePortrait(new Color(120, 80, 220), "Mage"));

            // 3. Mobs Sheets
            createAssetIfMissing(new File(mobDir, "mobs_sheet.png"), generateMobsSheet());
            createAssetIfMissing(new File(mobDir, "corrupted_drone.png"), generateMobSingle(new Color(180, 30, 40)));
            createAssetIfMissing(new File(mobDir, "shadow_creeper.png"), generateMobSingle(new Color(110, 40, 160)));
            createAssetIfMissing(new File(mobDir, "magma_golem.png"), generateMobSingle(new Color(220, 80, 20)));
            createAssetIfMissing(new File(mobDir, "void_stalker.png"), generateMobSingle(new Color(60, 20, 100)));

            // 4. Weapons & Tools
            createAssetIfMissing(new File(weaponDir, "weapon_sheet.png"), generateWeaponSheet());
            createAssetIfMissing(new File(toolDir, "tool_sheet.png"), generateToolSheet());

            // 5. Items
            createAssetIfMissing(new File(itemDir, "item_sheet.png"), generateItemSheet());

            // 6. Tiles
            createAssetIfMissing(new File(tileDir, "terrain_sheet.png"), generateTerrainSheet());
            createAssetIfMissing(new File(tileDir, "grass_block.png"), generateTileSingle(new Color(80, 180, 60), new Color(110, 80, 40)));
            createAssetIfMissing(new File(tileDir, "stone_block.png"), generateTileSingle(new Color(120, 125, 135), new Color(80, 85, 95)));
            createAssetIfMissing(new File(tileDir, "spark_ore.png"), generateTileSingle(new Color(100, 105, 115), new Color(0, 240, 255)));
            createAssetIfMissing(new File(tileDir, "water_block.png"), generateTileSingle(new Color(40, 130, 230), new Color(80, 180, 255)));
            createAssetIfMissing(new File(tileDir, "ancient_brick.png"), generateTileSingle(new Color(75, 70, 90), new Color(140, 120, 180)));

            // 7. Effects
            createAssetIfMissing(new File(effectDir, "effects_sheet.png"), generateEffectsSheet());

            // 8. Maps
            createAssetIfMissing(new File(mapDir, "world_overworld_preview.png"), generateWorldMapPreview());
            createAssetIfMissing(new File(mapDir, "minimap_legend.png"), generateMinimapLegend());
            createAssetIfMissing(new File(mapDir, "biomes_map.png"), generateBiomesMap());

            // 9. UI
            createAssetIfMissing(new File(uiDir, "ui_sheet.png"), generateUISheet());
            createAssetIfMissing(new File(uiDir, "controls_diagram.png"), generateControlsDiagram());
            createAssetIfMissing(new File(uiDir, "spark_icon.png"), generateSparkIcon());
            createAssetIfMissing(new File(uiDir, "tutorial_banner.png"), generateTutorialBanner());
        } catch (Exception ignored) {}
    }

    private void createAssetIfMissing(File file, BufferedImage img) {
        if (!file.exists()) {
            try {
                ImageIO.write(img, "png", file);
            } catch (IOException ignored) {}
        }
    }

    public void preloadAll() {
        // Preload key sheets
        getSprite("characters/rin_sheet.png");
        getSprite("characters/pip_sheet.png");
        getSprite("characters/echo_sheet.png");
        getSprite("npcs/npc_sheet.png");
        getSprite("mobs/mobs_sheet.png");
        getSprite("weapons/weapon_sheet.png");
        getSprite("tools/tool_sheet.png");
        getSprite("items/item_sheet.png");
        getSprite("tiles/terrain_sheet.png");
        getSprite("effects/effects_sheet.png");
        getSprite("ui/ui_sheet.png");
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
        BufferedImage fallback = generateFallback(relativePath);
        spriteCache.put(relativePath, fallback);
        return fallback;
    }

    private BufferedImage generateFallback(String path) {
        if (path.contains("rin_sheet")) return generateRinSheet();
        if (path.contains("pip_sheet")) return generatePipSheet();
        if (path.contains("echo_sheet")) return generateEchoSheet();
        if (path.contains("npc_sheet")) return generateNPCSheet();
        if (path.contains("mobs_sheet")) return generateMobsSheet();
        if (path.contains("weapon_sheet")) return generateWeaponSheet();
        if (path.contains("tool_sheet")) return generateToolSheet();
        if (path.contains("item_sheet")) return generateItemSheet();
        if (path.contains("terrain_sheet")) return generateTerrainSheet();
        if (path.contains("effects_sheet")) return generateEffectsSheet();
        if (path.contains("ui_sheet")) return generateUISheet();
        return generateSparkIcon();
    }

    // --- High-Level Animation Factory Methods ---

    public AnimationController createRinAnimationController() {
        BufferedImage sheetImg = getSprite("characters/rin_sheet.png");
        SpriteSheet sheet = new SpriteSheet(sheetImg, 32, 32);

        AnimationController ctrl = new AnimationController();

        // 1. IDLE (Row 0, 4 frames, 0.16s each)
        Animation idle = new Animation("IDLE", true);
        for (int i = 0; i < 4; i++) idle.addFrame(sheet.getSprite(i, 0), 0.16f);
        ctrl.registerAnimation(AnimationState.IDLE, idle);

        // 2. WALK (Row 1, 6 frames, 0.10s each)
        Animation walk = new Animation("WALK", true);
        for (int i = 0; i < 6; i++) walk.addFrame(sheet.getSprite(i, 1), 0.10f);
        ctrl.registerAnimation(AnimationState.WALK, walk);

        // 3. RUN (Row 2, 8 frames, 0.07s each)
        Animation run = new Animation("RUN", true);
        for (int i = 0; i < 8; i++) run.addFrame(sheet.getSprite(i, 2), 0.07f);
        ctrl.registerAnimation(AnimationState.RUN, run);

        // 4. JUMP (Row 3, frames 0..2)
        Animation jump = new Animation("JUMP", false);
        for (int i = 0; i < 3; i++) jump.addFrame(sheet.getSprite(i, 3), 0.12f);
        ctrl.registerAnimation(AnimationState.JUMP, jump);

        // 5. FALL (Row 3, frames 3..4)
        Animation fall = new Animation("FALL", true);
        for (int i = 3; i < 5; i++) fall.addFrame(sheet.getSprite(i, 3), 0.14f);
        ctrl.registerAnimation(AnimationState.FALL, fall);

        // 6. DOUBLE_JUMP (Row 3, frames 5..7)
        Animation djump = new Animation("DOUBLE_JUMP", false);
        for (int i = 5; i < 8; i++) djump.addFrame(sheet.getSprite(i, 3), 0.08f);
        ctrl.registerAnimation(AnimationState.DOUBLE_JUMP, djump);

        // 7. DASH (Row 4, 4 frames, 0.06s each)
        Animation dash = new Animation("DASH", false);
        for (int i = 0; i < 4; i++) dash.addFrame(sheet.getSprite(i, 4), 0.06f);
        ctrl.registerAnimation(AnimationState.DASH, dash);

        // 8. GLIDE (Row 5, 4 frames, 0.12s each)
        Animation glide = new Animation("GLIDE", true);
        for (int i = 0; i < 4; i++) glide.addFrame(sheet.getSprite(i, 5), 0.12f);
        ctrl.registerAnimation(AnimationState.GLIDE, glide);

        // 9. ATTACK (Row 6, 6 frames, 0.06s each)
        Animation attack = new Animation("ATTACK", false);
        for (int i = 0; i < 6; i++) attack.addFrame(sheet.getSprite(i, 6), 0.06f);
        ctrl.registerAnimation(AnimationState.ATTACK, attack);

        // 10. MINE (Row 7, 4 frames, 0.09f each)
        Animation mine = new Animation("MINE", false);
        for (int i = 0; i < 4; i++) mine.addFrame(sheet.getSprite(i, 7), 0.09f);
        ctrl.registerAnimation(AnimationState.MINE, mine);

        // 11. CAST_MAGIC (Row 8, 6 frames, 0.08f each)
        Animation cast = new Animation("CAST_MAGIC", false);
        for (int i = 0; i < 6; i++) cast.addFrame(sheet.getSprite(i, 8), 0.08f);
        ctrl.registerAnimation(AnimationState.CAST_MAGIC, cast);

        // 12. HURT (Row 9, frames 0..2)
        Animation hurt = new Animation("HURT", false);
        for (int i = 0; i < 3; i++) hurt.addFrame(sheet.getSprite(i, 9), 0.10f);
        ctrl.registerAnimation(AnimationState.HURT, hurt);

        // 13. DEAD (Row 9, frames 3..7)
        Animation dead = new Animation("DEAD", false);
        for (int i = 3; i < 8; i++) dead.addFrame(sheet.getSprite(i, 9), 0.15f);
        ctrl.registerAnimation(AnimationState.DEAD, dead);

        // Populate aliases for remaining states
        ctrl.registerAnimation(AnimationState.HEAVY_ATTACK, attack);
        ctrl.registerAnimation(AnimationState.BUILD, mine);
        ctrl.registerAnimation(AnimationState.INTERACT, idle);
        ctrl.registerAnimation(AnimationState.ECHO_RECORD, cast);
        ctrl.registerAnimation(AnimationState.ECHO_RELEASE, cast);
        ctrl.registerAnimation(AnimationState.FISHING, idle);
        ctrl.registerAnimation(AnimationState.WALL_CLING, idle);
        ctrl.registerAnimation(AnimationState.WALL_JUMP, jump);
        ctrl.registerAnimation(AnimationState.CLIMBING, walk);
        ctrl.registerAnimation(AnimationState.MOUNTING, idle);
        ctrl.registerAnimation(AnimationState.SWIMMING, walk);

        ctrl.setState(AnimationState.IDLE);
        return ctrl;
    }

    public AnimationController createPipAnimationController() {
        BufferedImage sheetImg = getSprite("characters/pip_sheet.png");
        SpriteSheet sheet = new SpriteSheet(sheetImg, 16, 16);

        AnimationController ctrl = new AnimationController();
        Animation idle = new Animation("IDLE", true);
        for (int i = 0; i < 4; i++) idle.addFrame(sheet.getSprite(i, 0), 0.12f);
        ctrl.registerAnimation(AnimationState.IDLE, idle);

        Animation excited = new Animation("EXCITED", true);
        for (int i = 0; i < 4; i++) excited.addFrame(sheet.getSprite(i, 1), 0.08f);
        ctrl.registerAnimation(AnimationState.RUN, excited);

        Animation alert = new Animation("ALERT", true);
        for (int i = 0; i < 4; i++) alert.addFrame(sheet.getSprite(i, 2), 0.10f);
        ctrl.registerAnimation(AnimationState.HURT, alert);

        ctrl.setState(AnimationState.IDLE);
        return ctrl;
    }

    public AnimationController createMobAnimationController(MobType type) {
        BufferedImage sheetImg = getSprite("mobs/mobs_sheet.png");
        SpriteSheet sheet = new SpriteSheet(sheetImg, 32, 32);

        int rowOffset = 0;
        if (type == MobType.SHADOW_CREEPER) rowOffset = 1;
        else if (type == MobType.MAGMA_GOLEM) rowOffset = 2;
        else if (type == MobType.VOID_STALKER) rowOffset = 3;

        AnimationController ctrl = new AnimationController();

        Animation idle = new Animation("IDLE", true);
        for (int i = 0; i < 4; i++) idle.addFrame(sheet.getSprite(i, rowOffset * 3), 0.15f);
        ctrl.registerAnimation(AnimationState.IDLE, idle);
        ctrl.registerAnimation(AnimationState.WALK, idle);
        ctrl.registerAnimation(AnimationState.RUN, idle);

        Animation attack = new Animation("ATTACK", false);
        for (int i = 0; i < 4; i++) attack.addFrame(sheet.getSprite(i, rowOffset * 3 + 1), 0.09f);
        ctrl.registerAnimation(AnimationState.ATTACK, attack);

        Animation dead = new Animation("DEAD", false);
        for (int i = 0; i < 4; i++) dead.addFrame(sheet.getSprite(i, rowOffset * 3 + 2), 0.12f);
        ctrl.registerAnimation(AnimationState.DEAD, dead);
        ctrl.registerAnimation(AnimationState.HURT, dead);

        ctrl.setState(AnimationState.IDLE);
        return ctrl;
    }

    public NPCVisualController createNPCVisualController(NPCManager npcManager) {
        NPCVisualController nvc = new NPCVisualController();
        BufferedImage sheetImg = getSprite("npcs/npc_sheet.png");
        SpriteSheet sheet = new SpriteSheet(sheetImg, 32, 32);

        if (npcManager != null) {
            int npcIdx = 0;
            for (NPCDefinition npc : npcManager.getAll()) {
                int row = (npcIdx % 9) * 2;
                Map<AnimationState, Animation> anims = new EnumMap<>(AnimationState.class);

                Animation idle = new Animation("IDLE", true);
                for (int i = 0; i < 4; i++) idle.addFrame(sheet.getSprite(i, row), 0.16f);
                anims.put(AnimationState.IDLE, idle);

                Animation work = new Animation("WORK", true);
                for (int i = 4; i < 8; i++) work.addFrame(sheet.getSprite(i, row), 0.12f);
                anims.put(AnimationState.WORK, work);

                Animation sit = new Animation("SIT", true);
                for (int i = 0; i < 4; i++) sit.addFrame(sheet.getSprite(i, row + 1), 0.25f);
                anims.put(AnimationState.SIT, sit);

                Animation sleep = new Animation("SLEEP", true);
                for (int i = 4; i < 8; i++) sleep.addFrame(sheet.getSprite(i, row + 1), 0.35f);
                anims.put(AnimationState.SLEEP, sleep);

                nvc.registerNPC(npc, anims);
                npcIdx++;
            }
        }
        return nvc;
    }

    public BufferedImage getWeaponSprite(int weaponId) {
        BufferedImage sheetImg = getSprite("weapons/weapon_sheet.png");
        SpriteSheet sheet = new SpriteSheet(sheetImg, 24, 24);
        int idx = Math.max(0, (weaponId - 101) % 12);
        return sheet.getSprite(idx);
    }

    public BufferedImage getToolSprite(int toolId) {
        BufferedImage sheetImg = getSprite("tools/tool_sheet.png");
        SpriteSheet sheet = new SpriteSheet(sheetImg, 16, 16);
        return sheet.getSprite(Math.abs(toolId) % 10);
    }

    public BufferedImage getItemIcon(int itemId) {
        BufferedImage sheetImg = getSprite("items/item_sheet.png");
        SpriteSheet sheet = new SpriteSheet(sheetImg, 16, 16);
        int idx = (itemId / 100) * 8 + (itemId % 8);
        return sheet.getSprite(Math.abs(idx) % sheet.getColumns());
    }

    public BufferedImage getTileTexture(BlockType type, int variant) {
        BufferedImage sheetImg = getSprite("tiles/terrain_sheet.png");
        SpriteSheet sheet = new SpriteSheet(sheetImg, 16, 16);
        int row = type.ordinal() % sheet.getRows();
        int col = Math.abs(variant) % sheet.getColumns();
        return sheet.getSprite(col, row);
    }

    public BufferedImage getNPCPortrait(String npcId) {
        if (npcId != null && npcId.toLowerCase().contains("kael")) {
            return getSprite("portraits/kael_portrait.png");
        } else if (npcId != null && npcId.toLowerCase().contains("sylvan")) {
            return getSprite("portraits/sylvan_portrait.png");
        }
        return getSprite("portraits/engineer_portrait.png");
    }

    // --- Procedural Generation Methods ---

    private BufferedImage generateRinSheet() {
        // 8 cols x 10 rows of 32x32 frames
        BufferedImage sheet = new BufferedImage(256, 320, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 8; col++) {
                int fx = col * 32;
                int fy = row * 32;
                drawRinFrame(g, fx, fy, row, col);
            }
        }
        g.dispose();
        return sheet;
    }

    private void drawRinFrame(Graphics2D g, int x, int y, int row, int col) {
        int cx = x + 16;
        int cy = y + 26;

        // Scarf flutter offset
        int scarfDx = -6 - (col % 4) * 2;
        int scarfDy = -4 + (col % 3);

        // Body bobbing
        int bob = (row == 1 || row == 2) ? ((col % 2) * 2) : (col % 2);

        // Head
        g.setColor(new Color(245, 195, 150));
        g.fillRect(cx - 3, cy - 20 - bob, 6, 6);
        // Hair
        g.setColor(new Color(55, 38, 25));
        g.fillRect(cx - 4, cy - 22 - bob, 8, 4);
        // Cyan Eye
        g.setColor(new Color(0, 240, 255));
        g.fillRect(cx + 1, cy - 18 - bob, 2, 1);

        // Cyan Resonance Scarf (Animated)
        g.setColor(new Color(0, 240, 255));
        g.fillRect(cx - 4, cy - 14 - bob, 8, 2);
        g.fillRect(cx + scarfDx, cy - 13 - bob + scarfDy, 6, 2);

        // Orange Sunroot Jacket
        g.setColor(new Color(242, 128, 58));
        g.fillRect(cx - 4, cy - 12 - bob, 8, 7);

        // Golden Spark Gauntlet
        g.setColor(new Color(255, 215, 0));
        if (row == 6) { // Attack pose
            g.fillRect(cx + 4 + col * 2, cy - 14, 5, 4);
        } else if (row == 7) { // Mine pose
            g.fillRect(cx + 2, cy - 18 + col * 3, 4, 4);
        } else {
            g.fillRect(cx + 3, cy - 10 - bob, 4, 4);
        }

        // Trousers & Boots
        g.setColor(new Color(40, 35, 50));
        if (row == 1 || row == 2) { // Walk/Run stride
            int legStride = (col % 4) * 2 - 3;
            g.fillRect(cx - 4 - legStride, cy - 5, 3, 5);
            g.fillRect(cx + 1 + legStride, cy - 5, 3, 5);
        } else {
            g.fillRect(cx - 4, cy - 5, 3, 5);
            g.fillRect(cx + 1, cy - 5, 3, 5);
        }
    }

    private BufferedImage generatePipSheet() {
        // 4 cols x 5 rows of 16x16 frames
        BufferedImage sheet = new BufferedImage(64, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 4; c++) {
                int px = c * 16 + 8;
                int py = r * 16 + 8 + (c % 2) * 2;
                Color glow = (r == 4) ? new Color(100, 255, 140, 180) :
                             (r == 2) ? new Color(255, 60, 60, 200) : new Color(255, 235, 50, 180);
                g.setColor(glow);
                g.fillOval(px - 5, py - 5, 10, 10);
                g.setColor(Color.WHITE);
                g.fillOval(px - 2, py - 2, 4, 4);
            }
        }
        g.dispose();
        return sheet;
    }

    private BufferedImage generateEchoSheet() {
        BufferedImage rin = generateRinSheet();
        BufferedImage echo = new BufferedImage(rin.getWidth(), rin.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = echo.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g.drawImage(rin, 0, 0, null);
        g.setColor(new Color(0, 240, 255, 95));
        g.fillRect(0, 0, rin.getWidth(), rin.getHeight());
        g.dispose();
        return echo;
    }

    private BufferedImage generateNPCSheet() {
        // 8 cols x 18 rows of 32x32 frames (9 NPCs x 2 rows each: [IDLE/WORK], [SIT/SLEEP])
        BufferedImage sheet = new BufferedImage(256, 576, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();

        Color[] npcColors = {
            new Color(170, 90, 40),   // Kael Blacksmith
            new Color(60, 160, 80),   // Sylvan Botanist
            new Color(210, 140, 40),  // Rustrail Engineer
            new Color(200, 180, 100), // Desert Nomad
            new Color(80, 160, 220),  // Sky Pilot
            new Color(130, 135, 150), // Hollowdeep Miner
            new Color(180, 220, 240), // Arctic Researcher
            new Color(240, 60, 180),  // Neon Mechanic
            new Color(120, 80, 220)   // Moonkeeper Mage
        };

        for (int npc = 0; npc < 9; npc++) {
            Color cloak = npcColors[npc];
            for (int r = 0; r < 2; r++) {
                int row = npc * 2 + r;
                for (int c = 0; c < 8; c++) {
                    int fx = c * 32 + 16;
                    int fy = row * 32 + 26;

                    // Head & Face
                    g.setColor(new Color(230, 190, 150));
                    g.fillRect(fx - 3, fy - 18, 6, 6);
                    // Cloak / Uniform
                    g.setColor(cloak);
                    g.fillRect(fx - 4, fy - 12, 8, 8);
                    // Legs / Sitting
                    g.setColor(new Color(40, 40, 50));
                    if (r == 1 && c >= 4) { // Sleep
                        g.fillRect(fx - 8, fy - 4, 16, 4);
                    } else if (r == 1) { // Sit
                        g.fillRect(fx - 5, fy - 6, 10, 4);
                    } else { // Idle / Work
                        g.fillRect(fx - 4, fy - 4, 3, 4);
                        g.fillRect(fx + 1, fy - 4, 3, 4);
                    }
                }
            }
        }
        g.dispose();
        return sheet;
    }

    private BufferedImage generateMobsSheet() {
        // 4 cols x 12 rows of 32x32 frames (4 mobs x 3 rows each: IDLE, ATTACK, DEAD)
        BufferedImage sheet = new BufferedImage(128, 384, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();

        Color[] mobColors = {
            new Color(180, 30, 40),  // Drone
            new Color(110, 40, 160), // Shadow Creeper
            new Color(220, 80, 20),  // Magma Golem
            new Color(60, 20, 100)   // Void Stalker
        };

        for (int m = 0; m < 4; m++) {
            Color mc = mobColors[m];
            for (int r = 0; r < 3; r++) {
                int row = m * 3 + r;
                for (int c = 0; c < 4; c++) {
                    int x = c * 32 + 4;
                    int y = row * 32 + 4;
                    g.setColor(mc);
                    g.fillRoundRect(x, y, 24, 24, 6, 6);
                    // Eyes
                    g.setColor(Color.RED);
                    g.fillRect(x + 6, y + 8, 3, 3);
                    g.fillRect(x + 15, y + 8, 3, 3);
                }
            }
        }
        g.dispose();
        return sheet;
    }

    private BufferedImage generateWeaponSheet() {
        // 12 weapons of 24x24 pixels
        BufferedImage sheet = new BufferedImage(288, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        for (int i = 0; i < 12; i++) {
            int x = i * 24 + 4;
            // Blade
            g.setColor(new Color(210, 220, 235));
            g.fillRect(x + 8, 2, 4, 14);
            // Guard
            g.setColor(new Color(220, 180, 40));
            g.fillRect(x + 4, 16, 12, 2);
            // Grip
            g.setColor(new Color(80, 50, 30));
            g.fillRect(x + 9, 18, 2, 4);
        }
        g.dispose();
        return sheet;
    }

    private BufferedImage generateToolSheet() {
        // 10 tools of 16x16 pixels
        BufferedImage sheet = new BufferedImage(160, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        for (int i = 0; i < 10; i++) {
            int x = i * 16;
            // Handle
            g.setColor(new Color(120, 80, 40));
            g.drawLine(x + 2, 14, x + 12, 4);
            // Pickaxe Head
            g.setColor(new Color(180, 190, 200));
            g.fillRect(x + 9, 2, 5, 4);
        }
        g.dispose();
        return sheet;
    }

    private BufferedImage generateItemSheet() {
        // 16 items of 16x16 pixels
        BufferedImage sheet = new BufferedImage(256, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        for (int i = 0; i < 16; i++) {
            int x = i * 16 + 2;
            g.setColor(new Color(0, 240, 255));
            g.fillOval(x + 2, 2, 8, 8);
            g.setColor(Color.WHITE);
            g.drawOval(x + 2, 2, 8, 8);
        }
        g.dispose();
        return sheet;
    }

    private BufferedImage generateTerrainSheet() {
        // 8 variants x 16 block types of 16x16 pixels
        BufferedImage sheet = new BufferedImage(128, 256, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();

        BlockType[] types = BlockType.values();
        for (int r = 0; r < Math.min(16, types.length); r++) {
            BlockType b = types[r];
            for (int c = 0; c < 8; c++) {
                int x = c * 16;
                int y = r * 16;
                g.setColor(b.baseColor);
                g.fillRect(x, y, 16, 16);
                g.setColor(b.highlightColor);
                g.drawLine(x, y, x + 15, y);
                g.setColor(b.shadowColor);
                g.drawLine(x, y + 15, x + 15, y + 15);
            }
        }
        g.dispose();
        return sheet;
    }

    private BufferedImage generateEffectsSheet() {
        BufferedImage sheet = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(255, 120, 20, 200));
        g.fillOval(10, 10, 20, 20); // Ember
        g.setColor(new Color(40, 160, 255, 200));
        g.fillOval(50, 10, 20, 20); // Tide
        g.setColor(new Color(0, 240, 255, 220));
        g.fillOval(90, 10, 20, 20); // Echo
        g.dispose();
        return sheet;
    }

    private BufferedImage generatePortrait(Color cloakColor, String name) {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(20, 25, 38));
        g.fillRect(0, 0, 32, 32);
        g.setColor(new Color(235, 195, 155));
        g.fillRect(10, 8, 12, 12);
        g.setColor(cloakColor);
        g.fillRect(6, 20, 20, 12);
        g.setColor(new Color(0, 240, 255));
        g.drawRect(0, 0, 31, 31);
        g.dispose();
        return img;
    }

    private BufferedImage generateUISheet() {
        BufferedImage sheet = new BufferedImage(128, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        // Hearts
        g.setColor(Color.RED);
        g.fillOval(2, 2, 6, 6);
        g.fillOval(8, 2, 6, 6);
        g.fillPolygon(new int[]{2, 14, 8}, new int[]{6, 6, 12}, 3);
        // Spark Meter
        g.setColor(new Color(0, 240, 255));
        g.fillRect(20, 2, 24, 8);
        g.dispose();
        return sheet;
    }

    private BufferedImage generateRinSingle(boolean run) {
        BufferedImage img = new BufferedImage(16, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        drawRinFrame(g, -8, -12, run ? 2 : 0, 0);
        g.dispose();
        return img;
    }

    private BufferedImage generatePipSingle() {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(255, 235, 50));
        g.fillOval(1, 1, 8, 8);
        g.setColor(Color.WHITE);
        g.fillOval(3, 3, 4, 4);
        g.dispose();
        return img;
    }

    private BufferedImage generateEchoSingle() {
        BufferedImage rin = generateRinSingle(false);
        BufferedImage echo = new BufferedImage(16, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = echo.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g.drawImage(rin, 0, 0, null);
        g.setColor(new Color(0, 240, 255, 90));
        g.fillRect(0, 0, 16, 20);
        g.dispose();
        return echo;
    }

    private BufferedImage generateNPCSingle(Color cloak, String initial) {
        BufferedImage img = new BufferedImage(16, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(230, 190, 150));
        g.fillRect(5, 4, 6, 5);
        g.setColor(cloak);
        g.fillRect(4, 9, 8, 7);
        g.dispose();
        return img;
    }

    private BufferedImage generateMobSingle(Color color) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRoundRect(2, 2, 12, 12, 4, 4);
        g.setColor(Color.RED);
        g.fillRect(4, 5, 2, 2);
        g.fillRect(10, 5, 2, 2);
        g.dispose();
        return img;
    }

    private BufferedImage generateTileSingle(Color base, Color highlight) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(base);
        g.fillRect(0, 0, 16, 16);
        g.setColor(highlight);
        g.fillRect(2, 2, 4, 4);
        g.dispose();
        return img;
    }

    private BufferedImage generateWorldMapPreview() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(65, 130, 55));
        g.fillRect(0, 0, 64, 64);
        g.setColor(new Color(30, 80, 180));
        g.fillRect(48, 0, 16, 64);
        g.dispose();
        return img;
    }

    private BufferedImage generateMinimapLegend() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(20, 25, 35));
        g.fillRect(0, 0, 32, 32);
        g.dispose();
        return img;
    }

    private BufferedImage generateBiomesMap() {
        BufferedImage img = new BufferedImage(48, 48, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(70, 150, 70));
        g.fillRect(0, 0, 48, 48);
        g.dispose();
        return img;
    }

    private BufferedImage generateControlsDiagram() {
        BufferedImage img = new BufferedImage(64, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(25, 30, 45));
        g.fillRect(0, 0, 64, 32);
        g.dispose();
        return img;
    }

    private BufferedImage generateSparkIcon() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0, 240, 255));
        g.fillPolygon(new int[]{8, 14, 8, 2}, new int[]{1, 8, 15, 8}, 4);
        g.dispose();
        return img;
    }

    private BufferedImage generateTutorialBanner() {
        BufferedImage img = new BufferedImage(128, 24, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(15, 20, 35));
        g.fillRect(0, 0, 128, 24);
        g.dispose();
        return img;
    }

    public int getCachedSpriteCount() {
        return spriteCache.size();
    }

    public static void main(String[] args) {
        AssetManager am = new AssetManager(new File("assets"));
        System.out.println("AssetManager initialized. Cached sprites: " + am.getCachedSpriteCount());
    }
}
