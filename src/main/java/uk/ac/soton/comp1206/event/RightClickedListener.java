package uk.ac.soton.comp1206.event;

/**
 * The Right Clicked listener is used to handle the event when the GameBoard is right clicked. It passes the
 * GameBlock that was clicked in the message.
 */
public interface RightClickedListener {

    /**
     * Handle a block clicked event
     */
    public void RightClicked();
}
