package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.Set;

/**
 * The Line Cleared listener is used to handle the event when a line in a grid is cleared. It passes the
 * set of game blocks that were cleared.
 */
public interface LineClearedListener {

    /**
     * Handle a line clear event
     * @param clearedBlocks the blocks that were cleared.
     */
    void onLineCleared(Set<GameBlockCoordinate> clearedBlocks);
}
