package com.deadbeat.world;

import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.deadbeat.world.CityMap.CityBlock;
import com.deadbeat.world.CityMap.Obstacle;
import com.deadbeat.world.CityMap.Decoration;
import com.deadbeat.world.CityMap.Kind;
import com.deadbeat.world.CityMap.DecorKind;
import com.deadbeat.world.CityMap.Surface;

/**
 * PNG-based urban scenery: camera culling, varied road surfaces, and layered
 * props/facades. Collider geometry is owned ONLY by CityMap.
 * Textures are loaded once and disposed as a group; no loading in draw().
 */
public final class CityRenderer implements Disposable {
    private static final int TILE = CityMap.TILE_SIZE;
    private final CityMap map;
    private static final String[] SURFACE_NAMES = {
        "asphalt", "sidewalk", "brick", "alley", "lot", "dirt", "roof"
    };
    private static final int ASPHALT = 0;
    private static final int SIDEWALK = 1;
    private static final int BRICK = 2;
    private static final int ALLEY = 3;
    private static final int LOT = 4;
    private static final int DIRT = 5;
    private static final int ROOF = 6;
    private final Texture[] surfaceTextures = new Texture[SURFACE_NAMES.length];
    private final TextureRegion[] surfaceRegions = new TextureRegion[SURFACE_NAMES.length];
    private final Map<String,Texture> sprites = new HashMap<String,Texture>();
    private final Texture pixel;

    private static final String[] FILES={
       "car_vertical","car_blue","car_horizontal","car_horizontal_blue",
       "planter","planter_long","bench","bin","lamppost","powerbox",
       "dumpster","dumpster_blue","vending","barrels","crate","pallet",
       "tires","tent","garbage","fence","barrier","concrete_barricade",
       "roof_unit","paper","graffiti_wall","weeds","puddle",
       "garbage_broken_1","garbage_broken_2","garbage_broken_3",
       "house_front","brick_front",
       "plaster_front","shop_front","punk_shop","shutter_front",
       "garage_front","warehouse_front"
    };

    public CityRenderer(CityMap map,Texture pixel) {
        this.map=map;this.pixel=pixel;
        // Each floor material is its own 64x64 seamless PNG. Its samples are
        // attached to an integer world grid, never to the moving viewport.
        // Road paint is drawn separately from the ground material.
        for (int i=0; i<SURFACE_NAMES.length; i++) {
            Texture t = new Texture(Gdx.files.internal(
                    "world2/surfaces/" + SURFACE_NAMES[i] + ".png"));
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            if (t.getWidth()!=TILE || t.getHeight()!=TILE) {
                t.dispose();
                throw new IllegalStateException("Each surface tile must be 64x64: "
                        + SURFACE_NAMES[i]);
            }
            surfaceTextures[i]=t;
            surfaceRegions[i]=new TextureRegion(t);
        }
        for(String name:FILES) {
            Texture texture=new Texture(Gdx.files.internal("world2/"+name+".png"));
            texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            sprites.put(name,texture);
        }
    }

    public void draw(SpriteBatch batch,float left,float bottom,float w,float h) {
        drawGround(batch,left,bottom,w,h);
        int i0=map.avenueIndex(left)-1;
        int i1=map.avenueIndex(left+w)+1;
        int j0=map.streetIndex(bottom)-1;
        int j1=map.streetIndex(bottom+h)+1;
        // Decorative ground-level objects first, then collidable objects and buildings.
        for(int j=j0;j<=j1;j++)for(int i=i0;i<=i1;i++) {
            CityBlock block=map.getBlock(i,j);
            for(int n=0;n<block.decorationCount();n++) {
                Decoration d=block.decoration(n);
                if(visible(d.x,d.y,d.width,d.height,left,bottom,w,h))
                    drawDecoration(batch,d,
                            map.isGarbageBroken(block.blockX,block.blockY,n),
                            map.brokenGarbageVariant(block.blockX,block.blockY,n));
            }
        }
        for(int j=j0;j<=j1;j++)for(int i=i0;i<=i1;i++) {
            CityBlock block=map.getBlock(i,j);
            for(int n=0;n<block.obstacleCount();n++) {
                Obstacle o=block.obstacle(n);
                if(visible(o.x,o.y,o.width,o.height,left-30,bottom-125,w+60,h+250))
                    drawObstacle(batch,o);
            }
        }
        batch.setColor(1f,1f,1f,1f);
    }

