package com.deadbeat.audio;

import com.badlogic.gdx.Input;
import com.deadbeat.DeadbeatGame;
import com.deadbeat.audio.AudioTestEnvironment.RecordingMusic;
import com.deadbeat.progression.UpgradeSelection;
import com.deadbeat.screens.GameScreen;
import com.deadbeat.screens.MainMenuScreen;
import com.deadbeat.world.GameWorld;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.deadbeat.audio.AudioTestEnvironment.*;
import static org.junit.Assert.*;

public class AudioIntegrationTest {
    private final AudioSettings settings = AudioSettings.getInstance();
    private float oldVolume;
    private boolean oldMuted;
    private AudioTestEnvironment environment;

    @Before
    public void setUp() {
        oldVolume = settings.getVolume();
        oldMuted = settings.isMuted();
        settings.setVolume(.65f);
        settings.setMuted(false);
        environment = new AudioTestEnvironment();
    }

    @After
    public void tearDown() {
        try {
            if (environment != null) environment.close();
        } finally {
            settings.setVolume(oldVolume);
            settings.setMuted(oldMuted);
        }
    }

    private MainMenuScreen menu(DeadbeatGame game) {
        MainMenuScreen menu = environment.own(new MainMenuScreen(game));
        menu.resize(960, 540);
        return menu;
    }

    private GameScreen screen() {
        GameScreen screen = environment.own(new GameScreen());
        screen.resize(960, 540);
        return screen;
    }

    private SoundtrackPlayer soundtrack(GameScreen screen) throws Exception {
        return (SoundtrackPlayer) field(screen, "soundtrack");
    }

    private RecordingMusic music(SoundtrackPlayer player) throws Exception {
        return (RecordingMusic) field(player, "current");
    }

    private RecordingMusic theme(MainMenuScreen menu) throws Exception {
        return (RecordingMusic) field(menu, "menuTheme");
    }

    @Test
    public void recreatedMenusAndDefaultGameScreenPreserveSharedPreferencesBeforePlay() throws Exception {
        settings.setVolume(.23f);
        settings.setMuted(true);
        MainMenuScreen first = menu(new DeadbeatGame());
        assertEquals(0f, theme(first).volumeAtPlay, 0f);
        environment.release(first);
        GameScreen game = screen();
        assertEquals(.23f, soundtrack(game).getVolume(), 0f);
        assertTrue(soundtrack(game).isMuted());
        assertEquals(0f, music(soundtrack(game)).volumeAtPlay, 0f);
        soundtrack(game).setVolume(.37f);
        soundtrack(game).toggleMute();
        environment.release(game);
        MainMenuScreen second = menu(new DeadbeatGame());
        assertEquals(.37f, theme(second).volumeAtPlay, 0f);
        assertEquals(.37f, settings.getVolume(), 0f);
        assertFalse(settings.isMuted());
    }

    @Test
    public void menuControlsShareVolumeAndMuteWithTheStartedGame() throws Exception {
        DeadbeatGame game = new DeadbeatGame();
        MainMenuScreen menu = menu(game);
        environment.frame(menu, Input.Keys.LEFT);
        assertEquals(.6f, settings.getVolume(), .0001f);
        assertEquals(.6f, theme(menu).volume, .0001f);
        environment.frame(menu, Input.Keys.M);
        assertTrue(settings.isMuted());
        assertEquals(0f, theme(menu).volume, 0f);
        environment.frame(menu, Input.Keys.ENTER);
        GameScreen screen = environment.own((GameScreen) game.getScreen());
        assertTrue(themeDisposed(menu));
        assertEquals(.6f, soundtrack(screen).getVolume(), .0001f);
        assertTrue(soundtrack(screen).isMuted());
        assertEquals(0f, music(soundtrack(screen)).volumeAtPlay, 0f);
        soundtrack(screen).toggleMute();
        assertEquals(.6f, music(soundtrack(screen)).volume, .0001f);
    }

    private boolean themeDisposed(MainMenuScreen menu) throws Exception {
        return field(menu, "menuTheme") == null && environment.tracks.get(0).disposeCalls == 1;
    }

    @Test
    public void explicitGameInitializationAssignsMuteWithoutInvertingIt() throws Exception {
        for (boolean muted : new boolean[] {true, true, false, false, true}) {
            GameScreen game = environment.own(new GameScreen(.42f, muted));
            RecordingMusic music = music(soundtrack(game));
            assertEquals(.42f, settings.getVolume(), 0f);
            assertEquals(muted, settings.isMuted());
            assertEquals(muted ? 0f : .42f, music.volumeAtPlay, 0f);
            assertEquals(1, music.volumeCalls);
            environment.release(game);
        }
    }

