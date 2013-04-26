package net.indexador.importacao.teste;

import javax.persistence.*;

import net.indexador.entidades.*;

public class Teste {
  public static void main(String[] args) {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("idx-pu");
    EntityManager em = emf.createEntityManager();
    Indice indice = em.find(Indice.class, 1);
    System.out.println(indice);
  }
}
