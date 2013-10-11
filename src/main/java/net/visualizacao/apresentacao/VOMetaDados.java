package net.visualizacao.apresentacao;

import java.util.*;

/**
 * @author marcoreis
 *
 */
public class VOMetaDados {
  private List<String> colunas;
  private Collection<Object[]> tuplas;

  public void setColunas(List<String> colunas) {
    this.colunas = colunas;
  }

  public List<String> getColunas() {
    return colunas;
  }

  public Collection<Object[]> getTuplas() {
    if (tuplas == null) {
      tuplas = new ArrayList<Object[]>();
    }
    return tuplas;
  }

  public void addTupla(Object[] objects) {
    getTuplas().add(objects);
  }
}
