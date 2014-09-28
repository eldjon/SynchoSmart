package tr.edu.metu.thesis.jpa;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the survey_instance_question database table.
 * 
 */
@Entity
@Table(name="survey_instance_question")
@NamedQuery(name="SurveyInstanceQuestion.findAll", query="SELECT s FROM SurveyInstanceQuestion s")
public class SurveyInstanceQuestion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="SURVEY_INSTANCE_QUESTION_GENERATOR", sequenceName="SURVEY_INSTANCE_QUESTION_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SURVEY_INSTANCE_QUESTION_GENERATOR")
	@Column(name="survery_instance_question_id")
	private int surveryInstanceQuestionId;

	//bi-directional many-to-one association to ParticipantAnswer
	@OneToMany(mappedBy="surveyInstanceQuestion")
	private List<ParticipantAnswer> participantAnswers;

	//bi-directional many-to-one association to Question
	@ManyToOne
	@JoinColumn(name="question_id")
	private Question question;

	//bi-directional many-to-one association to SurveyInstance
	@ManyToOne
	@JoinColumn(name="survey_instance_id")
	private SurveyInstance surveyInstance;

	public SurveyInstanceQuestion() {
	}

	public int getSurveryInstanceQuestionId() {
		return this.surveryInstanceQuestionId;
	}

	public void setSurveryInstanceQuestionId(int surveryInstanceQuestionId) {
		this.surveryInstanceQuestionId = surveryInstanceQuestionId;
	}

	public List<ParticipantAnswer> getParticipantAnswers() {
		return this.participantAnswers;
	}

	public void setParticipantAnswers(List<ParticipantAnswer> participantAnswers) {
		this.participantAnswers = participantAnswers;
	}

	public ParticipantAnswer addParticipantAnswer(ParticipantAnswer participantAnswer) {
		getParticipantAnswers().add(participantAnswer);
		participantAnswer.setSurveyInstanceQuestion(this);

		return participantAnswer;
	}

	public ParticipantAnswer removeParticipantAnswer(ParticipantAnswer participantAnswer) {
		getParticipantAnswers().remove(participantAnswer);
		participantAnswer.setSurveyInstanceQuestion(null);

		return participantAnswer;
	}

	public Question getQuestion() {
		return this.question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public SurveyInstance getSurveyInstance() {
		return this.surveyInstance;
	}

	public void setSurveyInstance(SurveyInstance surveyInstance) {
		this.surveyInstance = surveyInstance;
	}

}