package com.deadbeat.entities;

import com.badlogic.gdx.math.Rectangle;
import com.deadbeat.movement.*;
import com.deadbeat.world.CollisionMap;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/** Enemigo con movimiento intercambiable y apariencia fija desde su creación. */
public final class Zombie implements Damageable {
    private static final MovementStrategy CHASE = new ChaseMovement(),
            ZIGZAG = new ZigzagMovement();
    public static final int COMMON_VARIANTS = 3;
    public static final int TANK_VARIANTS = 2;
    public static final int FAST_VARIANTS = 2;

    private final ZombieType type;
    private final Rectangle arena;
    private final CollisionMap collisionMap;
    /** Índice visual único desde el spawn, independiente para cada tipo de enemigo. */
    private final int visualVariant;
    private float x, y, health, age;
    private float facingX = 0f, facingY = -1f;
    private boolean moving;
    private MovementStrategy strategy;

    /** Constructor compatible con los tests y usos existentes sin mapa. */
    public Zombie(float x, float y, ZombieType type, Rectangle arena) {
        this(x, y, type, arena, null);
    }

    /** En el juego, cada enemigo recibe el mismo mapa de colisiones que el jugador. */
    public Zombie(float x, float y, ZombieType type, Rectangle arena, CollisionMap collisionMap) {
        this.type = Objects.requireNonNull(type);
        this.collisionMap = collisionMap;
        this.arena = new Rectangle(Objects.requireNonNull(arena));
        this.x = x;
        this.y = y;
        health = type.health;
        visualVariant = type == ZombieType.NORMAL
                ? ThreadLocalRandom.current().nextInt(COMMON_VARIANTS)
                : type == ZombieType.TANK
                ? ThreadLocalRandom.current().nextInt(TANK_VARIANTS)
                : ThreadLocalRandom.current().nextInt(FAST_VARIANTS);
        setMovementStrategy(type == ZombieType.FAST ? ZIGZAG : CHASE);
        advance(0, 0, 0);
    }

    public void update(float delta, Player target) {
        if (!isAlive() || !Float.isFinite(delta) || delta <= 0f || target == null)
            return;

        age += delta;
        if (type == ZombieType.FAST)
            setMovementStrategy(((int) (age / 2f)) % 2 == 0 ? ZIGZAG : CHASE);

        // Do not cross the player's center. Previously enemies pursued the exact
        // center, overshot it, reversed direction on the next tick and appeared
        // to spin in place while dealing damage.
        final float dx = target.getX() - x;
        final float dy = target.getY() - y;
        final float separation = (float) Math.sqrt(dx * dx + dy * dy);
        final float contactDistance = getRadius() + target.getRadius() - 0.25f;
        if (separation <= contactDistance || !Float.isFinite(separation)) {
            moving = false;
            return;
        }
        // Cap the travel at the first point of contact, including large deltas.
        final float maximumStepSeconds = (separation - contactDistance) / type.speed;
        strategy.move(this, target, Math.min(delta, maximumStepSeconds));
    }

    public void advance(float dx, float dy, float delta) {
        if (!Float.isFinite(dx) || !Float.isFinite(dy)
                || !Float.isFinite(delta) || delta <= 0f) {
            moving = false;
            return;
        }
        final float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0f || !Float.isFinite(length)) {
            moving = false;
            return;
        }

        final float desiredX = dx / length;
        final float desiredY = dy / length;
        final float distance = type.speed * delta;
        // Small substeps stop fast zombies tunnelling through thin fences.
        // Independent X/Y checks preserve sliding along solid walls.
        final float maxStep = Math.max(2f, getRadius() * 0.4f);
        final int steps = Math.max(1, (int) Math.ceil(distance / maxStep));
        final float stepX = desiredX * distance / steps;
        final float stepY = desiredY * distance / steps;
        final float oldX = x;
        final float oldY = y;
        for (int step = 0; step < steps; step++) {
            final float nextX = Math.max(arena.x + getRadius(),
                    Math.min(arena.x + arena.width - getRadius(), x + stepX));
            if (collisionMap == null || !collisionMap.collidesCircle(nextX, y, getRadius()))
                x = nextX;
            final float nextY = Math.max(arena.y + getRadius(),
                    Math.min(arena.y + arena.height - getRadius(), y + stepY));
            if (collisionMap == null || !collisionMap.collidesCircle(x, nextY, getRadius()))
                y = nextY;
        }
        final float movedX = x - oldX;
        final float movedY = y - oldY;
        final float movedDistance = (float) Math.sqrt(movedX * movedX + movedY * movedY);
        // Tiny collision-correction jitter is not actual walking. Do not use
        // such micro movements to change the sprite's heading or animation.
        moving = movedDistance > 0.08f;
        if (!moving) return; // blocked or jittering: retain facing.

        // Face the direction ACTUALLY travelled, not the direction requested by
        // chase AI. Hysteresis avoids flicker between adjacent 45-degree views
        // near walls and when the fast zombie changes its zigzag direction.
        final float actualX = movedX / movedDistance;
        final float actualY = movedY / movedDistance;
        if (facingX * actualX + facingY * actualY < 0.82f) {
            facingX = actualX;
            facingY = actualY;
        }
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

    /** Apariencia seleccionada al aparecer; nunca se vuelve a sortear al dibujar.
     * NORMAL usa 0..COMMON_VARIANTS-1, TANK usa 0..TANK_VARIANTS-1,
     * y FAST usa 0..FAST_VARIANTS-1. */
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
