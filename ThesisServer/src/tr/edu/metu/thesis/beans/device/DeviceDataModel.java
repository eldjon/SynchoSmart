package tr.edu.metu.thesis.beans.device;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import tr.edu.metu.thesis.jpa.Device;

@SuppressWarnings("unchecked")
public class DeviceDataModel extends ListDataModel<Device> 
							    implements SelectableDataModel<Device>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7239065201021348804L;
	private List<Device> deviceList;

	public DeviceDataModel(){
		
	}
	
	public DeviceDataModel(List<Device> data){
		
		super(data);
		deviceList = data;
	}
	
	@Override
	public Device getRowData(String id) {
		
		List<Device> data = (List<Device>) getWrappedData();
		for(Device d : data)
			if(((int)d.getDeviceId()) == Integer.parseInt(id))
				return d;
		return null;
	}

	@Override
	public Object getRowKey(Device dev) {
		
		return dev.getDeviceId();
	}
	
	public List<Device> getDeviceList(){
		
		return deviceList;
	}
	
}
