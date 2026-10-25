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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** coordina las entidades, el combate y el dibujo de la arena. */
public final class GameWorld implements Disposable {

    private static final float ARENA_LEFT = 24f;
    private static final float ARENA_BOTTOM = 68f;
    private static final float ARENA_TOP_MARGIN = 88f;
    private static final float ARENA_RIGHT_MARGIN = 24f;

    private final float width;
    private final float height;
    private final Texture pixel;
    private final Player player;
    private final PlayerSpriteRenderer playerSpriteRenderer;
    private float elapsedTime;
    private final Rectangle arena;
    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private float pulse;

    public GameWorld(float width, float height) {
        this(width, height, true);
    }

    /** permite validar la simulación sin crear recursos gráficos. */
    public GameWorld(float width, float height, boolean graphics) {
        this.width = width;
        this.height = height;
        arena = new Rectangle(ARENA_LEFT, ARENA_BOTTOM,
                width - ARENA_LEFT - ARENA_RIGHT_MARGIN,
                height - ARENA_BOTTOM - ARENA_TOP_MARGIN);
        player = new Player(arena.x + arena.width / 2f,
                arena.y + arena.height / 2f, arena);

        if (graphics) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 1f, 1f, 1f);
            pixmap.fill();
            pixel = new Texture(pixmap);
            pixmap.dispose();
            playerSpriteRenderer = new PlayerSpriteRenderer();
        } else {
            pixel = null;
            playerSpriteRenderer = null;
        }
        reset();
    }

    public void update(float delta) {
        if (!Float.isFinite(delta) || delta <= 0f || !player.isAlive()) {
            return;
        }
        player.update(delta);
        updateCombat(delta);
    }

    public void update(float delta, float horizontal, float vertical) {
        if (!Float.isFinite(delta) || delta <= 0f || !player.isAlive()) {
            return;
        }
        player.update(delta, horizontal, vertical);
        updateCombat(delta);
    }

    private void updateCombat(float delta) {
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
        elapsedTime = 0f;
        pulse = 0f;
        zombies.clear();
        projectiles.clear();
        // grupo inicial para probar combate sin incorporar oleadas.
        zombies.add(new Zombie(arena.x + 50f, arena.y + 50f, ZombieType.NORMAL, arena));
        zombies.add(new Zombie(arena.x + 50f, arena.y + arena.height - 50f,
                ZombieType.NORMAL, arena));
        zombies.add(new Zombie(arena.x + arena.width - 50f, arena.y + 50f,
                ZombieType.TANK, arena));
        zombies.add(new Zombie(arena.x + arena.width - 50f,
                arena.y + arena.height - 50f, ZombieType.FAST, arena));
        zombies.add(new Zombie(arena.x + arena.width / 2f,
                arena.y + arena.height - 50f, ZombieType.NORMAL, arena));
    }

    public void draw(SpriteBatch batch) {
        // Asphalt floor and arena borders.
        rect(batch, 16f, 60f, width - 32f, height - 140f, 0.10f, 0.11f, 0.15f);
        rect(batch, 16f, 60f, width - 32f, 8f, 0.72f, 0.15f, 0.30f);
        rect(batch, 16f, height - 88f, width - 32f, 8f, 0.72f, 0.15f, 0.30f);
        rect(batch, 16f, 60f, 8f, height - 140f, 0.72f, 0.15f, 0.30f);
        rect(batch, width - 24f, 60f, 8f, height - 140f, 0.72f, 0.15f, 0.30f);

        // Quiet grid to make movement easy to assess.
        for (float x = 56f; x < width - 24f; x += 40f) {
            rect(batch, x, 68f, 1f, height - 156f, 0.16f, 0.16f, 0.19f);
        }
        for (float y = 100f; y < height - 88f; y += 40f) {
            rect(batch, 24f, y, width - 48f, 1f, 0.16f, 0.16f, 0.19f);
        }

        for (Zombie zombie : zombies) {
            float radius = zombie.getRadius();
            rect(batch, zombie.getX() - radius, zombie.getY() - radius,
                    2f * radius, 2f * radius,
                    zombie.getType() == ZombieType.TANK ? 0.65f : 0.3f,
                    zombie.getType() == ZombieType.FAST ? 0.8f : 0.5f, 0.18f);
            rect(batch, zombie.getX() - radius, zombie.getY() + radius + 3f,
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
        // Player x/y denotes the gameplay hitbox center, approximately between the feet.
        rect(batch, player.getX() - 14f, player.getY() - 14f,
                28f, 4f, 0.045f, 0.045f, 0.06f);
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

    public float getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public void dispose() {
        if (playerSpriteRenderer != null) {
            playerSpriteRenderer.dispose();
        }
        if (pixel != null) {
            pixel.dispose();
        }
    }
}