    /**
     * Continuous geometry first, world-aligned seamless PNGs second.
     * There is no tile-by-tile classification of the road surface. Otherwise a
     * 64px tile at the edge of a curved/wide street would break its outline.
     */
    private void drawGround(SpriteBatch batch,float left,float bottom,float w,float h) {
        // Draw the road asphalt as a uniform underlying layer.
        drawMaterial(batch, ASPHALT, left, bottom, w, h);
        int i0=map.avenueIndex(left)-1, i1=map.avenueIndex(left+w)+1;
        int j0=map.streetIndex(bottom)-1, j1=map.streetIndex(bottom+h)+1;
        for(int j=j0;j<=j1;j++) for(int i=i0;i<=i1;i++) {
            float x0=map.avenueCenter(i)+map.avenueHalf(i);
            float x1=map.avenueCenter(i+1)-map.avenueHalf(i+1);
            float y0=map.streetCenter(j)+map.streetHalf(j);
            float y1=map.streetCenter(j+1)-map.streetHalf(j+1);
            if(x1<=x0 || y1<=y0 || !visible(x0,y0,x1-x0,y1-y0,left,bottom,w,h))continue;
            // Sidewalk is exactly bounded by street edges, not by a rounded tile index.
            drawClippedMaterial(batch,SIDEWALK,x0,y0,x1-x0,y1-y0,left,bottom,w,h);
            int material;
            switch(map.getBlock(i,j).district) {
                case SHOPS: material=BRICK; break;
                case INDUSTRIAL: case BACKSTREET: material=ALLEY; break;
                case PARKING: material=LOT; break;
                case CAMP: material=DIRT; break;
                default: material=SIDEWALK;
            }
            // Keep an uninterrupted 35px pedestrian border around each plot.
            if(x1-x0>70f && y1-y0>70f)
                drawClippedMaterial(batch,material,x0+35f,y0+35f,
                        x1-x0-70f,y1-y0-70f,left,bottom,w,h);
        }
        drawRoadEdges(batch,left,bottom,w,h);
    }

    /**
     * Draws a ground material on a fixed, integer world-space grid.
     *
     * Important: NEVER stretch a repeating TextureRegion across the camera
     * viewport. Such UVs must be changed each frame when the camera moves,
     * causing the ground to appear to slide or swim underneath world objects.
     *
     * Instead, every 64x64 source texture is stamped at fixed world positions
     * (..., -64, 0, 64, ...). At a region edge we crop BOTH the quad and the
     * PNG using integer texel coordinates. Therefore the texel at world point
     * (wx, wy) depends only on wx/wy, not on the camera or viewport.
     */
    private void drawMaterial(SpriteBatch batch, int material,
                              float x, float y, float width, float height) {
        if (width <= 0f || height <= 0f) return;

        // Round consistently in world space. The camera is already pixel-snapped
        // by GameScreen; using the same integer world grid prevents seams.
        final int x0 = Math.round(x);
        final int y0 = Math.round(y);
        final int x1 = Math.round(x + width);
        final int y1 = Math.round(y + height);
        if (x1 <= x0 || y1 <= y0) return;

        final TextureRegion region = surfaceRegions[material];
        batch.setColor(1f, 1f, 1f, 1f);
        for (int tileY = worldTileOrigin(y0); tileY < y1; tileY += TILE) {
            final int bottom = Math.max(y0, tileY);
            final int top = Math.min(y1, tileY + TILE);
            final int drawHeight = top - bottom;
            if (drawHeight <= 0) continue;

            // TextureRegion's integer Y coordinate counts downward from the
            // image's top, but world-space Y increases upward.
            final int sourceTop = TILE - (bottom - tileY) - drawHeight;
            for (int tileX = worldTileOrigin(x0); tileX < x1; tileX += TILE) {
                final int left = Math.max(x0, tileX);
                final int right = Math.min(x1, tileX + TILE);
                final int drawWidth = right - left;
                if (drawWidth <= 0) continue;

                // setRegion(int...) produces local [0..1] UVs: no float UV
                // precision loss even at the far end of the 131,072px world.
                region.setRegion(left - tileX, sourceTop, drawWidth, drawHeight);
                batch.draw(region, left, bottom, drawWidth, drawHeight);
            }
        }
    }

    /** Floor division, not truncation: also works for negative coordinates. */
    static int worldTileOrigin(int worldPixel) {
        return Math.floorDiv(worldPixel, TILE) * TILE;
    }

