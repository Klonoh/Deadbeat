package com.deadbeat.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;

/**
 * Player's gameplay state. Coordinates represent the CENTER of its circular hitbox.
 *
 * Input is read in update(delta), but the overload receiving movement axes can be
 * exercised without creating a LibGDX window (e.g. in automated tests).
 * This class does not allocate textures, render anything or own disposable assets.
 */
public final class Player implements Damageable {

    public static final float DEFAULT_SPEED = 185f;
    public static final float DEFAULT_MAX_HEALTH = 100f;
    public static final float DEFAULT_RADIUS = 14f;
    public static final float INVULNERABILITY_SECONDS = 0.65f;

    private final float arenaLeft;
    private final float arenaBottom;
    private final float arenaRight;
    private final float arenaTop;
    private final float spawnX;
    private final float spawnY;

    private float x;
    private float y;
    private final float radius;
    private float moveSpeed;
    private float health;
    private float maxHealth;
    private float invulnerabilityRemaining;
    private float facingX;
    private float facingY;
    private boolean moving;
    private float walkTime;

    /**
     * @param centerX initial center X coordinate
     * @param centerY initial center Y coordinate
     * @param arena interior rectangle allowed for the entire player hitbox
     */
    public Player(float centerX, float centerY, Rectangle arena) {
        if (arena == null || !Float.isFinite(centerX) || !Float.isFinite(centerY)
                || !Float.isFinite(arena.x) || !Float.isFinite(arena.y)
                || !Float.isFinite(arena.width) || !Float.isFinite(arena.height)
                || arena.width < 2f * DEFAULT_RADIUS
                || arena.height < 2f * DEFAULT_RADIUS) {
            throw new IllegalArgumentException("Invalid player spawn or playable arena");
        }

        arenaLeft = arena.x;
        arenaBottom = arena.y;
        arenaRight = arena.x + arena.width;
        arenaTop = arena.y + arena.height;
        radius = DEFAULT_RADIUS;

        // Store a legal spawn to guarantee that reset cannot place the player out of bounds.
        spawnX = clamp(centerX, arenaLeft + radius, arenaRight - radius);
        spawnY = clamp(centerY, arenaBottom + radius, arenaTop - radius);
        reset();
    }

    /** Updates the player using WASD or arrow keys. Opposite keys cancel each other. */
    public void update(float delta) {
        float horizontal = 0f;
        float vertical = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            horizontal -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            horizontal += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vertical -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vertical += 1f;
        }

        update(delta, horizontal, vertical);
    }

    /**
     * Advances simulation with explicit directional axes, independently of keyboard.
     * Axes are expected in [-1, 1]. Diagonal direction is normalized, not accelerated.
     */
    public void update(float delta, float horizontal, float vertical) {
        if (!Float.isFinite(delta) || delta <= 0f) {
            return;
        }
        invulnerabilityRemaining = Math.max(0f, invulnerabilityRemaining - delta);
        if (!isAlive()) {
            moving = false;
            walkTime = 0f;
            return;
        }

        if (!Float.isFinite(horizontal) || !Float.isFinite(vertical)) {
            moving = false;
            walkTime = 0f;
            return;
        }

        // Clamp externally supplied axes (e.g. a future analog controller).
        horizontal = clamp(horizontal, -1f, 1f);
        vertical = clamp(vertical, -1f, 1f);
        float lengthSquared = horizontal * horizontal + vertical * vertical;
        if (lengthSquared <= 0f) {
            moving = false;
            walkTime = 0f;
            return;
        }
        if (lengthSquared > 1f) {
            float inverseLength = 1f / (float) Math.sqrt(lengthSquared);
            horizontal *= inverseLength;
            vertical *= inverseLength;
        }

        moving = true;
        // Keep the animation phase small to avoid losing float precision in long sessions.
        walkTime = (walkTime + delta) % 1.76f;

        // Remember the last direction for the sprite and future attacks.
        facingX = horizontal;
        facingY = vertical;

        x = clamp(x + horizontal * moveSpeed * delta,
                arenaLeft + radius, arenaRight - radius);
        y = clamp(y + vertical * moveSpeed * delta,
                arenaBottom + radius, arenaTop - radius);
    }

    @Override
    public void takeDamage(float amount) {
        if (!Float.isFinite(amount) || amount <= 0f
                || !isAlive() || invulnerabilityRemaining > 0f) {
            return;
        }
        health = Math.max(0f, health - amount);
        invulnerabilityRemaining = isAlive() ? INVULNERABILITY_SECONDS : 0f;
        if (!isAlive()) {
            moving = false;
            walkTime = 0f;
        }
    }

    /** Restores some life without exceeding its maximum. */
    public void heal(float amount) {
        if (isAlive() && Float.isFinite(amount) && amount > 0f) {
            health = Math.min(maxHealth, health + amount);
        }
    }

    /** Prepared for the movement-speed upgrade in later iterations. */
    public void setMoveSpeed(float newSpeed) {
        if (!Float.isFinite(newSpeed) || newSpeed <= 0f) {
            throw new IllegalArgumentException("Move speed must be finite and positive");
        }
        moveSpeed = newSpeed;
    }

    /** Resets all mutable state, including position and temporary immunity. */
    public void reset() {
        x = spawnX;
        y = spawnY;
        moveSpeed = DEFAULT_SPEED;
        maxHealth = DEFAULT_MAX_HEALTH;
        health = maxHealth;
        invulnerabilityRemaining = 0f;
        facingX = 0f;
        facingY = -1f;
        moving = false;
        walkTime = 0f;
    }

    @Override
    public boolean isAlive() {
        return health > 0f;
    }

    public boolean isInvulnerable() {
        return invulnerabilityRemaining > 0f;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getRadius() { return radius; }
    public float getMoveSpeed() { return moveSpeed; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public float getInvulnerabilityRemaining() { return invulnerabilityRemaining; }
    public float getFacingX() { return facingX; }
    public float getFacingY() { return facingY; }
    public boolean isMoving() { return moving; }
    public float getWalkTime() { return walkTime; }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
