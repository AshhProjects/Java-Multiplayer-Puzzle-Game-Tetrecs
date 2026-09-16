package uk.ac.soton.comp1206.scene;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Multi Player challenge scene. Holds the UI for the multiplayer challenge mode in the game.
 */
public class MultiplayerScene extends ChallengeScene {
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    /**
     * The text field to enter a message, below the game board.
     */
    private TextField enterMessage;

    /**
     * The communicator to send and receive information about the game.
     */
    private final Communicator communicator = gameWindow.getCommunicator();

    /**
     * The username of the user playing this game.
     */
    private final String userName;

    /**
     * The max amount of users in this game.
     */
    private final int maxUsers;

    /**
     * Create a new Multiplayer challenge scene
     *
     * @param gameWindow the Game Window
     * @param myName the name of the user who's playing this game.
     * @param maxUsers the total amount of users who are playing this game.
     */
    public MultiplayerScene(GameWindow gameWindow, String myName, int maxUsers) {
        super(gameWindow);
        logger.info("Creating Multiplayer Scene");
        logger.info(scene);
        userName = myName;
        this.maxUsers = maxUsers;
    }

    /**
     * Disables the counter at the start of the game, to start the game instantly.
     */
    @Override
    protected void setCounter() {
        counterEnabled = false;
    }

    /**
     * Stops the game and starts the score screen
     * Overrides original, to replace the local scores list with the multiplayer leaderboard
     */
    @Override
    protected void endGame() {
        game.stop();
        //starts the score scene, parsing in the player leaderboard to replace the local scores list
        //which would've otherwise shown up.
        gameWindow.startScore(((MultiplayerGame) game).leaderboardListProperty());
    }

    /**
     * Calls the keyboardControls from the challengeScene for default keys, and adds 'T' key
     * for the chat.
     * @param key the key that was pressed by the user.
     */
    @Override
    public void keyboardControls(KeyCode key) {
        super.keyboardControls(key);

        //adds an additional T key, used to show the chat message box.
        if (key == KeyCode.T) {
            enterMessage.setVisible(true);
            enterMessage.setDisable(false);
        }
    }

    /**
     * Sets up the game board.
     * @return returns a VBox containing the game board, the chat message and the enter chat box.
     */
    @Override
    protected VBox setupBoard() {

        /*This is almost the same as the one from challengeScene, however it's edited to include the
        chat text message and the enter message box below the board.*/

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        Text textMessage = new Text("press T to enter Chat");
        textMessage.getStyleClass().add("messages");
        textMessage.setFill(Color.WHITE);
        enterMessage = new TextField();
        enterMessage.setVisible(false);
        enterMessage.setDisable(true);
        enterMessage.setOnKeyPressed((e) -> {
            var key = e.getCode();
            //If the enter button or escape button is pressed, it will go ahead and send the message.
            if (key == KeyCode.ENTER || key == KeyCode.ESCAPE) {
                enterMessage.setVisible(false);
                enterMessage.setDisable(true);
                if (key == KeyCode.ENTER) {
                    communicator.send("MSG " +enterMessage.getText());
                }
                enterMessage.setText("");

                /*This 'consume' is so that the escape button or enter button isn't mixed with the
                game's keyboard controls, so the user doesn't accidentally leave the game or
                place a block by pressing enter or escape.*/
                e.consume();
            }
        });

        //Prevents from too long messages from being entered, which can ruin the entire UI.
        enterMessage.setTextFormatter(new TextFormatter<String>((change) -> {
            if (change.getControlNewText().length() > 50) {
                return null;
            } else {
                return change;
            }
        }));

        /*the pane for the text message, used because depending on the letters in the text message,
        it can move the board up or down. So a stack pane is used to set a preferred height to
        prevent this.*/
        StackPane textMessagePane = new StackPane(textMessage);
        textMessagePane.setPrefHeight(40);

        //listens for incoming messages.
        communicator.addListener(message -> {

            //if the message received is a chat message,
            if (message.startsWith("MSG")) {
                Platform.runLater(() -> {

                    //this is to remove the "MSG " at the start of the message
                    String receivedMessage = message.substring(4);
                    String[] partsOFMessage = receivedMessage.split(":",2);
                    String username = partsOFMessage[0];
                    String playerMessage = partsOFMessage[1];
                    //This makes the chat message look nicer by adding <> around the player's name.
                    String displayMessage = ("<" + username + ">" + " : " + playerMessage + "\n");
                    textMessage.setText(displayMessage);
                    Multimedia.playSFX("message.wav");

                });
            }
        });

        board.setOnMouseExited(event -> {
            resetHover();
        });

        VBox boardAndChat = new VBox(board,textMessagePane,enterMessage);
        boardAndChat.setAlignment(Pos.CENTER);
        boardAndChat.setPadding(new Insets(119,0,0,0));

        return boardAndChat;
    }

    /**
     *
     * @param smallBoards the VBox of the two piece boards together.
     * @return returns a VBox of the right side of the UI, containing the Level and versus labels as well as the small boards.
     */
    @Override
    protected VBox setupRightSide(VBox smallBoards) {
        //sets up the multiplier, leaderboard, and piece board components for the right side.

        VBox multipliers = setupMultiplier();

        Text VersusLabel = new Text("Versus");
        VersusLabel.getStyleClass().add("rightmenu");

        /*Gets the maximum number between the amount of users there are and '15'.
        This is to make sure the colouring is fine, otherwise the top users could be completely
        red if only maxUsers was inputted.*/
        Leaderboard leaderboard = new Leaderboard(Math.max(maxUsers,15));

        /*binds the leaderboard scores property to the game's leaderboard list,
        as well as binds the list that talks about if each user is dead or alive, to the game's list
        which contains that information. This is used to strikeout the name if the user is dead.*/
        leaderboard.scoresProperty().bind(((MultiplayerGame) game).leaderboardListProperty());
        leaderboard.usersDeadProperty().bind(((MultiplayerGame) game).leaderboardPlayersDeadProperty());
        
        Text incomingLabel = new Text("Incoming");
        incomingLabel.getStyleClass().add("rightmenu");

        VBox.setMargin(incomingLabel,new Insets(20,0,0,0));

        VBox rightSide = new VBox(multipliers,VersusLabel,leaderboard,incomingLabel,smallBoards);
        rightSide.setAlignment(Pos.CENTER);
        rightSide.setSpacing(5);
        return rightSide;
    }

    /**
     * Setup the game object and model
     */
    @Override
    public void setupGame() {
        logger.info("Starting a new Multiplayer Game");

        //Start new game
        super.game = new MultiplayerGame(5, 5,communicator);

    }

    /**
     *
     * @return returns the VBox of the score in the corner of the screen, with the label being the username of the player.
     */
    @Override
    protected VBox setupScore() {
        return setupScore(userName);
    }

    /**
     *
     * @return returns the VBox of the title of the game, with the title being "Multiplayer Mode".
     */
    @Override
    protected VBox setupTitle() {
        return setupTitle("Multiplayer Mode");
    }

}
