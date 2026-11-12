package com.deadbeat.weapons;

import com.deadbeat.entities.Player;
import com.deadbeat.world.GameWorld;

/** método plantilla: solo un ataque exitoso activa el tiempo de espera compartido. */
public abstract class Weapon {
    private float damage, interval, remaining;

    protected Weapon(float damage, float interval) {
        this.damage = damage;
        this.interval = interval;
    }

    public final void update(float delta, Player player, GameWorld world) {
        if (!Float.isFinite(delta) || delta <= 0 || !player.isAlive())
            return;

        remaining = Math.max(0, remaining - delta);
        if (remaining == 0 && performAttack(player, world))
            remaining = interval;
    }

    protected abstract boolean performAttack(Player player, GameWorld world);

    public float getDamage() {
        return damage;
    }

    public float getInterval() {
        return interval;
    }

    public void improveDamage() {
        damage *= 1.2f;
    }

    public void improveRate() {
        interval = Math.max(0.15f, interval * 0.9f);
    }
}
