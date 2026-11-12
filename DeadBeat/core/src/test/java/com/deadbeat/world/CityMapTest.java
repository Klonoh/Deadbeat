package com.deadbeat.world;

import com.deadbeat.world.CityMap.CityBlock;
import com.deadbeat.world.CityMap.Obstacle;
import org.junit.Test;
import static org.junit.Assert.*;

public class CityMapTest {
    private static final float EPS = 0.001f;

    @Test public void spawnAndIntersectionStayOpen() {
        CityMap city=new CityMap();
        assertFalse(city.collidesCircle(CityMap.SPAWN+64f,CityMap.SPAWN+64f,14f));
        assertTrue(city.isRoad(CityMap.SPAWN,CityMap.SPAWN));
        assertFalse(city.collidesCircle(CityMap.SPAWN,CityMap.SPAWN,14f));
    }
    @Test public void streetsVaryInWidthAndPosition() {
        CityMap city=new CityMap();
        assertNotEquals(city.avenueHalf(0),city.avenueHalf(1),EPS);
        assertTrue(city.avenueCenter(2)-city.avenueCenter(1) > 650f);
        assertTrue(city.streetCenter(1)-city.streetCenter(0) > 560f);
        assertTrue(city.isRoad(city.avenueCenter(1),city.streetCenter(1)+250f));
        assertTrue(city.isRoad(city.avenueCenter(1)+260f,city.streetCenter(1)));
    }
    @Test public void buildingsAndPropsAreSolid() {
        CityMap city=new CityMap();
        for(int bx=-2;bx<=2;bx++)for(int by=-2;by<=2;by++){
            CityBlock block=city.getBlock(bx,by);
            for(int i=0;i<block.obstacleCount();i++) {
                Obstacle o=block.obstacle(i);
                assertTrue(city.collidesCircle(o.x+o.width/2f,o.y+o.height/2f,14f));
            }
        }
    }
    @Test public void circleVersusBuildingIsAccurate() {
        CityMap city=new CityMap();
        Obstacle first=city.getBlock(1,1).obstacle(0);
        float cy=first.y+first.height/2f;
        assertFalse(city.collidesCircle(first.x-14.1f,cy,14f));
        assertTrue(city.collidesCircle(first.x-13.9f,cy,14f));
    }
    @Test public void cachedSectorsAreDeterministic() {
        CityMap city=new CityMap();
        CityBlock initial=city.getBlock(0,0);
        Obstacle first=initial.obstacle(0);
        for(int i=0;i<145;i++)city.getBlock(i,17);
        assertTrue(city.cachedBlockCount()<=96);
        CityBlock after=city.getBlock(0,0);
        assertNotSame(initial,after);
        assertEquals(first.x,after.obstacle(0).x,EPS);
        assertEquals(first.y,after.obstacle(0).y,EPS);
        assertEquals(first.width,after.obstacle(0).width,EPS);
    }
    @Test public void terrainDistrictsProvideVariation() {
        CityMap city=new CityMap();
        boolean[] seen=new boolean[CityMap.District.values().length];
        for(int i=-4;i<=4;i++)for(int j=-4;j<=4;j++)seen[city.getBlock(i,j).district.ordinal()]=true;
        int found=0;for(boolean b:seen)if(b)found++;
        assertEquals(CityMap.District.values().length,found);
    }
    @Test public void roadGeometryDoesNotDependOnGroundTileBoundaries() {
        CityMap city=new CityMap();
        float x=city.avenueCenter(0);
        // Travel along a road for several grid cells: the entire corridor
        // remains ROAD irrespective of the 64px ground-tile boundaries.
        for(int n=0;n<150;n++) {
            float y=CityMap.SPAWN-2500f+n*37f;
            assertEquals(CityMap.Surface.ROAD,city.surfaceAt(x,y));
        }
    }

    @Test public void curbTransitionsUseRealStreetWidths() {
        CityMap city=new CityMap();
        float x=city.avenueCenter(0);
        float y=city.streetCenter(0)+220f;
        assertEquals(CityMap.Surface.ROAD,city.surfaceAt(x+city.avenueHalf(0)-1f,y));
        assertEquals(CityMap.Surface.SIDEWALK,city.surfaceAt(x+city.avenueHalf(0)+1f,y));
        assertEquals(CityMap.Surface.SIDEWALK,city.surfaceAt(x+city.avenueHalf(0)+34f,y));
    }

    @Test public void worldBordersAreSolid() {
        CityMap city=new CityMap();
        assertTrue(city.collidesCircle(1,1,14));
        assertTrue(city.collidesCircle(CityMap.WORLD_SIZE-1,CityMap.WORLD_SIZE-1,14));
    }
}
