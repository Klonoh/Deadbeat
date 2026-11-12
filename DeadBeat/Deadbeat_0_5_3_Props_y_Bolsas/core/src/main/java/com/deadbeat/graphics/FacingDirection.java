package com.deadbeat.graphics;

/**
 * Artwork row order. The first direction (DOWN) is the default player facing.
 * Angles are grouped in 45-degree sectors, including diagonals.
 */
public enum FacingDirection {
    DOWN, DOWN_RIGHT, RIGHT, UP_RIGHT, UP, UP_LEFT, LEFT, DOWN_LEFT;

    private static final FacingDirection[] DIRECTIONS = values();

    public static FacingDirection fromVector(float dx, float dy) {
        if (!Float.isFinite(dx) || !Float.isFinite(dy)
                || (dx == 0f && dy == 0f)) {
            return DOWN;
        }
        double eighthTurns = (Math.atan2(dy, dx) + Math.PI / 2.0)
                / (Math.PI / 4.0);
        int row = Math.floorMod((int) Math.round(eighthTurns), 8);
        return DIRECTIONS[row];
    }
}
