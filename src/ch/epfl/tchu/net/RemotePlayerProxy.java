package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ch.epfl.tchu.net.Serdes.*;
import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * A class that make the relation between a game and a network player
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class RemotePlayerProxy implements Player {

    private final BufferedReader r;
    private final BufferedWriter w;

    /**
     * constructor that initializes the BufferedReader, and the BufferedWriter with the socket given
     * @param socket : socket of the server
     */
    public RemotePlayerProxy(Socket socket) {
        try {
            r = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(),
                            US_ASCII));
            w = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(),
                            US_ASCII));
        }  catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * check if it receive a message
     * @return the received message
     */
    private String receiveMessage(){
        try {
            return r.readLine();
        }catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * send a message to the remotePlayerClient
     * @param message : send this message to the stream
     */
    private void sendMessage(String message){
        try{
            w.write(message);
            w.write('\n');
            w.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     *  which is called at the start of the game to communicate to the player its  identity(ownId),
     *  as well as the names of the various players, including its own, that are in playerNames,
     * @param ownId : identity of the player
     * @param playerNames : the names of each player
     */
    @Override
    public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {

        List<String> playerNamesString = new ArrayList<>();
        PlayerId.getAllPlayer().forEach(playerId->playerNamesString.add(playerNames.get(playerId)));

        String message = String.format("%s %s %s",
                MessageId.INIT_PLAYERS.name(),
                playerIdSerde.serialize(ownId),
                listStringSerde.serialize(playerNamesString));

        sendMessage(message);
    }


    /**
     *which is called each time a piece of information must be communicated to the player during the game;
     * this information is given in the form of a character string, generally produced by the Info class defined in step 3,
     * @param info : the info to share
     */
    @Override
    public void receiveInfo(String info) {
        String message = String.format("%s %s", MessageId.RECEIVE_INFO.name(), stringSerde.serialize(info));
        sendMessage(message);
    }

    /**
     *which is called whenever the game state has changed, to inform the player of the public component of this new state,
     * newState, as well as of its own state, ownState,
     * @param newState : the new state of the game
     * @param ownState : the state of the player
     */
    @Override
    public void updateState(PublicGameState newState, PlayerState ownState) {
        String message = String.format("%s %s %s", MessageId.UPDATE_STATE.name(), publicGameStateSerde.serialize(newState), playerStateSerde.serialize(ownState));
        sendMessage(message);
    }

    /**
     *which is called at the start of the game to inform the player of the five tickets that have been distributed to him
     *
     * @param tickets : the five tickets that have been distributed to the player
     */
    @Override
    public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
        String message = String.format("%s %s", MessageId.SET_INITIAL_TICKETS.name(), sortedBagTicketSerde.serialize(tickets));
        sendMessage(message);
    }

    /**
     *which is called at the start of the game to ask the player which of the tickets they were initially dealt
     *
     * @return the tickets that the player have choose
     */
    @Override
    public SortedBag<Ticket> chooseInitialTickets() {
        sendMessage(MessageId.CHOOSE_INITIAL_TICKETS.name());
        return sortedBagTicketSerde.deserialize(receiveMessage());
    }

    /**
     *which is called at the beginning of a player's turn, to find out what type of action they wish to perform during
     * that turn
     *
     * @return the type of action that the player plays
     */
    @Override
    public TurnKind nextTurn() {
        sendMessage(MessageId.NEXT_TURN.name());
        return turnKindSerde.deserialize(receiveMessage());
    }

    /**
     *which is called when the player has decided to draw additional tickets during the game, in order to tell him which
     * tickets have been drawn and which ones he is keeping,
     * @param options : the tickets that have been drawn
     * @return the choice of the tickets
     */
    @Override
    public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
        String message = String.format("%s %s", MessageId.CHOOSE_TICKETS.name(), sortedBagTicketSerde.serialize(options));
        sendMessage(message);

        return sortedBagTicketSerde.deserialize(receiveMessage());
    }

    /**
     *which is called when the player has decided to draw car/locomotive cards, in order to know where he wants to draw
     * them from: from one of the slots containing a face-up card - in which case the value returned is between 0 and 4
     * inclusive -, or from the deck - in which case the value returned is Constants.DECK_SLOT (i.e. -1),
     *
     * @return the choice of the player
     */
    @Override
    public int drawSlot() {
        sendMessage(MessageId.DRAW_SLOT.name());
        return intSerde.deserialize(receiveMessage());
    }

    /**
     *which is called when the player has decided to (attempt to) take a route, in order to know which route it is
     *
     * @return the route that the player have claimed
     */
    @Override
    public Route claimedRoute() {
        sendMessage(MessageId.ROUTE.name());
        return routeSerde.deserialize(receiveMessage());
    }

    /**
     *which is called when the player has decided to (attempt to) grab a road, in order to know which card(s) they
     * initially want to use for this
     * @return the choices cards of the player that want to grab the road
     */
    @Override
    public SortedBag<Card> initialClaimCards() {
        sendMessage(MessageId.CARDS.name());
        return sortedBagCardSerde.deserialize(receiveMessage());
    }

    /**
     *which is called when the player has decided to attempt to seize a tunnel and additional cards are needed,
     * in order to know which card(s) they wish to use for this, with the possibilities passed to them as an argument;
     * if the returned multiset is empty, it means that the player does not (or cannot) wish to choose any of the possibilities.
     * @param options : the cards that have been drawn
     * @return the choice of the player
     */
    @Override
    public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
        String message = String.format("%s %s", MessageId.CHOOSE_ADDITIONAL_CARDS.name(), listSortedBagCardSerde.serialize(options));
        sendMessage(message);

        return sortedBagCardSerde.deserialize(receiveMessage());
    }
}
