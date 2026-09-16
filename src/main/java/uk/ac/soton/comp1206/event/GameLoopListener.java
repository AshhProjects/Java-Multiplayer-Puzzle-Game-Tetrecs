package uk.ac.soton.comp1206.event;

/**
 * The Game Loop listener is used to handle the event when the game loops (when the game timer ends).
 */
public interface GameLoopListener {

    /**
     * Handle on game loop event.
     */
    void onGameLoop();

}
