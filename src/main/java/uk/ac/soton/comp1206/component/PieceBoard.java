package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * This is the PieceBoard, which will be the board representing a singular piece.
 * This will be used in the instructions screen to show all the pieces, as well as
 * In the game to show the next piece.
 */
public class PieceBoard extends GameBoard {

    private static final Logger logger = LogManager.getLogger(PieceBoard.class);

    /**
     * The piece that is on display for this piece-board.
     */
    private GamePiece piece;

    /**
     * The grid object this board is assigned to.
     */
    final Grid grid;

    /**
     * Sets up the piece board.
     * @param grid the grid this board is assigned to.
     * @param width The width of this board.
     * @param height The height of this board.
     */
    public PieceBoard(Grid grid, double width, double height) {
        super(grid, width, height);
        this.grid = grid;
    }


    /**
     * This sets the board to display the piece that is fed into it.
     * @param piece the piece that will be set to this board.
     */
    public void setPiece(GamePiece piece) {
        this.piece = piece;
        clearBlocks();
        updateBlocks();
    }

    /**
     * This clears the board, ready to display a new piece.
     */
    public void clearBlocks() {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                grid.set(x, y, 0);
            }
        }
    }

    /**
     * For a Piece Board, if it gets left-clicked on, it should rotate the piece.
     * Overrides the "right click on" that the game board had.
     */
    @Override
    public void rotationHandler() {
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                RightClicked(event);
            }
        });
        blocks[1][1].setCenter();
    }

    /**
     * This goes through the grid, setting it to display the current piece.
     */
    public void updateBlocks() {
        var shape = piece.getBlocks();
        var colour = piece.getValue();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (!(shape[x][y] == grid.get(x,y))) {
                    grid.set(x,y,colour);
                }
            }
        }
    }
}


