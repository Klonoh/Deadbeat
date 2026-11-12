package com.deadbeat.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.deadbeat.entities.Player;
import com.deadbeat.world.CityMap;
import com.deadbeat.world.GameWorld;

/** World-facing camera and independent fixed-screen HUD. */
public final class GameScreen extends ScreenAdapter {
    public static final float VIEW_WIDTH = 960f;
    public static final float VIEW_HEIGHT = 540f;

    private final FitViewport worldViewport;
    private final FitViewport hudViewport;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Texture hudPixel;
    private final GameWorld world;

    public GameScreen() {
        worldViewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT);
        hudViewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, new OrthographicCamera());
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        hudPixel = new Texture(p);
        p.dispose();
        world = new GameWorld();
        followPlayer();
    }

    @Override public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) world.reset();
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) world.getPlayer().takeDamage(20f);
        // Avoid a large teleport after minimizing or changing windows.
        float safeDelta = Math.max(0f, Math.min(delta, 0.05f));
        world.update(safeDelta);
        followPlayer();

        ScreenUtils.clear(0.05f, 0.055f, 0.07f, 1f);
        worldViewport.apply();
        batch.setProjectionMatrix(worldViewport.getCamera().combined);
        batch.begin();
        float left = worldViewport.getCamera().position.x - VIEW_WIDTH / 2f;
        float bottom = worldViewport.getCamera().position.y - VIEW_HEIGHT / 2f;
        world.draw(batch, left, bottom, VIEW_WIDTH, VIEW_HEIGHT);
        batch.end();

        // Apply the fixed HUD viewport AFTER the camera projection so its text
        // does not travel with the player through the map.
        hudViewport.apply();
        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();
        drawHud(world.getPlayer());
        batch.end();
    }

    private void followPlayer() {
        Player p = world.getPlayer();
        // Clamp only at the far map limits, leaving the player in the center of
        // the screen everywhere else (far beyond normal testing distance).
        float cx = clamp(p.getX(), VIEW_WIDTH / 2f,
                CityMap.WORLD_SIZE - VIEW_WIDTH / 2f);
        float cy = clamp(p.getY(), VIEW_HEIGHT / 2f,
                CityMap.WORLD_SIZE - VIEW_HEIGHT / 2f);
        // Pixel-snap the camera. Subpixel positions + Nearest filtering made
        // stationary scenery shimmer and the player look as if it jumped.
        worldViewport.getCamera().position.set(Math.round(cx), Math.round(cy), 0f);
        worldViewport.getCamera().update();
    }

    private void drawHud(Player player) {
        panel(11f, VIEW_HEIGHT - 70f, 525f, 59f);
        font.setColor(Color.WHITE);
        font.draw(batch, "DEADBEAT  |  CIUDAD PUNK 0.5.2", 22f, VIEW_HEIGHT - 31f);
        font.draw(batch, "WASD / flechas: caminar  |  H: dano  |  R: reiniciar  |  ESC: salir",
                22f, VIEW_HEIGHT - 54f);
        panel(11f, 10f, 605f, 49f);
        font.draw(batch, "VIDA: " + (int) player.getHealth() + "/"
                + (int) player.getMaxHealth(), 22f, 41f);
        font.draw(batch, "TIEMPO: " + (int) world.getElapsedTime() + " s", 153f, 41f);
        font.draw(batch, "POS: " + (int) (player.getX() - CityMap.SPAWN)
                + ", " + (int) (player.getY() - CityMap.SPAWN), 287f, 41f);
        String zone;
        switch (world.getCityMap().districtAt(player.getX(), player.getY())) {
            case SHOPS: zone = "COMERCIO"; break;
            case RESIDENTIAL: zone = "RESIDENCIAL"; break;
            case INDUSTRIAL: zone = "INDUSTRIAL"; break;
            case PARKING: zone = "ESTACIONAMIENTOS"; break;
            case CAMP: zone = "CAMPAMENTO"; break;
            case BACKSTREET: zone = "CALLEJONES"; break;
            default: zone = "CIUDAD";
        }
        font.draw(batch, "ZONA: " + zone, 432f, 41f);
        if (player.isInvulnerable()) {
            font.draw(batch, "INVULNERABLE", 22f, 24f);
        } else if (!player.isAlive()) {
            font.draw(batch, "SIN VIDA - R PARA REINICIAR", 22f, 24f);
        } else {
            font.draw(batch, "Explora las tiendas, callejones, parques y zonas industriales.", 22f, 24f);
        }
    }

    private void panel(float x, float y, float w, float h) {
        batch.setColor(0.07f, 0.075f, 0.10f, 0.88f);
        batch.draw(hudPixel, x, y, w, h);
        batch.setColor(0.72f, 0.20f, 0.43f, 1f);
        batch.draw(hudPixel, x, y, 4f, h);
        batch.setColor(Color.WHITE);
    }

    private static float clamp(float x, float lo, float hi) {
        return Math.max(lo, Math.min(hi, x));
    }

    @Override public void resize(int width, int height) {
        worldViewport.update(width, height, false);
        hudViewport.update(width, height, true);
        followPlayer();
    }

    @Override public void dispose() {
        world.dispose();
        font.dispose();
        batch.dispose();
        hudPixel.dispose();
    }
}
