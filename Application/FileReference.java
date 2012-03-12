package Application;

import java.io.Serializable;

public class FileReference implements Serializable{
    String      oid;
    String      remoteIP;
    int         remotePort;

    public FileReference(String oid, String remoteIP, int remotePort) {
        this.oid = oid;
        this.remoteIP = remoteIP;
        this.remotePort = remotePort;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getRemoteIP() {
        return remoteIP;
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
}
