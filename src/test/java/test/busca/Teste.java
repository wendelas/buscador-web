package test.busca;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.indexador.entidades.Indice;

public class Teste {
	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("idx-pu");
		EntityManager em = emf.createEntityManager();
		Indice indice = em.find(Indice.class, 1);
		System.out.println(indice);
	}
}