    @Test
    public void initiallyMutedGameNeverStartsItsTrackWithAudibleVolume() throws Exception {
        settings.setVolume(.23f);
        GameScreen game = environment.own(new GameScreen(.42f, true));
        assertEquals(0f, music(soundtrack(game)).volumeAtPlay, 0f);
        assertEquals(.42f, settings.getVolume(), 0f);
    }

    @Test
    public void explicitVolumeIsAppliedBeforePlayAndInvalidVolumeKeepsThePreference() throws Exception {
        GameScreen first = environment.own(new GameScreen(.28f, false));
        assertEquals(.28f, music(soundtrack(first)).volumeAtPlay, 0f);
        environment.release(first);
        GameScreen second = environment.own(new GameScreen(Float.NaN, false));
        assertEquals(.28f, music(soundtrack(second)).volumeAtPlay, 0f);
    }

    @Test
    public void activeTrackSynchronizesExternalChangesWithoutRedundantVolumeCalls() throws Exception {
        SoundtrackPlayer player = environment.own(new SoundtrackPlayer(new Random(7)));
        RecordingMusic current = music(player);
        assertEquals(1, current.volumeCalls);
        for (int i = 0; i < 10; i++) player.update();
        assertEquals(1, current.volumeCalls);
        settings.setVolume(.31f);
        player.update();
        assertEquals(.31f, current.volume, 0f);
        assertEquals(2, current.volumeCalls);
        settings.setMuted(true);
        player.update();
        assertEquals(0f, current.volume, 0f);
        settings.setVolume(.12f);
        player.update();
        assertEquals(3, current.volumeCalls);
        settings.setMuted(false);
        player.update();
        assertEquals(.12f, current.volume, 0f);
        assertEquals(4, current.volumeCalls);
        player.setVolume(.12f);
        assertEquals(4, current.volumeCalls);
    }

    @Test
    public void menuSynchronizesExternalChangesAndKeepsSliderAndSpeakerControls() throws Exception {
        MainMenuScreen menu = menu(new DeadbeatGame());
        RecordingMusic theme = theme(menu);
        environment.frame(menu);
        assertEquals(1, theme.volumeCalls);
        settings.setVolume(.21f);
        environment.frame(menu);
        assertEquals(.21f, theme.volume, 0f);
        settings.setMuted(true);
        environment.frame(menu);
        assertEquals(0f, theme.volume, 0f);
        environment.clicked = true;
        environment.mouseX = 494;
        environment.mouseY = 438;
        environment.frame(menu);
        assertEquals(.5f, settings.getVolume(), .0001f);
        assertFalse(settings.isMuted());
        assertEquals(.5f, theme.volume, .0001f);
        environment.clicked = true;
        environment.mouseX = 339;
        environment.mouseY = 438;
        environment.frame(menu);
        assertTrue(settings.isMuted());
        assertEquals(0f, theme.volume, 0f);
    }

    @Test
    public void pauseControlsPreserveMuteWhileChangingVolumeAndRestoreItOnUnmute() throws Exception {
        GameScreen game = screen();
        RecordingMusic track = music(soundtrack(game));
        environment.frame(game, Input.Keys.ESCAPE);
        assertEquals(true, field(game, "paused"));
        environment.frame(game, Input.Keys.M);
        environment.frame(game, Input.Keys.RIGHT);
        assertTrue(settings.isMuted());
        assertEquals(.7f, settings.getVolume(), .0001f);
        assertEquals(0f, track.volume, 0f);
        environment.frame(game, Input.Keys.M);
        assertFalse(settings.isMuted());
        assertEquals(.7f, track.volume, .0001f);
        environment.clicked = true;
        environment.mouseX = 468;
        environment.mouseY = 288;
        environment.frame(game);
        assertEquals((468f - 394f) / 233f, settings.getVolume(), .0001f);
        environment.clicked = true;
        environment.mouseX = 339;
        environment.mouseY = 293;
        environment.frame(game);
        assertTrue(settings.isMuted());
        assertEquals(0f, track.volume, 0f);
        environment.frame(game, Input.Keys.ENTER);
        assertEquals(false, field(game, "paused"));
    }

    @Test
    public void repeatedRestartsAndRecreationDoNotChangePreferencesOrReplayTheTrack() throws Exception {
        settings.setVolume(.31f);
        settings.setMuted(true);
        GameScreen game = screen();
        RecordingMusic track = music(soundtrack(game));
        for (int i = 0; i < 10; i++) environment.frame(game, Input.Keys.R);
        assertSame(track, music(soundtrack(game)));
        assertEquals(.31f, settings.getVolume(), 0f);
        assertTrue(settings.isMuted());
        assertEquals(1, track.volumeCalls);
        environment.release(game);
        GameScreen recreated = screen();
        assertEquals(.31f, settings.getVolume(), 0f);
        assertTrue(settings.isMuted());
        assertEquals(0f, music(soundtrack(recreated)).volumeAtPlay, 0f);
    }

