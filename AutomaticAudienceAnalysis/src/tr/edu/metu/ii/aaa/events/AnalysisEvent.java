package tr.edu.metu.ii.aaa.events;

import java.util.EventObject;


public abstract class AnalysisEvent extends EventObject{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected static final String BASE_NAME = "tr.edu.metu.ii.aaa.event.";
    protected int                _flags     = 0;
    protected long               _eventTime = -1;
 
    public AnalysisEvent(Object source, long eventTime) {

        super(source);
        _eventTime = eventTime;
    }

    public long getEventTime(){
        
        return _eventTime;
    }
    
    public void setFlag(int flag){
        
        _flags &= flag;
    }
    
    public boolean isFlagSet(int flag){
        
        return ((_flags & flag) > 0) ? true : false;
    }
    
    public void resetFlag(int flag){
        
        _flags &= (~flag);
    }
}
