package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.Constants;
import ch.epfl.tchu.game.Ticket;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;


import static ch.epfl.tchu.game.Card.LOCOMOTIVE;
import static ch.epfl.tchu.game.Constants.DECK_SLOT;

/**
 * the class that handle the view of the deck, the card in hand from the player and the ticket of the player
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
abstract class DecksViewCreator {

    private final static int WIDTH_RECTANGLE_IMAGE = 40;
    private final static int HEIGHT_RECTANGLE_IMAGE = 70;

    private final static int WIDTH_RECTANGLE_GAUGE = 50;
    private final static int HEIGHT_RECTANGLE_GAUGE = 5;

    private final static int WIDTH_OUTSIDE_CARD_RECTANGLE = 60;
    private final static int HEIGHT_OUTSIDE_CARD_RECTANGLE = 90;

    /**
     * creat the view of the card in hand from the player and the ticket of the player
     * @param observableGameState : the subject that are the state of the game that we observe with the observers
     * @return the current view of the hand
     */
    public static HBox createHandView(ObservableGameState observableGameState){
        HBox mainView = new HBox();
        mainView.getStylesheets().addAll("decks.css", "colors.css");

        /*
          redefinition of the class managing the display of the elements of a list view
         */
        class FormattedCell extends ListCell<Ticket> {
            @Override
            protected void updateItem(Ticket ticket, boolean empty) {
                super.updateItem(ticket, empty);
                if(ticket !=null || empty){
                    setGraphic(null);
                    setText(null);
                }
                if(ticket!=null){
                    //if the ticket is not null => we give the name of the ticket append to the point that the ticket actually give
                    setText(ticket+"\t\t:\t"+observableGameState.ticketsPlayerPoints().get(ticket));
                    //if the ticket was completed(positive points) => the text become Green else he stay black
                    setTextFill(observableGameState.ticketsPlayerPoints().get(ticket)<0 ? Color.BLACK:Color.GREEN);
                }
            }
        }

        //the tickets that the player of the observableGameState own
        ListView<Ticket> tickets = new ListView<>(observableGameState.ticketPlayer());
        tickets.setCellFactory(param -> new FormattedCell());//the display of the ticket is determined by the FormattedCell class see above for its definition


        tickets.setId("tickets");

        HBox hand_pane = new HBox();
        hand_pane.setId("hand-pane");

        //all the card type are add to the hand of the player
        for(Card card : Card.ALL){
            StackPane stackPane = new StackPane();
            stackPane.getStyleClass().addAll("card", card.equals(LOCOMOTIVE) ? "NEUTRAL" : card.name());

            //rectangles
            rectanglesDefinition(stackPane);
            Rectangle imageRectangle = new Rectangle(WIDTH_RECTANGLE_IMAGE,HEIGHT_RECTANGLE_IMAGE);
            imageRectangle.getStyleClass().add("train-image");

            //Listener
            ReadOnlyIntegerProperty count = observableGameState.nbrOfCardTypeInHand(card);
            stackPane.visibleProperty().bind(Bindings.greaterThan(count, 0));

            Text countText = new Text();
            countText.getStyleClass().add("count");
            countText.textProperty().bind(Bindings.convert(count)); //the text is actualized when the player use a card or get one card of the corresponding type
            countText.visibleProperty().bind(Bindings.greaterThan(count, 1));//only visible if the player own more than one of the corresponding card

            stackPane.getChildren().addAll(imageRectangle, countText);
            hand_pane.getChildren().add(stackPane);
        }

        mainView.getChildren().addAll(tickets, hand_pane);

        return mainView;
    }

    /**
     * creat the view of the cards/tickets that the player can get (face up card) and deck
     * @param observableGameState : the subject that are the state of the game that we observe with the observers
     * @param drawTicketsHandler : action manager that manages the ticket draw
     * @param drawCardHandler : action manager that manages the card draw
     * @return the current view of the cards
     */
    public static Node createCardsView(ObservableGameState observableGameState, ObjectProperty<ActionHandlers.DrawTicketsHandler> drawTicketsHandler, ObjectProperty<ActionHandlers.DrawCardHandler> drawCardHandler){
        VBox cardPane = new VBox();
        cardPane.setId("card-pane");
        cardPane.getStylesheets().addAll("decks.css", "colors.css");

        Button ticketDeck = new Button(StringsFr.TICKETS);  //creation of the button corresponding to the ticket deck
        ticketDeck.getStyleClass().add("gauged");
        ticketDeck.setGraphic(gauge(observableGameState.ticketPercentageRemaining()));
        cardPane.getChildren().add(ticketDeck);
        ticketDeck.disableProperty().bind(drawTicketsHandler.isNull()); //only activate when the ticket handler is filled -> possible to draw a card
        ticketDeck.setOnAction((a)-> drawTicketsHandler.get().onDrawTickets());

        for(int slot: Constants.FACE_UP_CARD_SLOTS) {
            StackPane cardStackPane = new StackPane();
            cardStackPane.getStyleClass().add(0,"card");
            cardStackPane.getStyleClass().add(1,"NEUTRAL"); //by default the card type is not specified at the beginning

            observableGameState.faceUpCard(slot).addListener((a, b, observable)-> cardStackPane.getStyleClass().set(1, observable == LOCOMOTIVE ? "NEUTRAL" : observable.name()));

            rectanglesDefinition(cardStackPane);
            Rectangle imageRectangle = new Rectangle(WIDTH_RECTANGLE_IMAGE,HEIGHT_RECTANGLE_IMAGE);
            imageRectangle.getStyleClass().add("train-image");
            imageRectangle.setOnMouseClicked((a)-> drawCardHandler.get().onDrawCard(slot));
            imageRectangle.disableProperty().bind(drawCardHandler.isNull()); //only activate when the card handler is filled -> possible to draw a card
            cardStackPane.getChildren().add(imageRectangle);

            cardPane.getChildren().add(cardStackPane);
        }

        Button cardDeck = new Button(StringsFr.CARDS); //creation of the button corresponding to the cards deck
        cardDeck.getStyleClass().add("gauged");
        cardDeck.setGraphic(gauge(observableGameState.cardPercentageRemaining()));
        cardPane.getChildren().add(cardDeck);
        cardDeck.disableProperty().bind(drawCardHandler.isNull()); //only activate when the card handler is filled -> possible to draw a card
        cardDeck.setOnAction((a)-> drawCardHandler.get().onDrawCard(DECK_SLOT));

        return cardPane;
    }

    /**
     * the gauge that indicate how much card7ticket are left in a deck
     * @param pctProperty : percentage property of an integer (it can be the ticket percentage or the card percentage)
     * @return group : the group where we define the gauge
     */
    private static Group gauge(ReadOnlyIntegerProperty pctProperty){
        Group group = new Group();
        Rectangle backgroundRectangle = new Rectangle(WIDTH_RECTANGLE_GAUGE ,HEIGHT_RECTANGLE_GAUGE);
        backgroundRectangle.getStyleClass().add("background");

        Rectangle foregroundRectangle = new Rectangle(WIDTH_RECTANGLE_GAUGE ,HEIGHT_RECTANGLE_GAUGE);
        foregroundRectangle.getStyleClass().add("foreground");
        foregroundRectangle.widthProperty().bind(pctProperty.multiply(50).divide(100));

        group.getChildren().addAll(backgroundRectangle, foregroundRectangle);

        return group;
    }

    /**
     * rectangle definition of outsideCardRectangle, and insideCardColoredRectangle
     * the rectangle that compose the card
     * @param stackPane : pane where we add the rectangles
     */
    private static void rectanglesDefinition(StackPane stackPane){
        Rectangle outsideCardRectangle = new Rectangle(WIDTH_OUTSIDE_CARD_RECTANGLE ,HEIGHT_OUTSIDE_CARD_RECTANGLE);
        outsideCardRectangle.getStyleClass().add("outside");

        Rectangle insideCardColoredRectangle = new Rectangle(WIDTH_RECTANGLE_IMAGE, HEIGHT_RECTANGLE_IMAGE);
        insideCardColoredRectangle.getStyleClass().addAll("filled", "inside");

        stackPane.getChildren().addAll(outsideCardRectangle, insideCardColoredRectangle);
    }
}
