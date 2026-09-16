package uk.ac.soton.comp1206;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static uk.ac.soton.comp1206.scene.MenuScene.config;


/**
 * This is used to handle the audio of the game. Plays the background music and sound effects.
 */
public class Multimedia {

    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    /**
     * audioPlayer is generally used for the sound effects.
     */
    private static MediaPlayer audioPlayer;

    /**
     * musicPlayer is generally used for the background music.
     */
    private static MediaPlayer musicPlayer;

    /**
     * The current song that's playing.
     */
    private static String musicPlaying = "";

    /**
     * The sound effect volume. Initial value: 1.0
     */
    private static double sfxVolume = 1.0;

    /**
     * The music volume. Initial value: 1.0
     */
    private static double musicVolume = 1.0;

    /**
     * Used to play a sound effect
     * @param file the file to play. Located in /sounds/... The file format must be specified.
     */
    public static void playSFX(String file) {
        try {
            //sets the full file path to the file in /sounds/ folder.
            String fullFilepath = Multimedia.class.getResource( "/sounds/"+file).toExternalForm();
            var play = new javafx.scene.media.Media(fullFilepath);

            //creates a new audioPlayer to play this sound.
            audioPlayer = new MediaPlayer(play);

            //sets the volume of this new audioPlayer.
            audioPlayer.setVolume(sfxVolume);

            //plays this audio player.
            audioPlayer.play();
            logger.info("Playing {}",file);
        } catch (Exception e) {
            logger.error("Error playing sound effect {}",file);
            logger.error(e.getMessage());
        }
    }

    /**
     * Used to play a background song.
     * @param file the file to play. Located in /music/... The file format must be specified.
     */
    public static void StartBGMusic(String file) {
        if (!musicPlaying.equals(file)) {
            Multimedia.stopBGMusic();
            try {
                //sets the full file path to the file in /music/ folder.
                String fullFilepath = Multimedia.class.getResource("/music/" + file).toExternalForm();
                var play = new javafx.scene.media.Media(fullFilepath);

                //creates a new musicPlayer to play this music.
                musicPlaying = file;
                musicPlayer = new MediaPlayer(play);

                //sets the volume of this new musicPlayer.
                musicPlayer.setVolume(musicVolume);

                //loops the music.
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);

                //plays the music.
                musicPlayer.play();
                logger.info("Playing {}", file);
            } catch (Exception e) {
                logger.error("Error playing song {}", file);
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * Stops the background music.
     */
    public static void stopBGMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
        musicPlaying = "";
    }

    /**
     * Stops all the sound effects playing.
     */
    public static void stopSFX() {
        if (audioPlayer != null) {
            audioPlayer.stop();
        }
    }

    /**
     *
     * @param volume the volume to set the sound effects to. Value must be a double between 0.0 to 1.0.
     */
    public static void setVolumeSFX(double volume) {
        //sets the field variable of this class to the volume that's given in.
        try {
            sfxVolume = volume;
            audioPlayer.setVolume(volume);
        } catch (Exception e) {
            logger.error("Error while trying to set SFX volume!");
            logger.error(e.getMessage());
        }
    }

    /**
     *
     * @param volume the volume to set the background music to. Value must be a double between 0.0 to 1.0.
     */
    public static void setVolumeBGMusic(double volume) {
        //sets the field variable of this class to the volume that's given in.
        try {
            musicVolume = volume;
            musicPlayer.setVolume(volume);
        } catch (Exception e) {
            logger.error("Error while trying to set music volume!");
            logger.error(e.getMessage());
        }
    }

    /**
     *
     * @return returns the sound effect volume amount.
     */
    public static double getSFXVolume() {
        return sfxVolume;
    }

    /**
     *
     * @return returns the background music volume amount.
     */
    public static double getMusicVolume() {
        return musicVolume;
    }

    /**
     * Sets the initial volume as loaded from the config file.
     * If no config file, sets it to 0.5 default.
     */
    public static void initialiseVolume() {
        // Set the initial values for the sliders from the config file
        double sfxVolume = Double.parseDouble(config.getProperty("sfxVolume", "0.5"));
        double musicVolume = Double.parseDouble(config.getProperty("musicVolume", "0.5"));
        setVolumeSFX(sfxVolume);
        setVolumeBGMusic(musicVolume);
    }
}
