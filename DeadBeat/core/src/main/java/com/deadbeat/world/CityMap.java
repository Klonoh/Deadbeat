package com.deadbeat.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic, lazily generated city layout. Streets form a connected network,
 * while the plots between them vary in shape, purpose and building dimensions.
 * No LibGDX or rendering dependencies: all colliders live in world coordinates.
 */
public final class CityMap implements CollisionMap {
    public static final float WORLD_SIZE = 131072f;
    public static final float SPAWN = WORLD_SIZE / 2f;
    public static final int TILE_SIZE = 64;
    public static final int BLOCK_SIZE = 768; // nominal size, road spacing is intentionally non-uniform
    public static final int STREET_X = 816;
    public static final int STREET_Y = 688;
    private static final int CACHE_LIMIT = 96;
    private static final int SEED = 0x51A7D3B9;

    public enum Kind { BUILDING, CAR, DUMPSTER, BARRICADE, PLANTER, FENCE, TENT, CRATE }
    public enum DecorKind { TRASH, PAPER, GRAFFITI, BENCH, LAMP, WEEDS, PUDDLE, MANHOLE, POWER, BARREL, VENDING, TIRES, PALLET, BIN, FENCE }
    public enum District { SHOPS, RESIDENTIAL, INDUSTRIAL, PARKING, CAMP, BACKSTREET }
    public enum Surface { ROAD, SIDEWALK, BRICK, ALLEY, LOT, DIRT }

    public static final class Obstacle {
        public final float x,y,width,height;
        public final Kind kind;
        public final int style;
        public Obstacle(float x,float y,float width,float height,Kind kind,int style) {
            this.x=x;this.y=y;this.width=width;this.height=height;
            this.kind=kind;this.style=style;
        }
    }

    public static final class Decoration {
        public final float x,y,width,height;
        public final DecorKind kind;
        public final int style;
        public Decoration(float x,float y,float width,float height,DecorKind kind,int style) {
            this.x=x;this.y=y;this.width=width;this.height=height;
            this.kind=kind;this.style=style;
        }
    }

    public static final class CityBlock {
        public final int blockX, blockY;
        public final District district;
        public final float left,bottom,right,top;
        private final Obstacle[] obstacles;
        private final Decoration[] decorations;
        CityBlock(int x,int y,District district,float left,float bottom,float right,float top,
                  Obstacle[] obstacles,Decoration[] decorations) {
            this.blockX=x;this.blockY=y;this.district=district;
            this.left=left;this.bottom=bottom;this.right=right;this.top=top;
            this.obstacles=obstacles;this.decorations=decorations;
        }
        public int obstacleCount(){return obstacles.length;}
        public Obstacle obstacle(int i){return obstacles[i];}
        public int decorationCount(){return decorations.length;}
        public Decoration decoration(int i){return decorations[i];}
    }

    /** Bag interactions persist even when the LRU map cache evicts a block. */
    private final Set<Long> brokenGarbageIds = new HashSet<Long>();

    private final LinkedHashMap<Long,CityBlock> cache =
        new LinkedHashMap<Long,CityBlock>(128,0.75f,true) {
            @Override protected boolean removeEldestEntry(Map.Entry<Long,CityBlock> eldest) {
                return size() > CACHE_LIMIT;
            }
        };

    public static int variation(int x,int y,int salt) {
        int h=SEED^(x*0x9E3779B9)^Integer.rotateLeft(y*0x85EBCA6B,11)^salt;
        h^=h>>>16;h*=0x7feb352d;h^=h>>>15;h*=0x846ca68b;
        return h^(h>>>16);
    }
    private static int positive(int hash,int modulo){return Math.floorMod(hash,modulo);}

    public float avenueCenter(int i) {
        return SPAWN + i*(float)STREET_X + (i==0?0:positive(variation(i,42,16),97)-48);
    }
    public float streetCenter(int j) {
        return SPAWN + j*(float)STREET_Y + (j==0?0:positive(variation(24,j,29),77)-38);
    }
    public float avenueHalf(int i){return 66f+positive(variation(i,17,51),29);}
    public float streetHalf(int j){return 51f+positive(variation(61,j,58),26);}

