package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * A message box component used to receive and show text messages in a channel.
 */
public class MessageBox extends VBox {
    private static final Logger logger = LogManager.getLogger(MessageBox.class);

    /**
     * The scroller for the messages.
     */
    private final ScrollPane scroller;

    /**
     * The text flow of all the messages together.
     */
    private final TextFlow messages;

    /**
     * The 'enter message' box used to enter and send the message.
     */
    private final TextField messageInput;

    /**
     * The join button used to start the game.
     */
    private final Button joinButton;

    /**
     * The leave button used to leave the game.
     */
    private final Button leaveButton;

    /**
     * The boolean to decide whether to scroll all the way to the bottom.
     */
    private boolean scrollToBottom;

    /**
     * The communicator used to send and receive messages.
     */
    private final Communicator communicator;

    /**
     * The title label of the message box. The same as the channel name/
     */
    private Text titleLabel;

    /**
     * All the messages in the message box.
     */
    private TextFlow playersLabel;

    /**
     * The game window that this message box belongs to.
     */
    private final GameWindow gameWindow;

    /**
     * the username of the local user on this game.
     */
    private String myUser;

    /**
     * The total amount of users in the channel.
     */
    private int maxUsers;

    /**
     * The constructor of the class.
     * @param gameWindow the game window that this message box belongs to.
     */
    public MessageBox(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.communicator = gameWindow.getCommunicator();
        this.messages = new TextFlow();
        this.messageInput = new TextField();
        this.joinButton = new Button("Join Game");
        this.leaveButton = new Button("Leave Channel");
        this.scroller = new ScrollPane();
        scroller.getStyleClass().add("gamepane");
        setupUI();
    }

    /**
     * To be called when a new channel is joined.
     * @param channelName the channel name the title will be set to.
     */
    public void setChannelName(String channelName) {

        //sets the title of the message box to the channel name that is given in.
        titleLabel.setText(channelName);
        Platform.runLater(() -> {
            //It clears all the messages that is in the message box, and adds the default initial
            //message "welcome to the lobby!".
            messages.getChildren().clear();
            addMessageNoUser("Welcome to the Lobby!");
            addMessageNoUser("to change your name, type /nick {name}");
        });
        //hides the join button by default.
        joinButton.setVisible(false);
    }

    /**
     * Jumps the scroll to the bottom.
     */
    private void jumpToBottom() {
        if (!scrollToBottom) return;
        scroller.setVvalue(1.0f);
        scrollToBottom = false;
    }

