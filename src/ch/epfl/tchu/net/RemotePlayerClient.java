package ch.epfl.tchu.net;

import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import static ch.epfl.tchu.net.Serdes.*;
import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * a class making the relation between the graphical interface of a player and the proxy of the player based on the server
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class RemotePlayerClient {

    private final Player player;
    private final BufferedReader r;
    private final BufferedWriter w;

    private final static int FIRST_ARG = 0;
    private final static int SECOND_ARG = 1;
    private final static int THIRD_ARG = 2;

    /**
     * constructor that initializes the BufferedReader, and the BufferedWriter with the socket created with the name and the port given
     * @param player : the player that are the client
     * @param name : the name to connect to the proxy
     * @param port : the port to connect to the proxy
     *
     */
    public RemotePlayerClient(Player player, String name, int port){
        this.player = player;
        try {
            Socket socket = new Socket(name, port);
            r = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                    US_ASCII));
            w = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(),
                            US_ASCII));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * This method performs a loop during which it :
     *
     * -waits for a message from the proxy,
     * -splits it using the space character as separator,
     * -determines the type of the message according to the first string resulting from the splitting,
     * -according to this type of message, deserializes the arguments, calls the corresponding method of the player;
     *   if this method returns a result, serializes it to send it back to the proxy in response.
     *
     */
    public void run(){
        try {
            String currentLine = r.readLine(); // wait to read a line of the stream
            while (currentLine != null) {
                String message = currentLine;
                String[] args = message.split(Pattern.quote(" ")); //we split the message with delimiter " ", args = arguments

                switch (MessageId.valueOf(args[FIRST_ARG])){ //the first argument is the type of message

                    case INIT_PLAYERS:
                        //if the first arg is INIT_PLAYERS then the second arg is the playerId of the own player and the third argument is the map of the players
                        List<String> playerNames = listStringSerde.deserialize(args[THIRD_ARG]);
                        Map<PlayerId,String> nameMap = new HashMap<>();
                        PlayerId.setNbrPlayer(playerNames.size());
                        for(int i=0;i<PlayerId.getNbrPlayer();i++){
                            nameMap.put(PlayerId.getAllPlayer().get(i),playerNames.get(i));
                        }
                        player.initPlayers(playerIdSerde.deserialize(args[SECOND_ARG]), nameMap);

                        break;
                    case RECEIVE_INFO:
                        //if the first arg is RECEIVE_INFO, then the second argument is the info that the player must received
                        player.receiveInfo(stringSerde.deserialize(args[SECOND_ARG]));
                        break;
                    case UPDATE_STATE:
                        //if the first arg is UPDATE_STATE, then the second argument is new State of the game, and the third arg is the PlayerState of the ownPlayer
                        player.updateState(publicGameStateSerde.deserialize(args[SECOND_ARG]),
                                playerStateSerde.deserialize(args[THIRD_ARG]));
                        break;
                    case SET_INITIAL_TICKETS:
                        //if the first arg is SET_INITIAL_TICKETS, then the second arg is the sorted bag of the tickets
                        player.setInitialTicketChoice(sortedBagTicketSerde.deserialize(args[SECOND_ARG]));
                        break;
                    case CHOOSE_INITIAL_TICKETS:
                        //if the first arg is CHOOSE_INITIAL_TICKETS, then we need a answer of the client that send his choice
                        sendMessageProxy(sortedBagTicketSerde.serialize(player.chooseInitialTickets()));
                        break;
                    case NEXT_TURN:
                        //if the first arg is NEXT_TURN, then we need a answer of the client that send his choice for this turn
                        sendMessageProxy(turnKindSerde.serialize(player.nextTurn()));
                        break;
                    case CHOOSE_TICKETS:
                        //if the first arg is CHOOSE_TICKETS, then the second arg is the sorted bag of tickets, with this information, the player must answer and send his choice
                        sendMessageProxy(sortedBagTicketSerde.serialize(
                                player.chooseTickets(sortedBagTicketSerde.deserialize(args[SECOND_ARG]))));
                        break;
                    case DRAW_SLOT:
                        //if the first arg is DRAW_SLOT, then we need a answer of the client that send his choice : deck or index of face up cards
                        sendMessageProxy(intSerde.serialize(player.drawSlot()));
                        break;
                    case ROUTE:
                        //if the first arg is ROUTE, then we need a answer of the client that send his choice of the route
                        sendMessageProxy(routeSerde.serialize(player.claimedRoute()));
                        break;
                    case CARDS:
                        //if the first arg is CARDS, then we need a answer of the client that choose the cards to claim the route
                        sendMessageProxy(sortedBagCardSerde.serialize(player.initialClaimCards()));
                        break;
                    case CHOOSE_ADDITIONAL_CARDS:
                        //if the first arg is CHOOSE_ADDITIONAL_CARDS, then we need a answer of the client that choose the additional cards
                        sendMessageProxy(sortedBagCardSerde.serialize(
                                player.chooseAdditionalCards(listSortedBagCardSerde.deserialize(args[SECOND_ARG]))));
                        break;
                    default:
                        break;
                }
                currentLine = r.readLine(); //at the end of the analysis of the current message, we read the next line
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }


    /**
     * send a message to the proxy of the removePlayerProxy
     * @param message : message to send to the proxy
     */
    private void sendMessageProxy(String message){
        try {
            w.write(message);
            w.write('\n');
            w.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }
}
