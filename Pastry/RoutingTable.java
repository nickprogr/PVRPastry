//NIKOLAOS VITSAS 3070011
//NIKOLAOS PROMPONAS-KEFALAS 3070172
//PANAGIOTIS ROUSIS 3070149
//POLITIS CHRISTOS 3070169
package Pastry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoutingTable implements Serializable{

    private int numOfEntries;
    private List<RoutingTableRow> table;


    
    

    public RoutingTable() {
        this.table = new ArrayList<RoutingTableRow>();
        this.numOfEntries = 0;
    }


    public RoutingTable(List<RoutingTableRow> rt) {
        this.table = new ArrayList<RoutingTableRow>(rt);
        this.numOfEntries = rt.size();
    }


    public RoutingTable(int numOfEntries, List<RoutingTableRow> rt) {
        this.table = new ArrayList<RoutingTableRow>(rt);
        this.numOfEntries = numOfEntries;
    }





    //getters
    public List<RoutingTableRow> getTable() {
        return table;
    }

    
    public RoutingTableRow getTableRow(int index){
        
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

    
    public int getNumOfEntries() {
        return numOfEntries;
    }
    
    
    /* Returns the element at row 'x' and collumn 'y' of the routing table. */
    public NodeAddress getEntry(int x , int y){

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
    
    

    


    //setters
    public void setTable(List<RoutingTableRow> rt) {
        this.table = new ArrayList<RoutingTableRow>(rt);
    }

    
    public void setTableRow(int index, RoutingTableRow row){
        
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

    
    public void setNumOfEntries(int numOfEntries) {
        this.numOfEntries = numOfEntries;
    }
    
    
    
    
    
    //exists
    public boolean exists(NodeAddress addr){
        
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
    
    
    public void calculateNumOfEntries(){
        
        this.numOfEntries = 0;
        
        for(RoutingTableRow rtr : this.table){
            if(rtr != null){
                this.numOfEntries += rtr.getNumOfEntries();
            }
        }
    }
    
    
    public int numOfEntriesPlusPlus(){
        this.numOfEntries++;
        return this.numOfEntries;
    }

    
}
