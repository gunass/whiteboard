package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Fills up the incoming connections deque
 * @author Alex Epstein, with thanks to IdxSrv
 */
public class IOThread extends Thread {
    LinkedBlockingDeque<Socket> incomingConnections;
    ServerSocket serverSocket;
    int port;
    public IOThread(int port, LinkedBlockingDeque<Socket> incomingConnections) {
        this.incomingConnections = incomingConnections;
        this.port = port;
    }

    @Override
    public void run() {
        System.out.println("IOThread running...");
        try {
            this.serverSocket = new ServerSocket(port);
            while (!isInterrupted()) {
                if (!incomingConnections.offer(serverSocket.accept())) {
                    System.out.println("Deque is full!");
                } else {
                    // bar
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
