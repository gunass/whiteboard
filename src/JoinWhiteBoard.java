import client.ClientGUI;

import java.io.*;
import java.net.Socket;

/**
 * FIXME: STUB
 * Executable used to join an existing whiteboard
 */
public class JoinWhiteBoard {

    public static void main(String args[]) throws IOException {

        if (args.length != 3) {
            System.out.println("Args must be <serverIP, serverPort, username>");
            System.exit(1);
        }

        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String username = args[2];

        if (username.contains(":")) {
            System.out.println("Forbidden character ':'");
            System.exit(1);
        }

        // Start the GUI

        ClientGUI cgui = new ClientGUI(serverIP, serverPort, username);


    }

}
