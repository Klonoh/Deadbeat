package com.deadbeat.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.deadbeat.entities.Zombie;
import com.deadbeat.entities.ZombieType;

/**
 * Renderizador dedicado a zombies comunes. No contiene lógica de combate.
 * Tres hojas PNG, cada una con 8 direcciones x 3 cuadros de 64x80.
 * Orden de filas: DOWN, DOWN_RIGHT, RIGHT, UP_RIGHT, UP,
 *                UP_LEFT, LEFT, DOWN_LEFT.
 * Las hojas son compartidas: nunca se crea una textura por zombie.
 */
public final class ZombieSpriteRenderer implements Disposable {
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 80;
    private static final float STEP_SECONDS = 0.19f;
    private static final int[] WALK_SEQUENCE = {0, 1, 0, 2};

    private final Texture[] textures = new Texture[Zombie.COMMON_VARIANTS];
    private final TextureRegion[][][] frames = new TextureRegion[Zombie.COMMON_VARIANTS][][];

    public ZombieSpriteRenderer() {
        for (int variant = 0; variant < textures.length; variant++) {
            Texture texture = new Texture(Gdx.files.internal(
                    "sprites/zombie_common_" + variant + ".png"));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            if (texture.getWidth() != FRAME_WIDTH * 3
                    || texture.getHeight() != FRAME_HEIGHT * 8) {
                texture.dispose();
                dispose();
                throw new IllegalStateException("Dimensiones incorrectas de zombie_common_"
                        + variant + ".png. Se esperan 192x640 píxeles.");
            }
            textures[variant] = texture;
            frames[variant] = TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT);
        }
    }

    /** Solo dibuja NORMAL. Los demás tipos conservan su render previo. */
    public void draw(SpriteBatch batch, Zombie zombie) {
        if (zombie.getType() != ZombieType.NORMAL || !zombie.isAlive()) return;
        final int variant = zombie.getVisualVariant();
        if (variant < 0 || variant >= frames.length) return;

        final int direction = FacingDirection.fromVector(
                zombie.getFacingX(), zombie.getFacingY()).ordinal();
        final int frame = zombie.isMoving()
                ? WALK_SEQUENCE[((int) (zombie.getAge() / STEP_SECONDS)) % WALK_SEQUENCE.length]
                : 0;

        batch.setColor(1f, 1f, 1f, 1f);
        // Los pies descansan cerca del centro de colisión, sin alterar la hitbox.
        batch.draw(frames[variant][direction][frame],
                Math.round(zombie.getX() - FRAME_WIDTH / 2f),
                Math.round(zombie.getY() - 12f), FRAME_WIDTH, FRAME_HEIGHT);
    }

    @Override
    public void dispose() {
        for (Texture t : textures) if (t != null) t.dispose();
    }
}
