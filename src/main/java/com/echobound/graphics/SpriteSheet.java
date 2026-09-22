package com.echobound.graphics;

import java.awt.image.BufferedImage;

public class SpriteSheet {
    private final BufferedImage sheet;
    private final int spriteWidth;
    private final int spriteHeight;
    private final int columns;
    private final int rows;

    public SpriteSheet(BufferedImage sheet, int spriteWidth, int spriteHeight) {
        this.sheet = sheet;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.columns = sheet != null ? sheet.getWidth() / spriteWidth : 0;
        this.rows = sheet != null ? sheet.getHeight() / spriteHeight : 0;
    }

    public BufferedImage getSprite(int col, int row) {
        if (sheet == null || col < 0 || col >= columns || row < 0 || row >= rows) {
            return getFallbackSprite(spriteWidth, spriteHeight);
        }
        return sheet.getSubimage(col * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight);
    }

    public BufferedImage getSprite(int index) {
        if (columns == 0) return getFallbackSprite(spriteWidth, spriteHeight);
        int col = index % columns;
        int row = index / columns;
        return getSprite(col, row);
    }

    public BufferedImage getSubimage(int x, int y, int w, int h) {
        if (sheet == null) return getFallbackSprite(w, h);
        int sx = Math.max(0, Math.min(x, sheet.getWidth() - 1));
        int sy = Math.max(0, Math.min(y, sheet.getHeight() - 1));
        int sw = Math.min(w, sheet.getWidth() - sx);
        int sh = Math.min(h, sheet.getHeight() - sy);
        if (sw <= 0 || sh <= 0) return getFallbackSprite(w, h);
        return sheet.getSubimage(sx, sy, sw, sh);
    }

    public int getSpriteWidth() {
        return spriteWidth;
    }

    public int getSpriteHeight() {
        return spriteHeight;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public BufferedImage getRawSheet() {
        return sheet;
    }

    private static BufferedImage getFallbackSprite(int w, int h) {
        return new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
    }
}
