package com.deadbeat.progression;

public final class WaveBuilder {
    private float duration = 30f;
    private int normal;
    private int fast;
    private int tank;

    public WaveBuilder duration(float value) {
        duration = value;
        return this;
    }

    public WaveBuilder normal(int value) {
        normal = value;
        return this;
    }

    public WaveBuilder fast(int value) {
        fast = value;
        return this;
    }

    public WaveBuilder tank(int value) {
        tank = value;
        return this;
    }

    public Wave build() {
        if (!Float.isFinite(duration) || duration <= 0f || normal < 0 || fast < 0
                || tank < 0 || (long) normal + fast + tank > 1000
                || normal + fast + tank == 0) {
            throw new IllegalArgumentException(
                    "La oleada requiere duracion positiva y entre 1 y 1000 enemigos");
        }
        return new Wave(duration, normal, fast, tank);
    }

    /** Cinco oleadas de 30 s, con más enemigos y una progresión más intensa. */
    public static Wave[] campaign(float duration) {
        return new Wave[] {
            new WaveBuilder().duration(duration).normal(24).build(),
            new WaveBuilder().duration(duration).normal(26).fast(8).build(),
            new WaveBuilder().duration(duration).normal(36).fast(10).tank(2).build(),
            new WaveBuilder().duration(duration).normal(44).fast(16).tank(4).build(),
            new WaveBuilder().duration(duration).normal(54).fast(20).tank(8).build()
        };
    }
}
