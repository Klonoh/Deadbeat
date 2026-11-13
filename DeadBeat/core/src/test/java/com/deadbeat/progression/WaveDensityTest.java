package com.deadbeat.progression;

import com.deadbeat.entities.ZombieType;
import org.junit.Test;
import static org.junit.Assert.*;

/** Las cinco oleadas tienen más enemigos sin alterar la duración base. */
public class WaveDensityTest {
    @Test public void campaignSpawnsMoreZombies() {
        int[] expected = {24, 34, 48, 64, 82};
        Wave[] waves = WaveBuilder.campaign(30f);
        assertEquals(5, waves.length);
        int total = 0;
        for (int i = 0; i < waves.length; i++) {
            assertEquals(expected[i], waves[i].getTotal());
            assertEquals(30f, waves[i].getDuration(), 0f);
            total += waves[i].getTotal();
        }
        assertEquals(252, total);
    }

    @Test public void enemyCompositionGrowsAcrossCampaign() {
        Wave[] waves = WaveBuilder.campaign(30f);
        int[][] expected = {
            {24, 0, 0}, {26, 8, 0}, {36, 10, 2},
            {44, 16, 4}, {54, 20, 8}
        };
        for (int i = 0; i < 5; i++) {
            int normal = 0, fast = 0, tank = 0;
            for (int j = 0; j < waves[i].getTotal(); j++) {
                ZombieType type = waves[i].typeAt(j);
                if (type == ZombieType.NORMAL) normal++;
                else if (type == ZombieType.FAST) fast++;
                else if (type == ZombieType.TANK) tank++;
            }
            assertArrayEquals(expected[i], new int[]{normal, fast, tank});
        }
    }
}
