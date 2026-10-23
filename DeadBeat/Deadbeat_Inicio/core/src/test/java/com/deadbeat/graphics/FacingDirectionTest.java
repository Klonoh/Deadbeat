package com.deadbeat.graphics;

import org.junit.Test;
import static org.junit.Assert.*;

public class FacingDirectionTest {
    @Test public void allEightDirectionsMatchSpriteSheetRows() {
        assertEquals(FacingDirection.DOWN, FacingDirection.fromVector(0f, -1f));
        assertEquals(FacingDirection.DOWN_RIGHT, FacingDirection.fromVector(1f, -1f));
        assertEquals(FacingDirection.RIGHT, FacingDirection.fromVector(1f, 0f));
        assertEquals(FacingDirection.UP_RIGHT, FacingDirection.fromVector(1f, 1f));
        assertEquals(FacingDirection.UP, FacingDirection.fromVector(0f, 1f));
        assertEquals(FacingDirection.UP_LEFT, FacingDirection.fromVector(-1f, 1f));
        assertEquals(FacingDirection.LEFT, FacingDirection.fromVector(-1f, 0f));
        assertEquals(FacingDirection.DOWN_LEFT, FacingDirection.fromVector(-1f, -1f));
    }

    @Test public void stationaryOrInvalidVectorUsesDefaultDirection() {
        assertEquals(FacingDirection.DOWN, FacingDirection.fromVector(0f, 0f));
        assertEquals(FacingDirection.DOWN, FacingDirection.fromVector(Float.NaN, 1f));
    }
}
