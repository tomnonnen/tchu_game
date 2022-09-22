package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static ch.epfl.tchu.game.Player.TurnKind.*;
import static javafx.application.Platform.runLater;

/**
 * A adapter between the graphical interface of the player and the game itself or the proxy(client)
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
final public class GraphicalPlayerAdapter implements Player {
    private GraphicalPlayer graphicalPlayer;
    private final BlockingQueue<TurnKind> turnKindQueue;
    private final BlockingQueue<SortedBag<Ticket>> ticketsQueue;
    private final BlockingQueue<Integer> cardQueue;
    private final BlockingQueue<SortedBag<Card>> cardsBagQueue;
    private final BlockingQueue<Route> routesQueue;

    private final static int ARRAY_BLOCKING_QUEUE_CAPACITY = 1;

    /**
     * constructor
     * creation(initialization) of size one queues intended to contain the player's choices
     */
    public GraphicalPlayerAdapter(){
        this.turnKindQueue = new ArrayBlockingQueue<>(ARRAY_BLOCKING_QUEUE_CAPACITY);
        this.ticketsQueue = new ArrayBlockingQueue<>(ARRAY_BLOCKING_QUEUE_CAPACITY);
        this.cardQueue = new ArrayBlockingQueue<>(ARRAY_BLOCKING_QUEUE_CAPACITY);
        this.cardsBagQueue = new ArrayBlockingQueue<>(ARRAY_BLOCKING_QUEUE_CAPACITY);
        this.routesQueue = new ArrayBlockingQueue<>(ARRAY_BLOCKING_QUEUE_CAPACITY);
    }

    /**
     * called at the start of the game to communicate to the player its  identity(ownId),
     * as well as the names of the various players, including its own, that are in playerNames,
     * @param ownId the id of the player
     * @param playerNames the player's names
     */
    @Override
    public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        runLater(() -> this.graphicalPlayer = new GraphicalPlayer(ownId, playerNames)
        );
    }

    /**
     * called each time a piece of information must be communicated to the player during the game;
     * this information is given in the form of a character string
     * @param info the text string to display to the player
     */
    @Override
    public void receiveInfo(String info) {
        runLater(() -> graphicalPlayer.receiveInfo(info));
    }

    /**
     * called whenever the game state has changed, to inform the player of the public component of the
     * game state, newState, as well as of its own state
     * @param publicGameState the new State of the game
     * @param ownState the new state of the player
     */
    @Override
    public void updateState(PublicGameState publicGameState, PlayerState ownState) {
        runLater(() -> graphicalPlayer.setState(publicGameState,ownState));
    }

    /**
     * called at the start of the game to inform the player of the five tickets that have
     * been distributed to him
     * @param tickets the tickets distributed to the player
     */
    @Override
    public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
        ActionHandlers.ChooseTicketsHandler chooseTicketsHandler = (SortedBag<Ticket> ts)->{
            try {
                ticketsQueue.put(ts);
            } catch (InterruptedException e) {
                throw new Error();
            }
        };
        runLater(() -> graphicalPlayer.chooseTickets(tickets, chooseTicketsHandler));
    }


    /**
     * called at the start of the game to ask the player which of the tickets they were initially
     * dealt (via the previous method) they are keeping
     * @return the tickets that as been drawn
     */
    @Override
    public SortedBag<Ticket> chooseInitialTickets() {
        try {
            return ticketsQueue.take();
        } catch (InterruptedException e) {
            throw new Error();
        }
    }

    /**
     * called at the beginning of a player's turn, to find out what type of action
     * they wish to perform during that turn
     * @return the action the player want to perform
     * @throws Error – if interrupted while waiting
     */
    @Override
    public TurnKind nextTurn() {
        ActionHandlers.DrawTicketsHandler drawTicketsHandler = ()->{
            try {
                turnKindQueue.put(DRAW_TICKETS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        ActionHandlers.DrawCardHandler drawCardHandler = (int emplacement)->{
            try {
                turnKindQueue.put(DRAW_CARDS);
                cardQueue.put(emplacement);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        ActionHandlers.ClaimRouteHandler claimRouteHandler = (Route route, SortedBag<Card> cards)->{
            try {
                turnKindQueue.put(CLAIM_ROUTE);
                routesQueue.put(route);
                cardsBagQueue.put(cards);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        //call of startTurn from the graphicalPlayer with the handler redefined putting the action done in a queue
        runLater(() -> graphicalPlayer.startTurn(drawTicketsHandler,drawCardHandler,claimRouteHandler));
        try {
            return turnKindQueue.take();
        } catch (InterruptedException e) {
           throw new Error();
        }
    }

    /**
     * called when the player has decided to draw additional tickets during the game,
     * in order to tell him which tickets have been drawn and which ones he is keeping
     * @param options the tickets that can be choose
     * @return the tickets chosen
     */
    @Override
    public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
        setInitialTicketChoice(options);
        return chooseInitialTickets();
    }

    /**
     * called when the player has decided to draw car/locomotive cards,
     * in order to know where he wants to draw them from
     * @return the index of the face up card drawn (0->4) ou -1 if the card is drawn from the deck
     */
    @Override
    public int drawSlot() {

        try {
            if(!cardQueue.isEmpty()){
                    return cardQueue.take();
            }else{
                ActionHandlers.DrawCardHandler drawCardHandler = (int emplacement)->{
                    try {
                        cardQueue.put(emplacement);
                    } catch (InterruptedException e) {
                        throw new Error();
                    }
                };
                runLater(() -> graphicalPlayer.drawCard(drawCardHandler));
            }
            return cardQueue.take();

        } catch (InterruptedException e) {
            throw new Error();
        }
    }

    /**
     * called when the player has decided to (attempt to) take a route, in order
     * to know which route it is,
     * @return the route the player want to claim
     */
    @Override
    public Route claimedRoute() {
        try {
            return routesQueue.take();
        } catch (InterruptedException e) {
            throw new Error();
        }
    }

    /**
     * called when the player has decided to (attempt to) grab a road,
     * in order to know which card(s) they initially want to use for this,
     * @return the card initially use to claim the route
     */
    @Override
    public SortedBag<Card> initialClaimCards() {
        try {
            return cardsBagQueue.take();
        } catch (InterruptedException e) {
            throw new Error();
        }
    }

    /**
     * called when the player has decided to attempt to seize a tunnel and additional cards are needed,
     * in order to know which card(s) they wish to use for this, with the possibilities passed to them as an argument;
     * @param options card in which the choice must be made
     * @return the chosen card -> if empty the claim of the route is abandoned
     */
    @Override
    public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {

        ActionHandlers.ChooseCardsHandler chooseCardsHandler = (SortedBag<Card> cards)->{
            try {
                cardsBagQueue.put(cards);
            } catch (InterruptedException e) {
                throw new Error();
            }
        };
        runLater(() -> graphicalPlayer.chooseAdditionalCards(options, chooseCardsHandler));

        try {
            return cardsBagQueue.take();  //blocking while waiting for the player to make his choice (handler executed and chosen cards put in the queue)
        } catch (InterruptedException e) {
           throw new Error();
        }
    }
}
