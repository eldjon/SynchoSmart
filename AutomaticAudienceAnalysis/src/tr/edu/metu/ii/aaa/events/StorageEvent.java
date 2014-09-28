package tr.edu.metu.ii.aaa.events;



public class StorageEvent extends AnalysisEvent{

    /**
     * 
     */
    private static final long serialVersionUID = 3825537506678855709L;
    public static final String STORAGE_INIT  = BASE_NAME + "init";
    public static final String STORAGE_FINAL = BASE_NAME + "finalize";

    private String _name = null;
    
    public StorageEvent(Object source,
                        long time, 
                        String name) {

        super(source,
              time);
        _name = name;
    }
    
    public String getName(){
        
        return _name;
    }
    
}
