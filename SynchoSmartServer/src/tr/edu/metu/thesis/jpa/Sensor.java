package tr.edu.metu.thesis.jpa;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the sensor database table.
 * 
 */
@Entity
@NamedQuery(name="Sensor.findAll", query="SELECT s FROM Sensor s")
public class Sensor implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="SENSOR_SENSORID_GENERATOR", sequenceName="ID_SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SENSOR_SENSORID_GENERATOR")
	@Column(name="sensor_id")
	private Integer sensorId;

	private String type;

	//bi-directional many-to-one association to ParticipantSensorRecord
	@OneToMany(mappedBy="sensor")
	private List<ParticipantSensorRecord> participantSensorRecords;

	//bi-directional many-to-one association to Device
	@ManyToOne
	@JoinColumn(name="device_id")
	private Device device;

	public Sensor() {
	}

	public Integer getSensorId() {
		return this.sensorId;
	}

	public void setSensorId(Integer sensorId) {
		this.sensorId = sensorId;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<ParticipantSensorRecord> getParticipantSensorRecords() {
		return this.participantSensorRecords;
	}

	public void setParticipantSensorRecords(List<ParticipantSensorRecord> participantSensorRecords) {
		this.participantSensorRecords = participantSensorRecords;
	}

	public ParticipantSensorRecord addParticipantSensorRecord(ParticipantSensorRecord participantSensorRecord) {
		getParticipantSensorRecords().add(participantSensorRecord);
		participantSensorRecord.setSensor(this);

		return participantSensorRecord;
	}

	public ParticipantSensorRecord removeParticipantSensorRecord(ParticipantSensorRecord participantSensorRecord) {
		getParticipantSensorRecords().remove(participantSensorRecord);
		participantSensorRecord.setSensor(null);

		return participantSensorRecord;
	}

	public Device getDevice() {
		return this.device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

}