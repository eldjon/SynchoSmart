package tr.edu.metu.ii.aaa.sensors;

import java.io.File;

import android.content.Context;
import android.hardware.Sensor;
import au.com.bytecode.opencsv.CSVWriter;
import tr.edu.metu.ii.aaa.services.DataCollectorService.SensorRecord;

public interface SensorDescriptor {

	
	public void init(String dir, int partNumber, Context ctx);
	
	public void writeToFile(SensorRecord record);

	public String getSensorName();
	
	public File getDataFile();
	
	public CSVWriter getCsvWriter();
    
	
	
    public static enum SensorNames{
        
    	AUDIO(15),
        ACCELEROMETER(Sensor.TYPE_ACCELEROMETER),
        GYROSCOPE(Sensor.TYPE_GYROSCOPE),
        LINEAR_ACCELERATION(Sensor.TYPE_LINEAR_ACCELERATION),
        MAGNETIC_FIELD(Sensor.TYPE_MAGNETIC_FIELD),
        ORIENTATION(Sensor.TYPE_ORIENTATION),
        ROTATION_VECTOR(Sensor.TYPE_ROTATION_VECTOR);
        
        private int _type = -1;
        
        private SensorNames(int type){
            
            _type = type;
        }
        
        public int getSensorType(){
            
            return _type;
        }
    }
}
