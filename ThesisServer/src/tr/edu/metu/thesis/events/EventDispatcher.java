package tr.edu.metu.thesis.events;

import java.util.HashSet;
import java.util.Set;

public class EventDispatcher {

	private static EventDispatcher _instance;
	
	private Set<ServerEventListener> _listeners;
	
	private EventDispatcher(){
		init();
	}
	
	public static EventDispatcher getInstance(){
		
		if(_instance == null)
			_instance = new EventDispatcher();
		
		return _instance;
	}
	
	protected void init(){
		
		_listeners = new HashSet<ServerEventListener>();
	}
	
	public void registerListener(ServerEventListener listener){
		
		_listeners.add(listener);
	}
	
	public void unregisterListener(ServerEventListener listener){
		
		_listeners.remove(listener);
	}
	
	public void fireEvent(ServerEvent event){
		for(ServerEventListener l : _listeners){
			l.onServerEvent(event);
		}
	}
}

