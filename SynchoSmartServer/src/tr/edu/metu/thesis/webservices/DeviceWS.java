package tr.edu.metu.thesis.webservices;

import java.util.List;

import tr.edu.metu.thesis.eao.EAOManager;
import tr.edu.metu.thesis.events.EventDispatcher;
import tr.edu.metu.thesis.events.SoapEvent;
import tr.edu.metu.thesis.gson.GsonParser;
import tr.edu.metu.thesis.gson.GsonParticipantAnswer;
import tr.edu.metu.thesis.gson.GsonParticipantAnswerList;
import tr.edu.metu.thesis.gson.QuestionnaireAdapter.GsonQuestionList;
import tr.edu.metu.thesis.jpa.Device;
import tr.edu.metu.thesis.jpa.Participant;
import tr.edu.metu.thesis.jpa.Participant.DeviceStatus;
import tr.edu.metu.thesis.jpa.ParticipantAnswer;
import tr.edu.metu.thesis.jpa.SurveyInstance;
import tr.edu.metu.thesis.socket.SocketHandler;


public class DeviceWS {


	public DeviceWS(){
		
	}
	
	/**
	 * Find the active survey for the given device 
	 * @param imei of the device 
	 * @return the ID of the active survey of -1 if not found
	 */
	public int getSurveyId(String imei){
		
		int result = 0;
		List<SurveyInstance> surveys = EAOManager.EAOSurveyInstance.findActiveSurvey();
		if(surveys.size() <= 0)
			result = -1;
		else 
			result = surveys.get(0).getSurveyInstanceId();
		
		SoapEvent event = new SoapEvent(this, 
										imei, 
										"getSurveyId");
		event.setResult(result);
		EventDispatcher.getInstance().fireEvent(event);
		return result;
	}
	
	public boolean notifyFileUploadFailure(String imei, String file){
		
		Boolean result = false;
		List<Device> devs = EAOManager.findByField(Device.class, 
												   "imei", 
												   imei);
		SoapEvent event = new SoapEvent(this, 
										imei, 
										"notifyFileUploadFailure");
		if(devs.size() <= 0)
			result = false;
		else
			result = true;
		
		event.setResult(result);
		EventDispatcher.getInstance().fireEvent(event);
		return result;
	}
	
	public boolean disconnectDevice(String imei){
		
		System.out.println("SoapWS.disconnectDevice()");
		boolean result = false;
		Device dev = (Device) EAOManager.findByField(Device.class,
													 "imei", 
													 imei);
		SoapEvent event = new SoapEvent(this, 
										imei, 
										"disconnectDevice");
		if(dev != null){
			
			result = true;
			dev.setStatus(DeviceStatus.DISCONNECTED.name());
			EAOManager.update(dev);
			SocketHandler.getInstance().stopDevice(dev);
		}
		event.setResult(result);
		EventDispatcher.getInstance().fireEvent(event);
		System.out.println("SoapWS.disconnectDevice()");
		return result;
	}
	
	public boolean provideSeatNumber(String imei, String seatNumber){
		
		synchronized (EAOManager.class) {
			
			List<SurveyInstance> activeSurvey = EAOManager.EAOSurveyInstance
					  .findActiveSurvey();
			SoapEvent event = new SoapEvent(this, 
					imei, 
					"provideSeatNumber");
			
			if(activeSurvey.size() > 0){
				SurveyInstance instance = activeSurvey.get(0);
				if(!instance.isSeatNumberUnique(seatNumber, imei)){
					
					event.setResult(false);
					EventDispatcher.getInstance().fireEvent(event);
					return false;
				}
				
				for(Participant p : instance.getParticipants())
					if(p.getDevice().getImei().equals(imei)){
					
						event.setResult(true);
						EventDispatcher.getInstance().fireEvent(event);
						p.setSeatNumber(seatNumber);
						EAOManager.update(p);
						return true;
					}
			}
			event.setResult(false);
			EventDispatcher.getInstance().fireEvent(event);
			return false;
		}
	}
	
