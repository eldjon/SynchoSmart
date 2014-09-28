package tr.edu.metu.ii.aaa.activities;

import tr.edu.metu.ii.aaa.R;
import tr.edu.metu.ii.aaa.core.AnalysisApp;
import tr.edu.metu.ii.aaa.core.Constants;
import tr.edu.metu.ii.aaa.events.AnalysisEvent;
import tr.edu.metu.ii.aaa.events.AnalysisEventListener;
import tr.edu.metu.ii.aaa.events.SoapEvent;
import tr.edu.metu.ii.aaa.services.ManagerService;
import tr.edu.metu.ii.aaa.services.ManagerService.ManagerSBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class SeatNumberAc extends Activity implements ServiceConnection,
                                                      AnalysisEventListener{

    private EditText         _seatNr  = null;
    private EditText         _msgEt   = null;
    
    private AnalysisApp      _app     = null;
    private ManagerService   _service = null;
    private volatile boolean _binded  = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.seat_number_layout);
        _app = (AnalysisApp) getApplication();
        initGui();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onPause() {

        super.onPause();
        if(_binded){
        	
        	_binded = false;
            unbindService(this);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        _msgEt.setVisibility(View.INVISIBLE);
        if(!_binded)
            bindService(new Intent(this.getApplicationContext(), 
                                   ManagerService.class), 
                                   this, 
                                   Context.BIND_AUTO_CREATE);
    }
    
    protected void initGui(){
        
        _seatNr = (EditText) findViewById(R.id.seat_number_tv);
        _msgEt  = (EditText) findViewById(R.id.error_msg_et);
        Button submitSeatNrBtn = (Button) findViewById(R.id.next_btn);
        
        submitSeatNrBtn.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
        
            	// every survey has to go only once through this activity because
            	// every user has to provide the seat number. Therefore we reset the
            	// application crashed preferences.
            	_app.setAppCrashed(false);
                if(_seatNr.getText() == null || 
                                _seatNr.getText().toString().equals("")){
                    _msgEt.setVisibility(View.VISIBLE);
                    _msgEt.setHint(R.string.msg_empty_text);
                    return;
                }
                
                if(_service == null){
                    
                    _msgEt.setVisibility(View.VISIBLE);
                    _msgEt.setHint(R.string.msg_app_not_ready);
                    return;
                }
                _service.sendSeatNumber(_seatNr.getText().toString());
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        _service = ((ManagerSBinder)service).getService();
        if(_service != null){
            _binded = true;
            _service.registerAnalysisListener(this);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        
        _binded = false;
        _service.unregisterAnalysisListener(this);
    }

    @Override
    public void onAnalysisEvent(AnalysisEvent event) {

        if(event instanceof SoapEvent){
            final SoapEvent soapE = (SoapEvent) event;
            if(soapE.getName().equals(SoapEvent.SEAT_NUMBER_SENT)){
                
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                
                        boolean seatNrSent = (Boolean)soapE.getResult();
                        if(seatNrSent){
                            
                            _app.setSeatProvided(true);
                            _service.getQuestionnaire();
                        }else{
                            AlertDialog dialog = new AlertDialog.Builder(SeatNumberAc.this)
                                                                .setMessage(R.string.msg_dublicate_seat_nr)
                                                                .setPositiveButton("Close", 
                                                                                   new DialogInterface.OnClickListener() {
                                                                    
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, 
                                                                                        int which) {
                                                                        dialog.dismiss();
                                                                    }
                                                                })
                                                                .create();
                            dialog.show();
                        }
                    }
                });
            } else if(soapE.getName().equals(SoapEvent.QUEST_RECEIVED)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        
                        String json = (String)soapE.getResult();

                        if(json.equals("")){
                            
                            _app.setHasQuestionnaire(false);
                            Intent intent = new Intent(Constants.INTENT_STATUS_AC);
                            startActivity(intent);
                            Toast.makeText(SeatNumberAc.this, 
                                           R.string.msg_no_questionnaire, 
                                           Toast.LENGTH_LONG).show();
                        } else {
                            
                            _app.setHasQuestionnaire(true);
                            _app.setQuestionnaire(json);
                            Intent intent = new Intent(Constants.INTENT_QUESTIONNAIRE_AC);
                            startActivity(intent);
                        }
                        finish();
                    }
                });
            }
            
        }
    }
}
