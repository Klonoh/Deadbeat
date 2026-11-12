package com.deadbeat.weapons;

import com.deadbeat.entities.*;
import com.deadbeat.world.GameWorld;

public final class ElectricGuitar extends Weapon {
    public ElectricGuitar() {
        super(22, 0.85f);
    }

    protected boolean performAttack(Player p, GameWorld world) {
        Zombie z = world.findNearestZombie(p.getX(), p.getY(), 300);
        if (z == null)
            return false;

        world.addProjectile(new Projectile(p.getX(), p.getY(),
                z.getX() - p.getX(), z.getY() - p.getY(), getDamage()));
        return true;
    }
}
