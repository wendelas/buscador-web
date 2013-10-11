package net.visualizacao.util;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import br.com.timbre.TimbreAnalyzer;

public class UtilBusca {
    private static Logger logger = Logger.getLogger(UtilBusca.class);
    private FSDirectory diretorio;
    private IndexSearcher buscador;
    private IndexReader reader;
    private long duracaoBusca;
    private Integer quantidadeLimiteRegistros = 5000;
    private String diretorioIndice;
    private String diretorioDicionarios;
    private HashMap<String,String> args;
    private Query query;

    public Integer getTotalDocumentosIndexados() {
	try {
	    diretorio = FSDirectory.open(new File(diretorioIndice));
	    reopen();
	    int total = reader.numDocs();
	    return total;
	} catch (IOException e) {
	    logger.error(e);
	    return 0;
	}
    }

    public void reopen() throws IOException {
	if (reader == null) {
	    diretorio = FSDirectory.open(new File(diretorioIndice));
	    reader = DirectoryReader.open(diretorio);
	    buscador = new IndexSearcher(reader);
	}
    }

    public UtilBusca() throws IOException {
	diretorio = FSDirectory.open(new File(diretorioIndice));
	this.diretorioDicionarios = diretorioIndice + "/dicionarios";
	this.args = new HashMap<String, String>();
	args.put("stopWords", "stopwords.txt");
    args.put("dictionaries", "synonyms.txt");
    args.put("baseDirectory", diretorioDicionarios);
    args.put("luceneMatchVersion",Version.LUCENE_44.toString());
	reopen();
    }

    public UtilBusca(String diretorioIndice) throws IOException {
	this.diretorioIndice = diretorioIndice;
	this.diretorioDicionarios = diretorioIndice + "/dicionarios";
	this.args = new HashMap<String, String>();
	args.put("stopWords", "stopwords.txt");
    args.put("dictionaries", "synonyms.txt");
    args.put("baseDirectory", diretorioDicionarios);
    args.put("luceneMatchVersion",Version.LUCENE_44.toString());
	reopen();
    }

    public UtilBusca(Integer quantidadeLimiteRegistros) throws IOException {
	this();
	this.quantidadeLimiteRegistros = quantidadeLimiteRegistros;
    }

    public UtilBusca(Integer quantidadeLimiteDeAcordaos, String diretorioIndice)
	    throws IOException {
	this(diretorioIndice);
	this.quantidadeLimiteRegistros = quantidadeLimiteDeAcordaos;
    }

    public TopDocs busca(Collection<String> campos, String argumentoDePesquisa)
	    throws ParseException, IOException {
	String[] arrCampos = new String[campos.size()];
	int i = 0;
	for (String campo : campos) {
	    arrCampos[i++] = campo;
	}
	return buscar(arrCampos, argumentoDePesquisa);
    }

    public TopDocs buscar(String[] campos, String argumentoDePesquisa)
	    throws ParseException, IOException {
	//
	long time = System.currentTimeMillis();
	argumentoDePesquisa = StringUtils.limpacaracter(argumentoDePesquisa);
	QueryParser analisador = null;
	if (campos.length == 1) {
	    analisador = new QueryParser(Version.LUCENE_44, campos[0],
		    //new StandardAnalyzer(Version.LUCENE_44));
    		new TimbreAnalyzer(Version.LUCENE_44, args));
	} else {
	    analisador = new MultiFieldQueryParser(Version.LUCENE_44, campos,
		    //new StandardAnalyzer(Version.LUCENE_44));
    		new TimbreAnalyzer(Version.LUCENE_44, args));
	}
	analisador.setDefaultOperator(Operator.AND);
	Query consulta = analisador.parse(argumentoDePesquisa);
	TopDocs hits = getBuscador()
		.search(consulta, quantidadeLimiteRegistros);
	duracaoBusca = System.currentTimeMillis() - time;
	return hits;
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

    public  IndexSearcher getBuscador() throws IOException {
	return buscador;
    }

    public Document doc(int docID) throws IOException {
	reopen();
	Formatter formatter = new SimpleHTMLFormatter("<strong>", "</strong>");
	Scorer scorer = new QueryScorer(getQuery());
	Highlighter hl = new Highlighter(formatter, scorer);
	Document doc = getBuscador().doc(docID);
	String texto = doc.get("TextoCompleto");
	TokenStream token = TokenSources.getTokenStream(doc, "TextoCompleto",
		//new StandardAnalyzer(Version.LUCENE_44));
			new TimbreAnalyzer(Version.LUCENE_44, args));
	try {
	    //String fragmentos = hl.getBestFragment(new StandardAnalyzer(
		    //Version.LUCENE_44), "TextoCompleto", texto);
	    //doc.add(new StringField("TextoCompleto.hl", fragmentos, Store.NO));
	    String fragmentos = hl.getBestFragment(new TimbreAnalyzer(
	    Version.LUCENE_44, args), "TextoCompleto", texto);
	    doc.add(new StringField("TextoCompleto.hl", fragmentos, Store.NO));
	} catch (Exception e) {
	    e.printStackTrace();
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
	//StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
	TimbreAnalyzer analyzer = new TimbreAnalyzer(Version.LUCENE_44, args);
	QueryParser queryParser = new QueryParser(Version.LUCENE_44, "",
		analyzer);
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
	}
	return hits;
    }

    public void addCampoOrdenacao() {
    }
}
