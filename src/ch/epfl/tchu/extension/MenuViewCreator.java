package ch.epfl.tchu.extension;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import ch.epfl.tchu.gui.ClientMain;
import ch.epfl.tchu.gui.GraphicalPlayerAdapter;
import ch.epfl.tchu.gui.ServerMain;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

import java.io.IOException;
import java.util.*;

import static ch.epfl.tchu.game.PlayerId.*;


public final class MenuViewCreator extends Application {

    private static final int NUMBER_PLAYER_SINGLE_GAME=2;
    private static final Font FONT = Font.font(55);
    private final static int DEFAULT_PORT = 5108;

    /**
     * main methode of the game (first to be launched
     * @param args : this arguments are not analyzed
     */
    public static void main(String[] args){
        launch(args);
    }

    /**
     * start methode of the JavaFx thread
     * @param primaryStage : primaryStage, not use
     */
    @Override
    public void start(Stage primaryStage) {
        createMenuView();
    }


    /**
     * We create the menu for our game that contains  :
     * Solo Mode : the user plays versus a Bot
     * Host : the user can be the host of the game, can choose the nr of players, and the names of the players
     * Join a Game : the user can join a game as a client, he must enter the ip and the port of the host
     * The Game start when all the players have join
     * The option to close the Game with "Exit"
     */
    public static void createMenuView(){
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.setTitle("Menu");
        Pane menu = new Pane();
        ImageView background = new ImageView();
        background.setImage(new Image("GameMenu.png"));


        //Solo Button
        Button btnSolo = new Button("Solo");
        btnSolo.setFont(FONT);
        btnSolo.setOnAction(event -> {
            Optional<String> stringOptional = getNameInputDialog("Please : enter the name you want to use in the game");
            if(stringOptional.isPresent()) {startSinglePlayerGame(stringOptional.get());  System.out.println("mode solo lancé");/*solo game started*/}
            else {System.out.println("Game start cancelled");}

        });

        //Host Button
        Button btnHost = new Button("Host");
        btnHost.setFont(FONT);
        btnHost.setOnAction(event -> {
            try {
                hostWindowsDialog();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //Join a Game Button
        Button btnJoin = new Button("Join a game");
        btnJoin.setFont(FONT);
        btnJoin.setOnAction(event -> joinWindowsDialog());

        //Exit Button
        Button btnExit = new Button("Exit");
        btnExit.setFont(FONT);
        btnExit.setOnAction(event -> stage.close());


        //All the Button together in the VBox
        VBox vBox = new VBox(30, btnSolo, btnHost,btnJoin, btnExit);
        vBox.setTranslateX(400);
        vBox.setTranslateY(200);
        menu.getChildren().addAll(background, vBox);

        Scene scene = new Scene(menu);

        stage.setScene(scene);
        stage.show();

    }

    /**
     * @param headerText : the headerText for the dialog
     * @return the dialog to ask the name that the user want to use for the game
     */
    private static Optional<String> getNameInputDialog(String headerText){
        TextInputDialog inDialog = new TextInputDialog("Player");
        inDialog.setTitle("Name");
        inDialog.setHeaderText(headerText);
        inDialog.setContentText("Username :");
        return inDialog.showAndWait();
    }

    /**
     * create a dialog for the host that enter the nbr of players for the game, the names of the players, and start the server if the dialog is correct and finish
     * @throws IOException when the method server of ServerMain throws an Exception
     */
    private static void hostWindowsDialog() throws IOException {

        boolean canStart = true;
        //ask the number of players
        ChoiceDialog<Integer> dialogWindow = new ChoiceDialog<>(2, List.of(2,3,4, 5));
        dialogWindow.setTitle("Make your choice");
        dialogWindow.setHeaderText("Number of player");
        dialogWindow.setContentText("How many player will play in this game:");

        Optional<Integer> result = dialogWindow.showAndWait();

        if (result.isPresent()){
            PlayerId.setNbrPlayer(result.get());
            //ask the names of the players
            List<String> playerNames = new ArrayList<>();
            Optional<String> stringFPOptional = getNameInputDialog("Please : enter the name you want to use in the game");
            if(stringFPOptional.isPresent()) playerNames.add(stringFPOptional.get());
            else {
                System.out.println("Game start cancelled");
                canStart=false;
            }
            for (PlayerId playerId: PlayerId.getAllPlayer()) {
                if(playerId == PLAYER_1) continue;
                Optional<String> stringOptional = getNameInputDialog(String.format("%s %s", "Please : enter the name of the Player " , PlayerId.getAllPlayer().indexOf(playerId) + 1));
                if(stringOptional.isPresent()) playerNames.add(stringOptional.get());
                else {
                    System.out.println("Game start cancelled");
                    canStart=false;
                }
            }
            //if all it's ok, we can start the server
            if(canStart){
                ServerMain.server(DEFAULT_PORT, playerNames);
            }

        }
    }

    /**
     * The dialog created when the user click on Join a Game,
     * The user must enter the ip and the port of the host to join a game
     */
    private static void joinWindowsDialog(){
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Join a game");
        dialog.setHeaderText("Please enter the ip address and the port the host has given you");


        ButtonType loginButtonType = new ButtonType("Join", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);


        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField port = new TextField();
        port.setPromptText("5108");
        TextField ip = new TextField();
        ip.setPromptText("localhost");

        grid.add(new Label("Port:"), 0, 0);
        grid.add(port, 1, 0);
        grid.add(new Label("Ip :"), 0, 1);
        grid.add(ip, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(port::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(port.getText(), ip.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            ClientMain.client(result.get().getKey(),result.get().getValue());
            System.out.println("mode multi lancé"); /*multi game started*/
        });
    }


    /**
     * start a Game of the user versus a Bot Player
     * @param playerName : the player name that the user want to use for the game
     */
    private static void startSinglePlayerGame(String playerName){
        PlayerId.setNbrPlayer(NUMBER_PLAYER_SINGLE_GAME);
        SortedBag<Ticket> tickets = SortedBag.of(ChMap.tickets());
        Map<PlayerId, String> names =
                Map.of(PLAYER_1, playerName, PLAYER_2, "Ordinateur");
        Map<PlayerId, Player> players =
                Map.of(PLAYER_1, new GraphicalPlayerAdapter(),
                        PLAYER_2, new BotPlayer());
        Random rng = new Random();
        new Thread(() -> Game.play(players, names, tickets, rng))
                .start();
    }


}
