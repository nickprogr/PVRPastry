package Pastry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;

public class LeafSet implements Serializable {

    private ArrayList<NodeAddress> largeSet;
    private ArrayList<NodeAddress> smallSet;
    private int numOfLeaves;
    private final Semaphore binarySemaphore;

    
    
    
    
    public LeafSet(ArrayList<NodeAddress> largeSet, ArrayList<NodeAddress> smallSet, int numOfLeaves) {
        this.largeSet = largeSet;
        this.smallSet = smallSet;
        this.numOfLeaves = numOfLeaves;
        this.binarySemaphore = new Semaphore(1, true);  /* Semaphore to ensure that only 1 procedure at a time */
                                                        /* will access this instance. */
    }

    public LeafSet() {
        this.smallSet = new ArrayList<NodeAddress>(16);
        this.largeSet = new ArrayList<NodeAddress>(16);
        this.numOfLeaves = 0;
        this.binarySemaphore = new Semaphore(1, true);
    }






    //getters
    public synchronized ArrayList<NodeAddress> getLargeSet() {
        return largeSet;
    }

    public synchronized ArrayList<NodeAddress> getSmallSet() {
        return smallSet;
    }

    public synchronized NodeAddress getLeafLarge(int i){
        
        try{
            return largeSet.get(i);
        }
        catch(IndexOutOfBoundsException iobe){
            return null;
        }
    }

    public synchronized NodeAddress getLeafSmall(int i){
        
        try{
            return smallSet.get(i);
        }
        catch(IndexOutOfBoundsException iobe){
            return null;
        }
    }
    
    public synchronized NodeAddress getLeafByID(String nid){
        
        for(NodeAddress na : this.smallSet)
            if(na.getNodeID().equals(nid))
                return na;
        
        for(NodeAddress na : this.largeSet)
            if(na.getNodeID().equals(nid))
                return na;
        
        return null;
    }
    
    public synchronized int getNumOfLeaves() {
        return numOfLeaves;
    }
    
    
    
    /**
     * Returns the entry with the minimum nodeID in the entire LeafSet. 
     */
    public synchronized NodeAddress getMin() throws NullPointerException{
        
        if(this.numOfLeaves == 0)
            throw new NullPointerException();
        else{
            
            if(!this.smallSet.isEmpty())
                return this.getLeafSmall(0);
            else
                return this.getLeafLarge(0);
        }
        
    }
    
    
    
    /**
     * Returns the entry with the maximum nodeID in the entire LeafSet. 
     */
    public synchronized NodeAddress getMax() throws NullPointerException{
        
        if(this.numOfLeaves == 0)
            throw new NullPointerException();
        else{
            
            if(!this.largeSet.isEmpty()){
                
                byte i=-1;
                
                for(i=1; i<16; i++){
                    if(this.getLeafLarge(i) == null)
                        return this.getLeafLarge(i-1);
                }
                
                return this.getLeafLarge(15);
                
            }
            else{
                
                byte i=-1;
                
                for(i=1; i<16; i++){
                    if(this.getLeafSmall(i) == null)
                        return this.getLeafSmall(i-1);
                }
                
                return this.getLeafSmall(15);
                
            }
            
        }
        
    }

    public Semaphore getBinarySemaphore() {
        return binarySemaphore;
    }

    



    
    //setters
    public synchronized void setLargeSet(ArrayList<NodeAddress> largeSet) {
        this.largeSet = largeSet;
    }

    public synchronized void setSmallSet(ArrayList<NodeAddress> smallSet) {
        this.smallSet = smallSet;
    }

    public synchronized void setLeafLarge(int i, NodeAddress n){
        try{
            this.largeSet.set(i, n);
        }
        catch(IndexOutOfBoundsException iobe){
            
            int s = this.largeSet.size()-1;
            int t = i-s;
            
            for(int j=0;j<t;j++)
                this.largeSet.add(null);
            
            this.largeSet.set(i, n);
            
        }
    }

    public synchronized void setLeafSmall(int i, NodeAddress n){
        try{
            this.smallSet.set(i, n);
        }
        catch(IndexOutOfBoundsException iobe){
            
            int s = this.smallSet.size()-1;
            int t = i-s;
            
            for(int j=0;j<t;j++)
                this.smallSet.add(null);
            
            this.smallSet.set(i, n);
            
        }
    }

    public synchronized void setNumOfLeaves(int numOfLeaves) {
        this.numOfLeaves = numOfLeaves;
    }




    //sorts
    public synchronized void sortSmaller(){
        Collections.sort(smallSet);
    }

    public synchronized void sortLarger(){
        Collections.sort(largeSet);
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

    
    
    public boolean contains(NodeAddress addr){
        
        String id = addr.getNodeID();
        
        for(NodeAddress na : this.largeSet)
            if(na.getNodeID().equals(id))
                return true;
        
        for(NodeAddress na : this.smallSet)
            if(na.getNodeID().equals(id))
                return true;
        
        
        return false;
    }
    
    
    
    public boolean remove(NodeAddress addr){
        
        String id = addr.getNodeID();
        
        for(NodeAddress na : this.largeSet){
            if(na.getNodeID().equals(id)){
                this.largeSet.remove(na);
                return true;
            }
        }
        
        for(NodeAddress na : this.smallSet){
            if(na.getNodeID().equals(id)){
                this.smallSet.remove(na);
                return true;
            }
        }
        
        
        return false;
        
    }
    
    
}
