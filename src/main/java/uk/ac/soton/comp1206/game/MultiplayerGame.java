package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The Multiplayer game class handles the main logic, state and properties of the multiplayer version of the TetrECS game.
 * Methods to manipulate the game state and to handle actions made by the player are mainly taken place in the Game class
 * that this extends from, but this contains additional methods that are only for the multiplayer, such as the handling
 * of sending and receiving messages to the server.
 */
public class MultiplayerGame extends Game {

    /**
     * Logger used to log for this class.
     */
    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    /**
     * The communicator used to send and receive messages about the game
     */
    private final Communicator communicator;

    /**
     * The queue of the pieces received from the server.
     */
    private final Queue<Integer> serverPieces;

    /**
     * the leaderboard list property bound to the leaderboard scoresList.
     * Used to display the leaderboard list, user and scores.
     */
    private final SimpleListProperty<Pair<String,Integer>> leaderboardList;

    /**
     * The leaderboardPlayersDead property bound to the leaderboard scoresList.
     * used to display if the player is dead, strikes through their name if they are in the leaderboard.
     */
    private final SimpleListProperty<Pair<String,Boolean>> leaderboardPlayersDead;

    /**
     * Create a new multiplayer game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     * @param communicator the communicator used to send and receive messages from the server
     */
    public MultiplayerGame(int cols, int rows,Communicator communicator) {
        //sets up a normal Game with the columns and rows
        super(cols, rows);
        this.communicator = communicator;

        //The queue of the server pieces, retrieved from the server
        this.serverPieces = new LinkedList<>();

        //the list of the player names for the right side of the screen
        var arrayListLeaderboard = new ArrayList<Pair<String,Integer>>();
        leaderboardList = new SimpleListProperty<>(FXCollections.observableArrayList(arrayListLeaderboard));

        //decides whether each player is dead or not
        var arrayListLeaderboardDead = new ArrayList<Pair<String,Boolean>>();
        leaderboardPlayersDead = new SimpleListProperty<>(FXCollections.observableArrayList(arrayListLeaderboardDead));

        // Add a listener to the Communicator to handle incoming messages
        communicator.addListener(message -> {

            //If the incoming message is giving a game piece
            if (message.startsWith("PIECE")) {
                int serverPiece = Integer.parseInt(message.split(" ")[1]);
                serverPieces.add(serverPiece);
            }

            //if the incoming message tells the scores of each player
            if (message.startsWith("SCORES")) {
                Platform.runLater(() -> {

                    /*clears the lists for the player scores and if they're dead, as to replace them
                    with the new one that just arrived (in the message)*/
                    leaderboardList.clear();
                    leaderboardPlayersDead.clear();

                    //removes the "SCORES" at the start of the message
                    String fullMessage = message.substring(7);

                    //this is to include the first player if there's only one player in the game
                    fullMessage += "\n";

                    String[] eachPlayer = fullMessage.split("\n");
                    for (String playerData : eachPlayer) {
                        String[] dataSplit = playerData.split(":");
                        String playerName = dataSplit[0];
                        String playerScore = dataSplit[1];
                        String playerAlive = dataSplit[2];
                        int playerScoreInt;
                        boolean playerAliveBool;
					  	playerAliveBool = !playerAlive.equals("DEAD");
                        playerScoreInt = Integer.parseInt(playerScore);

                        //adds the user and their score/alive information to the lists
                        leaderboardList.add(new Pair<>(playerName, playerScoreInt));
                        leaderboardPlayersDead.add(new Pair<>(playerName,playerAliveBool));
                    }
                });
            }
        });

    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    protected void initialiseGame() {
        logger.info("Initialising game");

        //creates a listener to the scores, so it will send the new score to the server if changed.
        scoreProperty().addListener((obs, oldScore, newScore) -> {
            communicator.send("SCORE " + newScore);
        });

        //creates a listener to the lives, so it will send the new life to the server if changed.
        livesProperty().addListener((obs, oldLives, newLives) -> {
            communicator.send("LIVES " + newLives);
        });

        this.followingPiece = spawnPiece();
        nextPiece();
        resetTimer();

        //sends an initial "SCORES" message to set up the scores for each player at the start.
        communicator.send("SCORES");
    }

    /**
     *
     * @return the leaderboardList property for binding. The players and their scores in the game.
     */
	public SimpleListProperty<Pair<String,Integer>> leaderboardListProperty() {
	  return leaderboardList;
	}

    /**
     *
     * @return the leaderboardPlayersDead property for binding. The players and whether they're alive or not in the game.
     */
  public SimpleListProperty<Pair<String,Boolean>> leaderboardPlayersDeadProperty() {
	return leaderboardPlayersDead;
  }

    /**
     * Clears out the properties before stopping the game.
     */
    public void stop() {
        logger.info("Stopping the game.");
        stopTimer();
        Multimedia.stopBGMusic();
        communicator.send("DIE");
    }

    /**
     * Creates a new random GamePiece by calling createPiece, with a random number.
     * @return returns the game piece that was created.
     */
    public GamePiece spawnPiece() {
        communicator.send("PIECE");

        //if the queue is empty, it will wait until the community gives back a piece.
        while (serverPieces.isEmpty()) {
            try {
                Thread.sleep(10); // wait for 10 milliseconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //get the top piece on the queue.
        return GamePiece.createPiece(serverPieces.remove());
    }

    /**
     * Handles when a block is clicked in the game.
     * @param gameBlock the block that was clicked
     */
    @Override
    public void blockClicked(GameBlock gameBlock) {
        super.blockClicked(gameBlock);

        //Sends a message to the server on how the current grid board looks like.
        updateGridServer();
    }

    /**
     * Sends a message to the server about the current status of the board.
     */
    protected void updateGridServer() {
        int rows = grid.getRows();
        int columns = grid.getCols();

        StringBuilder boardData = new StringBuilder();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                int blockValue = grid.get(x, y);
                boardData.append(blockValue).append(" ");
            }
        }
        // Remove the last comma and append a newline character
        boardData.deleteCharAt(boardData.length() - 1);
        communicator.send("BOARD " + boardData);
    }

    /**
     * Adds the logic to handle when a piece is played.
     * This specifically removes any lines which are full.
     */
    @Override
    public void afterPiece() {
        super.afterPiece();

        //Sends a message to the server on how the current grid board looks like.
        updateGridServer();
    }

}
