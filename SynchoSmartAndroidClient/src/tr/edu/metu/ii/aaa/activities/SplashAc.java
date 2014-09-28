package tr.edu.metu.ii.aaa.activities;

import tr.edu.metu.ii.aaa.R;
import tr.edu.metu.ii.aaa.core.AnalysisApp;
import tr.edu.metu.ii.aaa.core.Constants;
import tr.edu.metu.ii.aaa.dialogs.ServerIpDialog;
import tr.edu.metu.ii.aaa.events.AnalysisEvent;
import tr.edu.metu.ii.aaa.events.AnalysisEventListener;
import tr.edu.metu.ii.aaa.events.ServiceEvent;
import tr.edu.metu.ii.aaa.events.SoapEvent;
import tr.edu.metu.ii.aaa.services.ManagerService;
import tr.edu.metu.ii.aaa.services.ManagerService.ManagerSBinder;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.test.ActivityTestCase;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;


public class SplashAc extends FragmentActivity implements AnalysisEventListener, 
                                                          ServiceConnection{

    private ProgressDialog   _dialog  = null;
    private ManagerService   _service = null;
    private volatile boolean _binded  = false;
    
    private ImageView        _attIv   = null;
    private EditText         _attEt   = null;
    
    private AnalysisApp      _app     = null;
    private int              _socket  = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        _app = (AnalysisApp) getApplication();
        setContentView(R.layout.splash_layout);
        _attIv = (ImageView)findViewById(R.id.attention_iv);
        _attEt = (EditText) findViewById(R.id.attention_tv);
        initProgressDialog();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onPause() {

        super.onPause();
        
        if(_service != null)
            _service.unregisterAnalysisListener(this);

        if(_binded){
            
            unbindService(this);
            _binded = false;
        }
        if(_dialog != null && _dialog.isShowing())
            _dialog.dismiss();
    }

    @Override
    protected void onResume() {

        super.onResume();
        
        if(Constants.REALEASE_MODE){
        	
		    if(!_app.isAirplaneModeOn()){
		    	
		        _attIv.setVisibility(View.VISIBLE);
		        _attEt.setVisibility(View.VISIBLE);
		        _attEt.setHint(R.string.msg_airplane_mode);
		        _attEt.setEnabled(false);
		        return;
		    } else {
	        	startService(new Intent(Constants.INTENT_MANAGER_SERVICE));
		    }
        } else {
        	startService(new Intent(Constants.INTENT_MANAGER_SERVICE));
        }
        
        if(!_binded)
            bindService(new Intent(this.getApplicationContext(), 
                                   ManagerService.class), 
                                   this,
                                   Context.BIND_AUTO_CREATE);
        // if there is no internet just notify the user abt it.
        if(!_app.isNetworkOn()){
            
            _attIv.setVisibility(View.VISIBLE);
            _attEt.setVisibility(View.VISIBLE);
            _attEt.setHint(R.string.msg_no_internet);
            _attEt.setEnabled(false);
            return;
        }
    }
    
    // ******************************************************************************* //
    // ********************************** MENU RELATED METHODS *********************** //
    // ******************************************************************************* //
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_experiment, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
            
            case R.id.force_stop:

                return true;
            
            case R.id.reconnect:
                
                connectToServer();
                return true;
            
            case R.id.server_ip:
                
                ServerIpDialog dialog = new ServerIpDialog(_app);
                dialog.show(getSupportFragmentManager(), 
                            "server_ip_dialog");
                return true;
            
            default:
                return false;
        }
    }
    
    // ******************************************************************************* //
    // ***************************** PROTECTED METHODS ******************************* //
    // ******************************************************************************* //
    protected void initProgressDialog(){
        
        _dialog = new ProgressDialog(this);
        _dialog.setCancelable(true);
        _dialog.setIndeterminate(true);
        _dialog.setMessage(getText(R.string.msg_connecting));
    }

    // ******************************************************************************* //
    // ************************* EVENTS & CONNECTION HANDLERS ************************ //
    // ******************************************************************************* //
    @Override
    public void onAnalysisEvent(final AnalysisEvent event) {

        if(event instanceof SoapEvent){
            final SoapEvent soapE = (SoapEvent) event;
            if(soapE.getName().equals(SoapEvent.SOCKET_RECEIVED)){
                
                System.out.println(SplashAc.class.getName() + ": socket number received from server ");
                // if we were able to receive a socket number it means that the server is running and the 
                // device is available to join the ongoing survey so we can go to the next activity which
                // will provide the seat number
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                
                        _dialog.hide();
                        _socket = (Integer)soapE.getResult();
                        if(_socket == 0 || _socket == -1){
                            _attEt.setHint(R.string.msg_socket_number_error);
                            _attEt.setVisibility(View.VISIBLE);
                            _attIv.setVisibility(View.VISIBLE);
                        } else if(_socket == -2){ // try again. temporary connection error
                        	
                            _attEt.setHint(R.string.msg_socket_conn_error);
                            _attEt.setVisibility(View.VISIBLE);
                            _attIv.setVisibility(View.VISIBLE);
                        }
                        else {
                            _app.setServerSocket(_socket);
                        }
                    }
                });
            } else if(soapE.getName().equals(SoapEvent.SURVEY_ID_RECEIVED)){
                
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                
                        int surveyId = (Integer)soapE.getResult();
                        if(surveyId > 0 && _socket > 0)
                            goToNextActivity(surveyId);
                    }
                });
            }
        } else if(event instanceof ServiceEvent){
            
            runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    ServiceEvent serviceE = (ServiceEvent) event;
                    if(serviceE.getName().equals(ServiceEvent.SERVER_SERVICE_CON)){
                        if(!_dialog.isShowing())
                            connectToServer();
                    }
                }
            });
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
 
        _service = ((ManagerSBinder)service).getService();
        if(_service != null){
            
            _binded = true;
            _service.registerAnalysisListener(this);
            if(_dialog != null && !_dialog.isShowing())
                connectToServer();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        
        _binded = false;
    }
    
    private void goToNextActivity(int surveyId){
        
        // if we have different surveys it means that we have started
        // a new survey so we need to process through all activities
        if(_dialog.isShowing())
            _dialog.hide();
        
        Intent intent = null;
        System.out.println(SplashAc.this.getClass().getName() + ": Received survey id : " + surveyId);
        System.out.println(SplashAc.this.getClass().getName() + ": Survey id from the application : " + _app.getSurveyId());
        if(surveyId != _app.getSurveyId()){
            
            _app.setSurveyId(surveyId);
            intent = new Intent(Constants.INTENT_SEAT_NUMBER_AC);
        } else {
            
            // if we have the same survey but we didnt provide the seat number
            // we need to go to the seat number activity
            if(!_app.isSeatNumberProvided())
                intent = new Intent(Constants.INTENT_SEAT_NUMBER_AC);
            else {
                if(_app.hasQuestionnaire()){
                    if(!_app.questionnaireSubmitted())
                        intent = new Intent(Constants.INTENT_QUESTIONNAIRE_AC);
                    else 
                        intent = new Intent(Constants.INTENT_STATUS_AC);
                } else 
                    intent = new Intent(Constants.INTENT_STATUS_AC);
            }
        }
        if(intent != null){
        	// if the application has crashed prior to this lunch if the user is able to continue
        	// to the setup activity (meaning that there is an ongoing SURVEY) we have to check and 
        	// send the files if the application has crashed.
        	System.out.println(" --------------- crash (before) ---------------- " + _app.didAppCrash());
        	System.out.println(" --------------- intent (before) ---------------- " + intent.getAction());

        	if(!intent.getAction().equals(Constants.INTENT_STATUS_AC))
        		_app.setAppCrashed(false);
        	else if(_app.didAppCrash()) // if the app crashed before send the new data file and go to status activity
        		_service.sendDataFiles();
        	
        	System.out.println(" --------------- crash (after) ---------------- " + _app.didAppCrash());
        	System.out.println(" --------------- intent (after) ---------------- " + intent.getAction());
            startActivity(intent);
            finish();
        }
    }
    
    private void connectToServer(){
        
        if(_service != null){
            
            _service.getSocketNumber();
            synchronized (this) {
                
                boolean sent = _service.obtainSurveyId();
                if(sent)
                    _dialog.show();
            }
        }
    }
}
