package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);

    /**
     * The block that is currently being hovered over by the mouse/keyboard.
     */
    private GameBlock selectedBlock;

    /**
     * The actual game that this UI class is representing.
     */
    protected Game game;

    /**
     * The X coordinate of the block that is currently selected / hovered over by the mouse.
     */
    private int hoverX;

    /**
     * The Y coordinate of the block that is currently selected / hovered over by the mouse.
     */
    private int hoverY;

    /**
     * The visual board of the game's grid.
     */
    protected GameBoard board;

    /**
     * The integer value of the local high score from the text file.
     */
    private int highScore;

    /**
     * Boolean used to tell if the game has started.
     */
    private boolean gameStarted = false;

    /**
     * Used to tell if the counter at the start of the game is enabled or not.
     */
    protected boolean counterEnabled;

    /**
     * The animation for the counter at the start of the game. Field variable so it can be stopped in the leaveGame method.
     */
    private Timeline counterTimeline;

    /**
     * The animation for the bottom timer in the game. Field variable so it can be stopped in the leaveGame method.
     */
    private Timeline timerTimeline;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * This is used to set whether the counter will display at the start or not.
     */
    protected void setCounter() {
        counterEnabled = true;
    }

    /**
     *
     * @param background The style class (from the css file) to load to the stack pane.
     * @return StackPane that is the size of the game window.
     */
    protected StackPane background(String background) {
        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());

        //sets the style of the stackPane to the one that is given in as a parameter.
        challengePane.getStyleClass().add(background);
        return challengePane;
    }

    /**
     * returns the default stack pane
     * @return StackPane that is the size of the game window. Default style is "challenge-background".
     */
    protected StackPane background() {
	  return background("challenge-background");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        //creates a new game object.
        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        /*Sets up the background (made of 3 layers).
        Bottom most layer is the actual background image.
        Second layer is the filter between the background and the components at the top.
        The top layer is for the components, hence it has no style required.*/
        var background  = background();
        var backgroundEffect = background("game-background-effect");
        var challengePane = background("");

        //The mainPane contains the board and the right menu
        var mainPane = new BorderPane();

        /*the "topComponents" are the components at the top of the board, e.g. the title and the
        corner elements (score and lives). This is structured like this for overlapping, otherwise
        the board will be positioned too low since the top left corner element (score+level) is
        long vertically which pushes the board down.*/
        var topComponents = new BorderPane();

        challengePane.getChildren().addAll(topComponents,mainPane);

        /*This layer is for the 3..2..1 counter at the start of the game. Everything under it gets
        blurred, but the counter shouldn't, hence it's on a new BorderPane layer.*/
        var overlayCounter = new BorderPane();
        overlayCounter.setMaxWidth(gameWindow.getWidth());
        overlayCounter.setMaxHeight(gameWindow.getHeight());
        root.getChildren().addAll(background,backgroundEffect,challengePane,overlayCounter);

        //Returns the VBox of the title.
        VBox titleBox = setupTitle();

        /*Returns the VBox of the score + level for the top left corner.
        Contains the score and level, binds them to the game, as well as the labels for each of them.*/
        VBox scoreBox  = setupScore();

        /*Returns the VBox of the lives for the top right corner.
        Contains the lives counter, binds them to the game, as well as puts a label on it.*/
        VBox livesBox = setupLives();

        // create HBox for top left corner
        HBox topLeftBox = new HBox(scoreBox);
        topLeftBox.setAlignment(Pos.BASELINE_LEFT);
        topLeftBox.setPadding(new Insets(20,0,0,20));
        HBox.setHgrow(topLeftBox,Priority.ALWAYS);

        // create HBox for top right corner
        HBox topRightBox = new HBox(livesBox);
        topRightBox.setAlignment(Pos.BASELINE_RIGHT);
        topRightBox.setPadding(new Insets(20,20,0,0));
        HBox.setHgrow(topRightBox,Priority.ALWAYS);

        //The top HBox containing the corner elements and the title.
        HBox topSide = new HBox(topLeftBox,titleBox,topRightBox);

        /*this returns a VBox of the main game board.
        setupBoard() gets overridden in multiplayerScene as to attach the messaging components
        to the VBox board. This prevents the need to duplicate code.*/
        VBox boardBox = setupBoard();

        PieceBoard pieceBoard = new PieceBoard(game.getNextPieceGrid(),gameWindow.getWidth()/6,gameWindow.getWidth()/6);
        PieceBoard followingPieceBoard = new PieceBoard(game.getFollowingPieceGrid(),gameWindow.getWidth()/8,gameWindow.getWidth()/8);

        VBox smallBoards = new VBox(pieceBoard,followingPieceBoard);
        smallBoards.setAlignment(Pos.CENTER);
        smallBoards.setSpacing(30);

        //Sets up the right side menu, parses in the smallBoards as they're created here in this method.
        VBox rightSide = setupRightSide(smallBoards);
        rightSide.setPadding(new Insets(80,10,0,0));

        topComponents.setTop(topSide);
        mainPane.setRight(rightSide);
        mainPane.setCenter(boardBox);

        /*The setup for the listeners have been moved to a different method as to prevent the need
        for duplicate code here and multiplayerScene.*/
        setupListenersHandlers(pieceBoard,followingPieceBoard);

        //Sets up the timer for the bottom of the screen.
        Rectangle timerBar = setupTimerBar();
        HBox timerBox = new HBox(timerBar);
        timerBox.setPadding(new Insets(10));

        /*This is set on top of the board game layer (in the topComponents layer) as this prevents
        the game UI from moving when changed in the multiplayerScene.*/
        topComponents.setBottom(timerBox);

        //The blur effect for the start of the game, when the counter is running.
        GaussianBlur blurEffect = new GaussianBlur(15);
        challengePane.setEffect(blurEffect);

        Text counter = new Text("3");
        counter.getStyleClass().add("counter");
        counter.setFill(Color.GREEN);
        overlayCounter.setCenter(counter);
        counter.setOpacity(0.0);

        //Reduces the blur from 15 to 0 when the counter finishes. As an animation so it's smooth.
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(blurEffect.radiusProperty(), 15.0)),
                new KeyFrame(Duration.seconds(0.2), new KeyValue(blurEffect.radiusProperty(), 0.0))
        );

        // Scale the counter up animation.
        counterTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(counter.scaleXProperty(), 1.0), new KeyValue(counter.scaleYProperty(), 1.0)),
                new KeyFrame(Duration.seconds(1), new KeyValue(counter.scaleXProperty(), 1.5), new KeyValue(counter.scaleYProperty(), 1.5)),
                new KeyFrame(Duration.seconds(0.7), new KeyValue(counter.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(1), new KeyValue(counter.opacityProperty(), 0.0))
        );

        /*this sets the counter to true. This is its own method, as it gets overridden in the
        multiplayerScene class if the game is a multiplayer game, which then default-turns the
        counter off. This prevents code duplication.*/
        setCounter();

        if (counterEnabled) {
            logger.info("Counter reached: 3");
            Multimedia.playSFX("counter.wav");
            counterTimeline.play();

            counterTimeline.setOnFinished(event -> {
                counter.setText("2");
                counter.setFill(Color.YELLOW);
                logger.info("Counter reached: 2");
                counterTimeline.play();

                counterTimeline.setOnFinished(event2 -> {
                    counter.setText("1");
                    counter.setFill(Color.RED);
                    logger.info("Counter reached: 1");
                    counterTimeline.play();

                    counterTimeline.setOnFinished(event3 -> {
                        logger.info("Counter finished");
                        finishCounter(overlayCounter,timeline);
                    });
                });
            });
        } else {
            finishCounter(overlayCounter,timeline);
        }
    }

    /**
     * Used to finish the counter and start the game.
     * @param overlayCounter The BorderPane in front of the game, to remove.
     * @param timeline the timeline animation to unblur the background.
     */
    private void finishCounter(BorderPane overlayCounter, Timeline timeline) {

        /*the finishCounter method removes the (overlayCounter) borderPane layer which contains the
        counter, then plays the animation (timeline) to reduce the blur from 15 to 0.*/
        root.getChildren().remove(overlayCounter);
        Multimedia.StartBGMusic("game.wav");
        timeline.play();
        gameStarted = true;
        game.start();
    }

    /**
     *
     * @return a VBox containing the GameBoard component for the UI.
     */
    protected VBox setupBoard() {
        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);

        VBox gameBoard = new VBox(board);
        gameBoard.setAlignment(Pos.CENTER);
        gameBoard.setPadding(new Insets(50,0,0,0));
        board.setOnMouseExited(event -> {
            resetHover();
        });

        return gameBoard;
    }

    /**
     * Sets up the listeners for when:
     * If there's been a change to the piece boards (updates the boards).
     * If any lines were cleared (plays the animation for fading out the blocks as well as shaking the board).
     * if the key to rotate the game piece has been pressed.
     * If the key to swap the current piece and following piece has been pressed.
     * If the board has been hovered over.
     * If the board has been clicked on.
     * @param pieceBoard the small 3x3 piece board used to show the current piece
     * @param followingPieceBoard the small 3x3 piece board used to show the following piece
     */
    protected void setupListenersHandlers(PieceBoard pieceBoard, PieceBoard followingPieceBoard) {

        //Listens if there's been a change to nextpiece or followingpiece, and updates the boards.
        game.setNextPieceListener((nextpiece, followingPiece) -> {
            pieceBoard.setPiece(nextpiece);
            followingPieceBoard.setPiece(followingPiece);
        });

        //Listens if any lines were cleared.
        game.setLineClearedListener((gameBlocksCoordinates) -> {
            board.fadeOut(gameBlocksCoordinates);
            // Create a TranslateTransition animation to shake the board when a line is cleared.
            TranslateTransition shakeTransition = new TranslateTransition(Duration.millis(50), board);
            shakeTransition.setFromX(-5);
            shakeTransition.setToX(5);
            shakeTransition.setCycleCount(5);
            shakeTransition.setAutoReverse(true);

            // Play the animation
            shakeTransition.play();
        });

        /*the rotation handlers for the boards, either right-clicking on the game board or
        left clicking on the piece board.*/
        board.rotationHandler();
        pieceBoard.rotationHandler();

        followingPieceBoard.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                game.swapCurrentPiece();
            }
        });

        //Handles when a block is hovered over by the mouse.
        board.setOnBlockHover(this::blockHovered);

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        //Handle rotation when gameboard grid is clicked
        board.setOnRightClicked(this::RightClicked);

        //Handle rotation when pieceboard grid is clicked
        pieceBoard.setOnRightClicked(this::RightClicked);
    }

    /**
     *
     *
     * @return VBox containing the score with default label ("Score"). The score is bound to the game's score.
     */
    protected VBox setupScore() {
        //since this is a single player game, the label for the score is only "Score".
        return setupScore("Score");
    }

    /**
     *
     * @param scoreTitle the text for the score label to use.
     * @return VBox containing the score with the custom score label. The score is bound to the game's score.
     */
    protected VBox setupScore(String scoreTitle) {
        /*creates the label for Score. this is parsed in as it can either be "Scores" or
        the player's name if it's a multiplayer game.*/
        Text scoreTitleLabel = new Text(scoreTitle);
        Text scoreLabel = new Text();
        scoreTitleLabel.getStyleClass().add("heading");
        scoreLabel.getStyleClass().add("score");
        VBox scoreBox = new VBox(scoreTitleLabel,scoreLabel);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setSpacing(5);

        // Create a transition to animate the scoreLabel text change
        IntegerProperty currentScore = new SimpleIntegerProperty();
        Transition scoreTransition = new Transition() {
            {
                setCycleDuration(Duration.seconds(0.5)); // Change the duration as needed
            }
            protected void interpolate(double frac) {
                int oldVal = currentScore.get();
                int newVal = game.scoreProperty().get();
                int diff = newVal - oldVal;
                int increment = (int) (diff * frac);
                currentScore.set(oldVal + increment);
            }
        };

        // Bind the scoreLabel text to the currentScore property
        scoreLabel.textProperty().bind(currentScore.asString());

        // Add a listener to start the transition when the scoreProperty changes
        game.scoreProperty().addListener((observable, oldValue, newValue) -> {
            scoreTransition.stop();
            scoreTransition.playFromStart(); // Start the animation from the beginning
        });

        Text levelLabel = new Text("Level");
        Text level = new Text();
        levelLabel.getStyleClass().add("rightmenu");
        //Binds the level counter to the game's level property.
        level.textProperty().bind(game.levelProperty().asString());
        level.getStyleClass().add("level");

        VBox levelBox = new VBox(levelLabel,level);
        levelBox.setAlignment(Pos.CENTER);

        VBox scoreLevelBox = new VBox(scoreBox,levelBox);
        scoreLevelBox.setAlignment(Pos.CENTER);
        scoreLevelBox.setSpacing(10);

        return scoreLevelBox;
    }

    /**
     *
     * @return VBox of the default title label ("Challenge Mode")
     */
    protected VBox setupTitle() {
        return setupTitle("Challenge Mode");
    }

    /**
     *
     * @param title the custom title to set the title label to.
     * @return VBox of the custom title label
     */
    protected VBox setupTitle(String title) {
        Text challengeLabel = new Text(title);
        challengeLabel.getStyleClass().add("title");
        VBox titleBox = new VBox(challengeLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20,0,0,0));
        return titleBox;
    }

    /**
     *
     * @return VBox of the lives label and the lives counter. Binded to the game's lives property.
     */
    protected VBox setupLives() {
        Text livesTitleLabel = new Text("Lives");
        Text livesLabel = new Text();
        livesTitleLabel.getStyleClass().add("heading");
        //binds the lives counter to the game lives' property.
        livesLabel.textProperty().bind(game.livesProperty().asString());

        //creates the animation for the multiplier, to shake when the multiplier gets reset to 1.
        TranslateTransition lowerLives = new TranslateTransition(Duration.seconds(0.01), livesLabel);
        lowerLives.setToX(10);
        lowerLives.setAutoReverse(true);
        lowerLives.setCycleCount(20);

        game.livesProperty().addListener((obs, oldLives, newLives) -> {
            if (newLives.intValue() < oldLives.intValue()) {
                lowerLives.play();
            }
        });

        livesLabel.getStyleClass().add("lives");
        VBox livesBox = new VBox(livesTitleLabel,livesLabel);
        livesBox.setAlignment(Pos.CENTER);
        livesBox.setSpacing(5);
        return livesBox;
    }

    /**
     *
     * @return the VBox containing the label for multiplier, and the number which is binded to the
     * game. It also animates when the multiplier goes up or down.
     */
    protected VBox setupMultiplier() {
        //sets up the Multiplayer component for the right side.
        Text multiplierLabel = new Text("Multiplier");
        Text multiplier = new Text();
        multiplierLabel.getStyleClass().add("rightmenu");
        multiplier.textProperty().bind(game.multiplierProperty().asString());
        multiplier.getStyleClass().add("multiplier");

        //creates the animation for the multiplier, to scale up and rotate when the multiplier goes up.
        Timeline sequenceAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(multiplier.scaleXProperty(), 1), new KeyValue(multiplier.rotateProperty(), 0)),
                new KeyFrame(Duration.seconds(0.1), new KeyValue(multiplier.scaleXProperty(), 1.5), new KeyValue(multiplier.scaleYProperty(), 1.5), new KeyValue(multiplier.rotateProperty(), 10)),
                new KeyFrame(Duration.seconds(0.3), new KeyValue(multiplier.scaleXProperty(), 1), new KeyValue(multiplier.scaleYProperty(), 1), new KeyValue(multiplier.rotateProperty(), 0))
        );
        sequenceAnimation.setAutoReverse(false);

        //creates the animation for the multiplier, to shake when the multiplier gets reset to 1.
        TranslateTransition decreaseAnimation = new TranslateTransition(Duration.seconds(0.01), multiplier);
        decreaseAnimation.setToX(10);
        decreaseAnimation.setAutoReverse(true);
        decreaseAnimation.setCycleCount(20);


        game.multiplierProperty().addListener((obs, oldMultiplier, newMultiplier) -> {
            if (newMultiplier.intValue() > oldMultiplier.intValue()) {
                sequenceAnimation.play();
            } else if (newMultiplier.intValue() < oldMultiplier.intValue()) {
                decreaseAnimation.play();
            }
        });


        VBox multipliers = new VBox(multiplierLabel,multiplier);
        multipliers.setAlignment(Pos.CENTER);
        multipliers.setSpacing(5);
        return multipliers;
    }

    /**
     * Sets up the right area side of the UI.
     * Consists of the Level, High Score, and the piece boards (current piece + following piece).
     * @param smallboards the VBox of the two piece boards together.
     * @return the VBox of the right side of the UI.
     */
    protected VBox setupRightSide(VBox smallboards) {
        //sets up the Multiplier, High score, and piece board components for the right side.

        VBox multipliers = setupMultiplier();

        Text highScoreLabel = new Text("High Score");
        Text highScore = new Text(String.valueOf(this.highScore));
        highScoreLabel.getStyleClass().add("rightmenu");
        highScore.getStyleClass().add("hiscore");

        /*If the current game score is higher than the high score from the local score list, it will
        change the high score to the current game score.*/
        if (this.highScore >= game.scoreProperty().get()) {
            game.scoreProperty().addListener((obs, oldScore, newScore) -> {
                if (newScore.intValue() > this.highScore) {
                    this.highScore = newScore.intValue();
                    highScore.textProperty().bind(game.scoreProperty().asString());
                }
                logger.info("score updated");
            });
        }

        Text incomingLabel = new Text("Incoming");
        incomingLabel.getStyleClass().add("rightmenu");

        VBox.setMargin(incomingLabel,new Insets(20,0,0,0));

        VBox rightSide = new VBox(multipliers,highScoreLabel,highScore,incomingLabel,smallboards);
        rightSide.setAlignment(Pos.CENTER);
        rightSide.setSpacing(5);
        return rightSide;
    }

    /**
     * Stops the game and starts the score screen.
     */
    protected void endGame() {
        game.stop();
        //starts the score screen and parses in the player's score to be added to the score lists.
        gameWindow.startScore(game.scoreProperty().getValue());
    }

    /**
     * Sets up the timer at the bottom of the screen.
     * @return the physical timer component, as a 'rectangle'.
     */
    protected Rectangle setupTimerBar() {
        Rectangle timerBar = new Rectangle(0, 0, gameWindow.getWidth() - 20, 20);
        timerBar.setFill(Color.GREEN);

        timerTimeline = new Timeline();
        timerTimeline.setCycleCount(Timeline.INDEFINITE);

        KeyFrame keyFrame = new KeyFrame(Duration.millis(game.getTimerDelay()), new KeyValue(timerBar.widthProperty(), 1));
        timerTimeline.getKeyFrames().add(keyFrame);

        //if the game is started, it plays the timer animation.
        if (gameStarted) {
            timerTimeline.play();
        }

        //When the game loops, it resets the timer animation.
        game.setOnGameLoop(() -> {
            if (game.livesProperty().get() <= -1) {
                Platform.runLater(() -> {
                    endGame();
                    timerTimeline.stop();
                });
            }
            timerTimeline.stop();
            timerBar.setWidth(gameWindow.getWidth() - 20);
            timerBar.setFill(Color.GREEN);
            KeyFrame newKeyFrame = new KeyFrame(Duration.millis(game.getTimerDelay()), new KeyValue(timerBar.widthProperty(), 1));
            timerTimeline.getKeyFrames().set(0, newKeyFrame);

            if (gameStarted) {
                timerTimeline.play();
            }
        });

        //Changes the colour of the timer bar depending on the current progress of the timer animation.
        timerTimeline.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            double progress = newValue.toMillis() / (game.getTimerDelay());
            if (progress > 0.75) {
                timerBar.setFill(Color.RED);
            } else if (progress > 0.5) {
                timerBar.setFill(Color.ORANGE);
            } else if (progress > 0.25) {
                timerBar.setFill(Color.YELLOW);
            } else {
                timerBar.setFill(Color.GREEN);
            }
        });
        return timerBar;
    }

    /**
     * Gets the top high score when starting a game from the local file.
     * sets highScore integer to it.
     */
    private void getHighScore() {

        //checks if the local score file actually exists. If not, it sets high score to 0.
        File file = new File("scores.txt");
        String fullFilepath = file.getAbsolutePath();
        if (!file.exists()) {
            logger.error("scores list appears to be nonexistent, setting default high score");
            highScore = 1000;
            return;
        }

        //Reads the local scores file for a high score.
        try (BufferedReader br = new BufferedReader(new FileReader(fullFilepath))) {
            try {
                String line = br.readLine(); // read one line
                String[] parts = line.split(":");
                highScore = Integer.parseInt(parts[1]);
            } catch (Exception e) {
                logger.error("scores list appears to be empty, setting default high score");
                highScore = 1000;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up the default keyboard controls.
     * Call it when "gameWindow.getScene().setOnKeyPressed((e) -> {"
     * @param key The key that is being pressed.
     */
    protected void keyboardControls(KeyCode key) {

        //button pressed to leave the game.
        if (key == KeyCode.ESCAPE) {
            logger.info("Escape button pressed");
            leaveGame();
        }
        //The button pressed to place a block.
        if (key == KeyCode.ENTER || key == KeyCode.X) {
            logger.info("keyboard for placing block pressed");
            blockClicked(selectedBlock);
        }
        //the button pressed to rotate the current piece leftwards.
        if (key == KeyCode.Q || key == KeyCode.Z || key == KeyCode.CLOSE_BRACKET) {
            logger.info("rotate left (keyboard) button pressed");
            game.rotateCurrentPiece(true);
        }
        //the button pressed to rotate the current piece rightwards.
        if (key == KeyCode.E || key == KeyCode.C || key == KeyCode.OPEN_BRACKET) {
            logger.info("rotate right (keyboard) button pressed");
            game.rotateCurrentPiece(false);
        }
        //the button pressed to swap the current piece.
        if (key == KeyCode.SPACE || key == KeyCode.R) {
            logger.info("swap current piece button pressed");
            game.swapCurrentPiece();
        }
        //the keyboard control for moving the select coordinates up.
        if (key == KeyCode.W || key == KeyCode.UP) {
            if (hoverY > 0 ) {
                hoverY -= 1;
            } else {
                logger.info("Reached upper limit of grid");
            }
        }
        //the keyboard control for moving the select coordinates left.
        if (key == KeyCode.A || key == KeyCode.LEFT) {
            if (hoverX > 0) {
                hoverX -= 1;
            } else {
                logger.info("Reached left limit of grid");
            }
        }
        //the keyboard control for moving the select coordinates down.
        if (key == KeyCode.S || key == KeyCode.DOWN) {
            if (hoverY < 4 ) {
                hoverY += 1;
            } else {
                logger.info("Reached lower limit of grid");
            }
        }
        //the keyboard control for moving the select coordinates right.
        if (key == KeyCode.D || key == KeyCode.RIGHT) {
            if (hoverX < 4 ) {
                hoverX += 1;
            } else {
                logger.info("Reached right limit of grid");
            }
        }
        //this is to reset the hover effect of the block, in any case a keyboard control that
        //changes the block position/rotation was played.
        setSelectedBlock();
    }

    /**
     * Used to set the selected block to the current hoverX and hoverY coordinates.
     * Then proceeds to change the game board UI to display the current piece that is about to be played.
     * If the piece is playable, the piece will be white. If not, then it will be red.
     */
    private void setSelectedBlock() {
        //sets the selected block to the current hoverX and hoverY coordinates.
        selectedBlock = board.getBlock(hoverX,hoverY);

        //clears out the current board from any hover effect.
        resetHover();

        //by default, the block will be hoverable.
        boolean hoverable = true;

        //this is to make sure it's the center of the block that will be shown and placed.
        int topX = hoverX - 1;
        int topY = hoverY - 1;

        //if the game, for some reason, doesn't have a current piece, it will stop this method
        //as there won't be anything to place.
        if (game.getCurrentPiece() == null) {
            return;
        }

        //gets the current piece's block placements.
        int[][] blocks = game.getCurrentPiece().getBlocks();

        //if we can't place the piece, returns, and sets hoverable to false
        if (!canHoverOver(game.getCurrentPiece())) {
            hoverable = false;
        };

        //Set all the appropriate blocks to 'hover',
        for (var blockX = 0; blockX < blocks.length; blockX++) {
            for (var blockY = 0; blockY < blocks.length; blockY++) {
                //blockX and a blockY coordinate inside the blocks 3x3 array
                var blockValue = blocks[blockX][blockY];
                if (blockValue > 0) {
                    try {
                        GameBlock gameBlock = board.getBlock(topX+blockX,topY+blockY);
                        gameBlock.setHoverBlock(true,hoverable);
                    } catch (Exception e) {
                        hoverable = false;
                    }
                }
            }
        }
    }

    /**
     * Used to reset the game board UI to not have any hovered blocks.
     */
    protected void resetHover() {
        for (var blockX = 0; blockX < game.getGrid().getCols(); blockX++) {
            for (var blockY = 0; blockY < game.getGrid().getRows(); blockY++) {
                //blockX and a blockY coordinate inside the blocks 3x3 array
                board.getBlock(blockX,blockY).setHoverBlock(false,true);
            }
        }
    }

    /**
     * This method is used to decide if the piece that is passed in, is hoverable or not. This means that if there's
     * space on the game board for the piece to be played at the current hoverX and hoverY coordinates.
     * @param piece The piece that is decided if it's hoverable
     * @return true if it's hoverable, false if not.
     */
    public boolean canHoverOver(GamePiece piece) {
        int[][] blocks = piece.getBlocks();

        int topX = hoverX - 1;
        int topY = hoverY - 1;

        for (var blockX = 0; blockX < blocks.length; blockX++) {
            for (var blockY = 0; blockY < blocks.length; blockY++) {
                //blockX and a blockY coordinate inside the blocks 3x3 array
                try {
                    var blockValue = blocks[blockX][blockY];
                    if (blockValue > 0) {
                        //Check if we can place on our grid
                        var gridValue = board.getBlock(topX + blockX, topY + blockY).getValue();
                        if (gridValue != 0) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        }
        //Nothing in the way
        return true;
    }

    /**
     * Handle when a block is clicked.
     * @param gameBlock the Game Block that was clicked.
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
        blockHovered(gameBlock);
    }

    /**
     * Handle when a block is hovered over.
     * @param gameBlock the Game Block that was hovered.
     */
    private void blockHovered(GameBlock gameBlock) {
        selectedBlock = gameBlock;
        hoverX = selectedBlock.getX();
        hoverY = selectedBlock.getY();
        setSelectedBlock();
    }

    /**
     * Handle when a block is right-clicked.
     */
    private void RightClicked() {
        game.rotateCurrentPiece(false);
        setSelectedBlock();
    }

    /**
     * Setup the game object and model.
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);

        //Read in high score
        getHighScore();
    }

    /**
     * Initialise the scene and start the game.
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        //at the start, it will set up the keyboard controls with a listener on if any key was pressed.
        gameWindow.getScene().setOnKeyPressed((e) -> {
            keyboardControls(e.getCode());
        });
    }

    /**
     * Stops the game then switches back to the menu.
     * Not to be confused with "endGame" which happens when you lose all your lives, and goes to the score screen.
     */
    public void leaveGame() {
        logger.info("Stopping the game");

        //This is to enable being able to leave the game when the counter is still playing at the start.
        if (counterTimeline != null) {
            counterTimeline.stop();
            Multimedia.stopSFX();
        }
        //this is to stop the timer animation from running in the background when the game isn't running.
        if (timerTimeline != null) {
            timerTimeline.stop();
        }
        game.stop();
        gameWindow.startMenu();
    }

}
