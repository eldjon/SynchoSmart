package tr.edu.metu.ii.aaa.sensors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import tr.edu.metu.ii.aaa.R;
import tr.edu.metu.ii.aaa.services.DataCollectorService.SensorRecord;
import tr.edu.metu.ii.aaa.utils.DateUtils;
import android.content.Context;
import android.hardware.Sensor;
import au.com.bytecode.opencsv.CSVWriter;


public class BasicSensorDescriptor implements SensorDescriptor{
    
    public static final String PART_SUFFIX = "_part_";
    
    private SensorNames _name;
    private CSVWriter _csvWriter = null;
    private File      _dataFile  = null;
    
    public BasicSensorDescriptor(int sensor){
        
        for(SensorNames sn : SensorNames.values())
            if(sn.getSensorType() == sensor)
                _name = sn;
    }
    
    @Override
    public void init(String dir, int partNumber, Context ctx){

        if(partNumber <= 0)
            partNumber = 1;
        
        String path = dir.concat("/").concat(_name.name() + "_")
                                     .concat(PART_SUFFIX)
                                     .concat(Integer.toString(partNumber))
                                     .concat(".csv");
        _dataFile   = new File(path);
        try {
            _csvWriter   = new CSVWriter(new FileWriter(_dataFile));
            
            String[] csvString = new String[6];
            // prepare the columns names 
            csvString[0] = ctx.getString(R.string.lbl_date_time);
            
            if(_name.getSensorType() == Sensor.TYPE_ACCELEROMETER){
                
                csvString[1] = ctx.getString(R.string.lbl_acceleration_x);
                csvString[2] = ctx.getString(R.string.lbl_acceleration_y);
                csvString[3] = ctx.getString(R.string.lbl_acceleration_z);
                
            } else if(_name.getSensorType() == Sensor.TYPE_GRAVITY){
                
                csvString[1] = ctx.getString(R.string.lbl_gravity_x);
                csvString[2] = ctx.getString(R.string.lbl_gravity_y);
                csvString[3] = ctx.getString(R.string.lbl_gravity_z);
                
            }else if(_name.getSensorType() == Sensor.TYPE_GYROSCOPE){
                
                csvString[1] = ctx.getString(R.string.lbl_gyro_x);
                csvString[2] = ctx.getString(R.string.lbl_gyro_y);
                csvString[3] = ctx.getString(R.string.lbl_gyro_z);
                
            }else if(_name.getSensorType() == Sensor.TYPE_LINEAR_ACCELERATION){
                
                csvString[1] = ctx.getString(R.string.lbl_acceleration_x);
                csvString[2] = ctx.getString(R.string.lbl_acceleration_y);
                csvString[3] = ctx.getString(R.string.lbl_acceleration_z);
                
            }else if(_name.getSensorType() == Sensor.TYPE_MAGNETIC_FIELD){
                
                csvString[1] = ctx.getString(R.string.lbl_magnetic_x);
                csvString[2] = ctx.getString(R.string.lbl_magnetic_y);
                csvString[3] = ctx.getString(R.string.lbl_magnetic_z);
                
            }else if(_name.getSensorType() == Sensor.TYPE_ORIENTATION){
                
                csvString[1] = ctx.getString(R.string.lbl_azimuth);
                csvString[2] = ctx.getString(R.string.lbl_pitch);
                csvString[3] = ctx.getString(R.string.lbl_roll);
                
            }else if(_name.getSensorType() == Sensor.TYPE_PRESSURE){
                
                csvString[1] = ctx.getString(R.string.lbl_pressure);
                
            }else if(_name.getSensorType() == Sensor.TYPE_PROXIMITY){
                
                csvString[1] = ctx.getString(R.string.lbl_distance);
                
            }else if(_name.getSensorType() == Sensor.TYPE_ROTATION_VECTOR){
                
                csvString[1] = ctx.getString(R.string.lbl_rotation_vector_x);
                csvString[2] = ctx.getString(R.string.lbl_rotation_vector_y);
                csvString[3] = ctx.getString(R.string.lbl_rotation_vector_z);
                csvString[4] = ctx.getString(R.string.lbl_rotation_vector_cos);
                csvString[5] = ctx.getString(R.string.lbl_rotation_vector_accuracy);
                
            }else if(_name.getSensorType() == Sensor.TYPE_TEMPERATURE){
                
                csvString[1] = ctx.getString(R.string.lbl_rotation_vector_x);
            } else if(_name.getSensorType() == SensorNames.AUDIO.getSensorType()){
            	
                csvString[1] = ctx.getString(R.string.lbl_sound_level);
            }
            _csvWriter.writeNext(csvString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public String getSensorName(){
        
        return _name.name();
    }
    
    @Override
    public CSVWriter getCsvWriter(){
        
        return _csvWriter;
    }
    
    @Override
    public File getDataFile(){
        
        return _dataFile;
    }
    
    @Override
    public void writeToFile(SensorRecord event) {

        String[] csvString = null;
    	if(event._type == SensorNames.AUDIO.getSensorType()){
    		csvString = new String[2];
    		csvString[0] = DateUtils.convertToDate(event._timestamp);
    		csvString[1] = Float.toString(event._values[0]);
    	} else {
    		
            csvString = new String[4];
            csvString[0] = DateUtils.convertToDate(event._timestamp);
            csvString[1] = Float.toString(event._values[0]);
            csvString[2] = Float.toString(event._values[1]);
            csvString[3] = Float.toString(event._values[2]);
    	}
        _csvWriter.writeNext(csvString);
    }

}
