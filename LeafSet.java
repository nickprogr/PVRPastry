//NIKOLAOS VITSAS 3070011
//NIKOLAOS PROMPONAS-KEFALAS 3070172
//PANAGIOTIS ROUSIS 3070149
//POLITIS CHRISTOS 3070169
package Pastry;

import java.io.Serializable;
import java.util.Arrays;


public class LeafSet implements Serializable {

    private NodeAddress[] largeSet;
    private NodeAddress[] smallSet;
    private int numOfLeaves;

    
    
    
    
    public LeafSet(NodeAddress[] largeSet, NodeAddress[] smallSet, int numOfLeaves) {
        this.largeSet = largeSet;
        this.smallSet = smallSet;
        this.numOfLeaves = numOfLeaves;
    }

    public LeafSet() {
        this.smallSet = new NodeAddress[16];
        this.largeSet = new NodeAddress[16];
        this.numOfLeaves = 0;
    }






    //getters
    public NodeAddress[] getLargeSet() {
        return largeSet;
    }

    public NodeAddress[] getSmallSet() {
        return smallSet;
    }

    public NodeAddress getLeafLarge(int i){
        return largeSet[i];
    }

    public NodeAddress getLeafSmall(int i){
        return smallSet[i];
    }
    
    public int getNumOfLeaves() {
        return numOfLeaves;
    }





    
    //setters
    public void setLargeSet(NodeAddress[] largeSet) {
        this.largeSet = largeSet;
    }

    public void setSmallSet(NodeAddress[] smallSet) {
        this.smallSet = smallSet;
    }

    public void setLeafLarge(int i, NodeAddress n){
        this.largeSet[i]=n;
    }

    public void setLeafSmall(int i, NodeAddress n){
        this.smallSet[i]=n;
    }

    public void setNumOfLeaves(int numOfLeaves) {
        this.numOfLeaves = numOfLeaves;
    }




    //sorts
    public void sortSmaller(){
        Arrays.sort(smallSet);
    }

    public void sortLarger(){
        Arrays.sort(largeSet);
    }



}
