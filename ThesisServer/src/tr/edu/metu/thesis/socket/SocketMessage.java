package tr.edu.metu.thesis.socket;

import tr.edu.metu.thesis.socket.ServerMessage.SocketMessageType;

public interface SocketMessage {

	public byte[] getMsgInBytes();
	
	public int getSizeInBytes();
	
	public long getExecTime();
	
	public void setTime(long delay);
	
	public SocketMessageType getType();
}
