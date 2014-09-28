package tr.edu.metu.thesis.socket;

import java.nio.ByteBuffer;


public class ServerMessage implements SocketMessage{
	
	protected SocketMessageType _type;
	protected long              _execTime; // in milliseconds
	

	public ServerMessage(SocketMessageType type){
		
		_type     = type;
		_execTime = -1;
	}
	
	public ServerMessage(SocketMessageType type, 
						 long execTime){
		_type     = type;
		_execTime = execTime;
	}
	
	public long getExecTime(){
		
		return _execTime;
	}

	@Override
	public void setTime(long delay) {
		
		_execTime = delay;
	}

	@Override
	public SocketMessageType getType() {
		
		return _type;
	}
	
	public String getName(){
		
		return _type.name();
	}
	
	/**
	 * Return the size of the message in bytes. This includes the message itself
	 * as it will be send through sockets to clients
	 */
	public int getSizeInBytes(){
		
		return 1 + 8;  // 1 byte for type and 8 for time
	}
	
	public byte[] getMsgInBytes(){
		
		ByteBuffer bb = ByteBuffer.allocate(getSizeInBytes());
		bb.put((byte)_type.getMsg());
		bb.putLong(_execTime);
		bb.flip();
		return bb.array();
	}
	
	public static enum SocketMessageType {
	
		START(1),
		STOP(2),
		PAUSE(3),
		RESUME(4),
		EMPTY(-1);
		
		int _msg ;
		
		private SocketMessageType(int msg){
			_msg = msg;
		}
		
		public int getMsg(){
			
			return _msg;
		}
		
		public static SocketMessageType findSocketMT(int type){
			
			for(SocketMessageType smt:SocketMessageType.values())
				if(smt.getMsg() == type)
					return smt;
			
			return null;
		}
	}
}
