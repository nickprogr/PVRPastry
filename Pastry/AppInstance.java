package Pastry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * @author nickprogr
 */

public class AppInstance {
    
    public static Node myNode;
    public static boolean stateBuilt;
    
    
    public static void launchApp() throws InterruptedException {
        
        myNode = new Node();
        stateBuilt = false;
        
        
        //MessageServer//
        Thread thr1 = new Thread(new MessageServer(myNode.getAddress()));
        thr1.start();
        
        
        
        //bootstrap//
        NodeAddress bootstrapAddress = MulticastRequest.makeRequest();
        
        
        
        
        //if this is the first node in the ring.
        if(bootstrapAddress == null){
          
            /* RESEND */
            /* CREATE MY STATE */
            
            
            myNode.getState().getRoutingTable().setTableRow(0, new RoutingTableRow());
            
        }
        else{
                
            //createJoinMessage//
            final Message joinMsg = new Message("JOIN0", myNode.getAddress(), null, myNode.getAddress().getNodeID(), 0);
        
            Pastry.forward(joinMsg, bootstrapAddress);
            
            
            
            final NodeAddress bootAddr = bootstrapAddress;
            
            Thread timeOutThread = new Thread(
                
                new Runnable() {
                    public void run() { 
                        
                        while(true){
                        
                            try{
                                Thread.sleep(5000);
                            }
                            catch(InterruptedException ie){
                                ie.printStackTrace();
                            }
                        
                            if(!stateBuilt){
                                Pastry.forward(joinMsg, bootAddr);
                                
                                /* 3 CONSECUTIVE FAILS = NEW MULTICAST */
                            }
                            else
                                break;
                        }
                    }
                }      
            );
            
            
            timeOutThread.start();
        }
        
        
        
        
        
        
        
        
        
        
        //...//
        //Message m = new Message("TEST", myNode.getAddress(), new RoutingTableRow());
        //MessageDelivery.sendMessage(m, bootstrapAddress);
        
        //MulticastServer//
        Thread thr2 = new Thread(new MulticastServer(myNode.getAddress()));
        thr2.start();
        
    }
    
    
    
    
    /**
     * Method that takes as a parameter the LeafSet from the node with the closest
     * nodeID to the current node's nodeID, and constructs the LeafSet of the new node.
     */
    public static void constructLeafSet(Message msg){
        
        
        LeafSet temp = (LeafSet) msg.getObject();

        NodeAddress[] temp_array = new NodeAddress[temp.getNumOfLeaves() + 1];

        int index = -1;

        for(byte i=0;i< temp.getSmallSet().size();i++){
            if(temp.getLeafSmall(i) != null){
                index++;
                temp_array[index] = temp.getLeafSmall(i);
            }
        }
        for(byte i=0;i< temp.getLargeSet().size();i++){
            if(temp.getLeafLarge(i) != null){
                index++;
                temp_array[index] = temp.getLeafLarge(i);
            }
        }
        index++;
        temp_array[index] = msg.getAddress();




       
        
        
        ArrayList<NodeAddress> temp_small = new ArrayList<NodeAddress>(16),temp_large = new ArrayList<NodeAddress>(16);
        
        
        
        int counter_small = 0, counter_large = 0;
        
        for(NodeAddress na : temp_array){
            if(na.getNodeID().compareTo( myNode.getAddress().getNodeID()) > 0){
                if(counter_large < 16){    
                    temp_large.add(na);
                    counter_large++;
                }    
                else{
                    Collections.sort(temp_large);
                            
                    if(na.getNodeID().compareTo(temp_large.get(temp_large.size()-1).getNodeID()) < 0){
                        temp_large.set(temp_large.size()-1, na);
                    }  
                }
            }
            else{
                if(counter_small < 16){
                    temp_small.add(na);
                    counter_small++;
                }
                else{
                    Collections.sort(temp_small);
                        
                    if(na.getNodeID().compareTo(temp_small.get(0).getNodeID()) > 0){
                        temp_small.set(0, na);
                    }
                }
            }
        }
        
        
        
        Collections.sort(temp_small);
        Collections.sort(temp_large);
        
        
        
        LeafSet ls = new LeafSet();
        ls.setSmallSet(temp_small);
        ls.setLargeSet(temp_large);
        ls.setNumOfLeaves(ls.getSmallSet().size() + ls.getLargeSet().size());
        
        
        
        myNode.getState().setLeafSet(ls);
        myNode.getState().setTimeLastUpdated(Calendar.getInstance().getTimeInMillis());
        
        
        
        /* Ensure that the state has been fully created before sending informing messages. */
        try{
            Thread.sleep(500);
        }
        catch(InterruptedException ie){ie.printStackTrace();}
        
        
        
        
        
        
        /* Check if the enties of the Leaf Set can be used to fill empty cells in the Routing Table. */
        
        Set<NodeAddress> leafset = new TreeSet<NodeAddress>();

        
        /* LeafSetSmall */
        for(NodeAddress na : myNode.getState().getLeafSet().getSmallSet())
            if(na != null)
                leafset.add(na);
        
        /* LeafSetLarge */
        for(NodeAddress na : myNode.getState().getLeafSet().getLargeSet())
            if(na != null)
                leafset.add(na);
        
        
        
        
        
        
        
        
        
        /* Search all the entries of the received state to find where in the Routing Table should they be inserted. */
        for(NodeAddress na : leafset){
            
            //if(na.getNodeID().compareTo(myNode.getAddress().getNodeID()) != 0) {
                        
                int prfx = Pastry.shl(myNode.getAddress().getNodeID(), na.getNodeID());
                
                
                while(prfx >= 0){
                
                
                String s = na.getNodeID().substring(prfx, prfx+1);
                String col = "-1";
                        
                        
                if(s.equals("a"))
                    col = "10";
                else if(s.equals("b"))
                    col = "11";
                else if(s.equals("c"))
                    col = "12";
                else if(s.equals("d"))
                    col = "13";
                else if(s.equals("e"))
                    col = "14";
                else if(s.equals("f"))
                    col = "15";
                else
                    col = s;
            
            
                int col_n = Integer.parseInt(col);
            
            
                /**
                 * If the appropriate position is empty and if the node isn't already in the table, and it
                 * isn't the current node.
                 */
                if(myNode.getState().getRoutingTable().getEntry(prfx, col_n) == null){
                    //if(!myNode.getState().getRoutingTable().exists(na) ){
                    
                        myNode.getState().getRoutingTable().getTableRow(prfx).setRowIndex(na, col_n);
                        myNode.getState().getRoutingTable().getTableRow(prfx).numOfEntriesPlusPlus();
                        myNode.getState().getRoutingTable().numOfEntriesPlusPlus();
                    
                    //}
                }
                
                prfx--;
                
                }
            
            
            //}
        }
        
        
        /* Sends the original timestamp back to the sender. */
        Pastry.forward(new Message("ACK_LEAF_QUESTION", myNode.getAddress(), null, msg.getKey(), msg.getTimestamp()), msg.getAddress());
        
        
        
        //stateBuilt = true;
        
        /* Inform all nodes in current node's state. */
        //AppInstance.informArrival();
        
          
    }
    
    
    
    
    
    
    /**
     * Method that takes as a parameter a Row of the RoutingTable from the nodes that route the 
     * Join Message to the destination node, and constructs the RoutingTable of the new node row by row.
     */
    public static void constructRoutingTableRow(Message msg){
        
        RoutingTableRow rtr = (RoutingTableRow) msg.getObject();
        NodeAddress sender = msg.getAddress();
        int row = Integer.parseInt(msg.getKey());
        
        
        /* Checking if the senders nodeID can fill in an empty cell in the particular row. */
        
        String s = sender.getNodeID().substring(row, row+1);
        String col = "-1";
                        
                        
        if(s.equals("a"))
            col = "10";
        else if(s.equals("b"))
            col = "11";
        else if(s.equals("c"))
            col = "12";
        else if(s.equals("d"))
            col = "13";
        else if(s.equals("e"))
            col = "14";
        else if(s.equals("f"))
            col = "15";
        else
            col = s;
        
        
        
        
        
        
        if(rtr.getRowIndex(Integer.parseInt(col)) == null){
            rtr.setRowIndex(sender, Integer.parseInt(col));
        }
        
        
        rtr.calculateNumOfEntries();
        byte temp = rtr.getNumOfEntries();
        myNode.getState().getRoutingTable().setTableRow(row, rtr);
        myNode.getState().getRoutingTable().calculateNumOfEntries();
        
    }
    
    
    
    
    
