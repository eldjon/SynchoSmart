package tr.edu.metu.ii.aaa.services;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import tr.edu.metu.ii.aaa.core.AnalysisApp;
import tr.edu.metu.ii.aaa.core.BaseAnalysisObject;
import tr.edu.metu.ii.aaa.events.SoapEvent;
import tr.edu.metu.ii.aaa.events.SoapEvent.FileUploadStatus;
import android.R.integer;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

@SuppressWarnings("deprecation")
@SuppressLint("SimpleDateFormat")
public class SoapConnector extends BaseAnalysisObject{
    
    private static final int MSG_GET_SOCKET                   = 2;
    private static final int MSG_UPLOAD_DATA_FILE             = 3;
    private static final int MSG_PROVIDE_SEAT_NUMBER          = 4;
    private static final int MSG_SUBMIT_QUESTIONNAIRE         = 5;
    private static final int MSG_GET_QUESTIONNAIRE            = 6;
    private static final int MSG_DEVICE_DISCONNECT            = 7;
    private static final int MSG_GET_SURVEY_ID                = 8;
    private static final int MSG_FILE_FAILURE                 = 9;
    private static final int MSG_NOTIFY_EXCEPTION             = 10;
    private static final int MSG_NOTIFY_CMD_RECEIVED          = 11;
    
    public static final String  NAMESPACE                     = "http://webservices.thesis.metu.edu.tr/";
    private static final String METHOD_GET_SOCKET             = "getSocket";
    private static final String METHOD_GET_QUESTIONNAIRE      = "getQuestionnaire";
    private static final String METHOD_PROVIDE_SEAT_NUMBER    = "provideSeatNumber";
    private static final String METHOD_SUBMIT_QUESTIONNAIRE   = "submitQuestionnaire";
    private static final String METHOD_DEVICE_DISCONNECT      = "disconnectDevice";
    private static final String METHOD_GET_SURVEY_ID          = "getSurveyId";
    private static final String METHOD_NOTIFY_FILE_FAILURE    = "notifyFileUploadFailure";
    private static final String METHOD_NOTIFY_APP_EXCEPTION   = "notifyAppException";
    private static final String METHOD_SERVER_CMD_RECEIVED    = "receivedCmd";
    
    private HandlerThread      _soapHT        = null;
    private SoapHandler        _soapHandler   = null;
    private AnalysisApp        _app           = null;
    
     
    @SuppressLint("SimpleDateFormat")
    public SoapConnector(AnalysisApp app){
        
        super();
        _soapHT         = new HandlerThread("SOAP_THREAD", 
                                            Process.THREAD_PRIORITY_LOWEST);
        _soapHT.start();
        _soapHandler    = new SoapHandler(_soapHT.getLooper());
        _app            = app;
    }
    
    // ******************************************************************************* //
    // ********************** PUBLIC METHODS TO BE CALLED BY SERVICE ***************** //
    // ******************************************************************************* //
    public String soapUrl(){
        
        return "http://" + _app.getServerIp() 
                         + ":8080/ThesisServer/services/SoapWS";
    }
    
    public String uploadServletUrl(){
        
        return "http://" + _app.getServerIp() 
                         + ":8080/ThesisServer/UploadServlet";
    }
    
    public String notifyServletUrl(){
    	
        return "http://" + _app.getServerIp() 
        				 + ":8080/ThesisServer/NotifyServlet";
    }
    
    public void getSocket(){
        
        _soapHandler.sendEmptyMessage(MSG_GET_SOCKET);
    }
    
    public void provideSeatNumber(String seatNumber){
        
        Message msg = new Message();
        msg.obj     = seatNumber;
        msg.what    = MSG_PROVIDE_SEAT_NUMBER;
        _soapHandler.sendMessage(msg);
    }
    
    public void submitQuestionnaire(String questionnaire){
        
        Message msg = new Message();
        msg.what    = MSG_SUBMIT_QUESTIONNAIRE;
        msg.obj     = questionnaire;
        _soapHandler.sendMessage(msg);
    }
    
