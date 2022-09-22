package ch.epfl.tchu.game;

import java.util.List;
import ch.epfl.tchu.SortedBag;
import java.util.Map;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public interface Player {
    enum TurnKind {
        DRAW_TICKETS, // represents a round in which the player draws tickets
        DRAW_CARDS, //represents a turn in which the player draws railcar/locomotive cards,
        CLAIM_ROUTE; //represents a turn in which the player seizes a road (or at least attempts to do so)

        public final static List<TurnKind> ALL = List.of(values());
    }

    /**
     * called at the start of the game to communicate to the player its  identity(ownId),
     * as well as the names of the various players, including its own, that are in playerNames,
     * @param ownId the id of the player
     * @param playerNames the player's names
     */
    void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames);

    /**
     * called each time a piece of information must be communicated to the player during the game;
     * this information is given in the form of a character string, generally produced by the
     * Info class defined in step 3,
     * @param info the text string to display to the player
     */
    void receiveInfo(String info);

    /**
     * called whenever the game state has changed, to inform the player of the public component of the
     * game state, newState, as well as of its own state
     * @param publicGameState the new State of the game
     * @param ownState the new state of the player
     */
    void updateState(PublicGameState publicGameState, PlayerState ownState);

    /**
     * called at the start of the game to inform the player of the five tickets that have
     * been distributed to him,
     * @param tickets the tickets distributed to the player
     */
    void setInitialTicketChoice(SortedBag<Ticket> tickets);

    /**
     * called at the start of the game to ask the player which of the tickets they were initially
     * dealt (via the previous method) they are keeping,
     * @return the tickets that as been drawn
     */
    SortedBag<Ticket> chooseInitialTickets();

    /**
     * called at the beginning of a player's turn, to find out what type of action
     * they wish to perform during that turn
     * @return the action the player want to perform
     */
    TurnKind nextTurn();

    /**
     * called when the player has decided to draw additional tickets during the game,
     * in order to tell him which tickets have been drawn and which ones he is keeping,
     * @param options the tickets from which he can choose
     * @return the tickets chosen
     */
    SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options);

    /**
     * called when the player has decided to draw car/locomotive cards,
     * in order to know where he wants to draw them from: from one of the slots containing
     * a face-up card - in which case the value returned is between 0 and 4 inclusive -,
     * or from the deck - in which case the value returned is Constants.DECK_SLOT (i.e. -1),
     * @return the index of the face up card draw (0->4) ou -1 if the card is drawn from the deck
     */
    int drawSlot();

    /**
     * called when the player has decided to (attempt to) take a route, in order
     * to know which route it is,
     * @return the route the player want to claim
     */
    Route claimedRoute();

    /**
     * called when the player has decided to (attempt to) grab a road,
     * in order to know which card(s) they initially want to use for this
     * @return card that he initially uses to take the road
     */
    SortedBag<Card> initialClaimCards();

    /**
     * called when the player has decided to attempt to seize a tunnel and additional cards are needed,
     * in order to know which card(s) they wish to use for this, with the possibilities passed to them as an argument;
     * if the returned multiset is empty, it means that the player does not (or cannot) wish to choose any of
     * the possibilities.
     * @param options in which the choice must be made
     * @return the chosen maps -> if empty the route capture is aborted
     */
    SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options);
}
