package net.visualizacao.apresentacao;

import java.io.*;
import java.util.*;

import net.utilitarios.*;

import org.apache.log4j.*;
import org.apache.lucene.index.*;

//@KeepAlive
public class FrequenciaDosTermosBean extends BaseBean {
  private static final long serialVersionUID = 6837431655685273498L;
  protected static final Logger logger = Logger
      .getLogger(FrequenciaDosTermosBean.class);
  private Integer limite = 1;
  private Collection<FrequenciaDoTermo> termos;

  public void setLimite(Integer limite) {
    this.limite = limite;
  }

  public Integer getLimite() {
    return limite;
  }

  public void setTermos(Collection<FrequenciaDoTermo> termos) {
    this.termos = termos;
  }

  public Collection<FrequenciaDoTermo> getTermos() {
    return termos;
  }

  @Override
  public Collection<String> getCamposSelecionados() {
    Collection<String> lista = new ArrayList<String>();
    lista.add("TermoIndexacao");
    lista.add("TermoEmenta");
    return lista;
  }

  public void carregarDados() {
    UtilFrequenciaDosTermos util = new UtilFrequenciaDosTermos("");
    try {
      List<FrequenciaDoTermo> termList = util.getFrequencia(getLimite(),
          getCamposSelecionados());
      setTermos(termList);
    } catch (IndexNotFoundException e) {
      errorMsg(Constantes.ERRO_INDEX_NOT_FOUND, e.getLocalizedMessage());
      logger.error(e);
    } catch (IOException e) {
      errorMsg(Constantes.ERRO_CARREGAR_FREQUENCIA_TERMOS,
          e.getLocalizedMessage());
      logger.error(e);
    } catch (Exception e) {
      errorMsg(Constantes.ERRO_BUSCA_LUCENE, e.getLocalizedMessage());
      logger.error(e);
    }
  }

  public Integer getTotalAcordaosIndexados() {
    try {
      return new UtilBusca().getTotalDocumentosIndexados();
    } catch (IOException e) {
      errorMsg("erro", e);
      return 0;
    }
  }

  public Integer getTotalTermosIndexados() {
    try {
      return new UtilBusca("").getTotalDocumentosIndexados();
    } catch (IOException e) {
      errorMsg("erro", e);
      return 0;
    }
  }
}
