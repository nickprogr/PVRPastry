package Application;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

/**
 *
 * @author dre
 */
public class FileTransferServer extends SwingWorker<Boolean,Void>{

    FtpServer   server;
    int         listeningPort;
    String      dir;
    JTextArea   log;
    JButton     btn;
    JButton     upBtn;
    AppState    state;
    File[]      stored = null;

    public FileTransferServer   (String directory, JTextArea txt, JButton b, JButton up, AppState stateRef) {
        dir = directory;
        log = txt;
        btn = b;
        upBtn = up;
        state = stateRef;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        FtpServerFactory serverFactory = new FtpServerFactory();
        PropertiesUserManagerFactory userManagerFactory = new
        PropertiesUserManagerFactory();

        //userManagerFactory.setFile(new File("myusers.properties"));
        userManagerFactory.setPasswordEncryptor(new
        SaltedPasswordEncryptor());
        UserManager userManager = userManagerFactory.createUserManager();

        BaseUser user = new BaseUser();
        user.setName("pvrp");
        user.setPassword("pvrp");

        int in = ManagementFactory.getRuntimeMXBean().getName().indexOf("@");
        int pid = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().substring(0, in));

        listeningPort = pid + 20000;
        state.servPort = listeningPort;
        File fileDir = new File(dir);

        if(!fileDir.isDirectory())
            if(!fileDir.mkdir())
                return Boolean.FALSE;

        user.setHomeDirectory(dir);
        List<Authority> auths = new ArrayList<Authority>();
        Authority auth = new WritePermission();
        auths.add(auth);
        user.setAuthorities(auths);

        try {
            userManager.save(user);
        } catch (FtpException e1) {
            e1.printStackTrace();
        }
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(listeningPort);
        serverFactory.addListener("default",listenerFactory.createListener());
        serverFactory.setUserManager(userManager);

        server = serverFactory.createServer();

        try {
            server.start();
        } catch (FtpException e) {
            return Boolean.FALSE;
        }

        stored = fileDir.listFiles();
        /*
        AppForm.state.files.cacheUpdate.acquire();
        for(int i = 0; i < stored.length; i++){
            System.out.println(stored[i].getName());
            AppForm.state.files.addStoredFile(stored[i].getName());
        }
        
        if ( AppForm.state.pastryNode != null ) {
            for(int i = 0; i < AppForm.state.files.ownedFiles.size(); i++){
                FileReference oRef = new FileReference( AppForm.state.files.getFileHash(i)
                                                    , AppForm.state.pastryNode.getAddress().getIp()
                                                    , AppForm.state.servPort);
                Message msg = new Message("SERVE_OBJECT", AppForm.state.pastryNode.getAddress(), oRef, oRef.getOid(), Calendar.getInstance().getTimeInMillis()) ;
                System.out.println("Stuck");
                Pastry.route(msg, AppForm.state.pastryNode);
                System.out.println("Stuck");
            }
        }
        AppForm.state.files.cacheUpdate.release();*/
        return Boolean.TRUE;
    }

    @Override
    public void done() {
        Boolean up = Boolean.FALSE;
        try {
            up = get();
            if(up.booleanValue()==true){
                btn.setEnabled(true);
                upBtn.setEnabled(true);
                log.setText("====    Server is up!\n");
                state.servPort = listeningPort;

            } else {
                log.setText("====    Server could not be started!\n");
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(FileTransferServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(FileTransferServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
