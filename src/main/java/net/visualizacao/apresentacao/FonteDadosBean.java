package net.visualizacao.apresentacao;

import javax.annotation.PostConstruct;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import net.indexador.entidades.FonteDados;
import net.indexador.negocio.ExcecaoImportador;
import net.indexador.negocio.FachadaBuscador;
import net.visualizacao.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * 
 * @author marcoreis
 * 
 */
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
    fonteDados.setNomeDriver("com.mysql.jdbc.Driver");
    fonteDados.setUsuario("root");
    String query = "SELECT C.NUMCLASSE AS ID, CODCLASSE, DESCRCLASSE, DESCROBSERVACAO, P.NMEPESSOA "
        + "FROM PRO_CLASSE C INNER JOIN PRO_PESSOA P ON C.NUMPESSOAJURIDICA = P.NUMPESSOA";
    fonteDados.setQuery(query);
    fonteDados.setUrl("jdbc:mysql://localhost:3306/proteus");
  }

  public void setFonteDados(FonteDados fonteDados) {
    this.fonteDados = fonteDados;
  }

  public FonteDados getFonteDados() {
    return fonteDados;
  }

  public String salvar() {
    try {
      if (validar()) {
        FachadaBuscador.getInstancia().persistir(getFonteDados());
        infoMsg("mensagem", "Fonte de dados gravada com sucesso");
        return "sucesso";
      }
    } catch (Exception e) {
      errorMsg("erro.generico", e);
    }
    return "";
  }


  private boolean validar() {
    // somente diretorio de arquivos
    if (!StringUtils.vazia(getFonteDados().getDiretorio())) {
      if (!StringUtils.vazia(getFonteDados().getUrl())) {
        String msg = "Não é permitido informar um diretório e uma base de dados para a mesma fonte. "
            + "Por favor, escolha apenas uma opção de fonte.";
        errorMsg("erro.generico", msg);
      }
      // somente base de dados
    } else {
      if (StringUtils.vazia(getFonteDados().getQuery())) {
        errorMsg("erro.generico", "A query é obrigatória");
      }
      if (StringUtils.vazia(getFonteDados().getNome())) {
        errorMsg("erro.generico", "O nome da fonte é um campo obrigatório");
      }
      if (StringUtils.vazia(getFonteDados().getNomeDriver())) {
        errorMsg("erro.generico", "o nome do driver é um campo obrigatório");
      }
      if (StringUtils.vazia(getFonteDados().getUrl())) {
        errorMsg("erro.generico", "A url é obrigatória");
      }
      if (StringUtils.vazia(getFonteDados().getUsuario())) {
        errorMsg("erro.generico", "O usuário é um campo obrigatório");
      }
    }
    return !possuiErros();
  }

  public void setDadosVisualizacao(DataModel dadosVisualizacao) {
    this.dadosVisualizacao = dadosVisualizacao;
  }

  public void carregarDadosVisualizacao() {
    try {
      carregarDadosDiretorio();
      carregarDadosBanco();
    } catch (Exception e) {
      String msg = "Não foi possível carregar os dados para visualização. Erro Java: "
          + e.getMessage();
      errorMsg("mensagem", msg);
      logger.error(e);
    }
  }

  private void carregarDadosDiretorio() {
    if (StringUtils.vazia(getFonteDados().getDiretorio())) {
    }
  }

  private void carregarDadosBanco() throws ExcecaoImportador {
    metaDados = FachadaBuscador.getInstancia().buscarMetaData(getFonteDados());
    dadosVisualizacao = new ListDataModel(metaDados.getColunas());
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
    metaDados = new VOMetaDados();
    fonteDados = FachadaBuscador.getInstancia().buscarFontePeloId(
        getFonteDados().getId());
    return "editar";
  }

  public String novo() {
    inicializa();
    return "editar";
  }

  public String excluir() {
    try {
      FachadaBuscador.getInstancia().excluirFonteDados(getFonteDados().getId());
      infoMsg("mensagem", "Fonte excluida com sucesso");
    } catch (Exception e) {
      errorMsg("erro.generico", e);
    }
    return "";
  }

  public String getMensagemAjudaNomeDriver() {
    String msg = "A aplicação já está com os drivers do MySQL (com.mysql.jdbc.Driver), "
        + "PostgreSQL(???), "
        + "Oracle (???), "
        + "MSSQLSERVER (???). "
        + "Para registrar um novo driver, apenas copie o arquivo.jar em tomcat/metabusca/web/lib.";
    return msg;
  }

  public String getMensagemQuery() {
    String msg = "A query pode ser de uma ou várias tabelas, contendo todos os tipos de dados, inclusive lob. "
        + "O campo lob pode conter texto ou documentos (PDF, DOC, DOCX, XLS, PPT). "
        + "A query deve conter uma coluna 'ID'. "
        + "Datas devem ser utilizadas no formato americano (yyyy-MM-dd HH:mm:ss). ";
    return msg;
  }

  public String getMensagemNomeFonte() {
    String msg = "Utilize nomes que identifiquem facilmente o tipo de informação. "
        + "Exemplo: diretorio-documentos-local, tabela-dados-usuario, tabela-vendas, tabela-historico-compras. ";
    return msg;
  }

  public String getMensagemFonteDados() {
    String msg = "Uma fonte de dados pode ser uma query no banco de dados ou um diretório local no servidor. "
        + "<li>Para indexar um diretório, preencha apenas o campo 'Diretório de Documentos' e deixe os demais campos em branco.</li>"
        + "<li>Para indexar uma query, preencha os dados de conexão e depois o botão 'Visualizar' para validar a conexão/query. "
        + "<b>É essencial que a query contenha o campo 'ID'</b>.</li>"
        + "<li>Não é possível conter as duas opções pois a busca ainda não suporta fontes diferentes.</li>";
    return msg;
  }
}
