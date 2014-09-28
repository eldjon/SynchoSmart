package tr.edu.metu.thesis.beans.survey;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import tr.edu.metu.thesis.eao.EAOManager;
import tr.edu.metu.thesis.jpa.SurveyInstance;

public class SurveyDataModel extends ListDataModel<SurveyInstance>
							 implements Serializable, SelectableDataModel<SurveyInstance> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3933414638674062780L;

	
	public SurveyDataModel(List<SurveyInstance> data){
		
		super(data);
	}
	
	@Override
	public SurveyInstance getRowData(String id) {
		
		return EAOManager.findById(Integer.parseInt(id), 
								   SurveyInstance.class);
	}

	@Override
	public Object getRowKey(SurveyInstance survey) {
		
		return survey.getSurveyInstanceId();
	}

}
