package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static ch.epfl.tchu.game.PlayerId.*;

/**
 * all the serde corresponding to the specific object to serialize or deserialize
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public abstract class Serdes {

    //Serde for Integer
    final public static Serde<Integer> intSerde = Serde.of(
            i -> Integer.toString(i),
            Integer::parseInt
    );

    //Serde for String
    final public static Serde<String> stringSerde = Serde.of(
            rawStr -> Base64.getEncoder().encodeToString(rawStr.getBytes(StandardCharsets.UTF_8)),
            serializedStr -> new String(Base64.getDecoder().decode(serializedStr),StandardCharsets.UTF_8)
    );


    //Serdes for one Element
    final public static Serde<PlayerId> playerIdSerde = Serde.oneOf(PlayerId.getAllPlayer());
    final public static Serde<Player.TurnKind> turnKindSerde = Serde.oneOf(Player.TurnKind.ALL);
    final public static Serde<Card> cardSerde = Serde.oneOf(Card.ALL);
    final public static Serde<Route> routeSerde = Serde.oneOf(ChMap.routes());
    final public static Serde<Ticket> ticketSerde = Serde.oneOf(ChMap.tickets());

    //Serdes for List/SortedBag with serde to use and delimiter
    private final static String DELIMITER_STRING = ",";
    private final static String DELIMITER_COMPOSITE = ";";
    private final static String DELIMITER_PUBLIC_GAME_STATE = ":";
    final public static Serde<List<String>> listStringSerde = Serde.listOf(stringSerde,DELIMITER_STRING);
    final public static Serde<List<Card>> listCardSerde = Serde.listOf(cardSerde,DELIMITER_STRING);
    final public static Serde<List<Route>> listRouteSerde = Serde.listOf(routeSerde,DELIMITER_STRING);
    final public static Serde<SortedBag<Card>> sortedBagCardSerde = Serde.bagOf(cardSerde,DELIMITER_STRING);
    final public static Serde<SortedBag<Ticket>> sortedBagTicketSerde = Serde.bagOf(ticketSerde,DELIMITER_STRING);
    final public static Serde<List<SortedBag<Card>>> listSortedBagCardSerde = Serde.listOf(sortedBagCardSerde,DELIMITER_COMPOSITE);



    //Composite types
    private final static int FIRST_ARG = 0;
    private final static int SECOND_ARG = 1;
    private final static int THIRD_ARG = 2;
    private final static int LIMIT = -1;  //we use "-1", because if the limit is negative then the pattern will be applied as many times as possible and the array can have any length

    /**
     * Serde used to serialize/deserialize a object of type PublicCardState
     */
    final public static Serde<PublicCardState> publicCardStateSerde = Serde.of(
            publicCardState -> { //we serialize the elements : faceUpCards, deckSize and discardsSize with the delimiter ";"
                StringJoiner stringJoiner = new StringJoiner(DELIMITER_COMPOSITE);
                stringJoiner.add(listCardSerde.serialize(publicCardState.faceUpCards()));
                stringJoiner.add(intSerde.serialize(publicCardState.deckSize()));
                stringJoiner.add(intSerde.serialize(publicCardState.discardsSize()));
                return stringJoiner.toString();
            },
            str ->{ //we deserialize the elements : faceUpCards, deckSize and discardsSize with the delimiter ";"
                String[] serializedObjectArray = str.split(Pattern.quote(DELIMITER_COMPOSITE),LIMIT);
                return new PublicCardState(
                        listCardSerde.deserialize(serializedObjectArray[FIRST_ARG]), //that's the deserialization of the faceUpCards
                        intSerde.deserialize(serializedObjectArray[SECOND_ARG]), //that's the deserialization deckSize
                        intSerde.deserialize(serializedObjectArray[THIRD_ARG]) // that's the deserialization discardsSize
                );
            }
    );

    /**
     * Serde used to serialize/deserialize a object of type PublicPlayerState
     */
    final public static Serde<PublicPlayerState> publicPlayerStateSerde = Serde.of(
            publicPlayerState -> { //we serialize the elements : ticketCount, cardCount and routes with the delimiter ";"
                StringJoiner stringJoiner = new StringJoiner(DELIMITER_COMPOSITE);
                stringJoiner.add(intSerde.serialize(publicPlayerState.ticketCount()));
                stringJoiner.add(intSerde.serialize(publicPlayerState.cardCount()));
                stringJoiner.add(listRouteSerde.serialize(publicPlayerState.routes()));
                return stringJoiner.toString();
            },
            str ->{ //we deserialize the elements : ticketCount, cardCount and routes with the delimiter ";"
                String[] serializedObjectArray = str.split(Pattern.quote(DELIMITER_COMPOSITE),LIMIT);
                return new PublicPlayerState(
                        intSerde.deserialize(serializedObjectArray[FIRST_ARG]), //that's the deserialization of the ticketCount
                        intSerde.deserialize(serializedObjectArray[SECOND_ARG]), //that's the deserialization of the cardCount
                        (!serializedObjectArray[THIRD_ARG].equals("")) ? listRouteSerde.deserialize(serializedObjectArray[THIRD_ARG]) : List.of() ////that's the deserialization of the routes
                );
            }
    );

    /**
     * Serde used to serialize/deserialize a object of type PlayerState
     */
    final public static Serde<PlayerState> playerStateSerde = Serde.of(
            playerState -> { //we serialize the elements : tickets, cards, routes with delimiter ";"
                StringJoiner stringJoiner = new StringJoiner(DELIMITER_COMPOSITE);
                stringJoiner.add(sortedBagTicketSerde.serialize(playerState.tickets()));
                stringJoiner.add(sortedBagCardSerde.serialize(playerState.cards()));
                stringJoiner.add(listRouteSerde.serialize(playerState.routes()));
                return stringJoiner.toString();
            },
            str ->{ //we deserialize the elements : tickets, cards, routes with delimiter ";"
                String[] serializedObjectArray = str.split(Pattern.quote(DELIMITER_COMPOSITE),LIMIT);
                return new PlayerState(
                        sortedBagTicketSerde.deserialize(serializedObjectArray[FIRST_ARG]), //that's the deserialization of the tickets
                        sortedBagCardSerde.deserialize(serializedObjectArray[SECOND_ARG]), //that's the deserialization of the cards
                        listRouteSerde.deserialize(serializedObjectArray[THIRD_ARG]) //that's the deserialization of the routes
                );
            }
    );


    /**
     * Serde used to serialize/deserialize a object of type PublicGameState
     */
    final public static Serde<PublicGameState> publicGameStateSerde = Serde.of(
            publicGameState -> { //we serialize the elements : ticketsCount, cardState, currentPlayerId, playerState of the players, playerId of the last player
                StringJoiner stringJoiner = new StringJoiner(DELIMITER_PUBLIC_GAME_STATE);
                stringJoiner.add(intSerde.serialize(publicGameState.ticketsCount()));
                stringJoiner.add(publicCardStateSerde.serialize(publicGameState.cardState()));
                stringJoiner.add(playerIdSerde.serialize(publicGameState.currentPlayerId()));
                for(PlayerId playerId : PlayerId.getAllPlayer()){
                    stringJoiner.add(publicPlayerStateSerde.serialize(publicGameState.playerState(playerId)));
                }
                stringJoiner.add(playerIdSerde.serialize(publicGameState.lastPlayer())); // if lastPlayer is null it is sterilized as a null string
                return stringJoiner.toString();
            },
            str ->{ //we deserialize the elements : ticketsCount, cardState, currentPlayerId, playerState of the players, playerId of the last player
                String[] serializedObjectArray = str.split(Pattern.quote(DELIMITER_PUBLIC_GAME_STATE),LIMIT);

                //creation of the Hashmap containing the public states of the players
                Map<PlayerId, PublicPlayerState> playerState = new HashMap<>();
                for(int i=3; i<3+PlayerId.getNbrPlayer(); i++){
                    playerState.put(PlayerId.getAllPlayer().get(i-3),publicPlayerStateSerde.deserialize(serializedObjectArray[i]));
                }

                return new PublicGameState(
                        intSerde.deserialize(serializedObjectArray[FIRST_ARG]), //that's the deserialization of the ticketsCount
                        publicCardStateSerde.deserialize(serializedObjectArray[SECOND_ARG]), //that's the deserialization of the cardState
                        playerIdSerde.deserialize(serializedObjectArray[THIRD_ARG]), //that's the deserialization of the current playerId
                        playerState, //that's the deserialization of the playerState of the players
                        playerIdSerde.deserialize(serializedObjectArray[3+PlayerId.getNbrPlayer()]) //that's the deserialization of the playerId of the last player
                );
            }
    );

}
