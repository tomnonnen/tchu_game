package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class Route {

    /**
     * Two levels exist: OVERGROUND  (above ground)
     *                   UNDERGROUND (in a tunnel)
     */
    public enum Level{
        OVERGROUND,
        UNDERGROUND
    }

    private final String id;
    private final Station station1;
    private final Station station2;
    private final int length;
    private final Level level;
    private final Color color;
    /**
     * @param id route identifier
     * @param station1 starting station
     * @param station2 end station
     * @param length route length
     * @param level route level (UNDERGROUND/OVERGROUND)
     * @param color route colour
     * @throws NullPointerException if id, station1, station2 or level are null
     * @throws IllegalArgumentException if station1 equals station2 or if the length of the route is greater or less than the bounds
     */
    public Route(String id, Station station1, Station station2, int length, Level level, Color color){
        Preconditions.checkArgument(!station1.equals(station2));
        Preconditions.checkArgument(Constants.MIN_ROUTE_LENGTH <= length && Constants.MAX_ROUTE_LENGTH >= length);
        if(id == null || station1 == null || station2 == null || level == null){
            throw new NullPointerException("One of the arguments should not be null");
        } //To improve the readability we have choose to expressly check if the stations1 is null know that it is also check by the equal methode
        this.id = id;
        this.station1 = station1;
        this.station2 = station2;
        this.length = length;
        this.level = level;
        this.color = color;
    }

    /**
     *
     * @return the list of the two stations on the route, in the order in which they were passed to the constructor
     */
    public List<Station> stations(){
        return List.of(station1, station2);
    }

    /**
     * @param station station from one side of the route
     * @return the station of the route which is not the given one
     * @throws IllegalArgumentException if the given station is neither the first nor the second station of the route
     */
    public Station stationOpposite(Station station){
        Preconditions.checkArgument(station.equals(station1) || station.equals(station2));
        return station.equals(station1) ? station2 : station1;
    }


    /**
     * determine the twin route of the route by comparing
     * its begin and start station if a match occurs the route is say to have a twin and
     * the corresponding twin is return else return null
     * @return the twin route of the route
     */
    public Route getTwin(){
        for(Route route: ChMap.routes()){
            if(route.stations().containsAll(stations()) && !id().equals(route.id()))
            {  return route;} //if a correspondence was found we set the twin route and end the methode
        }
        return null;
    }



    /**
     * @param possibleClaimCards list containing the possible map combinations to retrieve a road or tunnel without colour
     */
    private void possibleClaimCardsNoColorSpecified(List<SortedBag<Card>> possibleClaimCards){
        if(level.equals(Level.UNDERGROUND)){ // in the case of a tunnel
            for(int i=0; i<length ; i++) {
                for (Card car : Card.CARS) {
                    possibleClaimCards.add(SortedBag.of(i, Card.LOCOMOTIVE, length - i, car));
                        /*
                        1. first pass: SortedBag containing only plain coloured wagons
                        2. second pass: 1. locomotive + wagons on the remaining places
                        3. ...
                        */
                }
            }
            //... only locomotives outside the loop to avoid adding x times the maximum number of locomotives
            possibleClaimCards.add(SortedBag.of(length, Card.LOCOMOTIVE));
        } else { // if it is not a tunnel
            for (Card car : Card.CARS) { // if it is not a tunnel
                possibleClaimCards.add(SortedBag.of(length, car));
                //1. only one SortedBag pass containing only plain coloured cars
            }
        }

    }

    /**
     * @param possibleClaimCards list containing the possible card combinations to retrieve a coloured road or tunnel
     */
    private void possibleClaimCardsColorSpecified(List<SortedBag<Card>> possibleClaimCards){
        if(level.equals(Level.UNDERGROUND)){ // in the case of a tunnel
            // we add the number of possible locomotives so that within the list, it is increasing
            for(int i=0; i<=length ;i++){
                possibleClaimCards.add(SortedBag.of(i, Card.LOCOMOTIVE, length-i, Card.of(color)));
            }
        } else { // if it is not a tunnel
            //add the only possibility, i.e. the number of cards being equal to the length of the road of colour equivalent to the colour of the road
            possibleClaimCards.add(SortedBag.of(length, Card.of(color)));
        }
    }


    /**
     * @return a list of all the sets of cards that could be played to (try to) take over the road,
     *   sorted in ascending order of number of locomotive cards, then by colour
     */
    public List<SortedBag<Card>> possibleClaimCards(){

        List<SortedBag<Card>> possibleClaimCards = new ArrayList<>();

        if(color == null){
            possibleClaimCardsNoColorSpecified(possibleClaimCards);// if the colour is neutral
        } else {
            possibleClaimCardsColorSpecified(possibleClaimCards); // in case the colour is neutral
        }
        return possibleClaimCards;
    }

    /**
     * @param claimCards: the cards that the player initially laid down
     * @param drawnCards: the three cards drawn from the top of the deck
     * @return the number of additional cards to play to take the road (in tunnel), knowing that the player initially laid down the claimCards and that the three cards drawn from the top of the deck are drawnCards
     * @throws IllegalArgumentException if the road it is applied to is not a tunnel, or if drawnCards does not contain exactly 3 cards
     */
    public int additionalClaimCardsCount(SortedBag<Card> claimCards, SortedBag<Card> drawnCards){
        Preconditions.checkArgument(level.equals(Level.UNDERGROUND) && drawnCards.size() == 3);

        int total=0;

        for(Card card : drawnCards){
            if(card.equals(Card.LOCOMOTIVE) || claimCards.contains(card)){ //card match a locomotive or a card that the player has laid down
                total++;
            }
        }

        return total;

    }


    /**
     * @return the number of building points a player gets when he takes the road.
     */
    public int claimPoints(){
        return Constants.ROUTE_CLAIM_POINTS.get(length);
    }


    /**
     * @return id of the route
     */
    public String id(){
        return id;
    }

    /**
     * @return first station of the route
     */
    public Station station1(){
        return station1;
    }

    /**
     * @return second station of the route
     */
    public Station station2(){
        return station2;
    }

    /**
     * @return size of the route
     */
    public int length(){
        return length;
    }

    /**
     * @return level of the route
     */
    public Level level(){
        return level;
    }

    /**
     * @return color of the route
     */
    public Color color(){
        return color;
    }

}
