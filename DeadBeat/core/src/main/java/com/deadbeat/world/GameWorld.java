package com.deadbeat.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.deadbeat.entities.Player;
import com.deadbeat.entities.Damageable;
import com.deadbeat.entities.Projectile;
import com.deadbeat.entities.Zombie;
import com.deadbeat.entities.ZombieType;
import com.deadbeat.graphics.PlayerSpriteRenderer;
import com.deadbeat.graphics.ZombieSpriteRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** integra el mapa de la ciudad con las entidades de combate. */
public final class GameWorld implements Disposable {
    private final Texture pixel;
    private final CityMap cityMap;
    private final CityRenderer cityRenderer;
    private final Player player;
    private final PlayerSpriteRenderer playerSpriteRenderer;
    private final ZombieSpriteRenderer zombieSpriteRenderer;
    private final Rectangle arena;
    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private float elapsedTime;
    private float pulse;

    public GameWorld() {
        this(true);
    }

    /** permite probar la simulación sin recursos gráficos. */
    public GameWorld(boolean graphics) {
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
        if (!Float.isFinite(delta) || delta <= 0f || !player.isAlive()) {
            return;
        }
        player.update(delta, cityMap);
        updateCombat(delta);
    }

    public void update(float delta, float horizontal, float vertical) {
        if (!Float.isFinite(delta) || delta <= 0f || !player.isAlive()) {
            return;
        }
        player.update(delta, horizontal, vertical, cityMap);
        updateCombat(delta);
    }

    private void updateCombat(float delta) {
        cityMap.breakGarbageNear(player.getX(), player.getY(), player.getRadius());
        elapsedTime += delta;
        pulse = Math.max(0f, pulse - delta);
        for (Zombie zombie : zombies) {
            zombie.update(delta, player);
        }
        player.updateWeapons(delta, this);

        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(delta);
            for (Zombie zombie : zombies) {
                if (projectile.isAlive() && zombie.isAlive()
                        && touch(projectile.getX(), projectile.getY(), projectile.getRadius(),
                                zombie.getX(), zombie.getY(), zombie.getRadius())) {
                    damage(zombie, projectile.getDamage());
                    projectile.expire();
                    break;
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
                zombies.remove(i);
            } else if (touch(player.getX(), player.getY(), player.getRadius(),
                    zombie.getX(), zombie.getY(), zombie.getRadius())) {
                damage(player, zombie.getType().damage);
            }
        }
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
        player.reset();
        cityMap.resetGarbage();
        elapsedTime = 0f;
        pulse = 0f;
        zombies.clear();
        projectiles.clear();
        ZombieType[] types = {ZombieType.NORMAL, ZombieType.NORMAL, ZombieType.TANK,
                ZombieType.FAST, ZombieType.NORMAL};
        // busca posiciones libres cerca del jugador, sin incorporar oleadas.
        for (int i = 0; i < types.length; i++) {
            boolean spawned = false;
            for (int attempt = 0; attempt < 120 && !spawned; attempt++) {
                double angle = i * Math.PI * 2 / types.length + attempt * 0.2;
                float distance = 180f + (attempt / 30) * 40f;
                float x = player.getX() + (float) Math.cos(angle) * distance;
                float y = player.getY() + (float) Math.sin(angle) * distance;
                if (!cityMap.collidesCircle(x, y, types[i].radius)) {
                    zombies.add(new Zombie(x, y, types[i], arena));
                    spawned = true;
                }
            }
            if (!spawned) {
                throw new IllegalStateException("No se encontro una posicion libre para el zombie");
            }
        }
    }

    public void draw(SpriteBatch batch, float viewLeft, float viewBottom,
                     float viewWidth, float viewHeight) {
        cityRenderer.draw(batch, viewLeft, viewBottom, viewWidth, viewHeight);
        for (Zombie zombie : zombies) {
            float radius = zombie.getRadius();
            if (zombie.getType() == ZombieType.NORMAL) {
                // Solo NORMAL utiliza las tres variantes visuales.
                rect(batch, zombie.getX() - 13f, zombie.getY() - 11f,
                        26f, 4f, .09f, .09f, .10f);
                zombieSpriteRenderer.draw(batch, zombie);
            } else {
                // FAST y TANK conservan el aspecto provisional previo.
                rect(batch, zombie.getX() - radius, zombie.getY() - radius,
                        2f * radius, 2f * radius,
                        zombie.getType() == ZombieType.TANK ? 0.65f : 0.3f,
                        zombie.getType() == ZombieType.FAST ? 0.8f : 0.5f, 0.18f);
            }
            float barY = zombie.getType() == ZombieType.NORMAL
                    ? zombie.getY() + 70f : zombie.getY() + radius + 3f;
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
