package com.deadbeat.weapons;

import com.deadbeat.entities.Player;
import com.deadbeat.world.GameWorld;

public final class Drumsticks extends Weapon {
    public Drumsticks() {
        super(12, 0.55f);
    }

    protected boolean performAttack(Player p, GameWorld world) {
        return world.strikeNearby(65, getDamage());
    }
}
