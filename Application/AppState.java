package Application;

import Pastry.Message;
import Pastry.Node;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.ftpserver.FtpServer;

public class AppState {
    FtpServer                       ftpServ;
    int                             servPort;
    Node                            pastryNode;
    boolean                         joinedRing;
    boolean                         serverRunning;
    FileCache                       files;
    ConcurrentLinkedQueue<Message>  downloadQueue;

    AppState() {
        ftpServ = null;
        servPort = -1;
        joinedRing = false;
        serverRunning = false;
        files = new FileCache();
        downloadQueue = new ConcurrentLinkedQueue<Message>();
    }
}
