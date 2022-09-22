package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ch.epfl.tchu.game.Card.ALL;
import static ch.epfl.tchu.game.ChMap.stations;
import static ch.epfl.tchu.game.Constants.ADDITIONAL_TUNNEL_CARDS;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class PlayerState extends PublicPlayerState{


    private final SortedBag<Ticket> tickets;
    private final SortedBag<Card> cards;

    /**
     * constructs the state of a player with the given tickets, maps and routes.
     * @param tickets the tickets that the player has
     * @param cards the cards the player owns
     * @param routes the routes the player owns
     */
    public PlayerState(SortedBag<Ticket> tickets, SortedBag<Card> cards, List<Route> routes){
        super(tickets.size(),cards.size(),routes);
        this.tickets = tickets;
        this.cards = cards;
    }

    /**
     * Construct the initial state in which the player has no tickets, and has not taken any roads.
     * @param initialCards cars that the player has
     * @return the initial state of a player to whom the given initial cards have been dealt,
     * @throws IllegalArgumentException if the number of initial cards is not 4.
     */
    public static PlayerState initial(SortedBag<Card> initialCards) {
        Preconditions.checkArgument(initialCards.size()==Constants.INITIAL_CARDS_COUNT);
        return new PlayerState(SortedBag.of(),initialCards,List.of());
    }

    /**
     * @return player's tickets
     */
    public SortedBag<Ticket> tickets(){
        return tickets;
    }

    /**
     *
     * @param newTickets tickets to add
     * @return a state identical to the receiver,
     * except that the player also has the given tickets
     */
    public PlayerState withAddedTickets(SortedBag<Ticket> newTickets){
        ArrayList<Ticket> newTicket = new ArrayList<>(tickets.toList());
        newTicket.addAll(newTickets.toList());
        return new PlayerState(SortedBag.of(newTicket),cards,routes());
    }

    /**
     *
     * @return the player's car/locomotive cards
     */
    public SortedBag<Card> cards(){
        return cards;
    }

    /**
     *
     * @param card cards to add
     * @return a state identical to the receiver,
     * except that the player also has the given card,
     */
    public PlayerState withAddedCard(Card card){
        return new PlayerState(tickets,cards.union(SortedBag.of(card)),routes());
    }



    /**
     *
     * @param route road which the player wishes to get
     * @return true if the player can take the given road,
     * i.e. if he has enough cars left and if he has the necessary cards,
     */
    public boolean canClaimRoute(Route route){

        for (SortedBag<Card> possibility : route.possibleClaimCards()) {
            if (cards.contains(possibility) && carCount() >= possibility.size()) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param route target route
     * @return a list of all the sets of cards that the player could use to take possession of the given route
     * @throws IllegalArgumentException if the player does not have enough cars to take the road
     */
    public List<SortedBag<Card>> possibleClaimCards(Route route){
        Preconditions.checkArgument(carCount()>=route.length());
        List<SortedBag<Card>> returnPossibleClaimCards = new ArrayList<>();
        for (SortedBag<Card> possibility : route.possibleClaimCards()) { //loop on all the possibles combinations to take the route
            if(cards.contains(possibility)){ //verifying if the possibility is doable with the cards of the player
                returnPossibleClaimCards.add(possibility);
            }
        }//at least one way to take te route
        return returnPossibleClaimCards;
    }

    /**
     * @param cards set of card to look at
     * @return the number of card type contain in the cards set
     */
    private static int nbrCardType(SortedBag<Card> cards){
        int nbrType = 0;
        for (Card card : ALL) {
            if(cards.contains(card)){
                nbrType++;
            }
        }
        return nbrType;
    }

    /**
     * @param additionalCardsCount number of additional cards that the player must play to take the tunnel with the returned compositions
     * @param initialCards initial cards laid down
     * @param drawnCards 3 cards drawn from the top of the deck
     * @return the list of all sets of cards that the player could use to take a tunnel, sorted in ascending order of the number of locomotive cards,
     * and that these force the player to lay down additionalCardsCount card
     * @throws IllegalArgumentException if the number of additional cards is not between 1 and 3 (inclusive), if the set of initial cards is empty
     * or contains more than 2 different types of cards,
     * or if the set of cards drawn does not contain exactly 3 cards,
     */
    public List<SortedBag<Card>> possibleAdditionalCards(int additionalCardsCount, SortedBag<Card> initialCards, SortedBag<Card> drawnCards){
        List<SortedBag<Card>> possibleAdditionalCards = new ArrayList<>();
        Preconditions.checkArgument(1<=additionalCardsCount&& additionalCardsCount<=ADDITIONAL_TUNNEL_CARDS && !initialCards.isEmpty()&& nbrCardType(initialCards)<=2 && drawnCards.size()==ADDITIONAL_TUNNEL_CARDS);
        SortedBag<Card> cardsAvailable = cards.difference(initialCards);
        Card CardType = Card.LOCOMOTIVE; //the card type is a locomotive card by default

        //searching the type of card other than locomotive laid down by the player
        for(Card card : Card.CARS){
            if(initialCards.contains(card)){
                CardType = card;
                break;//if we have found the other type of card (not a locomotive) in the hand of the player (if there is none we keep the default type locomotive)
            }
        }

        int nbrCardColorInHand = cardsAvailable.countOf(CardType); //nbr of the cards of the corresponding type the player owns
        int nbrLocomotiveInHand = cardsAvailable.countOf(Card.LOCOMOTIVE); //nbr of the LOCOMOTIVE card the player owns

        if(CardType==Card.LOCOMOTIVE){
            if(additionalCardsCount<=nbrLocomotiveInHand) {
                possibleAdditionalCards.add(SortedBag.of(additionalCardsCount, Card.LOCOMOTIVE));
            }
        }
        else{
            for(int i=0; i<=additionalCardsCount ;i++){ //making all combination from additionalCardsCount cards beginning with 0 locomotive and additionalCardsCount card of CardType going to additionalCardsCount locomotive
                if(i<=nbrLocomotiveInHand && additionalCardsCount-i<=nbrCardColorInHand){
                    possibleAdditionalCards.add(SortedBag.of(i, Card.LOCOMOTIVE, additionalCardsCount-i,CardType));
                }
            }
        }
        possibleAdditionalCards.sort(Comparator.comparingInt((cs -> cs.countOf(Card.LOCOMOTIVE)))); //making sure the sets are sorted by occurrences of locomotive

        return possibleAdditionalCards;
    }


    /**
     * @param route route taken
     * @param claimCards cards used to take the route
     * @return  a deck identical to the receiver, except that the player has additionally get the given route by means of the given cards
     */
    public PlayerState withClaimedRoute(Route route, SortedBag<Card> claimCards){
        ArrayList<Route> newRoute = new ArrayList<>(routes());
        newRoute.add(route);
        return new PlayerState(tickets,cards.difference(claimCards),newRoute);
    }

    /**
     * creat an array with group of the station connected between them
     * @return le stationConnectivity of the player
     */
    public StationConnectivity stationConnectivityPlayer(){
        //Creating StationPartition.Builder
        StationPartition.Builder stationPartitionbuilder =  new StationPartition.Builder(stations().size());
        for (Route routeElem :routes()) {
            stationPartitionbuilder.connect(routeElem.station1(),routeElem.station2()); //connect the station depending on the routes own by the player
        }

        //Creating StationPartition
        return stationPartitionbuilder.build(); //creating the StationConnectivity object using the StationPartition.Builder
    }


    /**
     * @return the number of points (possibly negative) obtained by the player thanks to his tickets
     */
    public int ticketPoints(){
        int points=0;

        //Creating StationPartition
        StationConnectivity stationConnectivity = stationConnectivityPlayer();

        //Summing up the point from each ticket
        for (Ticket ticket : tickets){
            points+=ticket.points(stationConnectivity);
        }

        return points;
    }

    /**
     * @return the total points obtained by the player at the end of the game
     */
    public int finalPoints(){
        return claimPoints()+ticketPoints();
    }

}
