package tr.edu.metu.ii.aaa.activities;

import tr.edu.metu.ii.aaa.R;
import tr.edu.metu.ii.aaa.events.AnalysisEvent;
import tr.edu.metu.ii.aaa.events.AnalysisEventListener;
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

public class SensorTestAc extends Activity implements ServiceConnection, AnalysisEventListener{

    private ManagerService   _service = null;
    private volatile boolean _binded  = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensor_test_layout);
		initGui();
        if(!_binded)
            bindService(new Intent(this.getApplicationContext(), 
                                   ManagerService.class), 
                                   this,
                                   Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
        if(_binded){
            
            unbindService(this);
            _binded = false;
        }
		super.onDestroy();
	}

	protected void initGui(){
		
		Button startBtn = (Button) findViewById(tr.edu.metu.ii.aaa.R.id.start_btn);
		Button stopBtn  = (Button) findViewById(tr.edu.metu.ii.aaa.R.id.stop_btn);
		
		startBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				_service.startDataCollection();
			}
		});
		
		stopBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				_service.stopDataCollection();
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
	}

	@Override
	public void onAnalysisEvent(AnalysisEvent event) {
		// TODO Auto-generated method stub
	}
}
