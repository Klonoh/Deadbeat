package com.deadbeat.world;

import org.junit.Test;
import static org.junit.Assert.*;
import com.deadbeat.world.CityMap.CityBlock;
import com.deadbeat.world.CityMap.Obstacle;

public class CityMapTest {
    private static final float EPS = 0.0001f;

    @Test public void spawnIsFreeAndWorldIsLargerThanScreen() {
        CityMap m = new CityMap();
        assertTrue(CityMap.WORLD_SIZE > 960f);
        assertFalse(m.collidesCircle(CityMap.SPAWN + 64f, CityMap.SPAWN + 64f, 14f));
    }

    @Test public void buildingsAndAbandonedCarsAreSolid() {
        CityMap m = new CityMap();
        CityBlock block = m.getBlock(128, 128);
        for (int i = 0; i < block.obstacleCount(); i++) {
            Obstacle o = block.obstacle(i);
            assertTrue(m.collidesCircle(o.x + o.width * 0.5f,
                    o.y + o.height * 0.5f, 14f));
        }
    }

    @Test public void nearbyStreetIsNavigable() {
        CityMap m = new CityMap();
        float x = CityMap.SPAWN + 85f;
        float y = CityMap.SPAWN + 85f;
        assertFalse(m.collidesCircle(x, y, 14f));
    }

    @Test public void solidCollisionUsesCircleAgainstRectangle() {
        CityMap m = new CityMap();
        Obstacle b = m.getBlock(128, 128).obstacle(0);
        float cy = b.y + b.height * 0.5f;
        assertFalse(m.collidesCircle(b.x - 14.1f, cy, 14f));
        assertTrue(m.collidesCircle(b.x - 13.9f, cy, 14f));
    }

    @Test public void cityIsReproducibleAfterCacheEviction() {
        CityMap m = new CityMap();
        CityBlock original = m.getBlock(128, 128);
        Obstacle first = original.obstacle(0);
        float x = first.x, y = first.y, width = first.width;
        for (int i = 0; i < 130; i++) m.getBlock(i, 7);
        assertTrue(m.cachedBlockCount() <= 96);
        CityBlock regenerated = m.getBlock(128, 128);
        assertNotSame(original, regenerated);
        assertEquals(x, regenerated.obstacle(0).x, EPS);
        assertEquals(y, regenerated.obstacle(0).y, EPS);
        assertEquals(width, regenerated.obstacle(0).width, EPS);
    }

    @Test public void rejectsMovementBeyondWorldEdges() {
        CityMap m = new CityMap();
        assertTrue(m.collidesCircle(3f, 3f, 14f));
        assertTrue(m.collidesCircle(CityMap.WORLD_SIZE - 3f,
                CityMap.WORLD_SIZE - 3f, 14f));
    }
}
