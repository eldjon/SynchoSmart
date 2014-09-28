package tr.edu.metu.thesis.beans.utils;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.context.RequestContext;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;

@ManagedBean
@ViewScoped
public class TestBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7442939387224454924L;

	private boolean showSth;
	private String someText;
	
	public TestBean(){
		someText = "";
	}
	
	public void testMethod(){
		
//		PushContext context = PushContextFactory.getDefault().getPushContext();
//		context.push("/thesisEndpoint", "asdfasdf");
		showSth = true;
		someText = "asdfasdfa";
		System.out.println(" ------------------------- test method ------------------------ " + showSth);
//		EventBus eventBus = EventBusFactory.getDefault().eventBus();
//		eventBus.publish("/thesisEndpoint", "message to client");
//		RequestContext.getCurrentInstance().update("testBtn");
//		RequestContext.getCurrentInstance().update("mainForm:testBtn");
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//			}
//		}).start();
//		FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add(":mainForm:testBtn");	
//		FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("testBtn");	
		
	}

	public boolean isShowSth() {
		return showSth;
	}

	public void setShowSth(boolean showSth) {
		this.showSth = showSth;
	}

	public String getSomeText() {
		return someText;
	}

	public void setSomeText(String someText) {
		this.someText = someText;
	}
	
}
