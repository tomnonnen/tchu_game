package ch.epfl.tchu.gui;

import ch.epfl.tchu.net.RemotePlayerClient;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

/**
 * The class that let the player connect to the server to play a game
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class ClientMain extends Application {

    private final static String DEFAULT_IP = "localhost";
    private final static int DEFAULT_PORT = 5108;

    /**
     * Launch a standalone application.
     * @param args run argument of the program
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * @param primaryStage the main window of the application
     * @throws Exception exception of the application
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        List<String> arguments = getParameters().getRaw();
        String ipString = DEFAULT_IP; //par default localhost
        String portString = String.valueOf(DEFAULT_PORT);     //par default 5108

        if(!arguments.isEmpty() && !arguments.get(0).isEmpty())    {  ipString = arguments.get(0);    }
        if(arguments.size()>=2  && !arguments.get(1).isEmpty())    {   portString = arguments.get(1); }

        client(portString,ipString);
    }


    /**
     * creat a GraphicalPlayerAdapter and a RemotePlayerClient letting the player graphically interact with the server (and the game)
     * @param port the port of the server to connect on
     * @param ip the ip of the server hosting the game
     */
    public static void client(String port,String ip){
        String ipUsed = ip==null || ip.equals("") ?  DEFAULT_IP : ip;
        int portUsed = port==null || port.equals("") ?  DEFAULT_PORT : Integer.parseInt(port);

        GraphicalPlayerAdapter graphicalPlayer = new GraphicalPlayerAdapter();  //creation of the graphicalPlayer of the client player
        RemotePlayerClient remotePlayerClient = new RemotePlayerClient(graphicalPlayer, ipUsed,portUsed); //creation of the RemotePlayerClient of the graphicalPlayer letting the client sending and getting message

        new Thread(remotePlayerClient::run).start(); //run the RemotePlayerClient on a other train as the main app
    }
}
