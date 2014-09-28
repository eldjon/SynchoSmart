package tr.edu.metu.thesis.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * The persistent class for the survey_template database table.
 * 
 */
@Entity
@Table(name="survey_template")
@NamedQuery(name="SurveyTemplate.findAll", query="SELECT s FROM SurveyTemplate s")
public class SurveyTemplate implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="SURVEY_TEMPLATE_GENERATOR", sequenceName="SURVEY_TEMPLATE_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SURVEY_TEMPLATE_GENERATOR")
	@Column(name="survey_template_id")
	private int surveyTemplateId;

	@Column(name = "description")
	private String description;

	@Column(name = "name")
	private String name;

    @Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creation_date")
	private Date creationDate;

	//bi-directional many-to-one association to Question
	@OneToMany(mappedBy="surveyTemplate", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Question> questions;

	//bi-directional many-to-one association to SurveyInstance
	@OneToMany(mappedBy="surveyTemplate", fetch = FetchType.EAGER)
	private List<SurveyInstance> surveyInstances;

	public SurveyTemplate() {
		
		questions       = new ArrayList<>();
		surveyInstances = new ArrayList<>();
	}
	
	public void clear(){
		
		description      = "";
		name             = "";
		creationDate     = null;
		questions.clear();
		surveyInstances.clear();
	}
	

	public int getSurveyTemplateId() {
		return this.surveyTemplateId;
	}

	public void setSurveyTemplateId(int surveyTemplateId) {
		this.surveyTemplateId = surveyTemplateId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Question> getQuestions() {
		return this.questions;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	public Question addQuestion(Question question) {
		getQuestions().add(question);
		question.setSurveyTemplate(this);

		return question;
	}

	public Question removeQuestion(Question question) {
		getQuestions().remove(question);
		question.setSurveyTemplate(null);

		return question;
	}

	public List<SurveyInstance> getSurveyInstances() {
		return this.surveyInstances;
	}

	public void setSurveyInstances(List<SurveyInstance> surveyInstances) {
		this.surveyInstances = surveyInstances;
	}

	public SurveyInstance addSurveyInstance(SurveyInstance surveyInstance) {
		getSurveyInstances().add(surveyInstance);
		surveyInstance.setSurveyTemplate(this);

		return surveyInstance;
	}

	public SurveyInstance removeSurveyInstance(SurveyInstance surveyInstance) {
		getSurveyInstances().remove(surveyInstance);
		surveyInstance.setSurveyTemplate(null);

		return surveyInstance;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

}