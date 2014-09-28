package tr.edu.metu.thesis.beans.templates;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import tr.edu.metu.thesis.eao.EAOManager;
import tr.edu.metu.thesis.jpa.SurveyTemplate;

public class SurveyTemplateDataModel extends ListDataModel<SurveyTemplate>
							 implements Serializable, SelectableDataModel<SurveyTemplate>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8559462152607500621L;

	public SurveyTemplateDataModel(List<SurveyTemplate> data){
		
		super(data);
	}
	
	@Override
	public SurveyTemplate getRowData(String id) {
		
		return EAOManager.findById(Integer.parseInt(id), 
								   SurveyTemplate.class);
	}

	@Override
	public Object getRowKey(SurveyTemplate template) {
		
		return template.getSurveyTemplateId();
	}

}
