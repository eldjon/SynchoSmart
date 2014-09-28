package tr.edu.metu.ii.aaa.sensors;

import tr.edu.metu.ii.aaa.sensors.SensorDescriptor.SensorNames;
import tr.edu.metu.ii.aaa.services.DataCollectorService.SensorRecord;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

public class AudioSensor {

    public static final int SAMPLE_RATE = 44100;
    
    private AudioRecord            _audioRecorder  = null;
    private AnalysisSensorListener _listener       = null;
    private Thread                 _recorderThread = null;
    private short[]                _buffer         = null;
    
    public AudioSensor(){
    	
    	int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, 
				  									  AudioFormat.CHANNEL_IN_MONO, 
				  									  AudioFormat.ENCODING_PCM_16BIT);
    	_buffer        = new short[bufferSize];
    	_audioRecorder = new AudioRecord(AudioSource.MIC, 
    									 SAMPLE_RATE, 
    									 AudioFormat.CHANNEL_CONFIGURATION_MONO, 
    									 AudioFormat.ENCODING_PCM_16BIT, 
    									 bufferSize);
    }
    
    public synchronized void startRecording(){
    	
    	System.out.println(" ================================ START recording audio ================================ ");
    	initAudioRecord();
    	initRecordingThread();
    	_recorderThread.start();
    }
    
    public synchronized void stopRecording(){
    	
    	System.out.println(" ================================ STOP recording audio =========================== ");
    	if(_recorderThread != null && !_recorderThread.isInterrupted())
    		_recorderThread.interrupt();
    	_recorderThread = null;
    	clearRecorder();
    	_buffer   = null;
    	_listener = null;
    }
    
    public synchronized void pauseRecording(){
    	
    	System.out.println(" ================================ PAUSE recording audio =========================== ");
    	_audioRecorder.stop();
    	if(_recorderThread != null && !_recorderThread.isInterrupted())
    		_recorderThread.interrupt();
    	_recorderThread = null;
    }
    
    public synchronized void resumeRecording(){
    	
    	System.out.println(" ================================ RESUME recording audio =========================== ");
    	initAudioRecord();
    	initRecordingThread();
    	_recorderThread.start();
    }
    
    public void setSensorListener(AnalysisSensorListener listener){
    	
    	_listener = listener;
    }
    
    // ******************************************************************************* //
    // ************************ AUDIO RECORDER RELATED METHODS *********************** //
    // ******************************************************************************* //
    private void initRecordingThread(){
    
    	_recorderThread = new Thread(new RecorderRunnable(),
    								 "AUDIO_RECORDER_THREAD");
    }
    
    private void initAudioRecord(){
    	
    	_audioRecorder.setPositionNotificationPeriod(_buffer.length);
    	
    	_audioRecorder.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
			
			@Override
			public void onPeriodicNotification(AudioRecord recorder) {
				
				if(_listener == null)
					return; 
				
				int total = 0;
				for(int i = 0; i < _buffer.length; i++)
					total += Math.abs(_buffer[i]);
				
				float avg = total/_buffer.length;
				avg = (float) (20 * Math.log10(avg/32768)); // convert average voltage to Decibel
				
				SensorRecord record = new SensorRecord();
				record._type        = SensorNames.AUDIO.getSensorType();
				record._timestamp   = System.currentTimeMillis();
				record._values      = new float[]{avg};
				_listener.onSensorRecord(record);
			}
			
			@Override
			public void onMarkerReached(AudioRecord recorder) {
				
			}
		});
    	
//    	FORMULA FOR CALCULATING SOUND LEVEL IN DECIBEL FROM SAMPLES RECORDED BY AUDIO RECORDER
//    	float Db = 20 * log10(ABS(sampleVal) / 32768)
    }
    
    private void clearRecorder(){
    	
    	System.out.println(" ---------------------------- Clear Audio recorder ------------------------------------ ");
    	if(_audioRecorder != null){
        	synchronized (_audioRecorder) {
    			try {
        			_audioRecorder.stop();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
				    	System.out.println(" ---------------------------- release recorder ------------------------------------ ");
	            		_audioRecorder.release();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
    		}
    	}
    }
    
    private class RecorderRunnable implements Runnable {

		@Override
		public void run() {
			
			_audioRecorder.startRecording();
			while(true){
				
				if(Thread.currentThread().isInterrupted())
					return;
				
				synchronized (AudioSensor.this) {
					
					if(_audioRecorder != null && _buffer != null)
						_audioRecorder.read(_buffer, 0, _buffer.length);
				}
				
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
				}
			}
		}
    }
}
