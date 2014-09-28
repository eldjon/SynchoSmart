package tr.edu.metu.thesis.jpa;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the participant_sensor_record database table.
 * 
 */
@Entity
@Table(name="participant_sensor_record")
@NamedQuery(name="ParticipantSensorRecord.findAll", query="SELECT p FROM ParticipantSensorRecord p")
public class ParticipantSensorRecord implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PARTICIPANT_SENSOR_RECORD_PARTICIPANTSENSORRECORDID_GENERATOR", sequenceName="ID_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PARTICIPANT_SENSOR_RECORD_PARTICIPANTSENSORRECORDID_GENERATOR")
	@Column(name="participant_sensor_record_id")
	private Integer participantSensorRecordId;

	private String data;

	@Column(name="submitted_on")
	private Timestamp submittedOn;

	//bi-directional many-to-one association to Participant
	@ManyToOne
	@JoinColumn(name="participant_id")
	private Participant participant;

	//bi-directional many-to-one association to Sensor
	@ManyToOne
	@JoinColumn(name="sensor_id")
	private Sensor sensor;

	public ParticipantSensorRecord() {
	}

	public Integer getParticipantSensorRecordId() {
		return this.participantSensorRecordId;
	}

	public void setParticipantSensorRecordId(Integer participantSensorRecordId) {
		this.participantSensorRecordId = participantSensorRecordId;
	}

	public String getData() {
		return this.data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Timestamp getSubmittedOn() {
		return this.submittedOn;
	}

	public void setSubmittedOn(Timestamp submittedOn) {
		this.submittedOn = submittedOn;
	}

	public Participant getParticipant() {
		return this.participant;
	}

	public void setParticipant(Participant participant) {
		this.participant = participant;
	}

	public Sensor getSensor() {
		return this.sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

}