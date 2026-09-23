package com.echobound.assets;

import com.echobound.entity.mob.MobType;
import com.echobound.sandbox.BlockType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Part 13 — Real Pixel Asset Pipeline
 *
 * Assembles the downloaded CC0/CC-BY pixel-art packs into game-ready sprite sheets
 * and writes them to assets/processed/.  Falls back gracefully when source files are
 * missing so the game can always start.
 *
 * Source packs consumed (all CC0 — no attribution required):
 *  - Kenney "Roguelike/RPG Pack" (CC0 mirror, opengameart.org/content/roguelikerpg-pack-1700-tiles)
 *    →  assets/external/tiles/roguelike_sheet.png — terrain (grass/dirt/stone/sand/water),
 *       trees, flowers, berries, brick/plank floors. 16×16 tiles, 1px margin (17px stride).
 *  - Other slots below (characters/mobs/items/weapons/tools/ui) fall back gracefully to the
 *    legacy procedurally-generated tier (see AssetManager) when their source file is absent —
 *    that is expected right now, not a bug; only terrain has been re-sourced so far.
 */
public class RealPixelAssetPipeline {

    // ── internal constants ────────────────────────────────────────────────────
    private static final int CHAR_FRAME_W   = 32;
    private static final int CHAR_FRAME_H   = 32;
    private static final int TILE_SIZE      = 16;
    private static final int ITEM_SIZE      = 16;
    private static final int WEAPON_W       = 24;
    private static final int WEAPON_H       = 24;
    private static final int NPC_FRAME_W    = 32;
    private static final int NPC_FRAME_H    = 32;
    // Must match AssetManager.createMobAnimationController()'s SpriteSheet(sheetImg, 32, 32).
    private static final int MOB_FRAME_W    = 32;
    private static final int MOB_FRAME_H    = 32;

    // Animation row mapping for rin_sheet (rows 0-9, 8 cols wide = 256×320)
    // Row 0 = IDLE (4 frames), Row 1 = WALK (6), Row 2 = RUN (8),
    // Row 3 = JUMP/FALL/DJUMP (8), Row 4 = DASH (4), Row 5 = GLIDE (4),
    // Row 6 = ATTACK (6), Row 7 = MINE (4), Row 8 = CAST (6), Row 9 = HURT/DEAD (8)
    private static final int RIN_SHEET_COLS = 8;
    private static final int RIN_SHEET_ROWS = 10;

    private final File externalDir;
    private final File processedDir;

    private final List<String> processingLog = new ArrayList<>();
    private int sheetsBuilt = 0;

    public RealPixelAssetPipeline(File assetsRoot) {
        this.externalDir = new File(assetsRoot, "external");
        this.processedDir = new File(assetsRoot, "processed");
        mkdirs(processedDir,
               new File(processedDir, "characters"),
               new File(processedDir, "tiles"),
               new File(processedDir, "items"),
               new File(processedDir, "weapons"),
               new File(processedDir, "mobs"),
               new File(processedDir, "npcs"),
               new File(processedDir, "effects"),
               new File(processedDir, "tools"));
    }

    private void mkdirs(File... dirs) {
        for (File d : dirs) d.mkdirs();
    }

    // ── Public entry point ────────────────────────────────────────────────────

    /**
     * Runs the full pipeline.  Each step writes a PNG to processed/ only if the
     * source files are available.  Returns the number of sheets successfully built.
     */
    public int buildAll() {
        buildRinSheet();
        buildNPCSheet();
        buildMobSheet();
        buildTerrainSheet();
        buildItemSheet();
        buildWeaponSheet();
        buildToolSheet();
        buildEffectsSheet();
        log("Pipeline complete — " + sheetsBuilt + " sheet(s) built from real assets.");
        return sheetsBuilt;
    }

    public List<String> getProcessingLog() { return Collections.unmodifiableList(processingLog); }
    public int getSheetsBuilt()            { return sheetsBuilt; }
    public File getProcessedDir()          { return processedDir; }

    // ── Step 1: Rin / Player Character sheet (256×320) ───────────────────────

