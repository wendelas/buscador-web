package net.indexador.negocio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import net.indexador.entidades.AnexoFonteDados;
import net.indexador.entidades.FonteDados;
import net.indexador.entidades.Indice;
import net.indexador.entidades.MetaDado;
import net.visualizacao.apresentacao.VOMetaDados;
import net.visualizacao.util.ExcecaoIndexador;
import net.visualizacao.util.Indexador;
import net.visualizacao.util.JPAUtil;
import net.visualizacao.util.StringUtils;

/**
 * 
 * @author marcoreis
 * 
 */
@SuppressWarnings("unchecked")
public class FachadaBuscador {
	private static final Logger logger = Logger.getLogger(FachadaBuscador.class);
	private static FachadaBuscador instancia;
	private Tika tika;
	private Indexador indexador;

	public static FachadaBuscador getInstancia() {
		if (instancia == null) {
			instancia = new FachadaBuscador();
		}
		return instancia;
	}

	public void persistir(FonteDados fonteDados) {
		EntityManager em = JPAUtil.getInstance().getEntityManager();
		try {
			em.getTransaction().begin();
			FonteDados fd = em.merge(fonteDados);
			em.getTransaction().commit();
			fonteDados.setId(fd.getId());
		} catch (Exception e) {
			em.getTransaction().rollback();
			throw new RuntimeException(e);
		}
		em.close();
	}

	public Collection<FonteDados> buscarFontes() {
		EntityManager em = JPAUtil.getInstance().getEntityManager();
		List<FonteDados> lista = em.createQuery("select f from FonteDados f order by f.nome").getResultList();
		em.close();
		return lista;
	}

	/**
	 * Recupera os dados/metadados do banco para gerar o indice Atencao: no
	 * mysql o getColumnLabel retorna valores em uppercase, por isso os campos
	 * indexados estao assim.
	 * 
	 * @param idFonteDados
	 * @return
	 * @throws ExcecaoIndexador
	 */
	// FIXME melhorar esse codigo
	public Map<String, Long> indexar(int idFonteDados) throws ExcecaoIndexador {
		EntityManager em = JPAUtil.getInstance().getEntityManager();
		FonteDados fonteDados = em.find(FonteDados.class, idFonteDados);

		try {
			indexador = new Indexador(fonteDados.getNome());
			long inicio = System.currentTimeMillis();
			try {
				// TODO Remover esse método e usar apache o commons IO
				indexador.excluirIndice();
			} catch (Exception e) {
				// Indice ainda nao existe
			}
			logger.info("Indice excluido");
			//
			indexador = new Indexador(fonteDados.getNome());

			// Indexa arquivos no disco
			if (!StringUtils.vazia(fonteDados.getDiretorio())) {
				indexador.indexaArquivosDoDiretorio(new File(fonteDados.getDiretorio()), fonteDados.getSeparador());
				logger.info("Total de documentos indexados: " + indexador.getQuantidadeDocumentosIndexados());
			}
			//
			Collection<AnexoFonteDados> anexos = buscarAnexos(idFonteDados);
			if (anexos != null) {
				indexarAnexos(anexos);
			}

			// Indexa banco de dados
			if (!StringUtils.vazia(fonteDados.getUrl())) {
				indexarBaseDados(fonteDados);
			}
			long fim = System.currentTimeMillis();
			long tempoMinutos = (fim - inicio) / (1000 * 60);
			String msg = "Tempo para indexar: " + tempoMinutos + " minutos.";
			logger.info(msg);
			logger.info("Quantidade itens indexados: " + getIndexador().getQuantidadeDocumentosIndexados());
			em.close();
			Map<String, Long> retorno = new HashMap<String, Long>();
			retorno.put("TempoMinutos", tempoMinutos);
			retorno.put("QuantidadeDocumentosIndexados", getIndexador().getQuantidadeDocumentosIndexados());
			return retorno;
		} catch (Exception e) {
			throw new ExcecaoIndexador(e);
		} finally {
			try {
				indexador.fecha();
			} catch (Exception e) {
			}
		}
	}