    public void getQuestionnaire(){
        
        _soapHandler.sendEmptyMessage(MSG_GET_QUESTIONNAIRE);
    }
    
    public void uploadDataFile(FileUploadStatus file, boolean reupload){
        
        Message msg = new Message();
        msg.what    = MSG_UPLOAD_DATA_FILE;
        msg.obj     = file;
        if(reupload)
            _soapHandler.sendMessageAtFrontOfQueue(msg);
        else 
            _soapHandler.sendMessage(msg);
    }
    
    public void notifyDeviceDisconnection(){
        
        _soapHandler.sendEmptyMessage(MSG_DEVICE_DISCONNECT);
    }
    
    public void notifyFileUploadFailure(File file){
        
        Message msg = new Message();
        msg.what    = MSG_FILE_FAILURE;
        msg.obj     = file;
        _soapHandler.sendMessage(msg);
    }
    
    public void getSurveyId(){
        
        _soapHandler.sendEmptyMessage(MSG_GET_SURVEY_ID);
    }
    
    public void notifyAppException(String exception){
        
        Message msg = new Message();
        msg.obj     = exception;
        msg.what    = MSG_NOTIFY_EXCEPTION;
        _soapHandler.sendMessage(msg);
    }
    
    public void notifyServerCmdReceived(int cmd){
    	
        Message msg = new Message();
        msg.what    = MSG_NOTIFY_CMD_RECEIVED;
        msg.arg1    = cmd;
        _soapHandler.sendMessage(msg);
    }
    
    public synchronized void close(){
        
        _soapHandler.removeCallbacksAndMessages(null);
        _soapHT.interrupt();
        _soapHandler       = null;
        _soapHT            = null;
    }
    
    // ******************************************************************************* //
    // *********************************** HANDLER *********************************** //
    // ******************************************************************************* //
    @SuppressLint("HandlerLeak")
    private class SoapHandler extends Handler{

