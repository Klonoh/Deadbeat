package com.deadbeat;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.deadbeat.screens.GameScreen;

/** Punto de entrada de la lógica del juego (independiente del sistema operativo). */
public final class DeadbeatGame extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen());
    }

    @Override
    public void dispose() {
        Screen activeScreen = getScreen();
        if (activeScreen != null) {
            activeScreen.dispose();
        }
        super.dispose();
    }
}
