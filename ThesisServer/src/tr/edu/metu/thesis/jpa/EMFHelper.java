package tr.edu.metu.thesis.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EMFHelper {

	private static final String PERSISTENCE_NAME = "ThesisServer";
	private static EntityManagerFactory _emf     = null; 
	
	private EMFHelper(){
		
		_emf = Persistence.createEntityManagerFactory(PERSISTENCE_NAME);
	}
	
	public static EntityManagerFactory getEMF(){
		
		if(_emf == null)
			new EMFHelper();
			
		return _emf;
	}
	
	public static void close(){
		
		if(_emf != null)
			_emf.close();
	}
}