    /** Cell whose western and southern main roads meet at index (i,j). */
    public int avenueIndex(float x) {
        int i=(int)Math.floor((x-SPAWN)/STREET_X);
        while (x < avenueCenter(i)) i--;
        while (x >= avenueCenter(i+1)) i++;
        return i;
    }
    public int streetIndex(float y) {
        int j=(int)Math.floor((y-SPAWN)/STREET_Y);
        while (y < streetCenter(j)) j--;
        while (y >= streetCenter(j+1)) j++;
        return j;
    }

    private float nearestAvenue(float x) {
        int i=avenueIndex(x);
        return x-avenueCenter(i) < avenueCenter(i+1)-x ? avenueCenter(i) : avenueCenter(i+1);
    }
    private float nearestStreet(float y) {
        int j=streetIndex(y);
        return y-streetCenter(j) < streetCenter(j+1)-y ? streetCenter(j) : streetCenter(j+1);
    }

    public boolean isRoad(float x,float y) {
        int i=avenueIndex(x),j=streetIndex(y);
        return Math.abs(x-avenueCenter(i)) <= avenueHalf(i)
            || Math.abs(x-avenueCenter(i+1)) <= avenueHalf(i+1)
            || Math.abs(y-streetCenter(j)) <= streetHalf(j)
            || Math.abs(y-streetCenter(j+1)) <= streetHalf(j+1);
    }

    /** Ground surfaces: roads, sidewalks, alleys and distinct district lots. */
    public Surface surfaceAt(float x,float y) {
        int i=avenueIndex(x),j=streetIndex(y);
        float dx=Math.min(Math.abs(x-avenueCenter(i))-avenueHalf(i),
                          Math.abs(x-avenueCenter(i+1))-avenueHalf(i+1));
        float dy=Math.min(Math.abs(y-streetCenter(j))-streetHalf(j),
                          Math.abs(y-streetCenter(j+1))-streetHalf(j+1));
        if(dx<=0 || dy<=0)return Surface.ROAD;
        if(dx<35 || dy<35)return Surface.SIDEWALK;
        District d=getBlock(i,j).district;
        switch(d) {
            case SHOPS: return Surface.BRICK;
            case INDUSTRIAL: case BACKSTREET: return Surface.ALLEY;
            case PARKING: return Surface.LOT;
            case CAMP: return Surface.DIRT;
            default: return Surface.SIDEWALK;
        }
    }

    /** Neighboring road data for centerline/crosswalk rendering. */
    public boolean isAvenue(float x) {
        int i=avenueIndex(x);
        return Math.abs(x-avenueCenter(i)) < avenueHalf(i)
            || Math.abs(x-avenueCenter(i+1)) < avenueHalf(i+1);
    }
    public boolean isStreet(float y) {
        int j=streetIndex(y);
        return Math.abs(y-streetCenter(j)) < streetHalf(j)
            || Math.abs(y-streetCenter(j+1)) < streetHalf(j+1);
    }
    public float nearestAvenueCenter(float x) {return nearestAvenue(x);}
    public float nearestStreetCenter(float y) {return nearestStreet(y);}

    public CityBlock getBlock(int i,int j) {
        long key=((long)i<<32)^(j&0xffffffffL);
        CityBlock result=cache.get(key);
        if(result==null) {result=generateBlock(i,j);cache.put(key,result);}
        return result;
    }
    public int cachedBlockCount(){return cache.size();}
    public District districtAt(float x,float y) {
        return getBlock(avenueIndex(x),streetIndex(y)).district;
    }

