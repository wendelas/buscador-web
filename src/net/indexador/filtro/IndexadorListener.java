package net.indexador.filtro;

import javax.persistence.*;

import net.indexador.entidades.*;

import com.sun.faces.config.*;

public class IndexadorListener extends ConfigureListener {
  /**
   * Sempre cria o diretorio raiz do usuario
   */
  public IndexadorListener() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("idx-pu");
    EntityManager em = emf.createEntityManager();
    try {
      FonteDados fonte = new FonteDados();
      fonte.setDiretorio(System.getProperty("user.home"));
      fonte.setNome("diretorio-raiz-do-usuario");
      em.getTransaction().begin();
      em.persist(fonte);
      em.getTransaction().commit();
    } catch (Exception e) {
      em.getTransaction().rollback();
    }
  }
}
