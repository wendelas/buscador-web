package net.visualizacao.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation.IDFExplanation;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class UtilBusca {
  private static Logger logger = Logger.getLogger(UtilBusca.class);
  private FSDirectory diretorio;
  private static Set<String> campos;
  private IndexSearcher buscador;
  private IndexReader reader;
  private long duracaoBusca;
  private Integer quantidadeLimiteRegistros = 5000;
  private String diretorioIndice;

  public Integer getTotalDocumentosIndexados() {
    try {
      diretorio = FSDirectory.open(new File(diretorioIndice));
      reopen();
      int total = reader.numDocs();
      fecha();
      return total;
    } catch (IOException e) {
      logger.error(e);
      return 0;
    }
  }

  public void reopen() throws IOException {
    diretorio = FSDirectory.open(new File(diretorioIndice));
    reader = IndexReader.open(diretorio);
    buscador = new IndexSearcher(reader);
  }

  public UtilBusca() throws IOException {
    diretorio = FSDirectory.open(new File(diretorioIndice));
    reopen();
  }

  public UtilBusca(String diretorioIndice) throws IOException {
    this.diretorioIndice = diretorioIndice;
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
    return busca(arrCampos, argumentoDePesquisa);
  }

  public TopDocs busca(String[] campos, String argumentoDePesquisa)
      throws ParseException, IOException {
    //
    long time = System.currentTimeMillis();
    argumentoDePesquisa = StringUtils.limpacaracter(argumentoDePesquisa);
    QueryParser analisador = null;
    if (campos.length == 1) {
      analisador = new QueryParser(Version.LUCENE_36, campos[0],
          new StandardAnalyzer(Version.LUCENE_36));
    } else {
      analisador = new MultiFieldQueryParser(Version.LUCENE_36, campos,
          new StandardAnalyzer(Version.LUCENE_36));
    }
    //    analisador.setDefaultOperator(Operator.AND);
    Query consulta = analisador.parse(argumentoDePesquisa);
    TopDocs hits = getBuscador().search(consulta, quantidadeLimiteRegistros);
    duracaoBusca = System.currentTimeMillis() - time;
    fecha();
    return hits;
  }

  public void fecha() {
    try {
      diretorio.close();
    } catch (Exception e) {
      logger.error(e);
    }
    try {
      reader.close();
    } catch (IOException e) {
      logger.error(e);
    }
    try {
      buscador.close();
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

  public Document doc(int docID) throws IOException {
    Document doc = getBuscador().doc(docID);
    fecha();
    return doc;
  }

  public float getIdf(String termo, String campo) throws IOException {
    Term term = new Term(campo, termo);
    IDFExplanation explicacao = Similarity.getDefault().idfExplain(term,
        buscador);
    return explicacao.getIdf();
  }

  public double getTfIdf(String termo) {
    return Math.sqrt(buscador.maxDoc());
  }

  public static void main(String[] args) {
    try {
      UtilBusca buscador = new UtilBusca("");
      long time = System.currentTimeMillis();
      String query = "PERANTE AUTORIDADE POLICIAL";
      TopDocs resultado = buscador.busca("*", query);
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
      fQuery.setBoost(getIdf(termo, campo));
      query.add(fQuery, Occur.SHOULD);
    }
    TopDocs hits = getBuscador().search(query, quantidadeLimiteRegistros);
    return hits;
  }

  public long getDuracaoBusca() {
    return duracaoBusca;
  }

  public TopDocs busca(Query query) throws IOException {
    TopDocs hits;
    try {
      long time = System.currentTimeMillis();
      hits = getBuscador().search(query, quantidadeLimiteRegistros);
      duracaoBusca = System.currentTimeMillis() - time;
    } finally {
      fecha();
    }
    return hits;
  }

  // FIXME remover
  public static Collection<String> getCampos() {
    if (campos == null) {
      campos = new HashSet<String>();
      carregaCamposDisponiveis();
    }
    return campos;
  }

  // FIXME Remover
  private static void carregaCamposDisponiveis() {
    try {
      UtilBusca buscador = new UtilBusca(1, "");
      TopDocs hits = buscador.busca("InteiroTeor", "a*");
      Set<String> camposNaoOrdenados = new HashSet<String>();
      camposNaoOrdenados.add("InteiroTeor");
      camposNaoOrdenados.add("Indexacao");
      if (hits.totalHits > 1) {
        buscador.reopen();
        Document doc = buscador.doc(hits.scoreDocs[0].doc);
        List<Fieldable> fields = doc.getFields();
        for (Fieldable f : fields) {
          // if (f.name().contains(Constantes.FIM_DO_REGISTRO)) {
          // continue;
          // }
          camposNaoOrdenados.add(f.name());
        }
        campos = new TreeSet<String>(camposNaoOrdenados);
      }
      buscador.fecha();
    } catch (Exception e) {
      logger.error(e);
    }
  }

  public TopDocs busca(String campo, String argumentoDePesquisa)
      throws ParseException, IOException {
    return busca(new String[] {campo}, argumentoDePesquisa);
  }

  public void addCampoOrdenacao() {
  }
}
