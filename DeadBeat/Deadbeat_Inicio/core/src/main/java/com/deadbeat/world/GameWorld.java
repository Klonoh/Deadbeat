package com.deadbeat.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.deadbeat.entities.Player;

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

    /** Prototype punk musician made of pixel rectangles. No asset files required. */
    private void drawPlayer(SpriteBatch batch) {
        float x = player.getX();
        float y = player.getY();

        // Shadow under the feet; the collision circle uses radius 14 from Player.
        rect(batch, x - 12f, y - 14f, 24f, 3f, 0.045f, 0.045f, 0.06f);

        // Flash while damage immunity lasts. Only the player sprite flickers.
        if (player.isInvulnerable()
                && ((int) (player.getInvulnerabilityRemaining() * 14f) % 2 == 0)) {
            return;
        }

        // Boots and trousers.
        rect(batch, x - 10f, y - 12f, 8f, 6f, 0.05f, 0.06f, 0.08f);
        rect(batch, x + 2f, y - 12f, 8f, 6f, 0.05f, 0.06f, 0.08f);
        rect(batch, x - 9f, y - 7f, 18f, 6f, 0.25f, 0.25f, 0.32f);

        // Arms and studded leather jacket.
        rect(batch, x - 13f, y - 3f, 5f, 8f, 0.67f, 0.18f, 0.33f);
        rect(batch, x + 8f, y - 3f, 5f, 8f, 0.67f, 0.18f, 0.33f);
        rect(batch, x - 10f, y - 4f, 20f, 12f, 0.12f, 0.13f, 0.20f);
        rect(batch, x - 2f, y - 4f, 4f, 12f, 0.72f, 0.18f, 0.34f);
        rect(batch, x - 8f, y + 4f, 3f, 3f, 0.82f, 0.82f, 0.80f);
        rect(batch, x + 5f, y + 4f, 3f, 3f, 0.82f, 0.82f, 0.80f);

        // Face, eyes and bright pink mohawk. Character faces the latest direction.
        rect(batch, x - 7f, y + 6f, 14f, 7f, 0.96f, 0.72f, 0.54f);
        float eyeOffset = player.getFacingX() < -0.25f ? -2f
                : (player.getFacingX() > 0.25f ? 2f : 0f);
        rect(batch, x - 4f + eyeOffset, y + 8f, 2f, 2f, 0.06f, 0.06f, 0.09f);
        rect(batch, x + 2f + eyeOffset, y + 8f, 2f, 2f, 0.06f, 0.06f, 0.09f);
        rect(batch, x - 6f, y + 12f, 12f, 2f, 0.96f, 0.19f, 0.60f);
        rect(batch, x - 4f, y + 13f, 3f, 1f, 1f, 0.30f, 0.74f);
        rect(batch, x + 1f, y + 13f, 3f, 1f, 1f, 0.30f, 0.74f);
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
        pixel.dispose();
    }
}
