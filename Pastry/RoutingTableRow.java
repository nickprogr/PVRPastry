package Pastry;

import java.io.Serializable;


public class RoutingTableRow implements Serializable {

    private NodeAddress[] row;
    private byte numOfEntries;

    
    
    public RoutingTableRow(NodeAddress[] row) {
        this.row = row;
    }
    
    public RoutingTableRow() {
        this.row = new NodeAddress[16];
        this.numOfEntries = 0;
    }



    
    //getters
    public synchronized NodeAddress[] getRow() {
        return row;
    }

    public synchronized NodeAddress getRowIndex(int index){
        return row[index];
    }

    public synchronized byte getNumOfEntries() {
        return numOfEntries;
    }
    
    



    
    //setters
    public synchronized void setRow(NodeAddress[] row) {
        this.row = row;
    }

    public synchronized void setRowIndex(NodeAddress n, int index){
        row[index] = n;
    }

    public synchronized void setNumOfEntries(byte numOfEntries) {
        this.numOfEntries = numOfEntries;
    }
    
    
    
    
    public synchronized void calculateNumOfEntries(){
        
        this.numOfEntries = 0;
        
        for(NodeAddress na : this.row){
            if(na != null){
                this.numOfEntries++;
            }
        }
    }
    
    
    public synchronized byte numOfEntriesPlusPlus(){
        this.numOfEntries++;
        return this.numOfEntries;
    }
    
    
    public synchronized byte numOfEntriesMinusMinus(){
        this.numOfEntries--;
        return this.numOfEntries;
    }

    
    
}
