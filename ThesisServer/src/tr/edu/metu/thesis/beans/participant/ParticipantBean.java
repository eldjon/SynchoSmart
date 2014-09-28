package tr.edu.metu.thesis.beans.participant;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import tr.edu.metu.thesis.beans.survey.SurveyDataModel;
import tr.edu.metu.thesis.jpa.Participant;
import tr.edu.metu.thesis.jpa.SurveyInstance;

@ManagedBean
@ViewScoped
public class ParticipantBean implements Serializable{

	private static final long serialVersionUID = -979296620483755761L;
	
	private ParticipantDataModel participants;
	private Participant          selectedParticipant;
	private Participant          newParticipant;
	private SurveyDataModel      surveys;
	private SurveyInstance       selectedSurvey;

	public ParticipantBean(){
		
	}
	
	public void add(){
		
	}
	
	public void update(){
		
	}
	
	public void delete(){
		
	}

	// ******************************************************************************* //
	// ******************************* GETTERS AND SETTER **************************** //
	// ******************************************************************************* //
	public ParticipantDataModel getParticipants() {
		return participants;
	}

	public void setParticipants(ParticipantDataModel participants) {
		this.participants = participants;
	}

	public Participant getSelectedParticipant() {
		return selectedParticipant;
	}

	public void setSelectedParticipant(Participant selectedParticipant) {
		this.selectedParticipant = selectedParticipant;
	}

	public Participant getNewParticipant() {
		return newParticipant;
	}

	public void setNewParticipant(Participant newParticipant) {
		this.newParticipant = newParticipant;
	}

	public SurveyDataModel getSurveys() {
		return surveys;
	}

	public void setSurveys(SurveyDataModel surveys) {
		this.surveys = surveys;
	}

	public SurveyInstance getSelectedSurvey() {
		return selectedSurvey;
	}

	public void setSelectedSurvey(SurveyInstance selectedSurvey) {
		this.selectedSurvey = selectedSurvey;
	}
	
}
