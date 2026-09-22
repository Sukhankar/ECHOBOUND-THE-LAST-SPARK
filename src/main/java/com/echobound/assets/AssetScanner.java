package com.echobound.assets;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class AssetScanner {

    public static class AssetInfo {
        public final String relativePath;
        public final int width;
        public final int height;
        public final long sizeBytes;
        public final int estimatedFrames;
        public final boolean corrupted;

        public AssetInfo(String relativePath, int width, int height, long sizeBytes, int estimatedFrames, boolean corrupted) {
            this.relativePath = relativePath;
            this.width = width;
            this.height = height;
            this.sizeBytes = sizeBytes;
            this.estimatedFrames = estimatedFrames;
            this.corrupted = corrupted;
        }
    }

    public static class AssetReport {
        public final List<AssetInfo> validAssets = new ArrayList<>();
        public final List<String> missingRequiredAssets = new ArrayList<>();
        public final List<String> corruptedAssets = new ArrayList<>();
        public final List<String> oversizedAssets = new ArrayList<>();
        public long totalSizeBytes = 0;

        public boolean isHealthy() {
            return missingRequiredAssets.isEmpty() && corruptedAssets.isEmpty() && oversizedAssets.isEmpty();
        }
    }

    public static final String[] REQUIRED_ASSETS = {
        "characters/rin_sheet.png",
        "characters/pip_sheet.png",
        "characters/echo_sheet.png",
        "npcs/npc_sheet.png",
        "mobs/mobs_sheet.png",
        "weapons/weapon_sheet.png",
        "tools/tool_sheet.png",
        "items/item_sheet.png",
        "tiles/terrain_sheet.png",
        "effects/effects_sheet.png",
        "ui/ui_sheet.png",
        "portraits/kael_portrait.png",
        "portraits/sylvan_portrait.png"
    };

    public static AssetReport scan(File baseDir) {
        AssetReport report = new AssetReport();
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            report.missingRequiredAssets.addAll(Arrays.asList(REQUIRED_ASSETS));
            return report;
        }

        // Check required assets
        for (String req : REQUIRED_ASSETS) {
            File f = new File(baseDir, req);
            if (!f.exists()) {
                report.missingRequiredAssets.add(req);
            }
        }

        // Scan all files recursively
        scanDirectory(baseDir, baseDir, report);

        return report;
    }

    private static void scanDirectory(File root, File current, AssetReport report) {
        File[] files = current.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(root, f, report);
            } else if (f.getName().toLowerCase().endsWith(".png")) {
                String relPath = root.toURI().relativize(f.toURI()).getPath();
                long size = f.length();
                report.totalSizeBytes += size;

                if (size > 500 * 1024) { // 500 KB limit for compact pixel art
                    report.oversizedAssets.add(relPath + " (" + size + " bytes)");
                }

                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img == null) {
                        report.corruptedAssets.add(relPath);
                        report.validAssets.add(new AssetInfo(relPath, 0, 0, size, 0, true));
                    } else {
                        int w = img.getWidth();
                        int h = img.getHeight();
                        int frames = calculateEstimatedFrames(relPath, w, h);
                        report.validAssets.add(new AssetInfo(relPath, w, h, size, frames, false));
                    }
                } catch (IOException e) {
                    report.corruptedAssets.add(relPath);
                    report.validAssets.add(new AssetInfo(relPath, 0, 0, size, 0, true));
                }
            }
        }
    }

    private static int calculateEstimatedFrames(String path, int width, int height) {
        if (path.contains("rin_sheet") || path.contains("echo_sheet")) {
            return (width / 32) * (height / 32);
        } else if (path.contains("pip_sheet")) {
            return (width / 16) * (height / 16);
        } else if (path.contains("npc_sheet") || path.contains("mobs_sheet")) {
            return (width / 32) * (height / 32);
        } else if (path.contains("weapon_sheet")) {
            return width / 24;
        } else if (path.contains("tool_sheet") || path.contains("item_sheet") || path.contains("terrain_sheet")) {
            return (width / 16) * (height / 16);
        }
        return 1;
    }
}
