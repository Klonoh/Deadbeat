package com.deadbeat.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.deadbeat.world.GameWorld;

/** Coordina la pantalla. La lógica del mundo se mantiene en GameWorld. */
public final class GameScreen extends ScreenAdapter {

    public static final float WORLD_WIDTH = 960f;
    public static final float WORLD_HEIGHT = 540f;

    private final FitViewport viewport;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GameWorld world;

    public GameScreen() {
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        world = new GameWorld(WORLD_WIDTH, WORLD_HEIGHT);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        // Evita saltos enormes en la simulacion si la ventana se congela un instante.
        float safeDelta = Math.min(delta, 0.05f);
        world.update(safeDelta);

        ScreenUtils.clear(0.035f, 0.04f, 0.055f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        world.draw(batch);
        font.draw(batch, "DEADBEAT  |  PROTOTIPO 0.1", 20f, WORLD_HEIGHT - 20f);
        font.draw(batch, "Arena de prueba - personaje y zombies: siguiente etapa", 20f, WORLD_HEIGHT - 43f);
        font.draw(batch, "Tiempo: " + (int) world.getElapsedTime() + " s", 20f, 30f);
        font.draw(batch, "ESC: salir", WORLD_WIDTH - 95f, 30f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        world.dispose();
        font.dispose();
        batch.dispose();
    }
}
