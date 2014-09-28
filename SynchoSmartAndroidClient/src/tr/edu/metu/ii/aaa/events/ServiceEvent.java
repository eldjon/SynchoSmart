package tr.edu.metu.ii.aaa.events;

import android.app.Service;


public class ServiceEvent extends AnalysisEvent{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final String DC_SERVICE_CON        = BASE_NAME + "dc_service_connected";
    public static final String DC_SERVICE_DISCON     = BASE_NAME + "dc_service_disconnected";
    public static final String SERVER_SERVICE_CON    = BASE_NAME + "server_service_connected";
    public static final String SERVER_SERVICE_DISCON = BASE_NAME + "server_service_disconnected";
    public static final String FUNF_SERVICE_CON      = BASE_NAME + "funf_service_connected";
    public static final String FUNF_SERVICE_DISCON   = BASE_NAME + "funf_service_disconnected";
    
    private String _name      = null;
    private Class<Service> _service = null;
    
    public ServiceEvent(Object source,
                        long eventTime, 
                        String name) {

        super(source,
              eventTime);
        _name = name;
    }
    
    public String getName(){
       
        return _name;
    }

    public void setService(){
    	
    }
    
    public Class<Service> getService(){
    
    	return _service;
    }
}
