package tr.edu.metu.thesis.events;

import java.nio.channels.SocketChannel;

import tr.edu.metu.thesis.jpa.Device;
import tr.edu.metu.thesis.socket.SocketMessage;

public class SocketEvent extends ServerEvent{

	private static final long serialVersionUID = -1695262959528948077L;
	protected static final String EVENT_NAME   = BASE_NAME + "socketevent.";

	public static final String EVENT_SOCKET_OPENED       = EVENT_NAME + "socketopened";
	public static final String EVENT_SOCKET_CLOSED       = EVENT_NAME + "socketclosed";
	public static final String EVENT_CLIENT_CONNECTED    = EVENT_NAME + "clientconnected";
	public static final String EVENT_CLIENT_DISCONNECTED = EVENT_NAME + "clientdisconnected";
	public static final String EVENT_MSG_SENT            = EVENT_NAME + "messagesent";
	
	protected SocketMessage _msg;
	protected SocketChannel _channel;
	protected Device        _device;
	
	public SocketEvent(Object source, String name) {
		
		super(source, name);
	}

	public SocketEvent(Object source, 
					   String name, 
					   SocketMessage msg){
		
		super(source, name);
		_msg = msg;
	}
	
	public SocketMessage getMessage(){
		
		return _msg;
	}
	
	public void setSocketChannel(SocketChannel channel){
		
		_channel = channel;
	}
	
	public SocketChannel getSocketChannel(){
		
		return _channel;
	}
	
	public void setDevice(Device device){
		
		_device = device;
	}
	
	public Device getDevice(){
		
		return _device;
	}
}
