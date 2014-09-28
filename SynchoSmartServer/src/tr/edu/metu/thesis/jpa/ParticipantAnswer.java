package tr.edu.metu.thesis.jpa;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the participant_answer database table.
 * 
 */
@Entity
@Table(name="participant_answer")
@NamedQuery(name="ParticipantAnswer.findAll", query="SELECT p FROM ParticipantAnswer p")
public class ParticipantAnswer implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PARTICIPANT_ANSWER_GENERATOR", sequenceName="PARTICIPANT_ANSWER_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PARTICIPANT_ANSWER_GENERATOR")
	@Column(name="participant_answer_id")
	private int participantAnswerId;

	//bi-directional many-to-one association to Participant
	@ManyToOne
	@JoinColumn(name="participant_id")
	private Participant participant;

	//bi-directional many-to-one association to SurveyInstanceQuestion
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="survery_instance_question_id")
	private SurveyInstanceQuestion surveyInstanceQuestion;
	
	@Column(name = "value")
	private String value;
	
	@Column(name = "selected_option")
	private int selectedOption;

	public ParticipantAnswer() {
	}

	public int getParticipantAnswerId() {
		return this.participantAnswerId;
	}

	public void setParticipantAnswerId(int participantAnswerId) {
		this.participantAnswerId = participantAnswerId;
	}

	public Participant getParticipant() {
		return this.participant;
	}

	public void setParticipant(Participant participant) {
		this.participant = participant;
	}

	public SurveyInstanceQuestion getSurveyInstanceQuestion() {
		return this.surveyInstanceQuestion;
	}

	public void setSurveyInstanceQuestion(SurveyInstanceQuestion surveyInstanceQuestion) {
		this.surveyInstanceQuestion = surveyInstanceQuestion;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getSelectedOption() {
		return selectedOption;
	}

	public void setSelectedOption(int selectedOption) {
		this.selectedOption = selectedOption;
	}

}