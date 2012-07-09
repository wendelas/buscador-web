package net.visualizacao.apresentacao;

import java.io.*;
import java.util.*;

import net.indexador.entidades.*;
import net.indexador.negocio.*;
import net.utilitarios.*;

import org.ajax4jsf.model.*;
import org.apache.log4j.*;
import org.apache.lucene.index.*;

@KeepAlive
public class FrequenciaDosTermosBean extends BaseBean {
  private static final long serialVersionUID = 6837431655685273498L;
  protected static final Logger logger = Logger
      .getLogger(FrequenciaDosTermosBean.class);
  private Integer limite = 1;
  private Integer idFonteDados;
  private FonteDados fonte;
  private VOMetaDados metaDados;
  private List<FrequenciaDoTermo> termos;

  public void setLimite(Integer limite) {
    this.limite = limite;
  }

  public Integer getLimite() {
    return limite;
  }

  public Collection<String> getCamposSelecionados() {
    try {
      metaDados = FachadaBuscador.getInstancia().buscarMetaData(fonte);
      return metaDados.getColunas();
    } catch (Exception e) {
      //Fonte de dados eh um diretorio
    }
    Collection<String> listaCamposArquivo = new ArrayList<String>();
    listaCamposArquivo.add("Texto");
    return listaCamposArquivo;
  }

  public void carregarDados() {
    fonte = FachadaBuscador.getInstancia().buscarFontePeloId(getIdFonteDados());
    UtilFrequenciaDosTermos util = new UtilFrequenciaDosTermos(
        fonte.getDiretorioIndice());
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

  public void setTermos(List<FrequenciaDoTermo> termList) {
    this.termos = termList;
  }

  public List<FrequenciaDoTermo> getTermos() {
    return termos;
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

  public void setFonte(FonteDados fonte) {
    this.fonte = fonte;
  }

  public FonteDados getFonte() {
    return fonte;
  }

  public void setMetaDados(VOMetaDados metaDados) {
    this.metaDados = metaDados;
  }

  public VOMetaDados getMetaDados() {
    return metaDados;
  }

  public void setIdFonteDados(Integer idFonteDados) {
    this.idFonteDados = idFonteDados;
  }

  public Integer getIdFonteDados() {
    return idFonteDados;
  }
}
