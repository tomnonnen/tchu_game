package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.List;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public enum Card {
    BLACK(Color.BLACK),
    VIOLET(Color.VIOLET),
    BLUE(Color.BLUE),
    GREEN(Color.GREEN),
    YELLOW(Color.YELLOW),
    ORANGE(Color.ORANGE),
    RED(Color.RED),
    WHITE(Color.WHITE),
    LOCOMOTIVE(null);

    /**
     * @param color : the colour of the wagon
     */
    Card(Color color) {
        this.color = color;
    }

    public final static List<Card>  ALL = List.of(values()); //enter the 8 colours in the list
    public final static int COUNT = ALL.size(); //storage the size of the ALL list in this variable
    public final static List<Card> CARS = List.of(BLACK, VIOLET, BLUE, GREEN, YELLOW, ORANGE, RED, WHITE); //list of wagons without the locomotive
    private final Color color; // wagon colour attribute

    /**
     * getter of the color
     * @return the colour of the card type if it is a wagon, return null if it is a locomotive
     */
    public Color color(){
        return this.color;
    }

    /**
     * @param color : the colour of the wagon
     * @return the car that correspond to the colour given in parameter
     */
    public static Card of(Color color) {
        Preconditions.checkArgument(color!=null);
        switch (color){
            case BLACK:
                return Card.BLACK;
            case VIOLET:
                return Card.VIOLET;
            case BLUE:
                return Card.BLUE;
            case GREEN:
                return Card.GREEN;
            case YELLOW:
                return Card.YELLOW;
            case ORANGE:
                return Card.ORANGE;
            case RED:
                return Card.RED;
            case WHITE:
                return Card.WHITE;
            default:
                 throw new IllegalArgumentException();
        }
    }

}
