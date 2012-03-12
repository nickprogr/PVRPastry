package Application;

import Pastry.Hashing;
import Pastry.NodeAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class FileCache {
    List<FileReference>     references;
    List<StoredFile>        ownedFiles;
    Semaphore               cacheUpdate;

    public FileCache() {
        this.references = new ArrayList<FileReference>();
        this.ownedFiles = new ArrayList<StoredFile>();
        this.cacheUpdate = new Semaphore(1,true);
    }

    public List<FileReference> getReferences() {
        return references;
    }

    public void setReferences(List<FileReference> references) {
        this.references = references;
    }

    public synchronized void addReference(FileReference ref) {
        System.out.println("Remote Port "+ref.remotePort);
        this.references.add(ref);
    }

    public synchronized void addStoredFile(String f) {
        String hash = Hashing.SHA1_128bit(f);
        this.ownedFiles.add(new StoredFile(f,hash));
    }

    public synchronized void addHelper(NodeAddress na, FileReference oRef){
        for (int i = 0; i < ownedFiles.size(); i++){
            if ( ownedFiles.get(i).fileHash.compareTo(oRef.oid) == 0){
                System.out.println("File "+ownedFiles.get(i).fileName+" is served by "+na.getNodeID());
                ownedFiles.get(i).addRefHolder(na);
            }
        }

    }

    public synchronized String getFileHash(int index) {
        return ownedFiles.get(index).fileHash;
    }

    private class StoredFile {

        public StoredFile(String fileName, String fileHash) {
            this.fileName = fileName;
            this.fileHash = fileHash;
            helpers = new ArrayList<NodeAddress>();
        }

        synchronized void addRefHolder(NodeAddress na){
            helpers.add(na);
        }

        synchronized void removeRefHolder(NodeAddress na){

        }

        String              fileName;
        String              fileHash;
        List<NodeAddress>   helpers;
    }
}


