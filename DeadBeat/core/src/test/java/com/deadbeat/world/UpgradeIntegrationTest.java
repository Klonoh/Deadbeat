package com.deadbeat.world;

import com.badlogic.gdx.math.Rectangle;
import com.deadbeat.entities.Player;
import com.deadbeat.entities.ExperienceOrb;
import com.deadbeat.progression.Upgrade;
import com.deadbeat.progression.UpgradeSelection;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class UpgradeIntegrationTest {
    @Test
    public void allSixEffectsAccumulateAndReset() {
        Player player = new Player(100f, 100f, new Rectangle(0f, 0f, 200f, 200f));
        player.takeDamage(40f);
        for (int i = 0; i < 2; i++) {
            for (Upgrade upgrade : Upgrade.values()) upgrade.apply(player);
        }
        assertEquals(22f * 1.2f * 1.2f, player.getWeapons().get(0).getDamage(), .001f);
        assertEquals(.85f * .9f * .9f, player.getWeapons().get(0).getInterval(), .001f);
        assertEquals(12f * 1.2f * 1.2f, player.getWeapons().get(1).getDamage(), .001f);
        assertEquals(.55f * .9f * .9f, player.getWeapons().get(1).getInterval(), .001f);
        assertEquals(185f * 1.1f * 1.1f, player.getMoveSpeed(), .001f);
        assertEquals(140f, player.getMaxHealth(), 0f);
        assertEquals(100f, player.getHealth(), 0f);
        player.reset();
        assertEquals(22f, player.getWeapons().get(0).getDamage(), 0f);
        assertEquals(.85f, player.getWeapons().get(0).getInterval(), 0f);
        assertEquals(12f, player.getWeapons().get(1).getDamage(), 0f);
        assertEquals(.55f, player.getWeapons().get(1).getInterval(), 0f);
        assertEquals(185f, player.getMoveSpeed(), 0f);
        assertEquals(100f, player.getMaxHealth(), 0f);
    }

    @Test
    public void choicesConsumeOneLevelAndRespectFiveApplications() {
        Player player = new Player(100f, 100f, new Rectangle(0f, 0f, 200f, 200f));
        UpgradeSelection selection = new UpgradeSelection();
        selection.award(40);
        int[] counts = new int[6];
        selection.choose(-1, player);
        assertEquals(40, selection.getPending());
        for (int i = 0; i < 30; i++) {
            assertTrue(selection.getChoices().size() <= 3);
            Upgrade chosen = selection.getChoices().get(0);
            counts[chosen.ordinal()]++;
            selection.choose(0, player);
            if (i < 29) assertEquals(39 - i, selection.getPending());
        }
        assertArrayEquals(new int[] {5, 5, 5, 5, 5, 5}, counts);
        assertEquals(0, selection.getPending());
        assertTrue(selection.getChoices().isEmpty());
        selection.choose(0, player);
        selection.reset();
        selection.award(1);
        assertEquals(3, selection.getChoices().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void multipleLevelsFreezeSimulationUntilAllChoicesAreMade() throws Exception {
        GameWorld world = new GameWorld(false);
        Field field = GameWorld.class.getDeclaredField("orbs");
        field.setAccessible(true);
        ((List<ExperienceOrb>) field.get(world)).add(new ExperienceOrb(
                world.getPlayer().getX(), world.getPlayer().getY(), 40));
        world.update(.001f, 0f, 0f);
        assertEquals(3, world.getPendingUpgrades());
        assertEquals(4, world.getPlayer().getLevel());
        assertEquals(4, world.getPlayer().getExperience());
        float time = world.getElapsedTime();
        float x = world.getPlayer().getX();
        world.update(1f, 1f, 0f);
        assertEquals(time, world.getElapsedTime(), 0f);
        assertEquals(x, world.getPlayer().getX(), 0f);
        for (int pending = 2; pending >= 0; pending--) {
            world.chooseUpgrade(0);
            assertEquals(pending, world.getPendingUpgrades());
        }
        world.update(.01f, 0f, 0f);
        assertTrue(world.getElapsedTime() > time);
        world.reset();
        assertFalse(world.isChoosingUpgrade());
        assertTrue(world.getUpgradeChoices().isEmpty());
        assertEquals(1, world.getPlayer().getLevel());
        world.dispose();
    }
}
