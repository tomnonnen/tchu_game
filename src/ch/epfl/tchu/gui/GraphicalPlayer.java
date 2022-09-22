package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import java.util.List;
import java.util.Map;
import static javafx.application.Platform.isFxApplicationThread;


/**
 * A graphical interface for the user to interact with the game
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class GraphicalPlayer {

    private final ObservableGameState observableGameState;
    private final ListProperty<Text> lastInfos;
    private final ObjectProperty<ActionHandlers.DrawTicketsHandler> drawTicketsHandlerObjectProperty;
    private final ObjectProperty<ActionHandlers.DrawCardHandler> drawCardHandlerObjectProperty;
    private final ObjectProperty<ActionHandlers.ClaimRouteHandler> claimRouteHandlerObjectProperty;
    private final Stage mainWindow;
    private final static int NR_LAST_INFOS = 5;


    /**
     * constructor creat all the different part composing the interface and the Property with value null
     * @param correspondingPlayerId the id of the player to which the GUI belongs
     * @param playerNames the name of the players in the game
     */
    public GraphicalPlayer(PlayerId correspondingPlayerId, Map<PlayerId,String> playerNames){
        //creation of a new ObservableGameState belonging to the player
        this.observableGameState = new ObservableGameState(correspondingPlayerId);
        //creation of the list containing the last 5 infos received by the player
        ObservableList<Text> newValue = FXCollections.observableArrayList();
        this.lastInfos = new SimpleListProperty<>(newValue);

        //creation of the properties intended to contain the handlers of the 3 possible actions of the game (Drawing of ticket / Drawing of card/ Taking of road)
        this.drawTicketsHandlerObjectProperty = new SimpleObjectProperty<>();
        this.drawCardHandlerObjectProperty = new SimpleObjectProperty<>();
        this.claimRouteHandlerObjectProperty = new SimpleObjectProperty<>();

        //creation of the 3 main nodes constituting the graphic interface
        Node mapView = MapViewCreator.createMapView(observableGameState, claimRouteHandlerObjectProperty, this::chooseClaimCards); //node corresponding to the map (road, car, map...)
        Node cardsView = DecksViewCreator.createCardsView(observableGameState, drawTicketsHandlerObjectProperty, drawCardHandlerObjectProperty); //node corresponding to the cards and tickets that the player can take
        Node handView = DecksViewCreator.createHandView(observableGameState); //node corresponding to the cards and tickets that the player owns
        Node infoView = InfoViewCreator.createInfoView(correspondingPlayerId, playerNames, observableGameState, lastInfos); //node corresponding to the information of the player

        //creation of the main window
        this.mainWindow= new Stage();
        //The node corresponding to the card is placed in the center, corresponding to the cards and tickets that the player can take on the right, the one corresponding to the cards and tickets that the player has in base and the one corresponding to the information of the player on the left
        BorderPane mainPane = new BorderPane(mapView, null, cardsView, handView, infoView);
        this.mainWindow.setScene(new Scene(mainPane));
        this.mainWindow.setTitle(String.format("tChu \u2014 %s",playerNames.get(correspondingPlayerId)));//title of the window :  tchu + the name of the player
        this.mainWindow.show();
    }

    /**
     * create a window with the owner of the game window
     * @param title the title of the window to create
     * @return the created window(stage)
     */
    private Stage createMainStage(String title){
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.setTitle(title);
        stage.initOwner(this.mainWindow);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setOnCloseRequest(Event::consume);
        return stage;
    }

    /**
     * creates the window of choices corresponding to the tickets
     * @param possibleChoice the list of choices available to the player
     * @param chooseTicketsHandler the action to perform when validating the choices
     * @return the choice window
     */
    private Stage selectWindowTickets(SortedBag<Ticket> possibleChoice, ActionHandlers.ChooseTicketsHandler chooseTicketsHandler){
        Stage stage = createMainStage(StringsFr.TICKETS_CHOICE);
        VBox vBox = new VBox();
        Scene scene = new Scene(vBox);
        scene.getStylesheets().add("chooser.css");
        stage.setScene(scene);

        //creation of the graphic list and insertion of the possible choices in the graphic list of choices
        ObservableList<Ticket> observableList = FXCollections.observableArrayList(possibleChoice.toList());
        ListView<Ticket> choicesListView = new ListView<>(observableList);
        choicesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //creation of the button to validate the choice made by default it is disabled
        Button validateButton = new Button(StringsFr.CHOOSE);
        validateButton.disableProperty().set(true);

        //the button is activated when the number of selected items is equal to the total number available-2
        choicesListView.getSelectionModel().getSelectedItems().addListener((InvalidationListener) observable -> validateButton.disableProperty().set(choicesListView.getSelectionModel().getSelectedItems().size() < possibleChoice.size() - 2));

        //when the choices are validated the corresponding handler is called and the window is closed
        validateButton.setOnAction((a)-> {
            stage.hide();
            chooseTicketsHandler.onChooseTickets(SortedBag.of(choicesListView.getSelectionModel().getSelectedItems()));
        });

        //creation of the introduction text
        TextFlow textFlow = new TextFlow();
        Text introText = new Text(String.format(StringsFr.CHOOSE_TICKETS , possibleChoice.size()-2,StringsFr.plural(possibleChoice.size()-2)));
        textFlow.getChildren().add(introText);

        //adding all graphic elements to the selection window
        vBox.getChildren().addAll(textFlow,choicesListView,validateButton);

        return stage;
    }

    /**
     * creation of the choice window corresponding to the cards
     * @param introduction the introduction message presented to the player (prompt)
     * @param possibleChoices the list of choices available to the player
     * @param chooseCardsHandler the action to perform when validating the choices
     * @param emptySelectionPossible if it is possible to select no choice or if the player is obliged to select at least one element
     * @return the choice window
     */
    private  Stage selectWindowCard(String introduction, List<SortedBag<Card>> possibleChoices, ActionHandlers.ChooseCardsHandler chooseCardsHandler,boolean emptySelectionPossible){
        Stage stage = createMainStage(StringsFr.CARDS_CHOICE);
        VBox vBox = new VBox();
        Scene scene = new Scene(vBox);
        scene.getStylesheets().add("chooser.css");
        stage.setScene(scene);

        //creation of the graphic list and insertion of the possible choices in the graphic list of choices
        ListView<SortedBag<Card>> choicesListView = new ListView<>();
        choicesListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        choicesListView.setCellFactory(v -> new TextFieldListCell<>(new CardBagStringConverter()));
        choicesListView.getItems().addAll(possibleChoices);

        //creation of the button allowing to validate the choice made
        Button validateButton = new Button(StringsFr.CHOOSE);

        //the button is disabled if it is necessary to select at least one choice and no entry has been selected yet
        if(!emptySelectionPossible){
            validateButton.disableProperty().bind(choicesListView.getSelectionModel().selectedItemProperty().isNull());
        }

        //when the choices are validated the corresponding handler is called with an empty sortedBag if no element is selected otherwise the selected element and the window is closed
        validateButton.setOnAction((a)-> {
            stage.hide();
            if(choicesListView.getSelectionModel().getSelectedItem()==null){
                chooseCardsHandler.onChooseCards(SortedBag.of());
            }
            else{
                chooseCardsHandler.onChooseCards(choicesListView.getSelectionModel().getSelectedItem());
            }

        });

        //creation of the introduction text
        TextFlow textFlow = new TextFlow();
        Text introText = new Text(introduction);
        textFlow.getChildren().add(introText);

        //adding all graphic elements to the selection window
        vBox.getChildren().addAll(textFlow,choicesListView,validateButton);
        return stage;
    }

    /**
     * class converting the card sorted bag to a displayable string
     * we redefined the text of the card names and throw an error if the fromString methode is called
     */
    private static class CardBagStringConverter extends StringConverter<SortedBag<Card>> {

        @Override
        public String toString(SortedBag<Card> cards) {
            return Info.getNamesCards(cards);
        }

        @Override
        public SortedBag<Card> fromString(String string) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * allows to update the game state
     * @param publicGameState the new public state of the game
     * @param playerState the new private state of the player to which the class (GraphicalPlayer) belongs
     */
    public void setState(PublicGameState publicGameState, PlayerState playerState){
        assert isFxApplicationThread();
        observableGameState.setState(publicGameState, playerState);
    }

    /**
     * allows to display an information to the player to which the class belongs (GraphicalPlayer)
     * @param info the information in the form of a string to display
     */
    public void receiveInfo(String info){
        assert isFxApplicationThread();
        lastInfos.get().add(new Text(info)); //adding the received info to the list containing them and being displayed on the player's GUI
        if(lastInfos.size()>NR_LAST_INFOS){lastInfos.get().remove(0);} //only the last 5 infos should be kept
    }

    /**
     * method called at the beginning of a turn allowing the player to perform one of the three possible actions in the game of tchu (Draw ticket / Draw card / Take road)
     * @param drawTicketsHandler the handler containing the actions to perform in case of ticket draw
     * @param drawCardHandler the handler containing the actions to perform when drawing cards
     * @param claimRouteHandler the handler containing the actions to perform when a route is taken
     */
    public void startTurn(ActionHandlers.DrawTicketsHandler drawTicketsHandler, ActionHandlers.DrawCardHandler drawCardHandler, ActionHandlers.ClaimRouteHandler claimRouteHandler){
        assert isFxApplicationThread();
        ActionHandlers.DrawTicketsHandler drawTicketsHandlerSpecific = ()->{
            emptyHandlerObjectProperty();
            drawTicketsHandler.onDrawTickets();
        };
        ActionHandlers.DrawCardHandler drawCardHandlerSpecific = (index)->{
            emptyHandlerObjectProperty();
            drawCardHandler.onDrawCard(index);
        };
        ActionHandlers.ClaimRouteHandler claimRouteHandlerSpecific = (route,cards)->{
            emptyHandlerObjectProperty();
            claimRouteHandler.onClaimRoute(route,cards);
        };
        drawCardHandlerObjectProperty.set(observableGameState.canDrawCards() ? drawCardHandlerSpecific : null);
        drawTicketsHandlerObjectProperty.set(observableGameState.canDrawTickets() ? drawTicketsHandlerSpecific : null);
        claimRouteHandlerObjectProperty.set(claimRouteHandlerSpecific);
    }

    /**
     * allows the player to choose tickets by opening a choice window
     * @param ticketsToChooseFrom the list of tickets available to the player
     * @param chooseTicketsHandler the handler containing the actions to be performed after the validation of the chosen tickets
     */
    public void chooseTickets(SortedBag<Ticket> ticketsToChooseFrom, ActionHandlers.ChooseTicketsHandler chooseTicketsHandler){
        assert isFxApplicationThread();
        selectWindowTickets(ticketsToChooseFrom,chooseTicketsHandler).show();
    }

    /**
     * allows the player to draw a card by adding the corresponding handler
     * @param drawCardHandler the handler containing the actions to be performed when cards are drawn
     */
    public void drawCard(ActionHandlers.DrawCardHandler drawCardHandler){
        assert isFxApplicationThread();
        ActionHandlers.DrawCardHandler drawCardHandlerSpecific = (index)->{
            emptyHandlerObjectProperty();
            drawCardHandler.onDrawCard(index);
        };
        drawCardHandlerObjectProperty.set(drawCardHandlerSpecific);
    }

    /**
     * allows the player to choose cards (to take a route) by opening a choice window
     * @param possibleClaimCards the list of cards available to the player
     * @param chooseCardsHandler the handler containing the actions to perform after the validation of the chosen cards
     */
    public void chooseClaimCards(List<SortedBag<Card>> possibleClaimCards, ActionHandlers.ChooseCardsHandler chooseCardsHandler){
        assert isFxApplicationThread();
        selectWindowCard(StringsFr.CHOOSE_CARDS,possibleClaimCards,chooseCardsHandler,false).show();
    }

    /**
     * allows the player to choose additional cards (to take a route) by opening a choice window
     * @param possibleAdditionalCards the list of cards available to the player
     * @param chooseCardsHandler the handler containing the actions to perform after the validation of the chosen cards
     */
    public void chooseAdditionalCards(List<SortedBag<Card>> possibleAdditionalCards, ActionHandlers.ChooseCardsHandler chooseCardsHandler){
        assert isFxApplicationThread();
        selectWindowCard(StringsFr.CHOOSE_ADDITIONAL_CARDS,possibleAdditionalCards,chooseCardsHandler,true).show();
    }

    /**
     * empty all player handlers => disable all actions
     */
    private void emptyHandlerObjectProperty(){
        assert isFxApplicationThread();
        drawTicketsHandlerObjectProperty.set(null);
        drawCardHandlerObjectProperty.set(null);
        claimRouteHandlerObjectProperty.set(null);
    }
}
