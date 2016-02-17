package net.visualizacao.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
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
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.Tika;
import org.apache.tika.detect.AutoDetectReader;

import net.indexador.entidades.AnexoFonteDados;
import net.indexador.negocio.GenericXMLParser;

public class Indexador {
    private static Logger logger = Logger.getLogger(Indexador.class);
    private IndexWriter writer;
    private String diretorioIndice;
    private String diretorioDicionarios;
    private int quantidadeArquivosIndexados = 0;
    private FieldType tipoAnalisado = new FieldType();
    private FieldType tipoNaoAnalisado = new FieldType();

    private Tika tika;

    public Indexador(String diretorioIndice) throws IOException {
	this.diretorioIndice = diretorioIndice;
	this.diretorioDicionarios = System.getProperty("user.home") + "/dados/indices/" + diretorioIndice
		+ "/dicionarios";
	String file = System.getProperty("user.home") + "/dados/indices/" + diretorioIndice;
	File dicionariosDir = new File(this.diretorioDicionarios);

	if (!dicionariosDir.exists()) {
	    dicionariosDir.mkdir();
	}

	File stopWordsFile = new File(this.diretorioDicionarios + "/stopwords.txt");
	File dictionariesFile = new File(this.diretorioDicionarios + "/dictionaries.txt");
	if (!stopWordsFile.exists()) {
	    URL url = getClass().getClassLoader().getResource("stopwords.txt");
	    try {
		File f = new File(url.toURI());
		FileUtils.copyFileToDirectory(f, dicionariosDir);
	    } catch (URISyntaxException e) {
		logger.error("Erro ao copiar dicionário de stopwords");
	    }
	}
	if (!dictionariesFile.exists()) {
	    URL url = getClass().getClassLoader().getResource("synonyms.txt");
	    try {
		File f = new File(url.toURI());
		FileUtils.copyFileToDirectory(f, dicionariosDir);
	    } catch (URISyntaxException e) {
		logger.error("Erro ao copiar dicionário de sinônimos");
	    }
	}

	Directory d = FSDirectory.open(Paths.get(file));
	logger.info("Diretorio do indice: " + file);
	// Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
	HashMap<String, String> args = new HashMap<String, String>();
	args.put("stopWords", "stopwords.txt");
	args.put("baseDirectory", diretorioDicionarios);
	// args.put("luceneMatchVersion", Version.LUCENE_44.toString());

	Analyzer analyzer = new StandardAnalyzer();
	// Analyzer analyzer = new TimbreAnalyzer(Version.LUCENE_44, args);
	IndexWriterConfig config = new IndexWriterConfig(analyzer);
	config.setOpenMode(OpenMode.CREATE_OR_APPEND);
	writer = new IndexWriter(d, config);
	//
	//
	// TODO: TipoNaoAnalisado não deve ser indexado
	// tipoNaoAnalisado.setIndexed(true);
	tipoNaoAnalisado.setStored(true);
	tipoAnalisado.setStored(true);
	tipoAnalisado.setTokenized(true);
	tipoAnalisado.setStoreTermVectors(true);
	tipoAnalisado.setStoreTermVectorOffsets(true);
	tipoAnalisado.setStoreTermVectorPayloads(true);
	tipoAnalisado.setStoreTermVectorPositions(true);
	//

    }

