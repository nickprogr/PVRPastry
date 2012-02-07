/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Pastry;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dre
 */

public class MessageHandler implements Runnable{
    NodeAddress addr;
    Socket sock_fd;

    public MessageHandler(Socket s_fd){
        sock_fd = s_fd;
    }

    public void run() {
        try {
            InputStream in = sock_fd.getInputStream();

            byte[] buf = new byte[6000];
            in.read(buf);

            Message msg = (Message) Utilities.byteArrayToObject(buf);
            checkMessage(msg);

        } catch (IOException ex) {
            Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
                Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method that recognizes the type of the message and decides further action.
     */

    private void checkMessage(Message receivedMsg){

        //...//
        //System.out.println(receivedMsg.getMsg());

        if(receivedMsg.getMsg().startsWith("JOIN")){
            Pastry.route(receivedMsg, AppInstance.myNode);
        }
        else if(receivedMsg.getMsg().equals("ROUTEROW")){
            AppInstance.constructRoutingTableRow(receivedMsg);
        }
        /* currently not implemented */
        /*else if(receivedMsg.getMsg().equals("NEIGHSET")){
            AppInstance.constructNeighborhoodSet(receiveMsg);
        }*/
        else if(receivedMsg.getMsg().equals("LEAFSET")){
            AppInstance.constructLeafSet(receivedMsg);
        }
        else if(receivedMsg.getMsg().equals("STATE")){
            AppInstance.updateState(receivedMsg);
        }




        //...//
        else{

            System.err.println(receivedMsg.getMsg());

        }


        /* OTHER CASES */


    }
}
