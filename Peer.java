import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

class Node implements Serializable
{
	private static final long serialVersionUID = 1L;
	float lx,ly,ux,uy;
	String IPAddress;
	//ArrayList<Node> neighbours = new ArrayList<Node>();
	HashMap<String, String> HashTable = new HashMap<String, String>();
	
	Node(float lx, float ly, float ux, float uy, String IPAddress)
	{
		this.lx=lx;this.ly=ly;
		this.ux=ux;this.uy=uy;
		this.IPAddress=IPAddress;	
	}
}

public class Peer extends UnicastRemoteObject implements remoteInterface,Serializable{
	
	Node peerNode;
	private static final long serialVersionUID = 1L;
	ArrayList<Node> neighbours = new ArrayList<Node>();
	HashMap<String, String> keywords = new HashMap<String, String>();
	
	protected Peer() throws RemoteException {
		super();
		Registry registry = LocateRegistry.createRegistry(6000);
		registry.rebind("peer", this);
	}
	
	boolean sameZone(float xCoordinate, float yCoordinate, Node peer)
	{
		System.out.println("In same zone");
		if (xCoordinate >= peer.lx && yCoordinate >= peer.ly
				&& xCoordinate <= peer.ux
				&& yCoordinate <= peer.uy) 
			return true;
		else 
			return false;
	}
	
	
	boolean divideHorizantally()
	{
		if(Math.abs(peerNode.lx-peerNode.ux)-Math.abs(peerNode.ly-peerNode.uy) >=0)
		{
			return false;
		}else{
			return true;
		}
	}
	
	boolean isNeighbor(Node newPeer, Node neighbor)
	{
		
		float breadth=Math.abs(neighbor.ly-neighbor.uy)+Math.abs(newPeer.ly-newPeer.uy);
		float length=Math.abs(neighbor.lx-neighbor.ux)+Math.abs(newPeer.lx-newPeer.ux);
		System.out.println("breadth :"+breadth);
		System.out.println("length :"+length);
		
		if(Math.abs(newPeer.ly-neighbor.uy) >breadth || Math.abs(newPeer.uy-neighbor.ly) >breadth)
			return false;
		if(Math.abs(newPeer.lx-neighbor.ux) >length || Math.abs(newPeer.ux-neighbor.lx) >length)
			return false;
		System.out.println("test isNeighbor");
		return true;
	}
	
	public void remoteUpdateNeighbor(Node peer,String Action) throws RemoteException, NotBoundException
	{
		System.out.println("remoteUpdateNeighbor 1 ");
		if(Action.equals("Add"))
		{
			this.neighbours.add(peer);
			return;
		}
		int i;
		for(i=0; i<this.neighbours.size();i++)
		{
			if(this.neighbours.get(i).IPAddress.equals(peer.IPAddress))
				break;
		}
		if(Action.equals("Update"))
			this.neighbours.remove(i);
		this.neighbours.add(peer);
	}
	
	ArrayList<Node> updatePeersNeighbors(Node newPeer) throws RemoteException, NotBoundException
	{
		System.out.println("updatePeersNeighbors 1");
		ArrayList<Node> newPeerNeighbor=new ArrayList<Node>();
		
		for(int i=0;i<this.neighbours.size();i++)
		{
			Registry peerRegistry = LocateRegistry.getRegistry(this.neighbours.get(i).IPAddress, 6000);
			remoteInterface peerRemoteObject = (remoteInterface) peerRegistry.lookup("peer");
			if(isNeighbor(newPeer,this.neighbours.get(i)))
			{
				System.out.println("Add");
				peerRemoteObject.remoteUpdateNeighbor(newPeer,"Add");
				newPeerNeighbor.add(this.neighbours.get(i));	
			}
			
			if(isNeighbor(this.peerNode, this.neighbours.get(i)))
			{
				System.out.println("Update");
				peerRemoteObject.remoteUpdateNeighbor(this.peerNode,"Update");
				
			}
			else
				peerRemoteObject.remoteUpdateNeighbor(this.peerNode,"Delete");
		}
		return newPeerNeighbor;	
	}
	
