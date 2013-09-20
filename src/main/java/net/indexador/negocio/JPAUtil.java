package net.indexador.negocio;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {
	private EntityManagerFactory emf;
	private EntityManager entityManager;
	private static JPAUtil instance;

	private JPAUtil() {
		try {
			emf = Persistence.createEntityManagerFactory("idx-pu");
			entityManager = emf.createEntityManager();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static JPAUtil getInstance() {
		if (instance == null) {
			instance = new JPAUtil();
		}
		return instance;
	}

	// FIXME Melhorar esse bloco
	public EntityManager getEntityManager() {
		return entityManager;
	}

}
