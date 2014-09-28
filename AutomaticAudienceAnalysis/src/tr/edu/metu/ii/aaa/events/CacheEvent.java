package tr.edu.metu.ii.aaa.events;



public class CacheEvent extends AnalysisEvent{

    /**
     * 
     */
    private static final long serialVersionUID        = 526515209357761664L;
    protected static final String CACHE_EVENT_NAME    = BASE_NAME 
                                                        + "cacheevent";
    public static final String INIT_STORAGE           = CACHE_EVENT_NAME 
                                                        + ".init_storage";
    public static final String START_STORAGE          = CACHE_EVENT_NAME 
                                                        + ".start_storage";
    public static final String STOP_STORAGE           = CACHE_EVENT_NAME 
                                                        + ".stop_storage";
    public static final String START_FINALIZE_STORAGE = CACHE_EVENT_NAME 
                                                        + ".start_finalize_storage";
    public static final String FINALIZE_STORAGE_DONE  = CACHE_EVENT_NAME 
                                                        + ".stop_finalize_storage";
    public static final String DATA_FILES_CLEARED     = CACHE_EVENT_NAME 
                                                        + ".data_files_cleared";
    private String _name = null;
    
    public CacheEvent(Object source, 
                      long time,
                      String name) {

        super(source,time);
        _name = name;
    }
    
    public String getName(){
        
        return _name;
    }
}