	float hashX(String keyword) {
		int sum=0;
		for(int i=0; i < keyword.length();i=i+2)
			sum += keyword.charAt(i);
		return (sum%10);
	}
	
	float hashY(String keyword) {
		int sum=0;
		for(int i=1; i < keyword.length();i=i+2)
			sum += keyword.charAt(i);
		return (sum%10);
	}
	
	HashMap<String,String> swapHashTables(Node newPeer)
	{
		System.out.println("Swapping hash tables");
		HashMap<String,String> newPeerKeywords=new HashMap<String,String>();
		//Iterator for the Existing keywords HashMap
		Iterator it = keywords.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
			if( sameZone(hashX(pairs.getKey()), hashY(pairs.getKey()), newPeer) )
			{
				keywords.remove(pairs.getKey());
				newPeerKeywords.put(pairs.getKey(), pairs.getValue());
			}		
		}
		return newPeerKeywords;
	}
	
	public void insert(float xCoordinate, float yCoordinate, String IPAddress) throws RemoteException, NotBoundException
	{
		System.out.println("Insert 1");
		Node newPeer;
		if(sameZone(xCoordinate,xCoordinate,this.peerNode))
		{
			if(divideHorizantally())
			{
				System.out.println("Divided horizontally");
				if(yCoordinate <= (this.peerNode.uy/2))
				{
					newPeer=new Node(this.peerNode.lx,this.peerNode.ly,this.peerNode.ux,this.peerNode.uy/2,IPAddress);
					this.peerNode.ly=this.peerNode.uy/2;
				}else{
					newPeer=new Node(this.peerNode.lx,this.peerNode.uy/2,this.peerNode.ux,this.peerNode.uy,IPAddress);
					this.peerNode.uy=this.peerNode.uy/2;
				}
			}else{
				System.out.println("Divided vertically");
				if(xCoordinate <= (this.peerNode.ux/2))
				{
					newPeer=new Node(this.peerNode.lx,this.peerNode.ly,this.peerNode.ux/2,this.peerNode.uy,IPAddress);
					this.peerNode.lx=this.peerNode.ux/2;
				}else{
					newPeer=new Node(this.peerNode.ux/2,this.peerNode.ly,this.peerNode.ux,this.peerNode.uy,IPAddress);
					this.peerNode.ux=this.peerNode.ux/2;
				}
				
			}
			//Updating the Neighbors
			ArrayList<Node> newPeerNeighbor=updatePeersNeighbors(newPeer);
			this.neighbours.add(newPeer);
			System.out.println("Added new peer");
			newPeerNeighbor.add(this.peerNode);
			//Swap hash tables
			HashMap<String,String> newPeerKeywords=swapHashTables(newPeer);
			
			System.out.println(" "+newPeer.IPAddress);
			Registry peerRegistry = LocateRegistry.getRegistry(newPeer.IPAddress, 6000);
			remoteInterface peerRemoteObject = (remoteInterface) peerRegistry.lookup("peer");
			peerRemoteObject.remoteFinalInsertUpdate(newPeer,newPeerKeywords,newPeerNeighbor);
			
		}else{
			//redirecting
			String temp_IPAddress=this.redirect(xCoordinate, yCoordinate);
			Registry peerRegistry = LocateRegistry.getRegistry(temp_IPAddress, 6000);
			remoteInterface peerRemoteObject = (remoteInterface) peerRegistry.lookup("peer");
			peerRemoteObject.insert(xCoordinate,yCoordinate,IPAddress);
				
		}
	}
	
	String redirect(float xCoordinate, float yCoordinate) throws RemoteException, NotBoundException
	{
		System.out.println(" IP 2 : "+this.peerNode.IPAddress);
		float distance,distanceMin = 100;
		int minIndex = 0;
		for (int i = 0; i < this.neighbours.size(); i++) {
			distance = (float) (Math.pow((xCoordinate - ((this.neighbours.get(i).lx + this.neighbours.get(i).ux) / 2)), 2))
					  + (float) (Math.pow((yCoordinate-((this.neighbours.get(i).ly + this.neighbours.get(i).      uy) / 2)), 2));
			if(distance < distanceMin)
			{
				distanceMin = distance;
				minIndex = i;
			}		
		}
		System.out.println("Minimum Index1 : " +minIndex);
		System.out.println("redirect IP : ");
		
		System.out.println(this.neighbours.get(minIndex).IPAddress);
		return this.neighbours.get(minIndex).IPAddress;
		
	}
	public void remoteFinalInsertUpdate(Node newPeer,HashMap<String,String> keywords,ArrayList<Node> neighbours) 
														throws RemoteException, NotBoundException
	{
		System.out.println("remote Final Insert Update");
		this.peerNode=newPeer;
		this.keywords=keywords;
		this.neighbours=neighbours;
	}
	public ArrayList<String> search(String keyword, float xCoordinate, float yCoordinate,String Action,ArrayList<String> path) throws RemoteException, NotBoundException
	{
		System.out.println("xCoordinate : "+xCoordinate);
		System.out.println("yCoordinate :"+yCoordinate);
		path.add(this.peerNode.IPAddress);
		if(sameZone(xCoordinate,yCoordinate,this.peerNode))
		{
			if(Action.equals("Insert"))
			{
				this.keywords.put(keyword, " ");
			}else{
				System.out.println("Keyword Found at" + this.peerNode.IPAddress);
				
			}
		}else{
			System.out.println(" IP :"+this.peerNode.IPAddress);
			String temp_IPAddress=this.redirect(xCoordinate, yCoordinate);
			System.out.println("Redirect IPAddress"+temp_IPAddress);
			Registry peerRegistry = LocateRegistry.getRegistry(temp_IPAddress, 6000);
			remoteInterface peerRemoteObject = (remoteInterface) peerRegistry.lookup("peer");
			peerRemoteObject.search(keyword, xCoordinate, yCoordinate, Action,path);
		}
		return path;
	}
	
	void insertKeyword(String keyword) throws RemoteException, NotBoundException
	{	
		ArrayList<String> path=new ArrayList<String>();
		path=search(keyword,hashX(keyword),hashY(keyword),"Insert",path);
		System.out.println(" Path Traversed :");
	}
	
	void searchKeyword(String keyword) throws RemoteException, NotBoundException
	{
		ArrayList<String> path=new ArrayList<String>();
		path=search(keyword,hashX(keyword),hashY(keyword),"Search",path);
		if(path.size()!=0)
			System.out.println("Keyword Found \n"+"path Traversed : "+path);
		else
			System.out.println("Keyword not found!");
	}
	
	void updateNode(Node updNode, float lx, float ly, float ux, float uy)
	{
		updNode.lx=lx;updNode.ly=ly;
		updNode.ux=ux;updNode.uy=uy;
	}
	void canExtend(Node leaveNode, Node node ) throws RemoteException, NotBoundException
	{
		if (leaveNode.lx > node.lx && node.ly >= leaveNode.ly && node.uy <= leaveNode.uy)
		{
			if(node.ly==leaveNode.ly)
			{
				updateNode(node,node.lx,node.ly,leaveNode.ux,node.uy);
				updateNode(leaveNode,leaveNode.lx,node.uy,leaveNode.ux,leaveNode.uy);
			}else if(node.uy==leaveNode.uy)
			{
				updateNode(node,node.lx,node.ly,leaveNode.ux,node.uy);
				updateNode(leaveNode,leaveNode.lx,node.ly,leaveNode.ux,leaveNode.uy);
			}
			
			
		}else if(leaveNode.lx < node.lx &&  node.ly >= leaveNode.ly && node.uy <= leaveNode.uy)
		{
			if(node.ly==leaveNode.ly)
			{
				updateNode(node,leaveNode.lx,node.ly,node.ux,node.uy);
				updateNode(leaveNode,leaveNode.lx,node.uy,leaveNode.ux,leaveNode.uy);
				
			}else if(node.uy==leaveNode.uy)
			{
				updateNode(node,leaveNode.lx,node.ly,node.ux,node.uy);
				updateNode(leaveNode,leaveNode.lx,node.ly,leaveNode.ux,leaveNode.uy);
			}
			
		}else if(leaveNode.ly > node.ly && node.lx >= leaveNode.lx && node.ux <= leaveNode.ux)
		{
			if(node.lx==leaveNode.lx)
			{
				updateNode(node,node.lx,node.ly,node.ux,leaveNode.uy);
				updateNode(leaveNode,node.ux,leaveNode.ly,leaveNode.ux,leaveNode.uy);
				
			}else if(node.ux==leaveNode.ux)
			{
				updateNode(node,node.lx,node.ly,node.ux,leaveNode.uy);
				updateNode(leaveNode,leaveNode.lx,leaveNode.ly,node.lx,leaveNode.uy);
			}
			
			
		}else if(leaveNode.ly < node.ly && node.lx >= leaveNode.lx && node.ux <= leaveNode.ux){
			
			if(node.lx==leaveNode.lx)
			{
				updateNode(node,node.lx,leaveNode.ly,node.ux,node.uy);
				updateNode(leaveNode,node.ux,leaveNode.ly,leaveNode.ux,leaveNode.uy);
				
			}else if(node.ux==leaveNode.ux)
			{
				updateNode(node,node.lx,leaveNode.ly,node.ux,node.uy);
				updateNode(leaveNode,leaveNode.lx,leaveNode.ly,node.lx,leaveNode.uy);
			}
			
		}
	}
	void leaveNode() throws RemoteException, NotBoundException
	{
		for(int i=0;i < this.neighbours.size();i++)
		{
			canExtend(this.peerNode,this.neighbours.get(i));
		}
	}
	void Join(String IPAddress, int port, float x, float y) throws RemoteException, NotBoundException, UnknownHostException
	{
		Registry registry = LocateRegistry.getRegistry(IPAddress, port);
		remoteInterface otherObj = (remoteInterface) registry.lookup("server");
		System.out.println("test Join 1");
		String result = otherObj.getBootStrapNode(InetAddress.getLocalHost().getHostAddress());
		
		if(result.equals("FirstNode"))
		{
			this.peerNode=new Node(0,0,10,10,InetAddress.getLocalHost().getHostAddress());
		}else{
			System.out.println("result : "+result);
			Registry peerRegistry = LocateRegistry.getRegistry(result, port);
			remoteInterface peerRemoteObject = (remoteInterface) peerRegistry.lookup("peer");
			peerRemoteObject.insert(x,y,InetAddress.getLocalHost().getHostAddress());
			
		}
			
	}
	
	void viewNode()
	{
		System.out.println("( "+this.peerNode.lx+" , "+this.peerNode.ly+" )");
		System.out.println("( "+this.peerNode.ux+" , "+this.peerNode.uy+" )");
		System.out.println("IPAddress :"+this.peerNode.IPAddress);
		System.out.println("---------------------------------------");
		System.out.println("Neighbors");
		for(int i=0; i < this.neighbours.size();i++)
		{
			System.out.println("( "+this.neighbours.get(i).lx+" , "+this.neighbours.get(i).ly+" )");
			System.out.println("( "+this.neighbours.get(i).ux+" , "+this.neighbours.get(i).uy+" )");
			System.out.println("IPAddress : "+this.neighbours.get(i).IPAddress);
			System.out.println("---------------------------------------");
		}
	}
	
	void userPrompt() throws RemoteException, NotBoundException, UnknownHostException
	{
		while(true)
		{
			Scanner sc = new Scanner(System.in);
			String input = sc.next();
			if (input.equals("join")) {
				float x=sc.nextFloat();
				float y=sc.nextFloat();
				Join("129.21.135.188", 6000,x,y);
			}else if(input.equals("Insert"))
			{
				insertKeyword(sc.next());
			}else if(input.equals("Search"))
			{
				searchKeyword(sc.next());
			}else if(input.equals("Leave")){
				leaveNode();
			}else if(input.equals("view"))
			{
				viewNode();
			}
		}
	}
	
	public static void main(String[] args) throws RemoteException, NotBoundException, UnknownHostException {
		Peer pr=new Peer();
		pr.userPrompt();
		
	}

	@Override
	public String getBootStrapNode(String ipAddress) throws RemoteException,
			NotBoundException {
		return null;
	}

}
