package com.deadbeat.world;

import com.badlogic.gdx.math.Rectangle;
import com.deadbeat.entities.ExperienceOrb;
import com.deadbeat.entities.Zombie;
import com.deadbeat.entities.ZombieType;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExperienceIntegrationTest {
    @SuppressWarnings("unchecked")
    private void defeated(GameWorld world, ZombieType type, float dx) throws Exception {
        Zombie zombie = new Zombie(world.getPlayer().getX() + dx,
                world.getPlayer().getY(), type,
                new Rectangle(0f, 0f, CityMap.WORLD_SIZE, CityMap.WORLD_SIZE));
        zombie.takeDamage(1000f);
        Field field = GameWorld.class.getDeclaredField("zombies");
        field.setAccessible(true);
        ((List<Zombie>) field.get(world)).add(zombie);
    }

    @Test
    public void eachDeathLeavesOnePersistentOrbWithItsReward() throws Exception {
        GameWorld world = new GameWorld(false);
        for (ZombieType type : ZombieType.values()) {
            world.reset();
            defeated(world, type, 100f);
            world.update(0.001f, 0f, 0f);
            assertEquals(1, world.getOrbs().size());
            ExperienceOrb orb = world.getOrbs().get(0);
            assertEquals(type.experience, orb.getAmount());
            assertEquals(world.getPlayer().getX() + 100f, orb.getX(), 0f);
            assertEquals(world.getPlayer().getY(), orb.getY(), 0f);
            for (int i = 0; i < 10; i++) world.update(0.001f, 0f, 0f);
            assertEquals(1, world.getOrbs().size());
            assertSame(orb, world.getOrbs().get(0));
            assertEquals(0, world.getPlayer().getExperience());
        }
        world.dispose();
    }

    @Test
    public void pickupUsesOriginalRadiusAndCannotAwardTwice() throws Exception {
        GameWorld world = new GameWorld(false);
        defeated(world, ZombieType.TANK, 33f);
        defeated(world, ZombieType.NORMAL, 34f);
        world.update(0.001f, 0f, 0f);
        assertEquals(3, world.getPlayer().getExperience());
        assertEquals(1, world.getOrbs().size());
        world.update(0.001f, 0f, 0f);
        assertEquals(3, world.getPlayer().getExperience());
        world.update(0.01f, 1f, 0f);
        assertEquals(4, world.getPlayer().getExperience());
        assertTrue(world.getOrbs().isEmpty());
        world.dispose();
    }

    @Test
    public void collectedRewardsGainMultipleLevelsAndResetEverything() throws Exception {
        GameWorld world = new GameWorld(false);
        for (int i = 0; i < 14; i++) defeated(world, ZombieType.TANK, 0f);
        defeated(world, ZombieType.FAST, 0f);
        world.update(0.001f, 0f, 0f);
        assertTrue(world.getOrbs().isEmpty());
        assertEquals(4, world.getPlayer().getLevel());
        assertEquals(7, world.getPlayer().getExperience());
        while (world.isChoosingUpgrade()) {
            world.chooseUpgrade(0);
        }
        defeated(world, ZombieType.NORMAL, 100f);
        world.update(0.001f, 0f, 0f);
        assertEquals(1, world.getOrbs().size());
        world.reset();
        assertTrue(world.getOrbs().isEmpty());
        assertEquals(1, world.getPlayer().getLevel());
        assertEquals(0, world.getPlayer().getExperience());
        assertEquals(1, world.getCurrentWave());
        world.dispose();
    }
}
