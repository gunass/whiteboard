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

    void addDrawing(UserIdentity uid, Drawing drawing) throws RemoteException;

    void clearCanvas(UserIdentity uid) throws RemoteException;

    boolean approveUser(UserIdentity uid, UserIdentity newuid) throws RemoteException;

    void notifyUserJoin(UserIdentity uid, String username) throws RemoteException;

    void notifyUserLeft(UserIdentity uid, String username) throws RemoteException;

    void newChatMessage(UserIdentity uid, String username, String message) throws RemoteException;

    void reset(UserIdentity uid) throws RemoteException;
}
