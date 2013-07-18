package net.visualizacao.util;

import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

public class JSFUtils {
  protected static final Logger logger = Logger.getLogger(JSFUtils.class);

  public static Map<String, Object> getSessionMap() {
    return getExternalContext().getSessionMap();
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
}
