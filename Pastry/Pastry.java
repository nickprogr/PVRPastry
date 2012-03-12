package Pastry;

import Application.AppUtilities;
import java.math.BigInteger;
import java.net.ConnectException;
import java.util.Calendar;


public class Pastry {
    
    
    /**
     * Method that takes care of the routing procedures and all relevant operations.
     */
    public static void route(Message msg){
        
        /* Locking the LeafSet and the Routing Table during the routing. */
        AppInstance.myNode.getState().getLeafSet().semAcquire();
        AppInstance.myNode.getState().getRoutingTable().semAcquire();
        
        
        /* If the message is Join Message, a part of our state is sent back to the new node. */
        if(msg.getMsg().startsWith("JOIN")){
            
            String temp_msg = msg.getMsg();
            
            if(temp_msg.equals("JOIN0")){
                
                //Pastry.forward(new Message("NEIGHSET", n.getAddress(), n.getState().getNeighborhoodSet(), msg.getKey()) , msg.getAddress());
                
                try{
                    Pastry.forward(new Message("ROUTEROW", AppInstance.myNode.getAddress(), AppInstance.myNode.getState().getRoutingTable().getTableRow(0), "0", 0) , msg.getAddress());
                }
                catch(ConnectException ce){/* DO NOTHING */}
            }    
                
            int prfx = shl(AppInstance.myNode.getAddress().getNodeID(), msg.getKey());
                
                
            if(prfx != 0){
                try{
                    Pastry.forward(new Message("ROUTEROW", AppInstance.myNode.getAddress(), AppInstance.myNode.getState().getRoutingTable().getTableRow(prfx), ((Integer) prfx).toString(), 0) , msg.getAddress());
                }
                catch(ConnectException ce){/* DO NOTHING */}
            }
              
        }
        
        
        
        NodeAddress min_entry = null;
        NodeAddress max_entry = null;
        
        try{
            min_entry = AppInstance.myNode.getState().getLeafSet().getMin();
            max_entry = AppInstance.myNode.getState().getLeafSet().getMax();
        }
        catch(NullPointerException npe){
            min_entry = new NodeAddress();
            min_entry.setNodeID("00000000000000000000000000000000");
            max_entry = new NodeAddress();
            max_entry.setNodeID("00000000000000000000000000000000");
        }
        
        
        
        
        /* If the key is between the range of our leaf set. */
        if(msg.getKey().compareTo(min_entry.getNodeID()) >= 0  && msg.getKey().compareTo(max_entry.getNodeID()) <= 0){


            /* Compute which is the closest nodeID to the message key. */
            BigInteger min = new BigInteger("fffffffffffffffffffffffffffffffffffff",16);
            String min_type = null;
            String nid = null;

            
            
            min = new BigInteger(msg.getKey(),16).subtract(new BigInteger(AppInstance.myNode.getAddress().getNodeID(),16));
            min = min.abs();
            nid = AppInstance.myNode.getAddress().getNodeID();
            
            /* small leafset */
            for(NodeAddress na: AppInstance.myNode.getState().getLeafSet().getSmallSet()){

                BigInteger dif,comp1,comp2;
                comp1 = new BigInteger(msg.getKey(),16);
                comp2 = new BigInteger(na.getNodeID(),16);
                dif = comp1.subtract(comp2);
                dif = dif.abs();

                if( dif.compareTo(min) < 0){
                    min = dif;
                    min_type = "small";
                    nid = na.getNodeID();
                }
            }
            
            /* large leafset */
            for(NodeAddress na: AppInstance.myNode.getState().getLeafSet().getLargeSet()){

                BigInteger dif,comp1,comp2;
                comp1 = new BigInteger(msg.getKey(),16);
                comp2 = new BigInteger(na.getNodeID(),16);
                dif = comp1.subtract(comp2);
                dif = dif.abs();

                if( dif.compareTo(min) < 0){
                    min = dif;
                    min_type = "large";
                    nid = na.getNodeID();
                }
            }
            
            
            
            /* If the closest node by nodeID is not the current one. */
            if(!nid.equals(AppInstance.myNode.getAddress().getNodeID())){
                
                if(msg.getMsg().startsWith("JOIN")){
                    int prfx = shl(nid, msg.getKey());
                    
                    msg.setMsg(new String("JOIN"+prfx));
                    
                    //msg.setAddress(AppInstance.myNode.getAddress());
                }
                
                try{
                    NodeAddress na = AppInstance.myNode.getState().getLeafSet().getLeafByID(nid);
                    Pastry.forward(msg, na);
                }
                /**
                 * FAULT TOLERANCE (Leaf Set Fault)
                 * 
                 * If we try to send a message to a failed leaf node, we remove it from our
                 * Leaf Set. then we spawn a new thread which will find a substitute for the failed node,
                 * and after the replacement has been done successfully, the whole routing procedure is going 
                 * to be repeated with the corrected Leaf Set.
                 */
                catch(ConnectException ce){
                    NodeAddress na = AppInstance.myNode.getState().getLeafSet().getLeafByID(nid);
                    
                    
                    if(min_type.equals("small"))
                        AppInstance.myNode.getState().getLeafSet().getSmallSet().remove(na);
                    else
                        AppInstance.myNode.getState().getLeafSet().getLargeSet().remove(na);
                    
                    int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                    AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t-1);
                    
                    
                    AppInstance.myNode.getState().getRoutingTable().semRelease();
                    
                    
                    Thread fault_h = new Thread(new FaultHandler(min_type));
                    fault_h.start();
                    
                    Pastry.route(msg);
                    return;
                    
                }
            }
            else{

                if(msg.getMsg().startsWith("JOIN")){

                    try{
                        Pastry.forward(new Message("LEAFSET", AppInstance.myNode.getAddress(), AppInstance.myNode.getState().getLeafSet(), msg.getKey(), Calendar.getInstance().getTimeInMillis()) , msg.getAddress());
                    }
                    catch(ConnectException ce){/* DO NOTHING */}

                }
                else if(msg.getMsg().startsWith("SERVE_OBJECT")) {
                    AppUtilities.serveObject(msg);
                } else if (msg.getMsg().startsWith("FIND_OBJECT")) {
                    AppUtilities.checkCache(msg);
                }

            }
               
        }
        
        
        /* If the key is not between the range of our leaf set, use the routing table. */
        else{

            /* Compute common prefix. */
            int len = shl(AppInstance.myNode.getAddress().getNodeID(), msg.getKey());

            String s = msg.getKey().substring(len, len+1);
            int col= -1;

            /* Compute the column from which we are going to choose the next destination node. */
            if(s.equals("a"))
                col = 10;
            else if(s.equals("b"))
                col = 11;
            else if(s.equals("c"))
                col = 12;
            else if(s.equals("d"))
                col = 13;
            else if(s.equals("e"))
                col = 14;
            else if(s.equals("f"))
                col = 15;
            else
                col = Integer.parseInt(s);  /* normal cases */

            
            
            boolean failed = false;
            
            /* If the appropriate entry of the routing table is not null */
            if(AppInstance.myNode.getState().getRoutingTable().getEntry(len,col) != null){
                
                
                BigInteger dif1,dif2,comp1,comp2,comp3;
                comp1 = new BigInteger(msg.getKey(),16);
                comp2 = new BigInteger(AppInstance.myNode.getState().getRoutingTable().getEntry(len,col).getNodeID(),16);
                comp3 = new BigInteger(AppInstance.myNode.getAddress().getNodeID(), 16);
                dif1 = comp1.subtract(comp2);
                dif1 = dif1.abs();
                dif2 = comp1.subtract(comp3);
                dif2 = dif2.abs();
                
                
                
                /* If the node in the entry is closer to the key than the current node. */
                if(dif1.compareTo(dif2) < 0 ){
                
                
                    if(msg.getMsg().startsWith("JOIN")){
                        int len2 = shl(AppInstance.myNode.getState().getRoutingTable().getEntry(len,col).getNodeID(), msg.getKey());
                    
                        msg.setMsg(new String("JOIN"+len2));
                        
                        //msg.setAddress(AppInstance.myNode.getAddress());
                    }
                
                    try{
                        Pastry.forward(msg, AppInstance.myNode.getState().getRoutingTable().getEntry(len,col));
                    }
                    /**
                     * FAULT TOLERANCE (Routing Table Fault)
                     * 
                     * If we try to send a message to a failed routing table node, we erase it from the table and
                     * then spawn a new thread which is lazily going to find a replacement for the failed node.
                     * The routing procedure will continue.
                     */
                    catch(ConnectException ce){
                        
                        failed = true;
                        
                        /* Erase the failed node from the Routing Table. */
                        AppInstance.myNode.getState().getRoutingTable().getTableRow(len).setRowIndex(null, col);
                        AppInstance.myNode.getState().getRoutingTable().getTableRow(len).numOfEntriesMinusMinus();
                        AppInstance.myNode.getState().getRoutingTable().numOfEntriesMinusMinus();
                    
                        Thread fault_h = new Thread(new FaultHandler("route", len, col));
                        fault_h.start();
                    }
                    
                }
                else{
                    failed = true;
                }
                
                
            }
            else
                failed = true;
            
            
            
            
            /**
             * For the cases that the message was successfully forwarded, or the case that the routing table entry
             * was null.
             */
            if(!failed){/* DO NOTHING */}
            /** 
             * Else forward to a node that shares a prefix with the key at least as
             * long as the local node. 
             */
            else {
                
                /**
                 * Find the distance between the key and the current node's nodeID, and compare it
                 * with the distance between the key and every node in current node's Leaf Set,
                 * Routing Table (and Neighborhood Set). Keep the minimum distance.
                 */
                
                
                BigInteger min = new BigInteger("fffffffffffffffffffffffffffffffffffff",16);
                NodeAddress min_node = null;
                String min_type = null;
                int min_len = -1;

                BigInteger dif,comp1,comp2;
                comp1 = new BigInteger(AppInstance.myNode.getAddress().getNodeID(),16);
                comp2 = new BigInteger(msg.getKey(),16);
                dif = comp1.subtract(comp2);
                
                BigInteger this_node_distance = dif.abs();
                
                
                
                
                /* LeafSet Nodes */
                
                /* small set */
                for(NodeAddress na: AppInstance.myNode.getState().getLeafSet().getSmallSet()){
                    if(na != null){
                        comp1 = new BigInteger(na.getNodeID(),16);
                        comp2 = new BigInteger(msg.getKey(),16);
                        dif = comp1.subtract(comp2);
                        dif = dif.abs();

                    
                        if(shl(na.getNodeID(), msg.getKey()) >= len && dif.compareTo(this_node_distance) < 0){

                            if(dif.compareTo(min) < 0) {
                                min = dif;
                                min_node = na;
                                min_type = "small";
                            }
                        }
                    }
                }
                
                /* large set */
                for(NodeAddress na: AppInstance.myNode.getState().getLeafSet().getLargeSet()){
                    if(na != null){
                        comp1 = new BigInteger(na.getNodeID(),16);
                        comp2 = new BigInteger(msg.getKey(),16);
                        dif = comp1.subtract(comp2);
                        dif = dif.abs();

                    
                        if(shl(na.getNodeID(), msg.getKey()) >= len && dif.compareTo(this_node_distance) < 0){

                            if(dif.compareTo(min) < 0) {
                                min = dif;
                                min_node = na;
                                min_type = "large";
                            }
                        }
                    }
                }
                
                
                
                
                
                /* RoutingTable Nodes*/
                
                for(RoutingTableRow rt_row: AppInstance.myNode.getState().getRoutingTable().getTable()){
                    for(NodeAddress na : rt_row.getRow()){
                        if(na != null){
                            comp1 = new BigInteger(na.getNodeID(),16);
                            comp2 = new BigInteger(msg.getKey(),16);
                            dif = comp1.subtract(comp2);
                            dif = dif.abs();



                            if(shl(na.getNodeID(), msg.getKey()) >= len && dif.compareTo(this_node_distance) < 0){

                                if(dif.compareTo(min) < 0) {
                                    min = dif;
                                    min_node = na;
                                    min_type = "route";
                                    min_len = shl(na.getNodeID(), msg.getKey());
                                }
                            }
                        }
                    }
                }
                
                
                
                
                /* NeighborhoodSet Nodes (currently not implemented) */
                
                /*for(NodeAddress na: n.getState().getNeighborhoodSet().getNeighborhoodSet()){
                    if(na != null){
                        comp1 = new BigInteger(na.getNodeID(),16);
                        comp2 = new BigInteger(msg.getKey(),16);
                        dif = comp1.subtract(comp2);
                        dif = dif.abs();


                        if(shl(na.getNodeID(), msg.getKey()) >= len && dif.compareTo(this_node_distance) < 0){

                            if(dif.compareTo(min) < 0) {
                                min = dif;
                                min_node = na;
                            }
                        }
                    }
                }*/
                
                
                
                if(min_node != null){
                    
                    if(msg.getMsg().startsWith("JOIN")){
                        int prfx = shl(min_node.getNodeID(), msg.getKey());
                    
                        msg.setMsg(new String("JOIN"+prfx));
                        
                        //msg.setAddress(AppInstance.myNode.getAddress());
                    }
                    
                    try{
                        Pastry.forward(msg, min_node);
                    }
                    catch(ConnectException ce){
                        /**
                         * FAULT TOLERANCE (Leaf Set Fault)
                         * 
                         * If we try to send a message to a failed lead node, we remove it from our
                         * Leaf Set. then we spawn a new thread which will find a substitute for the failed node,
                         * and after the replacement has been done successfully, the whole routing procedure is going 
                         * to be repeated with the corrected Leaf Set.
                         */
                        if(min_type.equals("small") || min_type.equals("large")){
                            
                            AppInstance.myNode.getState().getLeafSet().remove(min_node);
                            
                            int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                            AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t-1);
                            
                            
                            AppInstance.myNode.getState().getRoutingTable().semRelease();
                            
                            Thread fault_h = new Thread(new FaultHandler(min_type));
                            fault_h.start();
                            
                            
                    
                            Pastry.route(msg);
                            return;
                        }
                        /**
                         * FAULT TOLERANCE (Routing Table Fault)
                         * 
                         * If we try to send a message to a failed routing table node, we erase it from the table and
                         * then spawn a new thread which is lazily going to find a replacement for the failed node.
                         */
                        else{
                            
                            String str = min_node.getNodeID().substring(min_len, min_len+1);
                            int min_col= -1;

                            /* Compute the column from which we are going to choose the next destination node. */
                            if(str.equals("a"))
                                min_col = 10;
                            else if(str.equals("b"))
                                min_col = 11;
                            else if(str.equals("c"))
                                min_col = 12;
                            else if(str.equals("d"))
                                min_col = 13;
                            else if(str.equals("e"))
                                min_col = 14;
                            else if(str.equals("f"))
                                min_col = 15;
                            else
                                min_col = Integer.parseInt(str);  /* normal cases */
                            
                            /* Erase the failed node from the Routing Table. */
                            AppInstance.myNode.getState().getRoutingTable().getTableRow(min_len).setRowIndex(null, min_col);
                            AppInstance.myNode.getState().getRoutingTable().getTableRow(min_len).numOfEntriesMinusMinus();
                            AppInstance.myNode.getState().getRoutingTable().numOfEntriesMinusMinus();
                            
                            
                            /* Release the locks upon the LeafSet and the Routing Table. */
                            AppInstance.myNode.getState().getLeafSet().semRelease();
                            AppInstance.myNode.getState().getRoutingTable().semRelease();
                            
                            
                            Thread fault_h = new Thread(new FaultHandler("route", min_len, min_col));
                            fault_h.start();
                            
                            /**
                             * In this case, the failed node was the closest to the message key. So we have to
                             * repeat the routing procedure with a correct state.
                             */
                            
                            Pastry.route(msg);
                            return;
                        }
                    }
                }
                
                /**
                 * If min_node is null, then the node with the closest nodeID to the key
                 * is the current node.
                 */
                else{
                    
                    if(msg.getMsg().startsWith("JOIN")){
                        try{
                            Pastry.forward(new Message("LEAFSET", AppInstance.myNode.getAddress(), AppInstance.myNode.getState().getLeafSet(), msg.getKey(), Calendar.getInstance().getTimeInMillis()) , msg.getAddress());
                        }
                        catch(ConnectException ce){/* DO NOTHING */}
                    }
                    if( msg.getMsg().startsWith("SERVE_OBJECT") )
                        AppUtilities.serveObject(msg);

                    if( msg.getMsg().startsWith("FIND_OBJECT") )
                        AppUtilities.checkCache(msg);
                    
                }
                 
            }
             
        }
        
        /* Release the locks upon the LeafSet and the Routing Table. */
        AppInstance.myNode.getState().getLeafSet().semRelease();
        AppInstance.myNode.getState().getRoutingTable().semRelease();
        
    }
    
    
    
    
    
    
    
    /** 
     * Computes the number of common prefix digits.
     */
    public static int shl(String a, String b){
        int shared = 0;

        for(int i = 0 ; i < a.length() ; i++){
            if(a.charAt(i) == b.charAt(i))
                shared++;
            else
                break;
        }

        return shared;
    }
    
    
    
    
    /* Forwards a message to the next destination node. */
    public static void forward(Message msg, NodeAddress addr) throws ConnectException{
        
        MessageDelivery.sendMessage(msg, addr);

    }
    
}
