package Pastry;

/**
 * 
 * @author  nickprogr
 */

import java.io.Serializable;
import java.net.InetAddress;


public class NodeAddress implements Serializable,Comparable{

    private String nodeID;
    //private InetAddress ip;
    private String ip;
    private int socketPortNumber;



    public NodeAddress(String nodeID_PreHashed, InetAddress ip, int port) {
        
        // ... //
        //System.out.println(nodeID_PreHashed);
        
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
    
    /*public InetAddress getIp() {
        return ip;
    }*/
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
    
    /*public void setIp(InetAddress ip) {
        this.ip = ip;
    }*/
    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setSocketPortNumber(int socketPortNumber) {
        this.socketPortNumber = socketPortNumber;
    }
    
    

    
    
    
    @Override
    public int compareTo(Object o) {
        
        //...//
        if(this == null)    System.err.println("!!!    NULL    !!!");

        if(this.getNodeID().compareTo(((NodeAddress) o).getNodeID()) > 0)
            return 1;
        else if(this.getNodeID().compareTo(((NodeAddress) o).getNodeID()) == 0)
            return 0;
        else
            return -1;
        
    }


}
