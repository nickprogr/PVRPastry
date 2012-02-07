package Pastry;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

/**
 *
 * @author nickprogr
 */


public class MessageDelivery {
    
    /**
     * 
     * 
     */
    public static void sendMessage(Message msg, NodeAddress addr) {
        
        DatagramSocket outgoingSocket = null;
        DatagramPacket outgoingMsg = null;
        OutputStream out = null;
        Socket outSock = null;
        byte[] buf = null;
        
        
        try{
            
            buf = new byte[500];
            
            //...//
            //System.out.println("TTTT:  "+Utilities.objectToByteArray(msg).length);
            //System.out.println("KKKK:  "+Utilities.objectToByteArray(addr).length);
            
            buf = Utilities.objectToByteArray(msg);
            
            outgoingSocket = new DatagramSocket();

            InetSocketAddress remoteIP = new InetSocketAddress(addr.getIp(),addr.getSocketPortNumber()); // NEW CODE
            outSock = new Socket();                                                // NEW CODE
            outSock.connect(remoteIP, 5000);                                       // NEW CODE
            outSock.setSoTimeout(5000);                                            // NEW CODE

            outgoingMsg = new DatagramPacket(buf, buf.length, InetAddress.getByName(addr.getIp()), addr.getSocketPortNumber());
            
        }
        catch (Exception e){
            e.printStackTrace();
        }
        
        
        
        
        
        try{
            
            System.out.println("Sending "+msg.getMsg()+" message...");
            //outgoingSocket.send(outgoingMsg);

            out = outSock.getOutputStream();        // NEW CODE
            out.write(outgoingMsg.getData());       // NEW CODE
            
            outSock.close();
        }
        catch (SocketTimeoutException ste){
            System.out.println("Could not send the message.");
            outgoingSocket.close();
        }
        catch (Exception e){
            e.printStackTrace();
        } 
        
    }
    
    
}
