package tr.edu.metu.thesis.events;

public class SoapEvent extends ServerEvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6110143092939862259L;
	public static final String SOAP_EVENT_NAME = BASE_NAME + "soapevent";

	protected String _calledByImei = null;
	protected String _methodName   = null;
	
	public SoapEvent(Object source, 
					 String calledByDevice,
					 String methodName) {
		
		super(source, SOAP_EVENT_NAME);
		_calledByImei = calledByDevice;
		_methodName   = methodName;
	}
	
	
	/**
	 * 
	 * @return the imei of the client device which called the web service
	 */
	public String getCallingImei(){
		
		return _calledByImei;
	}
	
	public String getMethodCalled(){
		
		return _methodName;
	}

}
