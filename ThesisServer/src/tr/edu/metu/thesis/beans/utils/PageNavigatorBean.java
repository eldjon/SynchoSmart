package tr.edu.metu.thesis.beans.utils;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;

import tr.edu.metu.thesis.utils.FacesUtils;

@ManagedBean(name = "pageNavigatorBean")
@SessionScoped
public class PageNavigatorBean {

	public static final String BASE_URL = "http://localhost:8080/ThesisServer/";
	
	public void goToDeviceManagePage(ActionEvent event){
		
		FacesUtils.redirect(BASE_URL.concat("device/manage.jsf"));
	}
	
	public void goToSurveyTemplatePage(ActionEvent event){
		
		FacesUtils.redirect(BASE_URL.concat("tasks/surveyTemplates.jsf"));
	}
	
	public void goToStartSurveyPage(){
		
		FacesUtils.redirect(BASE_URL.concat("tasks/newSurvey.jsf"));
	}
	
	public void goToActiveSurveyPage(){
		
		FacesUtils.redirect(BASE_URL.concat("tasks/activeSurvey.jsf"));
	}
	
	public void goToSurveysPage(ActionEvent event){
		
		FacesUtils.redirect(BASE_URL.concat("tasks/surveys.jsf"));
	}
	
	public void goToHomePage(ActionEvent event){
		
		FacesUtils.redirect(BASE_URL);
	}
	
}
