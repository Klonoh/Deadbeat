package com.deadbeat.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.deadbeat.entities.Player;
import com.deadbeat.entities.ExperienceOrb;
import com.deadbeat.entities.Damageable;
import com.deadbeat.entities.Projectile;
import com.deadbeat.entities.Zombie;
import com.deadbeat.entities.ZombieType;
import com.deadbeat.graphics.PlayerSpriteRenderer;
import com.deadbeat.graphics.ZombieSpriteRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.deadbeat.progression.Upgrade;
import com.deadbeat.progression.UpgradeSelection;
import com.deadbeat.progression.Wave;
import com.deadbeat.progression.WaveBuilder;

/** integra el mapa de la ciudad con las entidades de combate. */
public final class GameWorld implements Disposable {
    // Mayor densidad simultánea sin dejar colas de spawn demasiado largas.
    private static final int MAX_ZOMBIES = 110;
    private final UpgradeSelection upgrades = new UpgradeSelection();
    private final Wave[] waves;
    private final Random random = new Random();
    private int waveIndex;
    private int spawned;
    private float waveTime;
    private boolean wavesFinished;
    private final Texture pixel;
    private final CityMap cityMap;
    private final CityRenderer cityRenderer;
    private final Player player;
    private final PlayerSpriteRenderer playerSpriteRenderer;
    private final ZombieSpriteRenderer zombieSpriteRenderer;
    private final Rectangle arena;
    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<ExperienceOrb> orbs = new ArrayList<>();
    private float elapsedTime;
    private float pulse;

    public GameWorld() {
        this(true);
    }

    /** permite probar la simulación sin recursos gráficos. */
    public GameWorld(boolean graphics) {
        this(graphics, 30f);
    }

