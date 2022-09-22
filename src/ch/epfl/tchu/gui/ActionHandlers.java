package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.Route;
import ch.epfl.tchu.game.Ticket;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public interface ActionHandlers {

    /**
     *DrawTicketsHandler, whose abstract method, named onDrawTickets and taking no arguments, is called when the player
     * wants to draw tickets
     */
    @FunctionalInterface
    interface DrawTicketsHandler{
        void onDrawTickets();
    }

    /**
     * DrawCardHandler, whose abstract method, named onDrawCard and taking a slot number (0 to 4, or -1 for the deck),
     * is called when the player wants to draw a card from the given slot
     */
    @FunctionalInterface
    interface DrawCardHandler{
        void onDrawCard(int emplacement);
    }

    /**
     * ClaimRouteHandler, whose abstract method, named onClaimRoute and taking as argument a route and a multiset of maps,
     * is called when the player wants to seize the given route using the given (initial) maps
     */
    @FunctionalInterface
    interface ClaimRouteHandler{
        void onClaimRoute(Route route, SortedBag<Card> cards);
    }

    /**
     * ChooseTicketsHandler, whose abstract method, named onChooseTickets and taking a multiset of tickets as argument,
     * is called when the player has chosen to keep the tickets given to him after a draw
     */
    @FunctionalInterface
    interface ChooseTicketsHandler{
        void onChooseTickets(SortedBag<Ticket> tickets);
    }

    /**
     * ChooseCardsHandler, whose abstract method, named onChooseCards and taking as argument a multiset of cards, is called
     * when the player has chosen to use the given cards as initial or additional cards when taking possession of a road;
     * if they are additional cards, then the multiset can be empty, which means that the player gives up taking possession
     * of the tunnel.
     */
    @FunctionalInterface
    interface ChooseCardsHandler{
        void onChooseCards(SortedBag<Card> cards);
    }
}
