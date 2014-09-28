package tr.edu.metu.thesis.events;

import java.util.EventObject;

public abstract class ServerEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2134134330775089676L;
	protected static final String BASE_NAME = "tr.edu.metu.thesis.events.";

	private String   _name;
	protected Object _result = null;

	public ServerEvent(Object source, String name) {
		super(source);
		_name = name;
	}

	public String getName(){
		
		return _name;
	}
	
	public void setResult(Object result){
		
		_result = result;
	}
	
	public Object getResult(){
		
		return _result;
	}
}
