package net.visualizacao.util;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.store.FSDirectory;

public class UtilBusca {
	private static Logger logger = Logger.getLogger(UtilBusca.class);
	private FSDirectory directory;
	private IndexSearcher searcher;
	// private IndexReader reader;
	private long duracaoBusca;
	private Integer quantidadeLimiteRegistros = 1000;
	private String diretorioDicionarios;
	private HashMap<String, String> args;
	private Query query;
	private Analyzer analyzer;
	private SearcherManager sm;

	public Analyzer getAnalyzer() {
		if (analyzer == null) {
			args = new HashMap<String, String>();
			args.put("stopWords", "stopwords.txt");
			args.put("dictionaries", "synonyms.txt");
			args.put("baseDirectory", diretorioDicionarios);
			analyzer = new StandardAnalyzer();
			logger.info("Novo analyzer");
		}
		return analyzer;
	}

	public UtilBusca(String diretorioIndice) throws IOException {
		abrirIndice(diretorioIndice);
	}

	private void abrirIndice(String diretorioIndice) throws IOException {
		directory = FSDirectory.open(Paths.get(diretorioIndice));
		this.diretorioDicionarios = diretorioIndice + "/dicionarios";
		sm = new SearcherManager(directory, null);
		logger.info("New SearcherManager");
	}

	public void release() {
		try {
			sm.release(searcher);
			searcher = null;
			logger.info("Release");
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	public Document doc(int docID) throws IOException {
		// Criar campo com highlight
		FastVectorHighlighter fhl = new FastVectorHighlighter();
		FieldQuery fq = fhl.getFieldQuery(getQuery());
		acquire();
		Document doc = searcher.doc(docID);
		String fragHighlight = fhl.getBestFragment(fq, searcher.getIndexReader(), docID, "TextoCompleto",
				doc.get("TextoCompleto").length());
		if (fragHighlight == null)
			fragHighlight = doc.get("TextoCompleto");
		try {
			doc.add(new StringField("TextoCompleto.hl", fragHighlight, Store.NO));
		} catch (Exception e) {
			logger.error(e);
		} finally {
			release();
		}
		return doc;
	}

	private void acquire() {
		try {
			searcher = sm.acquire();
			logger.info("Acquire");
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	public Query getQuery() {
		return query;
	}

	public static void main(String[] args) {
		try {
			UtilBusca buscador = null;
			long time = System.currentTimeMillis();
			String query = "PERANTE AUTORIDADE POLICIAL";
			TopDocs resultado = null; // buscador.busca("*", query);
			long total = System.currentTimeMillis() - time;
			// System.out.println(new BigDecimal(total / 1000d));
			System.out.println(resultado.totalHits);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public long getDuracaoBusca() {
		return duracaoBusca;
	}

	public TopDocs buscar(String consulta) {
		try {
			consulta = Normalizer.normalize(consulta, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+",
					"");
			QueryParser queryParser = new QueryParser("", getAnalyzer());
			queryParser.setDefaultOperator(Operator.AND);
			query = queryParser.parse("TextoCompleto:(" + consulta + ")");
			TopDocs hits = buscar();
			release();
			return hits;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public TopDocs buscar() {
		TopDocs hits;
		try {
			long time = System.currentTimeMillis();
			acquire();
			hits = searcher.search(query, quantidadeLimiteRegistros);
			duracaoBusca = System.currentTimeMillis() - time;
			release();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return hits;
	}

	public void fechar() {
		try {
			directory.close();
			sm.close();
			logger.info("Fechar SearchManager");
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

}