    private void buildRinSheet() {
        File ninjaDir = new File(externalDir, "characters/ninja_frames");
        if (!ninjaDir.isDirectory()) {
            log("[SKIP] rin_sheet — ninja_frames directory not found");
            return;
        }

        // Expected frame names for each animation row
        String[][] frameSets = {
            // Row 0: IDLE  (4 frames)
            {"idle_0.png","idle_1.png","idle_2.png","idle_3.png",null,null,null,null},
            // Row 1: WALK  (6 frames, reuse idle + run)
            {"idle_0.png","run_0.png","idle_1.png","run_1.png","idle_2.png","run_2.png",null,null},
            // Row 2: RUN   (6 frames → padded to 8)
            {"run_0.png","run_1.png","run_2.png","run_3.png","run_4.png","run_5.png","run_0.png","run_1.png"},
            // Row 3: JUMP/FALL/DJUMP (use jump frames)
            {"jump_0.png","jump_1.png","jump_2.png","jump_3.png","jump_0.png","jump_1.png","jump_2.png","jump_3.png"},
            // Row 4: DASH  (4 frames → use run fast)
            {"run_2.png","run_3.png","run_4.png","run_5.png",null,null,null,null},
            // Row 5: GLIDE (4 frames → use swim)
            {"swim_0.png","swim_1.png","swim_2.png","swim_3.png",null,null,null,null},
            // Row 6: ATTACK (3 frames → use attack)
            {"attack_0.png","attack_1.png","attack_2.png","attack_0.png","attack_1.png","attack_2.png",null,null},
            // Row 7: MINE  (4 frames → use x frames)
            {"x_0.png","x_1.png","x_2.png","x_3.png",null,null,null,null},
            // Row 8: CAST  (6 frames → swim + x mix)
            {"swim_4.png","swim_5.png","x_0.png","x_1.png","x_2.png","x_3.png",null,null},
            // Row 9: HURT/DEAD (use x + jump reversed)
            {"x_0.png","x_1.png","x_2.png","jump_3.png","jump_2.png","jump_1.png","jump_0.png",null}
        };

        BufferedImage sheet = new BufferedImage(
            RIN_SHEET_COLS * CHAR_FRAME_W, RIN_SHEET_ROWS * CHAR_FRAME_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        boolean anyFrame = false;
        for (int row = 0; row < RIN_SHEET_ROWS; row++) {
            String[] frames = frameSets[row];
            for (int col = 0; col < RIN_SHEET_COLS; col++) {
                if (frames[col] == null) continue;
                File f = new File(ninjaDir, frames[col]);
                if (!f.exists()) continue;
                try {
                    BufferedImage src = ImageIO.read(f);
                    if (src != null) {
                        g.drawImage(src, col * CHAR_FRAME_W, row * CHAR_FRAME_H, CHAR_FRAME_W, CHAR_FRAME_H, null);
                        anyFrame = true;
                    }
                } catch (IOException ignored) {}
            }
        }
        g.dispose();

        if (anyFrame) {
            write(sheet, new File(processedDir, "characters/rin_sheet.png"), "rin_sheet");
        } else {
            log("[SKIP] rin_sheet — no ninja frames could be loaded");
        }
    }

    // ── Step 2: NPC Sheet from Kenney roguelike characters (256×576) ─────────

    private void buildNPCSheet() {
        File src = new File(externalDir, "characters/kenney_roguelike_characters.png");
        if (!src.exists()) {
            log("[SKIP] npc_sheet — kenney_roguelike_characters.png not found");
            return;
        }
        try {
            BufferedImage source = ImageIO.read(src);
            if (source == null) { log("[FAIL] npc_sheet — could not read source"); return; }

            // Kenney roguelike characters: 16×16 sprites arranged in a grid
            int srcTileW = 16, srcTileH = 16;
            int srcCols  = source.getWidth() / srcTileW;
            int srcRows  = source.getHeight() / srcTileH;
            if (srcCols == 0 || srcRows == 0) { log("[FAIL] npc_sheet — source too small"); return; }

            // Build 9 NPC × 2 rows (IDLE+WORK row, SIT+SLEEP row) at 32×32
            // Each NPC gets a unique character from the Kenney sheet
            int npcCount = Math.min(9, srcCols * srcRows);
            BufferedImage npcSheet = new BufferedImage(256, 576, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = npcSheet.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            for (int npc = 0; npc < npcCount; npc++) {
                // Source character tile from kenney sheet
                int srcCol = npc % srcCols;
                int srcRow = npc / srcCols;
                if (srcRow >= srcRows) break;
                BufferedImage charTile = source.getSubimage(
                    srcCol * srcTileW, srcRow * srcTileH, srcTileW, srcTileH);

                // Destination: npc*2 rows at 32×32, 8 cols
                int dstRow0 = npc * 2;       // IDLE / WORK row
                int dstRow1 = npc * 2 + 1;   // SIT / SLEEP row
                for (int c = 0; c < 8; c++) {
                    // IDLE/WORK row: draw char tile scaled to 32×32
                    g.drawImage(charTile, c * NPC_FRAME_W, dstRow0 * NPC_FRAME_H,
                                NPC_FRAME_W, NPC_FRAME_H, null);
                    // SIT/SLEEP row: slightly darker tint
                    g.drawImage(charTile, c * NPC_FRAME_W, dstRow1 * NPC_FRAME_H,
                                NPC_FRAME_W, NPC_FRAME_H, null);
                    if (c >= 4) { // SIT shading
                        g.setColor(new Color(0, 0, 0, 60));
                        g.fillRect(c * NPC_FRAME_W, dstRow1 * NPC_FRAME_H, NPC_FRAME_W, NPC_FRAME_H);
                    }
                }
            }
            g.dispose();
            write(npcSheet, new File(processedDir, "npcs/npc_sheet.png"), "npc_sheet");
        } catch (IOException e) {
            log("[FAIL] npc_sheet — " + e.getMessage());
        }
    }

    // ── Step 3: Mob Sheet from the CC0 "Tiny Creatures" pack (128×N) ─────────

    /**
     * Source is the CC0, Kenney-collaborated "Tiny Creatures" pack (opengameart.org/content/
     * tiny-creatures): a single 160×288 tilemap, 16×16 tiles on a tight 10×18 grid, no margin
     * (see Tilesheet.txt in the pack). Coordinates were picked by hand off a rendered, labeled
     * contact sheet of the actual grid (col,row) — each one visually confirmed to be a real
     * creature art match for its MobType, not a guess. The pack ships one pose per creature
     * (no separate walk/attack/death frames), so WALK/ATTACK/DEAD are synthesized from the
     * single real base sprite via flip/offset/tint — the same technique the old fully-
     * procedural generateMobsSheet() used for its rounded-rectangle placeholders, just now
     * driven by real pixel art instead of a flat-fill shape.
     */
    private static final int CREATURE_TILE = 16;

    private static int[] mobSourceTile(MobType type) {
        return switch (type) {
            case CORRUPTED_DRONE      -> new int[]{8, 1};  // armored robot/knight figure
            case SHADOW_CREEPER       -> new int[]{4, 0};  // dark hooded shadow figure
            case MAGMA_GOLEM          -> new int[]{5, 4};  // orange fire elemental
            case VOID_STALKER         -> new int[]{7, 12}; // gray golem, tinted dark purple
            case WOODLAND_FOX         -> new int[]{8, 16}; // orange fox
            case CAVE_GLOWBAT         -> new int[]{6, 13}; // gray bat
            case EMBER_CAT            -> new int[]{6, 15}; // orange lion (feline, fire-colored)
            case MOSS_TURTLE_CREATURE -> new int[]{9, 14}; // green turtle
            case SKY_CLOUDBIRD        -> new int[]{7, 11}; // gray owl, tinted sky-blue
            case FIELD_RAT            -> new int[]{4, 13}; // small brown rabbit/critter
            case MARSH_BEETLE         -> new int[]{5, 14}; // orange scorpion, tinted green
            case DRAGON               -> new int[]{3, 3};  // red winged dragon
            case PHOENIX_CREATURE     -> new int[]{3, 10}; // fire-colored bird, tinted warmer
            case UNICORN_CREATURE     -> new int[]{1, 5};  // white horned unicorn
        };
    }

    private static Color mobRealTint(MobType type) {
        return switch (type) {
            case CORRUPTED_DRONE  -> new Color(120, 190, 255, 55);
            case VOID_STALKER     -> new Color(90, 20, 140, 80);
            case SKY_CLOUDBIRD    -> new Color(150, 205, 255, 60);
            case MARSH_BEETLE     -> new Color(60, 150, 60, 70);
            case PHOENIX_CREATURE -> new Color(255, 110, 20, 55);
            default               -> null;
        };
    }

    private void buildMobSheet() {
        File src = new File(externalDir, "mobs/tiny_creatures.png");
        if (!src.exists()) {
            log("[SKIP] mobs_sheet — tiny_creatures.png not found");
            return;
        }
        try {
            BufferedImage source = ImageIO.read(src);
            if (source == null) { log("[FAIL] mobs_sheet — unreadable"); return; }
            int srcCols = source.getWidth() / CREATURE_TILE;
            int srcRows = source.getHeight() / CREATURE_TILE;

            MobType[] types = MobType.values();
            int rowsPerMob = 4; // IDLE, WALK, ATTACK, DEAD — see AssetManager.createMobAnimationController()
            BufferedImage mobSheet = new BufferedImage(
                4 * MOB_FRAME_W, types.length * rowsPerMob * MOB_FRAME_H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = mobSheet.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            for (int m = 0; m < types.length; m++) {
                int[] rc = mobSourceTile(types[m]);
                int col = Math.min(rc[0], srcCols - 1);
                int row = Math.min(rc[1], srcRows - 1);
                BufferedImage base = source.getSubimage(
                    col * CREATURE_TILE, row * CREATURE_TILE, CREATURE_TILE, CREATURE_TILE);
                Color tint = mobRealTint(types[m]);

                for (int frame = 0; frame < 4; frame++) {
                    // IDLE: static real sprite, no distortion.
                    drawMobFrame(g, base, frame, m * rowsPerMob, tint, false, false, 0);
                    // WALK: alternating horizontal flip + vertical bob simulates a stride
                    // from a single source pose.
                    drawMobFrame(g, base, frame, m * rowsPerMob + 1, tint, frame % 2 == 1, false,
                                 frame % 2 == 0 ? 0 : 2);
                    // ATTACK: drawn slightly larger (lunging forward) with a red flash overlay.
                    drawMobFrame(g, base, frame, m * rowsPerMob + 2, tint, false, false, -2);
                    Color attackFlash = new Color(255, 0, 0, 40);
                    int ay = (m * rowsPerMob + 2) * MOB_FRAME_H;
                    g.setColor(attackFlash);
                    g.fillRect(frame * MOB_FRAME_W, ay, MOB_FRAME_W, MOB_FRAME_H);
                    // DEAD: flipped upside-down (fallen) with a grey desaturating overlay.
                    drawMobFrame(g, base, frame, m * rowsPerMob + 3, tint, false, true, 6);
                    Color deadFade = new Color(160, 160, 160, 130);
                    int dy = (m * rowsPerMob + 3) * MOB_FRAME_H;
                    g.setColor(deadFade);
                    g.fillRect(frame * MOB_FRAME_W, dy, MOB_FRAME_W, MOB_FRAME_H);
                }
            }
            g.dispose();
            write(mobSheet, new File(processedDir, "mobs/mobs_sheet.png"), "mobs_sheet");
        } catch (IOException e) {
            log("[FAIL] mobs_sheet — " + e.getMessage());
        }
    }

    /** Draws one real-art mob frame into the sheet, applying an optional tint wash and
     *  flip/offset used to synthesize WALK/ATTACK/DEAD from the pack's single base pose. */
    private void drawMobFrame(Graphics2D g, BufferedImage base, int frame, int destRow,
                               Color tint, boolean flipH, boolean flipV, int yOffset) {
        int destX = frame * MOB_FRAME_W;
        int cellY = destRow * MOB_FRAME_H;
        int destY = cellY + yOffset;
        int sx1 = flipH ? CREATURE_TILE : 0;
        int sx2 = flipH ? 0 : CREATURE_TILE;
        int sy1 = flipV ? CREATURE_TILE : 0;
        int sy2 = flipV ? 0 : CREATURE_TILE;
        g.drawImage(base, destX, destY, destX + MOB_FRAME_W, destY + MOB_FRAME_H,
                    sx1, sy1, sx2, sy2, null);
        if (tint != null) {
            g.setColor(tint);
            g.fillRect(destX, cellY, MOB_FRAME_W, MOB_FRAME_H);
        }
    }

    // ── Step 4: Terrain Sheet from the CC0 Roguelike/RPG pack (128×N) ─────────

    /**
     * Source is Kenney's classic "Roguelike/RPG Pack" (mirrored CC0 on opengameart.org),
     * a single 968×526 sheet: 16×16 tile content on a 17px grid stride (1px margin between
     * tiles — see Spritesheet/spritesheetInfo.txt in the pack). Coordinates below were
     * picked by hand off a rendered, labeled contact sheet of the actual grid (col,row),
     * not guessed — each one was visually confirmed to be the tile it's used for. Types with
     * no good real match in this pack (CACTUS, SNOW, ore glows) reuse the closest tile and
     * get a translucent tint, the same technique buildMobSheet() uses for mob archetypes.
     */
    private static final int ROGUE_TILE   = 16;
    private static final int ROGUE_STRIDE = 17; // 16px content + 1px margin

    private static int[][] terrainSourceTiles(BlockType type) {
        return switch (type) {
            case GRASS       -> new int[][]{{5, 0}, {5, 1}};
            case DIRT        -> new int[][]{{6, 0}, {6, 1}};
            case STONE       -> new int[][]{{7, 0}, {7, 1}};
            case SPARK_ORE   -> new int[][]{{9, 1}};
            case CRYSTAL_NODE-> new int[][]{{9, 1}};
            case WOOD_LOG    -> new int[][]{{12, 11}, {13, 11}, {14, 11}};
            case LEAVES      -> new int[][]{{12, 9}, {13, 9}, {14, 9}, {12, 10}, {13, 10}, {14, 10}};
            case WATER       -> new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}};
            case WOOD_PLANKS -> new int[][]{{8, 2}, {8, 3}, {8, 4}};
            case STONE_BRICK -> new int[][]{{6, 2}, {7, 2}};
            case SPARK_LAMP  -> new int[][]{{14, 7}, {13, 7}};
            case WORKBENCH   -> new int[][]{{8, 2}};
            case BARRICADE   -> new int[][]{{6, 0}};
            case SAND        -> new int[][]{{8, 0}, {8, 1}};
            case SNOW        -> new int[][]{{7, 0}, {7, 1}};
            case TALL_GRASS  -> new int[][]{{5, 0}, {5, 1}};
            // Real flower-on-grass and berry-on-grass art — no tint needed, these are direct hits.
            case WILDFLOWER  -> new int[][]{{0, 9}, {1, 9}, {2, 9}, {3, 9}, {4, 9}};
            case BERRY_BUSH  -> new int[][]{{0, 6}, {1, 6}, {2, 6}, {3, 6}, {4, 6}, {0, 7}, {1, 7}};
            case BOULDER     -> new int[][]{{5, 13}, {7, 13}, {5, 14}, {7, 14}};
            case CACTUS      -> new int[][]{{8, 0}};
            case RUINS_BRICK -> new int[][]{{5, 2}, {6, 2}};
            default          -> new int[][]{{0, 0}}; // AIR / unused — never actually rendered
        };
    }

    /**
     * Translucent color wash applied over a reused ground tile for block types the source
     * pack has no dedicated art for. Alpha is kept low enough that the underlying tile's
     * shading/texture still reads through — this is a tint, not a flat fill.
     */
    private static Color terrainTint(BlockType type) {
        return switch (type) {
            case SPARK_ORE    -> new Color(0, 200, 230, 110);
            case CRYSTAL_NODE -> new Color(190, 80, 230, 120);
            case SPARK_LAMP   -> new Color(0, 220, 255, 70);
            case WORKBENCH    -> new Color(230, 175, 50, 90);
            case BARRICADE    -> new Color(140, 70, 40, 110);
            case SNOW         -> new Color(255, 255, 255, 165);
            case TALL_GRASS   -> new Color(70, 160, 70, 80);
            case BOULDER      -> new Color(130, 128, 122, 90);
            case CACTUS       -> new Color(50, 150, 90, 150);
            default           -> null;
        };
    }

    private void buildTerrainSheet() {
        File src = new File(externalDir, "tiles/roguelike_sheet.png");
        if (!src.exists()) {
            log("[SKIP] terrain_sheet — roguelike_sheet.png not found");
            return;
        }
        try {
            BufferedImage source = ImageIO.read(src);
            if (source == null) { log("[FAIL] terrain_sheet — unreadable"); return; }

            // One row per BlockType ordinal, 8 variant columns each. Sized off
            // BlockType.values().length (not a hardcoded constant) so adding new block types
            // automatically gets its own row instead of aliasing an existing one.
            BlockType[] blockTypes = BlockType.values();
            int sheetH = blockTypes.length * TILE_SIZE;
            BufferedImage terrainSheet = new BufferedImage(128, sheetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = terrainSheet.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            for (int row = 0; row < blockTypes.length; row++) {
                BlockType type = blockTypes[row];
                int[][] coords = terrainSourceTiles(type);
                Color tint = terrainTint(type);

                for (int col = 0; col < 8; col++) {
                    int[] rc = coords[col % coords.length];
                    try {
                        BufferedImage tile = source.getSubimage(
                            rc[0] * ROGUE_STRIDE, rc[1] * ROGUE_STRIDE, ROGUE_TILE, ROGUE_TILE);
                        int dx = col * TILE_SIZE, dy = row * TILE_SIZE;
                        g.drawImage(tile, dx, dy, TILE_SIZE, TILE_SIZE, null);
                        if (tint != null) {
                            g.setColor(tint);
                            g.fillRect(dx, dy, TILE_SIZE, TILE_SIZE);
                        }
                    } catch (Exception ignored) {}
                }
            }
            g.dispose();
            write(terrainSheet, new File(processedDir, "tiles/terrain_sheet.png"), "terrain_sheet");
        } catch (IOException e) {
            log("[FAIL] terrain_sheet — " + e.getMessage());
        }
    }

    // ── Step 5: Item Sheet from Kenney micro items (256×16) ──────────────────

    private void buildItemSheet() {
        File src = new File(externalDir, "items/kenney_micro_items.png");
        if (!src.exists()) {
            log("[SKIP] item_sheet — kenney_micro_items.png not found");
            return;
        }
        try {
            BufferedImage source = ImageIO.read(src);
            if (source == null) { log("[FAIL] item_sheet — unreadable"); return; }

            // Scale/copy source as item sheet (standardize to 256×16, 16-item strip)
            int srcTileW = Math.max(1, source.getWidth() / 16);
            int srcTileH = source.getHeight();
            BufferedImage itemSheet = new BufferedImage(256, ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = itemSheet.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            int srcCols = source.getWidth() / srcTileW;
            for (int i = 0; i < 16; i++) {
                int srcCol = i % Math.max(1, srcCols);
                try {
                    BufferedImage tile = source.getSubimage(srcCol * srcTileW, 0, srcTileW, srcTileH);
                    g.drawImage(tile, i * ITEM_SIZE, 0, ITEM_SIZE, ITEM_SIZE, null);
                } catch (Exception ignored) {}
            }
            g.dispose();
            write(itemSheet, new File(processedDir, "items/item_sheet.png"), "item_sheet");
        } catch (IOException e) {
            log("[FAIL] item_sheet — " + e.getMessage());
        }
    }

    // ── Step 6: Weapon Sheet enhanced from Kenney platformer (288×24) ────────

    private void buildWeaponSheet() {
        File src = new File(externalDir, "tiles/kenney_platformer_tiles.png");
        if (!src.exists()) {
            log("[SKIP] weapon_sheet — source not found");
            return;
        }
        try {
            BufferedImage source = ImageIO.read(src);
            if (source == null) return;

            // Source grid is 18×18 (see PLATFORMER_SRC_TILE) — using 16 here previously sliced
            // across true tile boundaries and smeared adjacent art together.
            int srcW = 18, srcH = 18; // dead path: source file no longer exists, kept only for graceful SKIP
            int srcCols = source.getWidth() / srcW;
            // Pull 12 "weapon-like" tiles from the platformer sheet and scale to 24×24
            BufferedImage weaponSheet = new BufferedImage(288, WEAPON_H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = weaponSheet.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            for (int i = 0; i < 12; i++) {
                int srcCol = (i * 3) % Math.max(1, srcCols);
                try {
                    BufferedImage tile = source.getSubimage(srcCol * srcW, 0, srcW, srcH);
                    g.drawImage(tile, i * WEAPON_W, 0, WEAPON_W, WEAPON_H, null);
                } catch (Exception ignored) {}
            }
            g.dispose();
            write(weaponSheet, new File(processedDir, "weapons/weapon_sheet.png"), "weapon_sheet");
        } catch (IOException e) {
            log("[FAIL] weapon_sheet — " + e.getMessage());
        }
    }

    // ── Step 7: Tool Sheet (160×16) from kenney platformer ───────────────────

    private void buildToolSheet() {
        File src = new File(externalDir, "tiles/kenney_platformer_tiles.png");
        if (!src.exists()) {
            log("[SKIP] tool_sheet — source not found");
            return;
        }
        try {
            BufferedImage source = ImageIO.read(src);
            if (source == null) return;

            // Source grid is 18×18 (see PLATFORMER_SRC_TILE), not 16×16.
            int srcW = 18, srcH = 18; // dead path: source file no longer exists, kept only for graceful SKIP
            int srcCols = source.getWidth() / srcW;
            BufferedImage toolSheet = new BufferedImage(160, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = toolSheet.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            for (int i = 0; i < 10; i++) {
                int srcCol = (i * 5 + 1) % Math.max(1, srcCols);
                try {
                    BufferedImage tile = source.getSubimage(srcCol * srcW, 0, srcW, srcH);
                    g.drawImage(tile, i * TILE_SIZE, 0, TILE_SIZE, TILE_SIZE, null);
                } catch (Exception ignored) {}
            }
            g.dispose();
            write(toolSheet, new File(processedDir, "tools/tool_sheet.png"), "tool_sheet");
        } catch (IOException e) {
            log("[FAIL] tool_sheet — " + e.getMessage());
        }
    }

    // ── Step 8: Effects Sheet from Kenney UI tiles (128×128) ─────────────────

    private void buildEffectsSheet() {
        File uiDir = new File(externalDir, "ui");
        if (!uiDir.isDirectory()) {
            log("[SKIP] effects_sheet — ui tile dir not found");
            return;
        }
        // Use first 16 UI tiles arranged as 128×128 effects atlas
        BufferedImage effectSheet = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = effectSheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int placed = 0;
        for (int i = 0; i <= 15 && placed < 16; i++) {
            File tileFile = new File(uiDir, String.format("tile_%04d.png", i));
            if (!tileFile.exists()) continue;
            try {
                BufferedImage tile = ImageIO.read(tileFile);
                if (tile == null) continue;
                int row = placed / 4;
                int col = placed % 4;
                g.drawImage(tile, col * 32, row * 32, 32, 32, null);
                placed++;
            } catch (IOException ignored) {}
        }
        g.dispose();
        if (placed > 0) {
            write(effectSheet, new File(processedDir, "effects/effects_sheet.png"), "effects_sheet");
        } else {
            log("[SKIP] effects_sheet — no ui tiles readable");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void write(BufferedImage img, File dest, String name) {
        try {
            ImageIO.write(img, "png", dest);
            sheetsBuilt++;
            log("[OK]   " + name + " → " + dest.getPath()
                + " (" + img.getWidth() + "×" + img.getHeight() + ")");
        } catch (IOException e) {
            log("[FAIL] " + name + " write error: " + e.getMessage());
        }
    }

    private void log(String msg) {
        processingLog.add(msg);
    }

    // ── CLI entry point ───────────────────────────────────────────────────────

    public static void main(String[] args) {
        File root = (args.length > 0) ? new File(args[0]) : new File("assets");
        System.out.println("=== ECHOBOUND Part 13 — Real Pixel Asset Pipeline ===");
        System.out.println("Assets root: " + root.getAbsolutePath());
        RealPixelAssetPipeline pipeline = new RealPixelAssetPipeline(root);
        int built = pipeline.buildAll();
        pipeline.getProcessingLog().forEach(System.out::println);
        System.out.println("Sheets built: " + built);
    }
}
