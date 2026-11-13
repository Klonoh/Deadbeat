package com.deadbeat.progression;

import com.deadbeat.entities.Player;
import com.deadbeat.weapons.ElectricGuitar;
import com.deadbeat.weapons.Drumsticks;
import com.deadbeat.weapons.Weapon;

public enum Upgrade {
    GUITAR_DAMAGE("Guitarra: dano +20%"),
    GUITAR_RATE("Guitarra: intervalo -10%"),
    DRUM_DAMAGE("Baquetas: dano +20%"),
    DRUM_RATE("Baquetas: intervalo -10%"),
    MOVE_SPEED("Movimiento +10%"),
    MAX_HEALTH("Vida maxima +20 y curar 20");

    private final String label;

    Upgrade(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void apply(Player player) {
        switch (this) {
            case MOVE_SPEED:
                player.setMoveSpeed(Math.min(320f, player.getMoveSpeed() * 1.1f));
                break;
            case MAX_HEALTH:
                player.increaseMaxHealth();
                break;
            default:
                boolean guitar = this == GUITAR_DAMAGE || this == GUITAR_RATE;
                for (Weapon weapon : player.getWeapons()) {
                    if (!(guitar ? weapon instanceof ElectricGuitar : weapon instanceof Drumsticks)) {
                        continue;
                    }
                    if (this == GUITAR_DAMAGE || this == DRUM_DAMAGE) {
                        weapon.improveDamage();
                    } else {
                        weapon.improveRate();
                    }
                }
        }
    }
}
