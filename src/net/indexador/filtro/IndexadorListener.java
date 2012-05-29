package net.indexador.filtro;

import javax.persistence.*;

import com.sun.faces.config.*;

public class IndexadorListener extends ConfigureListener {
  public IndexadorListener() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("idx-pu");
  }
}
