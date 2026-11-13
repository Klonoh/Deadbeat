package com.deadbeat.entities;

import com.badlogic.gdx.math.Rectangle;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/** Comprueba la elección y persistencia de la apariencia del zombi común. */
public class ZombieAppearanceTest {
    private final Rectangle arena = new Rectangle(0f, 0f, 2000f, 2000f);

    @Test public void commonZombieKeepsItsVariantForItsLifetime() {
        Zombie zombie = new Zombie(400f, 500f, ZombieType.NORMAL, arena);
        int variant = zombie.getVisualVariant();
        assertTrue(variant >= 0 && variant < Zombie.COMMON_VARIANTS);
        zombie.advance(1f, 0f, 0.25f);
        zombie.takeDamage(4f);
        assertEquals(variant, zombie.getVisualVariant());
        assertTrue(zombie.isMoving());
        assertEquals(1f, zombie.getFacingX(), 0.001f);
        assertEquals(0f, zombie.getFacingY(), 0.001f);
    }

    @Test public void otherZombieTypesDoNotUseNormalSprites() {
        assertEquals(-1, new Zombie(400f, 500f, ZombieType.FAST, arena).getVisualVariant());
        assertEquals(-1, new Zombie(400f, 500f, ZombieType.TANK, arena).getVisualVariant());
    }

    @Test public void allThreeVariantsOccurAcrossIndependentSpawns() {
        // La probabilidad de que falte una variante después de 120 intentos es
        // despreciable; cada creación debe sortear su apariencia de nuevo.
        Set<Integer> variants = new HashSet<>();
        for (int i = 0; i < 120; i++) {
            variants.add(new Zombie(400f, 500f, ZombieType.NORMAL, arena).getVisualVariant());
        }
        assertEquals(Zombie.COMMON_VARIANTS, variants.size());
    }
}
