package tr.edu.metu.ii.aaa.services;

import java.io.File;

import tr.edu.metu.ii.aaa.core.AnalysisApp;
import tr.edu.metu.ii.aaa.core.AnalysisObject;
import tr.edu.metu.ii.aaa.events.AnalysisEvent;
import tr.edu.metu.ii.aaa.events.AnalysisEventListener;
import tr.edu.metu.ii.aaa.events.SoapEvent.FileUploadStatus;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


public class ServerComService extends Service
                              implements AnalysisObject{

    private SoapConnector                 _soapConn;
    private SocketConnector               _socketConn;
    private AnalysisApp                   _app;
    private ServerComBinder               _binder = new ServerComBinder();
    
    @Override
    public IBinder onBind(Intent intent) {

        return _binder;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        _app        = (AnalysisApp) getApplication();
        _soapConn   = new SoapConnector(_app);
        _socketConn = new SocketConnector(_app);
    }

    @Override
    public void onDestroy() {

        System.out.println("************************* DESTROYING SERVER_COM_SERVICE ********************* ");
        synchronized (this) {
			
            _soapConn.clearListeners();
            _soapConn.close();
            _socketConn.clearListeners();
            _socketConn.stop();
            _soapConn   = null;
            _socketConn = null;
		}
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {

        super.onStart(intent,
                      startId);
    }
    
    // ******************************************************************************* //
    // ************************* METHODS TO BE CALLED BY ACTIVITIES ****************** //
    // ******************************************************************************* //
    /**
     * This method sends a soap request to the server providing the device´s imei and 
     * asks if a socket is opened and ready for this device to connect to. The server 
     * checks the device imei and the socket status. If the device imei is registered
     * and socket i ready to accept connections, it returns the socket number otherwise
     * it return -1. This method calls listeners method to provide the result.
     */
    // ********************************* SOAP RELATED METHODS ************************ //
    public void obtainSocketFromServer(){
        
    	if(_soapConn != null)
    		_soapConn.getSocket();
    }
    
    public void provideSeatNumber(String seatNumber){
        
    	if(_soapConn != null)
    		_soapConn.provideSeatNumber(seatNumber);
    }
    
    public void obtainQuestionnaire(){
        
    	if(_soapConn != null)
    		_soapConn.getQuestionnaire();
    }
    
    public void submitQuetionnaire(String questionnaire){
        
    	if(_soapConn != null)
    		_soapConn.submitQuestionnaire(questionnaire);
    }
    
    public void uploadDataFile(FileUploadStatus file, boolean reupload){
        
    	if(_soapConn != null)
    		_soapConn.uploadDataFile(file, reupload);
    }
    
    public void notifyDisconnectedDevice(){
        
    	if(_soapConn != null)
    		_soapConn.notifyDeviceDisconnection();
    }
    
    public void notifyFileUploadFailure(File file){
        
    	if(_soapConn != null)
    		_soapConn.notifyFileUploadFailure(file);
    }
    
    public void obtainSurveyId(){
        
    	if(_soapConn != null)
    		_soapConn.getSurveyId();
    }
    
    public void notifyServerCmdReceived(int cmd){
    	
    	if(_soapConn != null)
    		_soapConn.notifyServerCmdReceived(cmd);
    }
    // ********************************* SOCKET RELATED METHODS ********************** //
    public void startSocket(){
        
        if(!_socketConn.isAlive())
            _socketConn.start();
    }
    
    public void stopSocket(){
        
        if(_socketConn.isAlive())
            _socketConn.stop();
    }

    // ******************************************************************************* //
    // ***************************** LISTENERS IMPLEMENTATION ************************ //
    // ******************************************************************************* //
    @Override
    public void fireEvent(AnalysisEvent event) {

    }

    @Override
    public void registerAnalysisListener(AnalysisEventListener listener) {

        _soapConn.registerAnalysisListener(listener);
        _socketConn.registerAnalysisListener(listener);
    }

    @Override
    public void unregisterAnalysisListener(AnalysisEventListener listener) {

        _soapConn.unregisterAnalysisListener(listener);
        _socketConn.unregisterAnalysisListener(listener);
    }
    

    @Override
    public void clearListeners() {

        _soapConn.clearListeners();
        _socketConn.clearListeners();
    }
    
    // ******************************************************************************* //
    // ************************************* BINDER CLASS **************************** //
    // ******************************************************************************* //
    public class ServerComBinder extends Binder {
       
        public ServerComService getService(){
            
            return ServerComService.this;
        }
    }
}
