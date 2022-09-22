package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Info;

import java.util.*;

import static ch.epfl.tchu.game.Constants.*;
import static ch.epfl.tchu.game.Player.TurnKind.CLAIM_ROUTE;
import static ch.epfl.tchu.game.PlayerId.*;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class Game {

    /**
     * This method simulate the game in its entirety
     * @param players : a map that join the Player with his PlayerId
     * @param playerNames : a map that join the PlayerId with his name
     * @param tickets : the initial deck of tickets of the game
     * @param rng : Random
     */
    public static void play(Map<PlayerId, Player> players, Map<PlayerId, String> playerNames, SortedBag<Ticket> tickets, Random rng){
        //lancement exception
        Preconditions.checkArgument(players.size() == PlayerId.getNbrPlayer() && playerNames.size()== PlayerId.getNbrPlayer());

        //===================
        //début de partie
        GameState gameState = GameState.initial(tickets, rng);
        gameState = beginGame(gameState, players, playerNames);

        //===================
        //milieu de partie
        do{

            gameState = middleGame(gameState, players, playerNames, rng);

            if(gameState.lastTurnBegins()){
                infoForPlayers(players, new Info(playerNames.get(gameState.currentPlayerId())).lastTurnBegins(gameState.currentPlayerState().carCount())); //on informe que c'est le dernier tour
                break;
            }

            gameState = gameState.forNextTurn();

        } while (!gameState.lastTurnBegins());

        //dernier tour
        for(int i=0; i<PlayerId.getNbrPlayer(); i++){
            gameState = gameState.forNextTurn();
            gameState = middleGame(gameState, players, playerNames, rng);
        }


        //===================
        //fin de partie
        endGame(gameState, players, playerNames);


    }


    /**
     *This method simulate the begin of the Game
     * @param gameState : the state of the game in which the method is called
     * @param players : a map that join the Player with his PlayerId
     * @param playerNames : a map that join the PlayerId with his name
     * @return the new GameState
     */
    public static GameState beginGame(GameState gameState, Map<PlayerId, Player> players, Map<PlayerId, String> playerNames){
        PlayerId firstPlayerId = gameState.currentPlayerId();
        Info info = new Info(playerNames.get(firstPlayerId));
        Map<Player, SortedBag<Ticket>> initialTicketsPlayer = new HashMap<>(); //les billets (parmis les 5 piochés) que chaque joueur décident de garder au début de la partie

        //point 1
        players.forEach((playerId, player) -> player.initPlayers(playerId, playerNames));

        //point 2
        infoForPlayers(players, info.willPlayFirst()); //on informe l'identité du premier joueur

        //point 3
        for(Player player : players.values()){
            player.setInitialTicketChoice(gameState.topTickets(INITIAL_TICKETS_COUNT));
            gameState = gameState.withoutTopTickets(INITIAL_TICKETS_COUNT);
        }


        //point 4
        updateStateForPlayers(players, gameState); // on met à jour afin que les joueurs puissent avoir connaissance de leurs cartes initiales
        for (Map.Entry<PlayerId, Player> p : players.entrySet()) {
            initialTicketsPlayer.put(p.getValue(), p.getValue().chooseInitialTickets());
            gameState = gameState.withInitiallyChosenTickets(p.getKey(), initialTicketsPlayer.get(p.getValue()));
        }

        //point 5
        players.forEach((playerId, player) -> infoForPlayers(players, new Info(playerNames.get(playerId)).keptTickets(initialTicketsPlayer.get(player).size()))); // on inform ici quels ticket chaque joueur a gardé


        return gameState;
    }

    /**
     *This method simulate the play of a player with his 3 choices during his tour
     * @param gameState : the state of the game in which the method is called
     * @param players : a map that join the Player with his PlayerId
     * @param playerNames : a map that join the PlayerId with his name
     * @param rng : Random
     * @return the new GameState
     */
    public static GameState middleGame(GameState gameState, Map<PlayerId, Player> players, Map<PlayerId, String> playerNames, Random rng){
        updateStateForPlayers(players, gameState); // on met à jour afin que les joueurs puissent avoir connaissance du tour précèdent
        Player currentPlayer = players.get(gameState.currentPlayerId());

        Info infoCurrentPlayer = new Info(playerNames.get(gameState.currentPlayerId()));
        infoForPlayers(players, infoCurrentPlayer.canPlay()); //on informe les joueurs du nouveau tour
        Player.TurnKind nextTurn = currentPlayer.nextTurn();


        switch (nextTurn){

            case DRAW_TICKETS:
                SortedBag<Ticket> ticketsPlayer = currentPlayer.chooseTickets(gameState.topTickets(IN_GAME_TICKETS_COUNT));
                gameState = gameState.withChosenAdditionalTickets(gameState.topTickets(IN_GAME_TICKETS_COUNT), ticketsPlayer);

                infoForPlayers(players, infoCurrentPlayer.drewTickets(IN_GAME_TICKETS_COUNT)); //on informe le joueur tire des billets
                infoForPlayers(players, infoCurrentPlayer.keptTickets(ticketsPlayer.size())); //on informe que le joueur a gardé ces billets
                break;

            case DRAW_CARDS:
                for(int i=0; i<DISCARDABLE_TICKETS_COUNT; i++) {
                    gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                    int drawSlot = currentPlayer.drawSlot();
                    if (drawSlot == Constants.DECK_SLOT) {
                        gameState = gameState.withBlindlyDrawnCard();
                        infoForPlayers(players, infoCurrentPlayer.drewBlindCard()); // on informe que le joueur courant a pris une carte non visible
                    } else {
                        Card visibleCard = gameState.cardState().faceUpCard(drawSlot);
                        gameState = gameState.withDrawnFaceUpCard(drawSlot);
                        infoForPlayers(players, infoCurrentPlayer.drewVisibleCard(visibleCard)); // on informe que le joueur courant a pris une carte visible
                    }
                    if (i < DISCARDABLE_TICKETS_COUNT - 1) {
                        updateStateForPlayers(players, gameState); //on met à jour afin que le joueur sache p.ex. quelle carte a remplacé la carte face visible qu'il a éventuellement tirée en premier,
                    }
                }
                break;

            case CLAIM_ROUTE:
                Route route = currentPlayer.claimedRoute();
                SortedBag<Card> initialClaimCards = currentPlayer.initialClaimCards();

                if (route.level() == Route.Level.UNDERGROUND) {
                    infoForPlayers(players, infoCurrentPlayer.attemptsTunnelClaim(route, initialClaimCards)); //on informe que le joueur essaye de s'emparer d'un tunnel

                    SortedBag.Builder<Card> builder = new SortedBag.Builder<>();
                    //Le joueur va piocher les 3 cartes additionnelles
                    for (int i = 0; i < ADDITIONAL_TUNNEL_CARDS; i++) {
                        gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                        builder.add(gameState.topCard());
                        gameState = gameState.withoutTopCard();
                    }
                    SortedBag<Card> drawnCards = builder.build();
                    int additionalClaimCardsCount = route.additionalClaimCardsCount(initialClaimCards, drawnCards);


                    infoForPlayers(players, infoCurrentPlayer.drewAdditionalCards(drawnCards, additionalClaimCardsCount));//on informe que le joueur a tiré les cartes additionels qui ont un prix

                    //On considère les cas possiles
                    if (additionalClaimCardsCount >= 1) {
                        List<SortedBag<Card>> options = gameState.currentPlayerState().possibleAdditionalCards(additionalClaimCardsCount,initialClaimCards,drawnCards);
                        SortedBag<Card> choicePlayer = !options.isEmpty() ? currentPlayer.chooseAdditionalCards(options) : SortedBag.of();
                        if(!choicePlayer.isEmpty()){
                            SortedBag<Card> allCards = initialClaimCards.union(choicePlayer);
                            gameState = gameState.withClaimedRoute(route, allCards);
                            infoForPlayers(players, infoCurrentPlayer.claimedRoute(route, allCards)); //on informe que le joueur s'est emparé du tunnel
                        }
                        else{
                            infoForPlayers(players, infoCurrentPlayer.didNotClaimRoute(route)); // on informe que le joueur n'a pas pu s'emparer de la route

                        }
                    } else if (additionalClaimCardsCount == 0) {
                        gameState = gameState.withClaimedRoute(route, initialClaimCards);
                        infoForPlayers(players, infoCurrentPlayer.claimedRoute(route, initialClaimCards)); //on informe que le joueur courant s'est emparé du tunnel
                    } else { //le joueur ne veut ou ne peut pas
                        infoForPlayers(players, infoCurrentPlayer.didNotClaimRoute(route)); // on informe que le joueur n'a pas pu s'emparer de la route
                    }

                    gameState = gameState.withMoreDiscardedCards(drawnCards); //remettre drawnCards dans la discards

                } else {
                    gameState = gameState.withClaimedRoute(route, initialClaimCards);
                    infoForPlayers(players, infoCurrentPlayer.claimedRoute(route, initialClaimCards)); //on informe que le joueur courant s'est emparé de la route
                }
                break;

            default:
                System.out.println("Error");
                break;
        }


        return gameState;
    }



    /**
     *This method simulate the end of the Game
     * @param gameState : the state of the game in which the method is called
     * @param players : a map that join the Player with his PlayerId
     * @param playerNames : a map that join the PlayerId with his name
     */
    public static void endGame(GameState gameState, Map<PlayerId, Player> players, Map<PlayerId, String> playerNames){

        updateStateForPlayers(players, gameState); //juste avant d'informer les joueurs du résultat final de la partie, afin qu'ils connaissent l'état dans lequel la partie s'est effectivement terminé.

        Map<PlayerId, Integer> finalPointsMap = new HashMap<>();
        Map<PlayerId, Trail> longestTrailMap = new HashMap<>();
        List<PlayerId> winnersList = new ArrayList<>();

        for(PlayerId playerId : PlayerId.getAllPlayer())
            finalPointsMap.put(playerId, gameState.playerState(playerId).finalPoints());

        int previous = 0;

        for (PlayerId playerId : PlayerId.getAllPlayer()) { // on fait le cas ici pour n joueurs et savoir qui a le plus long trail
            Trail trail = Trail.longest(gameState.playerState(playerId).routes());
            int trailSize = trail.length();

            if(trailSize > previous) { // si le trail du joueur playerId est plus long que le plus long precedent alors on supprime le tableau
                if(longestTrailMap.size() > 0)
                    longestTrailMap.clear();
                longestTrailMap.put(playerId, trail); // on ajoute le nouveau plus long trail
                previous = trailSize;
            } else if(trailSize == previous){ // si plusieurs trail plus long sont egaux, alors on les stocke ensemble
                longestTrailMap.put(playerId, trail);
            }
        }

        longestTrailMap.forEach(((playerId, trail) -> { //on informe les joueurs du plus long trail, de plus on ajoute les points bonus
            infoForPlayers(players, new Info(playerNames.get(playerId)).getsLongestTrailBonus(trail));
            finalPointsMap.replace(playerId, finalPointsMap.get(playerId) + LONGEST_TRAIL_BONUS_POINTS);
        }));

        previous = 0;
        for(PlayerId playerId : PlayerId.getAllPlayer()){ // meme fonctionnement que la boucle precedente
            int finalPoints = finalPointsMap.get(playerId);

            if(finalPoints > previous){
                if(finalPointsMap.size() > 0)
                    winnersList.clear();

                winnersList.add(playerId);
                previous = finalPoints;
            } else if(finalPoints == previous){
                winnersList.add(playerId);
            }
        }

        // on informe les joueurs des winners
        if(PlayerId.getNbrPlayer() == 2){
            if(winnersList.size() == 2){ //egalite entre les deux joueurs
                infoForPlayers(players, Info.draw(List.of(playerNames.get(PLAYER_1), playerNames.get(PLAYER_2)), finalPointsMap.get(PLAYER_1)));
            } else {
                PlayerId winnerId = winnersList.get(0);
                infoForPlayers(players, new Info(playerNames.get(winnerId)).won(finalPointsMap.get(winnerId), finalPointsMap.get(winnerId.next())));
            }
        } else { // dans le cas ou on a plus de 2 joueurs
            if(winnersList.size() > 1){ // dans le cas ou on a des egalites
                List<String> names = new ArrayList<>();
                winnersList.forEach((playerId -> names.add(playerNames.get(playerId))));
                infoForPlayers(players, Info.draw(names, finalPointsMap.get(winnersList.get(0))));
            } else { // dans le cas ou on a qu un seul winner
                PlayerId winnerId = winnersList.get(0);
                infoForPlayers(players, new Info(playerNames.get(winnerId)).wonMulti(finalPointsMap.get(winnerId)));
            }

        }

    }

    /**
     *This method informed the state of the game to the players
     * @param players : a map that join the Player with his PlayerId
     * @param string : a method of Info that return a string to give the information for the players
     */
    private static void infoForPlayers(Map<PlayerId, Player> players, String string){
        players.forEach((playerId, player) -> player.receiveInfo(string));
    }

    /**
     * This method updated the state of the players by the state of the game
     * @param players : a map that join the Player with his PlayerId
     * @param newState : the new State of the Game
     */
    private static void updateStateForPlayers(Map<PlayerId, Player> players, GameState newState){
        players.forEach((playerId, player) -> player.updateState(newState, newState.playerState(playerId)));
    }


}
