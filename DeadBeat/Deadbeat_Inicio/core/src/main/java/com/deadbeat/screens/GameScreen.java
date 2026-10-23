package com.deadbeat.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.deadbeat.entities.Player;
import com.deadbeat.world.GameWorld;

/** Coordinates drawing, input shortcuts and the GameWorld's simulation. */
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

        // Temporary DEBUG controls until enemies are implemented.
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            world.reset();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            world.getPlayer().takeDamage(20f);
        }

        // Limit simulation delta to avoid giant leaps after switching windows.
        float safeDelta = Math.max(0f, Math.min(delta, 0.05f));
        world.update(safeDelta);

        ScreenUtils.clear(0.035f, 0.04f, 0.055f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        Player player = world.getPlayer();
        batch.begin();
        world.draw(batch);
        font.draw(batch, "DEADBEAT  |  PROTOTIPO 0.2 - PLAYER", 20f, WORLD_HEIGHT - 20f);
        font.draw(batch, "WASD / flechas: mover    H: dano de prueba    R: reiniciar    ESC: salir",
                20f, WORLD_HEIGHT - 43f);
        font.draw(batch, "VIDA: " + (int) player.getHealth() + " / "
                + (int) player.getMaxHealth(), 20f, 35f);
        font.draw(batch, "TIEMPO: " + (int) world.getElapsedTime() + " s", 230f, 35f);
        if (player.isInvulnerable()) {
            font.draw(batch, "INVULNERABLE", 390f, 35f);
        } else if (!player.isAlive()) {
            font.draw(batch, "SIN VIDA - R PARA REINICIAR", 390f, 35f);
        }
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
