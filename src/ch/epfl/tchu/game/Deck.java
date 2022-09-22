package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.*;

/**
 * pile of cards of all types (not only wagon/locomotive)
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class Deck<C extends Comparable<C>> {


    private final List<C> cards;

    /**
     * private constructor
     * @param cards cards composing the deck
     */
    private Deck(List<C> cards){
        this.cards = List.copyOf(cards);
    }

    /**
     * @param cards multi-set of cards to mix
     * @param rng random number generator
     * @param <C> card type
     * @return a pile of cards with the same cards as the multiset cards,
     * shuffled using the rng random number generator
     */
    public static <C extends Comparable<C>> Deck<C> of(SortedBag<C> cards, Random rng){
        ArrayList<C> listCard = new ArrayList<>(cards.toList());
        Collections.shuffle(listCard,rng);
        return new Deck<>(listCard);
    }

    /**
     * @return  size of the deck (number of cards it contains)
     */
    public int size(){
        return cards.size();
    }

    /**
     * @return true if the deck is empty
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * @return the card at the top of the deck
     * @throws IllegalArgumentException if the deck is empty
     */
    public C topCard(){
        Preconditions.checkArgument(!isEmpty());
        return cards.get(0);            //supposes that the first card is the card from the top of the deck
    }

    /**
     * @return a deck identical to the receiver (this) but without the card on top
     * @throws IllegalArgumentException if the deck is empty
     */
    public Deck<C> withoutTopCard() {
        Preconditions.checkArgument(!isEmpty());
        return new Deck<>(List.copyOf(cards).subList(1,size()));
    }

    /**
     *
     * @param count number of cards to return (draw)
     * @return a multi-set containing the count cards on top of the pile
     * @throws IllegalArgumentException if count is not between 0 (included) and the deck size (included)
     */
    public SortedBag<C> topCards(int count){
        Preconditions.checkArgument(count>=0 && count<=size());
        return SortedBag.of(List.copyOf(cards).subList(0,count));//first index included last not included(count)

    }

    /**
     * @param count number of cards to ignore from the top of the stack
     * @return a deck identical to the receiver (this) but without the count cards from the top
     * @throws IllegalArgumentException if count is not between 0 (included) and the deck size (included)
     */
    public Deck<C> withoutTopCards(int count){
        Preconditions.checkArgument(count>=0 && count<=size());
        return new Deck<>(List.copyOf(cards).subList(count,size()));
    }
}
