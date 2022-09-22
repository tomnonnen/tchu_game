package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.Route;
import ch.epfl.tchu.game.Trail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ch.epfl.tchu.game.Card.ALL;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class Info {

    private final String playerName;

    /**
     * builds a message generator related to the player with the given name
     * @param playerName player name
     */
    public Info(String playerName){
        this.playerName = playerName;
    }

    /**
     * @param card the card whose name is to be displayed
     * @param count number of occurrences of the card
     * @return French name of the given card, singular if the absolute value of the second argument is 1
     */
    public static String cardName(Card card, int count){
        HashMap<Card,String> cardNames = new HashMap<>();
        cardNames.put(Card.BLACK,StringsFr.BLACK_CARD);
        cardNames.put(Card.VIOLET,StringsFr.VIOLET_CARD);
        cardNames.put(Card.BLUE,StringsFr.BLUE_CARD);
        cardNames.put(Card.GREEN,StringsFr.GREEN_CARD);
        cardNames.put(Card.YELLOW,StringsFr.YELLOW_CARD);
        cardNames.put(Card.ORANGE,StringsFr.ORANGE_CARD);
        cardNames.put(Card.RED,StringsFr.RED_CARD);
        cardNames.put(Card.WHITE,StringsFr.WHITE_CARD);
        cardNames.put(Card.LOCOMOTIVE,StringsFr.LOCOMOTIVE_CARD);

        return String.format("%s%s", cardNames.get(card),StringsFr.plural(count));
    }

    /**
     * @param playerNames names of players who are tied
     * @param points points they have achieved
     * @return the message declaring that the players, whose names are the given ones, have finished the game ex æqo having each won the given points
     */
    public static String draw(List<String> playerNames, int points){
        return String.format(StringsFr.DRAW, String.join(" et ", playerNames), points);
    }

    /**
     * @return the message stating that the player will play first
     */
    public String willPlayFirst(){
        return String.format(StringsFr.WILL_PLAY_FIRST, playerName);
    }

    /**
     * @param count number of tickets kept
     * @return the message stating that the player has kept the given number of tickets
     */
    public String keptTickets(int count){
        return String.format(StringsFr.KEPT_N_TICKETS, playerName, count, StringsFr.plural(count));
    }

    /**
     * @return the message stating that the player can play
     */
    public String canPlay(){
        return String.format(StringsFr.CAN_PLAY, playerName);
    }

    /**
     * @param count number of tickets drawn
     * @return the message stating that the player has drawn the given number of tickets
     */
    public String drewTickets(int count){
        return String.format(StringsFr.DREW_TICKETS, playerName, count, StringsFr.plural(count));
    }

    /**
     * @return the message stating that the player has drawn a card 'blind', i.e. from the top of the deck
     */
    public String drewBlindCard(){
        return String.format(StringsFr.DREW_BLIND_CARD, playerName);
    }

    /**
     * @param card the card that was drawn
     * @return the message stating that the player has drawn the given face-up card
     */
    public String drewVisibleCard(Card card){
        return String.format(StringsFr.DREW_VISIBLE_CARD, playerName, cardName(card,1));
    }

    /**
     * @param route route that the player has seized
     * @param cards cards used to seize the route
     * @return the message stating that the player has seized the given route using the given cards
     */
    public String claimedRoute(Route route, SortedBag<Card> cards){
        return String.format(StringsFr.CLAIMED_ROUTE, playerName, getRouteName(route), getNamesCards(cards));
    }


    /**
     * @param cards: the sortedBag given as parameter
     * @return a string list with all the cards that are in the sorted Bag
     */
    public static String getNamesCards(SortedBag<Card> cards){

        if(cards.size()!=0){
            List<String> namesCardsTab = new ArrayList<>();
            String namesCards;

            //add all the countability and the type of cards that exists in the SortedBag cards
            for (Card card : ALL) {
                if(cards.contains(card)){
                    namesCardsTab.add(String.format("%s %s", cards.countOf(card), cardName(card, cards.countOf(card))));
                }
            }

            //We create a list that contain all the elements without the last element, to prepare the delimiter "," and "and".
            List<String> namesCardsTabWithoutLastElement = new ArrayList<>();
            for(int i=0; i<namesCardsTab.size()-1; i++){
                namesCardsTabWithoutLastElement.add(namesCardsTab.get(i));
            }

            //We add the delimiter "," and "and" if it is necessary
            if(!namesCardsTabWithoutLastElement.isEmpty()){
                String namesCardsWithoutAnd=String.join(", ", namesCardsTabWithoutLastElement);
                namesCards = String.format("%s%s%s", namesCardsWithoutAnd, StringsFr.AND_SEPARATOR, namesCardsTab.get(namesCardsTab.size()-1));
            }
            else{
                namesCards = namesCardsTab.get(namesCardsTab.size()-1);
            }
            return namesCards;
        }
        else{
            return "";
        }

    }

    /**
     * @param route : route passed as parameter
     * @return the route name as a string
     */
    private static String getRouteName(Route route){
        return String.format("%s%s%s", route.station1().toString(), StringsFr.EN_DASH_SEPARATOR, route.station2().toString());
    }

    /**
     * @param route route that the player wants to take
     * @param initialCards route that the player initially played to try to take the tunnel
     * @return which returns the message that the player wishes to take the given tunnel route using the given cards initially
     */
    public String attemptsTunnelClaim(Route route, SortedBag<Card> initialCards){
        return String.format(StringsFr.ATTEMPTS_TUNNEL_CLAIM, playerName, getRouteName(route), getNamesCards(initialCards));
    }

    /**
     * @param drawnCards additional card that the player has drawn
     * @param additionalCost additional cost to the draw
     * @return the message stating that the player has drawn the three given additional cards, and that they involve an additional cost of the given number of cards
     */
    public String drewAdditionalCards(SortedBag<Card> drawnCards, int additionalCost){
        String additionalCardsAre = String.format(StringsFr.ADDITIONAL_CARDS_ARE, getNamesCards(drawnCards));
        String strCost;

        if(additionalCost > 0){
            strCost = String.format(StringsFr.SOME_ADDITIONAL_COST, additionalCost, StringsFr.plural(additionalCost));
        } else {
            strCost = StringsFr.NO_ADDITIONAL_COST;
        }

        return String.format("%s%s", additionalCardsAre, strCost);
    }

    /**
     * @param route route which the player has cancelled the take
     * @return the message declaring that the player was unable (or unwilling) to seize the given tunnel
     */
    public String didNotClaimRoute(Route route){
        return String.format(StringsFr.DID_NOT_CLAIM_ROUTE, playerName, getRouteName(route));

    }

    /**
     * @param carCount number of cards the player has
     * @return the message stating that the player has only the given number (and less than or equal to 2) of cars left, and that the last round is starting
     */
    public String lastTurnBegins(int carCount){
        return String.format(StringsFr.LAST_TURN_BEGINS, playerName, carCount, StringsFr.plural(carCount));
    }

    /**
     * @param longestTrail the longest trail the player has produced
     * @return the message stating that the player gets the endgame bonus for the given path, which is the longest, or one of the longest
     */
    public String getsLongestTrailBonus(Trail longestTrail){
        String s = String.format("%s%s%s",longestTrail.station1(),StringsFr.EN_DASH_SEPARATOR,longestTrail.station2());
        return String.format(StringsFr.GETS_BONUS, playerName, s);
    }

    /**
     * @param points number of points of the winner
     * @param loserPoints number of points of the loser
     * @return the message that the player wins the game with the given number of points, his opponent having obtained only loserPoints
     */
    public String won(int points, int loserPoints){
        return String.format(StringsFr.WINS, playerName, points, StringsFr.plural(points), loserPoints,StringsFr.plural(loserPoints));
    }

    /**
     * @param points number of points of the winner
     * @return the message that the player wins the game with the given number of points
     */
    public String wonMulti(int points){
        return String.format(StringsFr.WINS_MULTI, playerName, points, StringsFr.plural(points));
    }



}
