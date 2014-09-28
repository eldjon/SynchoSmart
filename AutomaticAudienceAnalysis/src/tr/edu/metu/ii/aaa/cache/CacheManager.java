package tr.edu.metu.ii.aaa.cache;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import tr.edu.metu.ii.aaa.core.AnalysisApp;
import tr.edu.metu.ii.aaa.core.BaseAnalysisObject;
import tr.edu.metu.ii.aaa.events.CacheEvent;
import tr.edu.metu.ii.aaa.events.StorageEvent;
import tr.edu.metu.ii.aaa.sensors.BasicSensorDescriptor;
import tr.edu.metu.ii.aaa.sensors.SensorDescriptor;
import tr.edu.metu.ii.aaa.services.DataCollectorService.SensorRecord;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;


@SuppressLint("HandlerLeak")
public class CacheManager extends BaseAnalysisObject{
    
    public final static int TOTAL_NUMBER_OF_SENSORS       = 20;
    protected final static int DEFAULT_LIST_SIZE          = 4096 ;
    
    // This message will trigger the background HandlerThread to start
    // unloading data from cache to file; 
    protected final static int MSG_PRIMARY_DATA_SAVING   = 1;
    protected final static int MSG_SECONDARY_DATA_SAVING = 2;
    protected final static int MSG_COMPLETE              = 3;
    protected final static int MSG_INITIATE              = 4;
    protected final static int MSG_RESUME                = 5;

    // Cache initialy is on the STATE_DEFAULT and it can change the state only to
    // STATE_STARTED after calling start(). 
    protected final static int STATE_DEFAULT              = 10;
    protected final static int STATE_STARTED              = 11;
    protected final static int STATE_PRIMARY_PROCESSING   = 12;
    protected final static int STATE_SECONDARY_PROCESSING = 13;
    protected final static int STATE_PAUSED               = 14;
    protected final static int STATE_ENDED                = 15;
    
    protected LinkedList<SensorRecord> _primaryList       = null;
    protected LinkedList<SensorRecord> _secondaryList     = null;
    private long                       _maxListSize;
    
    // Background thread responsible for saving data to files
    private HandlerThread       _cacheThread = null;
    private CacheHandler        _handler     = null;
    // Cache state
    private volatile int        _state       = STATE_DEFAULT;
    private AnalysisApp         _app         = null;
    private SensorDescriptor[] _sDescriptors = null;

    
    @SuppressLint("UseSparseArrays")
    public CacheManager(Context ctx, int[] sensors){
        
        super();
        _maxListSize    = DEFAULT_LIST_SIZE;
        _primaryList    = new LinkedList<SensorRecord>();
        _secondaryList  = new LinkedList<SensorRecord>();
        _app            = (AnalysisApp) ctx;
        
        // init descriptors
        _sDescriptors = new BasicSensorDescriptor[TOTAL_NUMBER_OF_SENSORS];
        for(int i = 0; i < sensors.length; i++){
            int sensorType            = sensors[i];
            BasicSensorDescriptor sd       = new BasicSensorDescriptor(sensorType);
            _sDescriptors[sensorType] = sd;
        }
        
        // create handling thread
        synchronized (this) {
        	
            _cacheThread = new HandlerThread("CACHE_THREAD");
            _cacheThread.start();
            _handler     = new CacheHandler(_cacheThread.getLooper());
        }
    }
    
    public synchronized void start() throws IllegalStateException{
        
        if(_state != STATE_DEFAULT && _state != STATE_PAUSED)
            throw new IllegalStateException("CacheManager has already been started once!!");
        
        _state = STATE_STARTED;
        _handler.sendEmptyMessage(MSG_INITIATE);
    }
    
    public synchronized void stop() throws IllegalStateException{
     
    	if(_state <= STATE_DEFAULT)
    		return; 
    	
    	if(_state != STATE_ENDED){
    		
            _state = STATE_ENDED;
            // save the rest of the data to file but do not accept any more data to be
            // saved in cache.(all the data from both lists will be saved to files)
            _handler.sendEmptyMessage(MSG_COMPLETE);
    	}
        else
            throw new IllegalStateException("CacheManager cannot be stop in undefined state! ");
    }
    
