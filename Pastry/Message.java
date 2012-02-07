package Pastry;

/**
 * 
 * @author nickprogr
 */

import java.io.Serializable;



public class Message implements Serializable{
    
    private String msg, key;
    private NodeAddress addr;
    private Object o;

    
    
    public Message(String msg, NodeAddress addr, Object o, String key) {
        this.msg = msg;
        this.addr = addr;
        this.o = o;
        this.key = key;
    }

    
    

    //getters
    public String getMsg() {
        return msg;
    }
    
    public NodeAddress getAddress() {
        return addr;
    }

    public Object getObject() {
        return o;
    }

    public String getKey() {
        return key;
    }

    
    

    
    
    
    //setters
    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public void setAddress(NodeAddress addr) {
        this.addr = addr;
    }

    public void setObject(Object o) {
        this.o = o;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    
     
    
}
