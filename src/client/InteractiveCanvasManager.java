package client;

import drawing.Drawing;
import server.IRemoteWhiteboard;
import util.UserIdentity;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the interactive canvas and exists as a remote object that communicates with server
 * The "clientside" distributed link - this object is called by the canvas to transmit new drawings to server
 * Remote methods are defined in the interface
 * @see client.IInteractiveCanvasManager
 * @author Alex Epstein
 */

public class InteractiveCanvasManager extends UnicastRemoteObject implements IInteractiveCanvasManager {

    private final UserIdentity uid;
    private final IRemoteWhiteboard remoteWhiteboard;
    protected InteractiveCanvas canvas;
    private volatile int approved = 0;
    private boolean admin = false;
    protected ClientGUI gui;

    /**
     * Creates a new ICM that communicates with the remote whiteboard, and manages the display of the canvas.
     * @param rmiReference
     * @param uid
     * @param width
     * @param height
     * @throws Exception
     */
    public InteractiveCanvasManager(String rmiReference, UserIdentity uid, int width, int height, ClientGUI gui) throws Exception {

        this.uid = uid;
        this.gui = gui;

        //FIXME: expand beyond localhost?
        String hostname = "localhost";

        try {
            Naming.rebind("//"+hostname+"/" + uid.username, this);
        } catch (RemoteException | MalformedURLException e) {
            throw new Exception("Error RMI binding self");
        }

        try {
            this.remoteWhiteboard = (IRemoteWhiteboard) Naming.lookup(rmiReference);
        } catch (Exception e) {
            throw new Exception("Couldn't find that whiteboard");
        }

        if (!remoteWhiteboard.startWhiteboard(uid)) {
            if (!remoteWhiteboard.joinWhiteboard(uid)) {
                throw new Exception("Join rejected");
            }
        } else {
            admin = true;
        }

        this.canvas = new InteractiveCanvas(width, height, uid.username, this);
        canvas.drawings = new Stack<>();
        canvas.drawings.addAll(remoteWhiteboard.getCanvas(uid));
    }

    /**
     * Called by the server: adds a drawing to the user's canvas.
     * @param drawing
     * @throws RemoteException
     */
    public void addDrawing(Drawing drawing) throws RemoteException {
        canvas.drawings.add(drawing);
        canvas.repaint();
    }

    /**
     * Called by the server: clears the user's canvas.
     * @throws RemoteException
     */
    public void clearCanvas() throws RemoteException {
        canvas.drawings = new Stack<>();
        canvas.canvasFlat = new BufferedImage(canvas.canvasFlat.getWidth(), canvas.canvasFlat.getHeight(), BufferedImage.TYPE_INT_ARGB);
        canvas.repaint();
    }

    /**
     * Called by the GUI on "clear" button press
     */
    public void requestClearCanvas() {
        try {
            remoteWhiteboard.clearCanvas(uid);
        } catch (Exception ignored) {};
    }

    /**
     * Called by the canvas when a drawing is complete, i.e. the mouse has been released.
     * Sends the drawing object to the server
     * @param drawing
     */
    protected void sendDrawing(Drawing drawing) {
        try {
            remoteWhiteboard.drawToCanvas(uid, drawing);
        } catch (RemoteException ignored) {}
    }

    /**
     * Called by the server when a new user requests to join the whiteboard. Recursively calls the private method
     * @param uid
     * @return
     */
    public boolean approveUser(UserIdentity uid) {
        return _approveUser(uid);
    }

    /**
     * Launches a dialog on the user's (admin) screen to ask approval for a new user.
     * On "yes", the join is approved. On "no" or window close, the join is rejected.
     * @param uid
     * @return
     */
    private boolean _approveUser(UserIdentity uid) {
        approved = 0;
        JDialog d = new JDialog((JFrame) canvas.getParent().getParent().getParent().getParent(), "Approve user");
        d.setResizable(false);
        d.setSize(300, 100);

        d.setLayout(new FlowLayout());

        JLabel label = new JLabel("Approve " + uid.username + "?\n");
        JButton yes = new JButton("Yes");
        JButton no = new JButton("No");

        yes.addActionListener(e -> {
            approved = 1;
        });
        no.addActionListener(e -> {
            approved = -1;
        });

        d.add(label);
        d.add(yes);
        d.add(no);

        d.setVisible(true);

        while (approved == 0 && d.isVisible()) {
            Thread.onSpinWait();
        }

        d.dispose();
        return approved == 1;
    }

    /**
     * Called by the GUI when the disconnect button is pressed. Tells the server the client is d/cing
     */
    public void notifyDisconnect() {
        try {
            remoteWhiteboard.notifyDisconnect(uid);
        } catch (RemoteException ignored) {}
    }

    /**
     * Called by the server to notify this client of a new user joining the server. Updates the GUI
     * @param username
     * @throws RemoteException
     */
    public void notifyUserJoin(String username) throws RemoteException {

        gui.addClientUser(username);
    }

    /**
     * Called by the server to notify this client that a user has left the server
     * @return
     */
    public void notifyUserLeft(String username) throws RemoteException {
        gui.rmClientUser(username);
    }

    boolean isAdmin() {
        return admin;
    }

    public void newChatMessage(String username, String message) throws RemoteException {
        gui.postToChat(username, message);
    }

    public void sendToChat(String message) {
        try {
            remoteWhiteboard.sendMessage(uid, message);
        } catch (RemoteException ignored) {}
    }

    /**
     * Downloads the array of drawings stored at the server, and serialises them into a .canvas file
     */
    public void download(File file){
        try {
            ObjectOutputStream write = new ObjectOutputStream(new FileOutputStream(file));
            write.writeObject(remoteWhiteboard.getCanvas(uid));
            write.close();
        } catch (Exception e) {
            System.out.println("Saving whiteboard failed!");
        }
    }

    /**
     * Reads the stored .canvas file and sends all drawings to the server
     * MUST BE ADMIN!
     */
    public void upload(File file) {
        try {
            ObjectInputStream read = new ObjectInputStream(new FileInputStream(file));
            ArrayList<Drawing> drawings;
            try {
                drawings = (ArrayList<Drawing>) read.readObject();
            } catch (ClassCastException c) {
                System.out.println("Saved canvas not readable");
                return;
            }
            read.close();
            remoteWhiteboard.clearCanvas(uid);
            remoteWhiteboard.drawAllToCanvas(uid, drawings);

        } catch (FileNotFoundException e) {

        } catch (IOException e){

        } catch (ClassNotFoundException e){

        }
    }

    public void removeUser(String kickID) {
        try {
            remoteWhiteboard.removeUser(kickID);
        } catch (RemoteException ignored) {}
    }

    public void reset(){
        gui.reset();
    }

}
