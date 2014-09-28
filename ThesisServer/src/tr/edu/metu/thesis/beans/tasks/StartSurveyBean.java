package tr.edu.metu.thesis.beans.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import tr.edu.metu.thesis.beans.device.DeviceDataModel;
import tr.edu.metu.thesis.beans.templates.SurveyTemplateDataModel;
import tr.edu.metu.thesis.beans.utils.InstanceStatus;
import tr.edu.metu.thesis.eao.EAOManager;
import tr.edu.metu.thesis.jpa.Device;
import tr.edu.metu.thesis.jpa.Participant;
import tr.edu.metu.thesis.jpa.Question;
import tr.edu.metu.thesis.jpa.SurveyInstance;
import tr.edu.metu.thesis.jpa.SurveyInstanceQuestion;
import tr.edu.metu.thesis.jpa.SurveyTemplate;
import tr.edu.metu.thesis.utils.FacesUtils;

@ManagedBean(name = "startSurveyBean")
@ViewScoped
public class StartSurveyBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3371943329273973159L;

	private SurveyTemplateDataModel templateList;
	private DeviceDataModel         deviceList;
	private SurveyTemplate          selected;
	private Device[]                selectedDevices;
	
	private SurveyInstance newInstance;
	
	public StartSurveyBean(){
		
		List<SurveyTemplate> data = EAOManager.findAll(SurveyTemplate.class);
		List<Device> devices      = EAOManager.findAll(Device.class);
		templateList              = new SurveyTemplateDataModel(data);
		deviceList                = new DeviceDataModel(devices);
		selected                  = new SurveyTemplate();
		newInstance               = new SurveyInstance();
	}
	
	// ******************************************************************************* //
	// *************************** METHODS CALLED BY JSF PAGE ************************ //
	// ******************************************************************************* //
	public void createInstance(){
		
		if(newInstance.getName() == null 
				|| newInstance.getName().length() <= 0){
			
			FacesUtils.addWarnMessage("Please provide a name for the new survey instance! "
	 				 				 + "Survey instance was not created!");
			return;
		}
		
		if(selectedDevices.length <= 0){
			
			FacesUtils.addWarnMessage("A survey instance should include at least one device! "
	 				 + "Survey instance was not created!");
			return;
		}
		// based on selected Devices create participants 
		// create questions for instance and then persist to the database
		List<SurveyInstanceQuestion> instanceQuestions = new ArrayList<>();
		List<Participant> instanceParticipants         = new ArrayList<>();
		
		for(Device d : selectedDevices){
			Participant participant = new Participant();
			participant.setDevice(d);
			participant.setSurveyInstance(newInstance);
			instanceParticipants.add(participant);
		}
		for(Question q : selected.getQuestions()){
			
			SurveyInstanceQuestion question = new SurveyInstanceQuestion();
			question.setQuestion(q);
			question.setSurveyInstance(newInstance);
			instanceQuestions.add(question);
		}
		newInstance.setParticipants(instanceParticipants);
		newInstance.setSurveyInstanceQuestions(instanceQuestions);
		newInstance.setStatus(InstanceStatus.CREATED.name());
		newInstance.setSurveyTemplate(selected);
		EAOManager.persist(newInstance);
		newInstance = new SurveyInstance();
		FacesUtils.addInfoMessage("The survey was successfully submitted!");
	}
	
	// ******************************************************************************* //
	// ********************************* GETTERS AND SETTER ************************** //
	// ******************************************************************************* //
	public SurveyTemplateDataModel getTemplateList() {
		return templateList;
	}

	public void setTemplateList(SurveyTemplateDataModel templateList) {
		this.templateList = templateList;
	}

	public DeviceDataModel getDeviceList() {
		return deviceList;
	}

	public void setDeviceList(DeviceDataModel deviceList) {
		this.deviceList = deviceList;
	}

	public SurveyTemplate getSelected() {
		return selected;
	}

	public void setSelected(SurveyTemplate selected) {
		this.selected = selected;
	}

	public Device[] getSelectedDevices() {
		return selectedDevices;
	}

	public void setSelectedDevices(Device[] selectedDevices) {
		this.selectedDevices = selectedDevices;
	}

	public SurveyInstance getNewInstance() {
		return newInstance;
	}

	public void setNewInstance(SurveyInstance newInstance) {
		this.newInstance = newInstance;
	}
}