    private CityBlock generateBlock(int bx,int by) {
        int h=variation(bx,by,1193);
        District district=District.values()[positive(h, District.values().length)];
        float west=avenueCenter(bx)+avenueHalf(bx)+40;
        float east=avenueCenter(bx+1)-avenueHalf(bx+1)-40;
        float south=streetCenter(by)+streetHalf(by)+36;
        float north=streetCenter(by+1)-streetHalf(by+1)-38;
        float w=east-west, ht=north-south;
        List<Obstacle> solids=new ArrayList<Obstacle>();
        List<Decoration> decals=new ArrayList<Decoration>();
        int s=positive(variation(bx,by,822),24);
        float split=w*(0.43f+positive(variation(bx,by,204),16)/100f);
        float gap=48+positive(variation(bx,by,102),30);

        switch(district) {
            case SHOPS:
                // Two unequal shops separated by a narrow service passage.
                addBuilding(solids,west+10,south+ht*0.49f,split-23,ht*0.45f,s%3);
                addBuilding(solids,west+split+gap,south+ht*0.55f,w-split-gap-14,ht*0.39f,3+s%3);
                add(solids,west+w*.55f,south+26,62,77,Kind.CAR,s%2);
                add(solids,west+split+7,south+ht*.46f,55,41,Kind.DUMPSTER,0);
                addDec(decals,west+split-6,south+ht*.50f,60,60,DecorKind.TRASH,s);
                addDec(decals,west+52,south+64,88,55,DecorKind.BENCH,0);
                addDec(decals,west+w-115,south+44,85,58,DecorKind.VENDING,0);
                break;
            case RESIDENTIAL:
                addBuilding(solids,west+20,south+ht*.38f,w*.39f,ht*.56f,6+s%3);
                addBuilding(solids,east-w*.41f-15,south+ht*.16f,w*.39f,ht*.49f,6+(s+1)%3);
                addDec(decals,west+w*.47f,south+45,85,55,DecorKind.WEEDS,s);
                addDec(decals,west+w*.53f,south+ht*.66f,95,60,DecorKind.PUDDLE,s);
                add(solids,west+w*.46f,south+ht*.29f,90,54,Kind.PLANTER,0);
                break;
            case INDUSTRIAL:
                addBuilding(solids,west+14,south+ht*.45f,w*.64f,ht*.49f,9+s%3);
                addBuilding(solids,west+w*.69f,south+ht*.54f,w*.28f,ht*.39f,9+(s+1)%3);
                add(solids,west+38,south+35,80,80,Kind.DUMPSTER,s%2);
                add(solids,west+135,south+45,62,60,Kind.CRATE,0);
                addDec(decals,west+w*.71f,south+30,113,54,DecorKind.BARREL,s);
                addDec(decals,west+220,south+70,84,64,DecorKind.PALLET,s);
                add(solids,west+w*.72f,south+15f,138,73,Kind.FENCE,s);
                break;
            case PARKING:
                // Spacious parking/abandoned garage plot instead of four uniform buildings.
                addBuilding(solids,east-w*.37f,south+ht*.50f,w*.33f,ht*.43f,3+s%3);
                add(solids,west+75,south+ht*.53f,70,120,Kind.CAR,s%2);
                add(solids,west+210,south+ht*.40f,132,67,Kind.CAR,2+s%2);
                add(solids,west+34,south+ht*.20f,92,69,Kind.BARRICADE,0);
                addDec(decals,west+w*.55f,south+52,100,72,DecorKind.TIRES,s);
                addDec(decals,west+50,south+50,90,54,DecorKind.WEEDS,s);
                add(solids,west+w*.63f,south+ht*.13f,125,66,Kind.FENCE,s);
                break;
            case CAMP:
                addBuilding(solids,west+12,north-ht*.36f,w*.35f,ht*.31f,6+s%3);
                add(solids,west+w*.63f,south+ht*.42f,160,100,Kind.TENT,0);
                add(solids,west+w*.48f,south+ht*.08f,65,65,Kind.CRATE,0);
                addDec(decals,west+50,south+35,132,70,DecorKind.TRASH,s);
                // Fence is a physical obstacle, not a decorative decal.
                add(solids,west+w*.54f,south+ht*.66f,144,76,Kind.FENCE,s);
                addDec(decals,west+w*.22f,south+ht*.45f,97,70,DecorKind.PUDDLE,s);
                break;
            case BACKSTREET:
                // Two deep, tall buildings face an irregular litter-strewn alley.
                addBuilding(solids,west+10,south+ht*.26f,w*.38f,ht*.67f,3+s%3);
                addBuilding(solids,east-w*.36f-10,south+ht*.35f,w*.34f,ht*.59f,6+s%3);
                add(solids,west+w*.43f,south+ht*.76f,80,65,Kind.DUMPSTER,s%2);
                add(solids,west+w*.51f,south+ht*.25f,62,53,Kind.CRATE,0);
                addDec(decals,west+w*.44f,south+ht*.48f,133,75,DecorKind.TRASH,s);
                addDec(decals,west+w*.48f,south+ht*.17f,90,74,DecorKind.PUDDLE,s);
                addDec(decals,west+w*.43f,south+ht*.63f,98,47,DecorKind.GRAFFITI,s);
                break;
            default: break;
        }

        // Reproducible small-scale environmental clutter: never blocks movement.
        // Place it in the plot instead of uniformly filling every road tile.
        for (int n=0;n<7;n++) {
            int q=variation(bx*19+n,by*11-n,4125);
            float dx=24f+positive(q,w>120?(int)(w-95f):1);
            float dy=20f+positive(q>>>7,ht>110?(int)(ht-88f):1);
            DecorKind kind=(n%4==0)?DecorKind.PAPER:
                           (n%4==1)?DecorKind.WEEDS:
                           (n%4==2)?DecorKind.PUDDLE:DecorKind.TRASH;
            float dw=(kind==DecorKind.TRASH)?48f:58f;
            float dh=(kind==DecorKind.PUDDLE)?39f:43f;
            addDec(decals,west+dx,south+dy,dw,dh,kind,q);
        }
        // Street furniture is kept close to sidewalks; main road lanes stay navigable.
        addDec(decals,west+8,south-24,32,106,DecorKind.LAMP,s);
        addDec(decals,east-45,north+4,32,106,DecorKind.LAMP,s+2);
        addDec(decals,west+w*.25f,south+12,52,50,DecorKind.BIN,s);
        if ((h&3)!=0) addDec(decals,west+w*.66f,south+ht*.22f,76,45,DecorKind.PAPER,s);
        if ((h&7)==3) addDec(decals,west+70,north-58,93,63,DecorKind.POWER,s);
        // A parked, partially damaged car may appear beside a vertical road, not inside intersections.
        if ((h&1)==0) {
            float rx=avenueCenter(bx)+avenueHalf(bx)-51f;
            float ry=streetCenter(by)+streetHalf(by)+195f;
            add(solids,rx,ry,44,77,Kind.CAR,s%2);
        }
        if ((h&7)==6) {
            float rx=avenueCenter(bx)+avenueHalf(bx)+160f;
            float ry=streetCenter(by)+streetHalf(by)-26f;
            add(solids,rx,ry,96,46,Kind.CAR,2+s%2);
        }
        resolveLayout(solids, decals, west, south, east, north);
        return new CityBlock(bx,by,district,west,south,east,north,
            solids.toArray(new Obstacle[0]), decals.toArray(new Decoration[0]));
    }

