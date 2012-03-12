package Pastry;

import Application.AppUtilities;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Calendar;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;


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
            
            if(ex.getClass().getName().equals("java.io.StreamCorruptedException")){
                /* DO NOTHING */
            }
            else
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
        System.out.println("Received "+receivedMsg.getMsg()+" message from "+receivedMsg.getAddress().getNodeID()+"...");
        
        

        if(receivedMsg.getMsg().startsWith("JOIN")){
            Pastry.route(receivedMsg);
        }
        else if(receivedMsg.getMsg().equals("ROUTEROW")){
            AppInstance.constructRoutingTableRow(receivedMsg);
        }
        else if(receivedMsg.getMsg().startsWith("ROUTE_ENTRY")){
            FaultHandler.RequestRouteEntry(receivedMsg);
        }
        /* currently not implemented */
        /*else if(receivedMsg.getMsg().equals("NEIGHSET")){
            AppInstance.constructNeighborhoodSet(receiveMsg);
        }*/
        else if(receivedMsg.getMsg().equals("STATE")){
            AppInstance.updateState(receivedMsg);
        }
        
        
        
        /* ACK MESSAGES */
        
        else if(receivedMsg.getMsg().equals("ACK_LEAF_QUESTION")){
            
            try{
            
                if(AppInstance.myNode.getState().getTimeLastUpdated() > receivedMsg.getTimestamp()){
                    Pastry.forward(new Message("LEAFSET", AppInstance.myNode.getAddress(), AppInstance.myNode.getState().getLeafSet(), receivedMsg.getKey(), Calendar.getInstance().getTimeInMillis()), receivedMsg.getAddress());
                }
                else{
                    Pastry.forward(new Message("ACK_LEAF_ANSWER", AppInstance.myNode.getAddress(), null, receivedMsg.getKey(), receivedMsg.getTimestamp()), receivedMsg.getAddress());
                }
                
            }
            catch(ConnectException ce){/* Do nothing */}    
            
        }
        else if(receivedMsg.getMsg().equals("ACK_LEAF_ANSWER")){
            AppInstance.stateBuilt = true;
        
            /* Inform all nodes in current node's state. */
            AppInstance.informArrival();
        }
        else if(receivedMsg.getMsg().equals("ACK_LEAF_SMALL_QUESTION")){
            
            try{
                
                if(AppInstance.myNode.getState().getTimeLastUpdated() > receivedMsg.getTimestamp()){
                    AppInstance.myNode.getState().getLeafSet().semAcquire();
                    Pastry.forward(new Message("SMALL_LEAFSET_UPDATE", AppInstance.myNode.getAddress(), AppInstance.myNode.getState().getLeafSet().getSmallSet(), receivedMsg.getKey(), Calendar.getInstance().getTimeInMillis()), receivedMsg.getAddress());
                    AppInstance.myNode.getState().getLeafSet().semRelease();
                }
                else{
                    Pastry.forward(new Message("ACK_LEAF_SMALL_ANSWER", AppInstance.myNode.getAddress(), null, receivedMsg.getKey(), receivedMsg.getTimestamp()), receivedMsg.getAddress());
                } 
                
            }
            /**
             * If the node that asked for our Leaf Set to correct his state departs, we have to replace it,
             * because if we are on its Leaf Set, then it is also on ours.
             */
            catch(ConnectException ce){
                
                NodeAddress na = receivedMsg.getAddress();
                String type = null;
                
                
                if(na.getNodeID().compareTo(AppInstance.myNode.getAddress().getNodeID()) < 0)
                    type = "small";
                else
                    type = "large";
                
                
                AppInstance.myNode.getState().getLeafSet().remove(na);
                    
                
                int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t-1);
                
                Thread fault_h = new Thread(new FaultHandler(type));
                fault_h.start();
            }   
        }
        else if(receivedMsg.getMsg().equals("ACK_LEAF_LARGE_QUESTION")){
            
            try{
            
                if(AppInstance.myNode.getState().getTimeLastUpdated() > receivedMsg.getTimestamp()){
                    AppInstance.myNode.getState().getLeafSet().semAcquire();
                    Pastry.forward(new Message("LARGE_LEAFSET_UPDATE", AppInstance.myNode.getAddress(), AppInstance.myNode.getState().getLeafSet().getLargeSet(), receivedMsg.getKey(), Calendar.getInstance().getTimeInMillis()), receivedMsg.getAddress());
                    AppInstance.myNode.getState().getLeafSet().semRelease();
                }
                else{
                    Pastry.forward(new Message("ACK_LEAF_LARGE_ANSWER", AppInstance.myNode.getAddress(), null, receivedMsg.getKey(), receivedMsg.getTimestamp()), receivedMsg.getAddress());
                }
                
            }
            /**
             * If the node that asked for our Leaf Set to correct his state departs, we have to replace it,
             * because if we are on its Leaf Set, then it is also on ours.
             */
            catch(ConnectException ce){
                
                NodeAddress na = receivedMsg.getAddress();
                String type = null;
                
                
                if(na.getNodeID().compareTo(AppInstance.myNode.getAddress().getNodeID()) < 0)
                    type = "small";
                else
                    type = "large";
                
                
                AppInstance.myNode.getState().getLeafSet().remove(na);
                    
                
                int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t-1);
                
                Thread fault_h = new Thread(new FaultHandler(type));
                fault_h.start();
            }
            
        }
        else if(receivedMsg.getMsg().equals("ACK_LEAF_SMALL_ANSWER") || receivedMsg.getMsg().equals("ACK_LEAF_LARGE_ANSWER")){
            AppInstance.myNode.getState().getLeafSet().semRelease();
        }
        
        
        
        
        /* LEAF MESSAGES */
        
        else if(receivedMsg.getMsg().equals("LEAFSET")){
            AppInstance.constructLeafSet(receivedMsg);
        }
        else if(receivedMsg.getMsg().equals("LARGE_LEAFSET") || receivedMsg.getMsg().equals("SMALL_LEAFSET")){
            FaultHandler.ReplaceFailedLeaf(receivedMsg); 
        }
        else if(receivedMsg.getMsg().equals("LARGE_LEAFSET_UPDATE") || receivedMsg.getMsg().equals("SMALL_LEAFSET_UPDATE")){
            String type = receivedMsg.getMsg().replaceAll("_LEAFSET_UPDATE", "");
            
            if(type.equals("SMALL")){
                NodeAddress na = Collections.min(AppInstance.myNode.getState().getLeafSet().getSmallSet()); 
                AppInstance.myNode.getState().getLeafSet().getSmallSet().remove(na);
                
                int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t-1);
            }
            else{
                NodeAddress na = Collections.max(AppInstance.myNode.getState().getLeafSet().getLargeSet()); 
                AppInstance.myNode.getState().getLeafSet().getLargeSet().remove(na);
                
                int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t-1);
            }
            
            
            FaultHandler.ReplaceFailedLeaf(receivedMsg); 
        }
        
        
        /* ASKS */
        
        else if(receivedMsg.getMsg().equals("ASK_LARGE_LEAFSET")){
            
            try{
                AppInstance.myNode.getState().getLeafSet().semAcquire();
                Pastry.forward(new Message("LARGE_LEAFSET", AppInstance.myNode.getAddress(), AppInstance.myNode.getState().getLeafSet().getLargeSet(), receivedMsg.getKey(), Calendar.getInstance().getTimeInMillis()), receivedMsg.getAddress());
                AppInstance.myNode.getState().getLeafSet().semRelease();
            }
            /**
             * If the node that asked for our Leaf Set to correct his state departs, we have to replace it,
             * because if we are on its Leaf Set, then it is also on ours.
             */
            catch(ConnectException ce){
                
                NodeAddress na = receivedMsg.getAddress();
                String type = null;
                
                
                if(na.getNodeID().compareTo(AppInstance.myNode.getAddress().getNodeID()) < 0)
                    type = "small";
                else
                    type = "large";
                
                
                AppInstance.myNode.getState().getLeafSet().remove(na);
                    
                
                int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t-1);
                
                Thread fault_h = new Thread(new FaultHandler(type));
                fault_h.start();
            }
            
        }
        else if(receivedMsg.getMsg().equals("ASK_SMALL_LEAFSET")){
            
            try{
                AppInstance.myNode.getState().getLeafSet().semAcquire();
                Pastry.forward(new Message("SMALL_LEAFSET", AppInstance.myNode.getAddress(), AppInstance.myNode.getState().getLeafSet().getSmallSet(), receivedMsg.getKey(), Calendar.getInstance().getTimeInMillis()), receivedMsg.getAddress());
                AppInstance.myNode.getState().getLeafSet().semRelease();
            }
            /**
             * If the node that asked for our Leaf Set to correct his state departs, we have to replace it,
             * because if we are on its Leaf Set, then it is also on ours.
             */
            catch(ConnectException ce){
                
                NodeAddress na = receivedMsg.getAddress();
                String type = null;
                
                
                if(na.getNodeID().compareTo(AppInstance.myNode.getAddress().getNodeID()) < 0)
                    type = "small";
                else
                    type = "large";
                
                
                AppInstance.myNode.getState().getLeafSet().remove(na);
                    
                
                int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t-1);
                
                Thread fault_h = new Thread(new FaultHandler(type));
                fault_h.start();
            }
            
        }
        else if(receivedMsg.getMsg().startsWith("ASK_ROUTE_ENTRY_")){
            
            String x_y = receivedMsg.getMsg().replaceAll("ASK_ROUTE_ENTRY_", "");
            String x = x_y.substring(0, x_y.indexOf("_"));
            String y = x_y.substring(x_y.indexOf("_")+1);
            int x_int = Integer.parseInt(x);
            int y_int = Integer.parseInt(y);
            
            
            /* Locking the Routing Table. */
            AppInstance.myNode.getState().getRoutingTable().semAcquire();
            try{
                Pastry.forward(new Message("ROUTE_ENTRY_"+x+"_"+y, AppInstance.myNode.getAddress(), AppInstance.myNode.getState().getRoutingTable().getEntry(x_int, y_int), receivedMsg.getKey(), 0), receivedMsg.getAddress());
            }
            catch(ConnectException ce){
                //ce.printStackTrace();
                /* Do nothing. */
            }
            /* Release the lock upon the Routing Table. */
            AppInstance.myNode.getState().getRoutingTable().semRelease();
        }
        
        
        
        
        
        /* Message sent when a node departs with warning. */
        else if(receivedMsg.getMsg().equals("DEPARTURE")){
            
            AppInstance.myNode.getState().getLeafSet().semAcquire();
            NodeAddress dep = receivedMsg.getAddress();
            
            if(AppInstance.myNode.getState().getLeafSet().contains(dep)){
            
                System.err.println(dep.getNodeID()+" has departed !");
                
                String type = null;
            
                if(dep.getNodeID().compareTo(AppInstance.myNode.getAddress().getNodeID()) < 0)
                    type = "small";
                else
                    type = "large";
                
                
                AppInstance.myNode.getState().getLeafSet().remove(dep);
                    
                
                int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t-1);
            
            
                Thread fault_h = new Thread(new FaultHandler(type));
                fault_h.start();
            }
            else
                AppInstance.myNode.getState().getLeafSet().semRelease();
            
        }
        
        
        /* APPLICATION CASES */
        
        else if(receivedMsg.getMsg().equals("SERVE_OBJECT")){
            Pastry.route(receivedMsg);
        }
        else if(receivedMsg.getMsg().equals("ACK_SERVE_OBJECT")){
            AppUtilities.updateFileState(receivedMsg);
        }
        else if(receivedMsg.getMsg().equals("FIND_OBJECT")){
            Pastry.route(receivedMsg);
        }
        else if(receivedMsg.getMsg().equals("REFERENCE_OBJECT")){
            AppUtilities.referenceObject(receivedMsg);
        }
        else if(receivedMsg.getMsg().equals("OBJECT_LOCATION")){
            AppUtilities.notifyDownloader(receivedMsg);
        }

        /* Unknown type. */
        else{
            System.err.println(receivedMsg.getMsg());
        }


    }
}
