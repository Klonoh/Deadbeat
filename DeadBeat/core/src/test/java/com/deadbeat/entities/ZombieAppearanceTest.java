package com.deadbeat.entities;

import com.badlogic.gdx.math.Rectangle;
import com.deadbeat.world.CollisionMap;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/** Apariencia aleatoria estable y movimiento con obstaculos. */
public class ZombieAppearanceTest {
    private final Rectangle arena = new Rectangle(0f, 0f, 2000f, 2000f);

    @Test public void commonZombieKeepsVariant() {
        Zombie zombie = new Zombie(400f, 500f, ZombieType.NORMAL, arena);
        int variant = zombie.getVisualVariant();
        assertTrue(variant >= 0 && variant < Zombie.COMMON_VARIANTS);
        zombie.advance(1f, 0f, .25f);
        zombie.takeDamage(4f);
        assertEquals(variant, zombie.getVisualVariant());
        assertEquals(1f, zombie.getFacingX(), .001f);
    }

    @Test public void tankZombieKeepsVariant() {
        Zombie zombie = new Zombie(400f, 500f, ZombieType.TANK, arena);
        int variant = zombie.getVisualVariant();
        assertTrue(variant >= 0 && variant < Zombie.TANK_VARIANTS);
        zombie.advance(0f, 1f, 1f);
        zombie.takeDamage(4f);
        assertEquals(variant, zombie.getVisualVariant());
    }

    @Test public void fastZombieKeepsVariantAfterMovingAndTakingDamage() {
        Zombie fast = new Zombie(400f, 500f, ZombieType.FAST, arena);
        int variant = fast.getVisualVariant();
        assertTrue(variant >= 0 && variant < Zombie.FAST_VARIANTS);
        fast.advance(0f, 1f, .2f);
        fast.takeDamage(3f);
        assertEquals(variant, fast.getVisualVariant());
    }

    @Test public void bothFastVariantsAppearAcrossSpawns() {
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 120; i++)
            seen.add(new Zombie(400f, 500f, ZombieType.FAST, arena).getVisualVariant());
        assertEquals(Zombie.FAST_VARIANTS, seen.size());
    }

    @Test public void commonAndTankAppearancesAreRandom() {
        Set<Integer> normal = new HashSet<>();
        Set<Integer> tank = new HashSet<>();
        for (int i = 0; i < 120; i++) {
            normal.add(new Zombie(400f, 500f, ZombieType.NORMAL, arena).getVisualVariant());
            tank.add(new Zombie(400f, 500f, ZombieType.TANK, arena).getVisualVariant());
        }
        assertEquals(3, normal.size());
        assertEquals(2, tank.size());
    }

    @Test public void zombiesCannotPassThroughWallAndSlideAlongIt() {
        CollisionMap wall = (x, y, r) -> x + r > 450f && x - r < 470f
                && y + r > 100f && y - r < 900f;
        for (ZombieType type : ZombieType.values()) {
            Zombie z = new Zombie(400f, 400f, type, arena, wall);
            z.advance(1f, 0f, 1f);
            assertTrue("Wall penetrated: " + type, z.getX() + z.getRadius() <= 450.01f);
            Zombie slide = new Zombie(400f, 400f, type, arena, wall);
            slide.advance(1f, 1f, 1f);
            assertTrue(slide.getX() + slide.getRadius() <= 450.01f);
            assertTrue("No sliding: " + type, slide.getY() > 400f);
        }
    }

    @Test public void fastZombieDoesNotTunnelThroughThinWall() {
        CollisionMap thinWall = (x, y, r) -> x + r > 425f && x - r < 427f;
        Zombie z = new Zombie(400f, 400f, ZombieType.FAST, arena, thinWall);
        z.advance(1f, 0f, 1.2f);
        assertTrue(z.getX() + z.getRadius() <= 425.01f);
    }
}
