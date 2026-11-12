package com.deadbeat.entities;

import com.badlogic.gdx.math.Rectangle;
import com.deadbeat.world.CityMap;
import com.deadbeat.world.CityMap.Obstacle;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlayerWorldCollisionTest {
    private static final float EPS=0.001f;
    private Player player(float x,float y) {
        return new Player(x,y,new Rectangle(0f,0f,CityMap.WORLD_SIZE,CityMap.WORLD_SIZE));
    }
    @Test public void playerCannotPassThroughBuilding() {
        CityMap city=new CityMap();
        Obstacle building=city.getBlock(1,1).obstacle(0);
        float x=building.x-Player.DEFAULT_RADIUS-5f;
        float y=building.y+building.height*.5f;
        Player player=player(x,y);
        player.update(.05f,1f,0f,city);
        assertEquals(x,player.getX(),EPS);
    }
    @Test public void playerSlidesAlongWalls() {
        CityMap city=new CityMap();
        Obstacle building=city.getBlock(1,1).obstacle(0);
        float x=building.x-Player.DEFAULT_RADIUS-1f;
        float y=building.y+building.height*.5f;
        Player player=player(x,y);
        player.update(.05f,1f,1f,city);
        assertEquals(x,player.getX(),EPS);
        assertTrue(player.getY()>y);
    }
    @Test public void playerCanMoveOnCentralConnectedStreet() {
        CityMap city=new CityMap();
        float x=CityMap.SPAWN,y=CityMap.SPAWN;
        Player player=player(x,y);
        for(int k=0;k<20;k++)player.update(.05f,0f,1f,city);
        assertTrue(player.getY()>y+170f);
    }
}
