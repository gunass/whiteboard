package server;

import drawing.Drawing;
import util.UserIdentity;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IRemoteWhiteboard extends Remote {

    public boolean joinWhiteboard(UserIdentity uid) throws RemoteException;
    public boolean startWhiteboard(UserIdentity uid) throws RemoteException;
    public ArrayList<Drawing> getCanvas(UserIdentity uid) throws RemoteException;

    public void drawToCanvas(UserIdentity uid, Drawing drawing) throws RemoteException;
    public void clearCanvas(UserIdentity uid) throws RemoteException;
    public ArrayList<UserIdentity> getUsers(UserIdentity uid) throws RemoteException;

}
