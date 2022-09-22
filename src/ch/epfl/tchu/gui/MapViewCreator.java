package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Route;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.List;


/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
abstract class MapViewCreator {

    private final static int WIDTH_RECTANGLE = 36;
    private final static int HEIGHT_RECTANGLE = 12;

    private final static int CIRCLE_RADIUS = 3;

    /**
     * creat the view of the map composed of all the route with specific color depending which player have them and the background image
     * @param observableGameState : the subject that are the state of the game that we observe with the observers
     * @param claimRouteHandlerObjectPropertyHandler: a property containing the action handler to use when the player wants to seize a route
     * @param cardChooser : a card selector
     * @return map : the view of the current map
     */
    public static Node createMapView(ObservableGameState observableGameState, ObjectProperty<ActionHandlers.ClaimRouteHandler> claimRouteHandlerObjectPropertyHandler, CardChooser cardChooser){
        Pane map = new Pane();
        map.getStylesheets().addAll("map.css", "colors.css");

        //background image
        ImageView background = new ImageView();
        background.setImage(new Image("map.png"));
        map.getChildren().add(background);

        //the groups for each route
        for(Route routeElem: ChMap.routes()){

            Group routeElemGroup = new Group();
            routeElemGroup.setId(routeElem.id()); //set the id (name of the route)
            routeElemGroup.getStyleClass().addAll("route", routeElem.level().toString(), routeElem.color() == null ? "NEUTRAL" : routeElem.color().toString());
            map.getChildren().add(routeElemGroup);

            //listener
            observableGameState.routesOwner(routeElem).addListener((o,oN,oV) -> routeElemGroup.getStyleClass().add(oV.toString()));

            //All the boxes composing the route to the number of route length of 1->route Length
            for(int i=1;i<=routeElem.length();i++){
                Group routeElemCaseGroup = new Group();
                routeElemCaseGroup.setId(routeElem.id()+"_"+i); //set the id (name of the route)

                //The rectangle representing the track (path)
                Rectangle trackRectangle = new Rectangle(WIDTH_RECTANGLE,HEIGHT_RECTANGLE);
                trackRectangle.getStyleClass().addAll("track", "filled");
                routeElemCaseGroup.getChildren().add(trackRectangle);

                //Group representing the car if existing
                Group wagon = new Group();
                wagon.getStyleClass().add("car");
                Rectangle carRectangle = new Rectangle(WIDTH_RECTANGLE,HEIGHT_RECTANGLE);
                carRectangle.getStyleClass().add("filled");
                Circle circleWagon1 = new Circle(CIRCLE_RADIUS);
                Circle circleWagon2 = new Circle(CIRCLE_RADIUS);
                circleWagon1.setCenterX(12); circleWagon1.setCenterY(6);
                circleWagon2.setCenterX(24); circleWagon2.setCenterY(6);
                wagon.getChildren().addAll(carRectangle,circleWagon1,circleWagon2);
                routeElemCaseGroup.getChildren().add(wagon);

                routeElemGroup.getChildren().add(routeElemCaseGroup);

            }

            //listener
            routeElemGroup.disableProperty().bind(claimRouteHandlerObjectPropertyHandler.isNull().or(observableGameState.routeCapturable(routeElem).not()));

            //handler
            routeElemGroup.setOnMouseClicked(e -> {
                List<SortedBag<Card>> possibleClaimCards = observableGameState.possibleClaimCards(routeElem);
                ActionHandlers.ClaimRouteHandler claimRouteH = claimRouteHandlerObjectPropertyHandler.get();
                if(possibleClaimCards.size()==1){
                    //it is only one possibility to claim the route, also it is claimed automatically
                    claimRouteH.onClaimRoute(routeElem,possibleClaimCards.get(0));
                }
                else{ //if it is more than one possibility to claim the route, we call the action handler and the method CardChooser
                    ActionHandlers.ChooseCardsHandler chooseCardsH =
                            chosenCards -> claimRouteH.onClaimRoute(routeElem, chosenCards);
                    cardChooser.chooseCards(possibleClaimCards, chooseCardsH);
                }
            });

        }

        return map;
    }

    /**
     * The chooseCards method of this interface is intended to be called when the player has to choose the cards he wants
     * to use to seize a road. The possibilities available to him are given by the options argument, while the action handler
     * is intended to be used when he has made his choice.
     */
    @FunctionalInterface
    public interface CardChooser {
        void chooseCards(List<SortedBag<Card>> options, ActionHandlers.ChooseCardsHandler handler);
    }

}
