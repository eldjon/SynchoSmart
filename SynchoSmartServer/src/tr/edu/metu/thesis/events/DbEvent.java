package tr.edu.metu.thesis.events;

import tr.edu.metu.thesis.jpa.Device;

public class DbEvent extends ServerEvent{

	private static final long serialVersionUID      = -4503216280182341595L;
	protected static String DB_EVENT_NAME           = BASE_NAME + "dbevent.";
	public static String ACTIVE_SURVEY_DEVICE_ADDED = DB_EVENT_NAME + "deviceadd";
	public static String ACTIVE_SURVEY_DEVICE_REMOVED = DB_EVENT_NAME + "deviceremoved";
	
	private Device _device;
	
	public DbEvent(Object source, String name, Device device) {
		super(source, name);
		_device = device;
	}
	
	public Device getDevice(){
		
		return _device;
	}
}
