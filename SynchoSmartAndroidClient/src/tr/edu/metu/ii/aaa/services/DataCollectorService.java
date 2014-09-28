package tr.edu.metu.ii.aaa.services;

import tr.edu.metu.ii.aaa.cache.CacheManager;
import tr.edu.metu.ii.aaa.core.AnalysisApp;
import tr.edu.metu.ii.aaa.core.AnalysisObject;
import tr.edu.metu.ii.aaa.events.AnalysisEvent;
import tr.edu.metu.ii.aaa.events.AnalysisEventListener;
import tr.edu.metu.ii.aaa.sensors.AnalysisSensorListener;
import tr.edu.metu.ii.aaa.sensors.AudioSensor;
import tr.edu.metu.ii.aaa.sensors.SensorDescriptor.SensorNames;
import tr.edu.metu.ii.aaa.utils.DateUtils;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;


public class DataCollectorService extends Service 
                                  implements AnalysisObject,
                                  SensorEventListener, 
                                  AnalysisSensorListener{

    protected static final int START_DATA_COLLECTION = 1;
    protected static final int SENSOR_RATE = SensorManager.SENSOR_DELAY_GAME; // microseconds
    
    private IBinder      _binder    = new DCSBinder();
    private CacheManager _cacheMan  = null;
    private AnalysisApp  _app       = null;
    private SensorManager _sManager = null;

    private int[] _selectedSensors  = null;
    
    private long _serverStartTime   = -1;
    private long _appStartTime      = -1;
    private int  _timeDiff          = 0;
    
    // For Audio recording
    private AudioSensor _audioSensor = null;

    // ******************************************************************************* //
    // ****************************** CREATE/DESTROY ********************************* //
    // ******************************************************************************* //
    @Override
    public IBinder onBind(Intent intent) {

        return _binder;
    }
    
    @Override
    public void onCreate() {

        super.onCreate();
        _app         = (AnalysisApp) getApplication();
        _sManager    = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        createSensors();
        _cacheMan    = new CacheManager(_app, _selectedSensors);
        _audioSensor = new AudioSensor();
    }

    @Override
    public void onDestroy() {
        
        System.out.println("************************* DESTROYING DATA_COLLECTOR_SERVICE ********************* ");
        if(_cacheMan != null)
            _cacheMan.clear();

        _cacheMan    = null;
        _audioSensor = null;
        super.onDestroy();
    }

    // ******************************************************************************* //
    // ******************* METHODS TO BE CALLED BY MANAGER SERVICE ******************* //
    // ******************************************************************************* //
    public void startDataCollection() throws IllegalStateException{
        
        registerSensors();
        
        for(int i = 0; i < _selectedSensors.length; i++)
        	if(_selectedSensors[i] == SensorNames.AUDIO.getSensorType()){
	    		_audioSensor.setSensorListener(this);
	    		_audioSensor.startRecording();
        	} 
        _cacheMan.start();
    }
    
    public void stopDataCollection() throws IllegalStateException{
        
        try {
            for(int i = 0; i < _selectedSensors.length; i++)
            	if(_selectedSensors[i] == SensorNames.AUDIO.getSensorType()){
    	    		_audioSensor.setSensorListener(this);
    	    		_audioSensor.stopRecording();
            	} 
            unregisterSensors();
            _cacheMan.stop();
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    public void pauseDataCollection() throws IllegalStateException{
        
    	try {
            for(int i = 0; i < _selectedSensors.length; i++)
            	if(_selectedSensors[i] == SensorNames.AUDIO.getSensorType()){
    	    		_audioSensor.setSensorListener(this);
    	    		_audioSensor.pauseRecording();
            	} 
            unregisterSensors();
            _cacheMan.pause();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void resumeDataCollection() throws IllegalStateException{
    	
        for(int i = 0; i < _selectedSensors.length; i++)
        	if(_selectedSensors[i] == SensorNames.AUDIO.getSensorType()){
	    		_audioSensor.setSensorListener(this);
	    		_audioSensor.resumeRecording();
        	} 
        
        registerSensors();
        _cacheMan.resume();
    }
        
    public synchronized boolean isRunning(){
        
        return _cacheMan.isRunning();
    }
    
    public void resetServerTime(long serverTime){
    
    	_appStartTime    = System.currentTimeMillis();
    	_serverStartTime = serverTime;
    	_timeDiff = (int) (_serverStartTime - _appStartTime);
    	System.out.println(" ------------ server time ---------- " + _serverStartTime + " " + DateUtils.convertToDate(_serverStartTime));
    	System.out.println(" ------------ app time ---------- " + _appStartTime + " " + DateUtils.convertToDate(_appStartTime));
    	System.out.println(" ------------ difference time ---------- " + _timeDiff);
    }
    
    public long getServerTime(){
    	
    	return _serverStartTime;
    }
    // ******************************************************************************* //
    // **************************** SENSORS OBJECT METHODS *************************** //
    // ******************************************************************************* //
    @Override
    public void fireEvent(AnalysisEvent event) {

    }

    @Override
    public void registerAnalysisListener(AnalysisEventListener listener) {

        _cacheMan.registerAnalysisListener(listener);
    }

    @Override
    public void unregisterAnalysisListener(AnalysisEventListener listener) {

        _cacheMan.unregisterAnalysisListener(listener);
    }    
    
    @Override
    public void clearListeners() {

        _cacheMan.clearListeners();
    }
    

    // ******************************************************************************* //
    // ********************************* BINDER CLASS ******************************** //
    // ******************************************************************************* //
    public class DCSBinder extends Binder{
       
        public DataCollectorService getService(){
            
            return DataCollectorService.this;
        }
    }
    
    
    // ******************************************************************************* //
    // ************************ PRIVATE AND PROTECTED METHODS ************************ //
    // ******************************************************************************* //
    private void createSensors(){

        _selectedSensors    = new int[7];
        _selectedSensors[0] = SensorNames.ACCELEROMETER.getSensorType();
        _selectedSensors[1] = SensorNames.GYROSCOPE.getSensorType();
        _selectedSensors[2] = SensorNames.LINEAR_ACCELERATION.getSensorType();
        _selectedSensors[3] = SensorNames.ORIENTATION.getSensorType();
        _selectedSensors[4] = SensorNames.MAGNETIC_FIELD.getSensorType();
        _selectedSensors[5] = SensorNames.ROTATION_VECTOR.getSensorType();
        _selectedSensors[6] = SensorNames.AUDIO.getSensorType();
    }
    
    private void registerSensors(){

        for(int i = 0; i < _selectedSensors.length; i++){
            Sensor s = _sManager.getDefaultSensor(_selectedSensors[i]);
            _sManager.registerListener(this, s, SENSOR_RATE);
        }
    }
    
    private void unregisterSensors(){
        _sManager.unregisterListener(this);
    }

    // ******************************************************************************* //
    // ***************************** HANDLING SENSOR EVENTS ************************** //
    // ******************************************************************************* //
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
            return;
        for(int i = 0; i < _selectedSensors.length; i++)
            if(event.sensor.getType() == _selectedSensors[i]){
                
            	if(_timeDiff == 0)
            		event.timestamp = System.currentTimeMillis();
            	else 
            		event.timestamp = System.currentTimeMillis() + _timeDiff;
            	
                _cacheMan.addSensorRecord(new SensorRecord(event));
                return;
            }
    }

	@Override
	public void onSensorRecord(SensorRecord record) {
		
        for(int i = 0; i < _selectedSensors.length; i++)
            if(record._type == _selectedSensors[i]){
            	record._timestamp += _timeDiff;
                _cacheMan.addSensorRecord(record);
                return;
            }
	}


    public static class SensorRecord {
        
        public int     _type;
        public float[] _values;
        public long    _timestamp;

        public SensorRecord(){
        }
        
        public SensorRecord(SensorEvent event){
            
            _type      = event.sensor.getType();
            _values    = new float[event.values.length];
            for(int i = 0; i < event.values.length; i++)
                _values[i] = event.values[i];
            _timestamp = event.timestamp;
        }
    }

}



/*
 * ******************************** EXPLANATIONS ON SENSORS  ****************************
 *    ACCELEROMETER 
 *    alpha is calculated as t / (t + dT)
      with t, the low-pass filter's time-constant
      and dT, the event delivery rate

      final float alpha = 0.8;

      gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
      gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
      gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

      linear_acceleration[0] = event.values[0] - gravity[0]; (the gravity value 9.81m/s^2)
      linear_acceleration[1] = event.values[1] - gravity[1];
      linear_acceleration[2] = event.values[2] - gravity[2];
        
      When the device lies flat on a table and is pushed on its left side toward the right, the x acceleration value is positive.
      When the device lies flat on a table, the acceleration value is +9.81, which correspond to the acceleration of the device (0 m/s^2) 
      minus the force of gravity (-9.81 m/s^2).
      When the device lies flat on a table and is pushed toward the sky with an acceleration of A m/s^2, 
      the acceleration value is equal to A+9.81 which correspond to the acceleration of the device (+A m/s^2) minus the force of gravity (-9.81 m/s^2).

        MAGNETIC FIELD
     All values are in micro-Tesla (uT) and measure the ambient magnetic field in the X, Y and Z axis. 
     
         GYROSCOPE
     All values are in radians/second and measure the rate of rotation around the device's local X, Y and Z axis. 
     The coordinate system is the same as is used for the acceleration sensor. Rotation is positive in the counter-clockwise direction.
     That is, an observer looking from some positive location on the x, y or z axis at a device positioned on the origin would report 
     positive rotation if the device appeared to be rotating counter clockwise. 
     Note that this is the standard mathematical definition of positive rotation and does not agree with the definition of roll given earlier.

     values[0]: Angular speed around the x-axis
     values[1]: Angular speed around the y-axis
     values[2]: Angular speed around the z-axis

        PREASSURE
    
     values[0]: Atmospheric pressure in hPa (millibar)

        PROXIMITY
     values[0]: Proximity sensor distance measured in centimeters 

        GRAVITY
     A three dimensional vector indicating the direction and magnitude of gravity. Units are m/s^2. 
     The coordinate system is the same as is used by the acceleration sensor.
     Note: When the device is at rest, the output of the gravity sensor should be identical to that of the accelerometer.            
 
        LINEAR ACCELEROMETER 
     A three dimensional vector indicating acceleration along each device axis, not including gravity. All values have units of m/s^2. 
     The coordinate system is the same as is used by the acceleration sensor.
     The output of the accelerometer, gravity and linear-acceleration sensors must obey the following relation:
                acceleration = gravity + linear-acceleration            
                
         ROTATION VECTOR
     The rotation vector represents the orientation of the device as a combination of an angle and an axis, in which the device has 
     rotated through an angle θ around an axis <x, y, z>.
     The three elements of the rotation vector are <x*sin(θ/2), y*sin(θ/2), z*sin(θ/2)>, such that the magnitude of the rotation 
     vector is equal to sin(θ/2), and the direction of the rotation vector is equal to the direction of the axis of rotation.
     The three elements of the rotation vector are equal to the last three components of a unit quaternion 
     <cos(θ/2), x*sin(θ/2), y*sin(θ/2), z*sin(θ/2)>.
     Elements of the rotation vector are unitless. The x,y, and z axis are defined in the same way as the acceleration sensor.
     The reference coordinate system is defined as a direct orthonormal basis, where:

     X is defined as the vector product Y.Z (It is tangential to the ground at the device's current location and roughly points East).
     Y is tangential to the ground at the device's current location and points towards magnetic north.
     Z points towards the sky and is perpendicular to the ground.


     values[0]: x*sin(θ/2)
     values[1]: y*sin(θ/2)
     values[2]: z*sin(θ/2)
     values[3]: cos(θ/2)
     values[4]: estimated heading Accuracy (in radians) (-1 if unavailable)


        ORIENTATION
     values[0]: Azimuth, angle between the magnetic north direction and the y-axis, 
     around the z-axis (0 to 359). 0=North, 90=East, 180=South, 270=West
     values[1]: Pitch, rotation around x-axis (-180 to 180), with positive values when the z-axis moves toward the y-axis.
     values[2]: Roll, rotation around the x-axis (-90 to 90) increasing as the device moves clockwise.             
 
 
        TEMPERATURE
     values[0]: ambient (room) temperature in degree Celsius.            
     
 *
 *
 */
