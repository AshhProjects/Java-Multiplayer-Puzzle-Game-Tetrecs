package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * The Block Hovered listener is used to handle the event when a block in a GameBoard is hovered. It passes the
 * GameBlock that was hovered in the message
 */

public interface BlockHoveredListener {

    /**
     * Handle a block hover event
     * @param block the block that was hovered over
     */
    public void blockHovered(GameBlock block);

}
