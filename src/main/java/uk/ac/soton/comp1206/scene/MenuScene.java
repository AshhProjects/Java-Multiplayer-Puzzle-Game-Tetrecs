package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * This is used to keep track if the splash screen was already shown.
     * If the splash screen was shown at startup, it won't show again when you go back from another scene.
     */
    private static Boolean splashFinished = false;

    /**
     * The config file name to load the settings from.
     */
    public static final String CONFIG_FILE = "settings.properties";

    /**
     * The properties config object to load the config into.
     */
    public static Properties config;

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
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
        background.getStyleClass().add("menu-background");
        background.setVisible(true);

        //the filter above the background, below the filter
        var backgroundEffect = new StackPane();
        backgroundEffect.setMaxWidth(gameWindow.getWidth());
        backgroundEffect.setMaxHeight(gameWindow.getHeight());
        backgroundEffect.getStyleClass().add("game-background-effect2");
        backgroundEffect.setVisible(true);

        //the components layer, where all the clickable components will be
        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.setVisible(true);

        root.getChildren().addAll(background,backgroundEffect,menuPane);

        menuPane.getChildren().add(mainPane);

        /*Define a clip rectangle that matches the visible bounds of the menu pane
        This is to hide the buttons when they slide in.*/
        var clip = new Rectangle(gameWindow.getWidth(), gameWindow.getHeight());
        menuPane.setClip(clip);

        /*The layer on top of the background layer, to move the block pieces and title to the top
        //above the filter.*/
        String fullFilepath = Multimedia.class.getResource("/images/TetrECSMAINMENU2.png").toExternalForm();
        var titleImageView = new ImageView(fullFilepath);
        titleImageView.setFitWidth(gameWindow.getWidth());
        titleImageView.setPreserveRatio(true);
        titleImageView.setSmooth(true);
        menuPane.getChildren().add(titleImageView);

        //Creates all the buttons for the main menu.
        var singlePlayerButton = createMenuButton("Single Player", event -> startGame());
        var multiPlayerButton = createMenuButton("Multi Player", event -> startMultiplayer());
        var howToPlayButton = createMenuButton("How to Play", event -> howtoPlay());
        var settingsButton = createMenuButton("Settings", event -> settingsScene());
        var creditsButton = createMenuButton("Credits", event -> creditsScene());
        var exitButton = createMenuButton("Exit", event -> System.exit(0));

        var menuButtonBox = new VBox(singlePlayerButton,multiPlayerButton,howToPlayButton,settingsButton,creditsButton,exitButton);

        menuButtonBox.setAlignment(Pos.CENTER_LEFT);
        menuButtonBox.setPadding(new Insets(75,0,0,0));
        mainPane.setCenter(menuButtonBox);

        /*If the main menu is returned to from a different scene, splashFinished will already be finished
        and hence it will animate the buttons to show.*/
        if (splashFinished) {
            animateMenuButtons((menuButtonBox));
            Multimedia.StartBGMusic("menu.mp3");
        }

        //If this is the first time the game is started up, it will play a splash screen animation.
        if (!splashFinished) {

            //Hide the backgrounds.
            background.setVisible(false);
            backgroundEffect.setVisible(false);
            menuPane.setVisible(false);

            //create a new pane to add the splash screen onto.
            var splashPane = new StackPane();
            splashPane.setMaxHeight(gameWindow.getHeight());
            splashPane.setMaxWidth(gameWindow.getWidth());
            root.getChildren().add(splashPane);

            //Animates the splash screen.
            String splashFilepath = Multimedia.class.getResource("/images/ECSGames.png").toExternalForm();
            var splashLogo = new ImageView(splashFilepath);
            splashLogo.setOpacity(0.0);
            splashLogo.setFitWidth(gameWindow.getWidth()/4);
            splashLogo.setPreserveRatio(true);
            splashPane.getChildren().add(splashLogo);
            StackPane.setAlignment(splashLogo, Pos.CENTER);
            Multimedia.playSFX("intro.wav");

            // Scale the logo up
            var splashTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(splashLogo.scaleXProperty(), 1.0), new KeyValue(splashLogo.scaleYProperty(), 1.0)),
                    new KeyFrame(Duration.seconds(3.6), new KeyValue(splashLogo.scaleXProperty(), 1.5), new KeyValue(splashLogo.scaleYProperty(), 1.5)),
                    new KeyFrame(Duration.seconds(3), new KeyValue(splashLogo.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.seconds(3.6), new KeyValue(splashLogo.opacityProperty(), 0.0))
            );
            splashTimeline.play();

            // Show the menu after the splash screen animation finishes
            splashTimeline.setOnFinished(event -> {
                splashPane.setVisible(false);
                splashFinished = true;

                //Adds a little delay before showing the buttons, so you can see the buttons appear
                PauseTransition delay = new PauseTransition(Duration.seconds(0.2));
                delay.setOnFinished(e -> animateMenuButtons(menuButtonBox));
                delay.play();
                splashPane.setVisible(false);
                background.setVisible(true);
                backgroundEffect.setVisible(true);
                menuPane.setVisible(true);
                backgroundEffect.setOpacity(0.0);
                background.setOpacity(0.0);
                menuPane.setOpacity(0.0);
                var fadeIn = new FadeTransition(Duration.seconds(1), menuPane);
                var fadeIn2 = new FadeTransition(Duration.seconds(1), backgroundEffect);
                var fadeIn3 = new FadeTransition(Duration.seconds(1), background);
                fadeIn.setToValue(1.0);
                fadeIn2.setToValue(1.0);
                fadeIn3.setToValue(1.0);
                fadeIn.play();
                fadeIn2.play();
                fadeIn3.play();
                Multimedia.StartBGMusic("menu.mp3");
            });
        }

    }

    private void animateMenuButtons(VBox buttons) {

        // Calculate the delay for each button animation

        Duration delay = Duration.ZERO;
        Duration delayIncrement = Duration.seconds(0.05);

        // Animate each button to come in from left
        for(Node button : buttons.getChildren()) {
            if (button instanceof Button) {
                TranslateTransition buttonTransition = new TranslateTransition(Duration.seconds(0.2), button);
                buttonTransition.setToX(0);
                buttonTransition.setDelay(delay);
                buttonTransition.play();

                delay = delay.add(delayIncrement);
            }

        }
    }


    /**
     * Initialise the menu as well as loading the config file
     */
    @Override
    public void initialise() {
        config = new Properties();
        try {
            FileInputStream input = new FileInputStream(CONFIG_FILE);
            config.load(input);
            input.close();
        } catch (IOException e) {
            // If the file doesn't exist, create it
            logger.warn("Could not load config file, creating new file");
        }
        Multimedia.initialiseVolume();
    }

    /**
     * Handle when the Start Game button is pressed
     */
    private void startGame() {
        gameWindow.startChallenge();
    }

    /**
     * Handle when the multiplayer button is pressed
     */
    private void startMultiplayer() {
        gameWindow.startMultiplayerLobby();
    }

    /**
     * Handle when the Start Game button is pressed
     */
    private void howtoPlay() {
        gameWindow.howToPlay();
    }

    /**
     * Handle when the credits button is pressed
     */
    private void creditsScene() {
        gameWindow.startCredits();
    }

    /**
     * Handle when the credits button is pressed
     */
    private void settingsScene() {
        gameWindow.startSettings();
    }

    /**
     * Create a menu button with the given text and event handler
     * @param text the text to display on the button
     * @param handler the event handler to handle button clicks
     * @return a new menu button
     */
    private Button createMenuButton(String text, EventHandler<MouseEvent> handler) {
        var menuButton = new Button(text);
        menuButton.getStyleClass().add("menuItem");
        menuButton.setOnMouseClicked(event ->
        {
            Multimedia.playSFX("select.wav");
            handler.handle(event);
        });
        menuButton.setTranslateX(-300);

        menuButton.setOnMouseEntered(event -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.2), menuButton);
            scaleTransition.setFromX(1);
            scaleTransition.setFromY(1);
            scaleTransition.setToX(1.2);
            scaleTransition.setToY(1.2);

            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.2), menuButton);
            translateTransition.setFromX(0);
            translateTransition.setToX(menuButton.getWidth() * 0.1);

            ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, translateTransition);
            parallelTransition.play();
        });

        menuButton.setOnMouseExited(event -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.2), menuButton);
            scaleTransition.setToX(1);
            scaleTransition.setToY(1);

            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.2), menuButton);
            translateTransition.setToX(0);
            translateTransition.setToY(0);

            ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, translateTransition);
            parallelTransition.play();
        });

        return menuButton;
    }

}
