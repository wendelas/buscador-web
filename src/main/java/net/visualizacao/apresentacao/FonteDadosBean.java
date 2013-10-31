package net.visualizacao.apresentacao;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import net.indexador.entidades.AnexoFonteDados;
import net.indexador.entidades.FonteDados;
import net.indexador.negocio.ExcecaoImportador;
import net.indexador.negocio.FachadaBuscador;
import net.visualizacao.util.StringUtils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 * 
 * @author marcoreis
 * 
 */
@ManagedBean
@RequestScoped
public class FonteDadosBean extends BaseBean {
    private static Logger logger = Logger.getLogger(FonteDadosBean.class);
    private static final long serialVersionUID = -7768860623840391492L;
    private FonteDados fonteDados;
    private UploadedFile arquivo;
    private VOMetaDados metaDados;
    private DataModel dadosVisualizacao;
    private String nome;
    private Collection<FonteDados> fontes;
    private Collection<AnexoFonteDados> anexos;
    private int idFonteDados;
    private String separador;
    private String dicionario;

    public String indexar() {
	try {
	    int qtd = FachadaBuscador.getInstancia().indexar(getIdFonteDados());
	    String msg = "Indexação concluída com sucesso. Foram indexados "
		    + qtd + " itens.";
	    infoMsg(msg);
	} catch (Exception e) {
	    errorMsg(e);
	}
	return "";
    }

    public void setIdFonteDados(int idFonteDados) {
	this.idFonteDados = idFonteDados;
    }

    public int getIdFonteDados() {
	return idFonteDados;
    }

    public void setNome(String nome) {
	this.nome = nome;
    }

    public String getNome() {
	return nome;
    }

    @PostConstruct
    public void inicializa() {
	carregarFontes();
	fonteDados = new FonteDados();
    }

    private void carregarFontes() {
	fontes = FachadaBuscador.getInstancia().buscarFontes();
    }

    public void carregarAnexos() {
	try {
	    anexos = FachadaBuscador.getInstancia().buscarAnexos(
		    getFonteDados().getId());
	} catch (Exception e) {
	    logger.error(e);
	}

    }

    public Collection<FonteDados> getFontes() {
	return fontes;
    }

    public void setFonteDados(FonteDados fonteDados) {
	this.fonteDados = fonteDados;
    }

    public FonteDados getFonteDados() {
	return fonteDados;
    }

    public void upload() {
	try {
	    AnexoFonteDados anexo = new AnexoFonteDados();
	    byte[] bytes = IOUtils.toByteArray(getArquivo().getInputstream());
	    anexo.setAnexo(bytes);
	    FachadaBuscador.getInstancia().persistir(getFonteDados());
	    FachadaBuscador.getInstancia().persistir(anexo,
		    getFonteDados().getId());
	    infoMsg("Arquivo gravado com sucesso");
	} catch (Exception e) {
	    errorMsg(e);
	}

    }

    public void salvar() {
	try {
	    if (getDicionario() != null) {
		getFonteDados().setDicionario(dicionario.getBytes());
	    }
	    FachadaBuscador.getInstancia().persistir(getFonteDados());
	    if (getArquivo() != null) {
		salvarAnexo();
	    }
	    infoMsg("Fonte de dados gravada com sucesso");
	    carregarFontes();
	} catch (Exception e) {
	    errorMsg(e);
	}
    }

    private boolean validar() {
	// somente diretorio de arquivos
	if (!StringUtils.vazia(getFonteDados().getDiretorio())) {
	    if (!StringUtils.vazia(getFonteDados().getUrl())) {
		String msg = "Não é permitido informar um diretório e uma base de dados para a mesma fonte. "
			+ "Por favor, escolha apenas uma opção de fonte.";
		errorMsg(msg);
	    }
	    // somente base de dados
	} else {
	    if (StringUtils.vazia(getFonteDados().getQuery())) {
		errorMsg("A query é obrigatória");
	    }
	    if (StringUtils.vazia(getFonteDados().getNome())) {
		errorMsg("O nome da fonte é um campo obrigatório");
	    }
	    if (StringUtils.vazia(getFonteDados().getNomeDriver())) {
		errorMsg("o nome do driver é um campo obrigatório");
	    }
	    if (StringUtils.vazia(getFonteDados().getUrl())) {
		errorMsg("A url é obrigatória");
	    }
	    if (StringUtils.vazia(getFonteDados().getUsuario())) {
		errorMsg("O usuário é um campo obrigatório");
	    }
	}
	return !possuiErros();
    }

    public void setDadosVisualizacao(DataModel dadosVisualizacao) {
	this.dadosVisualizacao = dadosVisualizacao;
    }

    public void carregarDadosVisualizacao() {
	try {
	    carregarDadosDiretorio();
	    carregarDadosBanco();
	} catch (Exception e) {
	    String msg = "Não foi possível carregar os dados para visualização. Erro Java: "
		    + e.getMessage();
	    errorMsg(msg);
	    logger.error(e);
	}
    }

    private void carregarDadosDiretorio() {
	if (StringUtils.vazia(getFonteDados().getDiretorio())) {
	}
    }

