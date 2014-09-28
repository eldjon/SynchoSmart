package tr.edu.metu.thesis.beans.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.resource.spi.IllegalStateException;

import tr.edu.metu.thesis.beans.device.DeviceDataModel;
import tr.edu.metu.thesis.beans.utils.InstanceStatus;
import tr.edu.metu.thesis.eao.EAOManager;
import tr.edu.metu.thesis.events.EventDispatcher;
import tr.edu.metu.thesis.events.ServerEvent;
import tr.edu.metu.thesis.events.ServerEventListener;
import tr.edu.metu.thesis.events.ServletEvent;
import tr.edu.metu.thesis.events.SoapEvent;
import tr.edu.metu.thesis.events.SocketEvent;
import tr.edu.metu.thesis.jpa.Device;
import tr.edu.metu.thesis.jpa.Participant;
import tr.edu.metu.thesis.jpa.Participant.DataStatus;
import tr.edu.metu.thesis.jpa.Participant.DeviceStatus;
import tr.edu.metu.thesis.jpa.SurveyInstance;
import tr.edu.metu.thesis.servlets.NotifyServlet;
import tr.edu.metu.thesis.servlets.ServletKeys;
import tr.edu.metu.thesis.servlets.UploadServlet;
import tr.edu.metu.thesis.socket.ServerMessage;
import tr.edu.metu.thesis.socket.ServerMessage.SocketMessageType;
import tr.edu.metu.thesis.socket.SocketHandler;
import tr.edu.metu.thesis.utils.FacesUtils;
																																																																																																																																																																																																																																				
