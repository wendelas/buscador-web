package net.visualizacao.util;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

public class UtilBusca {
    private static Logger logger = Logger.getLogger(UtilBusca.class);
    private FSDirectory diretorio;
    private IndexSearcher buscador;
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
	    logger.info("Novo analyzer");
	    args = new HashMap<String, String>();
	    args.put("stopWords", "stopwords.txt");
	    args.put("dictionaries", "synonyms.txt");
	    args.put("baseDirectory", diretorioDicionarios);
	    args.put("luceneMatchVersion", Version.LUCENE_44.toString());
	    // analyzer = new TimbreAnalyzer(Version.LUCENE_44, args);
	    analyzer = new StandardAnalyzer(Version.LUCENE_44);
	}
	return analyzer;
    }

    // public void reopen() throws IOException {
    // if (reader == null) {
    // diretorio = FSDirectory.open(new File(diretorioIndice));
    // reader = DirectoryReader.open(diretorio);
    // buscador = new IndexSearcher(reader);
    // }
    // }

    public UtilBusca(String diretorioIndice) throws IOException {
	diretorio = new NIOFSDirectory(new File(diretorioIndice));
	this.diretorioDicionarios = diretorioIndice + "/dicionarios";
	sm = new SearcherManager(diretorio, null);
	buscador = sm.acquire();
	logger.info("Abrir");
    }

    // public UtilBusca(String diretorioIndice) throws IOException {
    // this.diretorioIndice = diretorioIndice;
    // this.diretorioDicionarios = diretorioIndice + "/dicionarios";
    // reopen();
    // }

    public void fechar() {
	try {
	    if (diretorio != null)
		diretorio.close();
	    diretorio = null;
	} catch (Exception e) {
	    logger.error(e);
	}
	try {
	    sm.release(buscador);
	    buscador = null;
	    logger.info("Fechar");
	    // reader.close();
	    // reader = null;
	} catch (Exception e) {
	    logger.error(e);
	}
    }

    public IndexSearcher getBuscador() throws IOException {
	if (buscador == null) {
	    logger.info("Abrir");
	    buscador = sm.acquire();
	}
	return buscador;
    }

    public Document doc(int docID) throws IOException {
	// reopen();
	// Formatter formatter = new SimpleHTMLFormatter("<strong>",
	// "</strong>");
	// Scorer scorer = new QueryScorer(getQuery());
	FastVectorHighlighter fhl = new FastVectorHighlighter();
	FieldQuery fq = fhl.getFieldQuery(getQuery());
	Document doc = getBuscador().doc(docID);
	String frag = fhl.getBestFragment(fq, getBuscador().getIndexReader(),
		docID, "TextoCompleto", 400);
	if (frag == null)
	    frag = "";
	// Highlighter hl = new Highlighter(formatter, scorer);
	// String texto = doc.get("TextoCompleto");
	try {
	    // TokenStream token = TokenSources.getTokenStream(doc,
	    // "TextoCompleto", getAnalyzer());
	    // String fragmentos = hl.getBestFragment(new StandardAnalyzer(
	    // Version.LUCENE_44), "TextoCompleto", texto);
	    // doc.add(new StringField("TextoCompleto.hl", fragmentos,
	    // Store.NO));
	    // String fragmentos = hl.getBestFragment(getAnalyzer(),
	    // "TextoCompleto", texto);
	    doc.add(new StringField("TextoCompleto.hl", frag, Store.NO));
	} catch (Exception e) {
	    logger.error(e);
	} finally {
	    fechar();
	}
	return doc;
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
	QueryParser queryParser = new QueryParser(Version.LUCENE_44, "",
		getAnalyzer());
	queryParser.setDefaultOperator(Operator.AND);
	query = queryParser.parse("TextoCompleto:(" + consulta + ")");
	return buscar();
    }

    public TopDocs buscar() {
	TopDocs hits;
	try {
	    long time = System.currentTimeMillis();
	    hits = getBuscador().search(query, quantidadeLimiteRegistros);
	    duracaoBusca = System.currentTimeMillis() - time;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	} finally {
	    // fechar();
	}
	return hits;
    }

}
