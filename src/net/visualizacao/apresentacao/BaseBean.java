package net.visualizacao.apresentacao;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.visualizacao.util.JSFUtils;

import org.apache.log4j.Logger;

public class BaseBean implements Serializable {
  private static final long serialVersionUID = -5895396595360064610L;
  protected static final Logger logger = Logger.getLogger(BaseBean.class);
  protected static final String MAPEAMENTO_SUCESSO = "sucesso";
  protected static final String MSG_SUCESSO = "mensagem.sucesso";
  protected static final String MSG_ERRO = "erro.generico";
  //  private HtmlToolBar toolBar;
  private Collection<String> camposSelecionados;

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

  protected final void errorMsg(String msg, Object... parametros) {
    JSFUtils.errorMsg(msg, parametros);
  }

  protected final void infoMsg(String msg, Object... parametros) {
    JSFUtils.infoMsg(msg, parametros);
  }

  public Collection<String> getCamposDisponiveis() {
    return null;
  }

  public void setCamposSelecionados(Collection<String> camposSelecionados) {
    this.camposSelecionados = camposSelecionados;
  }

  public Collection<String> getCamposSelecionados() {
    return camposSelecionados;
  }
}
