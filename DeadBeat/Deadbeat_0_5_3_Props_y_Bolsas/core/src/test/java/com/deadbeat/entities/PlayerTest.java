package com.deadbeat.entities;

import com.badlogic.gdx.math.Rectangle;
import org.junit.Test;
import static org.junit.Assert.*;

/** Logic-only tests: require neither a window nor a LibGDX graphics context. */
public class PlayerTest {
    private static final float EPS = 0.001f;

    private Player createPlayer() {
        return new Player(100f, 100f, new Rectangle(0f, 0f, 200f, 200f));
    }


    @Test public void walkingStateBeginsAndStopsWithInput() {
        Player p = createPlayer();
        assertFalse(p.isMoving());
        assertEquals(0f, p.getWalkTime(), EPS);
        p.update(0.12f, 1f, 0f);
        assertTrue(p.isMoving());
        assertEquals(0.12f, p.getWalkTime(), EPS);
        p.update(0.10f, 0f, 0f);
        assertFalse(p.isMoving());
        assertEquals(0f, p.getWalkTime(), EPS);
    }

    @Test public void facingPersistsAfterStopping() {
        Player p = createPlayer();
        p.update(0.10f, -1f, 1f);
        p.update(0.10f, 0f, 0f);
        assertTrue(p.getFacingX() < 0f);
        assertTrue(p.getFacingY() > 0f);
        assertFalse(p.isMoving());
    }

    @Test public void resetRestoresDefaultFacingAndIdleState() {
        Player p = createPlayer();
        p.update(0.10f, 1f, 1f);
        p.reset();
        assertEquals(0f, p.getFacingX(), EPS);
        assertEquals(-1f, p.getFacingY(), EPS);
        assertFalse(p.isMoving());
    }

    @Test public void startsAliveAtSpecifiedPosition() {
        Player p = createPlayer();
        assertEquals(100f, p.getX(), EPS);
        assertEquals(100f, p.getY(), EPS);
        assertEquals(100f, p.getHealth(), EPS);
        assertTrue(p.isAlive());
    }

    @Test public void cardinalMovementUsesSpeedTimesDelta() {
        Player p = createPlayer();
        p.update(0.25f, 1f, 0f);
        assertEquals(146.25f, p.getX(), EPS);
        assertEquals(100f, p.getY(), EPS);
    }

    @Test public void diagonalIsNotFasterThanCardinal() {
        Player cardinal = createPlayer();
        Player diagonal = createPlayer();
        cardinal.update(0.2f, 1f, 0f);
        diagonal.update(0.2f, 1f, 1f);
        float cardinalDistance = cardinal.getX() - 100f;
        float dx = diagonal.getX() - 100f;
        float dy = diagonal.getY() - 100f;
        assertEquals(cardinalDistance, (float) Math.sqrt(dx * dx + dy * dy), EPS);
    }

    @Test public void boundariesIncludePlayersRadius() {
        Player p = createPlayer();
        p.update(10f, 1f, 1f);
        assertEquals(186f, p.getX(), EPS);
        assertEquals(186f, p.getY(), EPS);
        p.update(10f, -1f, -1f);
        assertEquals(14f, p.getX(), EPS);
        assertEquals(14f, p.getY(), EPS);
    }

    @Test public void zeroDirectionStopsCompletely() {
        Player p = createPlayer();
        p.update(0.1f, 0f, 0f);
        assertEquals(100f, p.getX(), EPS);
        assertEquals(100f, p.getY(), EPS);
    }

    @Test public void cannotMoveWhileDead() {
        Player p = createPlayer();
        p.takeDamage(100f);
        assertFalse(p.isAlive());
        p.update(0.1f, 1f, 0f);
        assertEquals(100f, p.getX(), EPS);
        assertEquals(100f, p.getY(), EPS);
    }

    @Test public void damageAndShortImmunityWork() {
        Player p = createPlayer();
        p.takeDamage(20f);
        p.takeDamage(20f);
        assertEquals(80f, p.getHealth(), EPS);
        assertTrue(p.isInvulnerable());
        p.update(0.64f, 0f, 0f);
        p.takeDamage(20f);
        assertEquals(80f, p.getHealth(), EPS);
        p.update(0.01f, 0f, 0f);
        p.takeDamage(20f);
        assertEquals(60f, p.getHealth(), EPS);
    }

    @Test public void healthNeverBecomesNegative() {
        Player p = createPlayer();
        p.takeDamage(999f);
        assertEquals(0f, p.getHealth(), EPS);
        assertFalse(p.isAlive());
    }

    @Test public void healingRespectsMaxHealth() {
        Player p = createPlayer();
        p.takeDamage(30f);
        p.heal(200f);
        assertEquals(100f, p.getHealth(), EPS);
    }

    @Test public void resetRestoresStateAndSpawn() {
        Player p = createPlayer();
        p.setMoveSpeed(235f);
        p.update(0.1f, 1f, 0f);
        p.takeDamage(20f);
        p.reset();
        assertEquals(100f, p.getX(), EPS);
        assertEquals(100f, p.getY(), EPS);
        assertEquals(100f, p.getHealth(), EPS);
        assertEquals(185f, p.getMoveSpeed(), EPS);
        assertFalse(p.isInvulnerable());
    }

    @Test public void ignoresInvalidDamageAndMovementAxes() {
        Player p = createPlayer();
        p.takeDamage(-10f);
        p.takeDamage(Float.NaN);
        p.update(0.1f, Float.NaN, 0f);
        assertEquals(100f, p.getHealth(), EPS);
        assertEquals(100f, p.getX(), EPS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveSpeed() {
        createPlayer().setMoveSpeed(0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsArenaSmallerThanPlayer() {
        new Player(10f, 10f, new Rectangle(0f, 0f, 15f, 15f));
    }

    @Test public void doesNotRetainMutableArenaReference() {
        Rectangle arena = new Rectangle(0f, 0f, 200f, 200f);
        Player p = new Player(100f, 100f, arena);
        arena.set(400f, 400f, 800f, 800f);
        p.update(5f, 1f, 0f);
        assertEquals(186f, p.getX(), EPS);
    }
}
