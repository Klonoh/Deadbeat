package com.deadbeat.weapons;

import com.badlogic.gdx.math.Rectangle;
import com.deadbeat.entities.Player;
import com.deadbeat.world.GameWorld;
import org.junit.Test;

import static org.junit.Assert.*;

/** prueba el método plantilla sin gráficos, enemigos ni un mundo en ejecución. */
public class WeaponTest {
    private static final class TestWeapon extends Weapon {
        private int attempts;
        private boolean attackAvailable;

        TestWeapon() {
            super(10f, 0.5f);
        }

        @Override
        protected boolean performAttack(Player player, GameWorld world) {
            attempts++;
            return attackAvailable;
        }
    }

    private Player player() {
        return new Player(100f, 100f, new Rectangle(0f, 0f, 200f, 200f));
    }

    @Test
    public void failedAttackDoesNotConsumeCooldown() {
        TestWeapon weapon = new TestWeapon();
        Player player = player();
        weapon.update(0.1f, player, null);
        weapon.attackAvailable = true;
        weapon.update(0.1f, player, null);
        assertEquals(2, weapon.attempts);
    }

    @Test
    public void successfulAttackWaitsForTheSharedInterval() {
        TestWeapon weapon = new TestWeapon();
        Player player = player();
        weapon.attackAvailable = true;
        weapon.update(0.1f, player, null);
        weapon.update(0.25f, player, null);
        assertEquals(1, weapon.attempts);
        weapon.update(0.25f, player, null);
        assertEquals(2, weapon.attempts);
    }

    @Test
    public void deadPlayerAndInvalidDeltaDoNotAttack() {
        TestWeapon weapon = new TestWeapon();
        Player player = player();
        weapon.update(0f, player, null);
        weapon.update(-1f, player, null);
        weapon.update(Float.NaN, player, null);
        weapon.update(Float.POSITIVE_INFINITY, player, null);
        player.takeDamage(100f);
        weapon.update(0.1f, player, null);
        assertEquals(0, weapon.attempts);
    }

    @Test
    public void rateUpgradeHasAMinimumInterval() {
        TestWeapon weapon = new TestWeapon();
        for (int i = 0; i < 100; i++) {
            weapon.improveRate();
        }
        assertEquals(0.15f, weapon.getInterval(), 0.0001f);
    }
}
