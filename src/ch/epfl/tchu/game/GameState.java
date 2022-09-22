package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.*;

import static ch.epfl.tchu.game.Constants.INITIAL_CARDS_COUNT;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class GameState extends PublicGameState{


    private final Map<PlayerId, PlayerState> globalPlayerState;
    private final CardState cardState;
    private final Deck<Ticket> ticketsDeck;

    /**
     * @param cardState original private state of the card
     * @return the public state of the private card
     */
    private static PublicCardState makeCardStatePublic(CardState cardState){
        return new PublicCardState(cardState.faceUpCards(), cardState.deckSize(), cardState.discardsSize());
    }

    /**
     * @param playerState original private state of the player
     * @return the public state of the private state of the player
     */
    private static Map<PlayerId, PublicPlayerState> makePlayerStatePublic(Map<PlayerId, PlayerState> playerState){
        Map<PlayerId, PublicPlayerState> publicPlayerState = new EnumMap<>(PlayerId.class);

        for (Map.Entry<PlayerId, PlayerState> player: playerState.entrySet()) { //enable the possibility to add more than two player by only changing the enum
            PlayerState playerStateValue = player.getValue();
            publicPlayerState.put(player.getKey(),new PublicPlayerState(playerStateValue.ticketCount(),playerStateValue.cardCount(),playerStateValue.routes()));
        }

        return publicPlayerState;
    }

    /**
     * constructor
     */
    private GameState(Deck<Ticket> ticketsDeck, CardState cardState, PlayerId currentPlayerId, Map<PlayerId, PlayerState> globalPlayerState, PlayerId lastPlayer){
        super(ticketsDeck.size(),makeCardStatePublic(cardState),currentPlayerId,makePlayerStatePublic(globalPlayerState),lastPlayer);
        this.globalPlayerState = Map.copyOf(globalPlayerState);
        this.cardState = cardState;
        this.ticketsDeck = ticketsDeck;
    }

    /**
     * @param tickets tickets from which the deck will be built and the 8 tickets from the players
     * @param rng random generator used to mix the decks and choose the first player
     * @return the initial state of a game of tCHu in which the ticket deck contains the given tickets and the card deck contains the Constants.ALL_CARDS cards, without the top 8 (2×4), distributed to the players
     */
    public static GameState initial(SortedBag<Ticket> tickets, Random rng){

        Deck<Card> cardsDeck = Deck.of(Constants.ALL_CARDS,rng);
        Deck<Ticket> ticketDeck = Deck.of(tickets,rng);
        Map<PlayerId, PlayerState> playerMap = new EnumMap<>(PlayerId.class);

        for(PlayerId player : PlayerId.getAllPlayer()) { //enable the possibility to add more than two player by only changing the enum
            playerMap.put(player, PlayerState.initial(cardsDeck.topCards(INITIAL_CARDS_COUNT)));
            cardsDeck = cardsDeck.withoutTopCards(INITIAL_CARDS_COUNT);
        }

        return new GameState(ticketDeck,CardState.of(cardsDeck),PlayerId.getAllPlayer().get(rng.nextInt(PlayerId.getNbrPlayer())),playerMap,null);
    }

    /**
     *
     * @param playerId player from which the state is returned
     * @return the full state of the given identity player
     */
    @Override
    public PlayerState playerState(PlayerId playerId){
        return globalPlayerState.get(playerId);
    }

    /**
     * @return the full state of the current player
     */
    @Override
    public PlayerState currentPlayerState(){
        return globalPlayerState.get(currentPlayerId());
    }

    /**
     * @param count nbr of ticket to return
     * @return the count tickets from the top of the deck
     * @throws IllegalArgumentException if count is not between 0 and the size of the deck (inclusive),
     */
    public SortedBag<Ticket> topTickets(int count){
        Preconditions.checkArgument(count>=0 && count <=ticketsDeck.size());
        return ticketsDeck.topCards(count);
    }

    /**
     * @param count nbr of ticket to ignore
     * @return a state identical to the receiver, but without the count notes from the top of the deck
     * @throws IllegalArgumentException if count is not between 0 and the size of the deck (inclusive)
     */
    public GameState withoutTopTickets(int count){
        Preconditions.checkArgument(count>=0 && count <=ticketsDeck.size());
        return new GameState(ticketsDeck.withoutTopCards(count),cardState,currentPlayerId(),globalPlayerState,lastPlayer());
    }

    /**
     * @return the card at the top of the deck
     * @throws IllegalArgumentException if the deck is empty
     */
    public Card topCard(){
        Preconditions.checkArgument(!cardState.isDeckEmpty());
        return cardState.topDeckCard();
    }

    /**
     * @return a state identical to the receiver but without the card on top of the deck
     * @throws IllegalArgumentException if the deck is empty
     */
    public GameState withoutTopCard(){
        Preconditions.checkArgument(!cardState.isDeckEmpty());
        return new GameState(ticketsDeck,cardState.withoutTopDeckCard(),currentPlayerId(),globalPlayerState,lastPlayer());
    }

    /**
     * @param discardedCards card to add to the discards
     * @return a state identical to the receiver but with the given cards added to the discard
     */
    public GameState withMoreDiscardedCards(SortedBag<Card> discardedCards){
        return new GameState(ticketsDeck,cardState.withMoreDiscardedCards(discardedCards),currentPlayerId(),globalPlayerState,lastPlayer());
    }

    /**
     * @param rng random generator
     * @return an identical state to the receiver unless the deck is empty, in which case it is recreated from the discard pile, shuffled using the given random generator.
     */
    public GameState withCardsDeckRecreatedIfNeeded(Random rng){
        if(!cardState.isDeckEmpty()) return this;
        return new GameState(ticketsDeck,cardState.withDeckRecreatedFromDiscards(rng),currentPlayerId(),globalPlayerState,lastPlayer());
    }

    private GameState withPlayerAddedTickets(PlayerId playerId, SortedBag<Ticket> ticketsToAdd){
        EnumMap<PlayerId, PlayerState> newGlobalPlayerState = new EnumMap<>(globalPlayerState);
        newGlobalPlayerState.replace(playerId,globalPlayerState.get(playerId).withAddedTickets(ticketsToAdd));
        return new GameState(ticketsDeck,cardState,currentPlayerId(),newGlobalPlayerState,lastPlayer());
    }

    /**
     * The 5 tickets initially distributed to the players will already have been extracted from the deck using the withoutTopTickets method,
     * The only purpose of withInitiallyChosenTickets is to modify the player's state to store the subset of these 5 tickets that he chose to keep.
     * @param playerId id of the player that keep the tickets
     * @param chosenTickets ticket that he keep
     * @return a state identical to the receiver but in which the chosen tickets have been added to the hand of the chosen player
     * @throws IllegalArgumentException if the player in question already has at least one ticket.
     */
    public GameState withInitiallyChosenTickets(PlayerId playerId, SortedBag<Ticket> chosenTickets){
        Preconditions.checkArgument(globalPlayerState.get(playerId).tickets().size() < 1); //player as strictly less than one ticket = > IllegalArgumentException
        return withPlayerAddedTickets(playerId, chosenTickets);
    }
    /**
     * @param drawnTickets ticket that the player drawn
     * @param chosenTickets ticket chosen from the player
     * @return a state identical to the receiver, but in which the current player has drawn the drawnTickets from the top of the deck, and chosen to keep those contained in chosenTicket
     * @throws IllegalArgumentException if the set of tickets kept is not included in the set of tickets drawn
     */
    public GameState withChosenAdditionalTickets(SortedBag<Ticket> drawnTickets, SortedBag<Ticket> chosenTickets){
        Preconditions.checkArgument(drawnTickets.contains(chosenTickets));
        return withoutTopTickets(drawnTickets.size()).withPlayerAddedTickets(currentPlayerId(),chosenTickets);
    }

    private GameState withPlayerAddedCards(PlayerId playerId, Card cardToAdd){
        EnumMap<PlayerId, PlayerState> newGlobalPlayerState = new EnumMap<>(globalPlayerState);
        newGlobalPlayerState.replace(playerId,globalPlayerState.get(playerId).withAddedCard(cardToAdd));
        return new GameState(ticketsDeck,cardState,currentPlayerId(),newGlobalPlayerState,lastPlayer());
    }

    /**
     * @param slot index of the faceUpCard to replace and get
     * @return a state identical to the receiver except that the face-up card
     * @throws IllegalArgumentException if it is not possible to draw cards from the top of the deck
     * @throws IllegalArgumentException if it is not possible to draw cards, i.e. if canDrawCards returns false
     */
    public GameState withDrawnFaceUpCard(int slot){
        return new GameState(ticketsDeck,cardState.withDrawnFaceUpCard(slot),currentPlayerId(),globalPlayerState,lastPlayer()).withPlayerAddedCards(currentPlayerId(),cardState.faceUpCard(slot));
    }

    /**
     * @return a state identical to the receiver except that the top card of the deck has been placed in the current player's hand
     * @throws IllegalArgumentException if it is not possible to draw cards, i.e. if canDrawCards returns false,
     */
    public GameState withBlindlyDrawnCard(){
        return new GameState(ticketsDeck,cardState.withoutTopDeckCard(),currentPlayerId(),globalPlayerState,lastPlayer()).withPlayerAddedCards(currentPlayerId(),cardState.topDeckCard());
    }

    /**
     * @param route route taken
     * @param cards cards used to take the corresponding route
     * @return a state identical to the receiver but in which the current player has seized the given route using the given cards.
     */
    public GameState withClaimedRoute(Route route, SortedBag<Card> cards){
        EnumMap<PlayerId, PlayerState> newGlobalPlayerState = new EnumMap<>(globalPlayerState);
        newGlobalPlayerState.replace(currentPlayerId(),globalPlayerState.get(currentPlayerId()).withClaimedRoute(route,cards));
        return new GameState(ticketsDeck,cardState.withMoreDiscardedCards(cards),currentPlayerId(),newGlobalPlayerState,lastPlayer());
    }

    /**
     * Only be called at the end of a player's turn,
     * @return true if the last turn is starting, i.e. if the identity of the last player is currently unknown but the current player has only two or fewer cars left
     */
    public boolean lastTurnBegins(){
        return lastPlayer()==null && globalPlayerState.get(currentPlayerId()).carCount()<=2;
    }

    /**
     * @return which ends the current player's turn,
     * i.e. returns a state identical to the receiver except that the current player is the one following the current player; moreover, if lastTurnBegins returns true, the current player becomes the last player.
     */
    public GameState forNextTurn(){
        return new GameState(ticketsDeck,cardState,currentPlayerId().next(),globalPlayerState,lastTurnBegins() ? currentPlayerId() : lastPlayer());
    }




}
