package net.visualizacao.apresentacao;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.log4j.Logger;

@ManagedBean
@RequestScoped
public class UtilitarioBean extends BaseBean {
	protected static final Logger logger = Logger.getLogger(UtilitarioBean.class);
	private static final long serialVersionUID = 5150692753297895526L;
	//
	private boolean importando = false;
	private int quantidadeRegistrosParaImportar = 20000;
	private int quantidadeRegistrosProcessados;
	private boolean forcarReindexacao;

	public boolean isImportando() {
		return importando;
	}

	public void setImportando(boolean importando) {
		this.importando = importando;
	}

	public int getQuantidadeRegistrosParaImportar() {
		return quantidadeRegistrosParaImportar;
	}

	public void setQuantidadeRegistrosParaImportar(int quantidadeRegistrosParaImportar) {
		this.quantidadeRegistrosParaImportar = quantidadeRegistrosParaImportar;
	}

	public int getQuantidadeRegistrosProcessados() {
		return quantidadeRegistrosProcessados;
	}

	public void setQuantidadeRegistrosProcessados(int quantidadeRegistrosProcessados) {
		this.quantidadeRegistrosProcessados = quantidadeRegistrosProcessados;
	}

	public boolean isForcarReindexacao() {
		return forcarReindexacao;
	}

	public void setForcarReindexacao(boolean forcarReindexacao) {
		this.forcarReindexacao = forcarReindexacao;
	}

}
