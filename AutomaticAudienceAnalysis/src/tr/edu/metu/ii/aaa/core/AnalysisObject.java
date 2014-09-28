package tr.edu.metu.ii.aaa.core;

import tr.edu.metu.ii.aaa.events.AnalysisEvent;
import tr.edu.metu.ii.aaa.events.AnalysisEventListener;


/**
 * This is the basic object of the application. Common functionalities of object implementing 
 * <code>AnalysisObject</code> include firing <code>AnalysisEvent</code>s such as SocketEvent,
 * DataCollectionEvent, CacheEvent etc.
 * 
 * 
 * @author eldi
 *
 */
public interface AnalysisObject {
    
    public void fireEvent(AnalysisEvent event);
    
    public void registerAnalysisListener(AnalysisEventListener listener);
    
    public void unregisterAnalysisListener(AnalysisEventListener listener);
    
    public void clearListeners();
}
