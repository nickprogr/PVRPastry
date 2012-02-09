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
    private long timeLastUpdated; /* timestamp in milliseconds. refers to LeafSet updates.*/

    
    
    
    public State(RoutingTable routingTable, LeafSet leafSet, long ts) {
        this.routingTable = routingTable;
        this.leafSet = leafSet;
        this.timeLastUpdated = ts;
    }
    
    public State(){
        this.routingTable = new RoutingTable();
        this.leafSet = new LeafSet();
        this.timeLastUpdated = 0;
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

    public long getTimeLastUpdated() {
        return timeLastUpdated;
    }
    
    
    
    
    
    
    
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

    public void setTimeLastUpdated(long timeLastUpdated) {
        this.timeLastUpdated = timeLastUpdated;
    }
    
    
     
}
