package Pastry;

import java.io.Serializable;

/**
 *
 * @author nickprogr
 */


public class State implements Serializable{
    
    private RoutingTable routingTable; 
    private LeafSet leafSet;
    //private NeighborhoodSet neighborhoodSet;

    
    
    
    public State(RoutingTable routingTable, LeafSet leafSet) {
        this.routingTable = routingTable;
        this.leafSet = leafSet;
    }
    
    public State(){
        this.routingTable = new RoutingTable();
        this.leafSet = new LeafSet();
    }
    
    
    
    
    
    //getters
    public RoutingTable getRoutingTable() {
        return routingTable;
    }
    
    public LeafSet getLeafSet() {
        return leafSet;
    }
    
    //public NeighborhoodSet getNeighborhoodSet() {
    //    return neighborhoodSet;
    //}
    
    
    
    
    
    //setters
    public void setRoutingTable(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }
    
    public void setLeafSet(LeafSet leafSet) {
        this.leafSet = leafSet;
    }
    
    //public void setNeighborhoodSet(NeighborhoodSet neighborhoodSet) {
    //    this.neighborhoodSet = neighborhoodSet;
    //}
    
     
}
