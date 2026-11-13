package com.deadbeat.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.deadbeat.audio.SoundtrackPlayer;
import com.deadbeat.entities.Player;
import com.deadbeat.world.CityMap;
import com.deadbeat.world.GameWorld;

/** Cámara de la ciudad y HUD de guitarra con menú de pausa superpuesto. */
public final class GameScreen extends ScreenAdapter {
    public static final float VIEW_WIDTH = 960f;
    public static final float VIEW_HEIGHT = 540f;

    // Media guitarra cortada a lo largo y apoyada en el borde superior.
    // La caja/cuerpo queda en la esquina izquierda y el mastil apunta a la derecha.
    private static final float GUITAR_X = 0f;
    private static final float GUITAR_WIDTH = 330f;
    private static final float GUITAR_HEIGHT = 78f;
    private static final float GUITAR_Y = VIEW_HEIGHT - GUITAR_HEIGHT;
    private static final int MASK_WIDTH = 206;
    private static final int MASK_HEIGHT = 49;

    private static final float MENU_X = 255f;
    private static final float MENU_Y = 40f;
    private static final float MENU_WIDTH = 450f;
    private static final float MENU_HEIGHT = 460f;
    private static final float BUTTON_X = MENU_X + 77f;
    private static final float BUTTON_WIDTH = MENU_WIDTH - 154f;
    private static final float BUTTON_HEIGHT = 58f;
    private static final float CONTINUE_Y = MENU_Y + 108f;
    private static final float EXIT_Y = MENU_Y + 31f;

    private static final float SPEAKER_X = MENU_X + 74f;
    private static final float SPEAKER_Y = MENU_Y + 197f;
    private static final float SPEAKER_SIZE = 42f;
    private static final float VOLUME_X = MENU_X + 139f;
    private static final float VOLUME_Y = MENU_Y + 212f;
    private static final float VOLUME_WIDTH = 233f;
    private static final float VOLUME_HEIGHT = 11f;

    private final FitViewport worldViewport;
    private final FitViewport hudViewport;
    private final SpriteBatch batch;
    private final BitmapFont titleFont;
    private final BitmapFont menuFont;
    private final BitmapFont smallFont;
    private final GlyphLayout glyph = new GlyphLayout();
    private final Vector2 mouseInHud = new Vector2();
    private final Texture pixel;
    private final Texture guitarMask;
    private final Texture guitarOutline;
    private final GameWorld world;
    private final SoundtrackPlayer soundtrack;

    private boolean paused;
    private boolean draggingVolume;
    private int selectedButton; // 0 = continuar, 1 = salir
    // Monotonic wall-clock time includes pauses and time spent outside the game window.
    private long runStartedAtNanos;

