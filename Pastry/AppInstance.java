package Pastry;

import Application.AppForm;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.SwingUtilities;

public class AppInstance {
    
    public static Node myNode;
    public static boolean stateBuilt, departing;
    public static Thread thr1, thr2;
    
    /**
     * Method that launches an instance of our application and sets it ready for use.
     * 
     * @throws InterruptedException 
     */
    public static void launchApp() throws InterruptedException {
        
        myNode = new Node();    /* The node of our application. */
        stateBuilt = false;     /* States if our state is fully built. Checked periodically by a thread. */
        departing = false;      /* States if the departing procedures is initiated. */
        
        
        /* Setting up the MessageServer in a separate Thread. It will always wait for a new message.*/
        thr1 = new Thread(new MessageServer(myNode.getAddress()));
        thr1.start();
        
        
        
        /* Finding a node to deliver our JOIN message. */
        NodeAddress bootstrapAddress = MulticastRequest.makeRequest();
        
        
        
        
        /* If no node was found, probably this is the first node in the network. */
        if(bootstrapAddress == null){
          
            /* TODO: RESEND 2 TIMES */
            
            myNode.getState().getRoutingTable().setTableRow(0, new RoutingTableRow());
            
        }
        else{
                
            /* Create a join message and deliver it to the bootstrap node. */
            final Message joinMsg = new Message("JOIN0", myNode.getAddress(), null, myNode.getAddress().getNodeID(), 0);
        
            try{
                Pastry.forward(joinMsg, bootstrapAddress);
            }
            catch(ConnectException ce){
                ce.printStackTrace();
                
                /* TODO: FIND NEW BOOTSTRAP NODE */
            }
            
            
            /* A final copy to be inserted in an inline thread. */
            final NodeAddress bootAddr = bootstrapAddress;
            
            
            
            /**
             * Inline thread that checks every 10 seconds if the state of the node has been initialized.
             * Once it is initialized, it stops running.
             */
            Thread timeOutThread = new Thread(
                
                new Runnable() {
                    public void run() { 
                        
                        while(true){
                        
                            try{
                                Thread.sleep(10000);
                            }
                            catch(InterruptedException ie){
                                ie.printStackTrace();
                            }
                        
                            if(!stateBuilt){
                                try{
                                    /* Forward again the JOIN message to the bootstrap node. */
                                    Pastry.forward(joinMsg, bootAddr);
                                }
                                catch(ConnectException ce){
                                    ce.printStackTrace();
                                    
                                    /* TODO: FIND NEW BOOTSTRAP NODE */
                                }
                            }
                            else
                                break;
                        }
                    }
                }      
            );
            
            
            timeOutThread.start();
        }
        
        
        /**
         * Setting up the Multicast Server in a separate thread. It will always wait for a new message.
         * When a multicast message is received, the receiver answers in order to be a bootstrap node candidate.
         */
        thr2 = new Thread(new MulticastServer(myNode.getAddress()));
        thr2.start();
        
    }
    
    
    
    
    /**
     * Method that takes as a parameter the LeafSet from the node with the closest
     * nodeID to the current node's nodeID, and constructs the LeafSet of the new node.
     * 
     * @param msg : The message that contains the LeafSet from the closer node in the ID space.
     */
    public static void constructLeafSet(Message msg){
        
        /**
         * Placing all the leaf nodes in an array, to be examined more easily. The sender's NodeAddress is
         * also placed in the new collection, because it may belong to our leaf space.
         */
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




       
        
        /* Building the 2 sets of our Leaf Set node by node. */
        ArrayList<NodeAddress> temp_small = new ArrayList<NodeAddress>(16),temp_large = new ArrayList<NodeAddress>(16);
        
        
        
        int counter_small = 0, counter_large = 0;
        
        for(NodeAddress na : temp_array){
            /**
             * If the node has ID larger than ours, it is placed on the LargeLeafSet, else it is placed in the 
             * SmallLeafSet. Every minor set can hold up to 16 nodes. If one has less than 16 nodes, then a new
             * node is inserted at once. Else we have to compare ID's and decide if a node has to be removed
             * from the set, in order to place another one in it.
             */
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
        
        
        /* Our Leaf Set is always sorted in ascending order. */
        Collections.sort(temp_small);
        Collections.sort(temp_large);
        
        
        /* Constructing the final Set. */
        LeafSet ls = new LeafSet();
        ls.setSmallSet(temp_small);
        ls.setLargeSet(temp_large);
        ls.setNumOfLeaves(ls.getSmallSet().size() + ls.getLargeSet().size());
        
        
        
        myNode.getState().setLeafSet(ls);
        myNode.getState().setTimeLastUpdated(Calendar.getInstance().getTimeInMillis());
        
        
        
        /* Ensure that all the RoutingTableRow messages have been already arrived. */
        try{
            Thread.sleep(300);
        }
        catch(InterruptedException ie){ie.printStackTrace();}
        
        
        
        /**
         * Check if the entries of the Leaf Set can be used to fill empty cells in the Routing Table.
         * We are going to place them in a new collection to examine them more easily, and because
         * it is better not to keep our Leaf Set locked for the whole procedure.
         */
        Set<NodeAddress> leafset = new TreeSet<NodeAddress>();

        
        
        
        /* Locking the LeafSet. */
        myNode.getState().getLeafSet().semAcquire();
        
        /* LeafSetSmall */
        for(NodeAddress na : myNode.getState().getLeafSet().getSmallSet())
            if(na != null)
                leafset.add(na);
        
        /* LeafSetLarge */
        for(NodeAddress na : myNode.getState().getLeafSet().getLargeSet())
            if(na != null)
                leafset.add(na);
        
        /* Release the lock upon the LeafSet. */
        myNode.getState().getLeafSet().semRelease();
        
        
        
        
        
        /* Locking the Routing Table during the filling. */
        AppInstance.myNode.getState().getRoutingTable().semAcquire();
        
        
        /* Search all the entries of the received state to find where in the Routing Table should they be inserted. */
        for(NodeAddress na : leafset){
            
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
            
            
                /* If the appropriate position is empty, insert the candidate node. */
                if(myNode.getState().getRoutingTable().getEntry(prfx, col_n) == null){
                    
                    myNode.getState().getRoutingTable().getTableRow(prfx).setRowIndex(na, col_n);
                    myNode.getState().getRoutingTable().getTableRow(prfx).numOfEntriesPlusPlus();
                    myNode.getState().getRoutingTable().numOfEntriesPlusPlus();
                    
                }
                else{
                    /* If the candidate node is not already in the Routing Table. */
                    if(!myNode.getState().getRoutingTable().exists(na) ){
                            
                        int prfx2 = Pastry.shl(myNode.getAddress().getNodeID(), myNode.getState().getRoutingTable().getEntry(prfx, col_n).getNodeID());
                        
                        /**
                         * And if the node that fills this position is also in a position of the table of 
                         * greater row number.
                         */
                        if(prfx2 > prfx)
                            myNode.getState().getRoutingTable().getTableRow(prfx).setRowIndex(na, col_n);
                    }
                }
                
                prfx--;
                
            }
            
        }
        
        /* Release the lock upon the Routing Table. */
        AppInstance.myNode.getState().getRoutingTable().semRelease();
        
        
        
        
        /* Sends the original timestamp back to the sender. */
        try{
            Pastry.forward(new Message("ACK_LEAF_QUESTION", myNode.getAddress(), null, msg.getKey(), msg.getTimestamp()), msg.getAddress());
        }
        catch(ConnectException ce){
            /* DO NOTHING*/
        }
        
          
    }
    
    
    
    
    
    
    /**
     * Method that takes as a parameter a Row of the RoutingTable from the nodes that route the 
     * Join Message to the destination node, and constructs the RoutingTable of the new node row by row.
     * 
     * @param msg : The message that contains the RoutingTableRow from a node of the JOIN message route path.
     */
    public static void constructRoutingTableRow(Message msg){
        
        /* We work on a temp row. */
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
        
        
        
        
        
        /* If the cell is null. */
        if(rtr.getRowIndex(Integer.parseInt(col)) == null){
            rtr.setRowIndex(sender, Integer.parseInt(col));
        }
        
        
        rtr.calculateNumOfEntries();
        
        AppInstance.myNode.getState().getRoutingTable().semAcquire();
        
        /* Placing in the original Routing Table. */
        myNode.getState().getRoutingTable().setTableRow(row, rtr);
        myNode.getState().getRoutingTable().calculateNumOfEntries();
        
        AppInstance.myNode.getState().getRoutingTable().semRelease();
    }
    
    
    
    
    
