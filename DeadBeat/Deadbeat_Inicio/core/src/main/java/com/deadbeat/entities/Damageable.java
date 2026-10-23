package com.deadbeat.entities;

/** Contract for entities that can receive damage and be defeated. */
public interface Damageable {

    /** Applies damage if the entity can currently receive it. */
    void takeDamage(float amount);

    /** Returns whether the entity has health remaining. */
    boolean isAlive();
}
