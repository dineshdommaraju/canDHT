//package Server;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


public class Server extends UnicastRemoteObject implements remoteInterface{
	private static final long serialVersionUID = 1L;
	ArrayList<String> nodesList=new ArrayList<String>();
	
	protected Server() throws RemoteException {
		super();
		Registry registry = LocateRegistry.createRegistry(6000);
		registry.rebind("server", this);
	}
	
	public String getBootStrapNode(String ipAddress) throws RemoteException, NotBoundException
	{
		System.out.println("getBootStrapNode 1");
		if(nodesList.isEmpty())
		{
			nodesList.add(ipAddress);
			return "FirstNode";
			
		}else{
			if(nodesList.contains(ipAddress))
				return " ";
			nodesList.add(ipAddress);
			return  nodesList.get(0);
			
		}
	}
	
	public static void main(String[] args) throws RemoteException {
		Server obj=new Server();
		
	}
	
	

}
