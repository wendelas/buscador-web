package net.visualizacao.apresentacao;

import java.io.*;
import java.util.*;

import javax.faces.component.*;
import javax.faces.context.*;
import javax.servlet.http.*;

import net.visualizacao.util.*;

import org.apache.log4j.*;
import org.richfaces.component.html.*;

public class BaseBean implements Serializable {
  private static final long serialVersionUID = -5895396595360064610L;
  protected static final Logger logger = Logger.getLogger(BaseBean.class);
  protected static final String MAPEAMENTO_SUCESSO = "sucesso";
  protected static final String MSG_SUCESSO = "mensagem.sucesso";
  protected static final String MSG_ERRO = "erro.generico";
  private HtmlToolBar toolBar;
  private String[] camposSelecionados;

  protected final FacesContext getFacesContext() {
    return JSFUtils.getFacesContext();
  }

  protected final UIViewRoot getViewRoot() {
    return JSFUtils.getViewRoot();
  }

  protected final ExternalContext getExternalContext() {
    return JSFUtils.getExternalContext();
  }

  protected final String getParametro(String nome) {
    return JSFUtils.getParametro(nome);
  }

  protected final void invalidaSessao() {
    HttpSession session = getSession();
    if (!session.isNew()) {
      session.invalidate();
    }
  }

  protected final HttpSession getSession() {
    return JSFUtils.getSession();
  }

  protected final HttpServletRequest getRequest() {
    return JSFUtils.getRequest();
  }

  protected final HttpServletResponse getResponse() {
    return JSFUtils.getResponse();
  }

  protected final String getContextPath() {
    return JSFUtils.getContextPath();
  }

  protected final boolean possuiErros() {
    return getFacesContext().getMessages("erro").hasNext();
  }

  protected final String getServletPath() {
    return JSFUtils.getServletPath();
  }

  protected final void redirecionaSemEnderecoBase(String url) {
    try {
      getExternalContext().redirect(url);
    } catch (IOException e) {
      logger.error("Erro ao redirecionar ", e);
      throw new IllegalStateException("Erro ao redirecionar ", e);
    }
  }

  protected void inicializa() {
  }

  public HtmlToolBar getToolBar() {
    //FIXME Gerar codigo dinamico
    toolBar = new HtmlToolBar();
    HtmlDropDownMenu menu = new HtmlDropDownMenu();
    menu.setValue("Indexação");
    //
    HtmlMenuItem menuItem = null;
    //
    menuItem = new HtmlMenuItem();
    menuItem.setValue("Busca Livre");
    menuItem.setOnclick("document.location.href='BuscaLivre.faces'");
    menuItem.setSubmitMode("none");
    menu.getChildren().add(menuItem);
    //
    menuItem = new HtmlMenuItem();
    menuItem.setValue("Frequência dos Termos");
    menuItem.setOnclick("document.location.href='FrequenciaDosTermos.faces'");
    menuItem.setSubmitMode("none");
    menu.getChildren().add(menuItem);
    //
    menuItem = new HtmlMenuItem();
    menuItem.setValue("Similaridade");
    //    menuItem.setOnclick("document.location.href='AcordaosSimilares.faces'");
    menuItem.setSubmitMode("none");
    menu.getChildren().add(menuItem);
    //
    toolBar.getChildren().add(menu);
    //
    HtmlDropDownMenu utilitarios = new HtmlDropDownMenu();
    utilitarios.setValue("Configuração");
    //
    HtmlMenuItem menuReindexar = null;
    //
    menuReindexar = new HtmlMenuItem();
    menuReindexar.setValue("Utilitário");
    menuReindexar.setOnclick("document.location.href='Utilitario.faces'");
    menuReindexar.setSubmitMode("none");
    utilitarios.getChildren().add(menuReindexar);
    //
//    menuReindexar = new HtmlMenuItem();
//    menuReindexar.setValue("Fonte de Dados");
//    menuReindexar.setOnclick("document.location.href='FonteDados.faces'");
//    menuReindexar.setSubmitMode("none");
    utilitarios.getChildren().add(menuReindexar);
    //
    toolBar.getChildren().add(utilitarios);
    //
    return toolBar;
  }

  public void setToolBar(HtmlToolBar toolBar) {
    this.toolBar = toolBar;
  }

  protected final void errorMsg(String msg, Object... parametros) {
    JSFUtils.errorMsg(msg, parametros);
  }

  protected final void infoMsg(String msg, Object... parametros) {
    JSFUtils.infoMsg(msg, parametros);
  }

  public Collection<String> getCamposDisponiveis() {
    return null;
  }

  public void setCamposSelecionados(String[] camposSelecionados) {
    this.camposSelecionados = camposSelecionados;
  }

  public String[] getCamposSelecionados() {
    return camposSelecionados;
  }
}
