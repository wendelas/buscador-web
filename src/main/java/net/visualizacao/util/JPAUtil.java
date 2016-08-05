package net.visualizacao.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {
	private EntityManagerFactory emf;
	private static JPAUtil instance;

	private JPAUtil() {
		emf = Persistence.createEntityManagerFactory("idx-pu");
	}

	public static JPAUtil getInstance() {
		if (instance == null) {
			instance = new JPAUtil();
		}
		return instance;
	}

	public EntityManager getEntityManager() {
		return emf.createEntityManager();
	}
}
