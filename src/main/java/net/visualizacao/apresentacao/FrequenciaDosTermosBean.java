package net.visualizacao.apresentacao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.indexador.entidades.FonteDados;
import net.indexador.negocio.FachadaBuscador;
import net.visualizacao.util.FrequenciaDoTermo;

import org.apache.log4j.Logger;

public class FrequenciaDosTermosBean extends BaseBean {
    private static final long serialVersionUID = 6837431655685273498L;
    protected static final Logger logger = Logger
	    .getLogger(FrequenciaDosTermosBean.class);
    private Integer limite = 1;
    private Integer idFonteDados;
    private FonteDados fonte;
    private VOMetaDados metaDados;
    private List<FrequenciaDoTermo> termos;

    public void setLimite(Integer limite) {
	this.limite = limite;
    }

    public Integer getLimite() {
	return limite;
    }

    public Collection<String> getCamposSelecionados() {
	try {
	    metaDados = FachadaBuscador.getInstancia().buscarMetaData(fonte);
	    return metaDados.getColunas();
	} catch (Exception e) {
	    // Fonte de dados eh um diretorio
	}
	Collection<String> listaCamposArquivo = new ArrayList<String>();
	listaCamposArquivo.add("Texto");
	return listaCamposArquivo;
    }

    public void carregarDados() {
	fonte = FachadaBuscador.getInstancia().buscarFontePeloId(
		getIdFonteDados());
	UtilFrequenciaDosTermos util = new UtilFrequenciaDosTermos(
		fonte.getDiretorioIndice());
	try {
	    List<FrequenciaDoTermo> termList = util.getFrequencia(getLimite(),
		    getCamposSelecionados());
	    setTermos(termList);
	} catch (Exception e) {
	    errorMsg(e);
	    logger.error(e);
	}
    }

    public void setTermos(List<FrequenciaDoTermo> termList) {
	this.termos = termList;
    }

    public List<FrequenciaDoTermo> getTermos() {
	return termos;
    }

    public Integer getTotalAcordaosIndexados() {
	return 1;// new UtilBusca().getTotalDocumentosIndexados();
    }

    public Integer getTotalTermosIndexados() {
	return 1;// new UtilBusca("").getTotalDocumentosIndexados();
    }

    public void setFonte(FonteDados fonte) {
	this.fonte = fonte;
    }

    public FonteDados getFonte() {
	return fonte;
    }

    public void setMetaDados(VOMetaDados metaDados) {
	this.metaDados = metaDados;
    }

    public VOMetaDados getMetaDados() {
	return metaDados;
    }

    public void setIdFonteDados(Integer idFonteDados) {
	this.idFonteDados = idFonteDados;
    }

    public Integer getIdFonteDados() {
	return idFonteDados;
    }
}
