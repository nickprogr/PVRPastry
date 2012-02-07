package Pastry;

/**
 *
 * @author nickprogr
 */


import java.math.BigInteger;
import java.util.Set;
import java.util.TreeSet;


public class Pastry {
    
    
    /**
     * Method that takes care of the routing procedures and all relevant operations.
     */
    public static void route(Message msg, Node n){
        
        
        /* If the message is Join Message, a part of our state is sent back to the new node. */
        if(msg.getMsg().startsWith("JOIN")){
            
            String temp_msg = msg.getMsg();
            
            /*if(temp_msg.equals("JOIN0")){
                
                //Pastry.forward(new Message("NEIGHSET", n.getAddress(), n.getState().getNeighborhoodSet(), msg.getKey()) , msg.getAddress());

                Pastry.forward(new Message("ROUTEROW", n.getAddress(), n.getState().getRoutingTable().getTableRow(0), "0") , msg.getAddress());
                
                
                
                int prfx = shl(n.getAddress().getNodeID(), msg.getKey());
                
                
                if(prfx != 0){
                    while(prfx > 0){
                        Pastry.forward(new Message("ROUTEROW", n.getAddress(), n.getState().getRoutingTable().getTableRow(prfx), ((Integer) prfx).toString()) , msg.getAddress());
                        prfx--;
                    }
                }
                    

                //msg.setMsg(new String("JOIN1"));      
                
            }
            else{
                
                temp_msg = temp_msg.replaceAll("JOIN", "");
                int prfx = Integer.parseInt(temp_msg);
                
                Pastry.forward(new Message("ROUTEROW", n.getAddress(), n.getState().getRoutingTable().getTableRow(prfx), ((Integer) prfx).toString()) , msg.getAddress());

                //prfx++;
                //msg.setMsg(new String("JOIN"+prfx)); 
                
 
            }*/
            
            
            
            int prfx = shl(n.getAddress().getNodeID(), msg.getKey());
                
                
            while(prfx >= 0){
                Pastry.forward(new Message("ROUTEROW", n.getAddress(), n.getState().getRoutingTable().getTableRow(prfx), ((Integer) prfx).toString()) , msg.getAddress());
                    prfx--;
            }
            
            
        }
        
        
        
        NodeAddress min_entry = n.getState().getLeafSet().getMin();
        NodeAddress max_entry = n.getState().getLeafSet().getMax();

        if(min_entry == null && max_entry == null){
            min_entry = new NodeAddress();
            min_entry.setNodeID("1");
            max_entry = new NodeAddress();
            max_entry.setNodeID("1");
        }
        
        
        
        
        /* If the key is between the range of our leaf set. */
        if(msg.getKey().compareTo(min_entry.getNodeID()) >= 0  && msg.getKey().compareTo(max_entry.getNodeID()) <= 0){


            /* Compute which is the closest nodeID to the message key. */
            BigInteger min = new BigInteger("fffffffffffffffffffffffffffffffffffff",16);
            String nid = null;

            Set<NodeAddress> temp = new TreeSet<NodeAddress>();

            
            
            temp.add(n.getAddress());
            for(NodeAddress na : n.getState().getLeafSet().getSmallSet())
                if(na != null)
                    temp.add(na);
            for(NodeAddress na : n.getState().getLeafSet().getLargeSet())
                if(na != null)
                    temp.add(na);
            


            for(NodeAddress na: temp){

                BigInteger dif,comp1,comp2;
                comp1 = new BigInteger(msg.getKey(),16);
                comp2 = new BigInteger(na.getNodeID(),16);
                dif = comp1.subtract(comp2);
                dif = dif.abs();

                if( dif.compareTo(min) < 0){
                    min = dif;
                    nid = na.getNodeID();
                }
            }
            
            
            
            /* If the closest node by nodeID is not the current one. */
            if(!nid.equals(n.getAddress().getNodeID())){
                
                if(msg.getMsg().startsWith("JOIN")){
                    int prfx = shl(n.getAddress().getNodeID(), msg.getKey());
                    
                    msg.setMsg(new String("JOIN"+prfx));
                }
                
                Pastry.forward(msg, n.getState().getLeafSet().getLeafByID(nid));
            }
            else{

                if(msg.getMsg().startsWith("JOIN")){

                    /*if(msg.getMsg().equals("JOIN0")){
                        
                        for(int i=1;i<n.getState().getRoutingTable().getTable().size();i++){
                            
                            //msg.setMsg(new String("JOIN"+i));

                            Pastry.forward(new Message("ROUTEROW", n.getAddress(), n.getState().getRoutingTable().getTableRow(i), ((Integer) i).toString()) , msg.getAddress());
                        }

                    }*/

                    Pastry.forward(new Message("LEAFSET", n.getAddress(), n.getState().getLeafSet(), msg.getKey()) , msg.getAddress());

                }
                
                /* MAYBE OTHER CASES */

            }
               
        }
        
        
        /* If the key is not between the range of our leaf set, use the routing table. */
        else{

            /* Compute common prefix. */
            int len = shl(n.getAddress().getNodeID(), msg.getKey());

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

            
            
            
            /* If the appropriate entry of the routing table is not null */
            if(n.getState().getRoutingTable().getEntry(len,col) != null){
                
                if(msg.getMsg().startsWith("JOIN")){
                    int len2 = shl(n.getState().getRoutingTable().getEntry(len,col).getNodeID(), msg.getKey());
                    
                    msg.setMsg(new String("JOIN"+len2));
                }
                    
                Pastry.forward(msg, n.getState().getRoutingTable().getEntry(len,col));
            }
            
            
            
            
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

                BigInteger dif,comp1,comp2;
                comp1 = new BigInteger(n.getAddress().getNodeID(),16);
                comp2 = new BigInteger(msg.getKey(),16);
                dif = comp1.subtract(comp2);
                
                BigInteger this_node_distance = dif.abs();
                
                
                
                
                /* LeafSet Nodes */
                
                /* small set */
                for(NodeAddress na: n.getState().getLeafSet().getSmallSet()){
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
                }
                
                /* large set */
                for(NodeAddress na: n.getState().getLeafSet().getLargeSet()){
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
                }
                
                
                
                
                
                /* RoutingTable Nodes*/
                
                for(RoutingTableRow rt_row: n.getState().getRoutingTable().getTable()){
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
                    }
                    
                    Pastry.forward(msg, min_node);
                }
                
                /**
                 * If min_node is null, then the node with the closest nodeID to the key
                 * is the current node.
                 */
                else{
                    
                    if(msg.getMsg().startsWith("JOIN"))
                        Pastry.forward(new Message("LEAFSET", n.getAddress(), n.getState().getLeafSet(), msg.getKey()) , msg.getAddress());
                    
                }
                 
            }
             
        }    
        
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
    
    
    
    
    
    public static void forward(Message msg, NodeAddress addr){
        
        MessageDelivery.sendMessage(msg, addr);
        
    }
    
}
