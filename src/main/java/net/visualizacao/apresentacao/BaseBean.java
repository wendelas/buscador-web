package net.visualizacao.apresentacao;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

@Model
public class BaseBean implements Serializable {
  private static final long serialVersionUID = -5895396595360064610L;
  protected static final Logger logger = Logger.getLogger(BaseBean.class);
  private FacesContext facesContext;
  private MenuModel menu;

  public MenuModel getMenu() {
    return menu;
  }

  @PostConstruct
  public void inicializa() {
    menu = new DefaultMenuModel();
    //First submenu
    DefaultSubMenu firstSubmenu = new DefaultSubMenu("Dynamic Submenu");
    DefaultMenuItem item = new DefaultMenuItem("External");
    item.setUrl("http://www.primefaces.org");
    item.setIcon("ui-icon-home");
    firstSubmenu.addElement(item);
    menu.addElement(firstSubmenu);
    //Second submenu
    DefaultSubMenu secondSubmenu = new DefaultSubMenu("Dynamic Actions");
    item = new DefaultMenuItem("Save");
    item.setIcon("ui-icon-disk");
    item.setCommand("#{menuBean.save}");
    item.setUpdate("mensagens");
    secondSubmenu.addElement(item);
    item = new DefaultMenuItem("Delete");
    item.setIcon("ui-icon-close");
    item.setCommand("#{menuBean.delete}");
    item.setAjax(false);
    secondSubmenu.addElement(item);
    item = new DefaultMenuItem("Redirect");
    item.setIcon("ui-icon-search");
    item.setCommand("#{menuBean.redirect}");
    secondSubmenu.addElement(item);
    menu.addElement(secondSubmenu);
  }

  protected void infoMsg(String message, String detail) {
    facesContext.addMessage(message, new FacesMessage(detail));
  }

  protected void errorMsg(String string, String string2) {
    // TODO Auto-generated method stub
  }

  protected void errorMsg(String string, Throwable t) {
    // TODO Auto-generated method stub
  }

  public boolean possuiErros() {
    return false;
  }
}
