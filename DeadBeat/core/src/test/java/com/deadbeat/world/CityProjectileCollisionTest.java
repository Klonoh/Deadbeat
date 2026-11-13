package com.deadbeat.world;

import com.deadbeat.entities.Projectile;
import org.junit.Test;

import static org.junit.Assert.*;

/** Regresiones: objetos y geometria de proyectiles en la ciudad. */
public class CityProjectileCollisionTest {
    @Test public void solidDecorationsAndBuildingsBlockAtTheirCenters() {
        CityMap map = new CityMap();
        boolean checkedFence = false, checkedBuilding = false;
        for (int bx = -3; bx <= 3; bx++) for (int by = -3; by <= 3; by++) {
            CityMap.CityBlock block = map.getBlock(bx, by);
            for (int k = 0; k < block.obstacleCount(); k++) {
                CityMap.Obstacle o = block.obstacle(k);
                if (o.kind == CityMap.Kind.FENCE) checkedFence = true;
                if (o.kind == CityMap.Kind.BUILDING) checkedBuilding = true;
                if (o.kind == CityMap.Kind.FENCE || o.kind == CityMap.Kind.BUILDING)
                    assertTrue(map.collidesCircle(o.x + o.width/2f, o.y + o.height/2f, 2f));
            }
        }
        assertTrue(checkedFence);
        assertTrue(checkedBuilding);
    }

    @Test public void projectileSegmentSamplesCatchThinFence() {
        // Replica la misma resolucion espacial usada por GameWorld.updateCombat.
        CollisionMap fence = (x, y, r) -> x + r > 430f && x - r < 432f;
        Projectile p = new Projectile(400f, 400f, 1f, 0f, 10f);
        float x0 = p.getX(), y0 = p.getY();
        p.update(.12f); // 48 unidades: el extremo queda tras la valla.
        float dx = p.getX()-x0, dy = p.getY()-y0;
        float distance = (float) Math.sqrt(dx*dx+dy*dy);
        int steps = Math.max(1, (int) Math.ceil(distance / Math.max(2f, p.getRadius()*.5f)));
        for (int step = 0; step <= steps; step++) {
            float t = (float)step/steps;
            if (fence.collidesCircle(x0 + dx*t, y0 + dy*t, p.getRadius())) {
                p.expire();
                break;
            }
        }
        assertFalse(p.isAlive());
    }
}
