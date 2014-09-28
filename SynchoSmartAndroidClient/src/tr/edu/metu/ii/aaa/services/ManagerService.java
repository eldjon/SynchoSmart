package tr.edu.metu.ii.aaa.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tr.edu.metu.ii.aaa.core.AnalysisApp;
import tr.edu.metu.ii.aaa.core.AnalysisObject;
import tr.edu.metu.ii.aaa.events.AnalysisEvent;
import tr.edu.metu.ii.aaa.events.AnalysisEventListener;
import tr.edu.metu.ii.aaa.events.CacheEvent;
import tr.edu.metu.ii.aaa.events.ServiceEvent;
import tr.edu.metu.ii.aaa.events.SoapEvent;
import tr.edu.metu.ii.aaa.events.SoapEvent.FileUploadStatus;
import tr.edu.metu.ii.aaa.events.SocketEvent;
import tr.edu.metu.ii.aaa.events.StorageEvent;
import tr.edu.metu.ii.aaa.services.DataCollectorService.DCSBinder;
import tr.edu.metu.ii.aaa.services.ServerComService.ServerComBinder;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;

/**
 * This is the service that will manage the life-cycle of DataCollectorService
 * and ConnectionService. Start/Stop tasks will be handled here. First Service 
 * to connect is the FUNF service. After FUNF service is connected we connect
 * the other 2 services (DataCollectorService and ServerComService).
 * @author eldi
 *
 */
public class ManagerService extends Service implements AnalysisEventListener, AnalysisObject{

    public static final String EXPERIMENT_PIPELINE = "experiment_pipeline";
    
    // services
    private DataCollectorService _dcService;
    private volatile boolean     _dcSBind;
    private ServerComService     _serverCService;
    private volatile boolean     _serverCSBind;
    
    private List<AnalysisEventListener> _listeners;
    
    private AnalysisApp          _app               = null;
    private Binder               _managerSBinder    = new ManagerSBinder();
    
