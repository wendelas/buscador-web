package net.indexador.negocio;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.indexador.entidades.FonteDados;
import net.indexador.entidades.Indice;
import net.indexador.entidades.MetaDado;
import net.visualizacao.apresentacao.VOMetaDados;
import net.visualizacao.util.ExcecaoIndexador;
import net.visualizacao.util.Indexador;
import net.visualizacao.util.StringUtils;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 
 * @author marcoreis
 *
 */
@SuppressWarnings("unchecked")
public class FachadaBuscador {
  private static final Logger logger = Logger.getLogger(FachadaBuscador.class);
  private EntityManagerFactory emf = Persistence
      .createEntityManagerFactory("idx-pu");
  private static FachadaBuscador instancia;
  private Tika tika;

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
   * Atencao: no mysql o getColumnLabel retorna valores em uppercase, por isso os campos indexados estao assim.
   * @param idFonteDados
   * @return 
   * @throws ExcecaoIndexador
   */
  //FIXME melhorar esse codigo
  public int indexar(int idFonteDados) throws ExcecaoIndexador {
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
      removeMetadados(fonteDados);
      ResultSetMetaData rsMetaDados = query.getMetaData();
      for (int i = 1; i <= rsMetaDados.getColumnCount(); i++) {
        String coluna = rsMetaDados.getColumnLabel(i).toUpperCase();
        persistir(fonteDados, coluna);
      }
      while (query.next()) {
        Map<String, String> mapa = new HashMap<String, String>();
        for (int i = 1; i <= rsMetaDados.getColumnCount(); i++) {
          String coluna = "";
          try {
            coluna = rsMetaDados.getColumnLabel(i).toUpperCase();
            Object valor = query.getObject(i);
            String texto = "[ColunaVazia]";
            //
            if (valor instanceof InputStream) {
              try {
                texto = getTika().parseToString(
                    (InputStream) query.getObject(i));
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
      return qtdeItensIndexados;
    } catch (Exception e) {
      throw new ExcecaoIndexador(e);
    } finally {
      try {
        idx.fecha();
      } catch (Exception e) {
      }
    }
  }

  private void removeMetadados(FonteDados fonteDados) {
    EntityManager em = emf.createEntityManager();
    for (MetaDado metadado : fonteDados.getMetadados()) {
      try {
        em.getTransaction().begin();
        MetaDado m = metadado;
        m = em.merge(metadado);
        em.remove(m);
        em.getTransaction().commit();
      } catch (Exception e) {
        logger.error(e);
        em.getTransaction().rollback();
      }
    }
  }

  private void persistir(FonteDados fonteDados, String coluna) {
    EntityManager em = emf.createEntityManager();
    try {
      String hql = "select m from MetaDado m where ucase(m.campo) like '"
          + coluna + "' and m.fonte.id = " + fonteDados.getId();
      List<MetaDado> list = em.createQuery(hql).getResultList();
      if (list.size() > 0) return;
      MetaDado md = new MetaDado();
      md.setCampo(coluna);
      md.setFonte(fonteDados);
      em.getTransaction().begin();
      em.persist(md);
      em.getTransaction().commit();
    } catch (Exception e) {
      em.getTransaction().rollback();
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
        colunas.add(rsMetaDados.getColumnLabel(i));
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
      } catch (Exception e) {
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

  public FonteDados buscarFontePeloNome(String dirHome) {
    try {
      EntityManager em = emf.createEntityManager();
      FonteDados fonte = (FonteDados) em.createQuery(
          "select f from FonteDados f where f.nome like '" + dirHome + "'")
          .getSingleResult();
      return fonte;
    } catch (Exception e) {
      return null;
    }
  }

  public Tika getTika() {
    if (tika == null) {
      tika = new Tika();
    }
    return tika;
  }
}
