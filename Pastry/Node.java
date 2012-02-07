package Pastry;

public class Node {
    
    private NodeAddress address;
    private State state;
    
    
    
    public Node(){
        this.address = Utilities.obtainIP();
        this.state = new State();
    }
    
    
    
    
    //getters
    public NodeAddress getAddress() {
        return address;
    }

    public State getState() {
        return state;
    }
    
    
    
    
    
    //setters
    public void setAddress(NodeAddress address) {
        this.address = address;
    }

    public void setState(State state) {
        this.state = state;
    }
    
    
    
}
