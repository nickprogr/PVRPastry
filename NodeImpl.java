package Pastry;

public class NodeImpl implements Node {
    
    private NodeAddress address;
    private RoutingTable routingTable;  
    private LeafSet leafSet;
    //private NeighborhoodSet neighborhoodSet;
    
    
    
    public NodeImpl(){
        this.address = Utilities.obtainIP();
    }
    
    
    
    
    //getters
    public NodeAddress getAddress() {
        return address;
    }

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
    public void setAddress(NodeAddress address) {
        this.address = address;
    }

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
