//package Server;

import java.rmi.*;


public interface remoteInterface extends Remote{
	public String getBootStrapNode(String ipAddress) throws RemoteException,NotBoundException;

}
