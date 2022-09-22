package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.extension.MenuViewCreator;

import java.util.List;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public enum PlayerId {
    PLAYER_1(), // represents the identity of player 1
    PLAYER_2(), // represents the identity of player 2
    PLAYER_3(), // represents the identity of player 3
    PLAYER_4(), // represents the identity of player 4
    PLAYER_5(); // represents the identity of player 5

    private static int COUNT = 5; //storage of the size of the ALL list
    private static List<PlayerId> ALL = List.of(values());//create a new list of PlayerId which is adapted by the nr of players that the host have choose
    private static final int MIN_NR_PLAYERS = 2;
    private static int nrChange = 0;
    /**
     * The nbr of player must be between 2 and the size of the enum (maximum of entry) and also can only be change one time
     * @param nbrPlayer the new number of players that we want to set
     */
    public static void setNbrPlayer(int nbrPlayer){
        nrChange++;
        Preconditions.checkArgument(nbrPlayer>=MIN_NR_PLAYERS && nbrPlayer<=values().length && nrChange == 1);
        COUNT=nbrPlayer;
        ALL = List.of(values()).subList(0,COUNT);
    }

    /**
     * @return the number of players in the game
     */
    public static int getNbrPlayer() {
        return COUNT;
    }

    public static List<PlayerId> getAllPlayer() {
        return List.copyOf(ALL);
    }

    /**
     * @return the identity of the player who follows the one to whom it is applied
     */
    public PlayerId next(){
        return ALL.get((this.ordinal()+1) % COUNT);
    } // next for multiple players


}
