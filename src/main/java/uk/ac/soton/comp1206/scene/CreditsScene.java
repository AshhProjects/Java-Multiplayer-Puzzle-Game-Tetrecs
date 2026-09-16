package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The credits scene, used to display the credits for the assets and game.
 */
public class CreditsScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(CreditsScene.class);

    /**
     * The actual credits that will be displayed.
     */
    private static final String CREDITS_TEXT = """
		Credits:


		Music:


		game music:

		Sonic 3: Ice Cap Zone Act 1


		lobby/score music:

		Qwerty Enchanted The House
		and Now It's Attacking Us


		main menu music:

		Cult of the Lamb - Start a Cult



		Quality Assurance Analyst:

		me



		Project Manager:

		me



		Game Design:

		me



		Programming:

		me (+ use of template provided by ECS)



		Art:

		me



		Sound Design:

		me (with use of JSFXR)
		
		+ splash screen from Pizza Tower
		
		+ 3..2..1.. from
		Looting the Louvre - Naofumi Hataya
		and Mario Kart



		Testing:

		me



		Bug Fixing:

		me



		Nap taking:

		me



		Watching YouTube Videos:

		me



		Special Thanks to:

		me



		Extra special Thanks to:

		me



		Thanks for playing!


		me (thank you myself for playing)



		""";


    /**
     * The animation to play the credits scroll.
     */
    private Timeline timeline;

    /**
     * Create a new credits scene
     *
     * @param gameWindow the Game Window this will be displayed in
     */

    public CreditsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Credits Scene");
    }

    /**
     * Initial set up of the scene.
     * This is used to detect if the user presses escape or backspace, to go back to the menu.
     */
    @Override
    public void initialise() {
        gameWindow.getScene().setOnKeyPressed((e) -> {
            KeyCode key = e.getCode();
            if (key == KeyCode.ESCAPE || key == KeyCode.BACK_SPACE) {
                timeline.stop();
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

        //the components layer, where the credits scroll will be
        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.setVisible(true);

        root.getChildren().addAll(background,backgroundEffect,menuPane);

        var creditsPane = new VBox();
        creditsPane.setAlignment(Pos.TOP_CENTER);
        creditsPane.setSpacing(20);

        var creditsText = new Text(CREDITS_TEXT);
        creditsText.getStyleClass().add("credits");
        creditsText.setFill(Color.WHITE);
        creditsText.setTextAlignment(TextAlignment.CENTER);
        creditsPane.getChildren().add(creditsText);

        menuPane.getChildren().add(creditsPane);

        // Animation to scroll credits from bottom to top
        KeyValue startValue = new KeyValue(creditsPane.translateYProperty(), gameWindow.getHeight());
        KeyValue endValue = new KeyValue(creditsPane.translateYProperty(), -3450);
        KeyFrame startFrame = new KeyFrame(Duration.ZERO, startValue);
        KeyFrame endFrame = new KeyFrame(Duration.seconds(20), endValue);
        timeline = new Timeline(startFrame, endFrame);

        timeline.setOnFinished(event -> {
            gameWindow.startMenu();
        });

        timeline.play();
    }
}
