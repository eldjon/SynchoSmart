package tr.edu.metu.thesis.settings;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name = "guiSettings")
@SessionScoped
public class GuiSettings implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -710048828110593446L;
	private int textLength;
	private String width200;
	
	
	public GuiSettings(){
		
		textLength = 200;
	}


	// ******************************************************************************* //
	// ***************************** GETTERS AND SETTER ****************************** //
	// ******************************************************************************* //
	public int getTextLength() {
		
		return textLength;
	}


	public void setTextLength(int textLength) {
		
		this.textLength = textLength;
	}


	public String getWidth200() {
		return width200;
	}


	public void setWidth200(String width200) {
		this.width200 = width200;
	}
	
}
