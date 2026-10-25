package com.deadbeat.entities;

public enum ZombieType {
    NORMAL(30, 75, 13, 10, 1),
    FAST(18, 120, 10, 7, 1),
    TANK(90, 45, 19, 16, 3);

    public final float health, speed, radius, damage;
    public final int experience;

    ZombieType(float health, float speed, float radius, float damage, int experience) {
        this.health = health;
        this.speed = speed;
        this.radius = radius;
        this.damage = damage;
        this.experience = experience;
    }
}
