package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.*;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class Ticket implements Comparable<Ticket> {

    private final List<Trip> trips;
    private final String text;

    /**
     * main constructor
     * @param trips specifies the trips that make up the ticket
     */
    public Ticket(List<Trip> trips){

        Preconditions.checkArgument(!trips.isEmpty());
        String startStationPreviousName = trips.get(0).from().name();

        for(Trip tripElem : trips){
            Preconditions.checkArgument(startStationPreviousName.equals(tripElem.from().name()));
        }

        this.trips = List.copyOf(trips);
        text = computeText(trips);
    }

    /**
     * secondary constructor
     * @param from station of departure
     * @param to station of arrival
     * @param points point of the ticket
     */
    public Ticket(Station from, Station to, int points){
        this(List.of(new Trip(from, to, points)));
    }

    /**
     * @return the list of trips that make up the ticket
     */
    public List<Trip> getTrip(){
        return trips;
    }


    /**
     * @return the textual representation of the ticket according to the schema :
     *      city-to-city ticket: Lausanne - St. Gallen (13)
     *      ticket from Bern to neighbouring countriesBerne - {Germany (6), Austria (11), France (5), Italy (8)}
     *      ticket from France to one of the other neighbouring countries: France - {Germany (5), Austria (14), Italy (11)}
     */
    public String text(){
        return text;
    }

    @Override
    public String toString() {
        return text();
    }

    /**
     * @param connectivity the connectivity of the player
     * @return the number of points of the trip with the most (max point trip)
     */
    public int points(StationConnectivity connectivity){
        int maxPoint = Integer.MIN_VALUE;

        for(Trip tripElem : trips){
            if(maxPoint<tripElem.points(connectivity)) {
                maxPoint =tripElem.points(connectivity);
            }
        }
        return maxPoint;
    }

    /**
     * compares the ticket to which it is applied (this) with the one passed as an argument (that) in alphabetical order of their textual representation
     * @param that the object to compare
     * @return returns a strictly negative integer if this is strictly smaller than that, a strictly positive integer if this is strictly larger than that, and zero if both are equal
     */
    @Override
    public int compareTo(Ticket that) {
        return this.text().compareTo(that.text());
    }

    /**
     * @param trips the list of trips of the ticket
     * @return the textual representation of the ticket
     */
    private static String computeText(List<Trip> trips){
        TreeSet<String> s = new TreeSet<>();
        String sJoin;

        for (Trip trip : trips) {
            String sPutPoints = String.format("%s (%s)", trip.to().name(), trip.points());
            s.add(sPutPoints);
        }

        //we add the commas as delimiter if we have more than one trip
        if(s.size() == 1){
            sJoin = s.last();
        } else {
            sJoin = String.format("{%s}", String.join(", ", s));
        }

        return String.format("%s - %s",trips.get(0).from().name(), sJoin);
    }

}
