package net.visualizacao.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.Normalizer;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.indexador.entidades.AnexoFonteDados;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.Tika;

public class Indexador {
    private static Logger logger = Logger.getLogger(Indexador.class);
    private IndexWriter writer;
    private String diretorioIndice;
    private int quantidadeArquivosIndexados = 0;
    private FieldType tipoAnalisado = new FieldType();
    private FieldType tipoNaoAnalisado = new FieldType();

    private Tika tika;

    public Indexador(String diretorioIndice) throws IOException {
	this.diretorioIndice = diretorioIndice;
	File file = new File(System.getProperty("user.home")
		+ "/dados/indices/" + diretorioIndice);
	Directory d = NIOFSDirectory.open(file);
	logger.info("Diretorio do indice: " + file.getAbsolutePath());
	Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44,
		analyzer);
	config.setOpenMode(OpenMode.CREATE_OR_APPEND);
	writer = new IndexWriter(d, config);
	//
	//
	tipoNaoAnalisado.setIndexed(true);
	tipoNaoAnalisado.setStored(true);
	tipoAnalisado.setIndexed(true);
	tipoAnalisado.setStored(true);
	tipoAnalisado.setTokenized(true);
	tipoAnalisado.setStoreTermVectors(true);
	//

    }

    private Set<String> getStopWords() {
	try {
	    URL resource = getClass().getClassLoader().getResource(
		    "stopwords.txt");
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    new FileInputStream(new File(resource.toURI()))));
	    String linha;
	    Set<String> stopWords = new HashSet<String>();
	    while ((linha = br.readLine()) != null) {
		stopWords.add(linha);
	    }
	    return stopWords;
	} catch (Exception e) {
	}
	return null;
    }

    public void indexaArquivosDoDiretorio(File raiz) {
	String nomeArquivo = "";
	try {
	    for (File arquivo : raiz.listFiles(new FiltroArquivoSuportado())) {
		nomeArquivo = arquivo.getAbsolutePath();
		if (arquivo.isFile()) {
		    if (jahIndexado(getNomeArquivoFormatado(arquivo))) {
			logger.info("Jah indexado: "
				+ arquivo.getAbsolutePath());
			continue;
		    }
		    String textoExtraido = "";
		    InputStream in = new FileInputStream(arquivo);
		    textoExtraido = getTika().parseToString(in);
		    if (indexaArquivo(arquivo, textoExtraido)) {
			quantidadeArquivosIndexados++;
		    }
		} else {
		    indexaArquivosDoDiretorio(arquivo);
		}
	    }
	} catch (Exception e) {
	    logger.error("Nao foi possivel analisar o arquivo " + nomeArquivo
		    + "[" + raiz.getAbsolutePath() + "]. Erro Java: " + e);
	}
    }

    private boolean jahIndexado(String id) {
	try {
	    UtilBusca buscador = new UtilBusca(diretorioIndice);
	    TopDocs hits = buscador.buscar("ID:" + id);
	    int qtd = hits.totalHits;
	    return qtd > 0;
	} catch (Exception e) {
	    return false;
	}
    }

    public void fecha() throws CorruptIndexException, IOException {
	writer.close();
    }

    /**
     * 
     * @param valores
     *            Mapa contendo a chave (nome da coluna) e o valor (conteudo da
     *            coluna) de uma tupla no banco de dados
     * @return
     * @throws ExcecaoIndexador
     */
    public boolean indexar(Map<String, String> valores) throws ExcecaoIndexador {
	Document documento = new Document();
	if (jahIndexado(valores.get("ID")))
	    return false;
	try {
	    for (String coluna : valores.keySet()) {
		String valor = valores.get(coluna);
		if (StringUtils.vazia(valor))
		    continue;
		valor = StringUtils.limpacaracter(valor);
		// TODO Verificar esse tipo: StringField.TYPE_STORED
		// Verificar em http://lucene.apache.org/core/4_0_0/MIGRATE.html
		documento
			.add(new Field(coluna, valor, StringField.TYPE_STORED));
	    }
	    writer.addDocument(documento);
	    writer.commit();
	    return true;
	} catch (Exception e) {
	    throw new ExcecaoIndexador(e);
	}
    }

    public boolean indexaArquivo(File arquivo, String textoExtraido) {
	try {
	    textoExtraido = StringUtils.limpacaracter(textoExtraido);
	    if (StringUtils.vazia(textoExtraido)) {
		return false;
	    }
	    Document documento = new Document();
	    documento.add(new Field("AnoUltimaModificacao", DateUtils
		    .getYear(new Date(arquivo.lastModified())),
		    tipoNaoAnalisado));
	    documento.add(new Field("UltimaModificacao", DateUtils
		    .getDateFormatoAmericano(new Date(arquivo.lastModified())),
		    tipoNaoAnalisado));
	    documento.add(new Field("DataIndexacaoLucene", DateUtils
		    .getDateFormatoAmericano(new Date()), tipoNaoAnalisado));
	    documento.add(new Field("ID", getNomeArquivoFormatado(arquivo),
		    tipoAnalisado));
	    documento.add(new Field("Caminho", arquivo.getAbsolutePath(),
		    tipoAnalisado));
	    documento.add(new Field("TextoCompleto", textoExtraido,
		    tipoAnalisado));
	    writer.addDocument(documento);
	    writer.commit();
	    return true;
	} catch (Exception e) {
	    logger.error(e);
	    return false;
	}
    }

    private String getNomeArquivoFormatado(File arquivo) {
	return StringUtils.limpacaracter(
		arquivo.getAbsolutePath().replaceAll("[/ &()-]", "_")
			.replaceAll("_+", "_")).toLowerCase();
    }

    public static void main(String[] args) throws IOException {
	String arquivo = "/Users/marcoreis/Documents/teste.txt";
	FileReader fileReader = new FileReader(arquivo);
	BufferedReader bufferedReader = new BufferedReader(fileReader);
	String linha;
	while ((linha = bufferedReader.readLine()) != null) {
	    System.out.println(StringUtils.limpacaracter(linha));
	}
    }

    public int getQuantidadeArquivosIndexados() {
	return quantidadeArquivosIndexados;
    }

    public Tika getTika() {
	if (tika == null) {
	    tika = new Tika();
	}
	return tika;
    }

    public boolean indexaAnexo(AnexoFonteDados anexo) {
	try {
	    String textoExtraido = getTika().parseToString(
		    new ByteArrayInputStream(anexo.getAnexo()));
	    textoExtraido = Normalizer.normalize(textoExtraido,
		    Normalizer.Form.NFD).replaceAll(
		    "\\p{InCombiningDiacriticalMarks}+", "");
	    if (StringUtils.vazia(textoExtraido)) {
		logger.warn("O arquivo [" + anexo.getNomeArquivo()
			+ "] não tem texto");
		return false;
	    }
	    Document documento = new Document();
	    documento.add(new Field("DataIndexacaoLucene", DateUtils
		    .getDateFormatoAmericano(new Date()), tipoNaoAnalisado));
	    documento.add(new Field("ID", anexo.getNomeArquivo(),
		    tipoNaoAnalisado));
	    documento.add(new Field("TextoCompleto", textoExtraido,
		    tipoAnalisado));
	    writer.addDocument(documento);
	    writer.commit();
	    quantidadeArquivosIndexados++;
	    return true;
	} catch (Exception e) {
	    logger.error(e);
	    return false;
	}

    }

    public void excluirIndice() {
	try {
	    fecha();
	} catch (Exception e) {
	    logger.error(e);
	}
	File diretorio = new File(System.getProperty("user.home")
		+ "/dados/indices/" + diretorioIndice);
	for (File arquivo : diretorio.listFiles()) {
	    arquivo.delete();
	}
	diretorio.delete();
    }
}