    /**
     * Method that sends the whole state of the new node, to every node that is contained
     * in this state. In that way, the new node informs every node in its state about its arrival.
     */
    public static void informArrival(){

        /* Locking the LeafSet during the method. */
        myNode.getState().getLeafSet().semAcquire();
        
        String type = null;
        NodeAddress dest = null;
        Set<String> temp = new TreeSet<String>();   /* Contains the IDs of the nodes we have already sent our state. */
        
        
        try{
        
            /* LeafSetSmall */
            
            type = "small";
            for(NodeAddress na : myNode.getState().getLeafSet().getSmallSet()){
                if(na != null){
                    temp.add(na.getNodeID());
                    dest = na;
                    Pastry.forward(new Message("STATE", myNode.getAddress(), myNode.getState(), myNode.getAddress().getNodeID(), 0), na);
                }
            }
            
            
            /* LeafSetLarge */
        
            type = "large";
            for(NodeAddress na : myNode.getState().getLeafSet().getLargeSet()){
                if(na != null){
                    temp.add(na.getNodeID());
                    dest = na;
                    Pastry.forward(new Message("STATE", myNode.getAddress(), myNode.getState(), myNode.getAddress().getNodeID(), 0), na);
                }
            }
            
            
            /* NeighbothoodSet (currently not implemented) */
            /*
            type = "neigh";
            for(NodeAddress na : myNode.getState().getNeighborhoodSet()){
                if(na != null){
                    dest = na;
                    Pastry.forward(new Message("STATE", myNode.getAddress(), myNode.getState(), myNode.getAddress().getNodeID(), 0), na);
                }
            }
            */
            
           
        }
        catch(ConnectException ce){
            /**
             * FAULT TOLERANCE (Leaf Set Fault)
             * 
             * If we sent our State to a failed node, we remove him from our Leaf Set. Then we spawn a new thread
             * which searches for a suitable substitution (Fault Tolerance). In the end, we recall the method and
             * start the whole procedure from the begining.
             */
            if(type.equals("small") || type.equals("large")){
                            
                AppInstance.myNode.getState().getLeafSet().remove(dest);
                            
                int t = AppInstance.myNode.getState().getLeafSet().getNumOfLeaves();
                AppInstance.myNode.getState().getLeafSet().setNumOfLeaves(t-1);
                            
                            
                Thread fault_h = new Thread(new FaultHandler(type));
                fault_h.start();
                    
                informArrival();
                return;
            }
        }
            
            
            
           
            
        /* RoutingTable */
        
        /* Locking the Routing Table during the informing. */
        myNode.getState().getRoutingTable().semAcquire();
            
        type = "route";
        int len = -1, col = -1;
        for(RoutingTableRow rtr : myNode.getState().getRoutingTable().getTable()) {
            len++;
            for(NodeAddress na : rtr.getRow()){
                col++;
                if(na != null){
                    if(!temp.contains(na.getNodeID())){
                        temp.add(na.getNodeID());
                        dest = na;
                        try{
                            Pastry.forward(new Message("STATE", myNode.getAddress(), myNode.getState(), myNode.getAddress().getNodeID(), 0), na);
                        }
                        /**
                         * FAULT TOLERANCE (Routing Table Fault)
                         * 
                         * If we sent our State to a failed node, we remove him from our Routing Table. Then we 
                         * spawn a new thread which searches for a suitable substitution (Fault Tolerance). The
                         * informing procedure continues without being aware of the replacements.
                         */
                        catch(ConnectException ce){
                            if(type.equals("route")){
                                
                                /* Erase the failed node from the Routing Table. */
                                AppInstance.myNode.getState().getRoutingTable().getTableRow(len).setRowIndex(null, col);
                                AppInstance.myNode.getState().getRoutingTable().getTableRow(len).numOfEntriesMinusMinus();
                                AppInstance.myNode.getState().getRoutingTable().numOfEntriesMinusMinus();
                                
                                Thread fault_h = new Thread(new FaultHandler("route", len, col));
                                fault_h.start();
                
                            }
                        }
                    }
                }
            }
            col = -1;
        }
            
            
            
        
        /* Prints for checking. */
        
        
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
        
        
        
        /* Release the lock upon the LeafSet and the Routing Table. */
        myNode.getState().getLeafSet().semRelease();
        myNode.getState().getRoutingTable().semRelease();
    }
    
    
    
    
    
    
    /**
     * Method that updates the state of a node, when a message containing the state of new node
     * is received.
     * 
     * @param msg : The message that contains the State being sent from the new node.
     */
    public static void updateState(Message msg){
        
        /* LeafSet Update */
        
        
        /* Locking the LeafSet during the method. */
        myNode.getState().getLeafSet().semAcquire();
        
        
        try{
        
            /** 
            * If the number of leaves is less than 32 OR 
            * if there are 32 leaves AND the new node's nodeID is between the range of the current node's LeafSet.
            */
            if(myNode.getState().getLeafSet().getNumOfLeaves() <  32 || 
                myNode.getState().getLeafSet().getNumOfLeaves() == 32 && 
                ((msg.getAddress().getNodeID().compareTo(myNode.getState().getLeafSet().getMin().getNodeID()) >= 0 ) &&
                (msg.getAddress().getNodeID().compareTo(myNode.getState().getLeafSet().getMax().getNodeID()) <= 0 ) )
            ){
                
                if(!AppInstance.myNode.getState().getLeafSet().contains(msg.getAddress())){
                
                    /* Similar way as in "constructLeafSet(Message msg)" */
            
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
            }
        }
        catch(NullPointerException npe){
            npe.printStackTrace();
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
        
        
        
        /* Locking the Routing Table during the filling. */
        myNode.getState().getRoutingTable().semAcquire();
        
        
        
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
            
                    NodeAddress addr = myNode.getState().getRoutingTable().getEntry(prfx, col_n);
                
            
                    /**
                     * If the appropriate position is empty then, if the candidate node isn't contained
                     * in the leaf set.
                     */
                    if(addr == null){
                            
                        myNode.getState().getRoutingTable().getTableRow(prfx).setRowIndex(na, col_n);
                        myNode.getState().getRoutingTable().getTableRow(prfx).numOfEntriesPlusPlus();
                        myNode.getState().getRoutingTable().numOfEntriesPlusPlus();
                        
                    }
                    else{
                        if(!myNode.getState().getRoutingTable().exists(na) ){
                            
                            int prfx2 = Pastry.shl(myNode.getAddress().getNodeID(), myNode.getState().getRoutingTable().getEntry(prfx, col_n).getNodeID());
                            
                            if(prfx2 > prfx)
                                myNode.getState().getRoutingTable().getTableRow(prfx).setRowIndex(na, col_n);
                        }
                    }
                
                    prfx--;
                
                }
            }
        }
        
        
        
