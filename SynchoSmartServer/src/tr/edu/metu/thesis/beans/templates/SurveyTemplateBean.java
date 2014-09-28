package tr.edu.metu.thesis.beans.templates;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import tr.edu.metu.thesis.eao.EAOManager;
import tr.edu.metu.thesis.jpa.Question;
import tr.edu.metu.thesis.jpa.QuestionOption;
import tr.edu.metu.thesis.jpa.SurveyTemplate;
import tr.edu.metu.thesis.utils.FacesUtils;

@ManagedBean(name = "surveyTemplateBean")
@ViewScoped
public class SurveyTemplateBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2937602067707581815L;

	// fields used for creating new survey template
	private SurveyTemplate  newTemplate;
	private Question        currentQuestion;
	private QuestionOption  currentOption;
	private Question        selectedQuestion;
	
	private SurveyTemplateDataModel templateList;
	private SurveyTemplate  selected;
	
	public SurveyTemplateBean(){
		
		newTemplate               = new SurveyTemplate();
		List<SurveyTemplate> data = EAOManager.findAll(SurveyTemplate.class);
		templateList              = new SurveyTemplateDataModel(data);
		currentQuestion           = new Question();
		selected                  = new SurveyTemplate();
		currentOption             = new QuestionOption();
		selectedQuestion          = new Question();
	}

	// ******************************************************************************* //
	// ************************* METHODS CALLED BY JSF PAGE ************************** //
	// ******************************************************************************* //
	public void add(){
		
		if(newTemplate.getName() == null || newTemplate.getName().length() <= 0){
			
			FacesUtils.addWarnMessage("Please provide a name for the given template! "
					 				 + "Template was not submitted!");
			return;
		}
		
		newTemplate.setCreationDate(new Date());
		boolean commited = EAOManager.persist(newTemplate);
		if(commited){
			
			List<SurveyTemplate> data = EAOManager.findAll(SurveyTemplate.class);
			templateList.setWrappedData(data);
			newTemplate      = new SurveyTemplate();
			currentQuestion  = new Question();
			selected         = new SurveyTemplate();
			currentOption    = new QuestionOption();
			selectedQuestion = new Question();
			FacesUtils.addInfoMessage("The new template was successfuly "
					+ "submitted to the database! ");
		}
		else 
			FacesUtils.addInfoMessage("An error occured! "
									+ "Check if the template already exists!");
	}
	
	public void delete(){
		
		if(selected == null || selected.getSurveyTemplateId() <= 0){
			
			FacesUtils.addWarnMessage("Please selected a survey template to delete!");
			return;
		}
	
		if(selected.getSurveyInstances() != null 
				&& selected.getSurveyInstances().size() > 0){
			
			FacesUtils.addWarnMessage("The selected survey template is related to survey instances! "
									+ " Remove its instances first!");
			return;
		}
		EAOManager.remove(selected);
		List<SurveyTemplate> data = EAOManager.findAll(SurveyTemplate.class);
		templateList              = new SurveyTemplateDataModel(data);
		selected = new SurveyTemplate();
		FacesUtils.addInfoMessage("Selected survey template was successfully removed!");
	}
	
	public void update(){
		
		if(selected == null || selected.getSurveyTemplateId() <= 0){
			
			FacesUtils.addWarnMessage("Please selected a survey template to update!");
			return;
		}
		EAOManager.update(selected);
		List<SurveyTemplate> data = EAOManager.findAll(SurveyTemplate.class);
		templateList              = new SurveyTemplateDataModel(data);
		FacesUtils.addInfoMessage("Selected survey template was successfully updated!");
	}
	
	public void removeQuestion(Question question){
		
		for(int i = 0; i < newTemplate.getQuestions().size(); i++)
			if(newTemplate.getQuestions().get(i).getValue().equals(question.getValue())){
				
				newTemplate.getQuestions().remove(i);
				return;
			}
	}
	
	public void addQuestionToTemplate(){
		
		Question question = new Question(currentQuestion);
		newTemplate.addQuestion(question);
		currentQuestion.clear();
	}
	
	public void manageOptions(Question question){
		
		selectedQuestion = question;
		currentOption.setSection("");
		currentOption.setValue("");
		currentOption.setQuestion(selectedQuestion);
	}
	
	public void addOptionToQuestion(){
		
		selectedQuestion.getQuestionOptions().add(new QuestionOption(currentOption));
	}
	
	public void removeOptionFromQuestion(QuestionOption option){
		
		System.out.println("SurveyTemplateBean.removeOptionFromQuestion(): " + option.getValue()); 
		for(int i = 0; i < selectedQuestion.getQuestionOptions().size(); i++){
			if(selectedQuestion.getQuestionOptions().get(i).getValue().equals(option.getValue())){
				
				selectedQuestion.getQuestionOptions().remove(i);
				return;
			}
		}
	}
	
	public void cancelTemplateSub(){
		
		newTemplate.clear();
		currentOption.clear();
		currentQuestion.clear();
		selectedQuestion.clear();
	}
	
	public void clearTemplateInfo(){
		
		newTemplate.clear();
	}
	
	// ******************************************************************************* //
	// ***************************** GETTERS AND SETTERS ***************************** //
	// ******************************************************************************* //
	public SurveyTemplate getNewTemplate() {
		
		return newTemplate;
	}

	public void setNewTemplate(SurveyTemplate newTemplate) {
		
		this.newTemplate = newTemplate;
	}

	public SurveyTemplateDataModel getTemplateList() {
		
		return templateList;
	}

	public void setTemplateList(SurveyTemplateDataModel surveyList) {
		
		this.templateList = surveyList;
	}

	public SurveyTemplate getSelected() {
		
		return selected;
	}

	public void setSelected(SurveyTemplate selected) {
		
		this.selected = selected;
	}

	public Question getCurrentQuestion() {
		return currentQuestion;
	}

	public void setCurrentQuestion(Question currentQuestion) {
		this.currentQuestion = currentQuestion;
	}

	public QuestionOption getCurrentOption() {
		return currentOption;
	}

	public void setCurrentOption(QuestionOption currentOption) {
		this.currentOption = currentOption;
	}

	public Question getSelectedQuestion() {
		return selectedQuestion;
	}

	public void setSelectedQuestion(Question selectedQuestion) {
		this.selectedQuestion = selectedQuestion;
	}
	
}
