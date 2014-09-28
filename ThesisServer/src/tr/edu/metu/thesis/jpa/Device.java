package tr.edu.metu.thesis.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import tr.edu.metu.thesis.jpa.Participant.DeviceStatus;
import tr.edu.metu.thesis.socket.ServerMessage;
import tr.edu.metu.thesis.socket.ServerMessage.SocketMessageType;


/**
 * The persistent class for the device database table.
 * 
 */
@Entity
@NamedQuery(name="Device.findAll", query="SELECT d FROM Device d")
public class Device implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3855807540792514522L;

	@Id
	@SequenceGenerator(name="DEVICE_GENERATOR", sequenceName="DEVICE_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="DEVICE_GENERATOR")
	@Column(name="device_id")
	private int deviceId;

	@Column(name = "brand")
	private String brand;

	@Column(name = "imei")
	private String imei;

	@Column(name = "model")
	private String model;

	@Column(name = "current_port")
	private int currentPort;

	@Column(name = "current_ip_address")
	private String currentIpAddress;

	//bi-directional many-to-one association to Participant
	@OneToMany(mappedBy="device")
	private List<Participant> participants;

	//bi-directional many-to-one association to Sensor
	@OneToMany(mappedBy="device", fetch = FetchType.EAGER, orphanRemoval = true)
	private List<Sensor> sensors;

	@Transient
	private String status;
	@Transient
	private ServerMessage lastReceivedCmd;
	@Transient
	private List<ServerMessage> receivedCmds;
	

	public Device() {
		
		imei = "";
//		receivedCmds    = new ArrayList<ServerMessage>();
//		lastReceivedCmd = new ServerMessage(SocketMessageType.EMPTY);
//		receivedCmds.add(lastReceivedCmd);
	}

	public void clear(){
		
		deviceId = 0;
		brand    = "";
		imei     = "";
		model    = "";
		participants.clear();
		sensors.clear();
		status = DeviceStatus.DISCONNECTED.name();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + deviceId;
		result = prime * result + ((imei == null) ? 0 : imei.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Device))
			return false;
		Device other = (Device) obj;
		if (deviceId != other.deviceId)
			return false;
		if(imei.equals(other.imei))
			return true;
		return false;
	}


	public int getDeviceId() {
		return this.deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public String getBrand() {
		return this.brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getImei() {
		
		if(imei != null)
			return this.imei.trim();
		
		return null;
	}

	public void setImei(String imei) {
		this.imei = imei.trim();
	}

	public String getModel() {
		
		if(model != null)
			return this.model.trim();
		
		return model;
	}

	public void setModel(String model) {
		this.model = model.trim();
	}

	public List<Participant> getParticipants() {
		return this.participants;
	}

	public void setParticipants(List<Participant> participants) {
		this.participants = participants;
	}

	public Participant addParticipant(Participant participant) {
		getParticipants().add(participant);
		participant.setDevice(this);

		return participant;
	}

	public Participant removeParticipant(Participant participant) {
		getParticipants().remove(participant);
		participant.setDevice(null);

		return participant;
	}

	public List<Sensor> getSensors() {
		return this.sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}

	public Sensor addSensor(Sensor sensor) {
		getSensors().add(sensor);
		sensor.setDevice(this);

		return sensor;
	}

	public Sensor removeSensor(Sensor sensor) {
		getSensors().remove(sensor);
		sensor.setDevice(null);

		return sensor;
	}

	public String getCurrentIpAddress() {
		if(currentIpAddress == null)
			return null;
		return currentIpAddress.trim();
	}

	public void setCurrentIpAddress(String currentIpAddress) {
		this.currentIpAddress = currentIpAddress.trim();
	}

	public int getCurrentPort() {
		return currentPort;
	}

	public void setCurrentPort(int currentPort) {
		this.currentPort = currentPort;
	}

	public String getStatus() {
		if(status == null)
			status = DeviceStatus.DISCONNECTED.name();
		return status.trim();
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ServerMessage getLastReceivedCmd() {
		
		if(lastReceivedCmd == null)
			lastReceivedCmd = new ServerMessage(SocketMessageType.EMPTY);
		return lastReceivedCmd;
	}

	public void setLastReceivedCmd(ServerMessage lastReceivedCmd) {
		
		this.lastReceivedCmd = lastReceivedCmd;
	}

	public List<ServerMessage> getReceivedCmds() {
		return receivedCmds;
	}

	public void setReceivedCmds(List<ServerMessage> receivedCmds) {
		this.receivedCmds = receivedCmds;
	}
	
	public void addNextCmd(ServerMessage lastCmd){
		
		lastReceivedCmd = lastCmd;
		if(receivedCmds == null)
			receivedCmds = new ArrayList<ServerMessage>();
		receivedCmds.add(lastCmd);
	}
}