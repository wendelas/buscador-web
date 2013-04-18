package net.indexador.filtro;

import javax.persistence.*;

import net.indexador.entidades.*;
import net.indexador.negocio.*;

public class IndexadorListener extends ConfigureListener {
	/**
	 * Sempre cria o diretorio raiz do usuario
	 */
	public IndexadorListener() {
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("idx-pu");
		EntityManager em = emf.createEntityManager();
		String nome = "diretorio-raiz-do-usuario";
		String dirHome = System.getProperty("user.home");
		FonteDados fonte = FachadaBuscador.getInstancia().buscarFontePeloNome(
				nome);
		if (fonte == null) {
			try {
				fonte = new FonteDados();
				fonte.setDiretorio(dirHome);
				fonte.setNome(nome);
				em.getTransaction().begin();
				em.persist(fonte);
				em.getTransaction().commit();
			} catch (Exception e) {
				em.getTransaction().rollback();
			}
		}
		//
		new ProcessoIndexador(fonte).start();
	}
}