    /**
     * Method that sends the whole state of the new node, to every node that is contained
     * in this state. In that way, the new node informs every node in its state about its arrival.
     */
    public static void informArrival(){

        /* Gathers all the nodes in the state into one set. */
        Set<NodeAddress> temp = new TreeSet<NodeAddress>();

        
        
        /* LeafSetSmall */
        for(NodeAddress na : myNode.getState().getLeafSet().getSmallSet())
            if(na != null)
                temp.add(na);
        
        /* LeafSetLarge */
        for(NodeAddress na : myNode.getState().getLeafSet().getLargeSet())
            if(na != null)
                temp.add(na);

        
        
        /* NeighbothoodSet (currently not implemented) */
        /*
        for(NodeAddress na : myNode.getState().getNeighborhoodSet())
            if(na != null)
                temp.add(na);
        */
        
        
        /* RoutingTable */
        for(RoutingTableRow rtr : myNode.getState().getRoutingTable().getTable()) {
            for(NodeAddress na : rtr.getRow()){
                if(na != null)
                    temp.add(na);
            }
        }


        for(NodeAddress na : temp)
            Pastry.forward(new Message("STATE", myNode.getAddress(), myNode.getState(), myNode.getAddress().getNodeID(), 0), na);
        
        
        //...//
        System.out.println("\n\n***BOOTSTRAP***\n\nLEAF SET "+myNode.getState().getLeafSet().getNumOfLeaves());
        System.out.println("SMALL");
        for(NodeAddress na : myNode.getState().getLeafSet().getSmallSet())
            if(na != null)
                System.out.println(na.getNodeID());
        System.out.println("LARGE");
        for(NodeAddress na : myNode.getState().getLeafSet().getLargeSet())
            if(na != null)
                System.out.println(na.getNodeID());
        
        /*System.out.println("\n\n***BOOTSTRAP***\n\nROUTING TABLE "+myNode.getState().getRoutingTable().getNumOfEntries());
        for(RoutingTableRow rtr : myNode.getState().getRoutingTable().getTable()){
            System.out.print("ROW - "+rtr.getNumOfEntries()+" :  ");
            for(NodeAddress na : rtr.getRow()){
                if(na != null)
                    System.out.print(" "+na.getNodeID()+" ");
                else
                    System.out.print(" null ");
            }
            System.out.println("");
        }*/
        
    }
    
    
    
    
    
    
    /**
     * Method that updates the state of a node, when a message containing the state of new node
     * is received.
     */
    public static void updateState(Message msg){
        
        /* LeafSet Update */
        
        
        /** 
         * If the number of leaves is less than 32 OR 
         * if there are 32 leaves AND the new node's nodeID is between the range of the current node's LeafSet.
         */
        if(myNode.getState().getLeafSet().getNumOfLeaves() <  32 || 
           myNode.getState().getLeafSet().getNumOfLeaves() == 32 && 
              ((msg.getAddress().getNodeID().compareTo(myNode.getState().getLeafSet().getMin().getNodeID()) >= 0 ) &&
               (msg.getAddress().getNodeID().compareTo(myNode.getState().getLeafSet().getMax().getNodeID()) <= 0 ) )
        ){

            
            /* similar way as in "constructLeafSet(Message msg)" */
            
            LeafSet temp = myNode.getState().getLeafSet();
            
            NodeAddress[] temp_array = new NodeAddress[temp.getNumOfLeaves() + 1];


            int index = -1;

            for(byte i=0;i< temp.getSmallSet().size();i++){
                if(temp.getLeafSmall(i) != null){
                    index++;
                    temp_array[index] = temp.getLeafSmall(i);
                }
            }
            for(byte i=0;i< temp.getLargeSet().size();i++){
                if(temp.getLeafLarge(i) != null){
                    index++;
                    temp_array[index] = temp.getLeafLarge(i);
                }
            }
            index++;
            temp_array[index] = msg.getAddress();



        
            
            ArrayList<NodeAddress> temp_small = new ArrayList<NodeAddress>(16),temp_large = new ArrayList<NodeAddress>(16);
        

            
            
            int counter_small = 0, counter_large = 0;
        
            for(NodeAddress na : temp_array){
                if(na.getNodeID().compareTo( myNode.getAddress().getNodeID()) > 0){
                    if(counter_large < 16){    
                        temp_large.add(na);
                        counter_large++;
                    }    
                    else{
                        Collections.sort(temp_large);
                            
                        if(na.getNodeID().compareTo(temp_large.get(temp_large.size()-1).getNodeID()) < 0){
                            temp_large.set(temp_large.size()-1, na);
                        }  
                    }
                }
                else{
                    if(counter_small < 16){
                        temp_small.add(na);
                        counter_small++;
                    }
                    else{
                        Collections.sort(temp_small);
                        
                        if(na.getNodeID().compareTo(temp_small.get(0).getNodeID()) > 0){
                            temp_small.set(0, na);
                        }
                    }
                }
            }
        
        
        
            Collections.sort(temp_small);
            Collections.sort(temp_large);
        
        
            LeafSet ls = new LeafSet();
            ls.setSmallSet(temp_small);
            ls.setLargeSet(temp_large);
            ls.setNumOfLeaves(ls.getSmallSet().size() + ls.getLargeSet().size());
        
            
            myNode.getState().setLeafSet(ls);
            myNode.getState().setTimeLastUpdated(Calendar.getInstance().getTimeInMillis());


        }
        
        
        
        
        
        /* RoutingTable Update */
        
        
        
        /* Check if the nodes in the received state can fill null entries of our Routing Table. */
        
        Set<NodeAddress> all = new TreeSet<NodeAddress>();
        State state = (State) msg.getObject();

        
        /* LeafSetSmall */
        for(NodeAddress na : state.getLeafSet().getSmallSet())
            if(na != null)
                all.add(na);
        
        /* LeafSetLarge */
        for(NodeAddress na : state.getLeafSet().getLargeSet())
            if(na != null)
                all.add(na);

        
        
        /* NeighbothoodSet (currently not implemented) */
        /*
        for(NodeAddress na : state.getNeighborhoodSet())
            if(na != null)
                temp.add(na);
        */
        

        /* RoutingTable */
        for(RoutingTableRow rtr : state.getRoutingTable().getTable()) {
            for(NodeAddress na : rtr.getRow()){
                if(na != null)
                    all.add(na);
            }
        }
        
        all.add(msg.getAddress());
        
        
        
        
        
        
        
        /* Search all the entries of the received state to find where in the Routing Table should they be inserted. */
        for(NodeAddress na : all){
            
            if(na.getNodeID().compareTo(myNode.getAddress().getNodeID()) != 0) {
                        
                int prfx = Pastry.shl(myNode.getAddress().getNodeID(), na.getNodeID());
                
                
                while(prfx >= 0){
                
                
                String s = na.getNodeID().substring(prfx, prfx+1);
                String col = "-1";
                        
                        
                if(s.equals("a"))
                    col = "10";
                else if(s.equals("b"))
                    col = "11";
                else if(s.equals("c"))
                    col = "12";
                else if(s.equals("d"))
                    col = "13";
                else if(s.equals("e"))
                    col = "14";
                else if(s.equals("f"))
                    col = "15";
                else
                    col = s;
            
            
                int col_n = Integer.parseInt(col);
            
            
                /**
                 * If the appropriate position is empty and if the node isn't already in the table, and it
                 * isn't the current node.
                 */
                if(myNode.getState().getRoutingTable().getEntry(prfx, col_n) == null){
                    //if(!myNode.getState().getRoutingTable().exists(na) ){
                    
                        myNode.getState().getRoutingTable().getTableRow(prfx).setRowIndex(na, col_n);
                        myNode.getState().getRoutingTable().getTableRow(prfx).numOfEntriesPlusPlus();
                        myNode.getState().getRoutingTable().numOfEntriesPlusPlus();
                    
                    //}
                }
                
                prfx--;
                
                }
            
            
            }
        }
        
        
        
        
        
        
        
        
        //...//
        System.out.println("\n\n***UPDATE***\n\nLEAF SET "+myNode.getState().getLeafSet().getNumOfLeaves());
        System.out.println("SMALL");
        for(NodeAddress na : myNode.getState().getLeafSet().getSmallSet())
            if(na != null)
                System.out.println(na.getNodeID());
        System.out.println("LARGE");
        for(NodeAddress na : myNode.getState().getLeafSet().getLargeSet())
            if(na != null)
                System.out.println(na.getNodeID());
        
        /*System.out.println("\n\n***UPDATE***\n\nROUTING TABLE "+myNode.getState().getRoutingTable().getNumOfEntries());
        for(RoutingTableRow rtr : myNode.getState().getRoutingTable().getTable()){
            System.out.print("ROW - "+rtr.getNumOfEntries()+" :  ");
            for(NodeAddress na : rtr.getRow()){
                if(na != null)
                    System.out.print(" "+na.getNodeID()+" ");
                else
                    System.out.print(" null ");
            }
            System.out.println("");
        }*/
           
        
    }
    
    
    
    
    
    /* METHODS FOR STATE CREATIONS, ARRIVAL INFORM, UPDATE STATE */
    
    
    
    
    public static void main(String args[]){
        
        try{
            launchApp();
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
         
        
        
        
        //...//
        System.err.println(myNode.getAddress().getNodeID());
        
        /*try{
            System.out.println(Utilities.objectToByteArray(new RoutingTableRow()).length);
        }
        catch(Exception e){}*/
        
        /*Calendar c = Calendar.getInstance();
        long l = c.getTimeInMillis();
        
        try{System.out.println(Utilities.objectToByteArray(l).length);}
        catch(Exception e){e.printStackTrace();}*/
        
        
    }
    
    
}
