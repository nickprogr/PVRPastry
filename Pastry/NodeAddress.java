package Pastry;

import java.io.Serializable;
import java.net.InetAddress;


public class NodeAddress implements Serializable,Comparable{

    private String nodeID;
    private String ip;
    private int socketPortNumber;



    public NodeAddress(String nodeID_PreHashed, InetAddress ip, int port) {
        
        this.nodeID = Hashing.SHA1_128bit(nodeID_PreHashed);  
        this.ip = ip.getHostAddress();
        this.socketPortNumber = port;
    }

    public NodeAddress() {
    }


    
    

    //getters
    public String getNodeID() {
        return nodeID;
    }
    
    public String getIp() {
        return ip;
    }

    public int getSocketPortNumber() {
        return socketPortNumber;
    }

    


    
    

    //setters
    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setSocketPortNumber(int socketPortNumber) {
        this.socketPortNumber = socketPortNumber;
    }
    
    

    
    
    
    @Override
    public int compareTo(Object o) {
        
        if(this.getNodeID().compareTo(((NodeAddress) o).getNodeID()) > 0)
            return 1;
        else if(this.getNodeID().compareTo(((NodeAddress) o).getNodeID()) == 0)
            return 0;
        else
            return -1;
        
    }


}
