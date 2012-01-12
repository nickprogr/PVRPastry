package Pastry;

public interface Node {
    
    public NodeAddress getAddress();
    public RoutingTable getRoutingTable();
    public LeafSet getLeafSet();
    //public NeighborhoodSet getNeighborhoodSet();
    
    public void setAddress(NodeAddress address);
    public void setRoutingTable(RoutingTable routingTable);
    public void setLeafSet(LeafSet leafSet);
    //public void setNeighborhoodSet(NeighborhoodSet neighborhoodSet);
    
}
