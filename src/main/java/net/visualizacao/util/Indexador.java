package net.visualizacao.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

public class Indexador {
	private static Logger logger = Logger.getLogger(Indexador.class);
	private IndexWriter writer;
	private String diretorioIndice;
	int quantidadeArquivosIndexados = 0;

	//	private Tika tika;

	public Indexador(String diretorioIndice) throws IOException {
		this.diretorioIndice = diretorioIndice;
		File file = new File(diretorioIndice);
		if (!file.exists()) {
			file.mkdirs();
		}
		Directory d = new NIOFSDirectory(file);
		logger.info("Diretorio do indice: " + diretorioIndice);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44,
				analyzer);
		writer = new IndexWriter(d, config);
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
					//textoExtraido = getTika().parseToString(in);
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
			TopDocs hits = buscador.busca("ID", id);
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
				documento
						.add(new Field(coluna, valor, Field.Store.YES,
								Field.Index.ANALYZED,
								TermVector.WITH_POSITIONS_OFFSETS));
			}
			writer.addDocument(documento);
			writer.commit();
			return true;
		} catch (Exception e) {
			throw new ExcecaoIndexador(e);
		}
	}

	private boolean indexaArquivo(File arquivo, String textoExtraido)
			throws ExcecaoIndexador {
		try {
			textoExtraido = StringUtils.limpacaracter(textoExtraido);
			if (StringUtils.vazia(textoExtraido)) {
				return false;
			}
			Document documento = new Document();
			documento.add(new Field("AnoUltimaModificacao", DateUtils
					.getYear(new Date(arquivo.lastModified())),
					Field.Store.YES, Field.Index.NOT_ANALYZED));
			documento.add(new Field("UltimaModificacao", DateUtils
					.getDateFormatoAmericano(new Date(arquivo.lastModified())),
					Field.Store.YES, Field.Index.NOT_ANALYZED));
			documento.add(new Field("DataIndexacaoLucene", DateUtils
					.getDateFormatoAmericano(new Date()), Field.Store.YES,
					Field.Index.NOT_ANALYZED));
			documento.add(new Field("ID", getNomeArquivoFormatado(arquivo),
					Field.Store.YES, Field.Index.NOT_ANALYZED));
			documento.add(new Field("Caminho", arquivo.getAbsolutePath(),
					Field.Store.YES, Field.Index.NOT_ANALYZED));
			documento.add(new Field("Texto", textoExtraido, Field.Store.YES,
					Field.Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
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

	// public Tika getTika() {
	// if (tika == null) {
	// tika = new Tika();
	// }
	// return tika;
	// }
}