	private void indexarBaseDados(FonteDados fonteDados)
			throws ClassNotFoundException, SQLException, ExcecaoIndexador, IOException {
		Connection con = null;
		Statement stmt = null;
		ResultSet query = null;
		Class.forName(fonteDados.getNomeDriver());
		con = DriverManager.getConnection(fonteDados.getUrl(), fonteDados.getUsuario(), fonteDados.getPassword());
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
							texto = getTika().parseToString((InputStream) query.getObject(i));
						} catch (Exception e) {
							// Nao eh doc/xls/pdf/etc...
						}
					} else if (valor != null) {
						texto = valor.toString();
					}
					//
					try {
						Document doc = Jsoup.parse(texto);
						texto = doc.text();
					} catch (Exception e1) {
						// Nao eh arquivo html
					}
					//
					if (!StringUtils.vazia(texto)) {
						mapa.put(coluna, texto);
					}
				} catch (Exception e) {
					logger.error(e);
				}
			}
			indexador.indexar(mapa);
		}
	}

	private void indexarAnexos(Collection<AnexoFonteDados> anexos) {
		for (AnexoFonteDados anexo : anexos) {
			indexarAnexo(anexo);
		}
	}

	private void indexarAnexo(AnexoFonteDados anexo) {
		getIndexador().indexaAnexo(anexo);
	}

	private void removeMetadados(FonteDados fonteDados) {
		EntityManager em = JPAUtil.getInstance().getEntityManager();
		for (MetaDado metadado : fonteDados.getMetadados()) {
			try {
				em.getTransaction().begin();
				MetaDado m = metadado;
				m = em.merge(metadado);
				em.remove(m);
				em.getTransaction().commit();
				em.close();
			} catch (Exception e) {
				logger.error(e);
				em.getTransaction().rollback();
			}
		}
	}

	private void persistir(FonteDados fonteDados, String coluna) {
		EntityManager em = JPAUtil.getInstance().getEntityManager();
		try {
			String hql = "select m from MetaDado m where ucase(m.campo) like '" + coluna + "' and m.fonte.id = "
					+ fonteDados.getId();
			List<MetaDado> list = em.createQuery(hql).getResultList();
			if (list.size() > 0)
				return;
			MetaDado md = new MetaDado();
			md.setCampo(coluna);
			md.setFonteDados(fonteDados);
			em.getTransaction().begin();
			em.persist(md);
			em.getTransaction().commit();
			em.close();
		} catch (Exception e) {
			em.getTransaction().rollback();
		}
	}

	public VOMetaDados buscarMetaData(FonteDados fonteDados) throws ExcecaoImportador {
		Connection con = null;
		Statement stmt = null;
		ResultSet query = null;
		VOMetaDados metaDados = new VOMetaDados();
		try {
			Class.forName(fonteDados.getNomeDriver());
			con = DriverManager.getConnection(fonteDados.getUrl(), fonteDados.getUsuario(), fonteDados.getPassword());
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
						if (StringUtils.vazia(valor))
							continue;
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
		EntityManager em = JPAUtil.getInstance().getEntityManager();
		FonteDados fonte = em.find(FonteDados.class, id);
		em.close();
		return fonte;
	}

	public void excluirFonteDados(Integer id) {
		EntityManager em = JPAUtil.getInstance().getEntityManager();
		try {
			FonteDados fonte = em.find(FonteDados.class, id);
			em.getTransaction().begin();
			em.remove(fonte);
			em.getTransaction().commit();
			Indexador indexador = new Indexador(fonte.getNome());
			indexador.excluirIndice();
		} catch (Exception e) {
			em.getTransaction().rollback();
			throw new RuntimeException(e);
		}
		em.close();
	}

	public Collection<Indice> buscarIndices() {
		EntityManager em = JPAUtil.getInstance().getEntityManager();
		List<Indice> lista = em.createQuery("select i from Indice i").getResultList();
		return lista;
	}

	public FonteDados buscarFontePeloNome(String dirHome) {
		try {
			EntityManager em = JPAUtil.getInstance().getEntityManager();
			FonteDados fonte = (FonteDados) em
					.createQuery("select f from FonteDados f where f.nome like '" + dirHome + "'").getSingleResult();
			em.close();
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

	private void persistirMetadadosAnexo(FonteDados fonte) {
		EntityManager em = JPAUtil.getInstance().getEntityManager();
		try {
			MetaDado metadado = new MetaDado();
			metadado.setCampo("DataIndexacaoLucene");
			metadado.setFonteDados(fonte);
			em.persist(metadado);
			metadado = new MetaDado();
			metadado.setCampo("ID");
			metadado.setFonteDados(fonte);
			em.persist(metadado);
			metadado = new MetaDado();
			metadado.setCampo("TextoCompleto");
			metadado.setFonteDados(fonte);
			em.persist(metadado);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void persistir(AnexoFonteDados anexo, Integer idFonteDados) {
		EntityManager em = JPAUtil.getInstance().getEntityManager();
		try {
			em.getTransaction().begin();
			FonteDados fonte = em.find(FonteDados.class, idFonteDados);
			anexo.setFonteDados(fonte);
			em.persist(anexo);
			em.getTransaction().commit();
			persistirMetadadosAnexo(fonte);
		} catch (Exception e) {
			em.getTransaction().rollback();
			throw new RuntimeException(e);
		}
		em.close();
	}

	public Collection<AnexoFonteDados> buscarAnexos(int idFonteDados) {
		EntityManager em = JPAUtil.getInstance().getEntityManager();
		String sql = "select a from AnexoFonteDados a where a.fonteDados.id = :id";
		Query query = em.createQuery(sql);
		query.setParameter("id", idFonteDados);
		List anexos = query.getResultList();
		em.close();
		return anexos;
	}

	public Indexador getIndexador() {
		return indexador;
	}
}
