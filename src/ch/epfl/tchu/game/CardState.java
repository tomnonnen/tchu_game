package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ch.epfl.tchu.game.Constants.FACE_UP_CARDS_COUNT;


//for (int slot: FACE_UP_CARD_SLOTS) { /* … */ }


/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class CardState extends PublicCardState{

    private final Deck<Card> deck; //the pick : order is important
    private final SortedBag<Card> discards; //the discards : => order does not matter

    /**
     * builds a private and public state of the cards
     * @param faceUpCards  the face-up cards are the given ones => cards placed face-up on the table
     * @param deck specifies the cards in the deck
     * @param discards specifies the cards in the discard pile
     * @throws IllegalArgumentException
     * if faceUpCards does not contain the right number of items (5)
     * or if the size of the deck
     * or the discard pile are negative (< 0).
     */
    private CardState(List<Card> faceUpCards, Deck<Card> deck, SortedBag<Card> discards) {
        super(faceUpCards, deck.size(), discards.size());
        this.deck = deck;
        this.discards = discards;
    }

    /**
     * @param deck the pile of cards at your disposal
     * @return a state in which the 5 cards face up are the first 5 of the given pile and the deck consists of the remaining cards of the pile, and the discard pile is empty
     * @throws IllegalArgumentException if the given pile contains less than 5 cards.
     */
    public static CardState of(Deck<Card> deck){
        Preconditions.checkArgument(deck.size()>=FACE_UP_CARDS_COUNT);
        return new CardState(deck.topCards(FACE_UP_CARDS_COUNT).toList(),deck.withoutTopCards(FACE_UP_CARDS_COUNT), SortedBag.of());
    }

    /**
     * @param slot index of the face-up card to be replaced
     * @return a set of cards identical to the receiver (this), except that the face-up card in the index slot has been replaced
     * by the card at the top of the deck, which is also removed from the deck
     * @throws IndexOutOfBoundsException if the given index is not between 0 (included) and 5 (excluded)
     * @throws IllegalArgumentException if the deck is empty
     */
    public CardState withDrawnFaceUpCard(int slot){
        Preconditions.checkArgument(!deck.isEmpty());
        if(slot<0 || slot>=FACE_UP_CARDS_COUNT) throw new IndexOutOfBoundsException();

        ArrayList<Card> newFaceUpCards = new ArrayList<>(faceUpCards());
        newFaceUpCards.set(slot,deck.topCard());

        return new CardState(newFaceUpCards,deck.withoutTopCard(),discards);
    }

    /**
     * @return the card at the top of the deck
     * @throws IllegalArgumentException if the deck is empty
     */
    public Card topDeckCard(){
        Preconditions.checkArgument(!deck.isEmpty());
        return deck.topCard();
    }

    /**
     * @return a set of cards identical to the receiver (this), but without the top card of the deck
     * @throws IllegalArgumentException if the deck is empty
     */
    public CardState withoutTopDeckCard(){
        Preconditions.checkArgument(!deck.isEmpty());
        return new CardState(faceUpCards(),deck.withoutTopCard(),discards);
    }

    /**
     * @param rng random number generator
     * @return a set of cards identical to the receiver (this), except that the cards from the discard pile have been
     * shuffled using the given random generator to form the new deck
     * @throws IllegalArgumentException if the receiver deck is not empty
     */
    public CardState withDeckRecreatedFromDiscards(Random rng){
        Preconditions.checkArgument(deckSize()==0);
        return new CardState(faceUpCards(), Deck.of(SortedBag.of(discards),rng), SortedBag.of());
    }


    /**
     * @param additionalDiscards card to be added to the discard pile
     * @return a set of cards identical to the receiver (this), but with the given cards added to the discard pile
     */
    public CardState withMoreDiscardedCards(SortedBag<Card> additionalDiscards){
        return new CardState(faceUpCards(), deck, discards.union(additionalDiscards));
    }
}
