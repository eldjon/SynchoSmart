package tr.edu.metu.ii.aaa.events;

import java.io.File;



public class SoapEvent extends AnalysisEvent{

    /**
     * 
     */
    private static final long serialVersionUID      = -5985130807299898695L;
    protected static final String BASE_EVENT_NAME   = "tr.edu.metu.ii.aaa.events.SoapEvent";

    public static final String SOCKET_RECEIVED      = BASE_NAME + "socket_received";
    public static final String SEAT_NUMBER_SENT     = BASE_NAME + "seat_number_sent";
    public static final String QUEST_RECEIVED       = BASE_NAME + "questionnaire_received";
    public static final String QUEST_SENT           = BASE_NAME + "questionnaire_sent";
    public static final String DATA_FILE_UPLOADING  = BASE_NAME + "data_file_uploading";
    public static final String DATA_FILE_UPLOADED   = BASE_NAME + "data_file_uploaded";
    public static final String DEVICE_DISCONNECT    = BASE_NAME + "device_disconnect";
    public static final String SURVEY_ID_RECEIVED   = BASE_NAME + "survey_id_received";
    public static final String FILE_FAILURE_NOTIFY  = BASE_NAME + "file_failure_notify";
    public static final String APP_EXCEPTION_NOTIFY = BASE_NAME + "app_exception_notify";
    public static final String DATA_UPLOAD_COMPLETE = BASE_NAME + "data_upload_complete";
    public static final String SERVER_CMD_RECEIVED  = BASE_NAME + "command_received";
    
    protected String _name   = null;
    protected Object _result = null;
    
    public SoapEvent(Object source, long time, String name) {

        super(source, time);
        _name = name;
    }
    
    public String getName(){
        
        return _name;
    }
    
    public void setResult(Object result){
        
        _result = result;
    }
    
    public Object getResult(){
        
        return _result;
    }
    
    public static class FileUploadStatus {
        
        File    _file       = null;
        boolean _uploaded   = false;
        String  _checkSum   = null;
        int     _attempt    = 0;
        boolean _isLastFile = false;
        
        public File getFile() {
        
            return _file;
        }
        
        public void setFile(File _file) {
        
            this._file = _file;
        }
        
        public boolean isUploaded() {
        
            return _uploaded;
        }
        
        public void setUploaded(boolean _uploaded) {
        
            this._uploaded = _uploaded;
        }
        
        public String getCheckSum() {
        
            return _checkSum;
        }
        
        public void setCheckSum(String _checkSum) {
        
            this._checkSum = _checkSum;
        }
        
        public int getAttempt(){
            return _attempt;
        }
        
        public void setLastFile(boolean lastFile){
        	
        	_isLastFile = lastFile;
        }
        
        public boolean isLastFile(){
        	return _isLastFile;
        }
        
        public int tryAgain(){
            
            _attempt++;
            return _attempt;
        }
    }
}