@ManagedBean(name = "activeSurveyBean")
@ViewScoped
public class ActiveSurveyBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4856556911117667931L;
	public static final int TIME_DELAY         = 0; // 
	
	private SurveyDataModel surveyList;
	private SurveyInstance selectedSurvey;
	private SurveyInstance activeSurvey;
	private Participant selectedParticipant;
	// if the user wants to add a device to an Active Survey
	private Participant addedParticipant;
	// if the user wants to add a selected Device to ActiveSurvey
	private Device selectedDevice;
	private DeviceDataModel freeDeviceList;
	
	private ServerEventListener listener = new ServerEventListener() {
		 
		@Override
		public void onServerEvent(ServerEvent event) {

			String imei       = null;
			SurveyInstance si = null;
			if(selectedSurvey != null)
				si = selectedSurvey;
			else if(activeSurvey != null)
				si = activeSurvey;
			
			if(event instanceof SocketEvent){
				SocketEvent socketE = (SocketEvent) event;
				doOnSocketEvent(socketE);
			} else if(event instanceof SoapEvent){
				synchronized (this) {
					
					SoapEvent soapE = (SoapEvent) event;
					imei            = soapE.getCallingImei();
					doOnSoapEvent(soapE, si, imei);
				}
			} else if(event instanceof ServletEvent){
				
				ServletEvent servletE = (ServletEvent) event;
				doOnServletEvent(servletE, si);
			}
		}
		
		protected void doOnSocketEvent(SocketEvent socketE){
			
			if(socketE.getName().equals(SocketEvent.EVENT_CLIENT_CONNECTED)){
				synchronized (ActiveSurveyBean.this) {
					
					System.out.println(ActiveSurveyBean.this.getClass().getName() 
									   + ": Client connected IMEI = " 
									   + socketE.getDevice().getImei());
					if(socketE.getDevice() != null){
						for(Device d : selectedSurvey.getSelectedDevices())
							if(d.equals(socketE.getDevice()))
								d.setStatus(DeviceStatus.CONNECTED.name());
					}
				}	
			}else if(socketE.getName().equals(SocketEvent.EVENT_CLIENT_DISCONNECTED)){
				
				if(socketE.getDevice() != null){
					for(Device d : selectedSurvey.getSelectedDevices())
						if(d.equals(socketE.getDevice()))
							d.setStatus(DeviceStatus.DISCONNECTED.name());
				}
			}else if(socketE.getName().equals(SocketEvent.EVENT_SOCKET_CLOSED)){
				
			}else if(socketE.getName().equals(SocketEvent.EVENT_SOCKET_OPENED)){
				
			}else if(socketE.getName().equals(SocketEvent.EVENT_MSG_SENT)){
				
			}
		}
		
		protected synchronized void doOnSoapEvent(SoapEvent soapE, 
												  SurveyInstance si, 
												  String imei){
			
			if(soapE.getMethodCalled().equals("notifyAppException")){
				
				System.out.println(" ------------------ Application Crashed --------------------------- ");
				System.out.println(soapE.getCallingImei());
				System.out.println(soapE.getResult());
				System.out.println(" ------------------------------------------------------------------ ");
				
			}
		}
		
		protected void doOnServletEvent(ServletEvent servletE, 
										 SurveyInstance si){
			
			String imei = servletE.getCallingImei();
			if(servletE.getSource() instanceof UploadServlet){
				if(si != null && si.getParticipants() != null){
					for(Participant p : si.getParticipants())
						if(p.getDevice().getImei().equals(imei)){
							
							String status = ((DataStatus)servletE.getResult()).name();
							String extrea = (String)servletE.getExtra(ServletKeys.FILENAME);
							if(extrea != null)
								status = status.concat(" : " + extrea);
							
							p.setDataStatus(status);
						}
					}						
			}else if(servletE.getSource() instanceof NotifyServlet){ 
				
				System.out.println(ActiveSurveyBean.this.getClass().getName() + 
						   ": The CMD " + servletE.getResult().toString() 
						   + " was received by - " + servletE.getCallingImei() );
		
				if(si != null && si.getParticipants() != null){
					for(Participant p : si.getParticipants())
						if(p.getDevice().getImei().equals(imei)){
							
							int msgType = (int)servletE.getResult();
							p.getDevice().addNextCmd(new ServerMessage(SocketMessageType
										 .findSocketMT(msgType)));
						}
				}
				
				for(Device d : SocketHandler.getInstance().getActiveDevices())
					if(d.getImei().equals(imei)){
						
						int msgType = (int)servletE.getResult();
						d.addNextCmd(new ServerMessage(SocketMessageType
									 				  .findSocketMT(msgType)));
					}
			}
			else { 
				System.out.println(ActiveSurveyBean.this.getClass().getName() 
								   + ": We have a null SelectedSurvey!!!");
			}
		}
	};
	
	/**
	 *  While creating this object register its listener to the event dispatcher
	 */
	public ActiveSurveyBean(){
		
		selectedSurvey            = new SurveyInstance();
		addedParticipant          = new Participant();
		selectedParticipant       = new Participant();
		List<SurveyInstance> data = EAOManager.EAOSurveyInstance
				  							  .findByNonStatus(InstanceStatus
				  						      .COMPLETED.name());
		surveyList = new SurveyDataModel(data);
		findActiveSurvey();
		updateDeviceStatus();
	}

	@PostConstruct
	public void onCreate(){
		
		EventDispatcher.getInstance().registerListener(listener);
	}
	
	@PreDestroy
	public void onDestroy(){
		
		EventDispatcher.getInstance().unregisterListener(listener);
	}
	
	// ******************************************************************************* //
	// ************************* METHODS CALLED BY JSF PAGE ************************** //
	// ******************************************************************************* //
	public void connectSurveyInstance(){
		
		if(!isSurveySelected())
			return;
		
		if(SocketHandler.getInstance().isActive()){
			
			FacesUtils.addWarnMessage("SocketHandler is already running! "
				 					+ " A SurveyInstance had it started previously!");
			return;
		}
		connectSocket();
	}
	
	/**
	 * 
	 * This method will send a START command to all connected devices indicating
	 * that in the given time the need to start recording sensor data.
	 * 
	 */
	public void startSurveyInstance(){
		
		if(!isSurveySelected())
			return;

		if(!SocketHandler.getInstance().isActive()){
			
			FacesUtils.addWarnMessage("No device has been connected yet! "
									+ "You cannot start the Survey");
			return;
		}
		
		// give a warning if some devices are not connected
		if(SocketHandler.getInstance().areAllDevicesConnected())
			FacesUtils.addWarnMessage("Not all devices are connected!");

		ServerMessage msg = new ServerMessage(SocketMessageType.START, 
				  							  System.currentTimeMillis() + TIME_DELAY);
		try {
			selectedSurvey.setStatus(InstanceStatus.STARTED.name());
			activeSurvey.setStatus(InstanceStatus.STARTED.name());
			for(SurveyInstance si : surveyList)
				if(si.equals(selectedSurvey))
					si.setStatus(InstanceStatus.STARTED.name());
			EAOManager.update(selectedSurvey);
			SocketHandler.getInstance().sendMessage(msg);
			FacesUtils.addWarnMessage("Devices will soon start collecting sensor data!");
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	public void pauseSurveyInstance(){
		
		if(!isSurveySelected())
			return;
		
		ServerMessage msg = new ServerMessage(SocketMessageType.PAUSE, 
											  System.currentTimeMillis() + TIME_DELAY);
		try {
			SocketHandler.getInstance().sendMessage(msg);
			selectedSurvey.setStatus(InstanceStatus.PAUSED.name());
			activeSurvey.setStatus(InstanceStatus.PAUSED.name());
			for(SurveyInstance si : surveyList)
				if(si.equals(selectedSurvey))
					si.setStatus(InstanceStatus.PAUSED.name());
			EAOManager.update(selectedSurvey);
			FacesUtils.addInfoMessage("The survey has been pause! "
									+ "Devices will be sending data to the server soon !");
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	public void resumeSurveyInstance(){
		
		if(!isSurveySelected())
			return;

		// reset the data status to WAITING after each RESUME click
		for(Participant p : selectedSurvey.getParticipants())
			p.setDataStatus(DataStatus.WAITING.name());
		
		ServerMessage msg = new ServerMessage(SocketMessageType.RESUME, 
											  System.currentTimeMillis() + TIME_DELAY);
		try {
			selectedSurvey.setStatus(InstanceStatus.STARTED.name());
			activeSurvey.setStatus(InstanceStatus.STARTED.name());
			for(SurveyInstance si : surveyList)
				if(si.equals(selectedSurvey))
					si.setStatus(InstanceStatus.STARTED.name());
			EAOManager.update(selectedSurvey);
			SocketHandler.getInstance().sendMessage(msg);
			FacesUtils.addInfoMessage("The survey has been resumed !");
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	public void endSurveyInstance(){
		
		if(!isSurveySelected())
			return;
		
		if(activeSurvey == null)
			return;

		ServerMessage msg = new ServerMessage(SocketMessageType.STOP, 
											  System.currentTimeMillis() + TIME_DELAY);
		try {
			SocketHandler.getInstance().sendMessage(msg);
			selectedSurvey.setStatus(InstanceStatus.COMPLETED.name());
			activeSurvey.setStatus(InstanceStatus.COMPLETED.name());
			EAOManager.update(selectedSurvey);
//			List<SurveyInstance> data = EAOManager.EAOSurveyInstance
//					  							  .findByNonStatus(InstanceStatus.COMPLETED.name());
//			surveyList     = new SurveyDataModel(data);
//			activeSurvey   = new SurveyInstance();
//			selectedSurvey = new SurveyInstance();
//			selectedDevice = new Device();
//			selectedParticipant = new Participant();
			FacesUtils.addInfoMessage("The survey has been completed!"
									+ "Devices will start delivering data soon ! ");
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	public void addSelectedDevice(){
		
		if(selectedDevice == null || selectedDevice.getDeviceId() <= 0){
			FacesUtils.addWarnMessage("Please select a device first!");
			return;
		}
		
		if(selectedSurvey == null 
				|| selectedSurvey.getSurveyInstanceId() <= 0){
			FacesUtils.addWarnMessage("Please select a survey from the list!");
			return;
		}
		
		if(!selectedSurvey.getStatus().equals(InstanceStatus.CREATED.name())){
			
			FacesUtils.addWarnMessage("You cannot edit an ongoing survey!");
			return;
		}
		
		Participant p = new Participant();
		p.setDevice(selectedDevice);
		p.setSurveyInstance(selectedSurvey);
		EAOManager.persist(p);
		EAOManager.refresh(selectedSurvey);
		selectedSurvey.addParticipant(p);
		findActiveSurvey();
		selectedDevice = new Device();
		FacesUtils.addInfoMessage("New device was successfully added to the survey!");
		return;
	}
	
	// add a device to the active survey.
	public void addParticipant(){
		
		if(addedParticipant.getDevice() == null 
				|| addedParticipant.getDevice().getImei() == null
				|| addedParticipant.getDevice().getImei().length() <= 0){
			
			FacesUtils.addWarnMessage("Please provide an valid IMEI! "
					+ " The participant was not added to the selected survey instance!");
			return;
		}
		
		List<Device> result = EAOManager.findByField(Device.class, 
													 "imei", 
													 addedParticipant.getDevice().getImei());
		if(result.size() > 0){
			FacesUtils.addWarnMessage("The device already exists in the database!");
			return;
		}
		
		if(!selectedSurvey.getStatus().equals(InstanceStatus.CREATED.name())){
			
			FacesUtils.addWarnMessage("You cannot edit an ongoing survey!");
			return;
		}
		
		addedParticipant.setSurveyInstance(selectedSurvey);
		EAOManager.persist(addedParticipant.getDevice());
		EAOManager.persist(addedParticipant);
		EAOManager.refresh(selectedSurvey);
		selectedSurvey.getParticipants().add(addedParticipant);
		addedParticipant = new Participant();
		FacesUtils.addInfoMessage("Participant was successfully added to the SurveyInstance!");
	}
	
	public void updateParticipant(){
		
		if(selectedParticipant == null || selectedParticipant.getParticipantId() <= 0){
			FacesUtils.addWarnMessage("Please select the participant you wish to update!");
			return;
		}
		
		if(!selectedSurvey.getStatus().equals(InstanceStatus.CREATED.name())){
			
			FacesUtils.addWarnMessage("You cannot edit an ongoing survey!");
			return;
		}
		
		EAOManager.update(selectedParticipant.getDevice());
		EAOManager.update(selectedParticipant);
		FacesUtils.addInfoMessage("The Device´s information was successfully updated!");
	}
	
	public void removeParticipant(){
		
		if(!selectedSurvey.getStatus().equals(InstanceStatus.CREATED.name())){
			
			FacesUtils.addWarnMessage("You cannot edit an ongoing survey!");
			return;
		}
		
		if(selectedParticipant == null || selectedParticipant.getParticipantId() <= 0){
			FacesUtils.addWarnMessage("Please select the participant you wish to remove!");
			return;
		}
		try {
			SocketHandler.getInstance().removeDevice(selectedParticipant.getDevice());
		} catch (IllegalStateException e) {
		}
		
		EAOManager.remove(selectedParticipant);
		selectedSurvey.getParticipants().remove(selectedParticipant);
		EAOManager.update(selectedSurvey);
		selectedParticipant = new Participant();
		FacesUtils.addInfoMessage("Device was successfully removed from the selected Survey!");
	}
	
	public void loadFreeDeviceList(){
		
		List<Device> data          = EAOManager.findAll(Device.class);
		List<Device> result        = new ArrayList<Device>();
		List<Device> surveyDevices = selectedSurvey.getSelectedDevices();
		Iterator<Device> it        = data.iterator();
		
		if(surveyDevices.size() <= 0){
			freeDeviceList = new DeviceDataModel(data);
		} else {
			
			while(it.hasNext()){
				Device d = it.next();
				for(int i = 0; i < surveyDevices.size(); i ++){
					
					if(d.equals(surveyDevices.get(i)))
						break;
					else if(i == surveyDevices.size() - 1)
						result.add(d);
				}
			}
			freeDeviceList = new DeviceDataModel(result);
		}
		updateDeviceStatus();
	}

	// ******************************************************************************* //
	// *************************** PROTECTED METHODS ********************************* //
	// ******************************************************************************* //
	protected void findActiveSurvey(){
		
		List<SurveyInstance> actives = EAOManager.EAOSurveyInstance.findActiveSurvey();
		if(actives != null){
			if(actives.size() == 1){
				
				activeSurvey   = actives.get(0);
				selectedSurvey = activeSurvey;
			}
			else if(actives.size() > 1){
				
				activeSurvey = actives.get(0);
				System.out.println("THERE IS MORE THAN ONE ACTIVE SURVEY!! "
								 + "NOT ACCEPTABLE!!");
			}
		}
	}
	
	protected boolean isSurveySelected(){
		
		if(selectedSurvey == null || selectedSurvey.getSurveyInstanceId() <= 0){
			
			FacesUtils.addWarnMessage("Select a survey from the survey list first!");
			return false;
		}
		return true;
	}
	
	protected void connectSocket(){
		
		List<Participant> participants = EAOManager.findByField(Participant.class, 
															    "surveyInstance.surveyInstanceId", 
															    selectedSurvey.getSurveyInstanceId());
		List<Device> devices = new ArrayList<>();
		for(Participant p : participants)
			devices.add(p.getDevice());
		
		try {
			SocketHandler.getInstance().open(devices);
			selectedSurvey.setStatus(InstanceStatus.CONNECTED.name());
			for(SurveyInstance si : surveyList)
				if(si.equals(selectedSurvey))
					si.setStatus(InstanceStatus.CONNECTED.name());
			EAOManager.update(selectedSurvey);
			findActiveSurvey();
			FacesUtils.addInfoMessage("Socket is opened and waiting for connections!");
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	public void updateDeviceStatus(){
		
		// if the SelectedSurvey is equal to the ActiveSurvey we will update the list of 
		// devices of the SelectedSurvey in order to see the active device with a different 
		// icon.
		if(selectedSurvey.equals(activeSurvey) && SocketHandler.getInstance().isActive()){
			for(Device surveyD : selectedSurvey.getSelectedDevices())
				for(Device d : SocketHandler.getInstance().getActiveDevices())
					if(d.equals(surveyD)){
						
						surveyD.setStatus(d.getStatus());
						surveyD.setLastReceivedCmd(d.getLastReceivedCmd());
						break;
					}
		}
	}

	// ******************************************************************************* //
	// ************************* GETTERS AND SETTERS ********************************* //
	// ******************************************************************************* //
	public SurveyDataModel getSurveyList() {
		return surveyList;
	}

	public void setSurveyList(SurveyDataModel surveyList) {
		this.surveyList = surveyList;
	}

	public SurveyInstance getSelectedSurvey() {
		return selectedSurvey;
	}

	public void setSelectedSurvey(SurveyInstance selectedSurvey) {
		this.selectedSurvey = selectedSurvey;
	}

	public SurveyInstance getActiveSurvey() {
		return activeSurvey;
	}

	public void setActiveSurvey(SurveyInstance activeSurvey) {
		this.activeSurvey = activeSurvey;
	}

	public Participant getSelectedParticipant() {
		return selectedParticipant;
	}

	public void setSelectedParticipant(Participant selectedParticipant) {
		this.selectedParticipant = selectedParticipant;
	}

	public Device getSelectedDevice() {
		return selectedDevice;
	}

	public void setSelectedDevice(Device selectedDevice) {
		this.selectedDevice = selectedDevice;
	}

	public DeviceDataModel getFreeDeviceList() {
		return freeDeviceList;
	}

	public void setFreeDeviceList(DeviceDataModel freeDeviceList) {
		this.freeDeviceList = freeDeviceList;
	}

	public Participant getAddedParticipant() {
		return addedParticipant;
	}

	public void setAddedParticipant(Participant addedParticipant) {
		this.addedParticipant = addedParticipant;
	}
}
