package com.deadbeat.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;

/**
 * Banda sonora: mezcla sin repeticiones por vuelta y permite navegar con Q/E.
 * Los MP3 siguen el formato music/track_01.mp3 ... track_99.mp3.
 * Los titulos y artistas se configuran en music/tracklist.properties.
 *
 * R reinicia GameWorld, no este reproductor; las teclas Q/E no pausan.
 * El historial registra canciones oidas: Q retrocede, E recupera el historial
 * antes de consumir la siguiente pista de la lista aleatoria.
 */
public final class SoundtrackPlayer implements Disposable {
    private static final int MAX_TRACK_NUMBER = 99;
    private static final int MAX_HISTORY = 512;
    public static final float DEFAULT_VOLUME = 0.65f;

    private final Random random;
    private final Properties trackMetadata = new Properties();
    private final List<Integer> history = new ArrayList<>();
    private int historyPosition = -1;
    private int[] order = new int[0];
    private int nextPosition;
    private int lastPlayedIndex = -1;

    private Music current;
    private int currentIndex = -1;
    private final AudioSettings audioSettings = AudioSettings.getInstance();
    private volatile boolean nextRequested;
    private boolean disposed;

    public SoundtrackPlayer() {
        this(new Random());
    }

    /** Inyecta la semilla para pruebas sin depender del orden aleatorio del sistema. */
    SoundtrackPlayer(Random random) {
        this.random = Objects.requireNonNull(random);
        loadMetadata();
        shuffleNewCycle();
        playNextFresh();
    }

    /** Avanza al terminar una pista, en el hilo de render incluso si hay pausa. */
    public void update() {
        if (disposed || !nextRequested) return;
        nextRequested = false;
        nextTrack();
    }

    /** E: siguiente del historial si retrocedimos, o siguiente de la mezcla. */
    public void nextTrack() {
        if (disposed) return;
        nextRequested = false;
        if (historyPosition + 1 < history.size()) {
            if (playFromHistory(historyPosition + 1, 1)) return;
        }
        playNextFresh();
    }

    /** Q: vuelve a una pista que ya sono; no modifica el orden pendiente. */
    public void previousTrack() {
        if (disposed || historyPosition <= 0) return;
        nextRequested = false;
        playFromHistory(historyPosition - 1, -1);
    }

    private boolean playFromHistory(int position, int direction) {
        // El MP3 podria haberse borrado desde que fue escuchado.
        for (int at = position; at >= 0 && at < history.size(); at += direction) {
            if (playIndex(history.get(at))) {
                historyPosition = at;
                return true;
            }
        }
        return false;
    }

    private void playNextFresh() {
        if (disposed) return;
        int attempts = 0;
        int maxAttempts = Math.max(1, order.length);
        while (attempts++ < maxAttempts) {
            if (nextPosition >= order.length) shuffleNewCycle();
            if (order.length == 0) {
                stopCurrent();
                return;
            }
            int index = order[nextPosition++];
            if (playIndex(index)) {
                // Una cancion elegida de la mezcla se anade al historial real.
                if (historyPosition + 1 < history.size()) {
                    history.subList(historyPosition + 1, history.size()).clear();
                }
                history.add(index);
                if (history.size() > MAX_HISTORY) history.remove(0);
                historyPosition = history.size() - 1;
                return;
            }
        }
        // Sin pistas reproducibles, no bloquea el juego.
        stopCurrent();
    }

    private boolean playIndex(int index) {
        FileHandle file = Gdx.files.internal(pathOf(index));
        if (!file.exists()) return false;
        stopCurrent();
        Music candidate = null;
        try {
            candidate = Gdx.audio.newMusic(file);
            candidate.setLooping(false);
            candidate.setVolume(effectiveVolume());
            current = candidate;
            currentIndex = index;
            candidate.setOnCompletionListener(music -> {
                if (music == current && !disposed) nextRequested = true;
            });
            candidate.play();
            lastPlayedIndex = index;
            return true;
        } catch (RuntimeException ex) {
            if (Gdx.app != null) {
                Gdx.app.error("DeadbeatMusic", "No se pudo reproducir " + file.path(), ex);
            }
            if (candidate != null) candidate.dispose();
            current = null;
            currentIndex = -1;
            return false;
        }
    }

    private static String pathOf(int index) {
        return String.format(Locale.ROOT, "music/track_%02d.mp3", index + 1);
    }

    private void shuffleNewCycle() {
        List<Integer> available = new ArrayList<>();
        for (int index = 0; index < MAX_TRACK_NUMBER; index++) {
            if (Gdx.files.internal(pathOf(index)).exists()) available.add(index);
        }
        Collections.shuffle(available, random);
        if (available.size() > 1 && available.get(0) == lastPlayedIndex) {
            int swapAt = 1 + random.nextInt(available.size() - 1);
            Collections.swap(available, 0, swapAt);
        }
        order = new int[available.size()];
        for (int i = 0; i < available.size(); i++) order[i] = available.get(i);
        nextPosition = 0;
    }

    private void loadMetadata() {
        FileHandle catalog = Gdx.files.internal("music/tracklist.properties");
        if (!catalog.exists()) return;
        try (Reader reader = catalog.reader("UTF-8")) {
            trackMetadata.load(reader);
        } catch (IOException | RuntimeException ex) {
            if (Gdx.app != null) Gdx.app.error("DeadbeatMusic", "Catalogo musical no disponible", ex);
        }
    }

    /** El HUD puede mostrar los metadatos sin leer ni decodificar el MP3. */
    public String getCurrentTitle() {
        if (currentIndex < 0) return "SIN CANCION";
        return trackMetadata.getProperty(metadataKey(currentIndex, "title"),
                String.format(Locale.ROOT, "PISTA %02d", currentIndex + 1));
    }

    public String getCurrentArtist() {
        if (currentIndex < 0) return "AGREGA MUSICA A ASSETS/MUSIC";
        return trackMetadata.getProperty(metadataKey(currentIndex, "artist"), "ARTISTA DESCONOCIDO");
    }

    private static String metadataKey(int index, String field) {
        return String.format(Locale.ROOT, "track_%02d.%s", index + 1, field);
    }

    public int getTrackNumber() { return currentIndex + 1; }
    public int getAvailableTrackCount() { return order.length; }
    public float getVolume() { return audioSettings.getVolume(); }
    public boolean isMuted() { return audioSettings.isMuted(); }

    public void setVolume(float amount) {
        audioSettings.setVolume(amount);
        applyVolume();
    }

    public void toggleMute() {
        audioSettings.toggleMute();
        applyVolume();
    }

    private float effectiveVolume() { return audioSettings.getEffectiveVolume(); }

    private void applyVolume() {
        if (current != null) current.setVolume(effectiveVolume());
    }

    private void stopCurrent() {
        if (current != null) {
            current.stop();
            current.dispose();
            current = null;
        }
        currentIndex = -1;
    }

    @Override public void dispose() {
        if (disposed) return;
        disposed = true;
        nextRequested = false;
        stopCurrent();
        history.clear();
    }
}