    /** Visible camera bounds are only a performance clip, never the texture origin. */
    private void drawClippedMaterial(SpriteBatch batch, int material,
            float x, float y, float width, float height,
            float viewLeft, float viewBottom, float viewWidth, float viewHeight) {
        final float x0 = Math.max(x, viewLeft);
        final float x1 = Math.min(x + width, viewLeft + viewWidth);
        final float y0 = Math.max(y, viewBottom);
        final float y1 = Math.min(y + height, viewBottom + viewHeight);
        if (x1 > x0 && y1 > y0)
            drawMaterial(batch, material, x0, y0, x1 - x0, y1 - y0);
    }

    /** Road paint is tied to the REAL road centerlines, not to floor tiles. */
    private void drawRoadEdges(SpriteBatch batch,float left,float bottom,float w,float h) {
        int i0=map.avenueIndex(left)-1,i1=map.avenueIndex(left+w)+1;
        int j0=map.streetIndex(bottom)-1,j1=map.streetIndex(bottom+h)+1;
        for(int i=i0;i<=i1;i++) {
            float x=map.avenueCenter(i),r=map.avenueHalf(i);
            for(int j=j0;j<j1;j++) {
                float y0=Math.max(bottom,map.streetCenter(j)+map.streetHalf(j)+4f);
                float y1=Math.min(bottom+h,map.streetCenter(j+1)-map.streetHalf(j+1)-4f);
                if(y1<=y0)continue;
                rect(batch,x-r-3f,y0,3f,y1-y0,.52f,.49f,.45f);
                rect(batch,x+r,y0,3f,y1-y0,.52f,.49f,.45f);
                // The same 64-unit world grid is used across all camera frames.
                // Do not make dash visibility depend on the viewport edge.
                float streetBegin=map.streetCenter(j)+map.streetHalf(j)+4f;
                float streetEnd=map.streetCenter(j+1)-map.streetHalf(j+1)-4f;
                float first=(float)Math.ceil(Math.max(streetBegin,bottom-26f)/64f)*64f;
                for(float yy=first;yy<y1 && yy+26f<=streetEnd;yy+=64f)
                    rect(batch,x-2f,yy,4f,26f,.74f,.72f,.66f);
            }
        }
        for(int j=j0;j<=j1;j++) {
            float y=map.streetCenter(j),r=map.streetHalf(j);
            for(int i=i0;i<i1;i++) {
                float x0=Math.max(left,map.avenueCenter(i)+map.avenueHalf(i)+4f);
                float x1=Math.min(left+w,map.avenueCenter(i+1)-map.avenueHalf(i+1)-4f);
                if(x1<=x0)continue;
                rect(batch,x0,y-r-3f,x1-x0,3f,.52f,.49f,.45f);
                rect(batch,x0,y+r,x1-x0,3f,.52f,.49f,.45f);
                float streetBegin=map.avenueCenter(i)+map.avenueHalf(i)+4f;
                float streetEnd=map.avenueCenter(i+1)-map.avenueHalf(i+1)-4f;
                float first=(float)Math.ceil(Math.max(streetBegin,left-26f)/64f)*64f;
                for(float xx=first;xx<x1 && xx+26f<=streetEnd;xx+=64f)
                    rect(batch,xx,y-2f,26f,4f,.74f,.72f,.66f);
            }
        }
        // Four crossings around every junction; bars span each road's actual width.
        for(int j=j0;j<=j1;j++)for(int i=i0;i<=i1;i++) {
            float x=map.avenueCenter(i),y=map.streetCenter(j);
            if(x<left-190||x>left+w+190||y<bottom-190||y>bottom+h+190)continue;
            float av=map.avenueHalf(i),st=map.streetHalf(j);
            for(float dx=-av+12f;dx<=av-18f;dx+=18f) {
                rect(batch,x+dx,y-st-29f,9f,20f,.79f,.77f,.72f);
                rect(batch,x+dx,y+st+9f,9f,20f,.79f,.77f,.72f);
            }
            for(float dy=-st+12f;dy<=st-18f;dy+=18f) {
                rect(batch,x-av-29f,y+dy,20f,9f,.79f,.77f,.72f);
                rect(batch,x+av+9f,y+dy,20f,9f,.79f,.77f,.72f);
            }
        }
    }

