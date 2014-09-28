package tr.edu.metu.ii.aaa.activities;

import tr.edu.metu.ii.aaa.R;
import tr.edu.metu.ii.aaa.core.AnalysisApp;
import tr.edu.metu.ii.aaa.core.Constants;
import tr.edu.metu.ii.aaa.events.AnalysisEvent;
import tr.edu.metu.ii.aaa.events.AnalysisEventListener;
import tr.edu.metu.ii.aaa.events.SoapEvent;
import tr.edu.metu.ii.aaa.gson.GsonQuestionList;
import tr.edu.metu.ii.aaa.gui.QuestionnaireAdapter;
import tr.edu.metu.ii.aaa.services.ManagerService;
import tr.edu.metu.ii.aaa.services.ManagerService.ManagerSBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class QuestionnaireAc extends Activity implements ServiceConnection,
                                                         AnalysisEventListener{

    private AnalysisApp          _app;
    private boolean              _binded  = false;
    private ManagerService       _service = null;
    private QuestionnaireAdapter _adapter = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        System.out.println(" ------------- QuestionnaireAc.onCreate() ----------------- ");
        _app = (AnalysisApp) getApplication();
        setContentView(R.layout.questionnaire_layout);
        initGui();
    }

    @Override
    protected void onDestroy() {

        if(_binded && _service != null)
            disconnectService();
        super.onDestroy();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
        if(!_binded)
            connectToService();
    }
    
    // ******************************************************************************* //
    // ***************************** INITIALIZATION METHODS ************************** //
    // ******************************************************************************* //
    private void initGui(){
        
        String ques = _app.getQuestionnaire();
        if(ques == null){
            
            Intent intent = new Intent(Constants.INTENT_STATUS_AC);
            startActivity(intent);
            finish();
        }
        GsonQuestionList data = _app.getQuestionnaireGson().fromJson(_app.getQuestionnaire(), 
                                                                     GsonQuestionList.class);
        
        ListView questionLv = (ListView) findViewById(R.id.questionnaire_lv);
        _adapter = new QuestionnaireAdapter(this, data);
        questionLv.setAdapter(_adapter);
//
//        if(getIntent().getExtras().containsKey(Constants.EXTRA_QUESTIONNAIRE)){
//            
//            String questionnaire  = getIntent().getExtras()
//                                               .getString(Constants.EXTRA_QUESTIONNAIRE);
//            System.out.println("QuestionnaireAc.initGui() : " + questionnaire); 
//            _adapter.notifyDataSetChanged();
//            
//        }
//        
        // init the button´s functionality
        Button nextBtn = (Button) findViewById(R.id.questionnaire_next_btn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                if(!_adapter.areAllQuestionsAnswered()){
                    
                    Toast.makeText(QuestionnaireAc.this, 
                                   R.string.msg_no_answers, 
                                   Toast.LENGTH_LONG).show();
                    return;
                }
                if(_service != null){
                    _service.sendQuestionnaire(_adapter.getGsonAnswers());
                    Toast.makeText(QuestionnaireAc.this, 
                                   R.string.msg_sending_data, 
                                   Toast.LENGTH_LONG).show();
                } else {
                    
                    Toast.makeText(QuestionnaireAc.this, 
                                   R.string.msg_app_not_ready, 
                                   Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // ******************************************************************************* //
    // ***************************** CONNECTION TO SERVICE *************************** //
    // ******************************************************************************* //
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        
        ManagerSBinder binder = (ManagerSBinder) service;
        synchronized (this) {
            
            if(binder.getService()!= null){
                
                _service = binder.getService();
                _binded  = true;
                _service.registerAnalysisListener(this);
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        
        _binded = false;
        _service.unregisterAnalysisListener(this);
    }
    
    private void connectToService(){
        
        bindService(new Intent(this.getApplicationContext(), 
                               ManagerService.class), 
                               this, 
                               Context.BIND_AUTO_CREATE);
    }

    private void disconnectService(){
        
        unbindService(this);
        _binded = false;
        _service.unregisterAnalysisListener(this);
    }
    
    // ******************************************************************************* //
    // ***************************** HANDLE APPLICATION EVENTS *********************** //
    // ******************************************************************************* //
    @Override
    public void onAnalysisEvent(AnalysisEvent event) {

        if(event instanceof SoapEvent){
            
            SoapEvent soapE = (SoapEvent) event;
            if(soapE.getName().equals(SoapEvent.QUEST_SENT)){
              
                if(((Boolean)soapE.getResult())){
                    
                    _app.setQuestionnaireSubmitted(true);
                    Toast.makeText(this, 
                                   R.string.msg_questionnaire_sent, 
                                   Toast.LENGTH_LONG).show();
                } else {
                  
                    Toast.makeText(this, 
                                   R.string.msg_questionnaire_not_sent, 
                                   Toast.LENGTH_LONG).show();
                }
                // send questionnaire to the server 
                startActivity(new Intent(Constants.INTENT_STATUS_AC));
                finish();
            } 
        }
    }
}
