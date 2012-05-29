package net.indexador.negocio;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.persistence.*;

import net.indexador.entidades.*;
import net.utilitarios.*;
import net.visualizacao.apresentacao.*;

import org.apache.log4j.*;

/**
 * 
 * @author marcoreis
 *
 */
public class FachadaBuscador {
  private static final Logger logger = Logger.getLogger(FachadaBuscador.class);
  private EntityManagerFactory emf = Persistence
      .createEntityManagerFactory("idx-pu");
  private static FachadaBuscador instancia;

  public FachadaBuscador() {
  }

  public static synchronized FachadaBuscador getInstancia() {
    if (instancia == null) {
      instancia = new FachadaBuscador();
    }
    return instancia;
  }

  public void persistir(FonteDados fonteDados) {
    EntityManager em = emf.createEntityManager();
    try {
      em.getTransaction().begin();
      FonteDados fd = fonteDados;
      fd = em.merge(fonteDados);
      em.persist(fd);
      em.getTransaction().commit();
    } catch (Exception e) {
      em.getTransaction().rollback();
      throw new RuntimeException(e);
    }
  }

  public Collection<FonteDados> buscarFontes() {
    EntityManager em = emf.createEntityManager();
    List<FonteDados> lista = em.createQuery("select f from FonteDados f")
        .getResultList();
    return lista;
  }

  public void indexar(int idFonteDados) throws ExcecaoIndexador {
    EntityManager em = emf.createEntityManager();
    FonteDados fonteDados = em.find(FonteDados.class, idFonteDados);
    Indexador idx = null;
    try {
      long inicio = System.currentTimeMillis();
      new File(fonteDados.getDiretorioIndice() + "/write.lock").delete();
      idx = new Indexador(fonteDados.getDiretorioIndice());
      //Indexa arquivos no disco
      if (!StringUtils.vazia(fonteDados.getDiretorio())) {
        idx.indexaArquivosDoDiretorio(new File(fonteDados.getDiretorio()));
      }
      //Indexa banco de dados
      Connection con = null;
      Statement stmt = null;
      ResultSet query = null;
      Class.forName(fonteDados.getNomeDriver());
      con = DriverManager.getConnection(fonteDados.getUrl(),
          fonteDados.getUsuario(), fonteDados.getPassword());
      stmt = con.createStatement();
      query = stmt.executeQuery(fonteDados.getQuery());
      ResultSetMetaData rsMetaDados = query.getMetaData();
      while (query.next()) {
        Map<String, String> mapa = new HashMap<String, String>();
        for (int i = 1; i <= rsMetaDados.getColumnCount(); i++) {
          String coluna = "";
          try {
            coluna = rsMetaDados.getColumnName(i);
            String tipoDado = rsMetaDados.getColumnTypeName(i);
            Object valor = query.getObject(i);
            String texto = "";
            if ("LONGVARBINARY".equalsIgnoreCase(tipoDado)) {
              if (valor != null) {
                texto = valor.toString();
                //TODO
                //                try {
                //                  texto = UtilExtrator.fromDoc((InputStream) query.getObject(i));
                //                } catch (Exception e) {
                //                  //Tenta com DOC
                //                  try {
                //                    texto = UtilExtrator.fromPDF((InputStream) query.getObject(i));
                //                  } catch (Exception e1) {
                //                    //Nao eh nem DOC nem PDF
                //                  }
                //                }
              }
            } else {
              texto = valor.toString();
            }
            mapa.put(coluna, texto);
          } catch (Exception e) {
            mapa.put(coluna, "[Erro ao recuperar metadados - " + e + "]");
          }
        }
        idx.indexar(mapa);
      }
      long fim = System.currentTimeMillis();
      String msg = "Tempo para indexar: " + ((fim - inicio) / 1000)
          + " segundos.";
      logger.info(msg);
    } catch (Exception e) {
      throw new ExcecaoIndexador(e);
    } finally {
      idx.fecha();
    }
  }

  public VOMetaDados buscarMetaData(FonteDados fonteDados)
      throws ExcecaoImportador {
    Connection con = null;
    Statement stmt = null;
    ResultSet query = null;
    VOMetaDados metaDados = new VOMetaDados();
    try {
      Class.forName(fonteDados.getNomeDriver());
      con = DriverManager.getConnection(fonteDados.getUrl(),
          fonteDados.getUsuario(), fonteDados.getPassword());
      stmt = con.createStatement();
      query = stmt.executeQuery(fonteDados.getQuery());
      ResultSetMetaData rsMetaDados = query.getMetaData();
      //
      List<String> colunas = new ArrayList<String>();
      for (int i = 1; i <= rsMetaDados.getColumnCount(); i++) {
        colunas.add(rsMetaDados.getColumnName(i));
      }
      metaDados.setColunas(colunas);
      //
      while (query.next()) {
        Collection<String> valores = new ArrayList<String>();
        for (int i = 1; i <= rsMetaDados.getColumnCount(); i++) {
          try {
            String valor = query.getString(i);
            if (valor.length() > 100) {
              valor = valor.substring(0, 99);
            }
            valores.add(valor);
          } catch (Exception e) {
            valores.add("[Erro Metadados]");
          }
        }
        metaDados.addTupla(valores.toArray());
      }
      //
      return metaDados;
      //
    } catch (Exception e) {
      throw new ExcecaoImportador(e);
    } finally {
      try {
        con.close();
        stmt.close();
        query.close();
      } catch (SQLException e) {
      }
    }
  }

  public FonteDados buscarFontePeloId(Integer id) {
    EntityManager em = emf.createEntityManager();
    return em.find(FonteDados.class, id);
  }

  public void excluirFonteDados(Integer id) {
    try {
      EntityManager em = emf.createEntityManager();
      FonteDados fonte = em.find(FonteDados.class, id);
      em.getTransaction().begin();
      em.remove(fonte);
      em.getTransaction().commit();
    } catch (Exception e) {
    }
  }

  public Collection<Indice> buscarIndices() {
    EntityManager em = emf.createEntityManager();
    List<Indice> lista = em.createQuery("select i from Indice i")
        .getResultList();
    return lista;
  }
}