    public synchronized void pause() throws IllegalStateException{
        
        if(_state == STATE_PAUSED || _state <= STATE_DEFAULT)
            return ;
            
        if((_state == STATE_STARTED) 
                        || (_state == STATE_PRIMARY_PROCESSING)
                        || (_state == STATE_SECONDARY_PROCESSING)){
            
            _state = STATE_PAUSED;
            _handler.sendEmptyMessage(MSG_COMPLETE);
        }
        else 
            throw new IllegalStateException("CacheManager has not been started yet!!");
    }
    
    public synchronized void resume() throws IllegalStateException{
        
        if(_state != STATE_DEFAULT && _state != STATE_PAUSED)
            throw new IllegalStateException("CacheManager has already been started/resumed once!!");
        
        _state = STATE_STARTED;
        _handler.sendEmptyMessage(MSG_RESUME);
    }
    
    public void addSensorRecord(SensorRecord record){
        
        if(_state == STATE_PRIMARY_PROCESSING){
            
            synchronized (_secondaryList) {
                
                _secondaryList.add(record);
            }
        }
        else {
                
            synchronized (_primaryList) {
                
                _primaryList.add(record);
                if(_primaryList.size() >= _maxListSize){
                    
                    _state = STATE_PRIMARY_PROCESSING;
                    _handler.sendEmptyMessage(MSG_PRIMARY_DATA_SAVING);
                }
            }
        }
    }
    
    public synchronized boolean isRunning(){
        
        if(_state != STATE_DEFAULT)
            return true;
        return false;
    }

    public synchronized int getState(){
    	
    	return _state;
    }
   
    public synchronized void clear(){
        
        clearListeners();
        _handler.removeCallbacksAndMessages(null);
        _handler = null;
        _cacheThread.interrupt();
        _cacheThread = null;
    }
    
    // ******************************************************************************* //
    // *************************** PROTECTED METHODS ********************************* //
    // ******************************************************************************* //
    protected void canProcess(){
        
        if((_state == STATE_DEFAULT) 
                        || (_state == STATE_ENDED) 
                        || (_state == STATE_PAUSED))
            throw new IllegalStateException("CacheManager state does not accept any records´ operations!!" +
                    " It is either not started or stop() method was already called!");
    }
    
    protected class CacheHandler extends Handler{

