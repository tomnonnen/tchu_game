package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public class PublicGameState {

    private final int ticketsCount;
    private final PublicCardState publicCardState;
    private final PlayerId currentPlayerId;
    private final Map<PlayerId, PublicPlayerState> publicPlayerState;
    private final PlayerId lastPlayer;

    /**
     * @param ticketsCount: the size of the ticket deck
     * @param cardState: the public state of the wagon/locomotive cards
     * @param currentPlayerId: the current player
     * @param playerState: contains the public state of the players
     * @param lastPlayer: the identity of the last player
     * @throws IllegalArgumentException if the size of the deck is strictly negative or if playerState does not contain
     * @throws IllegalArgumentException if the size of the deck is strictly negative or if playerState does not contain * exactly two key/value pairs,
     * @throws NullPointerException if one of the other arguments (except lastPlayer!) is null.
     */
    public PublicGameState(int ticketsCount, PublicCardState cardState, PlayerId currentPlayerId, Map<PlayerId, PublicPlayerState> playerState, PlayerId lastPlayer){
        if(cardState==null || currentPlayerId==null|| playerState==null) throw new NullPointerException();
        Preconditions.checkArgument(ticketsCount >=0 && playerState.size()==PlayerId.getNbrPlayer());

        this.ticketsCount = ticketsCount;
        this.publicCardState =cardState;
        this.currentPlayerId = currentPlayerId;
        this.publicPlayerState = Map.copyOf(playerState);
        this.lastPlayer = lastPlayer; //which may be null if this identity is still unknown

    }

    /**
     * @return the size of the banknote deck
     */
    public int ticketsCount(){
        return ticketsCount;
    }

    /**
     * @return true if it is possible to draw tickets, i.e. if the deck is not empty,
     */
    public boolean canDrawTickets(){
        return ticketsCount>0;
    }

    /**
     * @return the public part of the wagon/locomotive card status,
     */
    public PublicCardState cardState(){
        return publicCardState;
    }

    /**
     * @return true if it is possible to draw cards, i.e. if the deck and the discard pile contain at least 5 cards between them
     */
    public boolean canDrawCards(){
        return (publicCardState.discardsSize() + publicCardState.deckSize() >= 5);
    }

    /**
     * @return the identity of the current player
     */
    public PlayerId currentPlayerId(){
        return currentPlayerId;
    }

    /**
     * @param playerId: identity of a player
     * @return the public part of the player's state with the given identity
     */
    public PublicPlayerState playerState(PlayerId playerId){
        return publicPlayerState.get(playerId);
    }

    /**
     * @return the public part of the current player's state
     */
    public PublicPlayerState currentPlayerState(){
        return publicPlayerState.get(currentPlayerId);
    }

    /**
     * @return all the roads that either player has taken
     */
    public List<Route> claimedRoutes(){
        List<Route> claimedRoutes = new ArrayList<>();

        for(PlayerId player : publicPlayerState.keySet()) {
            claimedRoutes.addAll(publicPlayerState.get(player).routes());
        }

        return claimedRoutes;

    }

    /**
     * @return the identity of the last player, or null if it is not yet known because the last round has not started.
     */
    public PlayerId lastPlayer(){
        return lastPlayer;
    }
}