	public boolean submitQuestionnaire(String imei, 
									   int surveyInstanceId, 
									   String questionnaire){
		
		GsonParticipantAnswerList result = GsonParser.GSON.fromJson(questionnaire, 
										   GsonParticipantAnswerList.class);
		// submit the answers to the database
		SurveyInstance survey = EAOManager.findById(surveyInstanceId, 
													SurveyInstance.class);
		if(survey == null)
			return false;
		
		for(GsonParticipantAnswer pa : result){
			ParticipantAnswer pAnswer = new ParticipantAnswer();
			pAnswer.setParticipant(survey.findParticipant(imei));
			pAnswer.setSelectedOption(pa.getSelectedOption());
			pAnswer.setValue(pa.getValue());
			pAnswer.setSurveyInstanceQuestion(survey.findSiq(surveyInstanceId, 
															 pa.getQuestionId()));
			EAOManager.persist(pAnswer);
		}
		return true;
	}
	
	public String getQuestionnaire(String imei){
																																																																																																																																																																																																																																																																																																																										
		List<Device> devices = EAOManager.findByField(Device.class,
													  "imei", 
													  imei);
		SoapEvent event = new SoapEvent(this, 
										imei, 
										"getQuestionnaire");
		event.setResult("");
		if(devices.size() > 0){
			
			Device d = devices.get(0);
			List<SurveyInstance> surveys = EAOManager.EAOSurveyInstance.findActiveSurvey();
			if(surveys.size() > 0){
				SurveyInstance s = surveys.get(0);
				if(s.getSelectedDevices().contains(d)){
					
					if(s.getSurveyTemplate().getQuestions() != null 
							&& s.getSurveyTemplate().getQuestions().size() > 0){
						try {
							
							String json = GsonParser.GSON.toJson(new GsonQuestionList(s
														 .getSurveyTemplate()
														 .getQuestions()));
							event.setResult(json);
							return json;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		EventDispatcher.getInstance().fireEvent(event);
		return "";
	}
	
	/**
	 * This web method will be called by mobile clients when they want to connect to a 
	 * socket of the server. The server checks if the device (imei) is eligible to connect
	 * (acceptable for the current survey), creates a socket and sends it number to the client.
	 * @param imei The device identificator.
	 * @return -1 if the socket was not created, and socket number otherwise.
	 */
	public int getSocket(String imei, String ipAddress){
		
		synchronized (this) {
			
			List<Device> devices = EAOManager.findByField(Device.class, "imei", imei);
			SoapEvent event = new SoapEvent(this, 
											imei, 
											"getSocket");
			if(devices.size() > 0 && SocketHandler.getInstance().isActive()){
				
				Device d = devices.get(0);
				List<SurveyInstance> surveys = EAOManager.EAOSurveyInstance.findActiveSurvey();
				if(surveys.size() > 0){
					SurveyInstance s = surveys.get(0);
					for(Participant p : s.getParticipants()){
						// if the given device is part of the Active Survey then 
						// update its IP address and send the port number to its client
						if(p.getDevice().equals(d)){

							d.setCurrentIpAddress(ipAddress);
							EAOManager.update(d);
							event.setResult(SocketHandler.getInstance().getPort());
							EventDispatcher.getInstance().fireEvent(event);
							return SocketHandler.getInstance().getPort();
						}
					}
				}
			}
			event.setResult(-1);
			EventDispatcher.getInstance().fireEvent(event);
		}
		return -1;
	}
	
	public boolean notifyAppException(String imei, String exception){
		SoapEvent event = new SoapEvent(this, 
										imei, 
										"notifyAppException");
		event.setResult(exception);
		EventDispatcher.getInstance().fireEvent(event);
		return true;
	}

	/**
	 * This web service method will be called by mobile clients everytime a 
	 * start/stop/pause/resume command is received. We need to make sure that
	 * all clients receive the given commands and if not we need to resend 
	 * them again or reconnect the client if necessary.
	 * 
	 * @param cmd Which was received by client
	 * @param imei The identificator of the device
	 * @return 
	 */
	public boolean receivedCmd(String imei, int cmd){
		
		synchronized (this) {
			
			SoapEvent se = new SoapEvent(this, 
					 imei, 
					 "receivedCmd");
			se.setResult(cmd);
			EventDispatcher.getInstance().fireEvent(se);
			System.out.println(" ---------------- SoapWS.receivedCmd() -------------- " + imei);
			return true;
		}
	}
}
