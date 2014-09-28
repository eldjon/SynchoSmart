package tr.edu.metu.thesis.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class Test {

	public static void main(String[] args) {
		
		EntityManagerFactory emf = EMFHelper.getEMF();
		EntityManager manager    = emf.createEntityManager();
		Device dev = new Device();
		dev.setBrand("brand 1");
		dev.setImei("imei");
		dev.setModel("model");
		try {
			
			manager.getTransaction().begin();
			manager.persist(dev);
			manager.getTransaction().commit();
		} catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			manager.close();
		}
	}

}
