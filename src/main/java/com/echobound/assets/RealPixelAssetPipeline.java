package com.echobound.assets;

import com.echobound.entity.mob.MobType;
import com.echobound.items.ItemCategory;
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
               new File(processedDir, "tools"),
               new File(processedDir, "ui"));
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
        buildHeartIcon();
        buildCategoryIconsSheet();
        log("Pipeline complete — " + sheetsBuilt + " sheet(s) built from real assets.");
        return sheetsBuilt;
    }

    public List<String> getProcessingLog() { return Collections.unmodifiableList(processingLog); }
    public int getSheetsBuilt()            { return sheetsBuilt; }
    public File getProcessedDir()          { return processedDir; }

    // ── Step 1: Rin / Player Character sheet (256×320) ───────────────────────

    /**
     * Source is the CC0 "Hero Spritesheets (Ars Notoria)" pack (opengameart.org/content/
     * hero-spritesheets-ars-notoria, by Balmer): a single 368×200 sheet, 46×50 frames on an
     * 8×4 grid, genuine 16-bit pixel art (DB32 palette). Confirmed visually off a labeled
     * contact sheet, not guessed: row 0 = stand/lunge-punch, row 1 = airborne punch, row 2 =
     * knockdown/low-punch, row 3 = an 8-frame run cycle. The base character has no sword drawn
     * in — it's a bare-fisted fighter — so ATTACK/MINE/CAST reuse its real punch/lunge frames
     * rather than a weapon swing; combat damage is unaffected, only the on-screen animation.
     * As with buildMobSheet()/the ninja-based rin_sheet before it, real frames are
     * repeated/reordered (never invented) to fill the richer row layout
     * AssetManager.createRinAnimationController() expects.
     */
    private static final int HERO_FRAME_W = 46;
    private static final int HERO_FRAME_H = 50;

    private void buildRinSheet() {
        File src = new File(externalDir, "characters/hero_sheet.png");
        if (!src.exists()) {
            log("[SKIP] rin_sheet — hero_sheet.png not found");
            return;
        }
        try {
            BufferedImage source = ImageIO.read(src);
            if (source == null) { log("[FAIL] rin_sheet — unreadable"); return; }

            // Per destination row: source (col, row) pairs into the 8×4 hero grid.
            int[][][] frameSets = {
                { {0,0}, {0,0}, {0,0}, {0,0} },                                   // 0 IDLE (4) — static stand pose
                { {0,3}, {1,3}, {2,3}, {3,3}, {4,3}, {5,3} },                     // 1 WALK (6) — first 6 of the run cycle
                { {0,3}, {1,3}, {2,3}, {3,3}, {4,3}, {5,3}, {6,3}, {7,3} },       // 2 RUN (8) — full real run cycle
                { {1,1}, {2,1}, {3,1},                                            // 3 JUMP(0-2) — crouch → airborne
                  {4,1}, {5,1},                                                    //   FALL(3-4) — descending
                  {6,0}, {7,0}, {2,1} },                                          //   DJUMP(5-7) — leg-raised sprint reuse
                { {1,3}, {3,3}, {5,3}, {7,3} },                                   // 4 DASH (4) — alternating run frames
                { {0,2}, {1,2}, {0,1}, {0,2} },                                   // 5 GLIDE (4) — spread-limb falling poses
                { {2,0}, {3,0}, {4,0}, {5,0}, {4,0}, {2,0} },                     // 6 ATTACK (6) — real punch/lunge sequence
                { {2,2}, {3,2}, {4,2}, {5,2} },                                   // 7 MINE (4) — low punch/lunge variant
                { {2,1}, {3,1}, {4,1}, {5,1}, {3,1}, {2,1} },                     // 8 CAST (6) — airborne punch, raised-arm feel
                { {0,2}, {1,2}, {0,2},                                            // 9 HURT(0-2) — knockdown hit poses
                  {1,2}, {1,2}, {1,2}, {1,2}, {1,2} },                            //   DEAD(3-7) — lying-down pose held
            };

            BufferedImage sheet = new BufferedImage(
                RIN_SHEET_COLS * CHAR_FRAME_W, RIN_SHEET_ROWS * CHAR_FRAME_H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = sheet.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            for (int row = 0; row < frameSets.length; row++) {
                int[][] frames = frameSets[row];
                for (int col = 0; col < frames.length; col++) {
                    int srcCol = frames[col][0];
                    int srcRow = frames[col][1];
                    BufferedImage tile = source.getSubimage(
                        srcCol * HERO_FRAME_W, srcRow * HERO_FRAME_H, HERO_FRAME_W, HERO_FRAME_H);
                    g.drawImage(tile, col * CHAR_FRAME_W, row * CHAR_FRAME_H, CHAR_FRAME_W, CHAR_FRAME_H, null);
                }
            }
            g.dispose();
            write(sheet, new File(processedDir, "characters/rin_sheet.png"), "rin_sheet");
        } catch (IOException e) {
            log("[FAIL] rin_sheet — " + e.getMessage());
        }
    }

    // ── Step 2: NPC Sheet from "32x32 RPG Character Sprites" (256×576) ───────

    /**
     * Source is the CC0 "32x32 RPG Character Sprites" pack (opengameart.org/content/
     * 32x32-rpg-character-sprites, by Eldiran): a 384x672 sheet, 32x32 cells on a 12x21 grid.
     * Each ROW is one complete character (columns are facing/pose variants; column 0 is the
     * front-facing stand). Row 0 and row 20 are the pack's blank white mannequin templates and
     * are skipped. (An earlier version of this method wrongly treated a character as two
     * stacked cells and squashed a 32x64 region — which actually merged two different
     * characters into one garbled sprite.) Nine rows were picked for visually distinct
     * archetypes: armored knight, villager, red-bandana rogue, blue knight, hooded mage, white-
     * robed priest, dark-hooded rogue, wide-hat traveler, adventurer.
     * The source uses classic magenta (255,0,255) color-key transparency, not an alpha channel,
     * so it's keyed out to real alpha here before compositing — left as-is, every NPC would
     * render inside a solid pink box.
     */
    private static final int RPGCHAR_CELL = 32;
    private static final int[] NPC_SOURCE_ROWS = {1, 2, 3, 4, 5, 6, 8, 9, 10};

    private void buildNPCSheet() {
        File src = new File(externalDir, "characters/rpg_characters.png");
        if (!src.exists()) {
            log("[SKIP] npc_sheet — rpg_characters.png not found");
            return;
        }
        try {
            BufferedImage raw = ImageIO.read(src);
            if (raw == null) { log("[FAIL] npc_sheet — could not read source"); return; }
            BufferedImage source = keyOutMagenta(raw);

            int npcCount = NPC_SOURCE_ROWS.length;
            BufferedImage npcSheet = new BufferedImage(256, npcCount * 2 * NPC_FRAME_H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = npcSheet.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            for (int npc = 0; npc < npcCount; npc++) {
                BufferedImage charTile = source.getSubimage(
                    0, NPC_SOURCE_ROWS[npc] * RPGCHAR_CELL, RPGCHAR_CELL, RPGCHAR_CELL);

                int dstRow0 = npc * 2;       // IDLE / WORK row
                int dstRow1 = npc * 2 + 1;   // SIT / SLEEP row
                for (int c = 0; c < 8; c++) {
                    g.drawImage(charTile, c * NPC_FRAME_W, dstRow0 * NPC_FRAME_H,
                                NPC_FRAME_W, NPC_FRAME_H, null);
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

    /**
     * Source for both item and weapon icons is the same CC0 "Hero Spritesheets (Ars Notoria)"
     * pack already used for Rin (opengameart.org/content/hero-spritesheets-ars-notoria, by
     * Balmer) — its assets/icons/ folder ships real 16×16 icons for potions, rings, books,
     * swords and staffs, copied wholesale into assets/external/items/. Real icon art, not
     * placeholder swatches; unlike the Rin/NPC sheets these already carry proper PNG alpha
     * (indexed color + tRNS), no color-key transparency fix needed.
     */
    private static final String[] ITEM_ICON_FILES = {
        "potionHealth.png", "potionHealthBig.png", "potionMana.png", "potionManaBig.png",
        "ring-01.png", "ring-02.png", "ring-03.png", "ring-04.png",
        "book1.png", "book2.png", "book3.png", "book4.png",
        "statSTRPotion.png", "statDEFPotion.png", "statSPDPotion.png", "statCONPotion.png",
    };

    private void buildItemSheet() {
        File dir = new File(externalDir, "items");
        if (!dir.isDirectory()) {
            log("[SKIP] item_sheet — items directory not found");
            return;
        }
        BufferedImage itemSheet = new BufferedImage(256, ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = itemSheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int placed = 0;
        for (int i = 0; i < ITEM_ICON_FILES.length; i++) {
            File f = new File(dir, ITEM_ICON_FILES[i]);
            if (!f.exists()) continue;
            try {
                BufferedImage icon = ImageIO.read(f);
                if (icon == null) continue;
                g.drawImage(icon, i * ITEM_SIZE, 0, ITEM_SIZE, ITEM_SIZE, null);
                placed++;
            } catch (IOException ignored) {}
        }
        g.dispose();
        if (placed > 0) {
            write(itemSheet, new File(processedDir, "items/item_sheet.png"), "item_sheet");
        } else {
            log("[SKIP] item_sheet — no icon files could be loaded");
        }
    }

    // ── Step 6: Weapon Sheet from Ars Notoria sword/staff icons (288×24) ─────

    private static final String[] WEAPON_ICON_FILES = {
        "sword1.png", "sword2.png", "sword3.png", "sword4.png",
        "sword5.png", "sword6.png", "sword7.png", "sword8.png",
        "staff1.png", "staff2.png", "staff3.png", "staff4.png",
    };

    private void buildWeaponSheet() {
        File dir = new File(externalDir, "items");
        if (!dir.isDirectory()) {
            log("[SKIP] weapon_sheet — items directory not found");
            return;
        }
        BufferedImage weaponSheet = new BufferedImage(288, WEAPON_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = weaponSheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int placed = 0;
        for (int i = 0; i < WEAPON_ICON_FILES.length; i++) {
            File f = new File(dir, WEAPON_ICON_FILES[i]);
            if (!f.exists()) continue;
            try {
                BufferedImage icon = ImageIO.read(f);
                if (icon == null) continue;
                g.drawImage(icon, i * WEAPON_W, 0, WEAPON_W, WEAPON_H, null);
                placed++;
            } catch (IOException ignored) {}
        }
        g.dispose();
        if (placed > 0) {
            write(weaponSheet, new File(processedDir, "weapons/weapon_sheet.png"), "weapon_sheet");
        } else {
            log("[SKIP] weapon_sheet — no icon files could be loaded");
        }
    }

    // ── Step 7: Tool Sheet (160×16) from CC0 Tool Icons (FacadeGaikan) ────────

    /**
     * Source is the FacadeGaikan sub-collection of "CC0 Tool Icons" (opengameart.org/content/
     * cc0-tool-icons, node/29290) — real 32×32 pickaxe/hoe/shovel art in wood/stone/metal
     * tiers, matching this game's own tiered tool progression. No axe or fishing rod exists
     * in this sub-pack, so only 9 of the 10 slots are real art; the 10th reuses pick_metal
     * rather than leaving a gap.
     */
    private static final String[] TOOL_ICON_FILES = {
        "pick_wood.png", "pick_stone.png", "pick_metal.png",
        "hoe_wood.png", "hoe_stone.png", "hoe_metal.png",
        "shovel_wood.png", "shovel_stone.png", "shovel_metal.png",
        "pick_metal.png",
    };

    private void buildToolSheet() {
        File dir = new File(externalDir, "tools");
        if (!dir.isDirectory()) {
            log("[SKIP] tool_sheet — tools directory not found");
            return;
        }
        BufferedImage toolSheet = new BufferedImage(160, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = toolSheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int placed = 0;
        for (int i = 0; i < TOOL_ICON_FILES.length; i++) {
            File f = new File(dir, TOOL_ICON_FILES[i]);
            if (!f.exists()) continue;
            try {
                BufferedImage icon = ImageIO.read(f);
                if (icon == null) continue;
                g.drawImage(icon, i * TILE_SIZE, 0, TILE_SIZE, TILE_SIZE, null);
                placed++;
            } catch (IOException ignored) {}
        }
        g.dispose();
        if (placed > 0) {
            write(toolSheet, new File(processedDir, "tools/tool_sheet.png"), "tool_sheet");
        } else {
            log("[SKIP] tool_sheet — no icon files could be loaded");
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

    // ── Step 9: Heart Icon from "Pixel Art HUD Bars and Icons" (16×16) ───────

    /**
     * Source is the CC0 "Pixel Art HUD Bars and Icons" pack (opengameart.org/content/
     * pixel-art-hud-bars-and-icons, by grizzlei): a small 144×46 sheet of resource bars and
     * icons. The heart icon (3rd of 4 in the bottom icon row, hand-picked off a pixel-bounds
     * scan of the actual sheet — an 9×9 region at (18,35)) is the only piece used here.
     * SandboxHUD.renderHearts() previously approximated a heart out of two filled circles and
     * a triangle — exactly the "geometric figure" look this whole asset pass targets — so this
     * feeds real heart art into that renderer instead. Scaled up to 16×16 for a crisp base to
     * downscale from at render time.
     */
    private void buildHeartIcon() {
        File src = new File(externalDir, "ui/heart_icon.png");
        if (!src.exists()) {
            log("[SKIP] heart_icon — ui/heart_icon.png not found");
            return;
        }
        try {
            BufferedImage source = ImageIO.read(src);
            if (source == null) { log("[FAIL] heart_icon — unreadable"); return; }
            BufferedImage scaled = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(source, 0, 0, 16, 16, null);
            g.dispose();
            write(scaled, new File(processedDir, "ui/heart_icon.png"), "heart_icon");
        } catch (IOException e) {
            log("[FAIL] heart_icon — " + e.getMessage());
        }
    }

    // ── Step 10: Item Category Icon Sheet (240×16) ────────────────────────────

    /**
     * ItemDefinition (see com.echobound.items) has no sprite reference at all — only a flat
     * Color iconColor — so InventoryEquipmentWindow and SandboxHUD's quickslot bar have always
     * drawn every item as a plain colored square, regardless of what real art exists for
     * items/weapons/tools elsewhere in the pipeline. Auditing ItemRegistry shows only 8 of
     * ItemCategory's 15 values are ever actually registered (WEAPONS, MAGIC, ARMOR, RELICS,
     * MATERIALS, FOOD, POTIONS, CURRENCY), so rather than a full per-item icon system, this
     * gives each of those a representative real icon, reused from the Ars Notoria icon set
     * already vetted for items/weapons/tools. MATERIALS has no reasonable real-art match in
     * anything downloaded so far (no ore/wood/gem icon) and is deliberately left with no entry
     * here — the renderer falls back to the original flat-color square for it, same graceful-
     * partial pattern as buildToolSheet()'s missing axe/fishing-rod.
     * One 16×16 icon per column, indexed by ItemCategory.ordinal() (240 = 15 × 16); unused
     * category columns are left fully transparent.
     */
    private static String categoryIconFile(ItemCategory cat) {
        return switch (cat) {
            case WEAPONS  -> "items/sword1.png";
            case MAGIC    -> "items/book1.png";
            case ARMOR    -> "items/armor1.png";
            case RELICS   -> "items/ring-05.png";
            case FOOD, POTIONS -> "items/potionHealth.png";
            case CURRENCY -> "items/ring-01.png";
            default       -> null; // MATERIALS and unused categories — no real-art match
        };
    }

    private void buildCategoryIconsSheet() {
        ItemCategory[] categories = ItemCategory.values();
        BufferedImage sheet = new BufferedImage(categories.length * 16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int placed = 0;
        for (ItemCategory cat : categories) {
            String iconFile = categoryIconFile(cat);
            if (iconFile == null) continue;
            File f = new File(externalDir, iconFile);
            if (!f.exists()) continue;
            try {
                BufferedImage icon = ImageIO.read(f);
                if (icon == null) continue;
                g.drawImage(icon, cat.ordinal() * 16, 0, 16, 16, null);
                placed++;
            } catch (IOException ignored) {}
        }
        g.dispose();
        if (placed > 0) {
            write(sheet, new File(processedDir, "items/category_icons.png"), "category_icons");
        } else {
            log("[SKIP] category_icons — no icon files could be loaded");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Converts classic magenta (255,0,255) color-key transparency to real alpha=0.
     *  Several older CC0 packs (e.g. the Eldiran RPG character sheet) predate widespread
     *  PNG alpha-channel authoring and instead reserve pure magenta as "not part of the
     *  sprite" — composited as-is it renders as a solid pink box around every sprite. */
    private static BufferedImage keyOutMagenta(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                if ((argb & 0x00FFFFFF) == 0x00FF00FF) {
                    out.setRGB(x, y, 0);
                } else {
                    out.setRGB(x, y, argb | 0xFF000000);
                }
            }
        }
        return out;
    }

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
