package net.visualizacao.apresentacao;

import javax.annotation.*;
import javax.faces.model.*;

import net.indexador.entidades.*;
import net.indexador.negocio.*;

import org.ajax4jsf.model.*;
import org.apache.log4j.*;

/**
 * 
 * @author marcoreis
 *
 */
@KeepAlive
public class FonteDadosBean extends BaseBean {
  private static Logger logger = Logger.getLogger(FonteDadosBean.class);
  private static final long serialVersionUID = -7768860623840391492L;
  private FonteDados fonteDados;
  private VOMetaDados metaDados;
  private DataModel dadosVisualizacao;

  @PostConstruct
  public void inicializa() {
    fonteDados = new FonteDados();
    fonteDados.setNome("tabela-???");
    //    fonteDados.setNomeDriver("com.mysql.jdbc.Driver");
    //    fonteDados.setPassword("root");
    //    fonteDados.setUser("root");
    //    fonteDados.setQuery("SELECT * FROM JBPM_LOG");
    //    fonteDados.setUrl("jdbc:mysql://localhost:3306/sgm");
    fonteDados.setNomeDriver("com.intersys.jdbc.CacheDriver");
    fonteDados.setUsuario("sistjweb");
//    fonteDados.setPassword("N974T6gR");
    fonteDados.setQuery("SELECT * FROM CACHE_SISTJ_ATO.TEXTODOATO");
    fonteDados.setUrl("jdbc:Cache://tjdf78:1972/sistjweb");
  }

  public void setFonteDados(FonteDados fonteDados) {
    this.fonteDados = fonteDados;
  }

  public FonteDados getFonteDados() {
    return fonteDados;
  }

  public String salvar() {
    try {
      FachadaBuscador.getInstancia().persistir(getFonteDados());
      infoMsg("mensagem", "Fonte de dados gravada com sucesso");
      return "sucesso";
    } catch (Exception e) {
      logger.error(e);
      errorMsg("erro.generico", e);
      return null;
    }
  }

  public void setDadosVisualizacao(DataModel dadosVisualizacao) {
    this.dadosVisualizacao = dadosVisualizacao;
  }

  public void carregarDadosVisualizacao() {
    try {
      metaDados = FachadaBuscador.getInstancia()
          .buscarMetaData(getFonteDados());
      dadosVisualizacao = new ListDataModel(metaDados.getColunas());
    } catch (Exception e) {
      errorMsg("mensagem", e);
      logger.error(e);
      ;
    }
  }

  public DataModel getDadosVisualizacao() {
    return dadosVisualizacao;
  }

  public void setMetaDados(VOMetaDados metaDados) {
    this.metaDados = metaDados;
  }

  public VOMetaDados getMetaDados() {
    return metaDados;
  }

  public String editar() {
    fonteDados = FachadaBuscador.getInstancia().buscarFontePeloId(
        getFonteDados().getId());
    return "editar";
  }

  public String novo() {
    inicializa();
    return "editar";
  }

  public String excluir() {
    FachadaBuscador.getInstancia().excluirFonteDados(getFonteDados().getId());
    return "";
  }
}
