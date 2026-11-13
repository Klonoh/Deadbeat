package com.deadbeat.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Reproductor de música aleatoria sin repetición dentro de cada ciclo.
 *
 * Carga automáticamente los archivos music/track_01.mp3 a track_99.mp3
 * que existan. Actualmente hay siete pistas, pero el código no necesita
 * cambios para agregar más. Los archivos nuevos se detectan al crear el
 * siguiente ciclo (o al reiniciar la aplicación).
 *
 * El reproductor es independiente de GameWorld: reiniciar la run con R
 * no altera ni la pista actual ni el orden pendiente. Solo dispose() detiene
 * la música. El callback de finalización se procesa en update(), en el hilo
 * de render, para mantener el uso de LibGDX en un único hilo.
 */
public final class SoundtrackPlayer implements Disposable {
    private static final int MAX_TRACK_NUMBER = 99;
    public static final float DEFAULT_VOLUME = 0.65f;

    private final Random random;
    private int[] order = new int[0];
    private int nextPosition;
    private int lastPlayedIndex = -1;

    private Music current;
    private int currentIndex = -1;
    private float volume = DEFAULT_VOLUME;
    private boolean muted;
    private volatile boolean nextRequested;
    private boolean disposed;

    public SoundtrackPlayer() {
        this(new Random());
    }

    /** Permite crear un reproductor con semilla fija para pruebas. */
    SoundtrackPlayer(Random random) {
        this.random = Objects.requireNonNull(random);
        shuffleNewCycle();
        playNext();
    }

    /** Se llama cada frame, incluso durante la pausa. */
    public void update() {
        if (disposed || !nextRequested) return;
        nextRequested = false;
        playNext();
    }

    private static String pathOf(int index) {
        return String.format(Locale.ROOT, "music/track_%02d.mp3", index + 1);
    }

    private void shuffleNewCycle() {
        List<Integer> available = new ArrayList<>();
        for (int index = 0; index < MAX_TRACK_NUMBER; index++) {
            if (Gdx.files.internal(pathOf(index)).exists()) {
                available.add(index);
            }
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

    private void playNext() {
        stopCurrent();
        if (disposed || order.length == 0) return;

        // Al agotarse el ciclo se detectan archivos nuevos y se mezclan.
        // El límite impide bucles infinitos si todos los MP3 fallan al abrirse.
        int attempts = 0;
        int maxAttempts = Math.max(1, order.length);
        while (attempts++ < maxAttempts) {
            if (nextPosition >= order.length) shuffleNewCycle();
            if (order.length == 0) return;
            int index = order[nextPosition++];
            FileHandle file = Gdx.files.internal(pathOf(index));
            if (!file.exists()) continue;

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
                return;
            } catch (RuntimeException ex) {
                if (Gdx.app != null) {
                    Gdx.app.error("DeadbeatMusic", "No se pudo reproducir " + file.path(), ex);
                }
                if (candidate != null) candidate.dispose();
                current = null;
                currentIndex = -1;
            }
        }
        // Si no hay ninguna canción válida, el juego continúa en silencio.
    }

    private void stopCurrent() {
        if (current == null) {
            currentIndex = -1;
            return;
        }
        current.stop();
        current.dispose();
        current = null;
        currentIndex = -1;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float amount) {
        if (!Float.isFinite(amount)) return;
        volume = Math.max(0f, Math.min(1f, amount));
        applyVolume();
    }

    public boolean isMuted() {
        return muted;
    }

    public void toggleMute() {
        muted = !muted;
        applyVolume();
    }

    /** Identificador 1..99 del archivo actual; 0 si no hay música. */
    public int getTrackNumber() {
        return currentIndex + 1;
    }

    /** Número de archivos descubiertos al iniciar el ciclo actual. */
    public int getAvailableTrackCount() {
        return order.length;
    }

    private float effectiveVolume() {
        return muted ? 0f : volume;
    }

    private void applyVolume() {
        if (current != null) current.setVolume(effectiveVolume());
    }

    @Override
    public void dispose() {
        if (disposed) return;
        disposed = true;
        nextRequested = false;
        stopCurrent();
    }
}