    /** permite acelerar las oleadas en pruebas sin cambiar la campaña. */
    public GameWorld(boolean graphics, float waveDuration) {
        waves = WaveBuilder.campaign(waveDuration);
        cityMap = new CityMap();
        arena = new Rectangle(0f, 0f, CityMap.WORLD_SIZE, CityMap.WORLD_SIZE);
        player = new Player(CityMap.SPAWN + 64f, CityMap.SPAWN + 64f, arena);
        if (graphics) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 1f, 1f, 1f);
            pixmap.fill();
            pixel = new Texture(pixmap);
            pixmap.dispose();
            cityRenderer = new CityRenderer(cityMap, pixel);
            playerSpriteRenderer = new PlayerSpriteRenderer();
            zombieSpriteRenderer = new ZombieSpriteRenderer();
        } else {
            pixel = null;
            cityRenderer = null;
            playerSpriteRenderer = null;
            zombieSpriteRenderer = null;
        }
        reset();
    }

    public void update(float delta) {
        if (!Float.isFinite(delta) || delta <= 0f || !player.isAlive() || isChoosingUpgrade()) {
            return;
        }
        player.update(delta, cityMap);
        updateCombat(delta);
    }

    public void update(float delta, float horizontal, float vertical) {
        if (!Float.isFinite(delta) || delta <= 0f || !player.isAlive() || isChoosingUpgrade()) {
            return;
        }
        player.update(delta, horizontal, vertical, cityMap);
        updateCombat(delta);
    }

    private void updateCombat(float delta) {
        cityMap.breakGarbageNear(player.getX(), player.getY(), player.getRadius());
        elapsedTime += delta;
        pulse = Math.max(0f, pulse - delta);
        updateSpawning(delta);
        for (Zombie zombie : zombies) {
            zombie.update(delta, player);
        }
        player.updateWeapons(delta, this);

        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            float oldX = projectile.getX();
            float oldY = projectile.getY();
            projectile.update(delta);
            if (projectile.isAlive()) {
                // Barrido segmentado: una bala rapida no puede saltarse una valla
                // ni impactar a un zombie situado al otro lado de un edificio.
                float dx = projectile.getX() - oldX;
                float dy = projectile.getY() - oldY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                float maxStep = Math.max(2f, projectile.getRadius() * 0.5f);
                int steps = Math.max(1, (int) Math.ceil(distance / maxStep));
                for (int step = 0; step <= steps && projectile.isAlive(); step++) {
                    float t = (float) step / steps;
                    float px = oldX + dx * t;
                    float py = oldY + dy * t;
                    if (cityMap.collidesCircle(px, py, projectile.getRadius())) {
                        projectile.expire();
                        break;
                    }
                    for (Zombie zombie : zombies) {
                        if (zombie.isAlive()
                                && touch(px, py, projectile.getRadius(),
                                        zombie.getX(), zombie.getY(), zombie.getRadius())) {
                            damage(zombie, projectile.getDamage());
                            projectile.expire();
                            break;
                        }
                    }
                }
            }
            if (!arena.contains(projectile.getX(), projectile.getY())) {
                projectile.expire();
            }
            if (!projectile.isAlive()) {
                projectiles.remove(i);
            }
        }

        for (int i = zombies.size() - 1; i >= 0; i--) {
            Zombie zombie = zombies.get(i);
            if (!zombie.isAlive()) {
                orbs.add(new ExperienceOrb(zombie.getX(), zombie.getY(),
                        zombie.getType().experience));
                zombies.remove(i);
            } else if (touch(player.getX(), player.getY(), player.getRadius(),
                    zombie.getX(), zombie.getY(), zombie.getRadius())) {
                damage(player, zombie.getType().damage);
            }
        }
        collectExperience();
        advanceWave();
    }

    private void collectExperience() {
        if (!player.isAlive()) {
            return;
        }
        for (int i = orbs.size() - 1; i >= 0; i--) {
            ExperienceOrb orb = orbs.get(i);
            // conserva los radios de recogida de la versión original.
            if (touch(player.getX(), player.getY(), 28f, orb.getX(), orb.getY(), 5f)) {
                upgrades.award(player.addExperience(orb.getAmount()));
                orbs.remove(i);
            }
        }
    }

    public boolean isChoosingUpgrade() {
        return upgrades.getPending() > 0;
    }

    public List<Upgrade> getUpgradeChoices() {
        return upgrades.getChoices();
    }

    public int getPendingUpgrades() {
        return upgrades.getPending();
    }

    public void chooseUpgrade(int index) {
        if (player.isAlive() && !(wavesFinished && zombies.isEmpty())) {
            upgrades.choose(index, player);
        }
    }

    public void cancelUpgradeSelection() {
        upgrades.cancel();
    }

    public List<ExperienceOrb> getOrbs() {
        return Collections.unmodifiableList(orbs);
    }

    private void updateSpawning(float delta) {
        if (wavesFinished) {
            return;
        }
        waveTime += delta;
        Wave wave = waves[waveIndex];
        int due = waveTime >= wave.getDuration() ? wave.getTotal()
                : Math.min(wave.getTotal(),
                        1 + (int) (waveTime / wave.getDuration() * wave.getTotal()));
        while (spawned < due && zombies.size() < MAX_ZOMBIES) {
            if (!spawn(wave.typeAt(spawned))) {
                break;
            }
            spawned++;
        }
    }

    private void advanceWave() {
        if (wavesFinished || !player.isAlive()) {
            return;
        }
        Wave wave = waves[waveIndex];
        if (waveTime >= wave.getDuration() && spawned == wave.getTotal()) {
            if (waveIndex == waves.length - 1) {
                wavesFinished = true;
            } else {
                waveIndex++;
                waveTime = 0f;
                spawned = 0;
            }
        }
    }

    private boolean spawn(ZombieType type) {
        double phase = random.nextDouble() * Math.PI * 2;
        // anillos locales: el borde de la ciudad queda demasiado lejos.
        for (int attempt = 0; attempt < 96; attempt++) {
            double angle = phase + (attempt % 32) * Math.PI * 2 / 32;
            float distance = 320f + (attempt / 32) * 80f;
            float x = player.getX() + (float) Math.cos(angle) * distance;
            float y = player.getY() + (float) Math.sin(angle) * distance;
            if (cityMap.collidesCircle(x, y, type.radius)) {
                continue;
            }
            boolean occupied = false;
            for (Zombie zombie : zombies) {
                if (zombie.isAlive() && touch(x, y, type.radius,
                        zombie.getX(), zombie.getY(), zombie.getRadius())) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) {
                zombies.add(new Zombie(x, y, type, arena, cityMap));
                return true;
            }
        }
        // conserva el cupo y vuelve a intentar en la siguiente actualización.
        return false;
    }

    public int getCurrentWave() {
        return waveIndex + 1;
    }

    public float getWaveRemaining() {
        return Math.max(0f, waves[waveIndex].getDuration() - waveTime);
    }

    public boolean areWavesFinished() {
        return wavesFinished;
    }

    private static void damage(Damageable target, float amount) {
        if (target.isAlive()) {
            target.takeDamage(amount);
        }
    }

    public static boolean touch(float x, float y, float radius,
                                float otherX, float otherY, float otherRadius) {
        float dx = x - otherX;
        float dy = y - otherY;
        return dx * dx + dy * dy <= (radius + otherRadius) * (radius + otherRadius);
    }

    public Zombie findNearestZombie(float x, float y, float range) {
        Zombie nearest = null;
        float best = range * range;
        for (Zombie zombie : zombies) {
            if (zombie.isAlive()) {
                float distance = (x - zombie.getX()) * (x - zombie.getX())
                        + (y - zombie.getY()) * (y - zombie.getY());
                if (distance <= best) {
                    best = distance;
                    nearest = zombie;
                }
            }
        }
        return nearest;
    }

    public void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    public boolean strikeNearby(float range, float amount) {
        boolean hit = false;
        for (Zombie zombie : zombies) {
            if (zombie.isAlive() && touch(player.getX(), player.getY(), range,
                    zombie.getX(), zombie.getY(), zombie.getRadius())) {
                damage(zombie, amount);
                hit = true;
            }
        }
        if (hit) {
            pulse = 0.16f;
        }
        return hit;
    }

    public List<Zombie> getZombies() {
        return Collections.unmodifiableList(zombies);
    }

    public int getProjectileCount() {
        return projectiles.size();
    }

    public void reset() {
        upgrades.reset();
        player.reset();
        cityMap.resetGarbage();
        elapsedTime = 0f;
        pulse = 0f;
        zombies.clear();
        projectiles.clear();
        orbs.clear();
        waveIndex = 0;
        waveTime = 0f;
        spawned = 0;
        wavesFinished = false;
    }

    public void draw(SpriteBatch batch, float viewLeft, float viewBottom,
                     float viewWidth, float viewHeight) {
        cityRenderer.draw(batch, viewLeft, viewBottom, viewWidth, viewHeight);
        for (ExperienceOrb orb : orbs) {
            rect(batch, orb.getX() - 4f, orb.getY() - 4f, 8f, 8f, 0.2f, 0.9f, 1f);
        }
        for (Zombie zombie : zombies) {
            float radius = zombie.getRadius();
            if (zombie.getType() == ZombieType.NORMAL) {
                rect(batch, zombie.getX() - 13f, zombie.getY() - 11f,
                        26f, 4f, .09f, .09f, .10f);
                zombieSpriteRenderer.draw(batch, zombie);
            } else if (zombie.getType() == ZombieType.TANK) {
                // El tanque mantiene una silueta mayor (128x128 vs. 64x80).
                rect(batch, zombie.getX() - 25f, zombie.getY() - 14f,
                        50f, 7f, .08f, .08f, .10f);
                zombieSpriteRenderer.draw(batch, zombie);
            } else {
                // Fast variants are smaller than the common zombie sprite.
                rect(batch, zombie.getX() - 10f, zombie.getY() - 9f,
                        20f, 3f, .09f, .09f, .10f);
                zombieSpriteRenderer.draw(batch, zombie);
            }
            float barY = zombie.getType() == ZombieType.NORMAL
                    ? zombie.getY() + 70f
                    : zombie.getType() == ZombieType.TANK
                    ? zombie.getY() + 114f : zombie.getY() + 59f;
            rect(batch, zombie.getX() - radius, barY,
                    2f * radius * zombie.getHealth() / zombie.getType().health,
                    3f, 0.95f, 0.2f, 0.4f);
        }
        for (Projectile projectile : projectiles) {
            rect(batch, projectile.getX() - 5f, projectile.getY() - 3f,
                    10f, 6f, 1f, 0.2f, 0.7f);
        }
        if (pulse > 0f) {
            for (int i = 0; i < 32; i++) {
                double angle = i * Math.PI * 2 / 32;
                rect(batch, player.getX() + (float) Math.cos(angle) * 65f - 2f,
                        player.getY() + (float) Math.sin(angle) * 65f - 2f,
                        4f, 4f, 0.9f, 1f, 0.3f);
            }
        }
        drawPlayer(batch);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /** Draws an unobtrusive shadow and the current directional animation frame. */
    private void drawPlayer(SpriteBatch batch) {
        rect(batch, player.getX() - 17f, player.getY() - 15f,
                34f, 5f, 0.08f, 0.08f, 0.10f);
        playerSpriteRenderer.draw(batch, player);
    }

    private void rect(SpriteBatch batch, float x, float y, float w, float h,
                      float red, float green, float blue) {
        batch.setColor(red, green, blue, 1f);
        batch.draw(pixel, x, y, w, h);
    }

    public Player getPlayer() {
        return player;
    }

    public CityMap getCityMap() {
        return cityMap;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public void dispose() {
        if (playerSpriteRenderer != null) {
            playerSpriteRenderer.dispose();
        }
        if (zombieSpriteRenderer != null) {
            zombieSpriteRenderer.dispose();
        }
        if (cityRenderer != null) {
            cityRenderer.dispose();
        }
        if (pixel != null) {
            pixel.dispose();
        }
    }
}