    // TODO:Remover esse método stopwords
    private Set<String> getStopWords() {
	try {
	    URL resource = getClass().getClassLoader().getResource("stopwords.txt");
	    BufferedReader br = new BufferedReader(
		    new InputStreamReader(new FileInputStream(new File(resource.toURI()))));
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
			logger.info("Jah indexado: " + arquivo.getAbsolutePath());
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
	    logger.error("Nao foi possivel analisar o arquivo " + nomeArquivo + "[" + raiz.getAbsolutePath()
		    + "]. Erro Java: " + e);
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
		documento.add(new Field(coluna, valor, StringField.TYPE_STORED));
	    }
	    writer.addDocument(documento);
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
	    documento.add(new Field("AnoUltimaModificacao", DateUtils.getYear(new Date(arquivo.lastModified())),
		    tipoNaoAnalisado));
	    documento.add(new Field("UltimaModificacao",
		    DateUtils.getDateFormatoAmericano(new Date(arquivo.lastModified())), tipoNaoAnalisado));
	    documento.add(
		    new Field("DataIndexacaoLucene", DateUtils.getDateFormatoAmericano(new Date()), tipoNaoAnalisado));
	    documento.add(new Field("ID", getNomeArquivoFormatado(arquivo), tipoAnalisado));
	    documento.add(new Field("Caminho", arquivo.getAbsolutePath(), tipoAnalisado));
	    documento.add(new Field("TextoCompleto", textoExtraido, tipoAnalisado));
	    writer.addDocument(documento);
	    quantidadeArquivosIndexados++;
	    return true;
	} catch (Exception e) {
	    logger.error(e);
	    return false;
	}
    }

    private String getNomeArquivoFormatado(File arquivo) {
	return StringUtils.limpacaracter(arquivo.getAbsolutePath().replaceAll("[/ &()-]", "_").replaceAll("_+", "_"))
		.toLowerCase();
    }

    public static void main(String[] args) throws IOException {
	String arquivo = "/Users/marcoreis/Documents/teste.txt";
	FileReader fileReader = new FileReader(arquivo);
	BufferedReader bufferedReader = new BufferedReader(fileReader);
	String linha;
	while ((linha = bufferedReader.readLine()) != null) {
	    System.out.println(StringUtils.limpacaracter(linha));
	}
	bufferedReader.close();
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

    private void indexarXML(AnexoFonteDados anexo) {
	GenericXMLParser parser = new GenericXMLParser();
	parser.parse(new ByteArrayInputStream(anexo.getAnexo()));
    }

    public void indexaAnexo(AnexoFonteDados anexo) {
	if (anexo.getNomeArquivo().toLowerCase().endsWith(".xml")) {
	    indexarXML(anexo);
	} else if (anexo.getNomeArquivo().toLowerCase().endsWith(".pdf")
		|| anexo.getNomeArquivo().toLowerCase().endsWith(".doc")
		|| anexo.getNomeArquivo().toLowerCase().endsWith(".xls")
		|| anexo.getNomeArquivo().toLowerCase().endsWith(".ppt")
		|| anexo.getNomeArquivo().toLowerCase().endsWith(".rtf")) {
	    indexarArquivoBinario(anexo);
	} else if (anexo.getNomeArquivo().toLowerCase().endsWith(".csv")) {
	    indexarCSV(anexo);
	} else if (anexo.getNomeArquivo().toLowerCase().endsWith(".zip")) {
	    try {
		byte[] buffer = new byte[1024];
		ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(anexo.getAnexo()),
			Charset.forName("ISO-8859-1"));
		ZipEntry entry = zip.getNextEntry();
		while (entry != null) {
		    int len;
		    ByteArrayOutputStream saida = new ByteArrayOutputStream();
		    while ((len = zip.read(buffer)) > 0) {
			saida.write(buffer, 0, len);
		    }
		    AnexoFonteDados subAnexo = new AnexoFonteDados();
		    subAnexo.setDataEnvio(new Timestamp((System.currentTimeMillis())));
		    subAnexo.setNomeArquivo(entry.getName());
		    subAnexo.setTamanho(entry.getSize());
		    subAnexo.setAnexo(saida.toByteArray());
		    saida.close();
		    indexaAnexo(subAnexo);
		    entry = zip.getNextEntry();
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    private void indexarCSV(AnexoFonteDados anexo) {
	InputStream is = null;
	BufferedReader br = null;
	int quantidadeLinhas = 0;
	try {
	    is = new ByteArrayInputStream(anexo.getAnexo());
	    br = new BufferedReader(new AutoDetectReader(is));
	    String[] metadados = null;
	    int linha = 1;
	    String line = br.readLine();
	    while (line != null) {
		if (linha == 1) {
		    linha++;
		    if (anexo.getSeparador().length() > 0) {
			metadados = line.split(anexo.getSeparador());
		    } else {
			metadados = new String[] { line };
		    }
		} else {
		    Document doc = new Document();
		    try {
			if (anexo.getSeparador().length() > 0) {
			    String[] dados = line.split(anexo.getSeparador());
			    for (int i = 0; i < metadados.length; i++) {
				doc.add(new Field(metadados[i], dados[i], tipoAnalisado));
			    }
			} else {
			    doc.add(new Field(metadados[0], line, tipoAnalisado));
			}
		    } catch (Exception e) {
			logger.error("Erro na linha " + quantidadeLinhas);
		    }
		    doc.add(new Field("TextoCompleto", line, tipoAnalisado));
		    writer.addDocument(doc);
		}
		quantidadeLinhas++;
		if (quantidadeLinhas % 100000 == 0) {
		    logger.info("Quantidade de linhas processadas: " + quantidadeLinhas);
		}
		line = br.readLine();
	    }
	    logger.info("Adicionado: " + anexo.getNomeArquivo());
	} catch (Exception e) {
	    logger.info(e);
	} finally {
	    try {
		is.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    try {
		br.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    private void indexarArquivoBinario(AnexoFonteDados anexo) {
	try {
	    String textoExtraido = getTika().parseToString(new ByteArrayInputStream(anexo.getAnexo()));
	    textoExtraido = Normalizer.normalize(textoExtraido, Normalizer.Form.NFD)
		    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	    if (StringUtils.vazia(textoExtraido)) {
		logger.warn("O arquivo [" + anexo.getNomeArquivo() + "] não tem texto");
		return;
	    }
	    Document documento = new Document();
	    documento.add(
		    new Field("DataIndexacaoLucene", DateUtils.getDateFormatoAmericano(new Date()), tipoNaoAnalisado));
	    documento.add(new Field("ID", anexo.getNomeArquivo(), tipoNaoAnalisado));
	    documento.add(new Field("TextoCompleto", textoExtraido, tipoAnalisado));
	    writer.addDocument(documento);
	    quantidadeArquivosIndexados++;
	    logger.info("Adicionado: " + anexo.getNomeArquivo());
	} catch (Exception e) {
	    logger.error(e);
	}
    }

    public void excluirIndice() {
	try {
	    fecha();
	} catch (Exception e) {
	    logger.error(e);
	}
	File diretorio = new File(System.getProperty("user.home") + "/dados/indices/" + diretorioIndice);
	for (File arquivo : diretorio.listFiles()) {
	    arquivo.delete();
	}
	diretorio.delete();
    }
}
