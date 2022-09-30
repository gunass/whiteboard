package server;

import drawing.Drawing;
import server.IWhiteboard;

import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Stack;

/**
 * Remote object that holds all image data (canvas image, and stack of Drawings)
 * @author Alex Epstein with thanks to skeleton code: https://en.wikipedia.org/wiki/Java_remote_method_invocation
 */

// FIXME: insecure. See https://community.oracle.com/tech/developers/discussion/1179889/simple-rmi-authentication
public class Whiteboard extends UnicastRemoteObject implements IWhiteboard {
    private final int DEFAULT_WIDTH = 500;
    private final int DEFAULT_HEIGHT = 500;
    private final int DEFAULT_COLOURSPACE = BufferedImage.TYPE_INT_RGB;

    BufferedImage canvas;
    Stack<Drawing> drawings;

    /**
     * Creates a new whiteboard based on the provided canvas.
     * @param canvas
     */
    public Whiteboard(BufferedImage canvas) throws RemoteException {
        super(0);
        this.canvas = canvas;
        this.drawings = new Stack<Drawing>();
    }

    /**
     * Creates a new whiteboard with a blank canvas
     */
    public Whiteboard() throws RemoteException {
        super(0);
        this.drawings = new Stack<Drawing>();
        this.canvas = new BufferedImage(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_COLOURSPACE);
    }


    public BufferedImage getCanvas() {
        return canvas;
    }

    public void drawToCanvas(Drawing drawing) {
        // FIXME: stub
    }

    protected void resetCanvas() {

    }

}
