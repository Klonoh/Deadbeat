package com.deadbeat.movement;

import com.deadbeat.entities.*;

public final class ZigzagMovement implements MovementStrategy {
    public void move(Zombie z, Player p, float delta) {
        float dx = p.getX() - z.getX(), dy = p.getY() - z.getY();
        float sway = (float) Math.sin(z.getAge() * 8) * 0.7f;
        z.advance(dx - dy * sway, dy + dx * sway, delta);
    }
}
