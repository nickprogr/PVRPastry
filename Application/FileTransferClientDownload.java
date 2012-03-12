/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Application;

import Pastry.Message;
import Pastry.Node;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileOutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import java.util.*;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.io.CopyStreamException;
public class FileTransferClientDownload extends SwingWorker<Void,Void> implements PropertyChangeListener{

    int         serverPort;
    String      filename;
    String      addr;
    JTextArea   appLog;

    FileTransferClientDownload(String name, JTextArea log) {
        filename = name;
        appLog = log;
    }

    FileTransferClientDownload(int p, String name, JTextArea log) {
        serverPort = p;
        filename = name;
        appLog = log;
        addr = "localhost";
        addPropertyChangeListener(this);
    }

    FileTransferClientDownload(int p, String name, String address, JTextArea log) {
        serverPort = p;
        filename = name;
        appLog = log;
        addr = address;
        addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("Mpampis Event "+evt.getPropertyName());
    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            Node n = AppForm.state.pastryNode;
            String fhash = Pastry.Hashing.SHA1_128bit(filename);
            System.out.println("File Hash "+fhash);
            Pastry.Pastry.route(
                    new Message("FIND_OBJECT", n.getAddress(), null, fhash, Calendar.getInstance().getTimeInMillis()));

            Message m;

            while(true) {
                if ((m = AppForm.state.downloadQueue.peek()) != null){
                    if ( m.getKey().compareTo(fhash) == 0 ) {
                        addr = ((FileReference)m.getObject()).getRemoteIP();
                        serverPort = ((FileReference)m.getObject()).getRemotePort();
                        AppForm.state.downloadQueue.remove(m);
                        break;
                    }
                }
                Thread.sleep(100);
            }
            
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

            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            input = ftp.retrieveFileStream(filename);

            if(!FTPReply.isPositivePreliminary(ftp.getReplyCode())) {
                input.close();
                ftp.logout();
                ftp.disconnect();

                System.err.println("File transfer failed.");
            }

            output = new FileOutputStream(new File(filename));

            try {
                //Util.copyStream(input, output);
                copy(input,output);
            } catch (CopyStreamException e) {

            }

            input.close();
            output.close();
            System.gc();

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
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void done() {
        appLog.append("File Downloaded");
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[4096];
        int read, sum = 0;
        int max = in.available();
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
            //sum += (int)read/max;
            //this.setProgress(sum);
        }
    }
}
