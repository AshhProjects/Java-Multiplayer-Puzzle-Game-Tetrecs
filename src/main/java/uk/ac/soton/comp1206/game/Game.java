package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * The nextPieceListener is used to display the next piece that is about to be played.
     */
    private NextPieceListener nextPieceListener;

    /**
     * The lineClearedListener is used for the fadeout animation when a line gets cleared.
     */
    private LineClearedListener lineClearedListener;

    /**
     * The gameLoopListener is used for when the game loops when the timer runs out/losing a life.
     */
    private GameLoopListener gameLoopListener;

    /**
     * This integer property is used to keep track of the score of the user playing the game.
     */
    private final IntegerProperty score = new SimpleIntegerProperty(0);
    /**
     * This integer property is used to keep track of the level of the user playing the game.
     */
    private final IntegerProperty level = new SimpleIntegerProperty(0);
    /**
     * This integer property is used to keep track of the amount of lives the user has.
     */
    private final IntegerProperty lives = new SimpleIntegerProperty(3);
    /**
     * This integer property is used to keep track of the user's multiplier as they play the game.
     */
    private final IntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * The random variable used to generate random pieces.
     */
    private final Random random = new Random();

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;
    /**
     * The next piece grid model linked to to the game.
     */
    protected final Grid nextPieceGrid;
    /**
     * The following piece grid model linked to the game,
     */
    protected final Grid followingPieceGrid;

    /**
     * The current piece that the game is about to play.
     */
    private GamePiece currentPiece;
    /**
     * The following piece that the game will play next.
     */
    protected GamePiece followingPiece;
    /**
     * The timer used to reduce lives if it runs out. Reduces remainingTime every second.
     */
    private ScheduledExecutorService timer;
    /**
     * The current time of the timer.
     */
    private int remainingTime;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
        this.nextPieceGrid = new Grid(3,3);
        this.followingPieceGrid = new Grid(3,3);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();

    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start.
     */
    protected void initialiseGame() {
        logger.info("Initialising game");
        this.followingPiece = spawnPiece();
        nextPiece();
        resetTimer();
    }

    /**
     * Stops the timer.
     */
    protected void stopTimer() {
        if (timer != null) {
            timer.shutdown();
            timer = null;
        }
    }

    /**
     * The method ran every second from the timer.
     * Every second it reduces the remainingTime, as well as checks if remainingTime is 0, which then calls gameLoop.
     */
    private void timerTask() {
        if (remainingTime <= 0) {
            // timer has expired, call the game loop
            logger.info("timer ended.");
            gameLoop();
        } else {
            remainingTime -= 1000; // decrement the timer delay
        }
    }

    /**
     * Resets the timer (when a piece is played).
     * Calls game-loop listener, and resets the timer if there's still lives left.
     */
    public void resetTimer() {
        gameLoopListener.onGameLoop();
        remainingTime = getTimerDelay();
        stopTimer();
        if (lives.get() >= 0) {
		    //creates and starts a new timer
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(this::timerTask, 0, 1, TimeUnit.SECONDS);
            logger.info("started initial Timer {} seconds",remainingTime/1000);
        }
    }

    /**
     * Called when the timer ends.
     * Used to reduce the lives, play the sound effects, reset multiplier and the timer.
     */
    public void gameLoop() {
        logger.info("timer Ended, running gameloop");
        lives.set(lives.get() - 1); //takes one life away
        Multimedia.playSFX("lifelose.wav");
        nextPiece();
        multiplier.set(1);
        resetTimer();
    }

    /**
     *
     * @return the new timer delay, calculated from the level.
     * Calculates the delay at the maximum of either 2500 milliseconds or 12000 - 500 * the current level
     */
    public int getTimerDelay() {
        int delay = 12000 - 500 * level.get();
        return Math.max(delay,2500);
    }

    /**
     * sets the game loop listener to on
     * @param gameLoopListener the game loop listener it takes in to assign to the game.
     */
    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    /**
     * sets the next piece listener to on
     * @param nextPieceListener the next piece listener it takes in to assign to the game.
     */
    public void setNextPieceListener(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;
    }

    /**
     * sets the line cleared listener to on
     * @param lineClearedListener the line cleared listener it takes in to assign to the game.
     */
    public void setLineClearedListener(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    /**
     * Creates a new random GamePiece by calling createPiece, with a random number.
     * @return the random game piece.
     */
    protected GamePiece spawnPiece() {

        var maxPieces = GamePiece.PIECES;
        var randomPiece = random.nextInt(maxPieces);
        logger.info("Picking random piece: {}",randomPiece);

        return GamePiece.createPiece(randomPiece);

    }

    /**
     * Rotates the piece that's given.
     * @param rotateLeft whether the piece will be rotated left, otherwise will rotate right.
     */
    public void rotateCurrentPiece(boolean rotateLeft) {
        currentPiece.rotate(rotateLeft);
        Multimedia.playSFX("rotate.wav");
        logger.info("rotating the current piece");

        //updates the currentPiece and following piece boards
        nextPieceListener.nextPiece(currentPiece,followingPiece);
    }

    /**
     * Replaces current piece with a new piece.
     */
    public void nextPiece() {
        currentPiece = followingPiece;
        followingPiece = spawnPiece();
        logger.info("The next piece is: {}",currentPiece);

        //updates the currentPiece and following piece boards
        nextPieceListener.nextPiece(currentPiece,followingPiece);
    }

    /**
     * Swaps the current piece and the following piece.
     */
    public void swapCurrentPiece() {
        GamePiece tempPiece = currentPiece;
        currentPiece = followingPiece;
        followingPiece = tempPiece;
        nextPieceListener.nextPiece(currentPiece,followingPiece);
        Multimedia.playSFX("rotate.wav");
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        if (grid.canPlayPiece(currentPiece, x, y)) {
            grid.playPiece(currentPiece, x, y);
            Multimedia.playSFX("place.wav");
            afterPiece();
            //reset the timer
            resetTimer();

            //sets the current piece to the next piece
            nextPiece();
        } else {
            Multimedia.playSFX("fail.wav");
            //Can't play piece
        }
    }

    /**
     * Calculates the score and sets it.
     * Called in the subroutine afterPiece.
     * @param lines the amount of lines that was cleared
     * @param blocksCleared the amount of blocks that was cleared
     */
    public void score(int lines, int blocksCleared) {

        int points = lines * blocksCleared * 10 * multiplier.get();
        logger.info("adding {} to the score",points);
        score.set(score.get() + points);
        multiplier.set(multiplier.get() + 1);

        //sets the new level depending on the score, if necessary
        if (score.get() / 1000 > level.get()) {
            level.set(score.get() / 1000);
            Multimedia.playSFX("level.wav");
            logger.info("New level set at {}",level.get());
        }

    }

    /**
     * Adds the logic to handle when a piece is played.
     * This specifically removes any lines which are full.
     */
    public void afterPiece() {

        //the set of blocks which will be cleared, it's a set to remove duplicates
        Set<GameBlockCoordinate> blocksToClear = new HashSet<>();

        //the total number of lines that were cleared
        int linesToClear = 0;

        // Check for horizontal lines
        for (int y = 0; y < rows; y++) {
            boolean lineFound = true;
            for (int x = 0; x < cols; x++) {
                if (grid.get(x,y) == 0) {
                    lineFound = false;
                    break;
                }
            }
            if (lineFound) {
                linesToClear++;
                for (int x = 0; x < cols; x++) {
                    blocksToClear.add(new GameBlockCoordinate(x, y));
                }
            }
        }

        // Check for vertical lines
        for (int x = 0; x < cols; x++) {
            boolean lineFound = true;
            for (int y = 0; y < rows; y++) {
                if (grid.get(x,y) == 0) {
                    lineFound = false;
                    break;
                }
            }
            if (lineFound) {
                linesToClear++;
                for (int y = 0; y < rows; y++) {
                    blocksToClear.add(new GameBlockCoordinate(x, y));
                }
            }
        }

        // Clear lines
        for (GameBlockCoordinate blockCoordinate : blocksToClear) {
            logger.info("Clearing lines: {},{}",blockCoordinate.getX(),blockCoordinate.getY());
            grid.set(blockCoordinate.getX(),blockCoordinate.getY(),0);
        }
        if (linesToClear > 0) {
            //update score and level
            Multimedia.playSFX("clear.wav");
            lineClearedListener.onLineCleared(blocksToClear);
            score(linesToClear,blocksToClear.size());
        } else {
            multiplier.set(1);
        }

    }

    /**
     * Clears out the properties before stopping the game.
     */
    public void stop() {
        logger.info("Stopping the game.");
        stopTimer();
    }

    /**
     *
     * @return the current piece in the game.
     */
    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    /**
     *
     * @return the score integer property that contains the user's score.
     */
    public IntegerProperty scoreProperty() {
        return score;
    }

    /**
     *
     * @return the level integer property that contains the user's level.
     */
    public IntegerProperty levelProperty() {
        return level;
    }

    /**
     *
     * @return the lives integer property that contains the user's lives.
     */
    public IntegerProperty livesProperty() {
        return lives;
    }

    /**
     *
     * @return the multiplier integer property that contains the user's multiplier.
     */
    public IntegerProperty multiplierProperty() {
        return multiplier;
    }


    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the grid model for the next piece
     * @return next piece grid model
     */
    public Grid getNextPieceGrid() {
        return nextPieceGrid;
    }

    /**
     * Get the grid model for the following piece
     * @return following piece grid model
     */
    public Grid getFollowingPieceGrid() {
        return followingPieceGrid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

}
