package com.deadbeat.entities;

import com.badlogic.gdx.math.Rectangle;
import com.deadbeat.movement.*;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/** Enemigo con movimiento intercambiable y apariencia fija desde su creación. */
public final class Zombie implements Damageable {
    private static final MovementStrategy CHASE = new ChaseMovement(),
            ZIGZAG = new ZigzagMovement();
    public static final int COMMON_VARIANTS = 3;

    private final ZombieType type;
    private final Rectangle arena;
    /** -1 para FAST/TANK, índice 0..2 para NORMAL. */
    private final int visualVariant;
    private float x, y, health, age;
    private float facingX = 0f, facingY = -1f;
    private boolean moving;
    private MovementStrategy strategy;

    public Zombie(float x, float y, ZombieType type, Rectangle arena) {
        this.type = Objects.requireNonNull(type);
        this.arena = new Rectangle(Objects.requireNonNull(arena));
        this.x = x;
        this.y = y;
        health = type.health;
        visualVariant = type == ZombieType.NORMAL
                ? ThreadLocalRandom.current().nextInt(COMMON_VARIANTS)
                : -1;
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
        moving = length > 0f && delta > 0f;
        if (moving) {
            facingX = dx / length;
            facingY = dy / length;
            x += facingX * type.speed * delta;
            y += facingY * type.speed * delta;
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

    /** Apariencia seleccionada al aparecer; nunca se vuelve a sortear al dibujar. */
    public int getVisualVariant() {
        return visualVariant;
    }

    public float getFacingX() {
        return facingX;
    }

    public float getFacingY() {
        return facingY;
    }

    public boolean isMoving() {
        return moving;
    }
}
