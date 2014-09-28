package tr.edu.metu.thesis.jpa;

import java.io.Serializable;
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
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;


/**
 * The persistent class for the participant database table.
 * 
 */
@Entity
@NamedQuery(name="Participant.findAll", query="SELECT p FROM Participant p")
public class Participant implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PARTICIPANT_GENERATOR", sequenceName="PARTICIPANT_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PARTICIPANT_GENERATOR")
	@Column(name="participant_id")
	private int participantId;

	@Column(name= "occupation")
	private String occupation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="date_of_birth")
	private Date dateOfBirth;

	@Column(name="firstname")
	private String firstname;

	@Column(name="gender")
	private boolean gender;

	@Column(name="lastname")
	private String lastname;

	@Column(name="nationality")
	private String nationality;

	@Column(name="seat_number")
	private String seatNumber;

	//bi-directional many-to-one association to Device
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="device_id")
	private Device device;

	//bi-directional many-to-one association to SurveyInstance
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="survey_instance_id")
	private SurveyInstance surveyInstance;

	//bi-directional many-to-one association to ParticipantAnswer
	@OneToMany(mappedBy="participant", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ParticipantAnswer> participantAnswers;

	//bi-directional many-to-one association to ParticipantSensorRecord
	@OneToMany(mappedBy="participant")
	private List<ParticipantSensorRecord> participantSensorRecords;

	// Data delivery status, and the command received from server
	@Transient
	private String dataStatus;

	public Participant() {
		
		device       = new Device();
		dataStatus   = DataStatus.WAITING.name();
	}

	public int getParticipantId() {
		return this.participantId;
	}

	public void setParticipantId(int participantId) {
		this.participantId = participantId;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public boolean getGender() {
		return this.gender;
	}

	public void setGender(boolean gender) {
		this.gender = gender;
	}

	public String getLastname() {
		return this.lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getNationality() {
		return this.nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public Device getDevice() {
		return this.device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public SurveyInstance getSurveyInstance() {
		return this.surveyInstance;
	}

	public void setSurveyInstance(SurveyInstance surveyInstance) {
		this.surveyInstance = surveyInstance;
	}

	public List<ParticipantAnswer> getParticipantAnswers() {
		return this.participantAnswers;
	}

	public void setParticipantAnswers(List<ParticipantAnswer> participantAnswers) {
		this.participantAnswers = participantAnswers;
	}

	public ParticipantAnswer addParticipantAnswer(ParticipantAnswer participantAnswer) {
		getParticipantAnswers().add(participantAnswer);
		participantAnswer.setParticipant(this);

		return participantAnswer;
	}

	public ParticipantAnswer removeParticipantAnswer(ParticipantAnswer participantAnswer) {
		getParticipantAnswers().remove(participantAnswer);
		participantAnswer.setParticipant(null);

		return participantAnswer;
	}

	public List<ParticipantSensorRecord> getParticipantSensorRecords() {
		return this.participantSensorRecords;
	}

	public void setParticipantSensorRecords(List<ParticipantSensorRecord> participantSensorRecords) {
		this.participantSensorRecords = participantSensorRecords;
	}

	public ParticipantSensorRecord addParticipantSensorRecord(ParticipantSensorRecord participantSensorRecord) {
		getParticipantSensorRecords().add(participantSensorRecord);
		participantSensorRecord.setParticipant(this);

		return participantSensorRecord;
	}

	public ParticipantSensorRecord removeParticipantSensorRecord(ParticipantSensorRecord participantSensorRecord) {
		getParticipantSensorRecords().remove(participantSensorRecord);
		participantSensorRecord.setParticipant(null);

		return participantSensorRecord;
	}

	public String getDataStatus() {
		
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		
		if(dataStatus == null)
			this.dataStatus = DataStatus.WAITING.name();
		else 
			this.dataStatus = dataStatus;
	}

	public String getSeatNumber() {
		
		if(seatNumber == null)
			return null;
		
		return seatNumber.trim();
	}

	public void setSeatNumber(String seatNumber) {
		this.seatNumber = seatNumber;
	}

	public static enum DeviceStatus{
		
		CONNECTED,
		DISCONNECTED;
	}
	
	public static enum DataStatus{
		
		WAITING,
		RECEIVING_DATA,
		COMPLETE;
	}
}