package net.visualizacao.apresentacao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletResponse;

import net.indexador.entidades.FonteDados;
import net.indexador.entidades.MetaDado;
import net.indexador.negocio.FachadaBuscador;
import net.visualizacao.util.StringUtils;
import net.visualizacao.util.UtilBusca;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.NoSuchDirectoryException;

@ManagedBean
@ViewScoped
public class BuscaLivreBean extends BaseBean {
    private static final int QUANTIDADE_CARACTERES_VISUALIZACAO = 200;
    private static final long serialVersionUID = -7508553590263034662L;
    private String consulta;
    private long duracaoBusca;
    private ScoreDoc[] itens;
    private int idFonteDados;
    private FonteDados fonte;
    private Document documento;
    private UtilBusca buscador;

    public UtilBusca getBuscador() throws IOException {
	if (buscador == null) {
	    buscador = new UtilBusca(getFonte().getDiretorioIndice());
	}
	return buscador;
    }

    public void setConsulta(String consulta) {
	this.consulta = consulta;
    }

    public String getConsulta() {
	return consulta;
    }

    public BigDecimal getDuracaoBusca() {
	Double d = duracaoBusca / 1000d;
	BigDecimal bd = new BigDecimal(d).setScale(4, BigDecimal.ROUND_CEILING);
	return bd;
    }

    public int getQuantidadeDeItens() {
	try {
	    return getItens().length;
	} catch (Exception e) {
	    return 0;
	}
    }

    public Collection<String> getCamposSelecionados() {
	try {
	    if (StringUtils.vazia(fonte.getDiretorio())) {
		Collection<String> campos = new ArrayList<String>();
		for (MetaDado metadado : fonte.getMetadados()) {
		    campos.add(metadado.getCampo());
		}
		return campos;
	    } else {
		Collection<String> listaCamposArquivo = new ArrayList<String>();
		listaCamposArquivo.add("Texto");
		return listaCamposArquivo;
	    }
	} catch (Exception e) {
	    // Consulta diretorio de documentos
	    logger.error(e);
	}
	return null;
    }

    public void consultar() {
	try {
	    fonte = FachadaBuscador.getInstancia().buscarFontePeloId(
		    getIdFonteDados());
	    TopDocs hits = getBuscador().buscar(getConsulta());
	    setItens(hits.scoreDocs);
	    duracaoBusca = getBuscador().getDuracaoBusca();
	} catch (NoSuchDirectoryException e) {
	    errorMsg("Essa fonte de dados ainda não foi indexada. Faça a indexação no menu de Utilitário.");
	} catch (Exception e) {
	    errorMsg(e);
	} finally {
	}
    }

    public void setItens(ScoreDoc[] itens) {
	this.itens = itens;
    }

    public ScoreDoc[] getItens() {
	return itens;
    }

    public Document doc(int doc) {
	try {
	    return getBuscador().doc(doc);
	} catch (IOException e) {
	    logger.error(e);
	}
	return null;
    }

    public String visualizarDetalhe() {
	try {
	    // solucao tosca para recuperar o id do documento
	    String scoreDoc = null;// JSFUtils.getParametro("scoreDoc");
	    Pattern p = Pattern.compile("doc.(\\d+).*");
	    Matcher matcher = p.matcher(scoreDoc);
	    if (matcher.find()) {
		String id = matcher.group(1);
		documento = doc(new Integer(id));
	    }
	} catch (Exception e) {
	    logger.error(e);
	}
	return "detalhar";
    }

    public String mostraDados(ScoreDoc doc) {
	StringBuilder saida = new StringBuilder();
	try {
	    Document documento = doc(doc.doc);
	    // Arquivo no disco
	    if (fonte.getMetadados() == null
		    || fonte.getMetadados().size() == 0) {
		String id = documento.get("ID");
		String resultado = id == null ? "" : "[" + id + "] - ";
		resultado += limitaTamanho(documento.get("TextoCompleto.hl"));
		return resultado;
	    }
	    // Registro do banco
	    for (MetaDado metadado : fonte.getMetadados()) {
		String campo = metadado.getCampo().toUpperCase();
		String conteudo = documento.get(campo);
		conteudo = limitaTamanho(conteudo);
		saida.append("[");
		saida.append(campo);
		saida.append(": ");
		saida.append(conteudo);
		saida.append("] ");
	    }
	} catch (Exception e) {
	    saida.append("Erro: " + e);
	}
	return saida.toString();
    }

    private String limitaTamanho(String conteudo) {
	if (conteudo != null
		&& conteudo.length() > QUANTIDADE_CARACTERES_VISUALIZACAO) {
	    conteudo = conteudo
		    .substring(0, QUANTIDADE_CARACTERES_VISUALIZACAO)
		    + " (...)";
	}
	return conteudo;
    }

    public void setIdFonteDados(int idFonteDados) {
	this.idFonteDados = idFonteDados;
    }

    public int getIdFonteDados() {
	return idFonteDados;
    }

    public FonteDados getFonte() {
	return fonte;
    }

    public Document getDocumento() {
	return documento;
    }

    public String abrirPaginaBusca() {
	return "consultar";
    }

    public static void main(String[] args) {
	Pattern p = Pattern.compile("doc.(\\d*).*");
	Matcher matcher = p.matcher("doc=1052 score=0.43030652");
	// System.out.println(matcher.find());
	System.out.println(matcher.group(1));
    }

    public boolean caminhoArquivo(String valor) {
	return new File(valor).exists();
    }

    public void download(String fileName) {
	try {
	    HttpServletResponse response = null;// getResponse();
	    File arquivo = new File(fileName);
	    response.addHeader("Content-Disposition:", "attachment; filename="
		    + arquivo.getName());
	    OutputStream out = response.getOutputStream();
	    InputStream input = new FileInputStream(fileName);
	    IOUtils.copy(input, out);
	    out.flush();
	    out.close();
	    // getFacesContext().responseComplete();
	} catch (Exception e) {
	    // errorMsg("erro.generico",
	    // "Nï¿½o foi possï¿½vel baixar o arquivo. Erro Java: " + e);
	    logger.error(e);
	}
    }

    public String formataTexto(String texto) {
	return texto.replaceAll("\n", "<br />");
    }
}
