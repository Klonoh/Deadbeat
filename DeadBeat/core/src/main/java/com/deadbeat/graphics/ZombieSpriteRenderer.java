package com.deadbeat.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.deadbeat.entities.Zombie;
import com.deadbeat.entities.ZombieType;

/**
 * Renderizador dedicado a zombies con sprites.
 * NORMAL usa 8 direcciones x 3 cuadros de 64x80.
 * TANK usa 4 direcciones x 4 cuadros de 128x128.
 * FAST usa 4 direcciones x 6 cuadros de 64x80 y se dibuja mas pequeno.
 * Las hojas se cargan una sola vez y se comparten entre todas las instancias.
 */
public final class ZombieSpriteRenderer implements Disposable {
    private static final int COMMON_FRAME_WIDTH = 64;
    private static final int COMMON_FRAME_HEIGHT = 80;
    private static final int TANK_FRAME_WIDTH = 128;
    private static final int TANK_FRAME_HEIGHT = 128;
    private static final int FAST_FRAME_WIDTH = 64;
    private static final int FAST_FRAME_HEIGHT = 80;
    private static final float FAST_DRAW_WIDTH = 52f;
    private static final float FAST_DRAW_HEIGHT = 65f;
    private static final float COMMON_STEP_SECONDS = 0.19f;
    private static final float TANK_STEP_SECONDS = 0.23f;
    private static final float FAST_STEP_SECONDS = 0.075f;
    private static final int[] COMMON_WALK_SEQUENCE = {0, 1, 0, 2};
    private static final int[] TANK_WALK_SEQUENCE = {0, 1, 2, 3};

    private final Texture[] commonTextures = new Texture[Zombie.COMMON_VARIANTS];
    private final TextureRegion[][][] commonFrames = new TextureRegion[Zombie.COMMON_VARIANTS][][];
    private final Texture[] tankTextures = new Texture[Zombie.TANK_VARIANTS];
    private final TextureRegion[][][] tankFrames = new TextureRegion[Zombie.TANK_VARIANTS][][];
    private final Texture[] fastTextures = new Texture[Zombie.FAST_VARIANTS];
    private final TextureRegion[][][] fastFrames = new TextureRegion[Zombie.FAST_VARIANTS][][];

    public ZombieSpriteRenderer() {
        loadCommon();
        loadTank();
        loadFast();
    }

    private void loadCommon() {
        for (int variant = 0; variant < commonTextures.length; variant++) {
            Texture texture = new Texture(Gdx.files.internal(
                    "sprites/zombie_common_" + variant + ".png"));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            if (texture.getWidth() != COMMON_FRAME_WIDTH * 3
                    || texture.getHeight() != COMMON_FRAME_HEIGHT * 8) {
                texture.dispose();
                dispose();
                throw new IllegalStateException("Dimensiones incorrectas de zombie_common_"
                        + variant + ".png. Se esperan 192x640 píxeles.");
            }
            commonTextures[variant] = texture;
            commonFrames[variant] = TextureRegion.split(texture, COMMON_FRAME_WIDTH, COMMON_FRAME_HEIGHT);
        }
    }

    private void loadTank() {
        for (int variant = 0; variant < tankTextures.length; variant++) {
            Texture texture = new Texture(Gdx.files.internal(
                    "sprites/zombie_tank_" + variant + ".png"));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            if (texture.getWidth() != TANK_FRAME_WIDTH * 4
                    || texture.getHeight() != TANK_FRAME_HEIGHT * 4) {
                texture.dispose();
                dispose();
                throw new IllegalStateException("Dimensiones incorrectas de zombie_tank_"
                        + variant + ".png. Se esperan 512x512 píxeles.");
            }
            tankTextures[variant] = texture;
            tankFrames[variant] = TextureRegion.split(texture, TANK_FRAME_WIDTH, TANK_FRAME_HEIGHT);
        }
    }

