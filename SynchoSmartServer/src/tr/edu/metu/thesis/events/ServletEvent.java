package tr.edu.metu.thesis.events;

import java.util.HashMap;

public class ServletEvent extends ServerEvent{

	private static final long serialVersionUID = -2706313905836383120L;

	public static final String SERVLET_EVENT_NAME = "servlet_event";
	
	private String _imei;
	private HashMap<String, Object> _extras;
	
	public ServletEvent(Object source, String imei) {
		
		super(source, SERVLET_EVENT_NAME);
		_imei   = imei;
		_extras = new HashMap<String, Object>();
	}

	public Object getExtra(String key){
		
		return _extras.get(key);
	}
	
	public void putExtra(String key, Object value){
		
		_extras.put(key, value);
	}
	
	public String getCallingImei(){
		
		return _imei;
	}
}
