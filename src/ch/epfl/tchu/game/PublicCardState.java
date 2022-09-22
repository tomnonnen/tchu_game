package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.List;
import java.util.Objects;

import static ch.epfl.tchu.game.Constants.FACE_UP_CARDS_COUNT;


/**
 * represents (part of) the state of the car/locomotive cards that are not in the player's hands (public <-> private (CardState))
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public class PublicCardState {

    private final List<Card> faceUpCards;
    private final int deckSize;
    private final int discardsSize;

    /**
     * builds a public state of the cards
     * @param faceUpCards the face-up cards are those given => cards placed face-up on the table
     * @param deckSize number of cards in the deck
     * @param discardsSize nbr of cards from the discard pile
     * @throws IllegalArgumentException if faceUpCards does not contain the correct number of elements (5)
     * or if the size of the deck
     * or the discard pile are negative (< 0).
     */
    public PublicCardState(List<Card> faceUpCards, int deckSize, int discardsSize){
        Preconditions.checkArgument(faceUpCards.size()==FACE_UP_CARDS_COUNT && deckSize>=0 && discardsSize>=0);
        this.faceUpCards = List.copyOf(faceUpCards);
        this.deckSize = deckSize;
        this.discardsSize = discardsSize;
    }



    /**
     *
     * @return the 5 cards face up (5 items)
     */
    public List<Card> faceUpCards(){
        return faceUpCards;
    }

    /**
     * @param slot index of the face-up card to be returned
     * @return the face-up card at the given index
     * @throws IndexOutOfBoundsException if this index is not between 0 (included) and 5 (excluded)
     */
    public Card faceUpCard(int slot){
        Objects.checkIndex(slot,FACE_UP_CARDS_COUNT);
        return faceUpCards.get(slot);
    }

    /**
     * @return size of the deck pile
     */
    public int deckSize(){
        return deckSize;
    }

    /**
     * @return true if the deck is empty
     */
    public boolean isDeckEmpty(){
        return deckSize==0;
    }

    /**
     * @return size of the discard pile
     */
    public int discardsSize(){
        return discardsSize;
    }
}
