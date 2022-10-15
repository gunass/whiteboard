package client;

import drawing.Drawing;
import server.IRemoteWhiteboard;
import util.UserIdentity;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for client canvas manager. The server calls these methods and so mutates the underlying canvas.
 * @author Alex Epstein
 */
public interface IInteractiveCanvasManager extends Remote {

    void addDrawing(Drawing drawing) throws RemoteException;

    void clearCanvas() throws RemoteException;

    boolean approveUser(UserIdentity uid) throws RemoteException;

    void notifyUserJoin(String username) throws RemoteException;

    void notifyUserLeft(String username) throws RemoteException;

    void newChatMessage(String username, String message) throws RemoteException;

    void reset() throws RemoteException;
}
