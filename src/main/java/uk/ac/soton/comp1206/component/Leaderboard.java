package uk.ac.soton.comp1206.component;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The list of the multiplayer users and their scores. Extends ScoresList.
 * Updated dynamically.
 */
public class Leaderboard extends ScoresList {

    /**
     * Logger used to log for this class.
     */
    private static final Logger logger = LogManager.getLogger(Leaderboard.class);

    /**
     * The list of users with their name and if they're dead.
     */
    private final SimpleListProperty<Pair<String, Boolean>> usersDead;

  /**
   * The (arraylist) list of the users and whether they're dead or not.
   */
  private final ArrayList<Pair<String, Boolean>> usersDeadList;

    /**
     *
     * @param max the max amount of users on this leaderboard to display.
     */
    public Leaderboard(int max) {
        super(max);
        //Creates a list of all the users and if they're dead or not.
        usersDead = new SimpleListProperty<>();
        usersDeadList = new ArrayList<>();

        //adds a listener to detect if there was a change to usersDead list, as this is binded
        //to the multiplayer game.
        usersDead.addListener((observable, oldValue, newValue) -> {
            if (!usersDead.isEmpty()) {

                //clears usersDeadList, as it will be replaced with the new usersDead list.
                usersDeadList.clear();
                usersDeadList.addAll(usersDead);

                //runs the isDead method which will strike the names of any dead users.
                isDead();
            }
        });
    }

    /**
     * Reveals the scores as soon as it's updated.
     */
    @Override
    protected void revealAtEnd() {
        reveal();
    };

    /**
     * Checks whether each user is dead, and strikethrough their name if so.
     */
    protected void isDead() {
        //creates a hash map so that way it's easier, faster and more efficient to find a user
        Map<String,Boolean> userIsDeadMap = new HashMap<>();
        for (Pair<String, Boolean> user : usersDeadList) {
            userIsDeadMap.put(user.getKey(), user.getValue());
        }

        //for each node in the children of this leaderboard:
        for (Node node : getChildren()) {
            if (node instanceof Text theUser) {
                String playerName = theUser.getText().split(":")[0];

                /*it will get the username of the text component, and set isAlive to whether
                they're dead or not (as according to the usersDead list).
                If for some reason they're not on the list, it will default to true that they
                are alive.*/
                Boolean isAlive = userIsDeadMap.getOrDefault(playerName,true);

                //If the user is dead, it will strike through the text component.
                theUser.setStrikethrough(!isAlive);
            }

        }
    }
    public SimpleListProperty<Pair<String,Boolean>> usersDeadProperty() {
        return usersDead;
    }

}
