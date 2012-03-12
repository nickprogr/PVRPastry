package Pastry;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MessageServer  implements Runnable {
    
    NodeAddress addr;
    static ServerSocket server;

    
    
    public MessageServer (NodeAddress addr){
        this.addr = addr;
    }

    public void run(){
        try {
            server = new ServerSocket(addr.getSocketPortNumber());
            while (!Thread.interrupted()){
                Socket sock_fd = server.accept();

                Thread spawn = new Thread(new MessageHandler(sock_fd));
                spawn.start();
            }
        } catch (IOException ex) {
            if(ex.getClass().getName().equals("java.net.SocketException")){
                if(!AppInstance.departing) 
                    ex.printStackTrace();
            }
            else
                Logger.getLogger(MessageServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    
}
