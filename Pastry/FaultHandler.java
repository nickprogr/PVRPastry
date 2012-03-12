package Pastry;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class FaultHandler implements Runnable {
    
    private String type;    /* Shows in which table occured a fault. */
    private int row,col;    /* Used in the case where the fault have been found in the (row,col) position
                               of the Routing Table. */
    
    public FaultHandler(String type){
        this.type = type;
        this.row = -1;
        this.col = -1;
    }
    
    public FaultHandler(String type, int row, int col){
        this.type = type;
        this.row = row;
        this.col = col;
    }
    
    
    /* Method that sends messages requesting for replacing nodes for this node's state. */
    public void run(){
        
        if(this.type.equals("small")){
            
            /* Asking the Small LeafSet from the node with the Min nodeID in node's LeafSet. */
            try{
                Pastry.forward(new Message("ASK_SMALL_LEAFSET", AppInstance.myNode.getAddress(), null, null, 0), AppInstance.myNode.getState().getLeafSet().getMin());
            }
            /* If getMin() causes a NullPointerException, there are no nodes in Leaf Set, no further action is needed. */
            catch(NullPointerException npe){
                AppInstance.myNode.getState().getLeafSet().semRelease();
            }
            catch(ConnectException ce){
                
                NodeAddress na = AppInstance.myNode.getState().getLeafSet().getMin();
                String min_type = null;
                
                
                if(na.getNodeID().compareTo(AppInstance.myNode.getAddress().getNodeID()) < 0)
                    min_type = "small";
                else
                    min_type = "large";
                
                
                AppInstance.myNode.getState().getLeafSet().remove(na);
                    
                
                int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t-1);
                
                Thread fault_h = new Thread(new FaultHandler(min_type));
                fault_h.start();
                
                
                /* TODO:   new FaultHandler(this.type)*/
                /*   Not forget the original fault.   */
                    
            }
            
        }
        else if(this.type.equals("large")){
            
            /* Asking the Large LeafSet from the node with the Max nodeID in node's LeafSet. */
            try{
                Pastry.forward(new Message("ASK_LARGE_LEAFSET", AppInstance.myNode.getAddress(), null, null, 0), AppInstance.myNode.getState().getLeafSet().getMax());
            }
            /* If getMin() causes a NullPointerException, there are no nodes in Leaf Set, no further action is needed. */
            catch(NullPointerException npe){
                AppInstance.myNode.getState().getLeafSet().semRelease();
            }
            catch(ConnectException ce){
                
                NodeAddress na = AppInstance.myNode.getState().getLeafSet().getMax();
                String max_type = null;
                
                
                if(na.getNodeID().compareTo(AppInstance.myNode.getAddress().getNodeID()) < 0)
                    max_type = "small";
                else
                    max_type = "large";
                
                
                AppInstance.myNode.getState().getLeafSet().remove(na);
                    
                
                int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t-1);
                
                Thread fault_h = new Thread(new FaultHandler(max_type));
                fault_h.start();
                
                
                /* TODO:   new FaultHandler(this.type)*/
                /*   Not forget the original fault.   */
            }        
            
        }
        else{
            //...//
            System.err.println("ROUTE ENTRY FAULT");
            
            /* Locking the Routing Table during the correcting. */
            AppInstance.myNode.getState().getRoutingTable().semAcquire();
            
            /**
             * We send a request to every node in the same row of the Routing Table as the fault's row (except
             * for the failed column) until a valid node is returned. If no node of the same row returns us a valid
             * substitute, we continue with the larger index rows.
             */
            boolean brk = false;
            for(int i=this.row;i<AppInstance.myNode.getState().getRoutingTable().getTable().size();i++){
                for(int j=0;j<16;j++){
                    if(j != this.col){
                        if(AppInstance.myNode.getState().getRoutingTable().getTableRow(i).getRowIndex(j) != null){
                            try{
                                /**
                                 * The message label will include the coordinates of the failed node. The message 
                                 * key will include the coordinates of the receiver, in our Routing Table. If the 
                                 * answer from this node is not satisfying we will continue with the next node in 
                                 * our Routing Table.
                                 */
                                Pastry.forward(new Message("ASK_ROUTE_ENTRY_"+this.row+"_"+this.col, AppInstance.myNode.getAddress(), null, i+"_"+j, 0), AppInstance.myNode.getState().getRoutingTable().getTableRow(i).getRowIndex(j));
                                brk = true;
                                break;
                            }
                            catch(ConnectException ce){
                                
                                /* Erase the failed node from the Routing Table. */
                                AppInstance.myNode.getState().getRoutingTable().getTableRow(i).setRowIndex(null, j);
                                AppInstance.myNode.getState().getRoutingTable().getTableRow(i).numOfEntriesMinusMinus();
                                AppInstance.myNode.getState().getRoutingTable().numOfEntriesMinusMinus();
                                
                                Thread fault_h = new Thread(new FaultHandler("route", i, j));
                                fault_h.start();
                            }
                        }
                    }
                }
                if(brk) break;
            }
            
            /* Release the lock upon the Routing Table. */
            AppInstance.myNode.getState().getRoutingTable().semRelease();
            
        }
    }
    
    
    
    /* Method that handles a Small/Large Leaf Set, sent to us to find a substitute for a Leaf Set failed node. */
    public static void ReplaceFailedLeaf(Message rm){
        
        ArrayList<NodeAddress> temp = (ArrayList<NodeAddress>) rm.getObject();
        String type = rm.getMsg().replaceAll("_LEAFSET", "");
        NodeAddress replacement = null;
        Socket testSock = null;
        InetSocketAddress remoteIP = null;
        
        /* For every node in the set. */
        while(true){
                
            try{
                
                if(type.equals("SMALL"))
                    replacement = Collections.max(temp); 
                else
                    replacement = Collections.min(temp); 
            
                
                
                if(replacement != null){
                    
                    if(!AppInstance.myNode.getState().getLeafSet().contains(replacement)) {
                        
                        if(!AppInstance.myNode.getAddress().getNodeID().equals(replacement.getNodeID())){
                        
                            /* Checking if the candidate node is still alive. */
                    
                            remoteIP = new InetSocketAddress(replacement.getIp(), replacement.getSocketPortNumber());
                            testSock = new Socket();
                            testSock.connect(remoteIP, 2500);
                            testSock.setSoTimeout(2500); 
                    
                            testSock.close();
                        }
                    }
                }
            }
            catch(Exception e){
                
                /* If the candidate node is down, remove it, and take the next candidate. */
                if(e.getClass().getName().equals("java.net.ConnectException")){
                    temp.remove(replacement);
                    continue;
                }
                /* If the set has no more nodes. */
                else if(e.getClass().getName().equals("java.util.NoSuchElementException")){
                    replacement = null;
                }
                else
                    e.printStackTrace();
            }
            
            
            if(replacement != null){
            
                if(!AppInstance.myNode.getState().getLeafSet().contains(replacement)) {
                    
                    if(!AppInstance.myNode.getAddress().getNodeID().equals(replacement.getNodeID())){
                
                        /* Adding new node.*/
                
                        if(type.equals("SMALL")){
                            AppInstance.myNode.getState().getLeafSet().getSmallSet().add(replacement);
                        
                            int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                            AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t+1);
                        
                            AppInstance.myNode.getState().getLeafSet().sortSmaller();
                        }
                        else{
                            AppInstance.myNode.getState().getLeafSet().getLargeSet().add(replacement);
                        
                            int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                            AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t+1);
                        
                            AppInstance.myNode.getState().getLeafSet().sortLarger();
                        }
            
                        AppInstance.myNode.getState().setTimeLastUpdated(Calendar.getInstance().getTimeInMillis());
                        
                    }
                    
                }
                
            }
                
            break;
                
                
                
        }
        
        /* Sends the original timestamp back to the sender. */
        try{
            if(type.equals("SMALL"))
                Pastry.forward(new Message("ACK_LEAF_SMALL_QUESTION", AppInstance.myNode.getAddress(), null, rm.getKey(), rm.getTimestamp()), rm.getAddress());
            else
                Pastry.forward(new Message("ACK_LEAF_LARGE_QUESTION", AppInstance.myNode.getAddress(), null, rm.getKey(), rm.getTimestamp()), rm.getAddress());
        }
        /* The sender is in our Leaf Set. If it fails we have to substitute it. */
        catch(ConnectException ce){
            
            NodeAddress na = rm.getAddress();
            type = null;
                
                
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
    
    
    
    
    /* Method tha handles a NodeAddress sent to us as a substitute for a Routing Table failed enrty. */
    public static void RequestRouteEntry(Message rm){
        
        Socket testSock = null;
        InetSocketAddress remoteIP = null;
        NodeAddress replacement = (NodeAddress) rm.getObject();
        
        /* Extracting failed entry's coordinates. */
        String x_y = rm.getMsg().replaceAll("ROUTE_ENTRY_", "");
        String x = x_y.substring(0, x_y.indexOf("_"));
        String y = x_y.substring(x_y.indexOf("_")+1);
        int x_int = Integer.parseInt(x);
        int y_int = Integer.parseInt(y);
        
        
        
        try{
        
            if(replacement != null){
                if(!AppInstance.myNode.getAddress().getNodeID().equals(replacement.getNodeID())){
                
                    /* Checking if the candidate node is still alive. */
                    
                    remoteIP = new InetSocketAddress(replacement.getIp(), replacement.getSocketPortNumber());
                    testSock = new Socket();
                    testSock.connect(remoteIP, 2500);
                    testSock.setSoTimeout(2500); 
                    
                    testSock.close();
                
                }
            }
        }
        catch(Exception e){
            
            /* If the candidate node is down, continue the procedure of finding a suitable substitute. */
            if(e.getClass().getName().equals("java.net.ConnectException")){
                
                //...//
                System.err.println("**DOWN**");
                
                
                /* Exctract the coordinates of the entry whose node we have last contacted. */
                String temp = rm.getKey();
                String x2 = temp.substring(0, temp.indexOf("_"));
                String y2 = temp.substring(temp.indexOf("_")+1);
                int x2_int = Integer.parseInt(x2);
                int y2_int = Integer.parseInt(y2);
                
                
                /* Locking the Routing Table during the correcting. */
                AppInstance.myNode.getState().getRoutingTable().semAcquire();
        
        
                boolean brk = false;
                for(int i=x2_int;i<AppInstance.myNode.getState().getRoutingTable().getTable().size();i++){
                    for(int j=y2_int+1;j<16;j++){
                        if(j != y_int){
                            if(AppInstance.myNode.getState().getRoutingTable().getTableRow(i).getRowIndex(j) != null){
                                try{
                                    Pastry.forward(new Message("ASK_ROUTE_ENTRY_"+x+"_"+y, AppInstance.myNode.getAddress(), null, i+"_"+j, 0), AppInstance.myNode.getState().getRoutingTable().getTableRow(i).getRowIndex(j));
                                    brk = true;
                                    break;
                                }
                                catch(ConnectException ce){
                                    
                                    /* Erase the failed node from the Routing Table. */
                                    AppInstance.myNode.getState().getRoutingTable().getTableRow(i).setRowIndex(null, j);
                                    AppInstance.myNode.getState().getRoutingTable().getTableRow(i).numOfEntriesMinusMinus();
                                    AppInstance.myNode.getState().getRoutingTable().numOfEntriesMinusMinus();
                                    
                                    Thread fault_h = new Thread(new FaultHandler("route", i, j));
                                    fault_h.start();
                                }
                            }
                        }
                    }
                    y2_int = -1;
                    if(brk) break;
                }
                
                /* Release the lock upon the Routing Table. */
                AppInstance.myNode.getState().getRoutingTable().semRelease();
                
                return;
                
            }
            else
                e.printStackTrace();
            
        }
        
        
        if(replacement != null){
            if(!AppInstance.myNode.getAddress().getNodeID().equals(replacement.getNodeID())){
                
                /* Locking the Routing Table during the correcting. */
                AppInstance.myNode.getState().getRoutingTable().semAcquire();
                
                /* Adding new node.*/
                
                AppInstance.myNode.getState().getRoutingTable().getTableRow(x_int).setRowIndex(replacement, y_int);
                AppInstance.myNode.getState().getRoutingTable().getTableRow(x_int).numOfEntriesPlusPlus();
                AppInstance.myNode.getState().getRoutingTable().numOfEntriesPlusPlus();
                
                
                /* Release the lock upon the Routing Table. */
                AppInstance.myNode.getState().getRoutingTable().semRelease();
                
                //...//
                System.out.println(rm.getMsg()+":  "+replacement.getNodeID());
            }
        }
        else{
            //...//
            System.err.println("**NULL**");
            
            
            String temp = rm.getKey();
            String x2 = temp.substring(0, temp.indexOf("_"));
            String y2 = temp.substring(temp.indexOf("_")+1);
            int x2_int = Integer.parseInt(x2);
            int y2_int = Integer.parseInt(y2);
                
            /* Locking the Routing Table during the correcting. */
            AppInstance.myNode.getState().getRoutingTable().semAcquire();    
            
            
            boolean brk = false;
            for(int i=x2_int;i<AppInstance.myNode.getState().getRoutingTable().getTable().size();i++){
                for(int j=y2_int+1;j<16;j++){
                    if(j != y_int){
                        if(AppInstance.myNode.getState().getRoutingTable().getTableRow(i).getRowIndex(j) != null){
                            try{
                                Pastry.forward(new Message("ASK_ROUTE_ENTRY_"+x+"_"+y, AppInstance.myNode.getAddress(), null, i+"_"+j, 0), AppInstance.myNode.getState().getRoutingTable().getTableRow(i).getRowIndex(j));
                                brk = true;
                                break;
                            }
                            catch(ConnectException ce){
                                
                                /* Erase the failed node from the Routing Table. */
                                AppInstance.myNode.getState().getRoutingTable().getTableRow(i).setRowIndex(null, j);
                                AppInstance.myNode.getState().getRoutingTable().getTableRow(i).numOfEntriesMinusMinus();
                                AppInstance.myNode.getState().getRoutingTable().numOfEntriesMinusMinus();
                                
                                Thread fault_h = new Thread(new FaultHandler("route", i, j));
                                fault_h.start();
                            }
                        }
                    }
                }
                y2_int = -1;
                if(brk) break;
            }
            
            /* Release the lock upon the Routing Table. */
            AppInstance.myNode.getState().getRoutingTable().semRelease();
            
            return;
            
        }
        
        
        
    }
    
}
