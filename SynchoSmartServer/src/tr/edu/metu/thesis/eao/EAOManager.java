package tr.edu.metu.thesis.eao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import tr.edu.metu.thesis.jpa.Device;
import tr.edu.metu.thesis.jpa.EMFHelper;
import tr.edu.metu.thesis.jpa.SurveyInstance;

public abstract class EAOManager {

	public static void close(){
		
		EMFHelper.close();
	}
	
	public static <T> T findById(int id, Class<T> clazz){
		
		EntityManager manager  = EMFHelper.getEMF().createEntityManager();
		manager.getTransaction().begin(); 
		T value = manager.find(clazz, id);
		manager.getTransaction().commit();
		manager.close();
		return value;
	}
	
	public static <T> boolean persist(T object)	{

		EntityManager manager  = EMFHelper.getEMF().createEntityManager();
		manager.getTransaction().begin(); 
		manager.persist(object);
		if (manager.getTransaction().isActive()== false) 
			return false;
		manager.getTransaction().commit();
		manager.close();
		return true;
	}
	
	public static <T> void refresh(T entity){
		
		EntityManager manager  = EMFHelper.getEMF().createEntityManager();
		manager.getTransaction().begin();
		entity = manager.merge(entity);
		manager.refresh(entity);
		manager.getTransaction().commit();
		manager.close();	
		return;
	}
	
	public static <T> boolean remove(T object)	{
		
		EntityManager manager  = EMFHelper.getEMF().createEntityManager();
		manager.getTransaction().begin();
		object                 = manager.merge(object);
		manager.remove(object);
		manager.getTransaction().commit();
		manager.close();
		return true;
	}
	
	public static <T> void update(T toUpdate){
		
		EntityManager manager  = EMFHelper.getEMF().createEntityManager();
		manager.getTransaction().begin();
		toUpdate               = manager.merge(toUpdate);
		manager.flush();
		manager.getTransaction().commit();
		manager.close();		
		return;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> findAll(Class<T> clazz){
		
		EntityManager manager  = EMFHelper.getEMF().createEntityManager();
		manager.getTransaction().begin();
		String name            = clazz.getName();
		String simpleClassName = name.substring(name.lastIndexOf('.')+1);		
		String query           = "SELECT c FROM " +simpleClassName + " c ";
		Query findQuery        = manager.createQuery(query);
		List<T> returnedList   = findQuery.getResultList();
		manager.getTransaction().commit();
		manager.close();
	
		return returnedList;	
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> findByField(Class<T> clazz, String fieldName, Object fieldValue){
		
		EntityManager manager  = EMFHelper.getEMF().createEntityManager();
		String name            = clazz.getName();
		String simpleClassName = name.substring(name.lastIndexOf('.')+1);		
		String simpleFieldName = fieldName;
		if(fieldName.contains("."))
			simpleFieldName = fieldName.substring(fieldName.lastIndexOf('.')+1);
		
		String query           = "SELECT c FROM " + simpleClassName + " c " + "WHERE c." 
												  + fieldName + "=:" + simpleFieldName;
		manager.getTransaction().begin();
		Query findQuery        = manager.createQuery(query);
		findQuery.setParameter(simpleFieldName, fieldValue);
		List<T> returnedList   = findQuery.getResultList();
		manager.getTransaction().commit();
		manager.close();
	
		return returnedList;	

	}
	
	public static class EAOSurveyInstance {
		
		@SuppressWarnings("unchecked")
		public static List<SurveyInstance> findByNonStatus(final String status){
			
			EntityManager manager  = EMFHelper.getEMF().createEntityManager();
			manager.getTransaction().begin();
			Query findQuery        = manager.createNamedQuery("SurveyInstance.findAllByNonStatus");
			findQuery.setParameter("status", status);
			List<SurveyInstance> result = findQuery.getResultList();
			manager.getTransaction().commit();
			manager.close();
			return result;
		}
		
		@SuppressWarnings("unchecked")
		public static List<SurveyInstance> findByStatus(final String status){
			
			EntityManager manager  = EMFHelper.getEMF().createEntityManager();
			manager.getTransaction().begin();
			Query findQuery        = manager.createNamedQuery("SurveyInstance.findAllByStatus");
			findQuery.setParameter("status", status);
			List<SurveyInstance> result = findQuery.getResultList();
			manager.getTransaction().commit();
			manager.close();
			return result;
		}
		
		@SuppressWarnings("unchecked")
		public static List<SurveyInstance> findActiveSurvey(){
			
			EntityManager manager  = EMFHelper.getEMF().createEntityManager();
			manager.getTransaction().begin();
			Query findQuery        = manager.createNamedQuery("SurveyInstance.findActiveSurvey");
			List<SurveyInstance> result = findQuery.getResultList();
			manager.getTransaction().commit();
			manager.close();
			return result;
		}
		
		public static void addDevice(Device device){
			
		}
	}
}
