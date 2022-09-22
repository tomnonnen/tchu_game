package ch.epfl.tchu.game;


import java.util.List;


/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public enum Color {
    // the 8 colors :
    BLACK,
    VIOLET,
    BLUE,
    GREEN,
    YELLOW,
    ORANGE,
    RED,
    WHITE;

    public final static List<Color> ALL = List.of(values()); //enter the 8 colors in the list
    public final static int COUNT = ALL.size(); //storage the size of the ALL list in this variable

}
