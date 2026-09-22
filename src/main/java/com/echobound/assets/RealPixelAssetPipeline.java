package com.echobound.assets;

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
 * Source packs consumed:
 *  - DezrasDragons / itch.io  →  assets/external/characters/ninja_frames/
 *  - Kenney Micro Roguelike   →  assets/external/characters/kenney_roguelike_characters.png
 *  - Kenney Platformer Pack   →  assets/external/tiles/kenney_platformer_tiles.png
 *  - Kenney Tiny Dungeon      →  assets/external/mobs/kenney_tiny_dungeon_tiles.png
 *  - Kenney Micro Items       →  assets/external/items/kenney_micro_items.png
 *  - Kenney UI Tiles          →  assets/external/ui/tile_0000.png … tile_0090.png
 *
 * All sources are CC0 (Kenney) or CC-BY (DezrasDragons with credit in THIRD_PARTY_ASSETS.md).
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
    private static final int MOB_FRAME_W    = 16;
    private static final int MOB_FRAME_H    = 16;

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

    // ── Step 3: Mob Sheet from Kenney tiny dungeon (128×384) ─────────────────

    private void buildMobSheet() {
        File src = new File(externalDir, "mobs/kenney_tiny_dungeon_tiles.png");
        if (!src.exists()) {
            log("[SKIP] mobs_sheet — kenney_tiny_dungeon_tiles.png not found");
            return;
        }
        try {
            BufferedImage source = ImageIO.read(src);
            if (source == null) { log("[FAIL] mobs_sheet — unreadable"); return; }

            // Kenney tiny dungeon: 16×16 sprites, many different tiles
            int srcTileW = 16, srcTileH = 16;
            int srcCols  = source.getWidth()  / srcTileW;
            int srcRows  = source.getHeight() / srcTileH;

            // 4 mobs × 3 animation-rows (IDLE, ATTACK, DEAD) × 4 frames
            // Use different rows of the dungeon sheet for each mob
            BufferedImage mobSheet = new BufferedImage(128, 384, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = mobSheet.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            // Pick representative source rows for each mob archetype
            int[] mobSourceRows = {0, 1, 2, 3}; // rows in the dungeon tileset
            Color[] mobTints = {
                new Color(255, 80, 80, 0),   // Drone   — red tint  (0 = no tint)
                new Color(140, 60, 200, 40),  // Creeper — purple tint
                new Color(255, 140, 20, 60),  // Golem   — orange tint
                new Color(60, 20, 120, 80)    // Stalker — dark purple tint
            };

            for (int mob = 0; mob < 4; mob++) {
                int srcRow = mobSourceRows[mob] < srcRows ? mobSourceRows[mob] : 0;
                for (int animRow = 0; animRow < 3; animRow++) { // IDLE, ATTACK, DEAD
                    for (int frame = 0; frame < 4; frame++) {
                        int srcCol = Math.min(frame, srcCols - 1);
                        BufferedImage srcTile = source.getSubimage(
                            srcCol * srcTileW, srcRow * srcTileH,
                            srcTileW, srcTileH);

                        int destX = frame * MOB_FRAME_W;
                        int destY = (mob * 3 + animRow) * MOB_FRAME_H;

                        // Scale 16→16 (no scaling needed for mob sheet)
                        g.drawImage(srcTile, destX, destY, MOB_FRAME_W, MOB_FRAME_H, null);

                        // Apply mob-specific tint
                        if (mobTints[mob].getAlpha() > 0) {
                            g.setColor(mobTints[mob]);
                            g.fillRect(destX, destY, MOB_FRAME_W, MOB_FRAME_H);
                        }

                        // ATTACK row: add red glow overlay
                        if (animRow == 1) {
                            g.setColor(new Color(255, 0, 0, 30));
                            g.fillRect(destX, destY, MOB_FRAME_W, MOB_FRAME_H);
                        }
                        // DEAD row: grey-fade overlay
                        if (animRow == 2) {
                            g.setColor(new Color(180, 180, 180, 120));
                            g.fillRect(destX, destY, MOB_FRAME_W, MOB_FRAME_H);
                        }
                    }
                }
            }
            g.dispose();
            write(mobSheet, new File(processedDir, "mobs/mobs_sheet.png"), "mobs_sheet");
        } catch (IOException e) {
            log("[FAIL] mobs_sheet — " + e.getMessage());
        }
    }

    // ── Step 4: Terrain Sheet from Kenney platformer tiles (128×256) ─────────

    private void buildTerrainSheet() {
        File src = new File(externalDir, "tiles/kenney_platformer_tiles.png");
        if (!src.exists()) {
            log("[SKIP] terrain_sheet — kenney_platformer_tiles.png not found");
            return;
        }
        try {
            BufferedImage source = ImageIO.read(src);
            if (source == null) { log("[FAIL] terrain_sheet — unreadable"); return; }

            // Kenney platformer: 16×16 tiles
            int srcTileW = 16, srcTileH = 16;
            int srcCols  = source.getWidth()  / srcTileW;
            int srcRows  = source.getHeight() / srcTileH;
            if (srcCols == 0 || srcRows == 0) { log("[FAIL] terrain_sheet — too small"); return; }

            // Build 8 cols × 16 rows at 16×16 — map to BlockType ordinals
            // 16 block types × 8 variants
            BufferedImage terrainSheet = new BufferedImage(128, 256, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = terrainSheet.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            // Fill background (non-transparent)
            g.setColor(new Color(20, 20, 30));
            g.fillRect(0, 0, 128, 256);

            for (int blockRow = 0; blockRow < 16; blockRow++) {
                for (int col = 0; col < 8; col++) {
                    // Cycle through source tiles — offset by block type
                    int srcCol = (col + blockRow * 2) % srcCols;
                    int srcRow = blockRow % srcRows;
                    if (srcRow >= srcRows) srcRow = srcRows - 1;

                    try {
                        BufferedImage tile = source.getSubimage(
                            srcCol * srcTileW, srcRow * srcTileH,
                            srcTileW, srcTileH);
                        g.drawImage(tile, col * TILE_SIZE, blockRow * TILE_SIZE,
                                    TILE_SIZE, TILE_SIZE, null);
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

            int srcW = 16, srcH = 16;
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

            int srcW = 16, srcH = 16;
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
