package com.deadbeat.audio;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxNativesLoader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;

/** Ejecuta las pantallas reales sin dispositivo de audio ni contexto OpenGL. */
final class AudioTestEnvironment implements AutoCloseable {
    private final Application oldApp = Gdx.app;
    private final Audio oldAudio = Gdx.audio;
    private final Files oldFiles = Gdx.files;
    private final Graphics oldGraphics = Gdx.graphics;
    private final Input oldInput = Gdx.input;
    private final GL20 oldGl = Gdx.gl;
    private final GL20 oldGl20 = Gdx.gl20;
    private final GL30 oldGl30 = Gdx.gl30;
    private final List<Disposable> resources = new ArrayList<>();
    final List<RecordingMusic> tracks = new ArrayList<>();
    final Set<Integer> keys = new HashSet<>();
    int mouseX;
    int mouseY;
    boolean clicked;
    boolean held;
    int exitRequests;
    private int nextId = 1;

    AudioTestEnvironment() {
        GdxNativesLoader.load();
        Path assets = Path.of("..", "assets").toAbsolutePath().normalize();
        if (!java.nio.file.Files.isDirectory(assets)) assets = Path.of("assets").toAbsolutePath();
        final Path assetDirectory = assets;
        Gdx.files = stub(Files.class, (proxy, method, args) -> {
            if (method.getReturnType() == FileHandle.class) {
                String file = String.valueOf(args[0]);
                if (method.getName().equals("classpath")) return new ClasspathHandle(file);
                Path resolved = assetDirectory.resolve(file);
                return java.nio.file.Files.exists(resolved)
                        ? new FileHandle(resolved.toFile()) : new ClasspathHandle(file);
            }
            return defaultValue(method.getReturnType());
        });
        Gdx.audio = stub(Audio.class, (proxy, method, args) -> {
            if (method.getName().equals("newMusic")) {
                RecordingMusic music = new RecordingMusic(((FileHandle) args[0]).path());
                tracks.add(music);
                return music;
            }
            return defaultValue(method.getReturnType());
        });
        Gdx.graphics = stub(Graphics.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getWidth": case "getBackBufferWidth": return 960;
                case "getHeight": case "getBackBufferHeight": return 540;
                case "getGLVersion":
                    return new GLVersion(Application.ApplicationType.Desktop, "2.0", "test", "test");
                case "getGL20": return Gdx.gl20;
            }
            return defaultValue(method.getReturnType());
        });
        Gdx.app = stub(Application.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getType": return Application.ApplicationType.Desktop;
                case "getGraphics": return Gdx.graphics;
                case "getFiles": return Gdx.files;
                case "getAudio": return Gdx.audio;
                case "exit": exitRequests++; return null;
                case "error": throw new AssertionError("Unexpected Gdx error: " + args[1]);
            }
            return defaultValue(method.getReturnType());
        });
        Gdx.input = stub(Input.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "isKeyJustPressed": case "isKeyPressed": return keys.contains((Integer) args[0]);
                case "getX": return mouseX;
                case "getY": return mouseY;
                case "isButtonJustPressed": return clicked && (Integer) args[0] == Input.Buttons.LEFT;
                case "isButtonPressed": return held && (Integer) args[0] == Input.Buttons.LEFT;
            }
            return defaultValue(method.getReturnType());
        });
        Gdx.gl30 = null;
        Gdx.gl = Gdx.gl20 = stub(GL20.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "glCreateShader": case "glCreateProgram": case "glGenTexture": case "glGenBuffer":
                    return nextId++;
                case "glGetShaderiv": ((IntBuffer) args[2]).put(0, 1); return null;
                case "glGetProgramiv":
                    ((IntBuffer) args[2]).put(0, (Integer) args[1] == GL20.GL_LINK_STATUS ? 1 : 0);
                    return null;
                case "glGetShaderInfoLog": case "glGetProgramInfoLog": case "glGetString": return "";
            }
            return defaultValue(method.getReturnType());
        });
    }

    <T extends Disposable> T own(T resource) {
        resources.add(resource);
        return resource;
    }

    void release(Disposable resource) {
        resources.remove(resource);
        resource.dispose();
    }

    void frame(Screen screen, int... pressed) {
        keys.clear();
        for (int key : pressed) keys.add(key);
        try {
            screen.render(0f);
        } finally {
            keys.clear();
            clicked = false;
            held = false;
        }
    }

    static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    static void call(Object target, String name) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }

    @Override
    public void close() {
        try {
            for (int i = resources.size() - 1; i >= 0; i--) resources.get(i).dispose();
            for (RecordingMusic track : tracks) {
                assertEquals(track.path, 1, track.disposeCalls);
                assertFalse(track.playing);
            }
        } finally {
            Gdx.app = oldApp;
            Gdx.audio = oldAudio;
            Gdx.files = oldFiles;
            Gdx.graphics = oldGraphics;
            Gdx.input = oldInput;
            Gdx.gl = oldGl;
            Gdx.gl20 = oldGl20;
            Gdx.gl30 = oldGl30;
        }
    }

    private static <T> T stub(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        return null;
    }

    private static final class ClasspathHandle extends FileHandle {
        ClasspathHandle(String path) {
            super(path, Files.FileType.Classpath);
        }
    }

    static final class RecordingMusic implements Music {
        final String path;
        float volume = Float.NaN;
        float volumeAtPlay = Float.NaN;
        int volumeCalls;
        int disposeCalls;
        boolean playing;
        boolean looping;
        private float position;
        private OnCompletionListener listener;

        RecordingMusic(String path) { this.path = path; }
        @Override public void play() { volumeAtPlay = volume; playing = true; }
        @Override public void pause() { playing = false; }
        @Override public void stop() { playing = false; }
        @Override public boolean isPlaying() { return playing; }
        @Override public void setLooping(boolean isLooping) { looping = isLooping; }
        @Override public boolean isLooping() { return looping; }
        @Override public void setVolume(float volume) { this.volume = volume; volumeCalls++; }
        @Override public float getVolume() { return volume; }
        @Override public void setPan(float pan, float volume) { setVolume(volume); }
        @Override public void setPosition(float position) { this.position = position; }
        @Override public float getPosition() { return position; }
        @Override public void dispose() { disposeCalls++; playing = false; }
        @Override public void setOnCompletionListener(OnCompletionListener listener) { this.listener = listener; }
        void complete() { playing = false; listener.onCompletion(this); }
    }
}
