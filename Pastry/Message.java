package Pastry;

import java.io.Serializable;



public class Message implements Serializable{
    
    private String msg, key;
    private NodeAddress addr;
    private Object o;
    private long timestamp;  /* timestamp in milliseconds. */

    
    
    public Message(String msg, NodeAddress addr, Object o, String key, long ts) {
        this.msg = msg;
        this.addr = addr;
        this.o = o;
        this.key = key;
        this.timestamp = ts;
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

    public long getTimestamp() {
        return timestamp;
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

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    
     
    
}
