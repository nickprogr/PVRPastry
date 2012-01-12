package Pastry;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;



public class Utilities {
    
    
    //obtains the node IP address and returns a new NodeAddress object, which contains this IP
    //as an InetAddress object, and a String object of this IP followed by the process ID of this node instance.
    public static NodeAddress obtainIP(){

        try {
            
            String ip = InetAddress.getLocalHost().getHostAddress();

            return new NodeAddress(ip+ManagementFactory.getRuntimeMXBean().getName() , InetAddress.getLocalHost());
            
        }
        catch(UnknownHostException e){
            e.printStackTrace();
        }

        return null;

    }
    
    
}
