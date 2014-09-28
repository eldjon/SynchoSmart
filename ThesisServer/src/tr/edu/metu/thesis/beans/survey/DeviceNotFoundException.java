package tr.edu.metu.thesis.beans.survey;

public class DeviceNotFoundException extends Exception{

	private static final long serialVersionUID = 1581255256517620989L;

    public DeviceNotFoundException(){
        super();
    }
    
    public DeviceNotFoundException(String msg){
        
        super(msg);
    }

}