    public GameScreen() {
        worldViewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT);
        hudViewport = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, new OrthographicCamera());
        batch = new SpriteBatch();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.0f);
        menuFont = new BitmapFont();
        menuFont.getData().setScale(1.7f);
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.15f);

        Pixmap white = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        white.setColor(1f, 1f, 1f, 1f);
        white.fill();
        pixel = new Texture(white);
        white.dispose();

        guitarMask = new Texture(Gdx.files.internal("hud/guitar_health_mask.png"));
        guitarOutline = new Texture(Gdx.files.internal("hud/guitar_health_outline.png"));
        guitarMask.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        guitarOutline.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        if (guitarMask.getWidth() != MASK_WIDTH || guitarMask.getHeight() != MASK_HEIGHT
                || guitarOutline.getWidth() != MASK_WIDTH || guitarOutline.getHeight() != MASK_HEIGHT) {
            throw new IllegalStateException("Los sprites de media guitarra deben medir 206x49 pixeles.");
        }

        world = new GameWorld();
        soundtrack = new SoundtrackPlayer();
        paused = false;
        selectedButton = 0;
        runStartedAtNanos = System.nanoTime();
        followPlayer();
    }

    @Override
    public void render(float delta) {
        // La lista de música continúa también mientras la simulación está pausada.
        soundtrack.update();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            selectedButton = 0;
            draggingVolume = false;
        }

        if (paused) {
            handlePauseMenuInput();
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                world.reset();
                runStartedAtNanos = System.nanoTime();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
                world.getPlayer().takeDamage(20f);
            }
            // Se detienen zombis, proyectiles, armas y oleadas al pausar.
            float safeDelta = Math.max(0f, Math.min(delta, 0.05f));
            world.update(safeDelta);
            followPlayer();
        }

        ScreenUtils.clear(0.05f, 0.055f, 0.07f, 1f);
        worldViewport.apply();
        batch.setProjectionMatrix(worldViewport.getCamera().combined);
        batch.begin();
        float left = worldViewport.getCamera().position.x - VIEW_WIDTH / 2f;
        float bottom = worldViewport.getCamera().position.y - VIEW_HEIGHT / 2f;
        world.draw(batch, left, bottom, VIEW_WIDTH, VIEW_HEIGHT);
        batch.end();

        hudViewport.apply();
        batch.setProjectionMatrix(hudViewport.getCamera().combined);
        batch.begin();
        drawGuitarHealth(world.getPlayer());
        if (paused) {
            drawPauseMenu();
        }
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private void handlePauseMenuInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)
                || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedButton = 1 - selectedButton;
        }
        // Accesos alternativos al mouse para controlar volumen y muteo.
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) soundtrack.toggleMute();
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT))
            soundtrack.setVolume(soundtrack.getVolume() - 0.05f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT))
            soundtrack.setVolume(soundtrack.getVolume() + 0.05f);

        // Unproject respeta el viewport si la ventana tiene barras laterales.
        hudViewport.unproject(mouseInHud.set(Gdx.input.getX(), Gdx.input.getY()));
        float mx = mouseInHud.x;
        float my = mouseInHud.y;
        final int hovered = buttonUnderMouse(mx, my);
        if (hovered >= 0) selectedButton = hovered;

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (inside(mx, my, SPEAKER_X, SPEAKER_Y, SPEAKER_SIZE, SPEAKER_SIZE)) {
                soundtrack.toggleMute();
            } else if (inside(mx, my, VOLUME_X - 10f, VOLUME_Y - 14f,
                    VOLUME_WIDTH + 20f, VOLUME_HEIGHT + 28f)) {
                draggingVolume = true;
                changeVolumeAt(mx);
            } else if (hovered >= 0) {
                activateButton(hovered);
            }
        }
        if (draggingVolume && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            changeVolumeAt(mx);
        } else if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            draggingVolume = false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            activateButton(selectedButton);
        }
    }

    private void changeVolumeAt(float mouseX) {
        soundtrack.setVolume(clamp((mouseX - VOLUME_X) / VOLUME_WIDTH, 0f, 1f));
    }

    private int buttonUnderMouse(float x, float y) {
        if (inside(x, y, BUTTON_X, CONTINUE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) return 0;
        if (inside(x, y, BUTTON_X, EXIT_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) return 1;
        return -1;
    }

    private static boolean inside(float x, float y, float left, float bottom, float w, float h) {
        return x >= left && x <= left + w && y >= bottom && y <= bottom + h;
    }

    private void activateButton(int button) {
        if (button == 0) {
            paused = false;
        } else {
            Gdx.app.exit();
        }
    }

    private void followPlayer() {
        Player p = world.getPlayer();
        float cx = clamp(p.getX(), VIEW_WIDTH / 2f, CityMap.WORLD_SIZE - VIEW_WIDTH / 2f);
        float cy = clamp(p.getY(), VIEW_HEIGHT / 2f, CityMap.WORLD_SIZE - VIEW_HEIGHT / 2f);
        worldViewport.getCamera().position.set(Math.round(cx), Math.round(cy), 0f);
        worldViewport.getCamera().update();
    }

    /**
     * Media guitarra horizontal: cuerpo en la esquina superior izquierda.
     * La vida ocupa desde el cuerpo hacia el mastil y retrocede hacia la izquierda
     * conforme baja la salud. La silueta, la sombra y el contorno nunca se mueven.
     */
    private void drawGuitarHealth(Player player) {
        float ratio = player.getMaxHealth() > 0f
                ? clamp(player.getHealth() / player.getMaxHealth(), 0f, 1f) : 0f;
        float x = GUITAR_X;
        float y = GUITAR_Y;

        batch.setColor(0f, 0f, 0f, 0.45f);
        batch.draw(guitarMask, x + 3f, y - 4f, GUITAR_WIDTH, GUITAR_HEIGHT);
        batch.setColor(0.12f, 0.105f, 0.13f, 0.93f);
        batch.draw(guitarMask, x, y, GUITAR_WIDTH, GUITAR_HEIGHT);

        // El recorte horizontal mantiene la parte izquierda de la silueta fija.
        int remainingPixels = Math.min(MASK_WIDTH,
                Math.max(0, Math.round(MASK_WIDTH * ratio)));
        if (remainingPixels > 0) {
            TextureRegion redPart = new TextureRegion(guitarMask,
                    0, 0, remainingPixels, MASK_HEIGHT);
            batch.setColor(0.89f, 0.09f, 0.17f, 1f);
            batch.draw(redPart, x, y,
                    GUITAR_WIDTH * remainingPixels / MASK_WIDTH, GUITAR_HEIGHT);
        }
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(guitarOutline, x, y, GUITAR_WIDTH, GUITAR_HEIGHT);
    }

    private void drawPauseMenu() {
        // World-facing city freezes underneath this dark overlay.
        block(0f, 0f, VIEW_WIDTH, VIEW_HEIGHT, .035f, .03f, .038f, .80f);
        // Asymmetric aged-metal backing, with a double border and graffiti streaks.
        block(MENU_X + 7f, MENU_Y - 7f, MENU_WIDTH, MENU_HEIGHT,
                .04f, .025f, .035f, .96f);
        block(MENU_X, MENU_Y, MENU_WIDTH, MENU_HEIGHT,
                .105f, .098f, .115f, .97f);
        block(MENU_X + 5f, MENU_Y + 5f, MENU_WIDTH - 10f, MENU_HEIGHT - 10f,
                .135f, .125f, .135f, .97f);
        block(MENU_X + 8f, MENU_Y + MENU_HEIGHT - 93f, MENU_WIDTH - 16f, 83f,
                .24f, .075f, .11f, 1f);
        block(MENU_X + 8f, MENU_Y + MENU_HEIGHT - 105f, MENU_WIDTH - 16f, 6f,
                .93f, .33f, .15f, 1f);
        // Vertical oxidized rivets and torn diagonal scratches.
        for (int i = 0; i < 8; i++) {
            float px = MENU_X + 13 + i * 59f;
            block(px, MENU_Y + 9f, 5f, 5f, .64f, .53f, .44f, 1f);
            block(px, MENU_Y + MENU_HEIGHT - 20f, 5f, 5f, .64f, .53f, .44f, 1f);
        }
        for (int i = 0; i < 7; i++) {
            block(MENU_X + 28f + i * 28f, MENU_Y + MENU_HEIGHT - 125f - (i % 2) * 5f,
                    17f, 3f, .47f, .16f, .22f, 0.65f);
        }

        centered(titleFont, "DEADBEAT", VIEW_WIDTH / 2f,
                MENU_Y + MENU_HEIGHT - 36f, 1f, .86f, .74f, 1f);
        centered(menuFont, "// PAUSA //", VIEW_WIDTH / 2f,
                MENU_Y + MENU_HEIGHT - 85f, 1f, .87f, .69f, 1f);
        centered(smallFont, "TIEMPO TOTAL DE RUN", VIEW_WIDTH / 2f,
                MENU_Y + 327f, .84f, .71f, .66f, 1f);
        centered(menuFont, elapsedAsClock(), VIEW_WIDTH / 2f,
                MENU_Y + 290f, 1f, .90f, .81f, 1f);
        block(MENU_X + 74f, MENU_Y + 269f, MENU_WIDTH - 148f, 2f,
                .64f, .19f, .25f, .9f);
        centered(smallFont, "MUSICA  " + Math.round(soundtrack.getVolume() * 100f) + "%",
                VIEW_WIDTH / 2f, MENU_Y + 245f, .95f, .81f, .72f, 1f);
        drawVolumeControl();
        drawPauseButton(CONTINUE_Y, "CONTINUAR", selectedButton == 0);
        drawPauseButton(EXIT_Y, "SALIR", selectedButton == 1);
    }

    /** Bocina interactiva y volumen: conserva el nivel elegido al mutear. */
    private void drawVolumeControl() {
        boolean muted = soundtrack.isMuted();
        // Botón metálico con acento rojo cuando la música está silenciada.
        block(SPEAKER_X + 3f, SPEAKER_Y - 3f, SPEAKER_SIZE, SPEAKER_SIZE,
                .05f, .04f, .06f, .92f);
        block(SPEAKER_X, SPEAKER_Y, SPEAKER_SIZE, SPEAKER_SIZE,
                muted ? .46f : .23f, .14f, .18f, 1f);
        block(SPEAKER_X + 3f, SPEAKER_Y + 3f, SPEAKER_SIZE - 6f, SPEAKER_SIZE - 6f,
                .16f, .14f, .16f, 1f);
        // Bocina en pixel-art: caja, cono y ondas (o cruz de mute).
        float sx = SPEAKER_X + 9f;
        float sy = SPEAKER_Y + 11f;
        block(sx, sy + 6f, 9f, 10f, .94f, .87f, .76f, 1f);
        block(sx + 9f, sy + 3f, 6f, 16f, .94f, .87f, .76f, 1f);
        block(sx + 14f, sy, 3f, 22f, .94f, .87f, .76f, 1f);
        if (muted) {
            // Cruz roja en la zona de salida del altavoz.
            for (int i = 0; i < 5; i++) {
                block(sx + 20f + i * 2f, sy + 6f + i * 2f, 3f, 3f, .95f, .18f, .25f, 1f);
                block(sx + 20f + i * 2f, sy + 14f - i * 2f, 3f, 3f, .95f, .18f, .25f, 1f);
            }
        } else {
            block(sx + 22f, sy + 4f, 3f, 14f, .97f, .68f, .40f, 1f);
            block(sx + 27f, sy + 1f, 3f, 20f, .97f, .68f, .40f, 1f);
        }

        block(VOLUME_X - 5f, VOLUME_Y - 8f, VOLUME_WIDTH + 10f, VOLUME_HEIGHT + 16f,
                .09f, .075f, .09f, 1f);
        block(VOLUME_X, VOLUME_Y, VOLUME_WIDTH, VOLUME_HEIGHT,
                .31f, .28f, .30f, 1f);
        float fraction = soundtrack.getVolume();
        block(VOLUME_X, VOLUME_Y, VOLUME_WIDTH * fraction, VOLUME_HEIGHT,
                muted ? .48f : .90f, muted ? .34f : .12f, .23f, 1f);
        float knobX = VOLUME_X + VOLUME_WIDTH * fraction;
        block(knobX - 6f, VOLUME_Y - 7f, 12f, VOLUME_HEIGHT + 14f,
                .10f, .08f, .12f, 1f);
        block(knobX - 4f, VOLUME_Y - 5f, 8f, VOLUME_HEIGHT + 10f,
                1f, .76f, .53f, 1f);
    }

    private void drawPauseButton(float y, String label, boolean selected) {
        block(BUTTON_X + 5f, y - 5f, BUTTON_WIDTH, BUTTON_HEIGHT,
                .055f, .04f, .06f, .88f);
        if (selected) {
            block(BUTTON_X, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                    .50f, .12f, .23f, 1f);
            block(BUTTON_X, y, 7f, BUTTON_HEIGHT, 1f, .41f, .18f, 1f);
            block(BUTTON_X + BUTTON_WIDTH - 7f, y, 7f, BUTTON_HEIGHT,
                    1f, .41f, .18f, 1f);
        } else {
            block(BUTTON_X, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                    .23f, .20f, .23f, 1f);
            block(BUTTON_X, y, 5f, BUTTON_HEIGHT, .62f, .28f, .32f, 1f);
        }
        centered(menuFont, label, VIEW_WIDTH / 2f, y + 39f,
                selected ? 1f : .80f, selected ? .96f : .79f, .76f, 1f);
    }

    /** Total elapsed real time. It intentionally includes every second spent paused. */
    private String elapsedAsClock() {
        long seconds = Math.max(0L, (System.nanoTime() - runStartedAtNanos) / 1_000_000_000L);
        return two(seconds / 3600) + ":" + two((seconds / 60) % 60) + ":" + two(seconds % 60);
    }

    private static String two(long value) {
        return value < 10 ? "0" + value : Long.toString(value);
    }

    private void centered(BitmapFont font, String text, float centerX, float baseline,
                          float r, float g, float b, float a) {
        font.setColor(r, g, b, a);
        glyph.setText(font, text);
        font.draw(batch, glyph, centerX - glyph.width / 2f, baseline);
    }

    private void block(float x, float y, float w, float h,
                       float r, float g, float b, float a) {
        batch.setColor(r, g, b, a);
        batch.draw(pixel, x, y, w, h);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, false);
        hudViewport.update(width, height, true);
        followPlayer();
    }

    @Override
    public void dispose() {
        soundtrack.dispose();
        world.dispose();
        titleFont.dispose();
        menuFont.dispose();
        smallFont.dispose();
        guitarMask.dispose();
        guitarOutline.dispose();
        pixel.dispose();
        batch.dispose();
    }
}
