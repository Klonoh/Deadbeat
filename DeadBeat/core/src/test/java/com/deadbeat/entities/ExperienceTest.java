package com.deadbeat.entities;

import com.badlogic.gdx.math.Rectangle;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExperienceTest {
    private Player player() {
        return new Player(100f, 100f, new Rectangle(0f, 0f, 200f, 200f));
    }

    @Test
    public void accumulatesExperienceAndUsesOriginalThresholds() {
        Player player = player();
        assertEquals(1, player.getLevel());
        assertEquals(0, player.getExperience());
        assertEquals(8, player.getExperienceToNextLevel());
        assertEquals(0, player.addExperience(3));
        assertEquals(3, player.getExperience());
        assertEquals(1, player.addExperience(5));
        assertEquals(2, player.getLevel());
        assertEquals(0, player.getExperience());
        assertEquals(12, player.getExperienceToNextLevel());
    }

    @Test
    public void gainsMultipleLevelsWithoutChangingCombatStatistics() {
        Player player = player();
        assertEquals(3, player.addExperience(40));
        assertEquals(4, player.getLevel());
        assertEquals(4, player.getExperience());
        assertEquals(20, player.getExperienceToNextLevel());
        assertEquals(Player.DEFAULT_MAX_HEALTH, player.getMaxHealth(), 0f);
        assertEquals(Player.DEFAULT_SPEED, player.getMoveSpeed(), 0f);
        assertEquals(22f, player.getWeapons().get(0).getDamage(), 0f);
        assertEquals(12f, player.getWeapons().get(1).getDamage(), 0f);
    }

    @Test
    public void ignoresNonpositiveAmountsAndResetClearsProgress() {
        Player player = player();
        player.addExperience(15);
        assertEquals(0, player.addExperience(0));
        assertEquals(0, player.addExperience(-10));
        assertEquals(7, player.getExperience());
        player.reset();
        assertEquals(1, player.getLevel());
        assertEquals(0, player.getExperience());
        assertEquals(8, player.getExperienceToNextLevel());
    }

    @Test
    public void orbPreservesWorldCoordinatesAndOriginalRewards() {
        assertEquals(1, ZombieType.NORMAL.experience);
        assertEquals(1, ZombieType.FAST.experience);
        assertEquals(3, ZombieType.TANK.experience);
        for (ZombieType type : ZombieType.values()) {
            ExperienceOrb orb = new ExperienceOrb(65600f, 65700f, type.experience);
            assertEquals(65600f, orb.getX(), 0f);
            assertEquals(65700f, orb.getY(), 0f);
            assertEquals(type.experience, orb.getAmount());
        }
    }
}
