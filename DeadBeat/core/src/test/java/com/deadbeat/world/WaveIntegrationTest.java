package com.deadbeat.world;

import com.deadbeat.entities.Zombie;
import com.deadbeat.entities.ZombieType;
import com.deadbeat.progression.Wave;
import com.deadbeat.progression.WaveBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

public class WaveIntegrationTest {
    @Test
    public void builderPreservesCampaignAndValidatesConfiguration() {
        int[][] expected = {{24, 0, 0}, {26, 8, 0}, {36, 10, 2}, {44, 16, 4}, {54, 20, 8}};
        Wave[] waves = WaveBuilder.campaign(30f);
        for (int i = 0; i < waves.length; i++) {
            int[] counts = new int[3];
            for (int j = 0; j < waves[i].getTotal(); j++) {
                counts[waves[i].typeAt(j).ordinal()]++;
            }
            assertArrayEquals(expected[i], counts);
            assertEquals(30f, waves[i].getDuration(), 0f);
        }
        assertEquals(ZombieType.TANK, waves[4].typeAt(0));
        assertEquals(ZombieType.FAST, waves[4].typeAt(8));
        assertThrows(IllegalArgumentException.class, () -> new WaveBuilder().build());
        assertThrows(IllegalArgumentException.class, () -> new WaveBuilder().normal(-1).build());
        assertThrows(IllegalArgumentException.class, () -> new WaveBuilder().normal(1001).build());
        assertThrows(IllegalArgumentException.class, () -> WaveBuilder.campaign(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> WaveBuilder.campaign(0f));
    }

    @Test
    public void allWavesSpawnExactCountsAndReset() {
        GameWorld world = new GameWorld(false, 0.1f);
        java.util.Set<Zombie> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        int[] counts = new int[3];
        for (int frame = 0; frame < 10000 && !world.areWavesFinished(); frame++) {
            world.update(0.001f, 0f, 0f);
            for (Zombie zombie : world.getZombies()) {
                if (seen.add(zombie)) {
                    counts[zombie.getType().ordinal()]++;
                    assertFalse(world.getCityMap().collidesCircle(zombie.getX(), zombie.getY(), zombie.getRadius()));
                    float dx = zombie.getX() - world.getPlayer().getX();
                    float dy = zombie.getY() - world.getPlayer().getY();
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    assertTrue(distance > 319f && distance < 481f);
                }
                zombie.takeDamage(1000f);
            }
        }
        assertTrue(world.areWavesFinished());
        assertEquals(5, world.getCurrentWave());
        assertArrayEquals(new int[] {184, 54, 14}, counts);
        world.update(1f, 0f, 0f);
        assertTrue(world.getZombies().isEmpty());
        world.reset();
        assertEquals(1, world.getCurrentWave());
        assertEquals(0.1f, world.getWaveRemaining(), 0f);
        assertFalse(world.areWavesFinished());
        assertTrue(world.getZombies().isEmpty());
        world.update(0.001f, 0f, 0f);
        assertEquals(ZombieType.NORMAL, world.getZombies().get(0).getType());
        world.dispose();
    }
}
