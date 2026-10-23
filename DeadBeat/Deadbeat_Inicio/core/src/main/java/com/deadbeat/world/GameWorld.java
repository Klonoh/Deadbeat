package com.deadbeat.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.deadbeat.entities.Player;
import com.deadbeat.graphics.PlayerSpriteRenderer;

/** Holds simulation state and draws the prototype arena and player. */
public final class GameWorld implements Disposable {

    private static final float ARENA_LEFT = 24f;
    private static final float ARENA_BOTTOM = 68f;
    private static final float ARENA_TOP_MARGIN = 88f;
    private static final float ARENA_RIGHT_MARGIN = 24f;

    private final float width;
    private final float height;
    private final Texture pixel;
    private final Player player;
    private final PlayerSpriteRenderer playerSpriteRenderer;
    private float elapsedTime;

    public GameWorld(float width, float height) {
        this.width = width;
        this.height = height;
        Rectangle playableArena = new Rectangle(ARENA_LEFT, ARENA_BOTTOM,
                width - ARENA_LEFT - ARENA_RIGHT_MARGIN,
                height - ARENA_BOTTOM - ARENA_TOP_MARGIN);
        player = new Player(playableArena.x + playableArena.width / 2f,
                playableArena.y + playableArena.height / 2f, playableArena);

        // One shared 1x1 texture, reused by arena and player with no per-frame allocations.
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
        playerSpriteRenderer = new PlayerSpriteRenderer();
    }

    public void update(float delta) {
        if (delta <= 0f) {
            return;
        }
        elapsedTime += delta;
        player.update(delta);
    }

    public void reset() {
        player.reset();
        elapsedTime = 0f;
    }

    public void draw(SpriteBatch batch) {
        // Asphalt floor and arena borders.
        rect(batch, 16f, 60f, width - 32f, height - 140f, 0.10f, 0.11f, 0.15f);
        rect(batch, 16f, 60f, width - 32f, 8f, 0.72f, 0.15f, 0.30f);
        rect(batch, 16f, height - 88f, width - 32f, 8f, 0.72f, 0.15f, 0.30f);
        rect(batch, 16f, 60f, 8f, height - 140f, 0.72f, 0.15f, 0.30f);
        rect(batch, width - 24f, 60f, 8f, height - 140f, 0.72f, 0.15f, 0.30f);

        // Quiet grid to make movement easy to assess.
        for (float x = 56f; x < width - 24f; x += 40f) {
            rect(batch, x, 68f, 1f, height - 156f, 0.16f, 0.16f, 0.19f);
        }
        for (float y = 100f; y < height - 88f; y += 40f) {
            rect(batch, 24f, y, width - 48f, 1f, 0.16f, 0.16f, 0.19f);
        }

        drawPlayer(batch);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /** Draws an unobtrusive shadow and the current directional animation frame. */
    private void drawPlayer(SpriteBatch batch) {
        // Player x/y denotes the gameplay hitbox center, approximately between the feet.
        rect(batch, player.getX() - 14f, player.getY() - 14f,
                28f, 4f, 0.045f, 0.045f, 0.06f);
        playerSpriteRenderer.draw(batch, player);
    }

    private void rect(SpriteBatch batch, float x, float y, float w, float h,
                      float red, float green, float blue) {
        batch.setColor(red, green, blue, 1f);
        batch.draw(pixel, x, y, w, h);
    }

    public Player getPlayer() {
        return player;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public void dispose() {
        playerSpriteRenderer.dispose();
        pixel.dispose();
    }
}
