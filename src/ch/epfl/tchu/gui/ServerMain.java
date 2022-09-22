package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import ch.epfl.tchu.net.RemotePlayerProxy;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

import static ch.epfl.tchu.game.PlayerId.PLAYER_1;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class ServerMain extends Application {

    private final static int DEFAULT_PORT = 5108;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        List<String> arguments = getParameters().getRaw();
        server(DEFAULT_PORT,arguments);
    }



    public static void server(int portNumber,List<String> arguments) throws IOException {

        Map<PlayerId, String> playerNames = new HashMap<>();
        Map<PlayerId, Player> players = new HashMap<>();

        for(int i=0;i<PlayerId.getNbrPlayer();i++){
            if(arguments.isEmpty() || arguments.size()<=i || arguments.get(i).isEmpty())
                playerNames.put(PlayerId.getAllPlayer().get(i), String.format("Player %d", i+1));
            else
                playerNames.put(PlayerId.getAllPlayer().get(i), arguments.get(i));
        }

        ServerSocket serverSocket = new ServerSocket(portNumber);

        Alert waitingPlayerAlert = waitingForPlayerAlert(portNumber);
        waitingPlayerAlert.show();

        new Thread(()->{
            for(PlayerId playerId : PlayerId.getAllPlayer()) {
                if (playerId==PLAYER_1) continue;
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                RemotePlayerProxy remotePlayerProxy = new RemotePlayerProxy(socket);
                players.put(playerId, remotePlayerProxy);
                System.out.println(playerId.name()+" joined");
            }

            Player graphicalPlayer = new GraphicalPlayerAdapter();
            players.put(PLAYER_1, graphicalPlayer);

            Platform.runLater(waitingPlayerAlert::close);
            Game.play(players, playerNames, SortedBag.of(ChMap.tickets()), new Random());
        }).start();

    }

    private static String showMyIpAddress(){
        try {
            return NetworkInterface.networkInterfaces().filter(i -> {
                try { return i.isUp() && !i.isLoopback(); }
                catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
                    .flatMap(NetworkInterface::inetAddresses)
                    .filter(a -> a instanceof Inet4Address)
                    .map(InetAddress::getCanonicalHostName)
                    .collect(Collectors.joining());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "adresse ip non trouvé";
    }

    private static Alert waitingForPlayerAlert(int portNumber){

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Small info");
        alert.setHeaderText(null);
        alert.setContentText(
                "We are waiting for player to join \n" +
                        "Please give them this information: \n" +
                        "Port : "+portNumber+"\n" +
                        "Ip : "+ showMyIpAddress()
        );
        return alert;
    }

}