    private void loadFast() {
        for (int variant = 0; variant < fastTextures.length; variant++) {
            Texture texture = new Texture(Gdx.files.internal(
                    "sprites/zombie_fast_" + variant + ".png"));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            if (texture.getWidth() != FAST_FRAME_WIDTH * 6
                    || texture.getHeight() != FAST_FRAME_HEIGHT * 4) {
                texture.dispose();
                dispose();
                throw new IllegalStateException("Dimensiones incorrectas de zombie_fast_"
                        + variant + ".png. Se esperan 384x320 pixeles.");
            }
            fastTextures[variant] = texture;
            fastFrames[variant] = TextureRegion.split(texture, FAST_FRAME_WIDTH, FAST_FRAME_HEIGHT);
        }
    }

    public void draw(SpriteBatch batch, Zombie zombie) {
        if (!zombie.isAlive()) return;
        if (zombie.getType() == ZombieType.NORMAL) {
            drawCommon(batch, zombie);
        } else if (zombie.getType() == ZombieType.TANK) {
            drawTank(batch, zombie);
        } else if (zombie.getType() == ZombieType.FAST) {
            drawFast(batch, zombie);
        }
    }

    private void drawCommon(SpriteBatch batch, Zombie zombie) {
        final int variant = zombie.getVisualVariant();
        if (variant < 0 || variant >= commonFrames.length) return;

        final int direction = FacingDirection.fromVector(
                zombie.getFacingX(), zombie.getFacingY()).ordinal();
        final int frame = zombie.isMoving()
                ? COMMON_WALK_SEQUENCE[((int) (zombie.getAge() / COMMON_STEP_SECONDS)) % COMMON_WALK_SEQUENCE.length]
                : 0;

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(commonFrames[variant][direction][frame],
                Math.round(zombie.getX() - COMMON_FRAME_WIDTH / 2f),
                Math.round(zombie.getY() - 12f), COMMON_FRAME_WIDTH, COMMON_FRAME_HEIGHT);
    }

    private void drawTank(SpriteBatch batch, Zombie zombie) {
        final int variant = zombie.getVisualVariant();
        if (variant < 0 || variant >= tankFrames.length) return;

        final int row = tankRowFromVector(zombie.getFacingX(), zombie.getFacingY());
        final int frame = zombie.isMoving()
                ? TANK_WALK_SEQUENCE[((int) (zombie.getAge() / TANK_STEP_SECONDS)) % TANK_WALK_SEQUENCE.length]
                : 0;

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(tankFrames[variant][row][frame],
                Math.round(zombie.getX() - TANK_FRAME_WIDTH / 2f),
                Math.round(zombie.getY() - 20f), TANK_FRAME_WIDTH, TANK_FRAME_HEIGHT);
    }

    /**
     * Row order in fast sheets: DOWN, RIGHT, UP, LEFT.
     * We reuse the nearest of four painted directions for diagonals.
     * The 6-frame cycle is faster than the common and tank walk cycles.
     */
    private void drawFast(SpriteBatch batch, Zombie zombie) {
        final int variant = zombie.getVisualVariant();
        if (variant < 0 || variant >= fastFrames.length) return;
        final int row = fastRowFromVector(zombie.getFacingX(), zombie.getFacingY());
        final int frame = zombie.isMoving()
                ? ((int) (zombie.getAge() / FAST_STEP_SECONDS)) % 6 : 0;
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(fastFrames[variant][row][frame],
                Math.round(zombie.getX() - FAST_DRAW_WIDTH / 2f),
                Math.round(zombie.getY() - 10f), FAST_DRAW_WIDTH, FAST_DRAW_HEIGHT);
    }

    static int fastRowFromVector(float x, float y) {
        if (Math.abs(x) > Math.abs(y)) return x < 0f ? 3 : 1;
        return y > 0f ? 2 : 0;
    }

    /** Row order in tank sheets: DOWN, UP, LEFT, RIGHT. */
    private int tankRowFromVector(float x, float y) {
        if (Math.abs(x) > Math.abs(y)) return x < 0f ? 2 : 3;
        return y >= 0f ? 1 : 0;
    }

    @Override
    public void dispose() {
        for (Texture t : commonTextures) if (t != null) t.dispose();
        for (Texture t : tankTextures) if (t != null) t.dispose();
        for (Texture t : fastTextures) if (t != null) t.dispose();
    }
}
