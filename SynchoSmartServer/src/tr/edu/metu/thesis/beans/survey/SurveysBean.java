package tr.edu.metu.thesis.beans.survey;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import tr.edu.metu.thesis.eao.EAOManager;
import tr.edu.metu.thesis.jpa.Participant;
import tr.edu.metu.thesis.jpa.SurveyInstance;

@ManagedBean(name = "surveysBean")
@ViewScoped
public class SurveysBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6911188263950441756L;

	private SurveyDataModel surveyList;
	private SurveyInstance  selectedSurvey;
	private Participant     selectedParticipant;
	
	public SurveysBean(){
		
		loadSurveys();
	}

	// ******************************************************************************* //
	// ********************************** PROTECTED METHOD *************************** //
	// ******************************************************************************* //
	protected void loadSurveys(){
		
		List<SurveyInstance> data = EAOManager.findAll(SurveyInstance.class);
		surveyList                = new SurveyDataModel(data);
	}
	
	// ******************************************************************************* //
	// ********************************** GETTERS AND SETTERS ************************ //
	// ******************************************************************************* //
	public SurveyInstance getSelectedSurvey() {
		return selectedSurvey;
	}

	public void setSelectedSurvey(SurveyInstance selectedSurvey) {
		this.selectedSurvey = selectedSurvey;
	}

	public SurveyDataModel getSurveyList() {
		return surveyList;
	}

	public void setSurveyList(SurveyDataModel surveyList) {
		this.surveyList = surveyList;
	}

	public Participant getSelectedParticipant() {
		return selectedParticipant;
	}

	public void setSelectedParticipant(Participant selectedParticipant) {
		this.selectedParticipant = selectedParticipant;
	}
}
