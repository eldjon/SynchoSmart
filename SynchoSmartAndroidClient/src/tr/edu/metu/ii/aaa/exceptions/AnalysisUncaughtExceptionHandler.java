package tr.edu.metu.ii.aaa.exceptions;

import java.lang.Thread.UncaughtExceptionHandler;

import tr.edu.metu.ii.aaa.core.AnalysisApp;
import tr.edu.metu.ii.aaa.events.AnalysisEvent;
import tr.edu.metu.ii.aaa.events.AnalysisEventListener;
import tr.edu.metu.ii.aaa.events.SoapEvent;
import tr.edu.metu.ii.aaa.services.SoapConnector;

public class AnalysisUncaughtExceptionHandler implements UncaughtExceptionHandler, AnalysisEventListener{
    
    private UncaughtExceptionHandler _defaultEH; 
    private AnalysisApp              _app;
    private Thread                   _thread;
    private Throwable                _exception;
    
    public AnalysisUncaughtExceptionHandler(AnalysisApp app){
        
        _defaultEH = Thread.getDefaultUncaughtExceptionHandler();
        _app       = app;
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        _thread    = thread;
        _exception = ex;
        doHandleException(thread, ex);
    }
    
    private void doHandleException(Thread thread, Throwable exception){
        
    	exception.printStackTrace();
    	//set an indicator that application crashed before
    	_app.setAppCrashed(true);
        // vibrate the phone for a second
        _app.vibrate(1000);
        // deliver the exception to the server 
        SoapConnector sc = new SoapConnector(_app);
        sc.registerAnalysisListener(this);
        sc.notifyAppException(exception.getMessage());
    }

    @Override
    public void onAnalysisEvent(AnalysisEvent event) {

        if(event instanceof SoapEvent){
            
            SoapEvent se = (SoapEvent) event;
            if(se.getName().equals(SoapEvent.APP_EXCEPTION_NOTIFY))
                _defaultEH.uncaughtException(_thread, _exception);
        }
    }
}
