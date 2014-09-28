																																																																																																																																																																																																																																																														package tr.edu.metu.thesis.beans.device;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import tr.edu.metu.thesis.eao.EAOManager;
import tr.edu.metu.thesis.jpa.Device;
import tr.edu.metu.thesis.utils.FacesUtils;


@SuppressWarnings("unchecked")
@ManagedBean(name = "deviceBean")
@ViewScoped
public class DeviceBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7265443964175307146L;

	private DeviceDataModel deviceList = null;
	private Device          newDevice  = null;
	private Device          selected   = null;
	
	public DeviceBean(){
		
		newDevice  = new Device();
		selected   = new Device();
	}
	
	@PostConstruct
	public void init(){
		// load the list of devices from the database
		deviceList = new DeviceDataModel(EAOManager.findAll(Device.class));
	}
	
	// ******************************************************************************* //
	// ************************* METHODS CALLED BY JSF PAGE ************************** //
	// ******************************************************************************* //
	public void add(){
		
		if(newDevice == null || newDevice.getImei().equals("")){
			
			FacesUtils.addWarnMessage("Please provide a valid IMEI for the device");
			return;
		}
		
		List<Device> result = EAOManager.findByField(Device.class, "imei", newDevice.getImei());
		if(result.size() > 0){
			
			FacesUtils.addWarnMessage("The device already exists in the database!");
			return;
		}
			
		boolean persisted = EAOManager.persist(newDevice);
		if(persisted){
			
			FacesUtils.addInfoMessage("The new device was successfuly "
					+ "submitted to the database! ");
			deviceList.getDeviceList().add(newDevice);
			newDevice = new Device();
		}
		else
			FacesUtils.addErrorMessage("A database error occured! "
									 + "Device couldnt be added!");
	}
	
	public void delete(){
		
		boolean removed = EAOManager.remove(selected);
		if(removed){
			
			((List<Device>)deviceList.getWrappedData()).remove(selected);
			selected.clear();
			FacesUtils.addInfoMessage("The selected device was successfuly "
					+ "removed from the database! ");
		}
		else
			FacesUtils.addInfoMessage("An error occured! "
									+ "Check if the device is selected!");
	}
	
	public void update(){
		
		List<Device> result = EAOManager.findByField(Device.class, "imei", selected.getImei());
		if(result.size() > 0){
			if(result.get(0).getDeviceId() != selected.getDeviceId()){
				
				FacesUtils.addWarnMessage("The device already exists in the database!");
				deviceList = new DeviceDataModel(EAOManager.findAll(Device.class));
				selected = deviceList.getRowData(Integer.toString(selected.getDeviceId()));
				return;
			}
		}
		EAOManager.update(selected);
		FacesUtils.addInfoMessage("The selected device was successfuly "
								+ "updated! ");
	}
	
	// ******************************************************************************* //
	// ***************************** GETTERS AND SETTERS ***************************** //
	// ******************************************************************************* //
	public Device getNewDevice() {

		return newDevice;
	}

	public void setNewDevice(Device newDevice) {
		
		this.newDevice = newDevice;
	}

	public Device getSelected() {
		
		return selected;
	}

	public void setSelected(Device selected) {
		
		this.selected = selected;
	}
	
	public DeviceDataModel getDeviceList() {
		
		return deviceList;
	}

	public void setDeviceList(DeviceDataModel deviceList) {
		
		this.deviceList = deviceList;
	}
}
