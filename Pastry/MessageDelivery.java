package Pastry;

import java.io.OutputStream;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class MessageDelivery {
    
    public static void sendMessage(Message msg, NodeAddress addr) throws ConnectException{
        
        DatagramSocket outgoingSocket = null;
        DatagramPacket outgoingMsg = null;
        OutputStream out = null;
        Socket outSock = null;
        byte[] buf = null;
        
        
        try{
            
            buf = new byte[500];
            
            
            buf = Utilities.objectToByteArray(msg);
            
            outgoingSocket = new DatagramSocket();

            InetSocketAddress remoteIP = new InetSocketAddress(addr.getIp(),addr.getSocketPortNumber()); 
            outSock = new Socket();                                                
            outSock.connect(remoteIP, 2500);                                       
            outSock.setSoTimeout(2500);                                            

            outgoingMsg = new DatagramPacket(buf, buf.length, InetAddress.getByName(addr.getIp()), addr.getSocketPortNumber());
            
        }
        catch (Exception e){
            
            if(e.getClass().getName().equals("java.net.ConnectException")){
                System.err.println(addr.getNodeID()+" has departed !");
                throw ((ConnectException)e);
            }
            
            e.printStackTrace();
            return;
        }
        
        
        
        
        
        try{
            
            System.out.println("Sending "+msg.getMsg()+" message to "+addr.getNodeID()+"...");

            out = outSock.getOutputStream();        
            out.write(outgoingMsg.getData());       
            
            outSock.close();
        }
        catch (SocketTimeoutException ste){
            System.out.println("Could not send the message.");
            outgoingSocket.close();
        }
        catch (Exception e){
            e.printStackTrace();
            outgoingSocket.close();
        } 
        
    }
    
    
}
