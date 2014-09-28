package tr.edu.metu.thesis.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import tr.edu.metu.thesis.eao.EAOManager;
import tr.edu.metu.thesis.events.EventDispatcher;
import tr.edu.metu.thesis.events.ServletEvent;
import tr.edu.metu.thesis.jpa.Participant.DataStatus;
import tr.edu.metu.thesis.jpa.SurveyInstance;
import tr.edu.metu.thesis.settings.Config;


@WebServlet("/UploadServlet")
@MultipartConfig
public class UploadServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    public UploadServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
						                      throws ServletException, IOException {
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
				   throws ServletException, IOException {
		
		try {
			
			doOnPost(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void doOnPost(HttpServletRequest request, HttpServletResponse response)
						 throws ServletException, IOException {
		
		String imei        = request.getParameter("imei");
		System.out.println(this.getClass().getName() + " :Uploading data file from device : " + imei);
		ServletEvent event = new ServletEvent(this, imei);
		event.setResult(DataStatus.RECEIVING_DATA);
		
		int surveyId       = Integer.parseInt(request.getParameter("survey_id"));
		boolean isLastFile = Boolean.parseBoolean(request.getParameter("is_last_file"));
	    Part filePart      = request.getPart("uploaded_file");
	    String filename    = null;

	    for (String cd : filePart.getHeader("content-disposition").split(";")) {
	        if (cd.trim().startsWith("filename")) {
	            filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
	            filename = filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1);
	        }
	    }
	    
	    event.putExtra(ServletKeys.FILENAME, filename);
	    try {
			EventDispatcher.getInstance().fireEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SurveyInstance instance = EAOManager.findById(surveyId, 
													  SurveyInstance.class);
	    if(instance == null)
	    	return;
	    
		String deviceDataPath = Config.getInstance().getDataFilePath();
		String separator      = Config.getInstance().getFileSeparator();
		String finalPath      = deviceDataPath.concat(instance.getFolderName())
					  						  .concat(separator)
					  						  .concat(imei)
					  						  .concat(separator);
		File devFolder = new File(finalPath);
		if(!devFolder.exists())
			devFolder.mkdirs();
		
	    InputStream filecontent = filePart.getInputStream();
	    FileOutputStream fos = new FileOutputStream(finalPath.concat(filename));
	    byte[] data = new byte[1024];
	    while(filecontent.read(data) > 0)
	    	fos.write(data);
	    fos.close();
	    		
		if(isLastFile){
			
			event = new ServletEvent(this, imei);
			event.setResult(DataStatus.COMPLETE);
		    try {
				EventDispatcher.getInstance().fireEvent(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
