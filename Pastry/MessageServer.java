package Pastry;

/**
 *
 * @author nickprogr
 */



import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MessageServer  implements Runnable {
    
    NodeAddress addr;

    
    
    public MessageServer (NodeAddress addr){
        this.addr = addr;
    }

    public void run(){
        try {
            ServerSocket server = new ServerSocket(addr.getSocketPortNumber());
            while (!Thread.interrupted()){
                Socket sock_fd = server.accept();

                Thread spawn = new Thread(new MessageHandler(sock_fd));
                spawn.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(MessageServer.class.getName()).log(Level.SEVERE, null, ex);
        }
 ;
    }

    /*
    public void run(){
        
        
        DatagramSocket listeningSocket = null;
        DatagramPacket incomingMsg = null;
        Message msg = null;
        byte[] buf = null;
        
        
        
        try{
            
            listeningSocket = new DatagramSocket(this.addr.getSocketPortNumber());
            
            buf = new byte[6000];
            incomingMsg = new DatagramPacket(buf, buf.length);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        
        
        
        
            
        while (!Thread.interrupted()){
        
            try{
                
                listeningSocket.receive(incomingMsg);

                //...//
                //System.out.println("L:  "+incomingMsg.getData().length);
                
                
                msg = (Message) Utilities.byteArrayToObject(incomingMsg.getData());
                System.out.println("Received "+msg.getMsg()+" Message from "+msg.getAddress().getNodeID());
                
                
                //...//
                //System.out.println("TEST :    "+msg.getObject().getClass().getName());
                //System.out.println(((RoutingTableRow) msg.getObject()).getRow().length);
                
                
                this.checkMessage(msg);
                
            }    
            catch (Exception e){
                e.printStackTrace();
            }   
     
        }
         
    } 
    
    
    */
    
    
    
    
    /**
     * Method that recognizes the type of the message and decides further action.
     */
    
    //private void checkMessage(Message receivedMsg){
        
        //...//
        //System.out.println(receivedMsg.getMsg());
        
        /*if(receivedMsg.getMsg().startsWith("JOIN")){ 
            Pastry.route(receivedMsg, AppInstance.myNode);
        }
        else if(receivedMsg.getMsg().equals("ROUTEROW")){
            AppInstance.constructRoutingTableRow(receivedMsg);
        }*/
        /* currently not implemented */
        /*else if(receivedMsg.getMsg().equals("NEIGHSET")){
            AppInstance.constructNeighborhoodSet(receiveMsg);
        }*/
        /*else if(receivedMsg.getMsg().equals("LEAFSET")){
            AppInstance.constructLeafSet(receivedMsg);
        }
        else if(receivedMsg.getMsg().equals("STATE")){
            AppInstance.updateState(receivedMsg);
        }*/
        
        
        
        
        //...//
        /*else{
            
            System.err.println(receivedMsg.getMsg());
            
        }*/
        
        
        /* OTHER CASES */
        
        
    //}
    
    
}