    /**
     * Sets up the UI of the message bx.
     */
    private void setupUI() {
        //sends an initial NICK command to retrieve the name of the user.
        communicator.send("NICK");

        this.getStyleClass().add("gameBox");
        this.setMaxHeight(gameWindow.getHeight()/1.3);
        this.setMaxWidth(gameWindow.getWidth()/1.5);

        //makes the scroller invisible.
        this.scroller.getStyleClass().add("scroller");

        //gives a unique style to the messages.
        messages.getStyleClass().add("messages");

        scroller.setContent(messages);
        scroller.setFitToWidth(true);
        scroller.setPrefHeight(300);

        // Add channel title label
        titleLabel = new Text();
        titleLabel.getStyleClass().add("channelItem");

        //The list of players to display at the top.
        playersLabel = new TextFlow();
        playersLabel.getStyleClass().add("playerBox");
        playersLabel.setLineSpacing(10);

        // Add message input field
        messageInput.setOnAction(event -> {
            String message = messageInput.getText();
            if (!message.isEmpty()) {
                //if the message is a command to change name:
                if (message.startsWith("/nick")) {
                    //substring to remove the "/nick" at the start
                    String nickName = message.substring(5);
                    communicator.send("NICK " + nickName);
                } else {
                    //if it's not a command, it will just send the message to the server.
                    communicator.send("MSG " +message);
                }
                //clears the message input box after a message is sent.
                messageInput.clear();
            }
        });

        // Add join and leave buttons
        HBox joinBox = new HBox(joinButton);
        joinBox.setAlignment(Pos.BOTTOM_LEFT);
        HBox leaveBox = new HBox(leaveButton);
        leaveBox.setAlignment(Pos.BOTTOM_RIGHT);

        //This is to position the buttons in the corners of the messageBox.
        BorderPane buttonContainer = new BorderPane(null, null, leaveBox, null, joinBox);
        BorderPane.setMargin(joinBox, new Insets(0, 10, 0, 10));
        BorderPane.setMargin(leaveBox, new Insets(0, 10, 0, 10));

        // Add components to the message box
        getChildren().addAll(titleLabel, playersLabel, scroller, messageInput, new Separator(), buttonContainer);
        setPadding(new Insets(10));
        setSpacing(10);

        //if the leave button is pressed:
        leaveButton.setOnAction(event -> {
            Multimedia.playSFX("select.wav");
            communicator.send("PART");
            communicator.send("LIST");
        });

        //if the join button is pressed.
        joinButton.setOnAction(event -> {
            Multimedia.playSFX("select.wav");
            Multimedia.stopBGMusic();
            Multimedia.StartBGMusic("game.wav");
            communicator.send("START");
        });

        //Listens for incoming server messages
        communicator.addListener(message -> {

            //if the incoming message is a message to be displayed.
            if (message.startsWith("MSG")) {
                addMessage(message);
            }

            //if the incoming message is a list of all the users.
            if (message.startsWith("USERS")) {
                Platform.runLater(() -> {

                    //clears the playersLabel textFlow as it will be replaced by the new one.
                    playersLabel.getChildren().clear();
                    //defaults maxUsers to 0.
                    maxUsers = 0;
                    String allUserNames = message.split(" ")[1];
                    String[] userLists = allUserNames.split("\n");
                    for (String userString : userLists) {
                        Text userText = new Text(userString + " ");
                        //if the user that is added is the client, it will make that name yellow.
                        if (userString.equals(myUser)) {
                            userText.getStyleClass().add("myname");
                        }
                        playersLabel.getChildren().add(userText);
                        maxUsers++;
                    }
                });
            }

            /*if the incoming message is "HOST" which tells that you are the host of this channel.
            this makes the join button visible.*/
            if (message.startsWith("HOST")) {
                joinButton.setVisible(true);
            }

            //if the message is to start the game.
            if (message.startsWith("START")) {
                Platform.runLater(() -> {
                    gameWindow.startMultiplayer(myUser,maxUsers);
                });
            }

            /*if the message is "NICK" which tells you your username,
            this sets myUser variable to the current user's name.*/
            if (message.startsWith("NICK")) {
                //substring is to remove the "NICK" at the start.
                myUser = message.substring(5);
                logger.info("Nick called, and username is {}",myUser);
            }
        });
    }

    /**
     *
     * @param message the message to be added to the message box.
     * The message should follow a {user}:{message} format.
     */
    public void addMessage(String message) {
        Platform.runLater(() -> {

            //if the message is not a valid message, it won't display the message.
            if (!message.contains((":"))) return;

            //removes the "MSG" at the start
            String receivedMessage = message.substring(4);

            //splits the message to the username and the message itself
            String[] partsOFMessage = receivedMessage.split(":",2);
            String username = partsOFMessage[0];
            String playerMessage = partsOFMessage[1];

            //makes the display message look nicer by adding < > around the username
            Text displayMessage = new Text("<" + username + ">" + " : " + playerMessage + "\n");

            //adds the message to the text flow messages, and plays a sound effect.
            messages.getChildren().add(displayMessage);
            Multimedia.playSFX("message.wav");

            //This is an animation to make the message entering smoother, by fading it in.
            displayMessage.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(100),displayMessage);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();

            /*if the user's scroller is at the bottom, or not at the top, it will automatically
            scroll all the way down when a new message is received.*/
            if(scroller.getVvalue() == 0.0f || scroller.getVvalue() >  0.9f) {
                scrollToBottom = true;
            }
            gameWindow.getScene().addPostLayoutPulseListener(this::jumpToBottom);
        });
    }

    /**
     * Adds a simple message to the message box, does not follow a user:message format.
     * @param message the message to add.
     */
    public void addMessageNoUser(String message) {
        Platform.runLater(() -> {
            Text displayMessage = new Text(message + "\n");
            messages.getChildren().add(displayMessage);

            //simple fade in animation for the message.
            displayMessage.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(100),displayMessage);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();

            /*if the user's scroller is at the bottom, or not at the top, it will automatically
            scroll all the way down when a new message is received.*/
            if(scroller.getVvalue() == 0.0f || scroller.getVvalue() >  0.9f) {
                scrollToBottom = true;
            }
            gameWindow.getScene().addPostLayoutPulseListener(this::jumpToBottom);
        });
    }
}

