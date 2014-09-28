package tr.edu.metu.thesis.beans.participant;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import tr.edu.metu.thesis.jpa.Participant;

@SuppressWarnings("unchecked")
public class ParticipantDataModel  extends ListDataModel<Participant>
                                   implements SelectableDataModel<Participant>, 
                                              Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8621009711459141040L;
	private List<Participant> list;

	public ParticipantDataModel(List<Participant> data){
		
		super(data);
		list = data;
	}
	
	@Override
	public Participant getRowData(String id) {
		
		List<Participant> data = (List<Participant>) getWrappedData();
		for(Participant d : data)
			if(((int)d.getParticipantId()) == Integer.parseInt(id))
				return d;
		return null;
	}

	@Override
	public Object getRowKey(Participant p) {
		
		return p.getParticipantId();
	}
	
	public List<Participant> getParticipantList(){
		
		return list;
	}
}
