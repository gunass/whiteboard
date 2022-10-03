import client.ClientGUI;
import server.*;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Executable class that creates a new whiteboard server and a client (admin) GUI to join it.
 * @author Alex Epstein
 */

public class CreateWhiteBoard {
    int DEFAULT_PORT = 3200;

    public static void main(String args[]) throws RemoteException {

        try {
            RemoteWhiteboard managerServer = new RemoteWhiteboard(args[0]);

        } catch (IOException e) {
            System.out.println("Error: starting whiteboard manager failed");
            System.exit(1);
        }

        ClientGUI adminGUI = new ClientGUI();

    }
}
