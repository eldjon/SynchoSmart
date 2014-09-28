package tr.edu.metu.thesis.settings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

@SuppressWarnings("unused")
public class Config implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID  = 8167510391355012033L;
	private static final String BASE_URL        = "http://localhost:8080/ThesisServer/";
	private static final String DATA_FILE_PATH  = "data_file_path";
	private static final String FILE_SEPARATOR  = "file_separator";

	private static Config _instance;

	private String _deviceUrl     = null;
	private String _dataFilePath  = null;
	private String _fileSeparator = null;
	
	private Config(){
		
		init();
	}
	
	public static Config getInstance(){
		
		if(_instance == null){
			_instance = new Config();
		}
		return _instance;
	}
	
	public void init(){
		
		
		// load the dataFilePath from server.properties file.
		// IF there is no such property defined in this file then
		// set it by default based on operating system ()
		_deviceUrl = BASE_URL.concat("device");
        ClassLoader cl   = Config.this.getClass().getClassLoader();
        InputStream is   = cl.getResourceAsStream("META-INF/server.properties");
        Properties props = new Properties();
        
        if(is != null){
        	
            try {
				props.load(is);
	            _dataFilePath = props.getProperty(DATA_FILE_PATH, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        if(_dataFilePath == null){
        	
            String opeSystem = System.getProperty("os.name");
            if(opeSystem.toLowerCase().contains("windows")){
            	
            	_fileSeparator = "\\";
            	_dataFilePath = "C:\\sensorDataCollector\\";
            }
            else {
            	
            	_fileSeparator = "/";
            	_dataFilePath  = "/home/eldi/Desktop/analysis_data/";
            }
        }
        File dataFile = new File(_dataFilePath);
        if(!dataFile.exists())
        	dataFile.mkdirs();
        
        System.out.println(getClass().getName() + ": File Separator from properties file : " + _fileSeparator);
        System.out.println(getClass().getName() + ": Data File Path from properties file : " + _dataFilePath);
    }

	public String getDataFilePath(){
		
		return _dataFilePath;
	}
	
	public String getFileSeparator(){
		
		return _fileSeparator;
	}
}
