package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.Objects;


/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
final public class Station {
    final private int id; //dans le tCHu(51 gare) : 0->50
    final private String name;

    /**
     * Builds a station with the given ID number and name
     * @param id identification number
     * @param name name of the station
     * @throws IllegalArgumentException if id number is strictly negative (< 0).
     */
    public Station(int id, String name){
        Preconditions.checkArgument(id>=0);
        Objects.requireNonNull(name);
        this.id = id;
        this.name = name;
    }

    /**
     * @return the station identification number
     */
    public int id(){
        return id;
    }

    /**
     * @return name of the Station
     */
    public String name(){
        return name;
    }

    /**
     * redefinition of the method toString
     * @return name of the station
     */
    @Override
    public String toString() {
        return name;
    }
}
