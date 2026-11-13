package com.deadbeat.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.deadbeat.DeadbeatGame;
import com.deadbeat.audio.SoundtrackPlayer;

/**
 * Menu principal. Solo reproduce music/main_theme.mp3 (si existe):
 * la lista aleatoria de la partida NO se instancia hasta pulsar JUGAR.
 */
public final class MainMenuScreen extends ScreenAdapter {
    private static final float W = 960f;
    private static final float H = 540f;
    private static final float BUTTON_X = 335f;
    private static final float BUTTON_W = 290f;
    private static final float BUTTON_H = 66f;
    private static final float PLAY_Y = 222f;
    private static final float EXIT_Y = 130f;
    private static final String THEME_PATH = "music/main_theme.mp3";
    // Control de sonido discreto bajo SALIR, dentro del cartel.
    private static final float VOLUME_X = 376f;
    private static final float VOLUME_Y = 102f;
    private static final float VOLUME_W = 236f;
    private static final float VOLUME_H = 9f;
    private static final float SPEAKER_X = 329f;
    private static final float SPEAKER_Y = 92f;
    private static final float SPEAKER_SIZE = 27f;

    private final DeadbeatGame game;
    private final FitViewport viewport = new FitViewport(W, H, new OrthographicCamera());
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont titleFont = new BitmapFont();
    private final BitmapFont menuFont = new BitmapFont();
    private final GlyphLayout glyph = new GlyphLayout();
    private final Vector2 mouse = new Vector2();
    private final Texture pixel;
    private Music menuTheme;
    private int selected = 0;
    private float elapsed;
    private boolean disposed;
    private boolean draggingVolume;
    private float volume = SoundtrackPlayer.DEFAULT_VOLUME;
    private boolean muted;