    private void drawObstacle(SpriteBatch b,Obstacle o) {
        switch(o.kind) {
          case BUILDING: drawBuilding(b,o);break;
          case CAR:
            if(o.width>o.height) sprite(b,(o.style&1)==0?"car_horizontal":"car_horizontal_blue",o.x,o.y,o.width,o.height);
            else sprite(b,(o.style&1)==0?"car_vertical":"car_blue",o.x,o.y,o.width,o.height);
            break;
          case DUMPSTER: sprite(b,(o.style&1)==0?"dumpster":"dumpster_blue",o.x,o.y,o.width,o.height);break;
          case BARRICADE: sprite(b,"barrier",o.x,o.y,o.width,o.height);break;
          case PLANTER: sprite(b,"planter_long",o.x,o.y,o.width,o.height);break;
          case TENT: sprite(b,"tent",o.x,o.y,o.width,o.height);break;
          case CRATE: sprite(b,"crate",o.x,o.y,o.width,o.height);break;
          case FENCE: sprite(b,"fence",o.x,o.y,o.width,o.height);break;
          default:break;
        }
    }

    private void drawBuilding(SpriteBatch b,Obstacle o) {
        float x=o.x,y=o.y,w=o.width,h=o.height;
        // Deep shadow, vertical wall and roof silhouettes.
        rect(b,x+9,y-16,w+3,h+12,.10f,.10f,.12f);
        rect(b,x-4,y-5,w+8,h+10,.33f,.34f,.35f);
        rect(b,x+1,y+1,w-2,h-2,.20f,.21f,.22f);
        float inset=8f;
        drawMaterial(b, ROOF, x+inset,y+inset,w-2f*inset,h-2f*inset);
        // Rooftop caps / occasional equipment vary between industrial/residential.
        rect(b,x+4,y+h-10,w-8,6,.44f,.43f,.41f);
        if(w>165 && h>125) {
            sprite(b,"roof_unit",x+w*.60f,y+h*.58f,56,43);
            if(o.style%3==0) sprite(b,"powerbox",x+w*.22f,y+h*.54f,45,54);
        }
        String facade;
        if(o.style<3)facade=new String[]{"shop_front","punk_shop","shutter_front"}[o.style];
        else if(o.style<6)facade=new String[]{"shop_front","garage_front","shutter_front"}[o.style-3];
        else if(o.style<9)facade=new String[]{"house_front","brick_front","plaster_front"}[o.style-6];
        else facade=new String[]{"warehouse_front","garage_front","shutter_front"}[(o.style-9)%3];
        // The facade faces south, with a narrow shadow at the doorstep.
        sprite(b,facade,x-2,y-10,w+4,Math.min(126f,h*.62f+30f));
        if(o.style%4==0) sprite(b,"graffiti_wall",x+w*.57f,y+4,w*.31f,45);
    }

    private void drawDecoration(SpriteBatch b,Decoration d,boolean broken,int variant) {
        String name;
        switch(d.kind) {
          case TRASH:name=broken?"garbage_broken_"+(variant+1):"garbage";break;
          case PAPER:name="paper";break;
          case GRAFFITI:name="graffiti_wall";break;
          case BENCH:name="bench";break;
          case LAMP:name="lamppost";break;
          case POWER:name="powerbox";break;
          case BARREL:name="barrels";break;
          case VENDING:name="vending";break;
          case TIRES:name="tires";break;
          case PALLET:name="pallet";break;
          case BIN:name="bin";break;
          case FENCE:name="fence";break;
          case PUDDLE:
            sprite(b,"puddle",d.x,d.y,d.width,d.height);return;
          case WEEDS:
            sprite(b,"weeds",d.x,d.y,d.width,d.height);return;
          case MANHOLE:
            // Drain decoration: centered cover, no asphalt tile frame.
            b.setColor(.32f,.33f,.34f,1f);
            b.draw(pixel,d.x,d.y,d.width,d.height);
            b.setColor(.55f,.55f,.53f,1f);
            b.draw(pixel,d.x+5f,d.y+5f,Math.max(1f,d.width-10f),Math.max(1f,d.height-10f));return;
          default:return;
        }
        sprite(b,name,d.x,d.y,d.width,d.height);
    }

    private void sprite(SpriteBatch batch,String key,float x,float y,float w,float h) {
        Texture t=sprites.get(key);
        if(t==null)return;
        batch.setColor(1f,1f,1f,1f);
        batch.draw(t,x,y,w,h);
    }
    private void rect(SpriteBatch b,float x,float y,float w,float h,float r,float g,float bl) {
        b.setColor(r,g,bl,1f);b.draw(pixel,x,y,w,h);
    }
    private static boolean visible(float x,float y,float w,float h,float left,float bottom,float vw,float vh) {
        return x+w>=left && x<=left+vw && y+h>=bottom && y<=bottom+vh;
    }
    @Override public void dispose() {
        for(Texture t:surfaceTextures)if(t!=null)t.dispose();
        for(Texture t:sprites.values())t.dispose();
    }
}
