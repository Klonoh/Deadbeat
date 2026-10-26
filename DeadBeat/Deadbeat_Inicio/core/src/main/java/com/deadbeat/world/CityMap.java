package com.deadbeat.world;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lazily builds deterministic 512x512 city blocks. We never generate an entire
 * map texture or hold every block in memory. Blocks are regenerated with the
 * same seed when the player revisits them.
 *
 * Geometry and collision live here (not in the rendering code).
 */
public final class CityMap implements CollisionMap {
    public static final int BLOCK_SIZE = 512;
    public static final int TILE_SIZE = 32;
    public static final float WORLD_SIZE = 131072f;
    public static final float SPAWN = WORLD_SIZE / 2f;

    private static final int CACHE_LIMIT = 96;
    private static final int SEED = 0x51A7D3B9;

    public enum Kind { BUILDING, CAR, DUMPSTER }

    /** Immutable geometry for a world prop. */
    public static final class Obstacle {
        public final float x, y, width, height;
        public final Kind kind;
        public final int style;

        private Obstacle(float x, float y, float width, float height,
                         Kind kind, int style) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.kind = kind;
            this.style = style;
        }
    }

    public static final class CityBlock {
        public final int blockX, blockY;
        private final Obstacle[] obstacles;

        private CityBlock(int blockX, int blockY, Obstacle[] obstacles) {
            this.blockX = blockX;
            this.blockY = blockY;
            this.obstacles = obstacles;
        }

        public int obstacleCount() { return obstacles.length; }
        public Obstacle obstacle(int index) { return obstacles[index]; }
    }

    private final LinkedHashMap<Long, CityBlock> blocks =
            new LinkedHashMap<Long, CityBlock>(128, 0.75f, true) {
                @Override protected boolean removeEldestEntry(Map.Entry<Long, CityBlock> oldest) {
                    return size() > CACHE_LIMIT;
                }
            };

    public CityBlock getBlock(int bx, int by) {
        long key = ((long) bx << 32) ^ (by & 0xffffffffL);
        CityBlock existing = blocks.get(key);
        if (existing != null) return existing;
        CityBlock result = generateBlock(bx, by);
        blocks.put(key, result);
        return result;
    }

    public int cachedBlockCount() { return blocks.size(); }

    public static int variation(int x, int y, int salt) {
        int h = SEED ^ (x * 0x9E3779B9) ^ Integer.rotateLeft(y * 0x85EBCA6B, 11) ^ salt;
        h ^= h >>> 16;
        h *= 0x7feb352d;
        h ^= h >>> 15;
        h *= 0x846ca68b;
        return h ^ (h >>> 16);
    }

    private CityBlock generateBlock(int bx, int by) {
        float baseX = bx * (float) BLOCK_SIZE;
        float baseY = by * (float) BLOCK_SIZE;
        int baseSeed = variation(bx, by, 1183);
        // Four separated lots, all surrounded by navigable alleys / sidewalks.
        Obstacle[] objects = new Obstacle[6];
        for (int index = 0; index < 4; index++) {
            int lotX = index % 2;
            int lotY = index / 2;
            int hash = variation(bx * 4 + lotX, by * 4 + lotY, index + 14);
            float x = baseX + (lotX == 0 ? 176f : 348f) + ((hash >>> 4) & 7);
            float y = baseY + (lotY == 0 ? 176f : 348f) + ((hash >>> 10) & 7);
            float w = 105f + ((hash >>> 15) & 15);
            float h = 105f + ((hash >>> 20) & 15);
            objects[index] = new Obstacle(x, y, w, h, Kind.BUILDING,
                    (hash >>> 25) & 3);
        }
        // Abandoned cars: one on the vertical road, another on the horizontal.
        // Their placements vary per block but never obstruct the whole road.
        objects[4] = new Obstacle(baseX + 15f + ((baseSeed >>> 3) & 13),
                baseY + 238f + ((baseSeed >>> 11) & 63),
                39f, 66f, Kind.CAR, (baseSeed >>> 25) & 3);
        objects[5] = new Obstacle(baseX + 248f + ((baseSeed >>> 9) & 63),
                baseY + 19f + ((baseSeed >>> 17) & 13),
                66f, 39f, Kind.CAR, (baseSeed >>> 27) & 3);
        return new CityBlock(bx, by, objects);
    }

    @Override
    public boolean collidesCircle(float centerX, float centerY, float radius) {
        if (!Float.isFinite(centerX) || !Float.isFinite(centerY)
                || !Float.isFinite(radius) || radius < 0f) {
            return true;
        }
        if (centerX - radius < 0f || centerY - radius < 0f
                || centerX + radius > WORLD_SIZE || centerY + radius > WORLD_SIZE) {
            return true;
        }
        int bx0 = (int) Math.floor((centerX - radius) / BLOCK_SIZE);
        int bx1 = (int) Math.floor((centerX + radius) / BLOCK_SIZE);
        int by0 = (int) Math.floor((centerY - radius) / BLOCK_SIZE);
        int by1 = (int) Math.floor((centerY + radius) / BLOCK_SIZE);
        for (int bx = bx0; bx <= bx1; bx++) {
            for (int by = by0; by <= by1; by++) {
                CityBlock block = getBlock(bx, by);
                for (int i = 0; i < block.obstacleCount(); i++) {
                    Obstacle obstacle = block.obstacle(i);
                    float closestX = clamp(centerX, obstacle.x,
                            obstacle.x + obstacle.width);
                    float closestY = clamp(centerY, obstacle.y,
                            obstacle.y + obstacle.height);
                    float dx = centerX - closestX;
                    float dy = centerY - closestY;
                    if (dx * dx + dy * dy < radius * radius) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static float clamp(float value, float low, float high) {
        return Math.max(low, Math.min(high, value));
    }
}