    public MainMenuScreen(DeadbeatGame game) {
        if (game == null) throw new IllegalArgumentException("DeadbeatGame requerido");
        this.game = game;
        titleFont.getData().setScale(5.0f);
        menuFont.getData().setScale(2.1f);

        Pixmap white = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        white.setColor(1f, 1f, 1f, 1f);
        white.fill();
        pixel = new Texture(white);
        white.dispose();

        // Main theme is deliberately independent from SoundtrackPlayer.
        // If no theme has been supplied, the menu works silently.
        FileHandle theme = Gdx.files.internal(THEME_PATH);
        if (theme.exists()) {
            try {
                menuTheme = Gdx.audio.newMusic(theme);
                menuTheme.setLooping(true);
                applyThemeVolume();
                menuTheme.play();
            } catch (RuntimeException ex) {
                if (menuTheme != null) {
                    menuTheme.dispose();
                    menuTheme = null;
                }
                if (Gdx.app != null) {
                    Gdx.app.error("DeadbeatMenu", "No se pudo cargar " + THEME_PATH, ex);
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        elapsed += Math.max(0f, Math.min(delta, 0.05f));
        if (handleInput()) return;

        ScreenUtils.clear(.045f, .044f, .052f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        drawScenery();
        drawMenu();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    /** Returns true if a screen transition/exit was requested. */
    private boolean handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)
                || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selected = 1 - selected;
        }
        // Los controles de volumen no cambian el boton JUGAR/SALIR seleccionado.
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) changeVolume(volume - .05f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) changeVolume(volume + .05f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) toggleMute();

        viewport.unproject(mouse.set(Gdx.input.getX(), Gdx.input.getY()));
        int hovered = buttonAt(mouse.x, mouse.y);
        if (hovered >= 0) selected = hovered;

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (inside(mouse.x, mouse.y, SPEAKER_X, SPEAKER_Y,
                    SPEAKER_SIZE, SPEAKER_SIZE)) {
                toggleMute();
                return false;
            }
            if (inside(mouse.x, mouse.y, VOLUME_X - 8f, VOLUME_Y - 14f,
                    VOLUME_W + 16f, VOLUME_H + 28f)) {
                draggingVolume = true;
                changeVolume((mouse.x - VOLUME_X) / VOLUME_W);
                return false;
            }
        }
        if (draggingVolume && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            changeVolume((mouse.x - VOLUME_X) / VOLUME_W);
        } else if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            draggingVolume = false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            return activate(selected);
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && hovered >= 0) {
            return activate(hovered);
        }
        return false;
    }

    private int buttonAt(float x, float y) {
        if (inside(x, y, BUTTON_X, PLAY_Y, BUTTON_W, BUTTON_H)) return 0;
        if (inside(x, y, BUTTON_X, EXIT_Y, BUTTON_W, BUTTON_H)) return 1;
        return -1;
    }

    private static boolean inside(float x, float y, float bx, float by, float bw, float bh) {
        return x >= bx && x <= bx + bw && y >= by && y <= by + bh;
    }

    private boolean activate(int choice) {
        if (choice == 1) {
            Gdx.app.exit();
            return true;
        }
        // Stop the main theme BEFORE GameScreen creates its shuffled playlist.
        stopTheme();
        game.setScreen(new GameScreen(volume, muted));
        dispose();
        return true;
    }

    private void drawScenery() {
        box(0f, 0f, W, H, .08f, .078f, .093f, 1f);
        // City skyline, deliberately different silhouettes and irregular heights.
        for (int i = 0; i < 22; i++) {
            float x = i * 46f - 15f;
            int hash = (i * 73 + 41) % 11;
            float buildingH = 108f + hash * 11f;
            box(x, 0f, 52f, buildingH, .11f, .103f, .115f, 1f);
            box(x + 3f, buildingH - 7f, 44f, 5f, .18f, .16f, .17f, 1f);
            for (int j = 0; j < 4; j++) {
                if (((i * 3 + j) & 3) == 0) {
                    box(x + 11f + (j % 2) * 20f,
                            24f + (j / 2) * 34f, 6f, 14f,
                            .44f, .20f, .12f, .62f);
                }
            }
        }
        box(0f, 0f, W, 40f, .075f, .070f, .081f, 1f);
        for (int i = 0; i < 30; i++) {
            float x = (i * 113f) % W;
            float y = 10f + ((i * 37) % 90);
            box(x, y, 5f + i % 8, 2f, .45f, .34f, .32f, .2f);
        }
        // Rough-edged poster, dark metal and rust accents.
        box(178f, 55f, 604f, 439f, .025f, .024f, .035f, .93f);
        box(188f, 63f, 584f, 421f, .14f, .11f, .13f, .95f);
        box(196f, 71f, 568f, 405f, .07f, .075f, .09f, .97f);
        box(196f, 407f, 568f, 69f, .34f, .075f, .12f, 1f);
        box(196f, 396f, 568f, 6f, .86f, .36f, .16f, 1f);
        for (int i = 0; i < 9; i++) {
            float x = 205f + i * 63f;
            box(x, 470f, 6f, 6f, .60f, .47f, .40f, 1f);
            box(x, 79f, 6f, 6f, .60f, .47f, .40f, 1f);
        }
        for (int i = 0; i < 11; i++) {
            float x = 211f + i * 47f;
            float y = 370f - (i % 3) * 6f;
            box(x, y, 26f, 3f, .50f, .12f, .23f, .7f);
        }
        // subtle blinking red lights around the panel
        float blink = .65f + .25f * (float)Math.sin(elapsed * 2.2f);
        box(218f, 112f, 4f, 155f, .85f, .18f, .24f, blink);
        box(738f, 112f, 4f, 155f, .85f, .18f, .24f, blink);
    }

    private void drawMenu() {
        centered(titleFont, "DEADBEAT", 480f, 427f,
                .98f, .86f, .76f, 1f);
        drawButton(PLAY_Y, "JUGAR", selected == 0);
        drawButton(EXIT_Y, "SALIR", selected == 1);
        drawVolumeSlider();
    }

    /** Barra de volumen sin leyendas; el tema responde en tiempo real. */
    private void drawVolumeSlider() {
        // Bocina pixelada, silenciable con clic o M.
        box(SPEAKER_X + 2f, SPEAKER_Y + 8f, 8f, 11f, .93f, .79f, .73f, 1f);
        box(SPEAKER_X + 9f, SPEAKER_Y + 4f, 6f, 19f, .93f, .79f, .73f, 1f);
        if (muted || volume == 0f) {
            box(SPEAKER_X + 18f, SPEAKER_Y + 6f, 3f, 17f, .90f, .15f, .20f, 1f);
            box(SPEAKER_X + 16f, SPEAKER_Y + 13f, 8f, 3f, .90f, .15f, .20f, 1f);
        } else {
            box(SPEAKER_X + 19f, SPEAKER_Y + 8f, 3f, 14f, .94f, .55f, .30f, 1f);
            box(SPEAKER_X + 24f, SPEAKER_Y + 4f, 3f, 22f, .94f, .55f, .30f, 1f);
        }
        box(VOLUME_X - 3f, VOLUME_Y - 3f, VOLUME_W + 6f, VOLUME_H + 6f,
                .025f, .023f, .030f, 1f);
        box(VOLUME_X, VOLUME_Y, VOLUME_W, VOLUME_H,
                .31f, .25f, .27f, 1f);
        float filled = VOLUME_W * volume;
        if (!muted && filled > 0f) {
            box(VOLUME_X, VOLUME_Y, filled, VOLUME_H,
                    .86f, .16f, .24f, 1f);
        }
        box(VOLUME_X + filled - 5f, VOLUME_Y - 6f, 10f, 21f,
                .97f, .82f, .70f, 1f);
        box(VOLUME_X + filled - 3f, VOLUME_Y - 3f, 6f, 15f,
                .53f, .15f, .18f, 1f);
    }

    private void changeVolume(float value) {
        if (!Float.isFinite(value)) return;
        volume = Math.max(0f, Math.min(1f, value));
        // Arrastrar el slider vuelve a habilitar el audio si se habia muteado.
        muted = false;
        applyThemeVolume();
    }

    private void toggleMute() {
        muted = !muted;
        applyThemeVolume();
    }

    private void applyThemeVolume() {
        if (menuTheme != null) menuTheme.setVolume(muted ? 0f : volume);
    }

    private void drawButton(float y, String label, boolean active) {
        box(BUTTON_X + 5f, y - 5f, BUTTON_W, BUTTON_H, .02f, .02f, .03f, .99f);
        box(BUTTON_X, y, BUTTON_W, BUTTON_H,
                active ? .52f : .19f,
                active ? .105f : .18f,
                active ? .17f : .21f, 1f);
        box(BUTTON_X + 2f, y + 3f, 7f, BUTTON_H - 6f,
                active ? .98f : .53f, active ? .52f : .29f, .25f, 1f);
        if (active) box(BUTTON_X + BUTTON_W - 8f, y + 3f, 6f, BUTTON_H - 6f,
                .98f, .49f, .23f, 1f);
        centered(menuFont, label, 480f, y + 43f,
                active ? 1f : .82f, active ? .96f : .78f, .73f, 1f);
    }

    private void centered(BitmapFont font, String text, float cx, float baseline,
                          float r, float g, float b, float alpha) {
        font.setColor(r, g, b, alpha);
        glyph.setText(font, text);
        font.draw(batch, glyph, cx - glyph.width / 2f, baseline);
    }

    private void box(float x, float y, float w, float h,
                     float r, float g, float b, float alpha) {
        batch.setColor(r, g, b, alpha);
        batch.draw(pixel, x, y, w, h);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void stopTheme() {
        if (menuTheme != null) {
            menuTheme.stop();
            menuTheme.dispose();
            menuTheme = null;
        }
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void dispose() {
        if (disposed) return;
        disposed = true;
        stopTheme();
        titleFont.dispose();
        menuFont.dispose();
        batch.dispose();
        pixel.dispose();
    }
}
