package com.deadbeat.movement;

import com.deadbeat.entities.*;

public interface MovementStrategy {
    void move(Zombie zombie, Player target, float delta);
}