    @Test
    public void musicNavigationAndAutomaticCompletionUseCurrentPreferences() throws Exception {
        GameScreen game = screen();
        SoundtrackPlayer player = soundtrack(game);
        int firstNumber = player.getTrackNumber();
        RecordingMusic first = music(player);
        settings.setVolume(.24f);
        environment.frame(game, Input.Keys.E);
        int secondNumber = player.getTrackNumber();
        assertNotEquals(firstNumber, secondNumber);
        assertEquals(1, first.disposeCalls);
        assertEquals(.24f, music(player).volumeAtPlay, 0f);
        environment.frame(game, Input.Keys.Q);
        assertEquals(firstNumber, player.getTrackNumber());
        environment.frame(game, Input.Keys.E);
        assertEquals(secondNumber, player.getTrackNumber());
        settings.setMuted(true);
        RecordingMusic completed = music(player);
        completed.complete();
        environment.frame(game);
        assertNotSame(completed, music(player));
        assertEquals(1, completed.disposeCalls);
        assertEquals(0f, music(player).volumeAtPlay, 0f);
        assertEquals(1, music(player).volumeCalls);
    }

    @Test
    public void audioStillSynchronizesDuringUpgradeSelectionPauseDefeatAndVictory() throws Exception {
        GameScreen game = screen();
        GameWorld world = (GameWorld) field(game, "world");
        RecordingMusic track = music(soundtrack(game));
        UpgradeSelection upgrades = (UpgradeSelection) field(world, "upgrades");
        upgrades.award(2);
        environment.frame(game, Input.Keys.ESCAPE);
        settings.setVolume(.2f);
        environment.frame(game);
        assertEquals(.2f, track.volume, 0f);
        assertEquals(2, world.getPendingUpgrades());
        environment.frame(game, Input.Keys.ESCAPE);
        environment.frame(game, Input.Keys.NUM_1);
        assertEquals(1, world.getPendingUpgrades());
        settings.setMuted(true);
        environment.frame(game);
        assertEquals(0f, track.volume, 0f);
        world.getPlayer().takeDamage(world.getPlayer().getMaxHealth());
        environment.frame(game);
        assertEquals("DEFEAT", field(game, "outcome").toString());
        assertFalse(world.isChoosingUpgrade());
        settings.setMuted(false);
        settings.setVolume(.4f);
        environment.frame(game);
        assertEquals(.4f, track.volume, 0f);
        environment.frame(game, Input.Keys.R);
        java.lang.reflect.Field finished = GameWorld.class.getDeclaredField("wavesFinished");
        finished.setAccessible(true);
        finished.setBoolean(world, true);
        environment.frame(game);
        assertEquals("VICTORY", field(game, "outcome").toString());
        settings.setMuted(true);
        environment.frame(game);
        assertEquals(0f, track.volume, 0f);
        environment.frame(game, Input.Keys.R);
        assertEquals("PLAYING", field(game, "outcome").toString());
        assertEquals(.4f, settings.getVolume(), 0f);
        assertTrue(settings.isMuted());
        assertSame(track, music(soundtrack(game)));
    }

    @Test
    public void boundariesZeroAndNonFiniteValuesReachMusicSafely() throws Exception {
        SoundtrackPlayer player = environment.own(new SoundtrackPlayer(new Random(7)));
        RecordingMusic track = music(player);
        player.setVolume(-1f);
        assertEquals(0f, track.volume, 0f);
        player.toggleMute();
        player.toggleMute();
        assertEquals(0f, track.volume, 0f);
        player.setVolume(2f);
        assertEquals(1f, track.volume, 0f);
        for (float invalid : new float[] {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            player.setVolume(invalid);
            assertEquals(1f, settings.getVolume(), 0f);
            assertEquals(1f, track.volume, 0f);
        }
    }

    @Test
    public void disposalIsIdempotentAndPreservesTheSharedPreferences() throws Exception {
        settings.setVolume(.34f);
        settings.setMuted(true);
        SoundtrackPlayer player = environment.own(new SoundtrackPlayer(new Random(7)));
        RecordingMusic track = music(player);
        player.dispose();
        player.dispose();
        settings.setMuted(false);
        player.update();
        player.nextTrack();
        assertEquals(1, track.disposeCalls);
        assertEquals(.34f, settings.getVolume(), 0f);
        assertEquals(0f, track.volume, 0f);
        assertEquals(1, track.volumeCalls);
        assertEquals(0, player.getTrackNumber());
    }

    @Test
    public void menuExitControlStillRequestsApplicationExit() {
        MainMenuScreen menu = menu(new DeadbeatGame());
        environment.frame(menu, Input.Keys.ESCAPE);
        assertEquals(1, environment.exitRequests);
    }
}
