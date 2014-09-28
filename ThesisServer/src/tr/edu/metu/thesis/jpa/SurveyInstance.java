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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import tr.edu.metu.thesis.beans.participant.ParticipantDataModel;
import tr.edu.metu.thesis.beans.utils.InstanceStatus;


/**
 * The persistent class for the survey_instance database table.
 * 
 */
@Entity
@Table(name="survey_instance")
@NamedQueries({
	@NamedQuery(name="SurveyInstance.findAll", query="SELECT s FROM SurveyInstance s"),
	@NamedQuery(name="SurveyInstance.findAllByNonStatus", query="SELECT s FROM SurveyInstance s WHERE s.status <>:status"),
	@NamedQuery(name="SurveyInstance.findAllByStatus", query="SELECT s FROM SurveyInstance s WHERE s.status =:status"),
	@NamedQuery(name="SurveyInstance.findActiveSurvey", query="SELECT s FROM SurveyInstance s WHERE s.status = 'STARTED' or s.status = 'PAUSED' or s.status = 'CONNECTED'"),
})
public class SurveyInstance implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="SURVEY_INSTANCE_GENERATOR", sequenceName="SURVEY_INSTANCE_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SURVEY_INSTANCE_GENERATOR")
	@Column(name="survey_instance_id")
	private int surveyInstanceId;

	@Column(name="description")
	private String description;

    @Temporal(TemporalType.TIMESTAMP)
	@Column(name="end_time")
	private Date endTime;

	@Column(name="estimated_duration")
	private int estimatedDuration;

    @Temporal(TemporalType.TIMESTAMP)
	@Column(name="estimated_start_time")
	private Date estimatedStartTime;

    @Column(name = "name")
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
	@Column(name="start_time")
	private Date startTime;

	@Column(name="status")
	private String status;

	//bi-directional many-to-one association to Participant
	@OneToMany(mappedBy="surveyInstance", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Participant> participants;

	//bi-directional many-to-one association to SurveyTemplate
	@ManyToOne
	@JoinColumn(name="survey_template_id")
	private SurveyTemplate surveyTemplate;

	//bi-directional many-to-one association to SurveyInstanceQuestion
	@OneToMany(mappedBy="surveyInstance", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
	private List<SurveyInstanceQuestion> surveyInstanceQuestions;

	// status related fields
	@Transient
	private boolean canConnect = false;
	@Transient
	private boolean canStart   = false;
	@Transient
	private boolean canPause   = false;
	@Transient
	private boolean canResume  = false;
	@Transient
	private boolean canEnd     = false;
	@Transient
	private ParticipantDataModel participantsDm;

	
	public SurveyInstance() {
		name   = "";
		updateStatuses();
	}
	
	public boolean isSeatNumberUnique(String seatNumber, String imei){
		
		for(Participant p : participants)
			if(p.getSeatNumber() != null){
				if(seatNumber.equals(p.getSeatNumber()) 
						&& !p.getDevice().getImei().equals(imei))
					return false;
			}
		
		return true;
	}
	
	public Participant findParticipant(String imei){
		
		for(Participant p : participants)
			if(p.getDevice().getImei().equals(imei))
				return p;
		
		return null;
	}
	
	public SurveyInstanceQuestion findSiq(int surveyId, int questionId){
		
		for(SurveyInstanceQuestion siq : surveyInstanceQuestions)
			if(siq.getSurveyInstance().getSurveyInstanceId() == surveyId
			&& siq.getQuestion().getQuestionId() == questionId)
				return siq;
		
		return null;
	}
 
	public List<Device> getSelectedDevices(){

		List<Device> dev = new ArrayList<Device>();
		if(participants == null || participants.size() <= 0)
			return dev;
		
		for(Participant p : participants)
			dev.add(p.getDevice());
		
		return dev;
	}

	public ParticipantDataModel getParticipantsDm() {
		
		if(participantsDm == null){
			if(participants == null)
				participantsDm = new ParticipantDataModel(new ArrayList<Participant>());
			else participantsDm = new ParticipantDataModel(participants);
		}
		
		return participantsDm;
	}
	
	protected void updateStatuses(){
		
		canConnect = false;
		canEnd     = false;
		canPause   = false;
		canResume  = false;
		canStart   = false;
		
		if(surveyInstanceId > 0){
			if(getStatus().equals(InstanceStatus.CREATED.name())){
				canConnect = true;
			}
			else if(getStatus().equals(InstanceStatus.CONNECTED.name())){
				canStart = true;
				canEnd   = true;
			}
			else if(getStatus().equals(InstanceStatus.STARTED.name())){
				canPause = true;
				canEnd   = true;
			}
			else if(getStatus().equals(InstanceStatus.PAUSED.name())){
				canResume = true;
				canEnd    = true;
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(!(obj instanceof SurveyInstance))
			return false;
		
		if( this.surveyInstanceId == ((SurveyInstance)obj).surveyInstanceId)
			return true;
		
		return false;
	}

	public Participant addParticipant(Participant participant) {
		getParticipants().add(participant);
		participant.setSurveyInstance(this);

		return participant;
	}

	public Participant removeParticipant(Participant participant) {
		getParticipants().remove(participant);
		participant.setSurveyInstance(null);

		return participant;
	}

	public SurveyInstanceQuestion addSurveyInstanceQuestion(SurveyInstanceQuestion surveyInstanceQuestion) {
		getSurveyInstanceQuestions().add(surveyInstanceQuestion);
		surveyInstanceQuestion.setSurveyInstance(this);

		return surveyInstanceQuestion;
	}

	public SurveyInstanceQuestion removeSurveyInstanceQuestion(SurveyInstanceQuestion surveyInstanceQuestion) {
		getSurveyInstanceQuestions().remove(surveyInstanceQuestion);
		surveyInstanceQuestion.setSurveyInstance(null);

		return surveyInstanceQuestion;
	}

	public int getSurveyInstanceId() {
		return surveyInstanceId;
	}

	public void setSurveyInstanceId(int surveyInstanceId) {
		this.surveyInstanceId = surveyInstanceId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public int getEstimatedDuration() {
		return estimatedDuration;
	}

	public void setEstimatedDuration(int estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}

	public Date getEstimatedStartTime() {
		return estimatedStartTime;
	}

	public void setEstimatedStartTime(Date estimatedStartTime) {
		this.estimatedStartTime = estimatedStartTime;
	}

	public String getName() {
		
		if(name != null)
			return name.trim();
		
		return null;
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getStatus() {
		
		if(status != null)
			return status.trim();
		
		return null;
	}

	public void setStatus(String status) {
		this.status = status.trim();
		updateStatuses();
	}

	public List<Participant> getParticipants() {
		return participants;
	}

	public void setParticipants(List<Participant> participants) {
		this.participants = participants;
	}

	public SurveyTemplate getSurveyTemplate() {
		return surveyTemplate;
	}

	public void setSurveyTemplate(SurveyTemplate surveyTemplate) {
		this.surveyTemplate = surveyTemplate;
	}

	public List<SurveyInstanceQuestion> getSurveyInstanceQuestions() {
		return surveyInstanceQuestions;
	}

	public void setSurveyInstanceQuestions(
			List<SurveyInstanceQuestion> surveyInstanceQuestions) {
		this.surveyInstanceQuestions = surveyInstanceQuestions;
	}

	public boolean getCanConnect() {
		updateStatuses();
		return canConnect;
	}

	public void setCanConnect(boolean canConnect) {
		this.canConnect = canConnect;
	}

	public boolean getCanStart() {
		updateStatuses();
		return canStart;
	}

	public void setCanStart(boolean canStart) {
		this.canStart = canStart;
	}

	public boolean getCanPause() {
		updateStatuses();
		return canPause;
	}

	public void setCanPause(boolean canPause) {
		this.canPause = canPause;
	}

	public boolean getCanResume() {
		updateStatuses();
		return canResume;
	}

	public void setCanResume(boolean canResume) {
		this.canResume = canResume;
	}

	public boolean getCanEnd() {
		updateStatuses();
		return canEnd;
	}

	public void setCanEnd(boolean canEnd) {
		this.canEnd = canEnd;
	}

	public String getFolderName() {
		
		return name.concat("_")
				   .concat(Integer.toString(surveyInstanceId));
	}

	public void setParticipantsDm(ParticipantDataModel participantsDm) {
		this.participantsDm = participantsDm;
	}
}