package com.deadbeat.world;

import com.deadbeat.entities.Projectile;
import com.deadbeat.entities.Zombie;
import org.junit.Test;
import static org.junit.Assert.*;

public class CombatIntegrationTest {
    @Test
    public void resetRestoresMapCombatAndEquipment() {
        GameWorld world = new GameWorld(false);
        assertTrue(world.getZombies().isEmpty());
        world.update(0.001f, 0f, 0f);
        for (Zombie zombie : world.getZombies()) {
            assertFalse(world.getCityMap().collidesCircle(
                    zombie.getX(), zombie.getY(), zombie.getRadius()));
            zombie.takeDamage(1000f);
        }
        world.update(0.01f, 0f, 0f);
        assertTrue(world.getZombies().isEmpty());
        world.getPlayer().takeDamage(20f);
        world.reset();
        assertTrue(world.getZombies().isEmpty());
        assertEquals(2, world.getPlayer().getWeapons().size());
        assertEquals(100f, world.getPlayer().getHealth(), 0f);
        assertEquals(0, world.getProjectileCount());
        assertEquals(0, world.getCityMap().brokenGarbageCount());
        assertEquals(0f, world.getElapsedTime(), 0f);
        world.dispose();
    }

    @Test
    public void automaticCombatDamagesAndRemovesEnemies() {
        GameWorld world = new GameWorld(false);
        for (int i = 0; i < 600; i++) {
            world.update(1f / 60f, 0f, 0f);
        }
        assertTrue(world.getZombies().size() < 1 + (int) (world.getElapsedTime() / 30f * 14));
        assertTrue(world.getPlayer().isAlive());
        world.dispose();
    }

    @Test
    public void areaAttackHitsEachNearbyEnemyOnce() {
        GameWorld world = new GameWorld(false);
        world.update(0.001f, 0f, 0f);
        assertFalse(world.strikeNearby(1f, 12f));
        float health = world.getZombies().get(0).getHealth();
        assertTrue(world.strikeNearby(1000f, 12f));
        assertEquals(health - 12f, world.getZombies().get(0).getHealth(), 0f);
        world.dispose();
    }
    @Test
    public void projectileImpactAndExpiryAreCleanedUp() {
        GameWorld world = new GameWorld(false);
        world.update(0.001f, 0f, 0f);
        Zombie target = world.getZombies().get(0);
        float health = target.getHealth();
        world.addProjectile(new Projectile(target.getX(), target.getY(), 1f, 0f, 5f));
        world.update(0.001f, 0f, 0f);
        assertEquals(health - 5f, target.getHealth(), 0f);
        world.reset();
        for (Zombie zombie : world.getZombies()) zombie.takeDamage(1000f);
        world.addProjectile(new Projectile(world.getPlayer().getX(), world.getPlayer().getY(), 1f, 0f, 5f));
        world.update(1.6f, 0f, 0f);
        assertEquals(0, world.getProjectileCount());
        world.dispose();
    }

    @Test
    public void contactUsesPlayerInvulnerability() {
        GameWorld world = new GameWorld(false);
        world.update(0.001f, 0f, 0f);
        Zombie target = world.getZombies().get(0);
        target.setMovementStrategy((zombie, player, delta) -> {
            float dx = player.getX() + 25f - zombie.getX();
            float dy = player.getY() - zombie.getY();
            zombie.advance(dx, dy, (float) Math.sqrt(dx * dx + dy * dy) / zombie.getType().speed);
        });
        world.update(0.001f, 0f, 0f);
        float health = world.getPlayer().getHealth();
        assertEquals(90f, health, 0f);
        world.update(0.001f, 0f, 0f);
        assertEquals(health, world.getPlayer().getHealth(), 0f);
        assertTrue(world.getPlayer().isInvulnerable());
        world.dispose();
    }
}
