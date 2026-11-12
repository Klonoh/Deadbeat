package com.deadbeat.entities;

public final class Projectile {
    private float x, y, remaining = 1.5f;
    private final float vx, vy, damage;

    public Projectile(float x, float y, float dx, float dy, float damage) {
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        this.x = x;
        this.y = y;
        this.damage = damage;
        vx = length > 0 ? dx / length * 400 : 400;
        vy = length > 0 ? dy / length * 400 : 0;
    }

    public void update(float delta) {
        x += vx * delta;
        y += vy * delta;
        remaining -= delta;
    }

    public void expire() {
        remaining = 0;
    }

    public boolean isAlive() {
        return remaining > 0;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getDamage() {
        return damage;
    }

    public float getRadius() {
        return 5;
    }
}
