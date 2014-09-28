package tr.edu.metu.thesis.jpa;

import java.io.Serializable;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the question database table.
 * 
 */
@Entity
@NamedQuery(name="Question.findAll", query="SELECT q FROM Question q")
public class Question implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="QUESTION_GENERATOR", sequenceName="QUESTION_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="QUESTION_GENERATOR")
	@Column(name="question_id")
	private int questionId;

	@Column(name="value")
	private String value;

	//bi-directional many-to-one association to SurveyTemplate
	@ManyToOne
	@JoinColumn(name="survey_template_id")
	private SurveyTemplate surveyTemplate;

	//bi-directional many-to-one association to QuestionOption
	@OneToMany(mappedBy="question", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<QuestionOption> questionOptions;

	//bi-directional many-to-one association to SurveyInstanceQuestion
	@OneToMany(mappedBy="question")
	private List<SurveyInstanceQuestion> surveyInstanceQuestions;

	public Question() {
		
		value                   = "";
		questionOptions         = new ArrayList<>();
		surveyInstanceQuestions = new ArrayList<>();
	}
	
	public Question(Question question){
		
		this.value                   = question.value;
		this.questionOptions         = new ArrayList<>(question.questionOptions);
		this.surveyInstanceQuestions = new ArrayList<>(question.surveyInstanceQuestions);
		this.surveyTemplate          = question.surveyTemplate;
	}
	
	public void clear(){
		
		value          = "";
		surveyInstanceQuestions.clear();
		surveyTemplate = null;
		questionOptions.clear();
	}

	@Override
	public boolean equals(Object obj) {
		
		if(!(obj instanceof Question))
			return false;
		
		Question other = (Question) obj;
		return this.questionId == other.questionId ? true : false;
	}

	public int getQuestionId() {
		return this.questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public SurveyTemplate getSurveyTemplate() {
		return this.surveyTemplate;
	}

	public void setSurveyTemplate(SurveyTemplate surveyTemplate) {
		this.surveyTemplate = surveyTemplate;
	}

	public List<QuestionOption> getQuestionOptions() {
		return this.questionOptions;
	}

	public void setQuestionOptions(List<QuestionOption> questionOptions) {
		this.questionOptions = questionOptions;
	}

	public QuestionOption addQuestionOption(QuestionOption questionOption) {
		getQuestionOptions().add(questionOption);
		questionOption.setQuestion(this);

		return questionOption;
	}

	public QuestionOption removeQuestionOption(QuestionOption questionOption) {
		getQuestionOptions().remove(questionOption);
		questionOption.setQuestion(null);

		return questionOption;
	}

	public List<SurveyInstanceQuestion> getSurveyInstanceQuestions() {
		return this.surveyInstanceQuestions;
	}

	public void setSurveyInstanceQuestions(List<SurveyInstanceQuestion> surveyInstanceQuestions) {
		this.surveyInstanceQuestions = surveyInstanceQuestions;
	}

	public SurveyInstanceQuestion addSurveyInstanceQuestion(SurveyInstanceQuestion surveyInstanceQuestion) {
		getSurveyInstanceQuestions().add(surveyInstanceQuestion);
		surveyInstanceQuestion.setQuestion(this);

		return surveyInstanceQuestion;
	}

	public SurveyInstanceQuestion removeSurveyInstanceQuestion(SurveyInstanceQuestion surveyInstanceQuestion) {
		getSurveyInstanceQuestions().remove(surveyInstanceQuestion);
		surveyInstanceQuestion.setQuestion(null);

		return surveyInstanceQuestion;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}