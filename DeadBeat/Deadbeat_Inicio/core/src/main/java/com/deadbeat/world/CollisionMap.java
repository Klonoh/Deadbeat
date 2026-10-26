package com.deadbeat.world;

/** Read-only collision query used by moving entities; independent of rendering. */
public interface CollisionMap {
    /** Returns true if a circular hitbox would overlap a solid obstacle. */
    boolean collidesCircle(float centerX, float centerY, float radius);
}
