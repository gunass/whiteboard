import server.*;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Master server class that manages the RMI and socketed server components
 * This should be renamed to CreateWhiteBoard
 * @author Alex Epstein
 */

public class CreateWhiteBoard {
    int DEFAULT_PORT = 3200;

    public static void main(String args[]) throws RemoteException {

        // Parse args

        if (args.length != 3) {
            System.out.println("Arguments must be <server ip, server port, host username>");
            System.exit(1);
        }

        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String adminUsername = args[2];


        // Try start the socket server

        WhiteboardManager managerServer;
        try {

            managerServer = new WhiteboardManager(serverPort);
            managerServer.start();

            // start up a client gui for the admin

            // wait to finish
            managerServer.join();

        } catch (IOException e) {
            System.out.println("Error: starting whiteboard manager failed");
            System.exit(1);
        } catch (InterruptedException e) {
            System.out.println("Error: whiteboard manager interrupted");
            System.exit(1);
        }




    }

}
