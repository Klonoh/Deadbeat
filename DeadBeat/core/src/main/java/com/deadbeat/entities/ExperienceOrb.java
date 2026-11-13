package com.deadbeat.entities;

/** experiencia disponible para recoger en una posición del mundo. */
public final class ExperienceOrb {
    private final float x;
    private final float y;
    private final int amount;

    public ExperienceOrb(float x, float y, int amount) {
        this.x = x;
        this.y = y;
        this.amount = amount;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getAmount() {
        return amount;
    }
}
