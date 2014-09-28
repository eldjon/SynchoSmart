package tr.edu.metu.ii.aaa.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class ServerMessage {
    
    public static final int MESSAGE_SIZE = 9; // in bytes
    private byte _type;
    private long _time;
    
    public ServerMessage(byte[] data){
        
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        _type  = bb.get();
        _time = bb.getLong();
    }
    
    public int getType(){
        
        return _type;
    }
    
    public long getTime(){
        
        return _time;
    }
    
    public static enum SocketMessageType {
        
        START(1),
        STOP(2),
        PAUSE(3),
        RESUME(4);
        
        int _msg ;
        
        private SocketMessageType(int msg){
            _msg = msg;
        }
        
        public int getMsg(){
            
            return _msg;
        }
    }    
}