        /* Print for checking */
        
        
        
        
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
           
        
        
        /* Release the lock upon the LeafSet and the Routing Table. */
        //kanonika prin to kommati tou routing table
        myNode.getState().getLeafSet().semRelease();
        myNode.getState().getRoutingTable().semRelease();
        
    }
    
    
    
    
    
    
    /**
     * Method that informs all the nodes in current node's Leaf Set that it is going to depart.
     */
    public static void informDeparture(){
        
        /* Locking the LeafSet during the method. */
        myNode.getState().getLeafSet().semAcquire();
        
        
        /* LeafSetSmall */
        for(NodeAddress na : myNode.getState().getLeafSet().getSmallSet()){
            try{
                Pastry.forward(new Message("DEPARTURE", myNode.getAddress(), null, null, 0), na);
            }
            catch(ConnectException ce){ /* DO NOTHING */}
        }
        
        /* LeafSetLarge */
        for(NodeAddress na : myNode.getState().getLeafSet().getLargeSet()){
            try{
                Pastry.forward(new Message("DEPARTURE", myNode.getAddress(), null, null, 0), na);
            }
            catch(ConnectException ce){ /* DO NOTHING */}
        }
        
        
        /* Release the lock upon the LeafSet. */
        myNode.getState().getLeafSet().semRelease();
        
    }
    
    
    
    
    
    /**
     * Method that prepares the network for a node's departure, and finally shuts it down.
     */
    public static void exitApp(){
        
        departing = true;
        
        /* Inform all the Leaf Nodes about the departure */
        informDeparture();
        
        
        /* Close all the running threads. */
        thr1.interrupt();
        thr2.interrupt();
        
        /* Simply cause an exception in these blocking methods, causing their threads to stop. */
        MulticastServer.listeningSocket.close();
        try{MessageServer.server.close();}catch(Exception ioe){}
        
    }
    
    
    
    
    
    
    public static void main(String args[]){
        
        /*try{
            launchApp();
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }*/
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AppForm().setVisible(true);
            }
        });
        
        
        
        //...//
        //System.err.println(myNode.getAddress().getNodeID());
        
    }
    
    
}
