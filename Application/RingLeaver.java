package Application;

import Pastry.AppInstance;
import Pastry.Node;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class RingLeaver extends SwingWorker<Node,Void> {
    
    JTextArea   log;
    JButton     btn;
    JButton     dbtn;
    JButton     ftpbtn;
    JButton     upbtn;

    public RingLeaver (JTextArea logRef, JButton btnRef, JButton down, JButton ftp, JButton upld){
        log = logRef;
        btn = btnRef;
        dbtn = down;
        ftpbtn = ftp;
        upbtn = upld;
    }

    @Override
    protected Node doInBackground() {
        System.out.println("Leaving Pastry . . .");
        
        AppInstance.exitApp();
        
        return null;
    }

    @Override
    public void done() {
        try {
            dbtn.setEnabled(false);
            btn.setEnabled(false);
            ftpbtn.setEnabled(false);
            upbtn.setEnabled(false);
            log.append("====    "+AppForm.state.pastryNode.getAddress().getNodeID()+" departed \n             from network.\n");
        } catch (Exception ignore) {
            log.append(AppForm.state.pastryNode.getAddress().getNodeID()+"\n");
            ignore.printStackTrace();
        }
    }
    
}
