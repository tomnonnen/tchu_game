package ch.epfl.tchu.extension;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import ch.epfl.tchu.gui.ObservableGameState;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class BotPlayer implements Player {
    private Pair<Trail, Ticket> targetTrail;

    private ObservableGameState observableGameState;
    private final BlockingQueue<SortedBag<Ticket>> ticketsQueue;
    private final BlockingQueue<Integer> cardQueue;
    private final BlockingQueue<SortedBag<Card>> cardsBagQueue;
    private final BlockingQueue<Route> routesQueue;

    /**
     * constructor
     * creation(initialization) of size one queues intended to contain the bot choices
     */
    public BotPlayer() {
        this.ticketsQueue = new ArrayBlockingQueue<>(1);
        this.cardQueue = new ArrayBlockingQueue<>(1);
        this.cardsBagQueue = new ArrayBlockingQueue<>(1);
        this.routesQueue = new ArrayBlockingQueue<>(1);

    }

    /**
     * creation of a new ObservableGameState belonging to the bot
     * @param ownId the id of the bot
     * @param playerNames the players names -> ignored
     */
    @Override
    public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        observableGameState = new ObservableGameState(ownId);
    }

    /**
     * we display the info in the console
     * @param info the text string to display to the bot
     */
    @Override
    public void receiveInfo(String info) {
        System.out.println(info);/*only printing the info in the console do nothing else , don't care about what the other player are doing */ }

    /**
     * update the observableGameState of the bot that was created in initPlayer()
     * @param publicGameState the new State of the game
     * @param ownState the new state of the bot
     */
    @Override
    public void updateState(PublicGameState publicGameState, PlayerState ownState) {
        observableGameState.setState(publicGameState, ownState);
    }

    /**
     *  pick the random n first tickets that the bot can claim
     * @param tickets the tickets distributed to the bot
     */
    @Override
    public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
        Random rng = new Random();
        try {
            ticketsQueue.put(SortedBag.of(tickets.toList().subList(0,2+rng.nextInt(tickets.size()-2))));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
     * browse the tickets and for those that have not been captured
     * call the shortestTrail () method with all the routes owned
     * or claimable (without taking into account the cards(wagon/locomotive))
     * and the start and end station of the ticket.
     * @return the target trail for the bot corresponding to one of his ticket else if not found return null
     */
    private Pair<Trail,Ticket> generateTargetTrail(){
        for(Ticket ticket : observableGameState.ticketPlayer()){
            if(observableGameState.ticketsPlayerPoints().get(ticket)<0){
                List<Route> routeCapturable = new ArrayList<>();
                for (Route route :ChMap.routes()) {
                    if(observableGameState.routesOwner(route).get()==null || observableGameState.routesOwner(route).get()==observableGameState.playerId()){
                        routeCapturable.add(route);
                    }
                }
                for(Trip trip : ticket.getTrip()){
                    Trail shortTrail = Trail.shortestTrail(routeCapturable,trip.from(),trip.to());
                    if(shortTrail.length()!=0){
                        return new Pair<>(shortTrail,ticket);
                    }
                }
            }
        }
        return null;
    }


    /**
     * 1. The bot check if he need a new target trail -> hasn't a trail yet or trail not claimable anymore (the opponent player has taken one of the roads belonging to it...)
     * 2. If he need a new target trail, a new target path between 2 stations composing one of the bot tickets will be regenerated using the new method Trail. shortest ()
     * 2.1 -> if no path is creatable for each of the bot tickets or all tickets have been completed the ticket is considered lost and the bot draws new tickets from the ticket deck.
     * 3. If he has a valid target trail , the bot will try to complete his target trail by taking one of the roads belonging to it.
     * 4. In case the bot has a target path but can't take any of the routes in it, it looks in the up faces if one of the cards in it could be used to take a route from its target path if so it takes it, otherwise it draws a card from the deck.
     * @return the action the bot has done (in the same time he put his choice in the corresponding queue
     */
    @Override
    public TurnKind nextTurn() {
        if(targetTrail!=null) System.out.println(targetTrail.getKey());
        Random rng = new Random();
        boolean validTargetTrail=true;
        if(targetTrail!=null && targetTrail.getKey().length()!=0 && observableGameState.ticketsPlayerPoints().get(targetTrail.getValue())<0){ //if the points of the ticket are positive then the bot has already completed the trail(ticket) if the target is null then target not found or not defined and if the trail is of length 0
            for(Route route : targetTrail.getKey().getTrail()){
                Route twinRoute = route.getTwin();
                boolean routeStillCapturable=
                        (observableGameState.routesOwner(route).get()==null ||
                                observableGameState.routesOwner(route).get()==observableGameState.playerId()) &&
                                    (twinRoute==null || observableGameState.routesOwner(twinRoute).get()==null ||
                                        observableGameState.routesOwner(twinRoute).get()==observableGameState.playerId());
                if(!routeStillCapturable) validTargetTrail=false; break; //if he cannot claim one of the card composing his target trail without taking care of his actual cards, the trail isn't valid anymore
            }
        }
        else{
            validTargetTrail=false;
        }
        //if a new trail is need we generate a new one
        if(!validTargetTrail)
            this.targetTrail = generateTargetTrail();


        if(targetTrail!=null){
            //the bot try to claim a route or tunnel of his target trail
            for(Route route : targetTrail.getKey().getTrail()){
                if(observableGameState.routesOwner(route).get()==null && observableGameState.routeCapturable(route).get()){
                    try {
                        List<SortedBag<Card>> possibleClaimCards = observableGameState.possibleClaimCards(route);
                        cardsBagQueue.put(possibleClaimCards.get(rng.nextInt(possibleClaimCards.size())));
                        routesQueue.put(route);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return TurnKind.CLAIM_ROUTE;
                }
            }

            //the bot hadn't the possibility(cards) to claim any route or tunnel so he draw a card of the color of one of the route composing his target trail from the face up card or if not available one of the deck
           try {
               for(Route route : targetTrail.getKey().getTrail()){
                   for (int slot:Constants.FACE_UP_CARD_SLOTS) {
                       if(observableGameState.faceUpCard(slot).get().color()==route.color()) {
                           cardQueue.put(slot);
                            return TurnKind.DRAW_CARDS;
                       }
                   }
               }
               cardQueue.put(-1);
               return TurnKind.DRAW_CARDS;
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }

       return TurnKind.DRAW_TICKETS;
    }

    /**
     * let the bot choose between the tickets he has drawn
     * @param options the tickets from which he can choose
     * @return the ticket the bot is keeping
     */
    @Override
    public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
        setInitialTicketChoice(options);
        return chooseInitialTickets();
    }

    /**
     *
     * @return if the CardQueue is empty the deck slot(-1) else the corresponding place of the card the bot has drawn
     */
    @Override
    public int drawSlot() {
        if(!cardQueue.isEmpty()){
            try {
                return cardQueue.take();
            } catch (InterruptedException e) {
                throw new Error();
            }
        }
        else{
            return Constants.DECK_SLOT;
        }
    }

    /**
     * called when the bot has decided to (attempt to) take a route, in order
     * to know which route it is,
     * @return the route the bot want to claim
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
     * called when the bot has decided to (attempt to) grab a road,
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
     * called when the bot has decided to attempt to seize a tunnel and additional cards are needed,
     * in order to know which card(s) they wish to use for this, with the possibilities passed to them as an argument;
     * @param options card in which the choice must be made
     * @return the chosen card -> if empty the claim of the route is abandoned
     */
    @Override
    public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
        Random rng = new Random();
        return options.get(rng.nextInt(options.size()));
    }
}
