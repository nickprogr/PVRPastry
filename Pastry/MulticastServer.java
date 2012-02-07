package Pastry;

/**
 * 
 * @author nickprogr
 */

import java.io.IOException;
import java.net.MulticastSocket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;



public class MulticastServer implements Runnable {
    
    NodeAddress addr;

    
    
    public MulticastServer (NodeAddress addr){
        this.addr = addr;
    }
    
    
    
    /**
     * Runs a multicast server in a new thread. The server for any new request
     * sends a DatagramPacket object containing the ID and the IP address
     * of the responding node.
     */
    public void run(){
        
        
        MulticastSocket listeningSocket = null;
        DatagramSocket responseSocket = null;
        DatagramPacket joinMsg = null;
        DatagramPacket bootstrapMsg  = null;
        InetAddress multicastGroup = null;
        byte[] buf = null;

        
        
        /* Preparation and Binding */
        try{
            
            listeningSocket = new MulticastSocket(4446);
            listeningSocket.setReuseAddress(true);

            responseSocket = new DatagramSocket();

            multicastGroup = InetAddress.getByName("235.1.1.1");

            listeningSocket.joinGroup(multicastGroup);


            buf = new byte[32];
            bootstrapMsg = new DatagramPacket(buf, buf.length);
            joinMsg = new DatagramPacket(buf, buf.length);
            
        } 
        catch (Exception e){
            e.printStackTrace();
        }
        
        
        
        
        
        /* Receiving Request and Sending Answer */
        while (!Thread.interrupted()){
            try{
                listeningSocket.receive(joinMsg);
                System.out.println("Received Multicast Message from "+joinMsg.getAddress().getHostAddress());

                String request = (String) Utilities.byteArrayToObject(joinMsg.getData());

                if(request.equals("BOOT")) {

                    //info.setBoot_clue(NodeClient.numOfNodes);
                    buf = Utilities.objectToByteArray(addr);
                    bootstrapMsg = new DatagramPacket(buf, buf.length, joinMsg.getAddress(), 4440);
                    responseSocket.send(bootstrapMsg);
                }
                
             }
             catch (IOException ex){
                 ex.printStackTrace();
             }
             catch (ClassNotFoundException cnfe){
                 cnfe.printStackTrace();
             }

        }
             
    }
      
}
