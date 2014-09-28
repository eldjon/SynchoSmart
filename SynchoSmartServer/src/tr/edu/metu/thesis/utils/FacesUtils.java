package tr.edu.metu.thesis.utils;

import java.io.IOException;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public final class FacesUtils {

	public static void addErrorMessage(String msg){
		
		FacesContext.getCurrentInstance().addMessage(null, 
				new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
	}

	public static void addInfoMessage(String msg){
		
		FacesContext.getCurrentInstance().addMessage(null, 
				new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
	}

	public static void addWarnMessage(String msg){
		
		FacesContext.getCurrentInstance().addMessage(null, 
				new FacesMessage(FacesMessage.SEVERITY_WARN, msg, null));
	}

	public static Object getSessionBean(String beanName){
		
		return FacesContext.getCurrentInstance()
						   .getExternalContext()
						   .getSessionMap()
						   .get(beanName);
	}
	
	public static Object getRequestBean(String beanName){
		
		return FacesContext.getCurrentInstance()
						   .getExternalContext()
						   .getRequestMap()
						   .get(beanName);
	}
	
	public static String getLocaleName(){
		
		String localeName = FacesContext.getCurrentInstance()
										.getViewRoot()
										.getLocale()
										.getDisplayLanguage();
		return localeName;
	}
	
	public static Locale getLocale(){
		
		return FacesContext.getCurrentInstance()
						   .getViewRoot()
						   .getLocale();
	}
	
	public static void closeSession(){
		
		HttpSession session =  (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		session.invalidate();
		HttpServletResponse response =  (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.setHeader("Cache-control","no-store");
		response.setHeader("Pragma","no-cache");
		response.setDateHeader("Expires", -1); 
	}
	
	public static String getParameter(String paramName){
		
		String param = FacesContext.getCurrentInstance()
								   .getExternalContext()
								   .getRequestParameterMap()
								   .get(paramName);
		return param;
	}
	
    public static <E> Object getViewBean(String beanName, Class<E> classOfBean){
    	
            FacesContext context  = FacesContext.getCurrentInstance();
            E bean = (E) context.getApplication()
            					.evaluateExpressionGet(context, 
            										   "#{"+beanName+"}", 
            										   classOfBean);
            return bean;
    }
    
    public static void redirect(String page){
		try {
			FacesContext.getCurrentInstance()
						.getExternalContext()
						.redirect(page);
		} catch (IOException e) {
			e.printStackTrace();
		}	
    }

}
