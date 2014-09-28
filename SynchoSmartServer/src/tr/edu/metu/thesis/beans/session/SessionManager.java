package tr.edu.metu.thesis.beans.session;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.resource.spi.IllegalStateException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import tr.edu.metu.thesis.eao.EAOManager;
import tr.edu.metu.thesis.socket.SocketHandler;

@ManagedBean(name = "sessionManager")
public class SessionManager implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4513315028262081176L;

	public void closeSession(){
		
		System.out.println(" ----------- closing session and EMF ------------------ ");
		EAOManager.close();
		HttpSession session =  (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		session.invalidate();
		HttpServletResponse response =  (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.setHeader("Cache-control","no-store");
		response.setHeader("Pragma","no-cache");
		response.setDateHeader("Expires", -1); 
		
		// close Socket 
		try {
			SocketHandler.getInstance().close();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
}
