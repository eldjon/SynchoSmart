package tr.edu.metu.ii.aaa.core;

import java.io.File;
import java.text.SimpleDateFormat;

import tr.edu.metu.ii.aaa.exceptions.AnalysisUncaughtExceptionHandler;
import tr.edu.metu.ii.aaa.gson.GsonAdapterFactory;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


@SuppressLint({ "DefaultLocale", "SimpleDateFormat" })
public class AnalysisApp extends Application{

//    private static final String DEFAULT_SERVER_IP   = "144.122.98.160";
    private static final String DEFAULT_SERVER_IP   = "192.168.1.128";
    private static final String PREF_FILE_NAME      = "analysis_pref_file";
    private static final String PREF_SERVER_IP      = "server_ip";
    private static final String PREF_SERVER_SOCKET  = "server_socket_number";
    private static final String DEFAULT_STORAGE_DIR = "analysis_data";

    //state parameters
    private int               _serverSocket           = -1;
    private int               _surveyId               = -1;
    private boolean           _seatProvided           = false;
    private boolean           _hasQuestionnaire       = false;
    private boolean           _questionnaireSubmitted = false;
    
    private SimpleDateFormat  _sdf                    = null;
    private String            _imei                   = null;
    private String            _macAddress             = null;
    private SharedPreferences _sharedPref             = null;
    private Editor            _editor                 = null;
    private File              _storageDir             = null;
    // vibrator service
    private Vibrator          _vibrator               = null;
    private Gson              _questionnaireGson      = null;
    
    @Override
    public void onCreate() {

        super.onCreate();
        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        _macAddress = info.getMacAddress();

        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context
                                                                    .TELEPHONY_SERVICE);
        _imei         = tMgr.getDeviceId();
        _sharedPref   = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        _editor       = _sharedPref.edit();
        // initiate the storage directory
        File external = Environment.getExternalStorageDirectory();
        _storageDir   = new File(external.getAbsolutePath()
                                         .concat("/")
                                         .concat(DEFAULT_STORAGE_DIR));
        _sdf = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
        Thread.setDefaultUncaughtExceptionHandler(
               new AnalysisUncaughtExceptionHandler(this));
        
        _vibrator          = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        _questionnaireGson = new GsonBuilder().registerTypeAdapterFactory(
                                               new GsonAdapterFactory())
                                               .create();
        