        public SoapHandler(Looper looper) {

            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {

            SoapEvent event = null;
            String imei     = _app.getImei();
            String ip       = _app.getIpAddress();
            switch (msg.what) {

                case MSG_GET_SOCKET:
                
                    int socketNumber = doGetSocket(imei, ip);
                    _app.setServerSocket(socketNumber);
                    event = new SoapEvent(SoapConnector.this, 
                                          System.currentTimeMillis(), 
                                          SoapEvent.SOCKET_RECEIVED);
                    event.setResult(Integer.valueOf(socketNumber));
                    fireEvent(event);
                    break;

                case MSG_PROVIDE_SEAT_NUMBER:
                    
                    boolean result = doProvideSeatNumber(_app.getImei(), 
                                                         msg.obj.toString());
                    event = new SoapEvent(SoapConnector.this, 
                                          System.currentTimeMillis(), 
                                          SoapEvent.SEAT_NUMBER_SENT);
                    event.setResult(result);
                    fireEvent(event);

                    break;
                    
                case MSG_SUBMIT_QUESTIONNAIRE:
                    
                    boolean sQues = doSubmitQuestionnaire(_app.getImei(), 
                                                          _app.getSurveyId(),
                                                          msg.obj.toString());
                    event = new SoapEvent(SoapConnector.this, 
                                          System.currentTimeMillis(), 
                                          SoapEvent.QUEST_SENT);
                    event.setResult(sQues);
                    fireEvent(event);
                    break;
                    
                case MSG_GET_QUESTIONNAIRE:
                    
                    String questionnaire = doGetQuestionnaire(_app.getImei());
                    event = new SoapEvent(SoapConnector.this, 
                                          System.currentTimeMillis(), 
                                          SoapEvent.QUEST_RECEIVED);
                    event.setResult(questionnaire);
                    fireEvent(event);
                    break;
                    
                case MSG_DEVICE_DISCONNECT:
                    
                    event = new SoapEvent(SoapConnector.this, 
                                          System.currentTimeMillis(), 
                                          SoapEvent.DEVICE_DISCONNECT);
                    boolean disconnected = doNotifyDeviceDisconnection(imei);
                    event.setResult(disconnected);
                    fireEvent(event);
                    break;
                    
                case MSG_GET_SURVEY_ID:
                    
                    int surveyId = doGetSurveyId(imei);
                    event = new SoapEvent(SoapConnector.this, 
                                          System.currentTimeMillis(), 
                                          SoapEvent.SURVEY_ID_RECEIVED);
                    event.setResult(Integer.valueOf(surveyId));
                    doGetSurveyId(_app.getImei());
                    fireEvent(event);
                    break;
                    
                case MSG_FILE_FAILURE:
                    
                    File file = (File) msg.obj;
                    Boolean notified = doNotifyFileFailure(_app.getImei(), file);
                    event = new SoapEvent(SoapConnector.this, 
                                          System.currentTimeMillis(), 
                                          SoapEvent.FILE_FAILURE_NOTIFY);
                    event.setResult(notified);
                    fireEvent(event);
                    break;
                    
                case MSG_NOTIFY_EXCEPTION:
                    
                    notified = doNotifyException(_app.getImei(), 
                                                 (String)msg.obj);
                    event = new SoapEvent(SoapConnector.this, 
                                          System.currentTimeMillis(), 
                                          SoapEvent.APP_EXCEPTION_NOTIFY);
                    event.setResult(notified);
                    fireEvent(event);
                    break;
                    
                case MSG_NOTIFY_CMD_RECEIVED:
                    
                	notified = doServerCmdReceived(_app.getImei(),
                								   msg.arg1);
                    event = new SoapEvent(SoapConnector.this, 
                                          System.currentTimeMillis(), 
                                          SoapEvent.SERVER_CMD_RECEIVED);
                    event.setResult(msg.arg1);
                    fireEvent(event);
                    break;
                    
                case MSG_UPLOAD_DATA_FILE:
                    
                    event = new SoapEvent(SoapConnector.this, 
                                          System.currentTimeMillis(), 
                                          SoapEvent.DATA_FILE_UPLOADING);
                    event.setResult(Boolean.toString(true));
                    fireEvent(event);

                    FileUploadStatus fus = (FileUploadStatus) msg.obj;
                    
                    // upload files based on Apache libraries (handles big files)
                    boolean uploaded = doUploadFile(fus, 
                                                    _app.getImei(), 
                                                    _app.getSurveyId());
                    event = new SoapEvent(SoapConnector.this, 
                                          System.currentTimeMillis(), 
                                          SoapEvent.DATA_FILE_UPLOADED);
                    fus.setUploaded(uploaded);
                    fus.tryAgain();
                    event.setResult(fus);
                    fireEvent(event);
                    if(fus.isLastFile()){
                    	
                        event = new SoapEvent(SoapConnector.this, 
                                			  System.currentTimeMillis(), 
                                			  SoapEvent.DATA_UPLOAD_COMPLETE);
                        event.setResult(uploaded);
                        fireEvent(event);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    // ******************************************************************************* //
    // ******************** PRIVATE METHODS TO HANDLE SOAP WEB SERVICES ************** //
    // ******************************************************************************* //
    private int doGetSocket(String imei, String ip){
        
        HttpTransportSE transporter    = new HttpTransportSE(soapUrl());
        SoapObject request             = new SoapObject(NAMESPACE, METHOD_GET_SOCKET);
        SoapSerializationEnvelope sEnv = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        try { 

            request.addProperty("imei", imei);
            request.addProperty("ipAddress", ip);
            sEnv.setOutputSoapObject(request);
            transporter.call(NAMESPACE + METHOD_GET_SOCKET, sEnv);

            Object response = sEnv.getResponse();
            return Integer.parseInt(response.toString());
        } catch (Exception e) { 
            e.printStackTrace();
            return -2; // meaning that an exception occured 
        } 
    }
    
    private int doGetSurveyId(String imei){
        
        HttpTransportSE transporter    = new HttpTransportSE(soapUrl());
        SoapObject request             = new SoapObject(NAMESPACE, METHOD_GET_SURVEY_ID);
        SoapSerializationEnvelope sEnv = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        try { 

            request.addProperty("imei", imei);
            sEnv.setOutputSoapObject(request);
            transporter.call(NAMESPACE + METHOD_GET_SURVEY_ID, sEnv);

            Object response = sEnv.getResponse();
            return Integer.parseInt(response.toString());
        } catch (Exception e) { 
            e.printStackTrace();
        } 
        
        return -1;
    }
    
    private boolean doProvideSeatNumber(String imei, String seatNumber){
        
        HttpTransportSE transporter    = new HttpTransportSE(soapUrl());
        SoapObject request             = new SoapObject(NAMESPACE,
                                                        METHOD_PROVIDE_SEAT_NUMBER);
        SoapSerializationEnvelope sEnv = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        try { 

            request.addProperty("imei", imei);
            request.addProperty("seatNumber", seatNumber);
            sEnv.setOutputSoapObject(request);
            transporter.call(NAMESPACE + METHOD_PROVIDE_SEAT_NUMBER, sEnv);

            Object response = sEnv.getResponse();
            return Boolean.parseBoolean(response.toString());
        } catch (Exception e) { 
            e.printStackTrace();
        } 
        
        return false;
    }
    
    private boolean doNotifyDeviceDisconnection(String imei){
        
        HttpTransportSE transporter    = new HttpTransportSE(soapUrl());
        SoapObject request             = new SoapObject(NAMESPACE, 
                                                        METHOD_DEVICE_DISCONNECT);
        SoapSerializationEnvelope sEnv = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        try { 

            request.addProperty("imei", imei);
            sEnv.setOutputSoapObject(request);
            transporter.call(NAMESPACE + METHOD_DEVICE_DISCONNECT, 
                             sEnv);

            Object response = sEnv.getResponse();
            return Boolean.parseBoolean(response.toString());
        } catch (Exception e) { 
            e.printStackTrace();
        } 
        
        return false;
    }
    
    private boolean doNotifyFileFailure(String imei, File file){
        
        HttpTransportSE transporter    = new HttpTransportSE(soapUrl());
        SoapObject request             = new SoapObject(NAMESPACE, 
                                                        METHOD_NOTIFY_FILE_FAILURE);
        SoapSerializationEnvelope sEnv = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        try { 

            request.addProperty("imei", imei);
            request.addProperty("file", file.getName());
            sEnv.setOutputSoapObject(request);
            transporter.call(NAMESPACE + METHOD_NOTIFY_FILE_FAILURE, 
                             sEnv);

            Object response = sEnv.getResponse();
            return Boolean.parseBoolean(response.toString());
        } catch (Exception e) { 
            e.printStackTrace();
        } 
        
        return false;
    }

    private boolean doNotifyException(String imei, String exception){
        
        HttpTransportSE transporter    = new HttpTransportSE(soapUrl());
        SoapObject request             = new SoapObject(NAMESPACE, 
                                                        METHOD_NOTIFY_APP_EXCEPTION);
        SoapSerializationEnvelope sEnv = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        try { 

            request.addProperty("imei", imei);
            request.addProperty("exception", exception);
            sEnv.setOutputSoapObject(request);
            transporter.call(NAMESPACE + METHOD_NOTIFY_APP_EXCEPTION,
                             sEnv);

            Object response = sEnv.getResponse();
            return Boolean.parseBoolean(response.toString());
        } catch (Exception e) { 
            e.printStackTrace();
        } 
        
        return false;
    }

    private boolean doServerCmdReceived(String imei, int cmd){
    	
        try {
            HttpClient client         = new DefaultHttpClient();
            HttpPost post             = new HttpPost(notifyServletUrl());
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode
                                                            .BROWSER_COMPATIBLE);
            reqEntity.addPart("command", new StringBody(Integer.toString(cmd)));
            reqEntity.addPart("imei", new StringBody(imei));
           
            post.setEntity(reqEntity);
            HttpResponse response = client.execute(post);
            HttpEntity resEntity  = response.getEntity();
           
            if (resEntity != null) {
                return true;
            }
        } catch (Exception e) {
           e.printStackTrace();
        }                        
        return false;
    	
    	
//        HttpTransportSE transporter    = new HttpTransportSE(soapUrl());
//        SoapObject request             = new SoapObject(NAMESPACE, 
//                                                        METHOD_SERVER_CMD_RECEIVED);
//        SoapSerializationEnvelope sEnv = new SoapSerializationEnvelope(SoapEnvelope.VER11);
//        try { 
//
//            request.addProperty("imei", imei);
//            request.addProperty("cmd", cmd);
//            sEnv.setOutputSoapObject(request);
//            transporter.call(NAMESPACE + METHOD_SERVER_CMD_RECEIVED,
//                             sEnv);
//
//            Object response = sEnv.getResponse();
//            return Boolean.parseBoolean(response.toString());
//        } catch (Exception e) { 
//            e.printStackTrace();
//        } 
//        
//        return false;
    }

    @SuppressLint("DefaultLocale")
    private String doGetQuestionnaire(String imei){
        
        SoapObject request             = new SoapObject(NAMESPACE, 
                                                        METHOD_GET_QUESTIONNAIRE);
        SoapSerializationEnvelope sEnv = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        sEnv.setAddAdornments(false);
        sEnv.implicitTypes = true;
        
        try { 

            request.addProperty("imei", imei);
            sEnv.setOutputSoapObject(request);
            HttpTransportSE transporter = new HttpTransportSE(soapUrl(), 
                                                                 60000);
            transporter.call(NAMESPACE + METHOD_GET_QUESTIONNAIRE, 
                             sEnv);
            Object response = sEnv.getResponse();
            if(response.toString().equals("") || 
                            response.toString().toLowerCase().contains("anytype"))
                return "";
            
            else 
                return response.toString();
        } catch (Exception e) { 
            e.printStackTrace();
        } 
        
        return "";
    }
    
    private boolean doSubmitQuestionnaire(String imei, int surveyInstanceId, String json){
        
        HttpTransportSE transporter    = new HttpTransportSE(soapUrl());
        SoapObject request             = new SoapObject(NAMESPACE, 
                                                        METHOD_SUBMIT_QUESTIONNAIRE);
        SoapSerializationEnvelope sEnv = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        try { 

            request.addProperty("imei", imei);
            request.addProperty("surveyInstanceId", surveyInstanceId);
            request.addProperty("questionnaire", json);
            sEnv.setOutputSoapObject(request);
            transporter.call(NAMESPACE + METHOD_SUBMIT_QUESTIONNAIRE, 
                             sEnv);
            Object response = sEnv.getResponse();
            return Boolean.parseBoolean(response.toString());
        } catch (Exception e) { 
            e.printStackTrace();
        } 
        
        return false;
    }
    
    private boolean doUploadFile(FileUploadStatus fus, String imei, int surveyId) {
        
        try {
            HttpClient client         = new DefaultHttpClient();
            HttpPost post             = new HttpPost(uploadServletUrl());
            FileBody bin              = new FileBody(fus.getFile());
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode
                                                            .BROWSER_COMPATIBLE);
            reqEntity.addPart("uploaded_file", bin);
            reqEntity.addPart("imei", new StringBody(imei));
            reqEntity.addPart("survey_id", new StringBody(Integer.toString(surveyId)));
            reqEntity.addPart("is_last_file", new StringBody(Boolean.toString(fus.isLastFile())));
           
            post.setEntity(reqEntity);
            HttpResponse response = client.execute(post);
            HttpEntity resEntity = response.getEntity();
           
            if (resEntity != null) {
                return true;
            }
        } catch (Exception e) {
           e.printStackTrace();
        }                        
        return false;
    }
    
}
