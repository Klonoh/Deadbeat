package com.deadbeat.entities;

import com.badlogic.gdx.math.Rectangle;
import com.deadbeat.movement.*;

import java.util.Objects;

/** enemigo con movimiento intercambiable y límites definidos por el mundo. */
public final class Zombie implements Damageable {
    private static final MovementStrategy CHASE = new ChaseMovement(),
            ZIGZAG = new ZigzagMovement();

    private final ZombieType type;
    private final Rectangle arena;
    private float x, y, health, age;
    private MovementStrategy strategy;

    public Zombie(float x, float y, ZombieType type, Rectangle arena) {
        this.type = Objects.requireNonNull(type);
        this.arena = new Rectangle(arena);
        this.x = x;
        this.y = y;
        health = type.health;
        setMovementStrategy(type == ZombieType.FAST ? ZIGZAG : CHASE);
        advance(0, 0, 0);
    }

    public void update(float delta, Player target) {
        if (!isAlive() || !Float.isFinite(delta) || delta <= 0)
            return;

        age += delta;
        if (type == ZombieType.FAST)
            setMovementStrategy(((int) (age / 2)) % 2 == 0 ? ZIGZAG : CHASE);

        strategy.move(this, target, delta);
    }

    public void advance(float dx, float dy, float delta) {
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0) {
            x += dx / length * type.speed * delta;
            y += dy / length * type.speed * delta;
        }

        x = Math.max(arena.x + getRadius(),
                Math.min(arena.x + arena.width - getRadius(), x));
        y = Math.max(arena.y + getRadius(),
                Math.min(arena.y + arena.height - getRadius(), y));
    }

    public void setMovementStrategy(MovementStrategy value) {
        strategy = Objects.requireNonNull(value);
    }

    public MovementStrategy getMovementStrategy() {
        return strategy;
    }

    public void takeDamage(float damage) {
        if (Float.isFinite(damage) && damage > 0)
            health = Math.max(0, health - damage);
    }

    public boolean isAlive() {
        return health > 0;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadius() {
        return type.radius;
    }

    public float getHealth() {
        return health;
    }

    public float getAge() {
        return age;
    }

    public ZombieType getType() {
        return type;
    }
}