        public CacheHandler(Looper looper){
            
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                
                case MSG_PRIMARY_DATA_SAVING:
                    System.out
							.println("CacheManager.CacheHandler.handleMessage()  saving PRIMARY DATA");
                    fireEvent(new CacheEvent(CacheManager.this, 
                                             System.currentTimeMillis(), 
                                             CacheEvent.START_STORAGE));
                    synchronized (_primaryList) {
                        
                        doSavePrimaryList();
                    }
                    synchronized (CacheManager.this) {
                        
                        _state = STATE_SECONDARY_PROCESSING;
                        Message sMsg = new Message();
                        sMsg.what    = MSG_SECONDARY_DATA_SAVING;
                        _handler.sendMessageAtFrontOfQueue(sMsg);
                    }
                    
                    break;
                
                case MSG_SECONDARY_DATA_SAVING:
                    
                    System.out
                    	  .println("CacheManager.CacheHandler.handleMessage()  saving SECONDARY DATA");
                    fireEvent(new CacheEvent(CacheManager.this, 
                                             System.currentTimeMillis(), 
                                             CacheEvent.START_STORAGE));
                    synchronized (_secondaryList) {
                        
                        doSaveSecondaryList();
                    }
                    
                    break;
                    
                case MSG_COMPLETE:
                    System.out
						  .println("CacheManager.CacheHandler.handleMessage()  COMPLETE");
                    fireEvent(new CacheEvent(CacheManager.this, 
                                             System.currentTimeMillis(), 
                                             CacheEvent.START_FINALIZE_STORAGE));
                    doSaveData();
                    finalizeDataFiles();
                    
                    fireEvent(new CacheEvent(CacheManager.this, 
                                             System.currentTimeMillis(), 
                                             CacheEvent.FINALIZE_STORAGE_DONE));
                    break;
                
                case MSG_INITIATE:
                    
                    System.out
					  .println("CacheManager.CacheHandler.handleMessage()  INITIATE ");
                    fireEvent(new CacheEvent(CacheManager.this, 
                                             System.currentTimeMillis(), 
                                             CacheEvent.INIT_STORAGE));
                    initDataFiles(true);
                    break;
                    
                case MSG_RESUME:
                    
                    fireEvent(new CacheEvent(CacheManager.this, 
                                             System.currentTimeMillis(), 
                                             CacheEvent.INIT_STORAGE));
                    initDataFiles(false);
                    break;
                    
                default:
                    break;
            }
        }
    }   
    
    // ******************************************************************************* //
    // ********************* PROTECTED METHODS CALLED BY HANDLER ********************* //
    // ******************************************************************************* //
    protected void doSavePrimaryList(){
        
        while(_primaryList.size() > 0){
            
            SensorRecord pr = _primaryList.removeFirst();
            writeToFile(pr);
        }
    }
    
    protected void doSaveSecondaryList(){

        while(_secondaryList.size() > 0){
            
            SensorRecord pr = _secondaryList.removeFirst();
            writeToFile(pr);
        }
    }
    
    protected void doSaveData(){
        
        synchronized (CacheManager.this) {
            
            if(_state == STATE_PRIMARY_PROCESSING){
                
                doSavePrimaryList();
                doSaveSecondaryList();
            }
            else {
                
                doSaveSecondaryList();
                doSavePrimaryList();
            }
        }

    }

    /**
     * Create files for each selected sensor on the analysis_data folder.
     * Open the CSV writer and write the column names for them.
     * @param removeFiles
     */
    private void initDataFiles(boolean removeFiles){
        
        int partNumber = 1;
        // remove all files currently in the Storage Folder
        if(_app.getStorageDir().exists()){
            File[] files = _app.getStorageDir().listFiles();

            if(removeFiles){
                
                for(File f : files)
                    f.delete();
            } else {
                
                if(files != null && files.length > 0){
                    
                    String path = files[0].getAbsolutePath();
                    partNumber  = extractPartNumber(path);
                }
                for(File f : files)
                    f.delete();
            }
        } else {
            _app.getStorageDir().mkdir();  
        }
        
        for(SensorDescriptor sd : _sDescriptors){
            
            if(sd != null){
                
                sd.init(_app.getStorageDir().getAbsolutePath(), 
                		partNumber, 
                		_app);
                fireEvent(new StorageEvent(CacheManager.this, 
                                           System.currentTimeMillis(), 
                                           StorageEvent.STORAGE_INIT));
            }
        } 
    }
    
    private void writeToFile(SensorRecord event){
        
        _sDescriptors[event._type].writeToFile(event);
    }
    
    private void finalizeDataFiles(){
        
        for(SensorDescriptor sd : _sDescriptors)
            if(sd != null)
                try {
                    sd.getCsvWriter().close();
                    fireEvent(new StorageEvent(CacheManager.this, 
                                               System.currentTimeMillis(), 
                                               StorageEvent.STORAGE_FINAL));
                } catch (IOException e) {
                    e.printStackTrace();
                }

    }
    
    /**
     * Files in the StorageDirectory have the following format: SensorName_part_x.csv
     * When the user pauses and restarts the DataCollectorService we need to keep track
     * which part of the data we are storing in order when delivering to the server 
     * it will be distinguishable. First run the files will have suffix 1. So the second
     * resume the files will take suffix 2.
     * @param filePath
     * @return
     */
    protected int extractPartNumber(String filePath){
        
        if(!filePath.contains(BasicSensorDescriptor.PART_SUFFIX))
            return 1;
        else {
            int idx       = filePath.lastIndexOf("_");
            int dotIdx    = filePath.lastIndexOf(".");
            String number = filePath.substring(idx + 1, dotIdx);
            return Integer.parseInt(number) + 1;
        }
    }
}
