package com.deadbeat.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.deadbeat.world.CityMap.CityBlock;
import com.deadbeat.world.CityMap.Obstacle;

/** Only paints the camera-visible part of the procedurally generated city. */
public final class CityRenderer implements Disposable {
    private static final int TILE = CityMap.TILE_SIZE;
    private static final int BLOCK = CityMap.BLOCK_SIZE;
    private final CityMap map;
    private final Texture tilesTexture;
    private final TextureRegion[] tiles;
    private final Texture pixel;

    public CityRenderer(CityMap map, Texture pixel) {
        this.map = map;
        this.pixel = pixel;
        tilesTexture = new Texture(Gdx.files.internal("world/city_tiles.png"));
        tilesTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        if (tilesTexture.getWidth() != 256 || tilesTexture.getHeight() != 32) {
            tilesTexture.dispose();
            throw new IllegalStateException("Incorrect world/city_tiles.png atlas");
        }
        tiles = TextureRegion.split(tilesTexture, TILE, TILE)[0];
    }

    /** Render only tiles and props overlapping the current visible camera rectangle. */
    public void draw(SpriteBatch batch, float left, float bottom,
                     float viewWidth, float viewHeight) {
        int tx0 = Math.max(0, (int) Math.floor(left / TILE) - 1);
        int ty0 = Math.max(0, (int) Math.floor(bottom / TILE) - 1);
        int tx1 = Math.min((int)(CityMap.WORLD_SIZE / TILE) - 1,
                (int) Math.floor((left + viewWidth) / TILE) + 1);
        int ty1 = Math.min((int)(CityMap.WORLD_SIZE / TILE) - 1,
                (int) Math.floor((bottom + viewHeight) / TILE) + 1);
        batch.setColor(1f, 1f, 1f, 1f);
        for (int ty = ty0; ty <= ty1; ty++) {
            int localY = ty % 16;
            for (int tx = tx0; tx <= tx1; tx++) {
                int localX = tx % 16;
                int hash = CityMap.variation(tx, ty, 2026);
                int id;
                if (localX < 4 || localY < 4) {
                    id = (hash & 31) == 0 ? 7 : ((hash & 7) == 0 ? 1 : 0);
                } else if (localX == 4 || localY == 4 || localX == 15 || localY == 15) {
                    id = (hash & 7) == 0 ? 3 : 2;
                } else {
                    id = (hash & 31) == 0 ? 6 : ((hash & 7) == 0 ? 5 : 4);
                }
                batch.setColor(1f, 1f, 1f, 1f);
                batch.draw(tiles[id], tx * (float) TILE, ty * (float) TILE);
                // Deterministic tiny street debris; not collidable.
                if ((hash & 127) == 5 && id <= 1) {
                    rect(batch, tx * (float) TILE + 13f, ty * (float) TILE + 19f,
                            6f, 3f, 0.50f, 0.34f, 0.40f);
                }
            }
        }

        int bx0 = Math.max(0, (int) Math.floor(left / BLOCK) - 1);
        int by0 = Math.max(0, (int) Math.floor(bottom / BLOCK) - 1);
        int bx1 = Math.min((int)(CityMap.WORLD_SIZE / BLOCK) - 1,
                (int) Math.floor((left + viewWidth) / BLOCK) + 1);
        int by1 = Math.min((int)(CityMap.WORLD_SIZE / BLOCK) - 1,
                (int) Math.floor((bottom + viewHeight) / BLOCK) + 1);
        for (int by = by0; by <= by1; by++) {
            for (int bx = bx0; bx <= bx1; bx++) {
                drawRoadPaint(batch, bx, by);
            }
        }
        // Obstacles over road lines, debris and ground; game player goes over the map.
        for (int by = by0; by <= by1; by++) {
            for (int bx = bx0; bx <= bx1; bx++) {
                CityBlock block = map.getBlock(bx, by);
                for (int i = 0; i < block.obstacleCount(); i++) {
                    Obstacle o = block.obstacle(i);
                    if (o.x + o.width < left - 30f || o.x > left + viewWidth + 30f
                            || o.y + o.height < bottom - 30f || o.y > bottom + viewHeight + 30f) {
                        continue;
                    }
                    switch (o.kind) {
                        case BUILDING: drawBuilding(batch, o); break;
                        case CAR: drawCar(batch, o); break;
                        case DUMPSTER: drawDumpster(batch, o); break;
                        default: break;
                    }
                }
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawRoadPaint(SpriteBatch batch, int bx, int by) {
        float px = bx * (float) BLOCK;
        float py = by * (float) BLOCK;
        // Worn yellow lane markings, spaced across long road stretches.
        for (int i = 0; i < 4; i++) {
            float d = 180f + 85f * i;
            rect(batch, px + 62f, py + d, 4f, 32f, 0.60f, 0.51f, 0.28f);
            rect(batch, px + d, py + 62f, 32f, 4f, 0.60f, 0.51f, 0.28f);
        }
        // Two crosswalks bordering the road intersection; visual-only.
        for (int i = 0; i < 7; i++) {
            float k = 8f + i * 17f;
            rect(batch, px + k, py + 140f, 9f, 17f, 0.69f, 0.67f, 0.61f);
            rect(batch, px + 140f, py + k, 17f, 9f, 0.69f, 0.67f, 0.61f);
        }
        // Streetlamp footprints at a few junction corners.
        rect(batch, px + 163f, py + 163f, 7f, 7f, 0.22f, 0.23f, 0.28f);
        rect(batch, px + 164f, py + 165f, 3f, 3f, 0.75f, 0.26f, 0.52f);
    }

    private void drawBuilding(SpriteBatch batch, Obstacle b) {
        float x = b.x, y = b.y, w = b.width, h = b.height;
        float tint = b.style * 0.014f;
        rect(batch, x + 7f, y - 10f, w, h, 0.09f, 0.08f, 0.12f); // deep shadow
        rect(batch, x - 4f, y - 6f, w + 8f, h + 10f,
                0.17f + tint, 0.16f, 0.22f + tint); // bottom wall
        rect(batch, x - 4f, y - 6f, w + 8f, 7f, 0.12f, 0.13f, 0.16f);
        // Window bands and an abandoned metal entrance; rooftop means solid obstacle.
        for (int i = 0; i < 3; i++) {
            float wx = x + 13f + 31f * i;
            if (wx + 16f < x + w - 6f) {
                rect(batch, wx, y - 5f, 15f, 3f,
                        0.21f, (i == b.style % 3 ? 0.61f : 0.28f), 0.50f);
            }
        }
        // Brick silhouette / roof.
        rect(batch, x, y + 4f, w, h - 5f,
                0.22f + tint, 0.22f + tint / 2f, 0.29f + tint);
        rect(batch, x + 6f, y + 10f, w - 12f, h - 18f,
                0.13f + tint, 0.15f, 0.20f + tint);
        rect(batch, x + 6f, y + h - 12f, w - 12f, 3f, 0.32f, 0.25f, 0.34f);
        // Rooftop AC unit, fan, and a neon-edged graffiti tag.
        rect(batch, x + 18f, y + h - 42f, 31f, 23f, 0.33f, 0.34f, 0.38f);
        rect(batch, x + 22f, y + h - 38f, 23f, 15f, 0.14f, 0.16f, 0.19f);
        rect(batch, x + 29f, y + h - 38f, 7f, 15f, 0.29f, 0.31f, 0.33f);
        rect(batch, x + 11f, y + 22f, 37f, 4f,
                b.style % 2 == 0 ? 0.75f : 0.22f,
                b.style % 2 == 0 ? 0.19f : 0.63f, 0.49f);
        rect(batch, x + 24f, y + 18f, 24f, 3f, 0.66f, 0.57f, 0.24f);
        // Small vent gives scale; all details remain purely decorative.
        rect(batch, x + w - 32f, y + 21f, 19f, 17f, 0.29f, 0.30f, 0.35f);
        for (int k = 0; k < 3; k++) {
            rect(batch, x + w - 28f, y + 24f + k * 4f, 11f, 1f,
                    0.10f, 0.11f, 0.14f);
        }
    }

    private void drawCar(SpriteBatch batch, Obstacle c) {
        float x = c.x, y = c.y, w = c.width, h = c.height;
        rect(batch, x + 4f, y - 4f, w, h, 0.07f, 0.08f, 0.11f);
        float red = c.style == 0 ? 0.52f : (c.style == 1 ? 0.20f : 0.30f);
        float green = c.style == 0 ? 0.18f : (c.style == 1 ? 0.46f : 0.28f);
        rect(batch, x, y, w, h, red, green, 0.30f);
        rect(batch, x + 5f, y + 6f, w - 10f, h - 12f, 0.15f, 0.22f, 0.27f);
        if (h > w) {
            rect(batch, x + 7f, y + 15f, w - 14f, 8f, 0.36f, 0.41f, 0.44f);
            rect(batch, x + 7f, y + h - 23f, w - 14f, 8f, 0.36f, 0.41f, 0.44f);
        } else {
            rect(batch, x + 15f, y + 7f, 8f, h - 14f, 0.36f, 0.41f, 0.44f);
            rect(batch, x + w - 23f, y + 7f, 8f, h - 14f, 0.36f, 0.41f, 0.44f);
        }
        rect(batch, x + 3f, y + 2f, 5f, 4f, 0.84f, 0.27f, 0.24f);
        rect(batch, x + w - 8f, y + 2f, 5f, 4f, 0.84f, 0.27f, 0.24f);
    }

    private void drawDumpster(SpriteBatch batch, Obstacle o) {
        rect(batch, o.x + 2f, o.y - 4f, o.width, o.height, 0.08f, 0.10f, 0.12f);
        rect(batch, o.x, o.y, o.width, o.height, 0.15f, 0.34f, 0.28f);
    }

    private void rect(SpriteBatch batch, float x, float y, float w, float h,
                      float r, float g, float b) {
        batch.setColor(r, g, b, 1f);
        batch.draw(pixel, x, y, w, h);
    }

    @Override public void dispose() {
        tilesTexture.dispose();
    }
}
