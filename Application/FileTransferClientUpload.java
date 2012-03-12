package Application;

import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.beans.PropertyChangeEvent;
import java.io.File;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.io.CopyStreamException;

public class FileTransferClientUpload extends SwingWorker<Void,Void> implements PropertyChangeListener{

    int         serverPort;
    String      filename;
    String      addr;
    JTextArea   appLog;

    FileTransferClientUpload(int p, String name, JTextArea log) {
        serverPort = p;
        filename = name;
        appLog = log;
        addr = "localhost";
        addPropertyChangeListener(this);
    }

    FileTransferClientUpload(int p, String name, String address, JTextArea log) {
        serverPort = p;
        filename = name;
        appLog = log;
        addr = address;
        addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {

    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            FTPClient ftp = new FTPClient();
            ftp.connect(addr, serverPort);
            int reply = ftp.getReplyCode();

            if(!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
                System.exit(1);
            }
            
            ftp.setBufferSize(100000);
            ftp.login("pvrp", "pvrp");

            ftp.enterLocalPassiveMode();

            InputStream input;
            OutputStream output;
            input = new FileInputStream(filename);

            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            output = ftp.storeFileStream((new File(filename)).getName());

            if(!FTPReply.isPositivePreliminary(ftp.getReplyCode())) {
                input.close();
                output.close();
                ftp.logout();
                ftp.disconnect();

                System.err.println("File transfer failed.");
            }

            try {
                //Util.copyStream(input, output);
                copy(input,output);
            } catch (CopyStreamException e) {
                
            }

            input.close();
            output.close();
            
            if(!ftp.completePendingCommand()) {
                ftp.logout();
                ftp.disconnect();
                System.err.println("File transfer failed.");
            }
           
            //PropertyChangeEvent evt = new PropertyChangeEvent(this);
            //setProgress(reply);
            ftp.disconnect();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void done() {
        String fhash = Pastry.Hashing.SHA1_128bit((new File(filename)).getName());
        AppForm.state.servPort = serverPort;
        if (AppForm.state.pastryNode != null){
            FileReference oRef = new FileReference( fhash
                                                    , AppForm.state.pastryNode.getAddress().getIp()
                                                    , AppForm.state.servPort);
            
            AppForm.state.files.addStoredFile((new File(filename)).getName());
            appLog.append((new File(filename)).getName()+"\n");
            AppUtilities.requestService(oRef);
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[4096];
        int read, sum = 0;
        int max = in.available();
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
            sum += (int)read/max;
            this.setProgress(sum);
        }
    }
}

