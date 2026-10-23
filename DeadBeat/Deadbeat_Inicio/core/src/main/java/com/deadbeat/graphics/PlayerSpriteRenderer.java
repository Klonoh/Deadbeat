package com.deadbeat.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.deadbeat.entities.Player;

/**
 * Presentation-only component: maps the Player's facing direction and walking
 * timer to a frame. The sprite sheet is loaded once, not during render().
 *
 * The sheet contains eight rows (directions) x three columns (idle, walk, walk).
 * Each frame is 64x80 with transparency and foot alignment.
 */
public final class PlayerSpriteRenderer implements Disposable {
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 80;
    private static final float STEP_SECONDS = 0.11f;
    private static final int[] WALK_SEQUENCE = { 1, 0, 2, 0 };

    private final Texture texture;
    private final TextureRegion[][] frames;

    public PlayerSpriteRenderer() {
        texture = new Texture(Gdx.files.internal("sprites/player_walk.png"));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        if (texture.getWidth() != FRAME_WIDTH * 3
                || texture.getHeight() != FRAME_HEIGHT * 8) {
            texture.dispose();
            throw new IllegalStateException("Unexpected player_walk.png dimensions");
        }
        frames = TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT);
    }

    public void draw(SpriteBatch batch, Player player) {
        // Preserve the existing short damage-flash feedback from prototype 0.2.
        if (player.isInvulnerable()
                && ((int) (player.getInvulnerabilityRemaining() * 14f) % 2 == 0)) {
            return;
        }
        int row = FacingDirection.fromVector(player.getFacingX(),
                player.getFacingY()).ordinal();
        int frame = player.isMoving()
                ? WALK_SEQUENCE[((int) (player.getWalkTime() / STEP_SECONDS))
                        % WALK_SEQUENCE.length]
                : 0;

        batch.setColor(1f, 1f, 1f, 1f);
        // Frame bottom is placed near feet; the 14-unit hitbox remains unchanged.
        batch.draw(frames[row][frame], player.getX() - FRAME_WIDTH / 2f,
                player.getY() - 13f, FRAME_WIDTH, FRAME_HEIGHT);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
