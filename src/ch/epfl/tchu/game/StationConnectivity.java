package ch.epfl.tchu.game;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public interface StationConnectivity {

    /**
     * @param s1 departure station
     * @param s2 arrival station
     * @return true if and only if the given stations are connected by the player's network
     */
    boolean connected(Station s1, Station s2);
}
