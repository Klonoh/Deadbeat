package com.deadbeat.audio;

import org.junit.Test;
import static org.junit.Assert.*;

public class AudioSettingsTest {
    @Test
    public void returnsSameInstance() {
        assertSame(AudioSettings.getInstance(), AudioSettings.getInstance());
    }

    @Test
    public void sharesVolumeAndMute() {
        AudioSettings first = AudioSettings.getInstance();
        AudioSettings second = AudioSettings.getInstance();
        float oldVolume = first.getVolume();
        boolean oldMuted = first.isMuted();
        try {
            first.setVolume(0.7f);
            first.setMuted(true);
            assertEquals(0.7f, second.getVolume(), 0.0001f);
            assertEquals(0f, second.getEffectiveVolume(), 0.0001f);
            second.setMuted(false);
            assertEquals(0.7f, first.getEffectiveVolume(), 0.0001f);
        } finally {
            first.setVolume(oldVolume);
            first.setMuted(oldMuted);
        }
    }

    @Test
    public void clampsVolumeAndRejectsNonFiniteValues() {
        AudioSettings settings = AudioSettings.getInstance();
        float oldVolume = settings.getVolume();
        try {
            settings.setVolume(2f);
            assertEquals(1f, settings.getVolume(), 0f);
            settings.setVolume(Float.NaN);
            assertEquals(1f, settings.getVolume(), 0f);
        } finally {
            settings.setVolume(oldVolume);
        }
    }
}
