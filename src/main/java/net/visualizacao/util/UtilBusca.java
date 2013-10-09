package net.visualizacao.util;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class UtilBusca {
    private static Logger logger = Logger.getLogger(UtilBusca.class);
    private FSDirectory diretorio;
    private IndexSearcher buscador;
    private IndexReader reader;
    private long duracaoBusca;
    private Integer quantidadeLimiteRegistros = 5000;
    private Query query;
    private SearcherManager sm;
    private String diretorioIndice;

    public void reopen() {
	try {
	    if (sm == null) {
		diretorio = FSDirectory.open(new File(diretorioIndice));
		sm = new SearcherManager(diretorio, null);
	    }
	    buscador = sm.acquire();
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

    public UtilBusca(String diretorioIndice) {
	this.diretorioIndice = diretorioIndice;
    }

    public void release() {
	try {
	    sm.release(buscador);
	    buscador = null;
	    //
	} catch (IOException e) {
	    e.printStackTrace();
	}
	//
	try {
	    if (diretorio != null)
		diretorio.close();
	    diretorio = null;
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void fecha() {
	try {
	    diretorio.close();
	    diretorio = null;
	} catch (Exception e) {
	    logger.error(e);
	}
	try {
	    reader.close();
	    reader = null;
	} catch (IOException e) {
	    logger.error(e);
	}
    }

    public TopDocs buscaExata(String campo, String argumentoDePesquisa) {
	try {
	    Query query = new TermQuery(new Term(campo, argumentoDePesquisa));
	    TopDocs hits = getBuscador().search(query, 100);
	    return hits;
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	} finally {
	}
    }

    private IndexSearcher getBuscador() throws IOException {
	return buscador;
    }

    public Document doc(int docID) {
	try {
	    reopen();
	    Formatter formatter = new SimpleHTMLFormatter("<strong>",
		    "</strong>");
	    Scorer scorer = new QueryScorer(getQuery());
	    Highlighter hl = new Highlighter(formatter, scorer);
	    Document doc = getBuscador().doc(docID);
	    String texto = doc.get("TextoCompleto");
	    String fragmentos = hl.getBestFragment(new StandardAnalyzer(
		    Version.LUCENE_44), "TextoCompleto", texto);
	    doc.add(new StringField("TextoCompleto.hl", fragmentos, Store.NO));
	    return doc;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	} finally {
	    release();
	}
    }

    public Query getQuery() {
	return query;
    }

    // public float getIdf(String termo, String campo) throws IOException {
    // Term term = new Term(campo, termo);
    // IDFExplanation explicacao = Similarity.getDefault().idfExplain(term,
    // buscador);
    // return explicacao.getIdf();
    // }
    // public double getTfIdf(String termo) {
    // return Math.sqrt(buscador.maxDoc());
    // }
    public static void main(String[] args) {
	try {
	    UtilBusca buscador = new UtilBusca("");
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

    // FIXME Desacoplar do Lucene (nao sei como).
    public TopDocs processaSimilaridade(String campo, String[] termos)
	    throws IOException {
	// An easier way might be to submit doc A as a query
	// (adding all words to the query as OR terms,
	// boosting each by term frequency) and look
	// for doc B in the result set.
	BooleanQuery query = new BooleanQuery();
	for (String termo : termos) {
	    Term term = new Term(campo, termo);
	    // TermQuery tQuery = new TermQuery(term);
	    // tQuery.setBoost(getIdf(termo));
	    // FuzzyQuery fQuery = new FuzzyQuery(term, 0.8f);
	    FuzzyQuery fQuery = new FuzzyQuery(term);
	    query.add(fQuery, Occur.SHOULD);
	}
	TopDocs hits = getBuscador().search(query, quantidadeLimiteRegistros);
	return hits;
    }

    public long getDuracaoBusca() {
	return duracaoBusca;
    }

    public TopDocs buscar(String consulta) throws ParseException {
	consulta = Normalizer.normalize(consulta, Normalizer.Form.NFD)
		.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
	QueryParser queryParser = new QueryParser(Version.LUCENE_44, "",
		analyzer);
	queryParser.setDefaultOperator(Operator.AND);
	query = queryParser.parse("TextoCompleto:(" + consulta + ")");
	return buscar();
    }

    public TopDocs buscar() {
	reopen();
	TopDocs hits;
	try {
	    long time = System.currentTimeMillis();
	    hits = getBuscador().search(query, quantidadeLimiteRegistros);
	    duracaoBusca = System.currentTimeMillis() - time;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	} finally {
	    release();
	}
	return hits;
    }

    public void addCampoOrdenacao() {
    }
}
