package client;

import drawing.Drawing;
import server.IRemoteWhiteboard;
import util.UserIdentity;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
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
    private final String rmiReference;
    private final IRemoteWhiteboard remoteWhiteboard;
    protected final InteractiveCanvas canvas;
    private volatile int approved = 0;
    private boolean admin = false;

    /**
     * Creates a new ICM that communicates with the remote whiteboard, and manages the display of the canvas.
     * @param rmiReference
     * @param uid
     * @param width
     * @param height
     * @throws Exception
     */
    public InteractiveCanvasManager(String rmiReference, UserIdentity uid, int width, int height) throws Exception {

        this.uid = uid;
        this.rmiReference = rmiReference;

        //FIXME: expand beyond localhost
        String hostname = "localhost";

        try {
            Naming.rebind("//"+hostname+"/" + uid.username, this);
            System.out.println("Client bound in registry!");
        } catch (RemoteException | MalformedURLException e) {
            System.out.println("RMI error");
            throw new Exception();
        }

        this.remoteWhiteboard = (IRemoteWhiteboard) Naming.lookup(rmiReference);

        if (!remoteWhiteboard.startWhiteboard(uid)) {
            if (!remoteWhiteboard.joinWhiteboard(uid)) {
                System.out.println("Join failed");
                throw new Exception();
            }
        } else {
            admin = true;
        }
        System.out.println("Join success");

        this.canvas = new InteractiveCanvas(width, height, uid.username, this);
        canvas.drawings = remoteWhiteboard.getCanvas(uid);
        System.out.println("Drawings get success");


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
        canvas.drawings = new ArrayList<>();
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

    boolean isAdmin() {
        return admin;
    }

}
