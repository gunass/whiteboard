package server;

import client.IInteractiveCanvasManager;
import client.InteractiveCanvasManager;
import drawing.Drawing;
import util.UserIdentity;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Is a remote object that maintains a canonical array of Drawing objects, and is responsible for server->client
 * communication (mostly a relay of the form client->server->clients*)
 * @author Alex Epstein with thanks to skeleton code: https://en.wikipedia.org/wiki/Java_remote_method_invocation
 */
public class RemoteWhiteboard extends UnicastRemoteObject implements IRemoteWhiteboard {

    private UserIdentity admin;
    private IInteractiveCanvasManager adminClient;
    private String hostname;
    private ArrayList<UserIdentity> users = new ArrayList<UserIdentity>();
    private ArrayList<IInteractiveCanvasManager> clients = new ArrayList<IInteractiveCanvasManager>();
    private ArrayList<Drawing> drawings;

    /**
     * Creates a new instance of the manager on the provided hostname (i.e., binds self to //hostname:1099/Whiteboard)
     * @throws IOException
     */
    public RemoteWhiteboard(String hostname) throws IOException {

        super();
        this.hostname = hostname;

        try {
            int RMI_PORT = 1099;
            LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("RMI registry created");
        } catch (RemoteException e) {
            System.out.println("Error: RMI registry already exists");
            return;
        }
        try {
            Naming.rebind("//" + hostname + "/Whiteboard", this);
            System.out.println("Whiteboard bound in registry!");
        } catch (RemoteException | MalformedURLException e) {
            System.out.println("RMI error");
        }

        this.drawings = new ArrayList<Drawing>();
    }

    /**
     * The admin role is set to the first user to call this method after the server has started.
     * If admin role has been claimed, or if the server encounters an RMI error, returns false, true otherwise.
     * @param uid
     * @return
     * @throws RemoteException
     */
    public boolean startWhiteboard(UserIdentity uid) throws RemoteException {
        if (admin == null) {
            admin = uid;
            try {
                adminClient = (IInteractiveCanvasManager) Naming.lookup( "//" + hostname + "/" + uid.username);
                clients.add(adminClient);
                users.add(uid);
                adminClient.notifyUserJoin(uid.username);
            } catch (NotBoundException | MalformedURLException | ClassCastException e) {
                return false;
            }
            return true;
        } else return false;
    }

    /**
     * Allows new users to join the server, subject to the admin's approval.
     * If the admin rejects the user or RMI error, returns false, otherwise true.
     * @param uid
     * @return
     * @throws RemoteException
     */
    public boolean joinWhiteboard(UserIdentity uid) throws RemoteException {
        if (approveUser(uid)) {
            try {
                clients.add((IInteractiveCanvasManager) Naming.lookup( "//" + hostname + "/" + uid.username));
                users.add(uid);
                for (IInteractiveCanvasManager c : clients) {
                    c.notifyUserJoin(uid.username);
                }
            } catch (NotBoundException | MalformedURLException | ClassCastException e) {
                return false;
            }
            return true;
        } else return false;
    }

    /**
     * Calls the approveUser() method of the admin to ask for approval
     * @param uid
     * @return approval
     * @throws RemoteException
     */
    private boolean approveUser(UserIdentity uid) throws RemoteException {
        return adminClient.approveUser(uid);
    }

    public void notifyDisconnect(UserIdentity uid) throws RemoteException {
        if (!isUser(uid)) {
            return;
        }

        for (int i = 0; i < users.size(); i++) {
            if (uid.is(users.get(i))) {
                users.remove(users.get(i));
                clients.remove(clients.get(i));
            } else {
                clients.get(i).notifyUserLeft(uid.username);
            }
        }
    }

    /**
     * Returns a list of users currently on the server
     * @param uid
     * @return a list of users
     * @throws RemoteException
     */
    public ArrayList<UserIdentity> getUsers(UserIdentity uid) throws RemoteException {
        if (isUser(uid)) {
            return users;
        } else return null;
    }

    /**
     * Returns the current state of the canvas, i.e. an array of Drawings
     * @param uid
     * @return a list of drawings on the server
     * @throws RemoteException
     */
    public ArrayList<Drawing> getCanvas(UserIdentity uid) throws RemoteException {
        if (isUser(uid)) {
            return drawings;
        } else return null;
    }

    /**
     * If user is admin, reset the canvas to blank
     * @param uid
     * @throws RemoteException
     */
    public void clearCanvas(UserIdentity uid) throws RemoteException {
        if (uid.is(admin)) {
            drawings = new ArrayList<Drawing>();
            for (IInteractiveCanvasManager c : clients) {
                c.clearCanvas();
            }
        }
    }

    /**
     * Allows approved users to submit a drawing to add to the canvas.
     * Then, the drawing is sent to all users.
     * @param uid
     * @param drawing
     * @throws RemoteException
     */
    public void drawToCanvas(UserIdentity uid, Drawing drawing) throws RemoteException {
        if (isUser(uid)) {
            drawings.add(drawing);
            for (IInteractiveCanvasManager c : clients) {
                try {
                    c.addDrawing(drawing);
                } catch (RemoteException ignored) {}
            }
        }
    }

    /**
     * A method which draws a collection of drawings to the canvas, avoiding the inefficiencies
     * of multiple RMI connections
     * @param uid
     * @param drawings
     * @throws RemoteException
     */
    public void drawAllToCanvas(UserIdentity uid, Collection<Drawing> drawings) throws RemoteException {
        for (Drawing d : drawings) {
            drawToCanvas(uid, d);
        }
    }

    /**
     * Tests if a user is present in the list of admin-approved users
     * @param uid
     * @return
     */
    private boolean isUser(UserIdentity uid) {
        for (UserIdentity user : users) {
            if (user.is(uid)) {
                return true;
            }
        }
        return false;
    }

    public void sendMessage(UserIdentity uid, String message) throws RemoteException {
        if (isUser(uid)) {
            for (IInteractiveCanvasManager c : clients) {
                c.newChatMessage(uid.username, message);
            }
        }
    }


}