    private void carregarDadosBanco() throws ExcecaoImportador {
	metaDados = FachadaBuscador.getInstancia().buscarMetaData(
		getFonteDados());
	dadosVisualizacao = new ListDataModel(metaDados.getColunas());
    }

    public DataModel getDadosVisualizacao() {
	return dadosVisualizacao;
    }

    public void setMetaDados(VOMetaDados metaDados) {
	this.metaDados = metaDados;
    }

    public VOMetaDados getMetaDados() {
	return metaDados;
    }

    public String editar() {
	try {
	    metaDados = new VOMetaDados();
	    fonteDados = FachadaBuscador.getInstancia().buscarFontePeloId(
		    getFonteDados().getId());
	    carregarAnexos();
	} catch (Exception e) {
	    logger.error(e);
	}
	return "FonteDados.jsf";
    }

    public String novo() {
	inicializa();
	return "FonteDados";
    }

    public void excluir() {
	try {
	    FachadaBuscador.getInstancia().excluirFonteDados(
		    getFonteDados().getId());
	    infoMsg("Fonte excluída com sucesso");
	} catch (Exception e) {
	    errorMsg(e);
	}
	carregarFontes();
    }

    private void salvarAnexo() throws IOException {
	AnexoFonteDados anexo = new AnexoFonteDados();
	byte[] bytes = IOUtils.toByteArray(getArquivo().getInputstream());
	anexo.setNomeArquivo(getArquivo().getFileName());
	anexo.setTamanho(getArquivo().getSize());
	anexo.setAnexo(bytes);
	anexo.setFonteDados(getFonteDados());
	anexo.setDataEnvio(new Timestamp(System.currentTimeMillis()));
	anexo.setSeparador(getSeparador());
	FachadaBuscador.getInstancia()
		.persistir(anexo, getFonteDados().getId());
	carregarAnexos();
    }

    // O Firefox está travando ao usar o upload Advanced
    public void handleFileUpload(FileUploadEvent event) {
	try {
	    FachadaBuscador.getInstancia().persistir(getFonteDados());
	    infoMsg("Arquivo " + event.getFile().getFileName()
		    + " enviado com sucesso.");
	} catch (Exception e) {
	    errorMsg(e);
	}
    }

    public String getMensagemAjudaNomeDriver() {
	String msg = "A aplicação já está com os drivers do MySQL (com.mysql.jdbc.Driver), "
		+ "PostgreSQL(???), "
		+ "Oracle (???), "
		+ "MSSQLSERVER (???). "
		+ "Para registrar um novo driver, apenas copie o arquivo.jar em tomcat/metabusca/web/lib.";
	return msg;
    }

    public String getMensagemQuery() {
	String msg = "A query pode ser de uma ou várias tabelas, contendo todos os tipos de dados, inclusive lob. "
		+ "O campo lob pode conter texto ou documentos (PDF, DOC, DOCX, XLS, PPT). "
		+ "A query deve conter uma coluna 'ID'. "
		+ "Datas devem ser utilizadas no formato americano (yyyy-MM-dd HH:mm:ss). ";
	return msg;
    }

    public String getMensagemNomeFonte() {
	String msg = "Utilize nomes que identifiquem facilmente o tipo de informação. "
		+ "Exemplo: diretorio-documentos-local, tabela-dados-usuario, tabela-vendas, tabela-historico-compras. ";
	return msg;
    }

    public String getMensagemFonteDados() {
	StringBuilder msg = new StringBuilder();
	msg.append("<p>Uma fonte de dados pode ser um arquivo CSV, um binário (PDF, XLS, DOC, PPT, RTF, ZIP) [um XML ou um JSON (not implemented)]. </p>");
	msg.append("<p>Na versão de produção, a fonte de dados pode ser um de banco de dados relacional (SQL Server, Oracle, PostgreSQL, MySQL)");
	msg.append(", um diretório local no servidor ou um mapeamento NFS.</p>");
	// msg.append("<li>Para indexar um diretório, preencha apenas o campo 'Diretório de Documentos' e deixe os demais campos em branco.</li>");
	// msg.append("<li>Para indexar uma query, preencha os dados de conexão e depois o botão 'Visualizar' para validar a conexão/query. ");
	msg.append("<p>É essencial que a fonte de dados tenha o campo 'ID'. No caso de um arquivo binário, o ID será o próprio nome.</p>");
	// msg.append("<li>Não é possível conter as duas opções pois a busca ainda não suporta fontes diferentes.</li>");
	return msg.toString();
    }

    public void setArquivo(UploadedFile arquivo) {
	this.arquivo = arquivo;
    }

    public UploadedFile getArquivo() {
	return arquivo;
    }

    public Collection<AnexoFonteDados> getAnexos() {
	return anexos;
    }

    public void setSeparador(String separador) {
	this.separador = separador;
    }

    public String getSeparador() {
	return separador;
    }

    public void setDicionario(String dicionario) {
	this.dicionario = dicionario;
    }

    public void carregarDicionario() {
	fonteDados = FachadaBuscador.getInstancia().buscarFontePeloId(
		getIdFonteDados());
	if (getFonteDados().getDicionario() != null) {
	    setDicionario(new String(getFonteDados().getDicionario()));
	} else {
	    setDicionario("");
	}
    }

    public String getDicionario() {
	return dicionario;
    }
}