    private void resolveLayout(List<Obstacle> solids,List<Decoration> decals,
                               float west,float south,float east,float north) {
        resolveObstacleOverlaps(solids, west, south, east, north);
        List<Decoration> cleaned = new ArrayList<Decoration>(decals.size());
        for (Decoration d : decals) {
            Decoration placed = relocateDecoration(d, cleaned, solids, west, south, east, north);
            if (placed != null) cleaned.add(placed);
        }
        decals.clear();
        decals.addAll(cleaned);
    }

    private void resolveObstacleOverlaps(List<Obstacle> solids,float west,float south,float east,float north) {
        List<Obstacle> kept = new ArrayList<Obstacle>(solids.size());
        for (Obstacle o : solids) {
            if (o.kind == Kind.BUILDING) { kept.add(o); continue; }
            if (!overlapsAny(o, kept, obstaclePadding(o.kind))) { kept.add(o); continue; }
            Obstacle moved = relocateObstacle(o, kept, west, south, east, north);
            if (moved != null) kept.add(moved);
        }
        solids.clear();
        solids.addAll(kept);
    }

    private Obstacle relocateObstacle(Obstacle o,List<Obstacle> solids,float west,float south,float east,float north) {
        float[] tries = new float[]{0f, 18f, -18f, 36f, -36f, 54f, -54f, 72f, -72f};
        float pad = obstaclePadding(o.kind);
        for (float dy : tries) for (float dx : tries) {
            float nx = clamp(o.x + dx, west, east - o.width);
            float ny = clamp(o.y + dy, south, north - o.height);
            Obstacle candidate = new Obstacle(nx, ny, o.width, o.height, o.kind, o.style);
            if (!overlapsAny(candidate, solids, pad)) return candidate;
        }
        return null;
    }

