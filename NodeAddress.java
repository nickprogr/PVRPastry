//NIKOLAOS VITSAS 3070011
//NIKOLAOS PROMPONAS-KEFALAS 3070172
//PANAGIOTIS ROUSIS 3070149
//POLITIS CHRISTOS 3070169
package Pastry;

import java.io.Serializable;
import java.net.InetAddress;


public class NodeAddress implements Serializable{

    private String nodeID;
    private InetAddress ip;



    public NodeAddress(String nodeID_PreHashed, InetAddress ip) {
        this.nodeID = Hashing.SHA1_128bit(nodeID_PreHashed);  
        this.ip = ip;
    }

    public NodeAddress() {
    }


    
    

    //getters
    public InetAddress getIp() {
        return ip;
    }

    public String getNodeID() {
        return nodeID;
    }


    
    

    //setters
    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }



}
