package com.deadbeat.movement;

import com.deadbeat.entities.*;

public final class ChaseMovement implements MovementStrategy {
    public void move(Zombie z, Player p, float delta) {
        z.advance(p.getX() - z.getX(), p.getY() - z.getY(), delta);
    }
}
