//NIKOLAOS VITSAS 3070011
//NIKOLAOS PROMPONAS-KEFALAS 3070172
//PANAGIOTIS ROUSIS 3070149
//POLITIS CHRISTOS 3070169
package Pastry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoutingTable implements Serializable{

    int numOfEntries;
    List<RoutingTableRow> table;


    
    

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
        return table.get(index);
    }

    


    //setters
    public void setTable(List<RoutingTableRow> rt) {
        this.table = new ArrayList<RoutingTableRow>(rt);
    }

    public void setTableRow(int index, RoutingTableRow row){
        table.set(index, row);
    }

    
}
