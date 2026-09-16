package uk.ac.soton.comp1206.scene;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.MessageBox;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The multiplayer lobby scene. Displays all the games you can join to play in, or to host a new game.
 */
public class LobbyScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(LobbyScene.class);

    /**
     * The message box of the lobby scene. Where all the messages will be sent.
     * Used to join and leave the game channels too.
     */
    private MessageBox messageBox;

    /**
     * List of all the game channels on the left side.
     */
    private VBox allGamesList;

    /**
     * The communicator used to send and receive server messages.
     */
    private Communicator communicator;

    /**
     * The error message displayed if an error is received from the server.
     */
    private Text errorMessage;

    /**
     * Create a new Lobby scene
     *
     * @param gameWindow the Game Window this will be displayed in
     */

    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Lobby Scene");
    }

    /**
     * This is used to detect if the user presses escape or backspace, to go back to the menu.
     */
    @Override
    public void initialise() {
        // Start a timer to request current channels every 5 seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                communicator.send("LIST");
            }
        }, 0, 5000);

        communicator = gameWindow.getCommunicator();

        //If the key escape is pressed, it will leave the multiplayer screen
        gameWindow.getScene().setOnKeyPressed((e) -> {
            KeyCode key = e.getCode();
            if (key == KeyCode.ESCAPE) {
                timer.cancel();
                communicator.send("PART");
                gameWindow.startMenu();
            }
        });

        // Used to clear the error message after 5 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(event -> {
            errorMessage.setText("");
        });

        // Shake animation for the error when a new error is received.
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), errorMessage);
        shake.setFromX(0);
        shake.setToX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);

        // Add a listener to the Communicator to handle incoming messages
        communicator.addListener(message -> {
            //If the incoming message is a list of all the channels.
            if (message.startsWith("CHANNELS")) {
                Platform.runLater(() -> {

                    //clears out the current allGamesList list to replace them with the list
                    //from the incoming message.
                    allGamesList.getChildren().clear();
                    String[] receivedMessage = message.split(" ");

                    if (receivedMessage.length > 1) {
                        String[] allChannelNames = receivedMessage[1].split("\n");

                        for(String channelName : allChannelNames) {
                            var channelItem = new Button(channelName);
                            channelItem.getStyleClass().add("channelItem");
                            channelItem.setOnMouseClicked(event -> {
                                communicator.send("JOIN "+ channelItem.getText());
                                Multimedia.playSFX("select.wav");
                            });
                            allGamesList.getChildren().add(channelItem);
                        }
                    }
                });
            }

            //If the incoming message is that the user is joining a channel.
            if (message.startsWith("JOIN")) {
                String[] receivedMessage = message.split(" ");
                String channelName = receivedMessage[1];
                messageBox.setChannelName(channelName);
                messageBox.setVisible(true);
            }

            //If the incoming message is that the user left a channel.
            if (message.startsWith("PARTED")) {
                messageBox.setVisible(false);
            }

            //If the incoming message is that the game started.
            if (message.startsWith("START")) {
                //it will stop the "5 second ask for channel" timer to prevent it from
                //running in the background.
                timer.cancel();
            }

            //If the message received is an error message.
            if (message.startsWith("ERROR")) {
                String errorMessageText = message.substring(6);
                errorMessage.setText(errorMessageText);
                Multimedia.playSFX("fail.wav");
                shake.playFromStart();
                pause.playFromStart();
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
        background.getStyleClass().add("menu-background");
        background.setEffect(new GaussianBlur(6));
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

        Text multiplayerLabel = new Text("Multiplayer");
        multiplayerLabel.getStyleClass().add("title");
        VBox multiplayerTitle = new VBox(multiplayerLabel);
        multiplayerTitle.setPadding(new Insets(25));

        Text currentGamesLabel = new Text("current Games");
        currentGamesLabel.getStyleClass().add("heading");

        errorMessage = new Text("");
        errorMessage.getStyleClass().add("error");

        //the error message and currentGames label will be together.
        VBox currentGamesAndError = new VBox(currentGamesLabel,errorMessage);
        currentGamesAndError.setSpacing(15);
        var hostNewGame = new Button("Host New Game");
        hostNewGame.getStyleClass().add("channelItem");

        messageBox = new MessageBox(gameWindow);
        messageBox.setVisible(false);

        TextField enterName = new TextField();
	    VBox allGamesListLabels = new VBox(currentGamesAndError, hostNewGame, enterName);
        enterName.setVisible(false);
        allGamesListLabels.setSpacing(10);
        allGamesList = new VBox();
        allGamesList.setSpacing(20);

        //The scroller for the list of all the channels, in case it gets long.
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(allGamesList);
        scrollPane.setFitToWidth(true);

        VBox leftSide = new VBox(allGamesListLabels,scrollPane);
        leftSide.setSpacing(20);

        scrollPane.setPrefHeight(gameWindow.getHeight() - allGamesListLabels.getHeight() - 80);
        scrollPane.setVvalue(0);
        scrollPane.setHvalue(0);
        scrollPane.getStyleClass().add("scroller");
        scrollPane.setPadding(new Insets(5));

        multiplayerTitle.setAlignment(Pos.CENTER);
        mainPane.setTop(multiplayerTitle);
        mainPane.setLeft(leftSide);
        mainPane.setCenter(messageBox);

        //When the host new game button is pressed, it will show the enter name box.
        hostNewGame.setOnAction(event -> {
            Multimedia.playSFX("select.wav");
            enterName.setVisible(true);

            /*if the enter name box has been entered, it will create a new channel.
            and hide the 'enter name box'.*/
            enterName.setOnAction(event2 -> {
                Multimedia.playSFX("select.wav");
                communicator.send("CREATE " + enterName.getText());
                communicator.send("LIST");
                enterName.setVisible(false);
                enterName.clear();
            });
        });
    }
}

