package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.FileOutputStream;
import java.io.IOException;

import static uk.ac.soton.comp1206.scene.MenuScene.CONFIG_FILE;
import static uk.ac.soton.comp1206.scene.MenuScene.config;

/**
 * The settings scene, used to display the volume sliders and lets the user adjusts the volume for both the
 * sound effects and the background music.
 */
public class settingsScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(settingsScene.class);

    /**
     * Create a new settings scene
     *
     * @param gameWindow the Game Window this will be displayed in
     */

    public settingsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Credits Scene");
    }

    /**
     * Save the current volume settings to the config file
     */
    private void saveConfig() {
        //Sets the config to the value of the sfx volume and music volume.
        config.setProperty("sfxVolume", String.valueOf(Multimedia.getSFXVolume()));
        config.setProperty("musicVolume", String.valueOf(Multimedia.getMusicVolume()));

        //saves the config to a local file. CONFIG_FIlE is the filename of the config.
        try {
            FileOutputStream output = new FileOutputStream(CONFIG_FILE);
            config.store(output, null);
            output.close();
        } catch (IOException e) {
            logger.error("Could not save config file; ", e);
        }
    }

    /**
     * This is used to detect if the user presses escape or backspace, to go back to the menu.
     */
    @Override
    public void initialise() {
        gameWindow.getScene().setOnKeyPressed((e) -> {
            KeyCode key = e.getCode();
            if (key == KeyCode.ESCAPE) {
                saveConfig();
                gameWindow.startMenu();
            }
        });
    }

    /**
     * Build the instructions layout
     */
    @Override
    public void build() {

        logger.info("Building " + this.getClass().getName());


        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var mainPane = new BorderPane();

        //the actual background image layer
        var background = new StackPane();
        background.setMaxWidth(gameWindow.getWidth());
        background.setMaxHeight(gameWindow.getHeight());
        background.getStyleClass().add("empty-screen");
        background.setVisible(true);

        //the filter above the background, below the filter
        var backgroundEffect = new StackPane();
        backgroundEffect.setMaxWidth(gameWindow.getWidth());
        backgroundEffect.setMaxHeight(gameWindow.getHeight());
        backgroundEffect.getStyleClass().add("game-background-effect2");
        backgroundEffect.setVisible(true);

        //the components layer, where all the settings sliders will be
        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.setVisible(true);


        root.getChildren().addAll(background,backgroundEffect,menuPane);

        menuPane.getChildren().add(mainPane);

        // Sound Volume Slider
        var soundLabel = new Text("Sound Volume");
        soundLabel.getStyleClass().add("heading");
        var soundSlider = new Slider(0, 1, Multimedia.getSFXVolume());
        soundSlider.getStyleClass().add("sound-slider");
        soundSlider.setMaxWidth(250);
        VBox soundSliders = new VBox(soundLabel,soundSlider);
        soundSliders.setSpacing(10);

        /*Adds a listener to the sound slider, to detect if it was changed, and sets the
        sound effect volume appropriately.*/
        soundSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // set the volume of the audio player for sound effects
            Multimedia.setVolumeSFX(newValue.doubleValue());
        });

        //Music Volume Slider
        var musicLabel = new Text("Music Volume");
        musicLabel.getStyleClass().add("heading");
        var musicSlider = new Slider(0, 1, Multimedia.getMusicVolume());
        musicSlider.getStyleClass().add("sound-slider");
        musicSlider.setMaxWidth(250);
        VBox musicSliders = new VBox(musicLabel,musicSlider);
        musicSliders.setSpacing(10);

        /*Adds a listener to the music slider, to detect if it was changed, and sets the
        music volume appropriately.*/
        musicSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // set the volume of the audio player for background music
            Multimedia.setVolumeBGMusic(newValue.doubleValue());
        });

        VBox sliders = new VBox(soundSliders,musicSliders);
        sliders.setSpacing(30);
        sliders.setAlignment(Pos.CENTER_LEFT);
        sliders.setPadding(new Insets(100,50,50,100));

        mainPane.setTop(sliders);


    }
}
