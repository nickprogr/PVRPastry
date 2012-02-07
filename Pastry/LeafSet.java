//NIKOLAOS VITSAS 3070011
//NIKOLAOS PROMPONAS-KEFALAS 3070172
//PANAGIOTIS ROUSIS 3070149
//POLITIS CHRISTOS 3070169
package Pastry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;


public class LeafSet implements Serializable {

    private ArrayList<NodeAddress> largeSet;
    private ArrayList<NodeAddress> smallSet;
    private int numOfLeaves;

    
    
    
    
    public LeafSet(ArrayList<NodeAddress> largeSet, ArrayList<NodeAddress> smallSet, int numOfLeaves) {
        this.largeSet = largeSet;
        this.smallSet = smallSet;
        this.numOfLeaves = numOfLeaves;
    }

    public LeafSet() {
        //this.smallSet = new NodeAddress[16];
        //this.largeSet = new NodeAddress[16];
        this.smallSet = new ArrayList<NodeAddress>(16);
        this.largeSet = new ArrayList<NodeAddress>(16);
        this.numOfLeaves = 0;
    }






    //getters
    public ArrayList<NodeAddress> getLargeSet() {
        return largeSet;
    }

    public ArrayList<NodeAddress> getSmallSet() {
        return smallSet;
    }

    public NodeAddress getLeafLarge(int i){
        
        try{
            return largeSet.get(i);
        }
        catch(IndexOutOfBoundsException iobe){
            return null;
        }
    }

    public NodeAddress getLeafSmall(int i){
        
        try{
            return smallSet.get(i);
        }
        catch(IndexOutOfBoundsException iobe){
            return null;
        }
    }
    
    public NodeAddress getLeafByID(String nid){
        
        for(NodeAddress na : this.smallSet)
            if(na.getNodeID().equals(nid))
                return na;
        
        for(NodeAddress na : this.largeSet)
            if(na.getNodeID().equals(nid))
                return na;
        
        return null;
    }
    
    public int getNumOfLeaves() {
        return numOfLeaves;
    }
    
    
    
    /**
     * Returns the entry with the minimum nodeID in the entire LeafSet. 
     */
    public NodeAddress getMin(){
        
        if(this.numOfLeaves == 0)
            return null;
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
    public NodeAddress getMax(){
        
        if(this.numOfLeaves == 0)
            return null;
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





    
    //settersArrayList<NodeAddress>
    public void setLargeSet(ArrayList<NodeAddress> largeSet) {
        this.largeSet = largeSet;
    }

    public void setSmallSet(ArrayList<NodeAddress> smallSet) {
        this.smallSet = smallSet;
    }

    public void setLeafLarge(int i, NodeAddress n){
        
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

    public void setLeafSmall(int i, NodeAddress n){
        
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

    public void setNumOfLeaves(int numOfLeaves) {
        this.numOfLeaves = numOfLeaves;
    }




    //sorts
    public void sortSmaller(){
        Collections.sort(smallSet);
    }

    public void sortLarger(){
        Collections.sort(largeSet);
    }



}
