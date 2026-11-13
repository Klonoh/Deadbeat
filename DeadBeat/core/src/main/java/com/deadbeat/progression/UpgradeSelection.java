package com.deadbeat.progression;

import com.deadbeat.entities.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** administra las selecciones sin depender de pantallas ni recursos gráficos. */
public final class UpgradeSelection {
    private static final int LIMIT = 5;
    private final int[] counts = new int[Upgrade.values().length];
    private final List<Upgrade> choices = new ArrayList<>();
    private int pending;

    public void award(int levels) {
        if (levels <= 0) {
            return;
        }
        pending += levels;
        if (choices.isEmpty()) {
            offer();
        }
    }

    private void offer() {
        choices.clear();
        List<Upgrade> available = new ArrayList<>();
        for (Upgrade upgrade : Upgrade.values()) {
            if (counts[upgrade.ordinal()] < LIMIT) {
                available.add(upgrade);
            }
        }
        if (available.isEmpty()) {
            pending = 0;
            return;
        }
        Collections.shuffle(available);
        choices.addAll(available.subList(0, Math.min(3, available.size())));
    }

    public void choose(int index, Player player) {
        if (!player.isAlive() || index < 0 || index >= choices.size() || pending == 0) {
            return;
        }
        Upgrade upgrade = choices.get(index);
        upgrade.apply(player);
        counts[upgrade.ordinal()]++;
        pending--;
        choices.clear();
        if (pending > 0) {
            offer();
        }
    }

    public List<Upgrade> getChoices() {
        return Collections.unmodifiableList(choices);
    }

    public int getPending() {
        return pending;
    }

    public void cancel() {
        pending = 0;
        choices.clear();
    }

    public void reset() {
        cancel();
        Arrays.fill(counts, 0);
    }
}
