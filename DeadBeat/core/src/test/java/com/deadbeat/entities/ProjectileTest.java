package com.deadbeat.entities;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProjectileTest {
    @Test
    public void directionIsNormalizedAndMovementUsesDelta() {
        Projectile projectile = new Projectile(10f, 20f, 3f, 4f, 22f);
        projectile.update(0.25f);
        assertEquals(70f, projectile.getX(), 0.001f);
        assertEquals(100f, projectile.getY(), 0.001f);
        assertEquals(22f, projectile.getDamage(), 0f);
    }

    @Test
    public void lifetimeExpiresAndCollisionCanExpireImmediately() {
        Projectile timed = new Projectile(0f, 0f, 1f, 0f, 22f);
        timed.update(1f);
        assertTrue(timed.isAlive());
        timed.update(0.5f);
        assertFalse(timed.isAlive());

        Projectile hit = new Projectile(0f, 0f, 1f, 0f, 22f);
        hit.expire();
        assertFalse(hit.isAlive());
    }

    @Test
    public void overlappingTargetUsesAFiniteFallbackDirection() {
        Projectile projectile = new Projectile(0f, 0f, 0f, 0f, 22f);
        projectile.update(0.25f);
        assertEquals(100f, projectile.getX(), 0.001f);
        assertEquals(0f, projectile.getY(), 0f);
    }
}
