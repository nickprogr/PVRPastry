package Pastry;

public class AppInstance {
    
    public static NodeImpl myNode;
    
    
    public static void launchApp(){
        
        myNode = new NodeImpl();
        
        //bootstrap//
        //NodeInfo bootstrapInfo = MakeBootstrapRequest.makeRequest();
        
    }
    
    
    
    public static void main(String args[]){
        launchApp();
        
        //...//
        System.out.println(myNode.getAddress().getNodeID());
    }
    
    
}
