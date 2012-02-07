package Pastry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;



public class Utilities {
    
    
    /** 
     *  Obtains the node IP address and returns a new NodeAddress object, which contains this IP
     *  as an InetAddress object, a String object of this IP followed by the process ID of this node instance,
     *  and an integer number for the socket port of the node. As socket number we use the process ID of the
     *  application.
     */
    public static NodeAddress obtainIP(){
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            
            int in = ManagementFactory.getRuntimeMXBean().getName().indexOf("@");
            int port = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().substring(0, in))+10000;

            return new NodeAddress(ip+ManagementFactory.getRuntimeMXBean().getName() , InetAddress.getLocalHost(), port);
        }
        catch(UnknownHostException e){
            e.printStackTrace();
        }

        return null;
    }
    
    
    
    
    /**
     * Converts a serializable object into a byte array.
     */
    public static byte[] objectToByteArray(Object o) throws IOException {

        Serializable serObj = (Serializable) o;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(serObj);

        byte[] bytes = baos.toByteArray();

        return bytes;

    }
    
    
    
    
    /**
     * Converts a byte array into a serializable object.
     */
    public static Object byteArrayToObject(byte[] bytes) throws IOException, ClassNotFoundException {

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Serializable o = (Serializable)ois.readObject();
        
        return o;
        
    }
  
}
