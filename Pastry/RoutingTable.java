package Pastry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class RoutingTable implements Serializable{

    private int numOfEntries;
    private List<RoutingTableRow> table;
    private final Semaphore binarySemaphore;    /* Semaphore to ensure that only 1 procedure at a time */
                                                /* will access this instance. */


    
    

    public RoutingTable() {
        this.table = new ArrayList<RoutingTableRow>();
        this.numOfEntries = 0;
        this.binarySemaphore = new Semaphore(1, true);
    }


    public RoutingTable(List<RoutingTableRow> rt) {
        this.table = new ArrayList<RoutingTableRow>(rt);
        this.numOfEntries = rt.size();
        this.binarySemaphore = new Semaphore(1, true);
    }


    public RoutingTable(int numOfEntries, List<RoutingTableRow> rt) {
        this.table = new ArrayList<RoutingTableRow>(rt);
        this.numOfEntries = numOfEntries;
        this.binarySemaphore = new Semaphore(1, true);
    }





    //getters
    public synchronized List<RoutingTableRow> getTable() {
        return table;
    }

    
    public synchronized RoutingTableRow getTableRow(int index){
        
        try{
            return table.get(index);
        }
        catch(IndexOutOfBoundsException iobe){
            
            int s = this.table.size()-1;
            int t = index-s;
            
            for(int j=0;j<t;j++)
                table.add(new RoutingTableRow());
            
            return table.get(index);
            
        }
    }

    
    public synchronized int getNumOfEntries() {
        return numOfEntries;
    }
    
    
    /* Returns the element at row 'x' and collumn 'y' of the routing table. */
    public synchronized NodeAddress getEntry(int x , int y){

        if(x > this.table.size() || y > 15)
            return null;

        try{
            return this.table.get(x).getRowIndex(y);
        }
        catch(IndexOutOfBoundsException iobe){
            
            /*int s = this.table.size()-1;
            int t = x-s;
            
            for(int j=0;j<t;j++)
                table.add(new RoutingTableRow());
            
            return this.table.get(x).getRowIndex(y);*/
            return null;
            
        }
    }
    
    public Semaphore getBinarySemaphore() {
        return binarySemaphore;
    }
    
    

    


    //setters
    public synchronized void setTable(List<RoutingTableRow> rt) {
        this.table = new ArrayList<RoutingTableRow>(rt);
    }

    
    public synchronized void setTableRow(int index, RoutingTableRow row){
        
        try{
            table.set(index, row);
        }
        catch(IndexOutOfBoundsException iobe){
            
            int s = this.table.size()-1;
            int t = index-s;
            
            for(int j=0;j<t;j++)
                table.add(new RoutingTableRow());
            
            table.set(index, row);
            
        }
    }

    
    public synchronized void setNumOfEntries(int numOfEntries) {
        this.numOfEntries = numOfEntries;
    }
    
    
    
    
    
    //exists
    public synchronized boolean exists(NodeAddress addr){
        
        for(RoutingTableRow rtr : this.table){
            for(NodeAddress na : rtr.getRow()){
                if(na != null){
                    if(na.getNodeID().equals(addr.getNodeID())){
                        return true;
                    }
                }
            }
        }
        
        return false;
        
    }
    
    
    
    
    
    public synchronized void calculateNumOfEntries(){
        
        this.numOfEntries = 0;
        
        for(RoutingTableRow rtr : this.table){
            if(rtr != null){
                this.numOfEntries += rtr.getNumOfEntries();
            }
        }
    }
    
    
    public synchronized int numOfEntriesPlusPlus(){
        this.numOfEntries++;
        return this.numOfEntries;
    }

    
    public synchronized int numOfEntriesMinusMinus(){
        this.numOfEntries--;
        return this.numOfEntries;
    }
    
    
    
    
    public void semAcquire(){
        try{
            this.binarySemaphore.acquire();
        }
        catch(InterruptedException ie){ie.printStackTrace();}
    }
    
    public void semRelease(){
        this.binarySemaphore.release();
    }
}
