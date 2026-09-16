package uk.ac.soton.comp1206.scene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The instruction scene, used to show how to play the game.
 */
public class InstructionsScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    /**
     * Create a new instructions scene
     *
     * @param gameWindow the Game Window this will be displayed in
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating instructions Scene");
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

        //the components layer, where the instructions components will be
        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.setVisible(true);

        root.getChildren().addAll(background,backgroundEffect,menuPane);

        menuPane.getChildren().add(mainPane);

        // Add an image to show the instructions image
        var instructionsImage = new ImageView(Multimedia.class.getResource("/images/how-to-BG.png").toExternalForm());
        instructionsImage.setFitWidth(gameWindow.getWidth());
        instructionsImage.setPreserveRatio(true);
        mainPane.getChildren().add(instructionsImage);

        /*Dynamically sets up the pieces on the how to play screen.
        Creates two different HBoxes, one for the top row and one for the bottom.
        This is so it can be centerly aligned.*/
        var piecesTopRow = new HBox();
        piecesTopRow.setAlignment(Pos.CENTER);
        piecesTopRow.setSpacing(20);

        for (int i = 0; i < 8; i++) {
            var pieceBoard = new PieceBoard(new Grid(3, 3), 60, 60);
            pieceBoard.setPiece(GamePiece.createPiece(i));
            piecesTopRow.getChildren().add(pieceBoard);
        }

        var piecesBottomRow = new HBox();
        piecesBottomRow.setAlignment(Pos.CENTER);
        piecesBottomRow.setSpacing(20);

        for (int i = 8; i < 15; i++) {
            var pieceBoard = new PieceBoard(new Grid(3, 3), 60, 60);
            pieceBoard.setPiece(GamePiece.createPiece(i));
            piecesBottomRow.getChildren().add(pieceBoard);
        }

        var vbox = new VBox(piecesTopRow,piecesBottomRow);
        vbox.setSpacing(20);
        vbox.setAlignment(Pos.BOTTOM_CENTER);
        vbox.setPadding(new Insets(20,20,100,20));

        mainPane.setCenter(vbox);
    }
}

