package com.deadbeat.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

/**
 * Contiene la simulacion de la partida.
 * Por ahora solo dibuja una arena y una marca animada que verifica update(delta).
 */
public final class GameWorld implements Disposable {

    private final float width;
    private final float height;
    private final Texture pixel;
    private float elapsedTime;

    public GameWorld(float width, float height) {
        this.width = width;
        this.height = height;

        // Textura de 1x1 creada por codigo: no necesitamos sprites por ahora.
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta) {
        elapsedTime += delta;
        // En la proxima etapa actualizaremos aqui Player y los Zombie.
    }

    public void draw(SpriteBatch batch) {
        // Suelo de asfalto y limites de la arena.
        rect(batch, 16f, 60f, width - 32f, height - 140f, 0.10f, 0.11f, 0.15f);
        rect(batch, 16f, 60f, width - 32f, 8f, 0.72f, 0.15f, 0.30f);
        rect(batch, 16f, height - 88f, width - 32f, 8f, 0.72f, 0.15f, 0.30f);
        rect(batch, 16f, 60f, 8f, height - 140f, 0.72f, 0.15f, 0.30f);
        rect(batch, width - 24f, 60f, 8f, height - 140f, 0.72f, 0.15f, 0.30f);

        // Cuadricula tenue para mostrar visualmente la posicion en el mundo.
        for (float x = 56f; x < width - 24f; x += 40f) {
            rect(batch, x, 68f, 1f, height - 156f, 0.16f, 0.16f, 0.19f);
        }
        for (float y = 100f; y < height - 88f; y += 40f) {
            rect(batch, 24f, y, width - 48f, 1f, 0.16f, 0.16f, 0.19f);
        }

        // Indicador de pulso: si cambia de tamaño, update(delta) funciona.
        float pulse = 18f + MathUtils.sin(elapsedTime * 3f) * 5f;
        float centerX = width / 2f;
        float centerY = (60f + height - 80f) / 2f;
        rect(batch, centerX - pulse / 2f, centerY - pulse / 2f,
                pulse, pulse, 0.46f, 0.97f, 0.57f);

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void rect(SpriteBatch batch, float x, float y, float width, float height,
                      float red, float green, float blue) {
        batch.setColor(red, green, blue, 1f);
        batch.draw(pixel, x, y, width, height);
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public void dispose() {
        pixel.dispose();
    }
}
