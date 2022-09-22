package ch.epfl.tchu.game;

import java.util.*;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class Trail {

    private final List<Route> trail;
    private final int length;
    private final Station stationStart;
    private final Station stationEnd;
    private static final Trail emptyTrail = new Trail(new ArrayList<>(), 0,null,null);
    private static final boolean DEBUG = true;

    private Trail(List<Route> trail, int length, Station stationStart, Station stationEnd){
        this.trail = trail;
        this.length = length;
        this.stationStart = stationStart;
        this.stationEnd = stationEnd;
    }

    /**
     * if the route can be connected to the end of the trail she is add to the list of the possible trail
     * @param elemTrail the actual trail
     * @param elemRouteAvailable the route that we want to connect
     * @param cs1 the Array containing all the possible trail
     */
    private static void addTrailIfMatch(Trail elemTrail,Route elemRouteAvailable, ArrayList<Trail> cs1){
        assert elemTrail.station2() != null;
        if(elemRouteAvailable.station1().id() == elemTrail.station2().id()){
            List<Route> newTrailAttribute = new ArrayList<>(elemTrail.trail);
            newTrailAttribute.add(elemRouteAvailable);
            cs1.add(new Trail(newTrailAttribute,elemTrail.length()+elemRouteAvailable.length(),elemTrail.station1(),elemRouteAvailable.station2()));
        }
        else if(elemRouteAvailable.station2().id() == elemTrail.station2().id()){
            List<Route> newTrailAttribute = new ArrayList<>(elemTrail.trail);
            newTrailAttribute.add(elemRouteAvailable);
            cs1.add(new Trail(newTrailAttribute,elemTrail.length()+elemRouteAvailable.length(),elemTrail.station1(),elemRouteAvailable.station1()));
        }
    }
    /**
     * @param start the departure station
     * @param end the arrival station
     * @param routes the route available
     * @return empty trail if not possible to connect both station else the shortest possible trail to go from the start station to the end station
     */
    public static Trail shortestTrail(List<Route> routes,Station start, Station end){
        List<Trail> cs = new ArrayList<>();
        for(Route route : routes){
            if(route.station1()==start){        cs.add(new Trail(List.of(route),route.length(),start,route.station2()));    }
            else if(route.station2()==start){   cs.add(new Trail(List.of(route),route.length(),start,route.station1()));    }
        }
        Trail shortest = emptyTrail;
        while (!cs.isEmpty()){
            ArrayList<Trail> cs1 = new ArrayList<>();
            for (Trail elemTrail : cs){
                ArrayList<Route> rs = new ArrayList<>(routes);
                rs.removeAll(elemTrail.trail);
                for(Route elemRouteAvailable : rs){
                    addTrailIfMatch(elemTrail,elemRouteAvailable,cs1);
                    if(elemTrail.station2().id()==end.id()) break;
                }
                if((elemTrail.length()<shortest.length() || shortest.length()==0) && elemTrail.stationEnd.id()==end.id()){
                    shortest = elemTrail;
                    cs1=new ArrayList<>();
                    break;
                }
            }
            cs=cs1;
        }
        return shortest;
    }

    /**
     * @param routes the route available
     * @return the longest possible trail doable with the routes given
     */
    public static Trail longest(List<Route> routes){
        
        if(routes.isEmpty()){
            return emptyTrail;
        }

        List<Trail> cs = new ArrayList<>();
        for (Route elemRoute : routes){
            cs.add(new Trail(List.of(elemRoute),elemRoute.length(),elemRoute.station1(),elemRoute.station2()));
            cs.add(new Trail(List.of(elemRoute),elemRoute.length(),elemRoute.station2(),elemRoute.station1()));
        }
        Trail longest = new Trail(List.of(),0,null,null);
        while (!cs.isEmpty()){
            ArrayList<Trail> cs1 = new ArrayList<>();
            for (Trail elemTrail : cs){
                ArrayList<Route> rs = new ArrayList<>(routes);
                rs.removeAll(elemTrail.trail);
                for(Route elemRouteAvailable : rs){
                    addTrailIfMatch(elemTrail,elemRouteAvailable,cs1);
                }
                if(elemTrail.length()>longest.length()){
                    longest = elemTrail;
                }
            }
            cs=cs1;
        }
        return longest;
    }

    /**
     * @return size of the trail
     */
    public int length(){
        return length;
    }

    public List<Route> getTrail(){
        return trail;
    }

    /**
     * @return first Station of the trail, null if the trail is of length 0
     */
    public Station station1(){
        return stationStart;
    }
    /**
     * @return last Station of the trail, null if the trail is of length 0
     */

    public Station station2(){
        return stationEnd;
    }

    /**
     * @return the first, all intermediate stations, the last station and the length of the trail in brackets
     */
    @Override
    public String toString() {
        ArrayList<String> s = new ArrayList<>();
        if(length==0||trail.isEmpty()){
            return "You stay where you are! Your path is 0!";
        }
        s.add(Objects.requireNonNull(station1()).name());
        if(DEBUG){
            Station lastStation = station1();
            for (Route elemRoute : trail) {
                assert lastStation != null;
                if (lastStation.id() == elemRoute.station2().id()) {
                    s.add(elemRoute.station1().name());
                    lastStation = elemRoute.station1();
                } else {
                    s.add(elemRoute.station2().name());
                    lastStation = elemRoute.station2();
                }
            }
        }
        else{
            s.add(Objects.requireNonNull(station2()).name());
        }

        return String.format("%s (%s)", String.join(" - ",s), length());
    }
}
