package tr.edu.metu.ii.aaa.events;

import tr.edu.metu.ii.aaa.messages.ServerMessage;


public class SocketEvent extends AnalysisEvent{

    /**
     * 
     */
    private static final long serialVersionUID = -5785900357093891879L;

    // socket events
    public static final String SOCKET_START    = BASE_NAME + "socket_start";
    public static final String SOCKET_CONNECT  = BASE_NAME + "socket_connect";
    public static final String SOCKET_STOP     = BASE_NAME + "socket_stop";
    // socket message event
    public static final String MSG_START       = BASE_NAME + "start";
    public static final String MSG_PAUSE       = BASE_NAME + "pause";
    public static final String MSG_RESUME      = BASE_NAME + "resume";
    public static final String MSG_STOP        = BASE_NAME + "stop";
    public static final String MSG_EMPTY       = BASE_NAME + "empty";

    protected String        _name;
    protected ServerMessage _msg; 
    
    public SocketEvent(Object source, long time, String name) {

        super(source, time);
        _name = name;
    }
    
    public String getName(){
        
        return _name;
    }
    
    public ServerMessage getMsg(){
        
        return _msg;
    }
    
    public void setMsg(ServerMessage msg){
        
        _msg = msg;
    }
}
