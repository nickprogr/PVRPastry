//NIKOLAOS VITSAS 3070011
//NIKOLAOS PROMPONAS-KEFALAS 3070172
//PANAGIOTIS ROUSIS 3070149
//POLITIS CHRISTOS 3070169
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
    public NodeAddress[] getRow() {
        return row;
    }

    public NodeAddress getRowIndex(int index){
        return row[index];
    }

    public byte getNumOfEntries() {
        return numOfEntries;
    }
    
    



    
    //setters
    public void setRow(NodeAddress[] row) {
        this.row = row;
    }

    public void setRowIndex(NodeAddress n, int index){
        row[index] = n;
    }

    public void setNumOfEntries(byte numOfEntries) {
        this.numOfEntries = numOfEntries;
    }
    
    
    
    
    public void calculateNumOfEntries(){
        
        this.numOfEntries = 0;
        
        for(NodeAddress na : this.row){
            if(na != null){
                this.numOfEntries++;
            }
        }
    }
    
    
    public byte numOfEntriesPlusPlus(){
        this.numOfEntries++;
        return this.numOfEntries;
    }

    
    
}
