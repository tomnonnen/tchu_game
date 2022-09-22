package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.*;

import static ch.epfl.tchu.game.Constants.FACE_UP_CARD_SLOTS;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class ObservableGameState {

    private PublicGameState publicGameState;
    private PlayerState playerState;

    //public state of the Game
    private final IntegerProperty ticketPercentageRemaining; //0-100
    private final IntegerProperty cardPercentageRemaining; //0-100
    private final List<ObjectProperty<Card>> faceUpCards;
    private final Map<Route,ObjectProperty<PlayerId>> routesOwner;

    //public state of each of the players
    private final EnumMap<PlayerId, IntegerProperty> nbrTicketInHand;
    private final EnumMap<PlayerId, IntegerProperty> nbrCardInHand;
    private final EnumMap<PlayerId, IntegerProperty> nbrWagonPlayer;
    private final EnumMap<PlayerId, IntegerProperty> nbrPointObtained;

    //private state of the player corresponding to the ObservableGameState
    private final PlayerId playerId;
    private final ListProperty<Ticket> ticketPlayer;
    private final MapProperty<Ticket,Integer> ticketsPlayerPoints;
    private final EnumMap<Card,IntegerProperty> nbrOfCardTypeInHand; //= new EnumMap<>(globalPlayerState);
    private final Map<Route,BooleanProperty> routeCapturable; //= new EnumMap<>(globalPlayerState);

    private final static int INITIAL_INT_VALUE = 0;

    /**
     * constructor initializing all properties to their default value :
     *      objet   ->  null
     *      int     ->  0
     *      boolean ->  false
     * @param playerId the player which teh graphic interface is specific.
     */
    public ObservableGameState(PlayerId playerId){
        this.playerId = playerId;

        ticketPercentageRemaining = new SimpleIntegerProperty(INITIAL_INT_VALUE);
        cardPercentageRemaining = new SimpleIntegerProperty(INITIAL_INT_VALUE);
        nbrTicketInHand = creatPlayerEnumMapNumber();
        nbrCardInHand = creatPlayerEnumMapNumber();
        nbrWagonPlayer = creatPlayerEnumMapNumber();
        nbrPointObtained = creatPlayerEnumMapNumber();
        ticketPlayer = new SimpleListProperty<>();
        ticketsPlayerPoints = new SimpleMapProperty<>();
        nbrOfCardTypeInHand = creatCardEnumMapNumber();

        faceUpCards = new ArrayList<>();
        for (int slot : FACE_UP_CARD_SLOTS) {
            faceUpCards.add(slot, new SimpleObjectProperty<>());
        }

        routesOwner = new HashMap<>();
        routeCapturable = new HashMap<>();
        for(Route r : ChMap.routes()){ //we initialize the two maps
            routesOwner.put(r, new SimpleObjectProperty<>());
            routeCapturable.put(r, new SimpleBooleanProperty(false));
        }

    }

    /**
     * @return A map that contain each elem of the PlayerId Enum with the corresponding SimpleIntegerProperty set to 0
     */
    private EnumMap<PlayerId, IntegerProperty> creatPlayerEnumMapNumber(){
        EnumMap<PlayerId, IntegerProperty> playerMapNumber = new EnumMap<>(PlayerId.class);
        for(PlayerId playerId : PlayerId.getAllPlayer()){
            playerMapNumber.put(playerId, new SimpleIntegerProperty(INITIAL_INT_VALUE));
        }
        return playerMapNumber;
    }
    /**
     * @return A map that contain each elem of the Card Enum with the corresponding SimpleIntegerProperty set to 0
     */
    private EnumMap<Card, IntegerProperty> creatCardEnumMapNumber(){
        EnumMap<Card, IntegerProperty> cardMapNumber = new EnumMap<>(Card.class);
        for(Card cardType : Card.ALL){
            cardMapNumber.put(cardType,new SimpleIntegerProperty(INITIAL_INT_VALUE));
        }
        return cardMapNumber;
    }

    /**
     * enable to get the owner of the route in the publicGameState
     * @param publicGameState the state of the game in which we want to know the owner of the route
     * @param r the route
     * @return null if the route has no owner else the PlayerId
     */
    private PlayerId routeOwner(PublicGameState publicGameState,Route r){
        for(PlayerId player : PlayerId.getAllPlayer()){
            if(publicGameState.playerState(player).routes().contains(r)){
                return player; //if the route is contain in the route in hand from one of the player, we return the corresponding player else we try another player
            }
        }
        return null;//if no player own the route we return null by default
    }

    /**
     * update all the property with the new information contained in publicGameState and playerState
     * @param publicGameState the public state of the game
     * @param playerState the state of the player of the ObservableGameState
     */
    public void setState(PublicGameState publicGameState,PlayerState playerState){
        this.publicGameState = publicGameState;
        this.playerState = playerState;
        ticketPercentageRemaining.set((int)((100d/ChMap.tickets().size())*publicGameState.ticketsCount()));
        cardPercentageRemaining.set((int)((100d/Constants.TOTAL_CARDS_COUNT)*publicGameState.cardState().deckSize()));

        //we set the new face up cards
        for (int slot : FACE_UP_CARD_SLOTS) {
            Card newCard = publicGameState.cardState().faceUpCard(slot);
            faceUpCards.get(slot).set(newCard);
        }


        for (Route r : ChMap.routes()) {
            //we get the owner of the route
            PlayerId routeOwner = routeOwner(publicGameState,r);
            routesOwner.get(r).set(routeOwner);//we set the property that contain this value

            if(routeOwner ==null){  //his not owned by anyone
                routesOwner.get(r).set(null);//if the route is not claimed, his owner is null
                //to be claimable by the player, the player has to be the current player , the route have to be in the sense of PlayerState claimable and as its twin not been owned by a player
                boolean routeClaimedBoolean = playerId==publicGameState.currentPlayerId() && playerState.canClaimRoute(r); // => the player has to be the current player , the route have to be in the sense of PlayerState claimable
                Route twinRoute = r.getTwin();
                if(twinRoute!=null&&routeClaimedBoolean){ //has a twin and match the precedent condition claimable
                    routeClaimedBoolean = routeOwner(publicGameState,twinRoute)==null; //his twin as no owner
                }
                routeCapturable.get(r).set(routeClaimedBoolean);
            }
            else{
                routeCapturable.get(r).set(false);//if the route has already an owner she isn't claimable
            }
        }

        //set the different Game properties depending on the publicGameState for each player
        for(PlayerId player : PlayerId.getAllPlayer()){
            nbrTicketInHand.get(player).set(publicGameState.playerState(player).ticketCount());
            nbrCardInHand.get(player).set(publicGameState.playerState(player).cardCount());
            nbrWagonPlayer.get(player).set(publicGameState.playerState(player).carCount());
            nbrPointObtained.get(player).set(publicGameState.playerState(player).claimPoints());
        }



        Map<Ticket,Integer> ticketPointMap = new HashMap<>();
        for(Ticket ticket : playerState.tickets()){
            ticketPointMap.put(ticket,ticket.points(playerState.stationConnectivityPlayer()));
        }
        ObservableMap<Ticket,Integer> observableMap = FXCollections.observableMap(ticketPointMap);
        ticketsPlayerPoints.set(observableMap);


        //add all the ticket owned by the player to the observable game state property
        ObservableList<Ticket> observableList = FXCollections.observableArrayList(playerState.tickets().toList());
        ticketPlayer.set(observableList); //to be placed after the ticketsPlayerConnected.set(observableMap); because the graphic interface update use it

        //set how many card of each type the player has
        for(Card card: Card.ALL) {
            nbrOfCardTypeInHand.get(card).set(playerState.cards().countOf(card));
        }

    }


    //getter start : basically returning the corresponding attribute properties on a ReadOnly form.

    /**
     * @return the property containing the percentage of ticket remaining in the ticket deck
     */
    public ReadOnlyIntegerProperty ticketPercentageRemaining(){
        return ticketPercentageRemaining;
    }

    /**
     * @return the property containing the percentage of card remaining in the card deck
     */
    public ReadOnlyIntegerProperty cardPercentageRemaining(){
        return cardPercentageRemaining;
    }

    /**
     * @param slot the position of the face up card to get 
     * @return the property containing the face up card corresponding
     */
    public ReadOnlyObjectProperty<Card> faceUpCard(int slot) {
        return faceUpCards.get(slot);
    }

    /**
     * @param route the route 
     * @return the property containing the owner of the corresponding route 
     */
    public ReadOnlyObjectProperty<PlayerId> routesOwner(Route route) {
        return routesOwner.get(route);
    }

    /**
     * @param playerId the player 
     * @return the property containing the nbr of ticket in hand of the player 
     */
    public ReadOnlyIntegerProperty nbrTicketInHand(PlayerId playerId) {
        return nbrTicketInHand.get(playerId);
    }

    /**
     * @param playerId the player 
     * @return the property containing the nbr of card in hand of the player 
     */
    public ReadOnlyIntegerProperty nbrCardInHand(PlayerId playerId) {
        return nbrCardInHand.get(playerId);
    }

    /**
     * @param playerId the player 
     * @return the property containing that the player has left
     */
    public ReadOnlyIntegerProperty nbrWagonPlayer(PlayerId playerId) {
        return nbrWagonPlayer.get(playerId);
    }

    /**
     * @param playerId the player 
     * @return the property containing the nbr of point the player currently has 
     */
    public ReadOnlyIntegerProperty nbrPointObtained(PlayerId playerId) {
        return nbrPointObtained.get(playerId);
    }

    /**
     * @return the player of the observable game state 
     */
    public PlayerId playerId() {
        return playerId;
    }

    /**
     * @return the property containing the ticket of the player of the observable game state 
     */
    public ReadOnlyListProperty<Ticket> ticketPlayer() {
        return ticketPlayer;
    }

    /**
     * @return the property containing the ticket of the player of the observable game state
     */
    public ReadOnlyMapProperty<Ticket,Integer> ticketsPlayerPoints() {
        return ticketsPlayerPoints;
    }

    /**
     * @param card the type of the card
     * @return the property containing the nbr of card of the corresponding type the player of the observable game state currently has. 
     */
    public ReadOnlyIntegerProperty nbrOfCardTypeInHand(Card card) {
        return nbrOfCardTypeInHand.get(card);
    }

    /**
     * @param route the route
     * @return the property containing if the route is claimable or not
     */
    public ReadOnlyBooleanProperty routeCapturable(Route route) {
        return routeCapturable.get(route);
    }
    
    //getter end


    /**
     * A call to the canDrawTickets of the publicGameState
     */
    public boolean canDrawTickets(){
        return publicGameState.canDrawTickets();
    }

    /**
     * A call to the canDrawCards of the publicGameState
     */
    public boolean canDrawCards(){
        return publicGameState.canDrawCards ();
    }

    /**
     * A call to the possibleClaimCards of the PlayerState
     * @param route the route the player is interested in know his possible combinations of cards that enable him to take the route
     */
    public List<SortedBag<Card>> possibleClaimCards(Route route){
        return playerState.possibleClaimCards(route);
    }





}
