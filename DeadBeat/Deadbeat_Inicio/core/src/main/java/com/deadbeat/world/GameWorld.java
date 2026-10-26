package com.deadbeat.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.deadbeat.entities.Player;
import com.deadbeat.graphics.PlayerSpriteRenderer;

/** Simulation coordinator, independent from camera placement and screen HUD. */
public final class GameWorld implements Disposable {
    private final Texture pixel;
    private final CityMap cityMap;
    private final CityRenderer cityRenderer;
    private final Player player;
    private final PlayerSpriteRenderer playerRenderer;
    private float elapsedTime;

    public GameWorld() {
        cityMap = new CityMap();
        Rectangle bounds = new Rectangle(0f, 0f, CityMap.WORLD_SIZE, CityMap.WORLD_SIZE);
        player = new Player(CityMap.SPAWN + 64f, CityMap.SPAWN + 64f, bounds);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
        cityRenderer = new CityRenderer(cityMap, pixel);
        playerRenderer = new PlayerSpriteRenderer();
    }

    public void update(float delta) {
        if (!Float.isFinite(delta) || delta <= 0f) return;
        elapsedTime += delta;
        player.update(delta, cityMap);
    }

    public void draw(SpriteBatch batch, float viewLeft, float viewBottom,
                     float viewWidth, float viewHeight) {
        cityRenderer.draw(batch, viewLeft, viewBottom, viewWidth, viewHeight);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(pixel, player.getX() - 14f, player.getY() - 14f,
                28f, 4f);
        playerRenderer.draw(batch, player);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void reset() {
        player.reset();
        elapsedTime = 0f;
    }

    public Player getPlayer() { return player; }
    public CityMap getCityMap() { return cityMap; }
    public float getElapsedTime() { return elapsedTime; }

    @Override public void dispose() {
        playerRenderer.dispose();
        cityRenderer.dispose();
        pixel.dispose();
    }
}
