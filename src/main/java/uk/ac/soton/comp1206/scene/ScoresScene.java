    package uk.ac.soton.comp1206.scene;

    import javafx.application.Platform;
    import javafx.beans.property.BooleanProperty;
    import javafx.beans.property.SimpleBooleanProperty;
    import javafx.beans.property.SimpleListProperty;
    import javafx.beans.value.ChangeListener;
    import javafx.collections.FXCollections;
    import javafx.geometry.Insets;
    import javafx.geometry.Pos;
    import javafx.scene.control.TextField;
    import javafx.scene.input.KeyCode;
    import javafx.scene.layout.*;
    import javafx.scene.text.Text;
    import javafx.util.Pair;
    import org.apache.logging.log4j.LogManager;
    import org.apache.logging.log4j.Logger;
    import uk.ac.soton.comp1206.component.ScoresList;
    import uk.ac.soton.comp1206.event.CommunicationsListener;
    import uk.ac.soton.comp1206.network.Communicator;
    import uk.ac.soton.comp1206.ui.GamePane;
    import uk.ac.soton.comp1206.ui.GameWindow;
    import java.io.*;
    import java.util.ArrayList;

    /**
     * The scores scene, used to display the local scores/leaderboard (if multiplayer) and the online scores.
     */
    public class ScoresScene extends BaseScene {

        private static final Logger logger = LogManager.getLogger(ScoresScene.class);

        /**
         * The score of the user from the game.
         */
        private final int userScore;

        /**
         * the local scores list property, read in from a local file
         */
        public SimpleListProperty<Pair<String,Integer>> localScores;

        /**
         * The online scores list property, read in from the server.
         */
        public SimpleListProperty<Pair<String,Integer>> remoteScores;

        /**
         * The leaderboard scores list property, taken from a multiplayer game.
         */
        public SimpleListProperty<Pair<String,Integer>> leaderboardScores;


        /**
         * Decides if the local scores is ready to be displayed.
         */
        private final BooleanProperty isLocalScoresReady = new SimpleBooleanProperty(false);

        /**
         * Decides if the online scores is ready to be displayed.
         */
        private final BooleanProperty isOnlineScoresReady = new SimpleBooleanProperty(false);

        /**
         * Decides if the game that was played was a multiplayer game or a singleplayer game.
         */
        private final boolean isMultiplayer;


        /**
         * The max amount of users to display on the scores screen.
         */
        private final int maxUserAmount = 12;


        /**
         * Constructor to be called if the finished game was a multiplayer game.
         * @param gameWindow the game window this scores scene belongs to.
         * @param leaderboard the list of scores from all the users from the multiplayer game.
         */
        public ScoresScene(GameWindow gameWindow, SimpleListProperty<Pair<String,Integer>> leaderboard) {
            super(gameWindow);
            isMultiplayer = true;
            userScore = 0;
            leaderboardScores = leaderboard;
            logger.info("Creating Scores Scene (multiplayer)");
        }

        /**
         * Consturctor to be called if the finished game was a single playe game
         * @param score The score of the user from the single player game.
         * @param gameWindow the game window this scores scene belongs to.
         */
        public ScoresScene(int score, GameWindow gameWindow) {
            super(gameWindow);
            isMultiplayer = false;
            userScore = score;
            logger.info("Creating Scores Scene");
        }

        /**
         * This is used to detect if the user presses escape or backspace, to go back to the menu.
         */
        @Override
        public void initialise() {
            //If the user wants to go back to the main menu.
            gameWindow.getScene().setOnKeyPressed((e) -> {
                KeyCode key = e.getCode();
                if (key == KeyCode.ESCAPE || key == KeyCode.BACK_SPACE) {
                    gameWindow.startMenu();
                }
            });
        }

        /**
         *
         * @param background the style the stack pane should be set to.
         * @return a stack pane the size of the window
         */
        protected StackPane background(String background) {
            var challengePane = new StackPane();
            challengePane.setMaxWidth(gameWindow.getWidth());
            challengePane.setMaxHeight(gameWindow.getHeight());
            challengePane.getStyleClass().add(background);
            return challengePane;
        }

        /**
         * Builds the UI for the scores scene.
         */
        @Override
        public void build() {

            //If the game is not a multiplayer game, it will display the local scores list.
            if (!isMultiplayer) {
                ArrayList<Pair<String,Integer>> arrayListLocalScores = new ArrayList<>();
                localScores = new SimpleListProperty<>(FXCollections.observableArrayList(arrayListLocalScores));
            } else {
                /*otherwise if it's a multiplayer game, it will just set the localScores to
                leaderboardScores, to prevent code repetition.*/
                localScores = leaderboardScores;
            }

            ArrayList<Pair<String,Integer>> arrayListOnlineScores = new ArrayList<>();
            remoteScores = new SimpleListProperty<>(FXCollections.observableArrayList(arrayListOnlineScores));

            logger.info("Building " + this.getClass().getName());

            root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

            /*Sets up 3 layers, bottom is the background image, second is the filter, top is for
            the components.*/
            var background  = background("empty-screen");
            var backgroundEffect = background("game-background-effect");
            var scoresPane = background("");

            root.getChildren().addAll(background,backgroundEffect,scoresPane);
            var mainPane = new BorderPane();
            scoresPane.getChildren().add(mainPane);

            VBox topCenterBox = new VBox();
            topCenterBox.setAlignment(Pos.CENTER);
            topCenterBox.setPadding(new Insets(20));
            HBox.setHgrow(topCenterBox, Priority.ALWAYS);

            // creates Text for game over message
            Text gameOverText = new Text("Game Over");
            gameOverText.getStyleClass().add("bigtitle");

            // create the label for "high scores".
            Text highScoresText = new Text("High Scores");
            highScoresText.getStyleClass().add("title");

            /*Creates 2 scoresList components which will be used to display the local scores, or
            the online scores.*/
            ScoresList localScoresList = new ScoresList(maxUserAmount);
            ScoresList onlineScoresList = new ScoresList(maxUserAmount);

            /*The labels for those scores. Initially it sets the label for the local scores to
            "multiplayer scores" but later in an if statement, if it's not a mutiplayer game,
            it will be set to "local scores"*/
            Text localScoresLabel;
            localScoresLabel = new Text("Multiplayer Scores");
            localScoresLabel.getStyleClass().add("heading");
            Text onlineScoresLabel = new Text("Online Scores");
            onlineScoresLabel.getStyleClass().add("heading");

            VBox localScoresBox = new VBox(localScoresLabel,localScoresList);
            localScoresBox.setAlignment(Pos.TOP_CENTER);
            VBox onlineScoresBox = new VBox(onlineScoresLabel,onlineScoresList);
            onlineScoresBox.setAlignment(Pos.TOP_CENTER);

            //binds the scores to the lists of scores. So when those lists change, the scores do too.
            localScoresList.scoresProperty().bind(localScores);
            onlineScoresList.scoresProperty().bind(remoteScores);

            HBox bothScores = new HBox(localScoresBox,onlineScoresBox);
            bothScores.setAlignment(Pos.CENTER);
            bothScores.setSpacing(100);

            //Loads the scores from the server.
            loadOnlineScores();

            // add ImageView and Text to top center VBox
            topCenterBox.getChildren().addAll(gameOverText,highScoresText);
            topCenterBox.setSpacing(20);
            topCenterBox.setPadding(new Insets(100,0,0,0));

            //If it's a multiplayer game, it's ready to display the scores.
            if (isMultiplayer) {
                isLocalScoresReady.set(true);
            }
            //otherwise if it's not a multiplayer game, it has to read the scores from a local file.
            if (!isMultiplayer) {

                //changes the label's text to "local scores"
                localScoresLabel.setText("Local Scores");

                File file = new File("scores.txt");
                String fullFilepath = file.getAbsolutePath();
                //if the file does not exist, it will create it.
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                        logger.info("creating new file {}", fullFilepath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                logger.info("loading scores {}",fullFilepath);

                //it will load the scores from the local file.
                loadScores(fullFilepath);

                //If the local scores is 0, so no scores were found, it will add default scores.
                if (localScores.size() == 0) {
                    logger.info("local Scores is 0, adding default to local scores");
                    localScores.add(new Pair<>("Joe",1000));
                    localScores.add(new Pair<>("Bob",700));
                    localScores.add(new Pair<>("Honzo",500));
                    localScores.add(new Pair<>("Derek",400));
                    localScores.add(new Pair<>("Bogo",300));
                    localScores.add(new Pair<>("lobster",200));
                    localScores.add(new Pair<>("wow",90));
                    localScores.add(new Pair<>("user3",80));
                    localScores.add(new Pair<>("user2",50));
                    localScores.add(new Pair<>("user1",20));

                    //it then writes these to the local file.
                    writeScores(fullFilepath);
                }

                /*If the user's score is higher than the lowest score read from the file,
                it will prompt the user to enter their own score.*/
                if (userScore > localScores.get(localScores.size()-1).getValue()) {
                    logger.info("user score is larger than lowest score in local score list");
                    //hides both scores, as to let the user enter their score.
                    bothScores.setVisible(false);

                    //creates a text field where the user will enter their name.
                    TextField enterName = new TextField("enter your username");
                    enterName.setMaxWidth(gameWindow.getWidth());
                    topCenterBox.getChildren().add(enterName);

                    //When the name is entered, it will write it to the score list.
                    enterName.setOnAction(event -> {
                        String username = enterName.getText();

                        //if the user does not enter a name, it will write it as "default".
                        if (username.equals("")) {
                            username = "default";
                        }
                        logger.info("Username entered: {}", username);

                        // add the new score to the list and write to file
                        localScores.add(new Pair<>(username, userScore));

                        //sorts the score before it's displayed or written.
                        localScores.sort((ONE, TWO) -> TWO.getValue().compareTo(ONE.getValue()));

                        //it sends the score + the player's name to the server
                        writeOnlineScore(username,userScore);
                        //it then writes the score to the local list too.
                        writeScores(fullFilepath);

                        // hide the text field and show the full scores
                        logger.info("finished writing, showing the scores:");
                        enterName.setVisible(false);
                        topCenterBox.getChildren().remove(enterName);
                        isLocalScoresReady.set(true);
                        bothScores.setVisible(true);
                    });
                } else {
                    //if the user score is not bigger than the lowest score in the list, it won't
                    //bother asking the user to submit themselves.
                    isLocalScoresReady.set(true);
                }

            }

            /*it adds both scores to the screen after everything above, so that the 'enter name
            box' isn't below both scores, when both scores are empty.*/
            topCenterBox.getChildren().add(bothScores);

            // add top center VBox to main BorderPane
            mainPane.setTop(topCenterBox);

            /*Listens to if the local scores and online scores are both ready to be displayed,
            then displays them.*/
            ChangeListener<Boolean> changeListener = (observable, oldValue, newValue) -> {
                if (isLocalScoresReady.get() && isOnlineScoresReady.get()) {
                    logger.info("both scores are ready");
                    localScoresList.reveal();
                    onlineScoresList.reveal();
                }
            };

            //adds a listener to both these boolean so when they get changed, it runs the code
            //to show the scores.
            isLocalScoresReady.addListener(changeListener);
            isOnlineScoresReady.addListener(changeListener);
        }

        /**
         *
         * @param filename the name of the file to load the scores from.
         */
        public void loadScores(String filename) {

            logger.info("loading Score text file {}",filename);

            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("reading line {}",line);
                    String[] parts = line.split(":",2);
                    String name = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    localScores.add(new Pair<>(name, score));
                }
            } catch (Exception e) {
                logger.error("Invalid filename when loading scores");
                logger.error(e.getMessage());
            }
        }

        /**
         * Reads the scores from the server.
         * Reads HISCORES UNIQUE to display only the unique names in the high score list from the server.
         */
        public void loadOnlineScores() {
            Communicator communicator = gameWindow.getCommunicator();
            communicator.send("HISCORES UNIQUE");

            //adds a listener to the communicator for when a message is received.
            communicator.addListener(message -> {

                //if the message received tells the high scores.
                if (message.startsWith("HISCORES")) {
                    Platform.runLater(() -> {
                        // Parse scores from message
                        String[] lines = message.substring(9).split("\n");

                        /*Only looks at the top users up to the maximum amount, as to not add
                        //everyone to the list.*/
                        for (int i = 0; i <= maxUserAmount-1; i++) {
                            String[] parts = lines[i].split(":",2);
                            String name = parts[0];

                            /*Some scores from the server were "NaN" so this is to prevent
                            that, as error handling.*/
                            try {
                                int score = Integer.parseInt(parts[1]);
                                logger.info("adding in {} {}",name,score);
                                remoteScores.add(new Pair<>(name, score));
                            } catch (Exception e) {
                                logger.info("score was NaN");
                            }
                        }
                        isOnlineScoresReady.set(true);
                        // Clear listener
                        communicator.clearListeners();
                    });
                }
            });
        }

        /**
         * Writes the score to the local file.
         * @param filepath the file to write the scores to.
         */
        public void writeScores(String filepath) {

            try (PrintWriter writer = new PrintWriter(new File(filepath))) {
                logger.info("saving to {}",filepath);
                // write each score to the file in the name:score format
                for (Pair<String,Integer> score : localScores) {
                    logger.info("writing {}{}",score.getKey(),score.getValue());
                    writer.println(score.getKey() + ":" + score.getValue());
                }
            } catch (FileNotFoundException e) {
                // handle file not found exception
                e.printStackTrace();
            }
        }

        /**
         * Sends the score to the server.
         * @param username the username the score is for.
         * @param highScore the score that is going to be sent to the server.
         */
        public void writeOnlineScore(String username, int highScore) {
            Communicator communicator = gameWindow.getCommunicator();

            //sends the user's score and username to the server.
            String stringHighScore = String.valueOf(highScore);
            communicator.send("HISCORE " + username + ":" + stringHighScore);
        }
    }