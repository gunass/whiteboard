package server;

import drawing.Drawing;
import util.UserIdentity;

import java.io.*;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Represents a standard TCP server that manages non-canvas queries, such as group membership,
 * canvas switching, etc. Owns the Whiteboard remote object.
 * @author Alex Epstein with thanks to skeleton code: https://en.wikipedia.org/wiki/Java_remote_method_invocation
 */
public class WhiteboardManager extends Thread {

    private final int RMI_PORT = 1099;
    private String hostname = "localhost";
    private Whiteboard whiteboard;
    private LinkedBlockingDeque<Socket> incomingConnections;
    private IOThread ioThread;

    private UserIdentity admin;
    private ArrayList<UserIdentity> users;

    /**
     * Creates a new instance of the manager using the provided hostname and port.
     * Consists of two threads, one IOThread which queues incoming connection requests,
     * and the main thread (this) which processes requests in the order received
     * @param port
     * @throws IOException
     */

    public WhiteboardManager(int port) throws IOException {

        // construct
        this.incomingConnections = new LinkedBlockingDeque<Socket>();

        // start IOThread

        this.ioThread = new IOThread(port, incomingConnections);
        ioThread.start();
    }

    /**
     * Listen for incoming connections on the deque and process them in series
     */
    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Socket socket = incomingConnections.take();
                processRequest(socket);
            } catch (InterruptedException e) {
                System.out.println("InterruptedException in WBManager");
            } catch (IOException f) {
                System.out.println("IOException in WBManager");
            }
        }
    }

    /**
     * FIXME: STUB
     * Accepts client requests and processes them according to request type and client identity
     * @param socket
     * @throws IOException
     */
    private void processRequest(Socket socket) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        String msg = br.readLine();
        System.out.println("Client says: "+msg);

        // FIXME: implement
        // Read in the message, verify the identity of the sender (hostname+port+username) and perform the relevant action

        // Send a brief ack indicating success/failure of request
        String reply = "You'll have to speak up, I'm wearing a towel.\n";
        bw.write(reply);

    }

    /**
     * Creates a new blank whiteboard using the default constructor
     */
    private void createCanvas() throws RemoteException, MalformedURLException {
        // create a new blank whiteboard, registry, and bind the whiteboard

        this.whiteboard = new Whiteboard();

        try {
            LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("RMI registry created");
        } catch (RemoteException e) {
            System.out.println("Error: RMI registry already exists");
            return;
        }

        Naming.rebind("//"+hostname+"/Whiteboard", whiteboard);
        System.out.println("Whiteboard bound in registry!");
    }

    // The following are example methods for the type of requests that can be processed
    private void addNewUser() {
    }

    private void resetCanvas() {
    }

    private void saveCanvas() {
    }

    private void openCanvas() {
    }

    private void closeApplication() {
    }

    private void kickUser() {
    }

}
