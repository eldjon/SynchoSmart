package tr.edu.metu.thesis.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tr.edu.metu.thesis.events.EventDispatcher;
import tr.edu.metu.thesis.events.ServletEvent;

@WebServlet("/NotifyServlet")
@MultipartConfig
public class NotifyServlet extends HttpServlet{

	private static final long serialVersionUID = 6011867697516833942L;
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			   throws ServletException, IOException {
	
		String imei = request.getParameter("imei");
		int    cmd  = Integer.parseInt(request.getParameter("command"));
		
		ServletEvent event = new ServletEvent(this, imei);
		event.setResult(cmd);
		
	    try {
			EventDispatcher.getInstance().fireEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
