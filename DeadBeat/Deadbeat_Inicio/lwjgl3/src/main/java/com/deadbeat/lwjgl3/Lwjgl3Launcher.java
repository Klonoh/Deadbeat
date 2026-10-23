package com.deadbeat.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.deadbeat.DeadbeatGame;

/** Lanzador del videojuego en Windows, Linux o macOS. */
public final class Lwjgl3Launcher {

    private Lwjgl3Launcher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("DEADBEAT - Prototipo 0.3 - Sprites");
        config.setWindowedMode(960, 540);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new DeadbeatGame(), config);
    }
}
