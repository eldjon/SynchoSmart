package tr.edu.metu.thesis.jpa;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the question_option database table.
 * 
 */
@Entity
@Table(name="question_option")
@NamedQuery(name="QuestionOption.findAll", query="SELECT q FROM QuestionOption q")
public class QuestionOption implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="QUESTION_OPTION_GENERATOR", sequenceName="QUESTION_OPTION_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="QUESTION_OPTION_GENERATOR")
	@Column(name="question_option_id")
	private int questionOptionId;

	@Column(name="value")
	private String value;

	@Column(name="section")
	private String section;

	//bi-directional many-to-one association to Question
	@ManyToOne
	@JoinColumn(name="question_id")
	private Question question;

	public QuestionOption() {
		
		value   = "";
		section = "";
	}

	public void clear(){
		
		value            = "";
		section          = "";
	}
	
	public QuestionOption(QuestionOption other){
		
		value    = other.value;
		section  = other.section;
		question = other.question;
	}
	
	public int getQuestionOptionId() {
		return this.questionOptionId;
	}

	public void setQuestionOptionId(int questionOptionId) {
		this.questionOptionId = questionOptionId;
	}

	public Question getQuestion() {
		return this.question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}
}