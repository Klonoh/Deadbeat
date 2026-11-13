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

    public static Wave[] campaign(float duration) {
        return new Wave[] {
            new WaveBuilder().duration(duration).normal(14).build(),
            new WaveBuilder().duration(duration).normal(16).fast(4).build(),
            new WaveBuilder().duration(duration).normal(20).fast(6).tank(1).build(),
            new WaveBuilder().duration(duration).normal(24).fast(10).tank(3).build(),
            new WaveBuilder().duration(duration).normal(28).fast(14).tank(5).build()
        };
    }
}
