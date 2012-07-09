package net.visualizacao.util;

import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.sun.faces.util.MessageFactory;

public class JSFUtils {
  protected static final Logger logger = Logger.getLogger(JSFUtils.class);

  public static HttpSession getSession() {
    if (getExternalContext() != null) {
      return (HttpSession) getExternalContext().getSession(true);
    }
    return null;
  }

  public static Map<String, Object> getSessionMap() {
    return getExternalContext().getSessionMap();
  }

  public static HttpServletRequest getRequest() {
    return (HttpServletRequest) getExternalContext().getRequest();
  }

  public static HttpServletResponse getResponse() {
    return (HttpServletResponse) getExternalContext().getResponse();
  }

  public static String getContextPath() {
    return getRequest().getContextPath();
  }

  public static String getServletPath() {
    return getRequest().getServletPath();
  }

  public static FacesContext getFacesContext() {
    return FacesContext.getCurrentInstance();
  }

  public static UIViewRoot getViewRoot() {
    return getFacesContext().getViewRoot();
  }

  public static ExternalContext getExternalContext() {
    if (getFacesContext() != null) {
      return getFacesContext().getExternalContext();
    }
    return null;
  }

  public static String getParametro(String nome) {
    return getExternalContext().getRequestParameterMap().get(nome);
  }

  public static void errorMsg(String msg, Object... parametros) {
    try {
      FacesMessage fm = MessageFactory.getMessage(msg,
          FacesMessage.SEVERITY_ERROR, parametros);
      getFacesContext().addMessage(msg, fm);
    } catch (Exception e) {
      logger.error(e);
    }
  }

  public static void infoMsg(String msg, Object... parametros) {
    FacesMessage fm = MessageFactory.getMessage(msg,
        FacesMessage.SEVERITY_INFO, parametros);
    getFacesContext().addMessage("info", fm);
  }
}
