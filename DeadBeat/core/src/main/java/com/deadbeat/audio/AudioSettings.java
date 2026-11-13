package com.deadbeat.audio;

/** Configuracion global de audio compartida por menu y partida (Singleton). */
public final class AudioSettings {
    private static final AudioSettings INSTANCE = new AudioSettings();

    private float volume = SoundtrackPlayer.DEFAULT_VOLUME;
    private boolean muted;

    private AudioSettings() { }

    public static AudioSettings getInstance() {
        return INSTANCE;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        if (!Float.isFinite(volume)) return;
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void toggleMute() {
        muted = !muted;
    }

    public float getEffectiveVolume() {
        return muted ? 0f : volume;
    }
}
