package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
final public class Trip {
    private final Station from;
    private final Station to;
    private final int points;

    /**
     * Builds a new route between the two given stations and worth the given number of points
     * @param from departure station
     * @param to destination station
     * @param points the number of points of the route
     * @throws NullPointerException if one of the two stations is null
     * @throws IllegalArgumentException if the number of points is not strictly positive (> 0).
     */
    public Trip(Station from, Station to, int points){
        Preconditions.checkArgument(points > 0);

        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        this.points = points;
    }


     /**
      * @param from departure stations
      * @param to arrival stations
      * @param points the number of points of the routes
      * @return Returns a list of all possible routes from one of the stations in the first list (from) to one of the stations in the second list (to),
            * each worth the given number of points
      * @throws IllegalArgumentException if one of the lists is empty, or if the number of points is not strictly positive.
      */
    public static List<Trip> all(List<Station> from, List<Station> to, int points){

        Preconditions.checkArgument(!from.isEmpty() || !to.isEmpty());

        List<Trip> allPossibilities = new ArrayList<>();

        for(Station startStation : from){
            for(Station endStation : to){
                allPossibilities.add(new Trip(startStation,endStation,points));
            }
        }
        return allPossibilities;
    }


    /**
     * @return first station of the trip
     */
    public Station from() { return from; }

    /**
     * @return end station of the trip
     */
    public Station to() { return to; }

    /**
     * @return number of point of the trip
     */
    public int points() { return points; }

    /**
     * @param connectivity connectivity of the player
     * @return number of point of the trip depending on the connectivity of the player
     */
    public int points(StationConnectivity connectivity) {
        return connectivity.connected(from, to) ? points : -points;
    }
}
