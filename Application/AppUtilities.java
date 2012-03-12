package Application;

import Pastry.Message;
import Pastry.Node;
import Pastry.NodeAddress;
import Pastry.Pastry;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppUtilities {

    public static void requestService(FileReference oRef){
        Message msg = new Message("SERVE_OBJECT", AppForm.state.pastryNode.getAddress(), oRef, oRef.getOid(), Calendar.getInstance().getTimeInMillis()) ;
        Pastry.route(msg);
    }

    public static void updateFileState(Message msg) {
        try {
            FileReference oRef = (FileReference) msg.getObject();
            NodeAddress helper = msg.getAddress();
            AppForm.state.files.cacheUpdate.acquire();
            AppForm.state.files.addHelper(helper, oRef);
            System.out.println("File References "+AppForm.state.files.ownedFiles.size());
            AppForm.state.files.cacheUpdate.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(AppUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void referenceObject(Message msg){
        FileReference oRef = (FileReference) msg.getObject();
        try {
            AppForm.state.files.cacheUpdate.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(AppUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        AppForm.state.files.addReference(oRef);
        AppForm.state.files.cacheUpdate.release();
        try{
            Pastry.forward(new Message("ACK_SERVE_OBJECT", AppForm.state.pastryNode.getAddress(), msg.getObject(), msg.getKey(), Calendar.getInstance().getTimeInMillis()) , msg.getAddress());
        }
        catch(ConnectException ce){
            ce.printStackTrace();
        }
    }

    public static void serveObject(Message msg) {
        FileReference oRef = (FileReference) msg.getObject();
        NodeAddress noid = msg.getAddress();
        
        Node n = AppForm.state.pastryNode;
        try {
            AppForm.state.files.cacheUpdate.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(AppUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        AppForm.state.files.addReference(oRef);
        AppForm.state.files.cacheUpdate.release();
        
        try{
            Pastry.forward(new Message("ACK_SERVE_OBJECT", AppForm.state.pastryNode.getAddress(), msg.getObject(), msg.getKey(), Calendar.getInstance().getTimeInMillis()) , msg.getAddress());
        }
        catch(ConnectException ce){
            ce.printStackTrace();
        }

        Set<NodeAddress> nodeSet = new TreeSet<NodeAddress>();
        nodeSet.add(noid);
        for(NodeAddress na : n.getState().getLeafSet().getSmallSet())
            if(na != null)
                nodeSet.add(na);
        for(NodeAddress na : n.getState().getLeafSet().getLargeSet())
            if(na != null)
                nodeSet.add(na);
        List<NodeAddress> lst = new ArrayList<NodeAddress>(nodeSet);
        Collections.sort(lst);

        int num = lst.size();
        
        System.out.println("List of Nodes");
        for(int i = 0; i < num; i++){
            System.out.println(lst.get(i).getNodeID());
        }
        
        try{
            for(int i = 0; i < num; i++){
                if ( lst.get(i).getNodeID().compareTo(noid.getNodeID()) == 0 ){
                    if( num > 2 ){
                        if(i == 0) {
                            Pastry.forward(new Message("REFERENCE_OBJECT", noid, msg.getObject(), msg.getKey(), Calendar.getInstance().getTimeInMillis()) , lst.get(1));
                            Pastry.forward(new Message("REFERENCE_OBJECT", noid, msg.getObject(), msg.getKey(), Calendar.getInstance().getTimeInMillis()) , lst.get(2));
                        } else if (i == num - 1) {
                            Pastry.forward(new Message("REFERENCE_OBJECT", noid, msg.getObject(), msg.getKey(), Calendar.getInstance().getTimeInMillis()) , lst.get(num-3));
                            Pastry.forward(new Message("REFERENCE_OBJECT", noid, msg.getObject(), msg.getKey(), Calendar.getInstance().getTimeInMillis()) , lst.get(num-2));
                        } else {
                            Pastry.forward(new Message("REFERENCE_OBJECT", noid, msg.getObject(), msg.getKey(), Calendar.getInstance().getTimeInMillis()) , lst.get(i+1));
                            Pastry.forward(new Message("REFERENCE_OBJECT", noid, msg.getObject(), msg.getKey(), Calendar.getInstance().getTimeInMillis()) , lst.get(i-1));
                        }
                    } else if (num == 2) {
                        if(i == 0) {
                            Pastry.forward(new Message("REFERENCE_OBJECT", noid, msg.getObject(), msg.getKey(), Calendar.getInstance().getTimeInMillis()) , lst.get(1));
                        } else {
                            Pastry.forward(new Message("REFERENCE_OBJECT", noid, msg.getObject(), msg.getKey(), Calendar.getInstance().getTimeInMillis()) , lst.get(num-2));}
                    } else {

                    }
                    break;
                }
            }
        }
        catch(ConnectException ce){
            ce.printStackTrace();
        }
            
            
    }

    public static void notifyDownloader(Message receivedMsg) {
        AppForm.state.downloadQueue.add(receivedMsg);
        System.out.println("Message Key : "+receivedMsg.getKey());
        System.out.println("Sender : "+receivedMsg.getAddress().getNodeID());
        System.out.println("Server IP : "+((FileReference)receivedMsg.getObject()).remoteIP);
        System.out.println("Server Port : "+((FileReference)receivedMsg.getObject()).remotePort);
    }

    public static void checkCache(Message msg) {
        String fkey = msg.getKey();
        try {
            AppForm.state.files.cacheUpdate.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(AppUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        int num = AppForm.state.files.references.size();
        System.out.println("Number of References "+num);
        for ( int i = 0; i < num; i++ ) {
            if ( AppForm.state.files.references.get(i).oid.compareTo(fkey) == 0){
                try{
                    Pastry.forward( new Message("OBJECT_LOCATION", AppForm.state.pastryNode.getAddress()
                                        , AppForm.state.files.references.get(i), msg.getKey()
                                        , Calendar.getInstance().getTimeInMillis())
                                        , msg.getAddress());
                }
                catch(ConnectException ce){
                    ce.printStackTrace();
                }
            }
        }
        AppForm.state.files.cacheUpdate.release();
    }
}
