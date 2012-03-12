package Pastry;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;



public class MulticastRequest {
    
    /**
     * Makes a multicast request and awaits an answer from a node. 
     * The node that answered is going to help our node to build its state tables.
     */
    public static NodeAddress makeRequest() {
    
        
        MulticastSocket requestSocket = null;
        DatagramSocket listeningSocket = null;
        DatagramPacket answer = null;
        DatagramPacket request = null;
        InetAddress multicastGroup = null;
        byte[] buf = null;
        
        
        /* Preparation and Binding */
        try{
            
            final String req_msg = "BOOT";
            buf = new byte[32];
            buf = Utilities.objectToByteArray(req_msg);

            /* Bind the sockets */
            listeningSocket = new DatagramSocket(4440);
            requestSocket = new MulticastSocket(4446);
            multicastGroup = InetAddress.getByName("235.1.1.1");


            request = new DatagramPacket(buf, buf.length, multicastGroup, 4446);
            
            byte buff[] = new byte[320];
            answer = new DatagramPacket(buff, buff.length);
            
        }
        catch (Exception e){
            e.printStackTrace();
        }
        
        
        
        
        /* Sending Request and Receiving Answer */
        try{
            
            requestSocket.setLoopbackMode(false);
            System.out.println("Making join request...");
            requestSocket.send(request);
            listeningSocket.setSoTimeout(1000);



            listeningSocket.receive(answer);
            NodeAddress addr = (NodeAddress)Utilities.byteArrayToObject(answer.getData());
            System.out.println("Found a bootstrap server at: "+ addr.getIp()+" "+addr.getNodeID());

            listeningSocket.close();
            requestSocket.close();

            return addr;
            
        }
        catch (SocketTimeoutException ste){
            System.out.println("Did not found bootstrap server.");
            listeningSocket.close();
            requestSocket.close();
            return null;
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
        
    }
    
}
