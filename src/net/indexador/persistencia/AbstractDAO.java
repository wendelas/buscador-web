package net.indexador.persistencia;

import javax.persistence.*;
import javax.persistence.Query;

import net.utilitarios.*;

import org.apache.log4j.*;
import org.hibernate.*;

public abstract class AbstractDAO {
  protected static final Logger logger = Logger.getLogger(AbstractDAO.class);
  protected Session session;

  protected AbstractDAO() {
  }

  public void setSession(Session s) {
    this.session = s;
  }

  protected EntityManager getSession() {
    return null;
  }

  public void flush() {
    getSession().flush();
  }

  public void clear() {
    getSession().clear();
  }

  /**
   * getCondicaoLike
   * 
   * @param condicao
   * @return
   * @since
   */
  protected String getCondicaoLike(String condicao) {
    return StringUtils.getCondicaoLike(condicao.toLowerCase());
  }

  protected boolean existe(Query query) {
    return existeMaisDe(query, 0);
  }

  protected boolean existeMaisDe(Query query, int quantidade) {
    return false;
    //		return ((Number) query.uniqueResult()).intValue() > quantidade;
  }

  protected int conta(Query query) {
    return 0;
    //		return ((Number) query.uniqueResult()).intValue();
  }
}
