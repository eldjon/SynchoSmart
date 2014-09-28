package tr.edu.metu.ii.aaa.sensors;

import tr.edu.metu.ii.aaa.services.DataCollectorService.SensorRecord;

public interface AnalysisSensorListener {

	public void onSensorRecord(SensorRecord record);
	
}
