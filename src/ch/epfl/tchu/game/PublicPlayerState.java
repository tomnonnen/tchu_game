package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.List;


/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public class PublicPlayerState {

    private final int ticketCount; //nbr of tickets
    private final int cardCount; //nbr of cards
    private final List<Route> routes; //route the player owns
    private final int carCount; //nbr of cars
    private final int claimPoints; //building points

    /**
     * constructs the public state of a player who has the given number of tickets and cards, and has taken the given routes
     * @param ticketCount : number of tickets
     * @param cardCount: number of cards
     * @param routes: the routes it has seized
     * @throws IllegalArgumentException if the number of tickets or the number of cards is strictly negative (< 0).
     */
    public PublicPlayerState(int ticketCount, int cardCount, List<Route> routes){
        Preconditions.checkArgument(ticketCount>=0 && cardCount>=0);
        this.ticketCount = ticketCount;
        this.cardCount = cardCount;
        this.routes = routes;

        int provClaimPoints =0;
        int provCarCount = Constants.initialCarCount();
        for(Route route : routes){
            provClaimPoints += route.claimPoints() ;
            provCarCount -= route.length();
        }

        this.carCount = provCarCount;
        this.claimPoints = provClaimPoints;
    }

    /**
     * @return the number of tickets the player has
     */
    public int ticketCount(){
        return ticketCount;
    }

    /**
     * @return the number of cards the player has
     */
    public int cardCount(){
        return cardCount;
    }

    /**
     * @return the roads that the player owns
     */
    public List<Route> routes(){
        return routes;
    }

    /**
     * @return the number of cars the player owns
     */
    public int carCount(){
        return carCount;
    }

    /**
     * @return the number of building points obtained by the player
     */
    public int claimPoints(){
        return claimPoints;
    }
}