        readLastPreferences();
    }
    
    public Gson getQuestionnaireGson(){
        
        return _questionnaireGson;
    }
    
    public File getStorageDir(){
        
        return _storageDir;
    }
    
    public SimpleDateFormat getSDF(){
        
        return _sdf;
    }
    
    /**
     * Each time we set the survey id we need to reset other 
     * shared preferences information because setting a survey id
     * means that we want to start a completely new survey. Previous
     * survey (if there is any) it is considered completed
     */
    public void setSurveyId(int id){
        
        if(id != _surveyId){
            
            synchronized (this) {
                
                _surveyId               = id;
                _hasQuestionnaire       = false;
                _questionnaireSubmitted = false;
                _seatProvided           = false;
                _editor.putInt(Constants.PREF_LAST_SURVEY_ID, id);
                _editor.putBoolean(Constants.PREF_HAS_QUESTIONNAIRE, false);
                _editor.putBoolean(Constants.PREF_QUESTIONNAIRE_SUBMITTED, false);
                _editor.putBoolean(Constants.PREF_SEAT_PROVIDED, false);
                _editor.commit();
            }
        }
    }
    
    public void clearPreferences(){
        synchronized (this) {
            
//            _surveyId               = 0;
            _hasQuestionnaire       = false;
            _questionnaireSubmitted = false;
            _seatProvided           = false;
            _editor.putInt(Constants.PREF_LAST_SURVEY_ID, 0);
            _editor.putBoolean(Constants.PREF_HAS_QUESTIONNAIRE, false);
            _editor.putBoolean(Constants.PREF_QUESTIONNAIRE_SUBMITTED, false);
            _editor.putBoolean(Constants.PREF_SEAT_PROVIDED, false);
            _editor.putBoolean(Constants.PREF_APP_CRASHED, false);
            _editor.commit();
        }
    }
    
    public void setSeatProvided(boolean seat){
       
        _seatProvided           = seat;
        _hasQuestionnaire       = false;
        _questionnaireSubmitted = false;
        _editor.putBoolean(Constants.PREF_SEAT_PROVIDED, 
                           _seatProvided);
        _editor.putBoolean(Constants.PREF_HAS_QUESTIONNAIRE, 
                           _hasQuestionnaire);
        _editor.putBoolean(Constants.PREF_QUESTIONNAIRE_SUBMITTED, 
                           _questionnaireSubmitted);
        _editor.commit();
    }
    
    public void setHasQuestionnaire(boolean hasQuestionnaire){
        
        _hasQuestionnaire       = hasQuestionnaire;
        _questionnaireSubmitted = false;
        _editor.putBoolean(Constants.PREF_HAS_QUESTIONNAIRE, 
                           _hasQuestionnaire);
        _editor.putBoolean(Constants.PREF_QUESTIONNAIRE_SUBMITTED, 
                           _questionnaireSubmitted);
        _editor.commit();
    }
    
    public void setQuestionnaireSubmitted(boolean value){
        
        _questionnaireSubmitted = value;
        _editor.putBoolean(Constants.PREF_QUESTIONNAIRE_SUBMITTED, 
                           _questionnaireSubmitted);
        _editor.commit();
    }
    
    public void setQuestionnaire(String questionnaire){
        
        _editor.putString(Constants.PREF_QUESTIONNAIRE, questionnaire);
        _editor.commit();
    }
    
    public String getQuestionnaire(){
        
        return _sharedPref.getString(Constants.PREF_QUESTIONNAIRE, null);
    }
    
    public boolean isSeatNumberProvided(){
        
        return _seatProvided;
    }
    
    public boolean hasQuestionnaire(){
        
        return _hasQuestionnaire;
    }
    
    public boolean questionnaireSubmitted(){
        
        return _questionnaireSubmitted;
    }
    
    public int getSurveyId(){
        
        return _surveyId;
    }
    
    private void readLastPreferences(){
        
        _surveyId               = _sharedPref.getInt(Constants.PREF_LAST_SURVEY_ID, 
                                                     0);
        _hasQuestionnaire       = _sharedPref.getBoolean(Constants.PREF_HAS_QUESTIONNAIRE, 
                                                         false);
        _questionnaireSubmitted = _sharedPref.getBoolean(Constants.PREF_QUESTIONNAIRE_SUBMITTED, 
                                                         false);
        _seatProvided           = _sharedPref.getBoolean(Constants.PREF_SEAT_PROVIDED, 
                                                         false);
    }
    
    public synchronized boolean didAppCrash(){
    	
    	return _sharedPref.getBoolean(Constants.PREF_APP_CRASHED, false);
    }
    
    public synchronized void setAppCrashed(boolean crashed){
    	
    	_editor.putBoolean(Constants.PREF_APP_CRASHED, crashed);
    	_editor.commit();
    }
    
    // ******************************************************************************* //
    // *********************** METHODS TO BE CALLED BY ACTIVITIES ******************** //
    // ******************************************************************************* //
    public void vibrate(int milliseconds){
        if(_vibrator != null)
            _vibrator.vibrate(milliseconds);
    }
    
    public boolean isAirplaneModeOn() {

        return Settings.System.getInt(getContentResolver(),
                                      Settings.System.AIRPLANE_MODE_ON, 0) != 0;
     }
    
    @SuppressLint("DefaultLocale")
    public String getIpAddress(){
        
        if(!isNetworkOn())
            return null;
        
        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo info       = manager.getConnectionInfo();
        int ipAsInt         = info.getIpAddress();
        String ip           = null;
        ip                  = String.format("%d.%d.%d.%d",
                                            (ipAsInt & 0xff),
                                            (ipAsInt >> 8 & 0xff),
                                            (ipAsInt >> 16 & 0xff),
                                            (ipAsInt >> 24 & 0xff));
        return ip;
    }
    
    public String getMacAddress(){
        
        return _macAddress;
    }
    
    public boolean isNetworkOn(){
        
        ConnectivityManager conMan = (ConnectivityManager) 
                                      getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNet = conMan.getActiveNetworkInfo();
        
        if(activeNet == null)
            return false;
        
        if(activeNet.isAvailable() && activeNet.isConnected())
            return true;
        
        return false;
    }
    
    public synchronized int getServerSocket(){
        
        if(_serverSocket <= 0)
            _serverSocket = _sharedPref.getInt(PREF_SERVER_SOCKET, -1);
        return _serverSocket;
    }
    
    public synchronized void setServerSocket(int socket){
        
        _editor.putInt(PREF_SERVER_SOCKET, socket);
        _serverSocket = socket;
    }
    
    public String getServerIp(){
        
        String serverIp = _sharedPref.getString(PREF_SERVER_IP,
                                                null);
        
        if(serverIp!= null)
            return serverIp;
        
        return DEFAULT_SERVER_IP;
    }
    
    synchronized public void saveServerIp(String serverIp){
        
        _editor.putString(PREF_SERVER_IP, serverIp);
        _editor.commit();
    }
    
    public String getImei(){
        
        return _imei;
    }
}
