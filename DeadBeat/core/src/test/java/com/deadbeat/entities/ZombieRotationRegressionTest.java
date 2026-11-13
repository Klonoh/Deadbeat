package com.deadbeat.entities;

import com.badlogic.gdx.math.Rectangle;
import com.deadbeat.world.CollisionMap;
import org.junit.Test;

import static org.junit.Assert.*;

/** Regresiones: no oscilar ni voltear al alcanzar al jugador o una pared. */
public class ZombieRotationRegressionTest {
    private final Rectangle arena = new Rectangle(0f, 0f, 2000f, 2000f);

    @Test public void commonZombieStopsAtContactWithoutReversingItsFacing() {
        Player target = new Player(600f, 500f, arena);
        Zombie zombie = new Zombie(360f, 500f, ZombieType.NORMAL, arena);
        float previousX = zombie.getX();
        int backwardsFrames = 0;
        for (int frame = 0; frame < 450; frame++) {
            zombie.update(1f / 60f, target);
            if (zombie.getX() < previousX - 0.00001f) backwardsFrames++;
            previousX = zombie.getX();
        }
        assertEquals(0, backwardsFrames);
        assertTrue(zombie.getX() < target.getX());
        assertEquals(1f, zombie.getFacingX(), 0.001f);
        assertFalse(zombie.isMoving());
        assertTrue(zombie.getX() + zombie.getRadius() + target.getRadius()
                >= target.getX() - 0.5f);
    }

    @Test public void blockedZombieKeepsFacingInsteadOfSpinning() {
        CollisionMap wall = (x, y, r) -> x + r >= 450f;
        Zombie zombie = new Zombie(437f, 400f, ZombieType.NORMAL, arena, wall);
        float x = zombie.getX();
        float fx = zombie.getFacingX(), fy = zombie.getFacingY();
        for (int i = 0; i < 100; i++) {
            zombie.advance(1f, i % 2 == 0 ? .02f : -.02f, 1f / 60f);
        }
        assertEquals(x, zombie.getX(), .0001f);
        assertEquals(fx, zombie.getFacingX(), .0001f);
        assertEquals(fy, zombie.getFacingY(), .0001f);
        assertFalse(zombie.isMoving());
    }

    @Test public void whenSlidingSpriteFacesRealMovement() {
        CollisionMap wall = (x, y, r) -> x + r > 450f && y > 100f;
        Zombie zombie = new Zombie(437f, 400f, ZombieType.NORMAL, arena, wall);
        zombie.advance(1f, 1f, .5f);
        assertTrue(zombie.getY() > 400f);
        assertEquals(0f, zombie.getFacingX(), 0.01f);
        assertEquals(1f, zombie.getFacingY(), 0.01f);
    }
}