    private Decoration relocateDecoration(Decoration d,List<Decoration> placed,List<Obstacle> solids,
                                          float west,float south,float east,float north) {
        float[] tries = new float[]{0f, 14f, -14f, 28f, -28f, 42f, -42f, 56f, -56f};
        float pad = decorationPadding(d.kind);
        boolean avoidDecor = avoidDecorationOverlap(d.kind);
        for (float dy : tries) for (float dx : tries) {
            float nx = clamp(d.x + dx, west, east - d.width);
            float ny = clamp(d.y + dy, south, north - d.height);
            Decoration candidate = new Decoration(nx, ny, d.width, d.height, d.kind, d.style);
            if (overlapsAny(candidate, solids, pad)) continue;
            if (avoidDecor && overlapsAnyDecor(candidate, placed, pad)) continue;
            return candidate;
        }
        return null;
    }

    private static boolean avoidDecorationOverlap(DecorKind kind) {
        switch (kind) {
            case PAPER:
            case WEEDS:
            case PUDDLE:
            case GRAFFITI:
                return false;
            default:
                return true;
        }
    }

    private static float obstaclePadding(Kind kind) {
        switch (kind) {
            case FENCE: return 8f;
            case CAR: return 6f;
            case DUMPSTER:
            case TENT:
            case CRATE:
            case BARRICADE:
            case PLANTER:
                return 5f;
            default: return 0f;
        }
    }

    private static float decorationPadding(DecorKind kind) {
        switch (kind) {
            case LAMP: return 8f;
            case BENCH:
            case VENDING:
            case POWER:
            case BARREL:
            case TIRES:
            case PALLET:
            case BIN:
            case FENCE:
                return 5f;
            case TRASH: return 4f;
            default: return 1f;
        }
    }

    private static boolean overlapsAny(Obstacle o,List<Obstacle> others,float padding) {
        for (Obstacle other : others) {
            if (intersects(o.x, o.y, o.width, o.height, other.x, other.y, other.width, other.height, padding)) return true;
        }
        return false;
    }

    private static boolean overlapsAny(Decoration d,List<Obstacle> others,float padding) {
        for (Obstacle other : others) {
            if (intersects(d.x, d.y, d.width, d.height, other.x, other.y, other.width, other.height, padding)) return true;
        }
        return false;
    }

    private static boolean overlapsAnyDecor(Decoration d,List<Decoration> others,float padding) {
        for (Decoration other : others) {
            if (intersects(d.x, d.y, d.width, d.height, other.x, other.y, other.width, other.height, padding)) return true;
        }
        return false;
    }

    private static boolean intersects(float ax,float ay,float aw,float ah,
                                      float bx,float by,float bw,float bh,float padding) {
        return ax + aw > bx + padding && ax + padding < bx + bw
            && ay + ah > by + padding && ay + padding < by + bh;
    }

