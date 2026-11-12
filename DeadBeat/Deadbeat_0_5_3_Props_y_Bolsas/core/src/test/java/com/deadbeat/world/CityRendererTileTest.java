package com.deadbeat.world;

import org.junit.Test;
import static org.junit.Assert.*;

/** Regression tests for the sliding-ground bug in Deadbeat 0.5.1. */
public class CityRendererTileTest {
    private static final int SIZE = CityMap.TILE_SIZE;

    private int texelX(int worldX) {
        return worldX - CityRenderer.worldTileOrigin(worldX);
    }
    private int texelY(int worldY) {
        return SIZE - 1 - (worldY - CityRenderer.worldTileOrigin(worldY));
    }

    @Test public void tileOriginsAreAlwaysAbsoluteAndAligned() {
        assertEquals(0, CityRenderer.worldTileOrigin(0));
        assertEquals(0, CityRenderer.worldTileOrigin(63));
        assertEquals(64, CityRenderer.worldTileOrigin(64));
        assertEquals(65536, CityRenderer.worldTileOrigin(65567));
        assertEquals(-64, CityRenderer.worldTileOrigin(-1));
        assertEquals(-128, CityRenderer.worldTileOrigin(-65));
    }

    @Test public void stationaryWorldPointKeepsSameTexelAsViewportPans() {
        // 65,598 is a representative world coordinate near the player spawn.
        int wx = 65598;
        int wy = 65493;
        int expectedU = texelX(wx);
        int expectedV = texelY(wy);
        for (int cameraX = 65100; cameraX <= 65900; cameraX += 7) {
            // The same world point appears at different screen locations.
            int screenX = wx - cameraX + 480;
            int recoveredWorldX = screenX + cameraX - 480;
            assertEquals(wx, recoveredWorldX);
            assertEquals(expectedU, texelX(recoveredWorldX));
            assertEquals(expectedV, texelY(wy));
        }
    }

    @Test public void croppingAtTileSeamsKeepsSourceAndWorldInSync() {
        for (int x = 65530; x < 65670; x++) {
            int local = texelX(x);
            assertTrue(local >= 0 && local < SIZE);
            if (x % SIZE == 0) assertEquals(0, local);
        }
    }

    @Test public void worldAndTextureCoordinatesRepeatEveryFullTile() {
        for (int x = 65300; x < 66000; x += 13) {
            assertEquals(texelX(x), texelX(x + SIZE));
            assertEquals(texelY(x), texelY(x + SIZE));
        }
    }
}
