package tr.edu.metu.ii.aaa.core;

import java.util.ArrayList;
import java.util.List;

import tr.edu.metu.ii.aaa.events.AnalysisEvent;
import tr.edu.metu.ii.aaa.events.AnalysisEventListener;


public class BaseAnalysisObject extends Object implements AnalysisObject{
    
    protected List<AnalysisEventListener> _eventListeners;
    
    public BaseAnalysisObject(){
        
        _eventListeners = new ArrayList<AnalysisEventListener>();
    }
    
    @Override
    public void fireEvent(AnalysisEvent event){
        
        for(AnalysisEventListener sel: _eventListeners)
            sel.onAnalysisEvent(event);
    }
    
    @Override
    public void registerAnalysisListener(AnalysisEventListener listener){
        
        synchronized (_eventListeners) {
            
            if(_eventListeners.contains(listener))
                return;
            _eventListeners.add(listener);
        }
    }
    
    @Override
    public void unregisterAnalysisListener(AnalysisEventListener listener){
        
        synchronized (_eventListeners) {
            
            _eventListeners.remove(listener);
        }
    }

    @Override
    public void clearListeners() {

        synchronized (_eventListeners) {
            
            _eventListeners.clear();
        }
    }
}
