package uk.ac.soton.comp1206.component;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**

 * displays a list of scores in descending order.
 * Scores are represented as pairs of names and integers. The component has a maximum number
 * of scores that can be displayed. The list is updated as new scores are added.
 */
public class ScoresList extends VBox {

    private static final Logger logger = LogManager.getLogger(ScoresList.class);

    /**
     * The scores list property this scoresList UI element is bound to.
     */
    private final SimpleListProperty<Pair<String,Integer>> scores;

    /**
     * The ordered version of the scores list.
     */
	private final ArrayList<Pair<String,Integer>> scoresOrdered;

    /**
     *
     * @param max the max amount of users to display on this scoresList UI object.
     */
    public ScoresList(int max) {
        scores = new SimpleListProperty<>();
        setAlignment(Pos.CENTER);
        scoresOrdered = new ArrayList<>();

        //adds a listener to the scores, for if it was changed.
        scores.addListener((observable, oldValue, newValue) -> {
            int i = 0;
            //if the scores is not empty:
            if (!scores.isEmpty()) {
                //it will clear scoresOrdered, as it will be replaced with scores.
                scoresOrdered.clear();
                //it adds all the elements from scores to scoresOrdered.
			    scoresOrdered.addAll(scores);
                //it then orders the score.
                scoresOrdered.sort((ONE, TWO) -> TWO.getValue().compareTo(ONE.getValue()));
                //clears all the children as they will be replaced by the new scores list.
                getChildren().clear();

                for (Pair<String,Integer> score : scoresOrdered) {
                    i++;
                    Text user = new Text(score.getKey() + ": " + score.getValue().toString());

                    //This is to create a white to gray gradient from the top to the bottom.
                    double scoreFraction = (i / (double) max);
                    Color scoreColor = Color.WHITE.interpolate(Color.RED, scoreFraction);
                    user.setFill(scoreColor);
                    user.getStyleClass().add("scorelist");
                    user.setOpacity(0);
                    getChildren().add(user);

                    //if it reaches the max amount of scores to display, it will stop.
                    if (i == max) {
                        break;
                    }
                }
                /*Reveal immediately (if it's a leaderboard) or does nothing, and can be ignored.
                In this case it can be ignored.*/
                revealAtEnd();
            }

        });
    }

	public SimpleListProperty<Pair<String,Integer>> scoresProperty() {
	  return scores;
	}

    /**
     * Whether to reveal instantly as soon as the scores is created. In this case, no.
     */
    protected void revealAtEnd() {
        //used for leaderboard, gets overridden.
    };

    /**
     * Plays the animation to reveal each score one by one.
     */
    public void reveal() {
        //creates an animation to show each score.
        Timeline timeline = new Timeline();

        //This animation is so each element is displayed from very fast to slower.
        int i = 1;
        for (Node scoreBox : getChildren()) {
            KeyValue opacityValue = new KeyValue(scoreBox.opacityProperty(), 1);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(100 * i), opacityValue);
            timeline.getKeyFrames().add(keyFrame);
            i++;
        }
        timeline.play();
    }
}




