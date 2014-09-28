package tr.edu.metu.ii.aaa.activities;

import java.io.PrintWriter;
import java.util.Date;

import tr.edu.metu.ii.aaa.R;
import tr.edu.metu.ii.aaa.core.AnalysisApp;
import tr.edu.metu.ii.aaa.events.AnalysisEvent;
import tr.edu.metu.ii.aaa.events.AnalysisEventListener;
import tr.edu.metu.ii.aaa.events.SoapEvent;
import tr.edu.metu.ii.aaa.events.SocketEvent;
import tr.edu.metu.ii.aaa.services.ManagerService;
import tr.edu.metu.ii.aaa.services.ManagerService.ManagerSBinder;
import tr.edu.metu.ii.aaa.utils.DateUtils;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class StatusAc extends Activity implements ServiceConnection, AnalysisEventListener{

    private AnalysisApp      _app      = null;
    private ManagerService   _service  = null;
    private volatile boolean _binded   = false;
    private ListView         _statusLv = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_layout);
        _app = (AnalysisApp) getApplication();
        initGui();
        if(!_binded)
            bindService(new Intent(this.getApplicationContext(), 
                                   ManagerService.class), 
                                   this, 
                                   Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {

        try {
            if(_binded){
                _service.unregisterAnalysisListener(this);
                unbindService(this);
                _binded = false;
            }
        } catch (Exception e) {}
        super.onDestroy();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    public void onBackPressed() {

        finish();
        super.onBackPressed();
    }
    
    protected void initGui(){
        
        _statusLv = (ListView) findViewById(R.id.status_ac_lv);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, 
                                               R.layout.status_item_layout);
        _statusLv.setAdapter(listAdapter);
        addStatusMsg("Application running ...");
    }
    
    @SuppressWarnings({ "unchecked" })
    protected void addStatusMsg(final String msg){
        
        runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) _statusLv.getAdapter();
                String dateStr = _app.getSDF().format(new Date(System.currentTimeMillis()));
                adapter.add(dateStr + " | " + msg);
                adapter.notifyDataSetChanged();
                _statusLv.setSelection(_statusLv.getCount() - 1);
            }
        });
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
            
            case R.id.server_ip:
                
                return true;
            
            case R.id.reconnect:
                
                return true;
            
            default:
                return false;
        }
    }
    
    // ******************************************************************************* //
    // ********************************** LISTENERS METHODS ************************** //
    // ******************************************************************************* //
    @Override
    public void onAnalysisEvent(AnalysisEvent event) {

        if(event instanceof SocketEvent){
            
            SocketEvent socketE = (SocketEvent) event;
            doOnSocketEvent(socketE);
        } else if(event instanceof SoapEvent){
            
            SoapEvent soapE = (SoapEvent)event;
            doOnSoapEvent(soapE);
        }
    }

    protected void doOnSocketEvent(SocketEvent event){
        
    	long serverTime = -1;
    	if(_service != null)
    		serverTime = _service.getServerTime();
    	
        if(event.getName().equals(SocketEvent.MSG_START))
            addStatusMsg("Received start Command! " + " Server Time : " + DateUtils.convertToDate(serverTime));
        else if(event.getName().equals(SocketEvent.MSG_PAUSE))
            addStatusMsg("Received pause Command! ");
        else if(event.getName().equals(SocketEvent.MSG_RESUME))
            addStatusMsg("Received resume Command! " + " Server Time : " + DateUtils.convertToDate(serverTime));
        else if(event.getName().equals(SocketEvent.MSG_STOP)){
        	
            addStatusMsg("Received stop Command! ");
            synchronized (this) {
				if(_binded){
					
	            	_binded = false;
	                unbindService(this);
				}
                finish();
			}
        }
        else if(event.getName().equals(SocketEvent.MSG_EMPTY))
            addStatusMsg("Received empty Command (server is running)! ");
    }
    
    protected void doOnSoapEvent(SoapEvent event){
    	
    	if(event.getName().equals(SoapEvent.DATA_UPLOAD_COMPLETE)){
    		addStatusMsg("Data uploaded to the server!");
    	} else if(event.getName().equals(SoapEvent.SERVER_CMD_RECEIVED)){
    		addStatusMsg("Confirmation for receiving cmd was sent to server! " 
    					 + ((Integer)event.getResult()).toString());
    	}
    }
    
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        _service = ((ManagerSBinder)service).getService();
        if(_service != null){
            _binded = true;
            _service.registerAnalysisListener(this);
            _service.openSocketConnection();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

        _service.unregisterAnalysisListener(this);
        _binded = false;
    }
    
}
