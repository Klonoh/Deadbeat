package com.deadbeat.world;

import com.deadbeat.world.CityMap.CityBlock;
import com.deadbeat.world.CityMap.Decoration;
import com.deadbeat.world.CityMap.DecorKind;
import com.deadbeat.world.CityMap.Kind;
import com.deadbeat.world.CityMap.Obstacle;
import org.junit.Test;
import com.deadbeat.entities.Player;
import com.badlogic.gdx.math.Rectangle;
import static org.junit.Assert.*;

/** Regression checks for physical fences and deterministic garbage interactions. */
public class CityMapInteractionTest {
    private static final float RADIUS=14f;

    private static final class TrashLocation {
        final int bx,by,index;
        final Decoration bag;
        TrashLocation(int bx,int by,int index,Decoration bag) {
            this.bx=bx;this.by=by;this.index=index;this.bag=bag;
        }
    }

    private TrashLocation findBag(CityMap map) {
        for(int bx=-4;bx<=4;bx++)for(int by=-4;by<=4;by++){
            CityBlock b=map.getBlock(bx,by);
            for(int k=0;k<b.decorationCount();k++) {
                Decoration d=b.decoration(k);
                if(d.kind==DecorKind.TRASH)return new TrashLocation(bx,by,k,d);
            }
        }
        throw new AssertionError("No garbage bags generated");
    }

    @Test public void fenceObstaclesExistAndBlockTheCharacter() {
        CityMap map=new CityMap();
        int seen=0;
        for(int bx=-5;bx<=5;bx++)for(int by=-5;by<=5;by++) {
            CityBlock b=map.getBlock(bx,by);
            for(int k=0;k<b.obstacleCount();k++) {
                Obstacle o=b.obstacle(k);
                if(o.kind==Kind.FENCE) {
                    seen++;
                    assertTrue(map.collidesCircle(o.x+o.width*.5f,o.y+o.height*.5f,RADIUS));
                }
            }
        }
        assertTrue(seen>0);
    }

    @Test public void playerStopsAtSolidFence() {
        CityMap map=new CityMap();
        boolean checked=false;
        for(int bx=-3;bx<=3 && !checked;bx++)for(int by=-3;by<=3 && !checked;by++) {
            CityBlock block=map.getBlock(bx,by);
            for(int k=0;k<block.obstacleCount();k++) {
                Obstacle o=block.obstacle(k);
                if(o.kind!=Kind.FENCE)continue;
                float x=o.x-Player.DEFAULT_RADIUS-2f;
                float y=o.y+o.height*.5f;
                if(map.collidesCircle(x,y,Player.DEFAULT_RADIUS))continue;
                Player p=new Player(x,y,new Rectangle(0f,0f,
                        CityMap.WORLD_SIZE,CityMap.WORLD_SIZE));
                p.update(.05f,1f,0f,map);
                assertEquals(x,p.getX(),0.001f);
                checked=true;
                break;
            }
        }
        assertTrue(checked);
    }

    @Test public void trashBreaksWhenPlayerOverlapsBag() {
        CityMap map=new CityMap();
        TrashLocation t=findBag(map);
        assertFalse(map.isGarbageBroken(t.bx,t.by,t.index));
        assertTrue(map.breakGarbageNear(t.bag.x+t.bag.width*.5f,
                t.bag.y+t.bag.height*.5f,RADIUS)>0);
        assertTrue(map.isGarbageBroken(t.bx,t.by,t.index));
        assertTrue(map.brokenGarbageCount()>0);
    }

    @Test public void breakingSameBagTwiceDoesNotIncrement() {
        CityMap map=new CityMap();
        TrashLocation t=findBag(map);
        float x=t.bag.x+t.bag.width*.5f;
        float y=t.bag.y+t.bag.height*.5f;
        int first=map.breakGarbageNear(x,y,RADIUS);
        int amount=map.brokenGarbageCount();
        assertTrue(first>0);
        assertEquals(0,map.breakGarbageNear(x,y,RADIUS));
        assertEquals(amount,map.brokenGarbageCount());
    }

    @Test public void garbageDoesNotBreakWhenPlayerIsFarAway() {
        CityMap map=new CityMap();
        TrashLocation t=findBag(map);
        // Choose central road intersection far from this decoration.
        assertEquals(0,map.breakGarbageNear(CityMap.SPAWN,CityMap.SPAWN,RADIUS));
        assertFalse(map.isGarbageBroken(t.bx,t.by,t.index));
    }

    @Test public void trashStateSurvivesBlockCacheEviction() {
        CityMap map=new CityMap();
        TrashLocation t=findBag(map);
        map.breakGarbageNear(t.bag.x+t.bag.width*.5f,
                t.bag.y+t.bag.height*.5f,RADIUS);
        for(int i=0;i<135;i++)map.getBlock(i,80);
        assertTrue(map.cachedBlockCount()<=96);
        assertTrue(map.isGarbageBroken(t.bx,t.by,t.index));
        assertEquals(0,map.breakGarbageNear(t.bag.x+t.bag.width*.5f,
                t.bag.y+t.bag.height*.5f,RADIUS));
    }

    @Test public void resetRestoresAllIntactGarbageBags() {
        CityMap map=new CityMap();
        TrashLocation t=findBag(map);
        map.breakGarbageNear(t.bag.x+t.bag.width*.5f,
                t.bag.y+t.bag.height*.5f,RADIUS);
        map.resetGarbage();
        assertEquals(0,map.brokenGarbageCount());
        assertFalse(map.isGarbageBroken(t.bx,t.by,t.index));
        assertTrue(map.breakGarbageNear(t.bag.x+t.bag.width*.5f,
                t.bag.y+t.bag.height*.5f,RADIUS)>0);
    }

    @Test public void allThreeBrokenStylesAreDeterministicallyReachable() {
        CityMap map=new CityMap();
        boolean[] seen=new boolean[3];
        for(int x=-6;x<=6;x++)for(int y=-6;y<=6;y++) {
            CityBlock b=map.getBlock(x,y);
            for(int n=0;n<b.decorationCount();n++) {
                if(b.decoration(n).kind!=DecorKind.TRASH)continue;
                int v=map.brokenGarbageVariant(x,y,n);
                assertTrue(v>=0 && v<3);
                seen[v]=true;
                assertEquals(v,map.brokenGarbageVariant(x,y,n));
            }
        }
        assertTrue(seen[0] && seen[1] && seen[2]);
    }

    @Test public void invalidBreakCoordinatesDoNotChangeState() {
        CityMap map=new CityMap();
        assertEquals(0,map.breakGarbageNear(Float.NaN,0f,14f));
        assertEquals(0,map.breakGarbageNear(0f,Float.POSITIVE_INFINITY,14f));
        assertEquals(0,map.breakGarbageNear(0f,0f,-1f));
        assertEquals(0,map.brokenGarbageCount());
    }
}
