package net.indexador.negocio;

import java.io.*;
import java.sql.*;
import java.sql.Connection;
import java.util.*;

import javax.persistence.*;

import net.indexador.entidades.*;
import net.utilitarios.*;
import net.visualizacao.apresentacao.*;

import org.apache.log4j.*;
import org.apache.tika.*;
import org.jsoup.*;
import org.jsoup.nodes.*;

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
      logger.error(e);
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public Collection<FonteDados> buscarFontes() {
    EntityManager em = emf.createEntityManager();
    List<FonteDados> lista = em.createQuery(
        "select f from FonteDados f order by f.nome").getResultList();
    return lista;
  }

  /**
   * Recupera os dados/metadados do banco para gerar o indice
   * @param idFonteDados
   * @throws ExcecaoIndexador
   */
  //FIXME melhorar esse codigo
  public void indexar(int idFonteDados) throws ExcecaoIndexador {
    EntityManager em = emf.createEntityManager();
    FonteDados fonteDados = em.find(FonteDados.class, idFonteDados);
    Indexador idx = null;
    int qtdeItensIndexados = 0;
    try {
      long inicio = System.currentTimeMillis();
      new File(fonteDados.getDiretorioIndice() + "/write.lock").delete();
      idx = new Indexador(fonteDados.getDiretorioIndice());
      //Indexa arquivos no disco
      if (!StringUtils.vazia(fonteDados.getDiretorio())) {
        idx.indexaArquivosDoDiretorio(new File(fonteDados.getDiretorio()));
        logger.info("Total de arquivos indexados: "
            + idx.getQuantidadeArquivosIndexados());
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
            Object valor = query.getObject(i);
            String texto = "[ColunaVazia]";
            //
            if (valor instanceof InputStream) {
              try {
                texto = UtilExtrator.fromDoc((InputStream) query.getObject(i));
              } catch (Exception e) {
                //Nao eh doc/xls/pdf/etc...
              }
            } else if (valor != null) {
              texto = valor.toString();
            }
            //
            try {
              Document doc = Jsoup.parse(texto);
              texto = doc.text();
            } catch (Exception e1) {
              //Nao eh arquivo html
            }
            //
            if (!StringUtils.vazia(texto)) {
              mapa.put(coluna, texto);
            }
          } catch (Exception e) {
            logger.error(e);
          }
        }
        if (idx.indexar(mapa)) {
          qtdeItensIndexados++;
        }
        if (qtdeItensIndexados > 0 && qtdeItensIndexados % 100 == 0) {
          logger.info("Estatistica parcial: " + qtdeItensIndexados
              + " itens indexados.");
        }
      }
      long fim = System.currentTimeMillis();
      String msg = "Tempo para indexar: " + ((fim - inicio) / 1000)
          + " segundos.";
      logger.info(msg);
      logger.info("Quantidade itens indexados: " + qtdeItensIndexados);
    } catch (Exception e) {
      throw new ExcecaoIndexador(e);
    } finally {
      try {
        idx.fecha();
      } catch (Exception e) {
      }
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
      if (query.next()) {
        Collection<String> valores = new ArrayList<String>();
        for (int i = 1; i <= rsMetaDados.getColumnCount(); i++) {
          try {
            String valor = query.getString(i);
            if (StringUtils.vazia(valor)) continue;
            if (valor.length() > 100) {
              valor = valor.substring(0, 99);
            }
            valores.add(valor);
          } catch (Exception e) {
            logger.error(e);
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
      logger.error(e);
      throw new RuntimeException(e);
    }
  }

  public Collection<Indice> buscarIndices() {
    EntityManager em = emf.createEntityManager();
    List<Indice> lista = em.createQuery("select i from Indice i")
        .getResultList();
    return lista;
  }
}
