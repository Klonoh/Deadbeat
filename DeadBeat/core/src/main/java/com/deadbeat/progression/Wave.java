package com.deadbeat.progression;

import com.deadbeat.entities.ZombieType;

/** configuración inmutable creada por WaveBuilder. */
public final class Wave {
    private final float duration;
    private final int normal;
    private final int fast;
    private final int tank;

    Wave(float duration, int normal, int fast, int tank) {
        this.duration = duration;
        this.normal = normal;
        this.fast = fast;
        this.tank = tank;
    }

    public float getDuration() {
        return duration;
    }

    public int getTotal() {
        return normal + fast + tank;
    }

    public ZombieType typeAt(int index) {
        if (index < 0 || index >= getTotal()) {
            throw new IndexOutOfBoundsException();
        }
        // los tanques y rápidos aparecen antes que los normales.
        if (index < tank) {
            return ZombieType.TANK;
        }
        if (index < tank + fast) {
            return ZombieType.FAST;
        }
        return ZombieType.NORMAL;
    }
}
