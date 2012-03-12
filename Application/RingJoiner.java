package Application;

import Pastry.AppInstance;
import Pastry.Node;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class RingJoiner extends SwingWorker<Node,Void>{

    JTextArea   log;
    JButton     btn;
    JButton     dbtn;

    RingJoiner (JTextArea logRef, JButton btnRef, JButton down){
        log = logRef;
        btn = btnRef;
        dbtn = down;
    }

    @Override
    protected Node doInBackground() {
        //System.out.println("Worker");
        try {
            AppInstance.launchApp();
        } catch (InterruptedException ex) {
            Logger.getLogger(AppForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        return AppInstance.myNode;
    }

    @Override
    public void done() {
        Node nodeID = null;
        try {
            nodeID = get();
            AppForm.state.pastryNode = nodeID;
            dbtn.setEnabled(true);
            btn.setEnabled(true);
            log.append("====    "+nodeID.getAddress().getNodeID()+"\n");
            System.err.println(AppInstance.myNode.getAddress().getNodeID());
        } catch (Exception ignore) {
            log.append(nodeID.getAddress().getNodeID()+"\n");
            ignore.printStackTrace();
        }
        btn.setEnabled(true);
    }

}

