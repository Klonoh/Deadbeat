package com.deadbeat.entities;

import com.badlogic.gdx.math.Rectangle;
import com.deadbeat.world.CityMap;
import com.deadbeat.world.CityMap.Obstacle;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlayerWorldCollisionTest {
    private static final float EPS = 0.0001f;

    @Test public void playerCannotWalkThroughBuilding() {
        CityMap city = new CityMap();
        Obstacle building = city.getBlock(128, 128).obstacle(0);
        float x = building.x - Player.DEFAULT_RADIUS - 5f;
        float y = building.y + building.height * 0.5f;
        Player player = new Player(x, y,
                new Rectangle(0f, 0f, CityMap.WORLD_SIZE, CityMap.WORLD_SIZE));
        player.update(0.05f, 1f, 0f, city);
        assertEquals(x, player.getX(), EPS);
        assertFalse(player.isMoving());
    }

    @Test public void diagonalMovementSlidesAlongBuildingWall() {
        CityMap city = new CityMap();
        Obstacle building = city.getBlock(128, 128).obstacle(0);
        float x = building.x - Player.DEFAULT_RADIUS - 1f;
        float y = building.y + building.height * 0.5f;
        Player player = new Player(x, y,
                new Rectangle(0f, 0f, CityMap.WORLD_SIZE, CityMap.WORLD_SIZE));
        player.update(0.05f, 1f, 1f, city);
        assertEquals(x, player.getX(), EPS);
        assertTrue(player.getY() > y);
        assertTrue(player.isMoving());
    }

    @Test public void walkingAlongClearRoadWorks() {
        CityMap city = new CityMap();
        Player player = new Player(CityMap.SPAWN + 64f, CityMap.SPAWN + 64f,
                new Rectangle(0f, 0f, CityMap.WORLD_SIZE, CityMap.WORLD_SIZE));
        player.update(0.05f, 0f, 1f, city);
        assertEquals(CityMap.SPAWN + 64f + 9.25f, player.getY(), 0.015f);
    }
}