    private static float clamp(float value,float min,float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void addBuilding(List<Obstacle> solids,float x,float y,float w,float h,int style) {
        if(w>=70 && h>=70) add(solids,x,y,w,h,Kind.BUILDING,style);
    }
    private static void add(List<Obstacle> solids,float x,float y,float w,float h,Kind kind,int style) {
        solids.add(new Obstacle(x,y,w,h,kind,style));
    }
    private static void addDec(List<Decoration> list,float x,float y,float w,float h,DecorKind kind,int style) {
        list.add(new Decoration(x,y,w,h,kind,style));
    }

    /** Stable bag ID: 24 bits per block axis (ample for this world), 16 for decoration index. */
    private static long garbageId(int bx,int by,int decorationIndex) {
        return ((bx & 0xffffffL) << 40) | ((by & 0xffffffL) << 16)
                | (decorationIndex & 0xffffL);
    }

    /** Called once per frame after the player has moved; bags never block movement. */
    public int breakGarbageNear(float x,float y,float playerRadius) {
        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(playerRadius)
                || playerRadius < 0f) return 0;
        int x0=avenueIndex(x-playerRadius)-1,x1=avenueIndex(x+playerRadius)+1;
        int y0=streetIndex(y-playerRadius)-1,y1=streetIndex(y+playerRadius)+1;
        int broken=0;
        for(int bx=x0;bx<=x1;bx++)for(int by=y0;by<=y1;by++) {
            CityBlock block=getBlock(bx,by);
            for (int n=0;n<block.decorationCount();n++) {
                Decoration d=block.decoration(n);
                if (d.kind!=DecorKind.TRASH) continue;
                if (circleIntersectsRect(x,y,playerRadius,d.x,d.y,d.width,d.height)
                        && brokenGarbageIds.add(garbageId(bx,by,n))) broken++;
            }
        }
        return broken;
    }

    /** Stable, decorative-only variation: a bag breaks differently in each location. */
    public int brokenGarbageVariant(int bx,int by,int decorationIndex) {
        return positive(variation(bx*37+decorationIndex,by,0xA023),3);
    }

    public boolean isGarbageBroken(int bx,int by,int decorationIndex) {
        return brokenGarbageIds.contains(garbageId(bx,by,decorationIndex));
    }

    public int brokenGarbageCount() { return brokenGarbageIds.size(); }
    public void resetGarbage() { brokenGarbageIds.clear(); }

    private static boolean circleIntersectsRect(float x,float y,float radius,
            float left,float bottom,float width,float height) {
        float cx=Math.max(left,Math.min(x,left+width));
        float cy=Math.max(bottom,Math.min(y,bottom+height));
        float dx=x-cx,dy=y-cy;
        return dx*dx+dy*dy <= radius*radius;
    }

    @Override public boolean collidesCircle(float x,float y,float radius) {
        if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(radius)||radius<0f)return true;
        if(x-radius<0||y-radius<0||x+radius>WORLD_SIZE||y+radius>WORLD_SIZE)return true;
        // Query cells touched by the circle, including parked cars at road edges.
        int x0=avenueIndex(x-radius)-1,x1=avenueIndex(x+radius)+1;
        int y0=streetIndex(y-radius)-1,y1=streetIndex(y+radius)+1;
        for(int i=x0;i<=x1;i++) for(int j=y0;j<=y1;j++) {
            CityBlock block=getBlock(i,j);
            for(int k=0;k<block.obstacleCount();k++) {
                Obstacle o=block.obstacle(k);
                if(circleIntersectsRect(x,y,radius,o.x,o.y,o.width,o.height))return true;
            }
            // Legacy and future decorative fence placements must never be walk-through.
            for(int k=0;k<block.decorationCount();k++) {
                Decoration d=block.decoration(k);
                if(d.kind==DecorKind.FENCE
                        && circleIntersectsRect(x,y,radius,d.x,d.y,d.width,d.height))return true;
            }
        }
        return false;
    }
}