    private volatile boolean     _isCollectingData  = false;
    private volatile boolean     _isSocketConnected = false;
    private volatile boolean     _isUploadingData   = false;
    private WifiLock             _wifiLock          = null;
    
    
    private ServiceConnection _serverComConn = new ServiceConnection() {
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            
            _serverCSBind = false;
            _serverCService.unregisterAnalysisListener(ManagerService.this);
            // fire an event notifying that the ServerComService is connected
            fireEvent(new ServiceEvent(ManagerService.this, 
                                       System.currentTimeMillis(), 
                                       ServiceEvent.SERVER_SERVICE_DISCON));
        }
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            
            synchronized (this) {
                _serverCService = ((ServerComBinder)service).getService();
                if(_serverCService != null){
                    
                    _serverCSBind = true;
                    _serverCService.registerAnalysisListener(ManagerService.this);
                    // fire an event notifying that the ServerComService is connected
                    fireEvent(new ServiceEvent(ManagerService.this, 
                                               System.currentTimeMillis(), 
                                               ServiceEvent.SERVER_SERVICE_CON));
                }
            }
        }
    };
    
    
    private ServiceConnection _dataServiceConn = new ServiceConnection() {
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
    
            _dcSBind = false;
            _dcService.unregisterAnalysisListener(ManagerService.this);
            // fire an event notifying that the ServerComService is connected
            fireEvent(new ServiceEvent(ManagerService.this, 
                                       System.currentTimeMillis(), 
                                       ServiceEvent.DC_SERVICE_DISCON));
        }
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
    
            synchronized (this) {
                
                _dcService = ((DCSBinder)service).getService();
                if(_dcService != null){
                    
                    _dcSBind = true;
                    _dcService.registerAnalysisListener(ManagerService.this);
                    
                    // fire an event notifying that the ServerComService is connected
                    fireEvent(new ServiceEvent(ManagerService.this, 
                                               System.currentTimeMillis(), 
                                               ServiceEvent.DC_SERVICE_CON));
                }
            }
        }
    };

 
    @Override
    public IBinder onBind(Intent intent) {

        return _managerSBinder;
    }
    
    // ******************************************************************************* //
    // ******************************* CREATE/START/DESTROY ************************** //
    // ******************************************************************************* //
    @Override
    public void onCreate() {

        super.onCreate();
        _app         = (AnalysisApp) getApplication();
        _listeners   = new ArrayList<AnalysisEventListener>();
        connectServices();
        // get the WIFI lock and make sure it will not be off until the end of service
        Settings.System.putInt(getContentResolver(), 
        					   Settings.System.WIFI_SLEEP_POLICY,
        					   Settings.System.WIFI_SLEEP_POLICY_NEVER);
        WifiManager wifiM = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        _wifiLock         = wifiM.createWifiLock(WifiManager.WIFI_MODE_FULL, 
        										 "AAA_WIFI_LOCK");
        _wifiLock.acquire();
        System.out.println(" ***************************************************** ");
        System.out.println(" ************* MANAGER SERVICE CREATED *************** ");
        System.out.println(" ***************************************************** ");
    }
    
    protected void connectServices(){
        
        bindService(new Intent(ManagerService.this,
                               DataCollectorService.class), 
                               _dataServiceConn,
                    Context.BIND_AUTO_CREATE);
        bindService(new Intent(ManagerService.this,
                               ServerComService.class), 
                               _serverComConn,
                    Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {

        System.out.println(" ***************************************************** ");
        System.out.println(" ************* MANAGER SERVICE DESTROYED ************* ");
        System.out.println(" ***************************************************** ");

        stopDataService();
        stopServerService();
        _wifiLock.release();
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {

        super.onStart(intent,
                      startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent,
                                    flags,
                                    startId);
    }
    
    // ******************************************************************************* //
    // *************************** MANAGE DATA & CONNECTION ************************** //
    // ******************************************************************************* //
    public void startDataCollection(){
        
        synchronized (this) {
    	   if(_dcService != null && !_isCollectingData){
    		   
               _dcService.startDataCollection();
               _isCollectingData = true;
    	   }
        } 
    }
    
    public void pauseDataCollection(){
        
        if(_dcService != null && _isCollectingData)
            _dcService.pauseDataCollection();
    }
    
    public void stopDataCollection(){
        
        if(_dcService != null && _isCollectingData)
            _dcService.stopDataCollection();
    }
    
    /**
     * Call the web service method to get the socket number (if available) from
     * the server.
     */
    public boolean getSocketNumber(){
     
        if(_serverCService == null)
            return false;
        
        _serverCService.obtainSocketFromServer();
        return true;
    }
    
    /**
     * Call the web service method to deliver the questionnaire to the server
     * for the current device client.
     */
    public boolean sendQuestionnaire(String questionnaire){

        if(_serverCService == null)
            return false;
     
        _serverCService.submitQuetionnaire(questionnaire);
        return true;
    }
    
    /**
     * Call the web service method to receive the questionnaire from the server
     * for the current device client.
     */
    public boolean getQuestionnaire(){
    
        if(_serverCService == null)
            return false;
        
        _serverCService.obtainQuestionnaire();
        return true;
    }
    
    public boolean sendSeatNumber(String seatNumber){
        
        if(_serverCService == null)
            return false;
        
        _serverCService.provideSeatNumber(seatNumber);
        return true;
    }
    
    public boolean obtainSurveyId(){
        
        if(_serverCService == null)
            return false;
        
        _serverCService.obtainSurveyId();
        return true;
    }
    
    public void openSocketConnection(){
        
        if(!_isSocketConnected)
            _serverCService.startSocket();
    }
    
    public void closeSocketConnection(){
        
        if(_isSocketConnected)
            _serverCService.stopSocket();
    }
    
    public boolean isDCSBinded(){

        return _dcSBind;
    }
    
    public boolean isServerCSBinded(){
        
        return _serverCSBind;
    }
    
    public void sendDataFiles(){
    	
    	_isUploadingData = true;
        File storageDir = _app.getStorageDir();
        if(storageDir.listFiles().length <= 0){
        	
        	_isUploadingData = false;
        	return;
        }
        
        for(int i = 0; i < storageDir.listFiles().length; i++){
            FileUploadStatus fus = new FileUploadStatus();
            fus.setFile(storageDir.listFiles()[i]);
            if(i == storageDir.listFiles().length - 1)
            	fus.setLastFile(true);
            _serverCService.uploadDataFile(fus, false);
        }
    }

    public long getServerTime(){
    	
    	if(_dcService == null)
    		return -1;
    	
    	return _dcService.getServerTime();
    }
    // ******************************************************************************* //
    // ******************************* PROTECTED METHODS ***************************** //
    // ******************************************************************************* //
    protected void stopDataService(){
        
        if(_dcSBind){
        	
            unbindService(_dataServiceConn);
            _dcSBind = false;
        }
        _dcService.stopDataCollection();
        _dcService.stopSelf();
    }
    
    protected void stopServerService(){

        if(_serverCSBind){
        	
            unbindService(_serverComConn);
            _serverCSBind = false;
        }
        _serverCService.stopSocket();
        _serverCService.stopSelf();
    }

    // ******************************************************************************* //
    // ******************************* EVENT HANDLING ******************************** //
    // ******************************************************************************* //
    @Override
    public void onAnalysisEvent(AnalysisEvent event) {

        // dispatch the event received to other listeners
        fireEvent(event);
        
        if(event instanceof StorageEvent)
            doOnStorageEvent((StorageEvent)event);
        else if (event instanceof CacheEvent)
            doOnCacheEvent((CacheEvent)event);
        else if (event instanceof SoapEvent)
            doOnSoapEvent((SoapEvent)event);
        else if(event instanceof SocketEvent)
            doOnSocketEvent((SocketEvent)event);
    }
    
    protected void doOnStorageEvent(StorageEvent event){
    }
    
    protected synchronized void doOnCacheEvent(CacheEvent event){
        
        if(event.getName().equals(CacheEvent.INIT_STORAGE)){
        	
        } else if(event.getName().equals(CacheEvent.FINALIZE_STORAGE_DONE)){
            _isCollectingData = false;
            // if sensor data files have been finalized we can upload them to 
            // the server and complete one stage of the survey. And send a 
            // notification that the upload was successfully completed
            sendDataFiles();
        }
    }
    
    protected synchronized void doOnSoapEvent(SoapEvent event){
        
        if(event.getName().equals(SoapEvent.DATA_FILE_UPLOADING)){
            _isUploadingData = true;
        } else if(event.getName().equals(SoapEvent.DATA_FILE_UPLOADED)){
            FileUploadStatus fus = (FileUploadStatus) event.getResult();
            // if the upload failed, try 3 times to upload it, otherwise 
            // notify the server for a failure and continue with next file
            if(!fus.isUploaded()){
                if(fus.getAttempt() >= 3)
                    _serverCService.notifyFileUploadFailure(fus.getFile());
                else {
                    _serverCService.uploadDataFile(fus, true);
                }
            }
        } else if(event.getName().equals(SoapEvent.DATA_UPLOAD_COMPLETE)){
            _isUploadingData = false;
            System.out.println("----------- received DATA_UPLOAD_COMPLETE event (if app crashed resume data collection) --------- " + _app.didAppCrash());
            if(_app.didAppCrash()){
            	System.out.println("we received DATA_UPLOAD_COMPLETE from a crashed application");
            	// in case we experienced crashed application we will automaticaly reset the 
            	// data collection
            	_dcService.resetServerTime(System.currentTimeMillis());
                _dcService.resumeDataCollection();
                _app.setAppCrashed(false);
            } else if(!_isSocketConnected){
                // If the socket is not connected it means that we have received a close_event
                // and we have closed the socket. At this point we need to finish the application
                stopSelf();
            }
        } else if(event.getName().equals(SoapEvent.SERVER_CMD_RECEIVED)){
        	
        }
    }
    
    protected synchronized void doOnSocketEvent(SocketEvent event){

        _isSocketConnected = true;
        if(event.getName().equals(SocketEvent.SOCKET_STOP)){
            // notify server that the connection is closed
            _isSocketConnected = false;
        } else if(event.getName().equals(SocketEvent.SOCKET_START)){
        }
        
        // Whenever we receive a command from the server (except empty cmd) we need to 
        // notify the server that the command was received.
        if(event.getName().equals(SocketEvent.MSG_PAUSE) || 
        		event.getName().equals(SocketEvent.MSG_RESUME) ||
        		event.getName().equals(SocketEvent.MSG_START) ||
        		event.getName().equals(SocketEvent.MSG_STOP))
        	_serverCService.notifyServerCmdReceived(event.getMsg().getType());
        
        // if the SoapService is sending data files to server we shouldnt 
        // change the DataCollectionService. 
        if(!_isUploadingData){
            
            if(event.getName().equals(SocketEvent.MSG_START)){
            	
            	// if we receive a start command from server we need to :
            	// 1 - Start data collecting
            	// 2 - Synchronize the system time to server´s time
                if(!_isCollectingData){
                	
                	if(!_dcService.isRunning()){
                		
                    	_isCollectingData = true;
                    	_dcService.resetServerTime(event.getMsg().getTime());
                        _dcService.startDataCollection();
                	}
                }
            } else if(event.getName().equals(SocketEvent.MSG_STOP)){

                // if we receive a stop message, we close the socket and 
                // stop datacollection (upload these data to server)
                try {
                    _app.clearPreferences();
                    if(_isCollectingData){
                        _dcService.stopDataCollection();
                        _serverCService.stopSocket();
                    } else 
                    	stopSelf();
                } catch (IllegalStateException e) {
                    System.err.println("ManagerService: Called to stop data collection but it " +
                    		           "cant proceed with the request!");
                }
            } else if(event.getName().equals(SocketEvent.MSG_PAUSE)){
                
                // if we receive a pause message we stop data collection and
                // upload current files to the server
                if(_isCollectingData)
                    _dcService.pauseDataCollection();
                
            } else if(event.getName().equals(SocketEvent.MSG_RESUME)){
                
                if(!_isCollectingData){
                	try {
                    	_isCollectingData = true;
                        _dcService.resumeDataCollection();
                    	_dcService.resetServerTime(event.getMsg().getTime());
					} catch (Exception e) {
						System.out.println("Received message resume but the application was resumed before !");
						e.printStackTrace();
					}
                }
                
            } else if(event.getName().equals(SocketEvent.MSG_EMPTY)){
                
                // do nothing. its just a message to check if the device is still connected
            }
        }
    }


    // ******************************************************************************* //
    // ********************************* ANALYSIS OBJECT ***************************** //
    // ******************************************************************************* //
    @Override
    public void fireEvent(AnalysisEvent event) {

        synchronized (_listeners) {
            
            for(AnalysisEventListener l : _listeners)
                l.onAnalysisEvent(event);
        }
    }

    @Override
    public void registerAnalysisListener(AnalysisEventListener listener) {

        synchronized (_listeners) {
            
            _listeners.add(listener);
        }
    }

    @Override
    public void unregisterAnalysisListener(AnalysisEventListener listener) {

        synchronized (_listeners) {
            
            _listeners.remove(listener);
        }
    }

    @Override
    public void clearListeners() {
        
        _listeners.clear();
    }
    
    // ******************************************************************************* //
    // ********************************* BINDER CLASS ******************************** //
    // ******************************************************************************* //
    public class ManagerSBinder extends Binder{
       
        public ManagerService getService(){
            
            return ManagerService.this;
        }
    }
}
