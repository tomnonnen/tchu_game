package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.PlayerId;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


import java.util.Map;

/**
 * the part of the graphic display the actual state of the players playing and the last 5 info got by the player
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
abstract class InfoViewCreator {

    private static final int CIRCLE_RADIUS = 5;

    /**
     * creat the view composed of the stats of the player and the infos of the game got by the player
     * @param ownId : the ownId of the player that see the interface
     * @param playerNames : the names of the players
     * @param observableGameState : the observable state of the game
     * @param infos : the ListProperty that contains the infos that we bind to the Text
     * @return the main node of the view (a Vbox)
     */
    public static Node createInfoView(PlayerId ownId, Map<PlayerId, String> playerNames, ObservableGameState observableGameState, ObservableList<Text> infos){
        VBox vBox = new VBox();
        vBox.getStylesheets().addAll("info.css", "colors.css");
        Separator separator = new Separator(Orientation.HORIZONTAL);

        VBox playerStats = new VBox();
        playerStats.setId("player-stats");

        addTextFlow(playerNames, ownId, observableGameState, playerStats); //we add first the Id of the player that see
        for(PlayerId playerN : PlayerId.getAllPlayer()){ //then we add the others players
            if(!playerN.equals(ownId))
                addTextFlow(playerNames, playerN, observableGameState, playerStats);
        }

        TextFlow gameInfo = new TextFlow();
        gameInfo.setId("game-info");
        for(Text message : infos){
            gameInfo.getChildren().add(message);
        }
        Bindings.bindContent(gameInfo.getChildren(), infos);
        vBox.getChildren().addAll(playerStats, separator, gameInfo);
        return vBox;
    }

    /**
     * add a textFlow containing the current statistic of the player to the graphic interface
     * @param playerNames : the names of the players
     * @param playerN : the player id N
     * @param observableGameState : the observable state of the game
     * @param playerStats : the VBox where we add the textFlow
     */
    private static void addTextFlow(Map<PlayerId, String> playerNames, PlayerId playerN, ObservableGameState observableGameState, VBox playerStats){
        TextFlow textFlow = new TextFlow();
        textFlow.getStyleClass().add(playerN.name());
        Circle circle = new Circle(CIRCLE_RADIUS);
        circle.getStyleClass().add("filled");
        Text text = new Text();
        text.textProperty().bind(Bindings.format(StringsFr.PLAYER_STATS, playerNames.get(playerN), observableGameState.nbrTicketInHand(playerN), observableGameState.nbrCardInHand(playerN), observableGameState.nbrWagonPlayer(playerN), observableGameState.nbrPointObtained(playerN)));
        textFlow.getChildren().addAll(circle, text);
        playerStats.getChildren().add(textFlow);
    }

}
