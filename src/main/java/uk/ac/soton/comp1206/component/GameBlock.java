package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class); //used for logging

    /**
     * Tells whether this block is being hovered over or not.
     * The mouse doesn't have to be directly on it, if the mouse is about to place a piece, it will show the
     * current piece on the game board, and all the pieces that are to be shown have 'hover' set to true.
     */
    private boolean hover = false;

    /**
     * Used to decide the colour of the hover. If the block is placeable, it will be white. Otherwise, it will be red.
     */
    private boolean hoverable = false;

    /**
     * This is used to decide if it's the center block, for the following piece board.
     */
    private boolean isCenter = false;

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    /**
     * This is to decide what the block will look like when it has no value in.
     */
    private final Color emptyColour = Color.rgb(50, 50, 50, 0.5);

    /**
     * The board that this piece belongs to.
     */
    private final GameBoard gameBoard;
    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);

    }

    /**
     * Used to set whether the block is hovered over, and if it should be white (placeable block) or red (not placeable block).
     * @param hover Decides if it's a block that's being hovered over.
     * @param hoverable Decides if it's going to be red (false) or white (true).
     */
    public void setHoverBlock (boolean hover, boolean hoverable) {
        this.hover = hover;
        this.hoverable = hoverable;
        paint();
    }

    /**
     * Sets the block to be the center block, for the following piece board.
     */
    public void setCenter() {
        isCenter = true;
        paint();
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColour(COLOURS[value.get()]);
        }

        // Draw the hover effect if necessary
        if (hover) {
            var gc = getGraphicsContext2D();
            //if the block this piece belongs to is placeable, the hover effect will be gray.
            if (hoverable) {
                gc.setFill(Color.rgb(255, 255, 255, 0.3));
            } else {
                //if the block this piece belongs to is NOT placeable, the hover effect will be red.
                gc.setFill(Color.rgb(248, 2, 2, 0.7));
            }
            gc.fillRect(0, 0, width, height);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(0, 0, width, height);
        }

        /*Draw the center effect if necessary (used for Piece Block)
        creates a white circle in the middle of the block.*/
        if (isCenter) {
            var gc = getGraphicsContext2D();
            gc.setFill(Color.rgb(255, 255, 255, 0.6));
            double centerX = width / 2.0;
            double centerY = height / 2.0;
            double radius = Math.min(width, height) / 3.0;
            gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }

    }

    /**
     * Plays the animation for when the block is to be removed.
     */
    public void fadeOut() {
        AnimationTimer fadeOut = new AnimationTimer() {
            private double alpha = 1.0;
            private final Color startColour = Color.GREEN;
            private Color endColour;

            //The animation that will be played.
            public void handle(long now) {

                if (hover) { //if the piece this block is part of is a 'hover' effect
                    if (hoverable) { //if it's successfully hover-able
                        endColour = Color.rgb(255, 255, 255, 0.3);
                    } else { //If not successfully hover-able, if on top of another block or out of bounds
                        endColour = Color.rgb(248, 2, 2, 0.7);
                    }
                } else { //empty block colour
                    endColour = emptyColour;
                }

                // reduce the alpha value
                alpha -= 0.03;

                // If alpha reaches 0, clear the block
                if (alpha <= 0) {
                    stop();
                    paint();
                } else {
                    // Otherwise, calculate the colour the block should be at this moment.
                    var gc = getGraphicsContext2D();
                    Color currentColour = startColour.interpolate(endColour, 1.0 - alpha);
                    gc.clearRect(0, 0, width, height);
                    gc.setFill(currentColour);
                    gc.fillRect(0, 0, width, height);
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(0, 0, width, height);
                }
            }
        };

        // Start the animation
        fadeOut.start();
    }

    /**
     * Paint this canvas/block empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(emptyColour);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(0,0,width,height);

    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColour(Paint colour) {
        var gc = getGraphicsContext2D();

        // Clear
        gc.clearRect(0, 0, width, height);

        // Gradient fill
        Stop[] stops = new Stop[] {
                new Stop(0, Color.rgb(255, 255, 255, 0.8)),
                new Stop(1, (Color) colour)
        };
        LinearGradient gradient = new LinearGradient(0, 0, width, height, false, CycleMethod.NO_CYCLE, stops);
        gc.setFill(gradient);
        gc.fillRect(0, 0, width, height);

        // Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(0, 0, width, height);

        // Drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10);
        dropShadow.setOffsetX(5);
        dropShadow.setOffsetY(5);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
        setEffect(dropShadow);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    @Override
    public String toString() {
        //if toString is called, it will return a list of the information on this game block.
        return "GameBlock{" +
                "x=" + x +
                ", y=" + y +
                ", value= " + value.toString() +
                '}';
    }
}
