package com.echobound.world;

import com.echobound.physics.AABB;
import com.echobound.physics.PhysicsConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TileMap {
    private final int width;
    private final int height;
    private final TileType[][] tiles;
    private float spawnX = 64;
    private float spawnY = 200;

    public TileMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new TileType[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = TileType.EMPTY;
            }
        }
    }

    public static TileMap fromAscii(String[] lines) {
        int h = lines.length;
        int w = 0;
        for (String line : lines) {
            if (line.length() > w) w = line.length();
        }
        TileMap map = new TileMap(w, h);
        for (int y = 0; y < h; y++) {
            String line = lines[y];
            for (int x = 0; x < line.length(); x++) {
                char c = line.charAt(x);
                TileType type = TileType.fromChar(c);
                if (type == TileType.SPAWN) {
                    map.spawnX = x * PhysicsConfig.TILE_SIZE + 3;
                    map.spawnY = y * PhysicsConfig.TILE_SIZE + 2;
                    map.tiles[x][y] = TileType.EMPTY;
                } else {
                    map.tiles[x][y] = type;
                }
            }
        }
        return map;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getPixelWidth() {
        return width * PhysicsConfig.TILE_SIZE;
    }

    public float getPixelHeight() {
        return height * PhysicsConfig.TILE_SIZE;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public float getSpawnY() {
        return spawnY;
    }

    public TileType getTile(int tx, int ty) {
        if (tx < 0 || tx >= width) {
            return TileType.SOLID; // Walls on sides
        }
        if (ty < 0) {
            return TileType.EMPTY; // Sky above
        }
        if (ty >= height) {
            return TileType.HAZARD; // Pit at bottom
        }
        return tiles[tx][ty];
    }

    public void setTile(int tx, int ty, TileType type) {
        if (tx >= 0 && tx < width && ty >= 0 && ty < height) {
            tiles[tx][ty] = type;
        }
    }

    public List<AABB> getCollidingSolidTiles(AABB box) {
        List<AABB> solids = new ArrayList<>(8);
        int startX = (int) Math.floor(box.getLeft() / PhysicsConfig.TILE_SIZE);
        int endX = (int) Math.floor((box.getRight() - 0.001f) / PhysicsConfig.TILE_SIZE);
        int startY = (int) Math.floor(box.getTop() / PhysicsConfig.TILE_SIZE);
        int endY = (int) Math.floor((box.getBottom() - 0.001f) / PhysicsConfig.TILE_SIZE);

        for (int ty = startY; ty <= endY; ty++) {
            for (int tx = startX; tx <= endX; tx++) {
                TileType t = getTile(tx, ty);
                if (t.isSolid()) {
                    solids.add(new AABB(
                        tx * PhysicsConfig.TILE_SIZE,
                        ty * PhysicsConfig.TILE_SIZE,
                        PhysicsConfig.TILE_SIZE,
                        PhysicsConfig.TILE_SIZE
                    ));
                }
            }
        }
        return solids;
    }

    public List<AABB> getOneWayTiles(AABB box) {
        List<AABB> oneWays = new ArrayList<>(4);
        int startX = (int) Math.floor(box.getLeft() / PhysicsConfig.TILE_SIZE);
        int endX = (int) Math.floor((box.getRight() - 0.001f) / PhysicsConfig.TILE_SIZE);
        int startY = (int) Math.floor(box.getTop() / PhysicsConfig.TILE_SIZE);
        int endY = (int) Math.floor((box.getBottom() - 0.001f) / PhysicsConfig.TILE_SIZE);

        for (int ty = startY; ty <= endY; ty++) {
            for (int tx = startX; tx <= endX; tx++) {
                TileType t = getTile(tx, ty);
                if (t.isOneWay()) {
                    oneWays.add(new AABB(
                        tx * PhysicsConfig.TILE_SIZE,
                        ty * PhysicsConfig.TILE_SIZE,
                        PhysicsConfig.TILE_SIZE,
                        PhysicsConfig.TILE_SIZE
                    ));
                }
            }
        }
        return oneWays;
    }

    public void render(Graphics2D g, float camX, float camY, int viewW, int viewH) {
        int startX = Math.max(0, (int) Math.floor(camX / PhysicsConfig.TILE_SIZE));
        int endX = Math.min(width - 1, (int) Math.ceil((camX + viewW) / PhysicsConfig.TILE_SIZE));
        int startY = Math.max(0, (int) Math.floor(camY / PhysicsConfig.TILE_SIZE));
        int endY = Math.min(height - 1, (int) Math.ceil((camY + viewH) / PhysicsConfig.TILE_SIZE));

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                TileType t = tiles[x][y];
                int px = x * PhysicsConfig.TILE_SIZE - (int) camX;
                int py = y * PhysicsConfig.TILE_SIZE - (int) camY;
                int ts = PhysicsConfig.TILE_SIZE;

                switch (t) {
                    case SOLID -> {
                        // Sleek dark tech block
                        g.setColor(new Color(22, 26, 36));
                        g.fillRect(px, py, ts, ts);

                        // Top edge highlight
                        boolean aboveEmpty = (y == 0 || !tiles[x][y - 1].isSolid());
                        if (aboveEmpty) {
                            g.setColor(new Color(64, 180, 210, 200)); // Glowing cyan top trim
                            g.fillRect(px, py, ts, 2);
                            g.setColor(new Color(40, 52, 70));
                            g.fillRect(px, py + 2, ts, 1);
                        } else {
                            g.setColor(new Color(32, 38, 52));
                            g.drawRect(px, py, ts - 1, ts - 1);
                        }

                        // Inner subtle geometric tech circuit
                        if ((x + y) % 3 == 0) {
                            g.setColor(new Color(36, 44, 60));
                            g.fillRect(px + 4, py + 4, 3, 3);
                        }
                    }
                    case ONE_WAY -> {
                        // Tech beam / grate platform
                        g.setColor(new Color(16, 20, 28, 180));
                        g.fillRect(px, py + 2, ts, 5);

                        // Glowing top line
                        g.setColor(new Color(255, 190, 60, 220)); // Warm gold/amber energy bar
                        g.fillRect(px, py, ts, 2);

                        // Little tech rivets
                        g.setColor(new Color(255, 240, 150));
                        g.fillRect(px + 2, py, 2, 2);
                        g.fillRect(px + ts - 4, py, 2, 2);
                    }
                    case HAZARD -> {
                        // Spikes / corruption crystals
                        g.setColor(new Color(220, 40, 90));
                        int[] xPts = {px, px + ts / 2, px + ts};
                        int[] yPts = {py + ts, py + 2, py + ts};
                        g.fillPolygon(xPts, yPts, 3);
                        g.setColor(new Color(255, 120, 160));
                        g.drawLine(px + ts / 2, py + 2, px + ts / 2, py + ts);
                    }
                    default -> {}
                }
            }
        }
    }
}
