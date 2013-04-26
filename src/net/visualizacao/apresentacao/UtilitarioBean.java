package net.visualizacao.apresentacao;

import java.util.Collection;

import net.indexador.entidades.FonteDados;
import net.indexador.negocio.FachadaBuscador;

import org.apache.log4j.Logger;

public class UtilitarioBean extends BaseBean {
  protected static final Logger logger = Logger.getLogger(UtilitarioBean.class);
  private static final long serialVersionUID = 5150692753297895526L;
  //
  private boolean importando = false;
  private int quantidadeRegistrosParaImportar = 20000;
  private int quantidadeRegistrosProcessados;
  private boolean forcarReindexacao;
  private int idFonteDados;

  public String indexar() {
    try {
      int qtd = FachadaBuscador.getInstancia().indexar(getIdFonteDados());
      String msg = "Indexação concluída com sucesso. Foram indexados " + qtd
          + " itens.";
      infoMsg("mensagem", msg);
    } catch (Exception e) {
      errorMsg("erro.generico", e);
      logger.error(e);
    }
    return "";
  }

  public boolean isImportando() {
    return importando;
  }

  public void setImportando(boolean importando) {
    this.importando = importando;
  }

  public int getQuantidadeRegistrosParaImportar() {
    return quantidadeRegistrosParaImportar;
  }

  public void setQuantidadeRegistrosParaImportar(
      int quantidadeRegistrosParaImportar) {
    this.quantidadeRegistrosParaImportar = quantidadeRegistrosParaImportar;
  }

  public int getQuantidadeRegistrosProcessados() {
    return quantidadeRegistrosProcessados;
  }

  public void setQuantidadeRegistrosProcessados(
      int quantidadeRegistrosProcessados) {
    this.quantidadeRegistrosProcessados = quantidadeRegistrosProcessados;
  }

  public boolean isForcarReindexacao() {
    return forcarReindexacao;
  }

  public void setForcarReindexacao(boolean forcarReindexacao) {
    this.forcarReindexacao = forcarReindexacao;
  }

  public void setIdFonteDados(int idFonteDados) {
    this.idFonteDados = idFonteDados;
  }

  public int getIdFonteDados() {
    return idFonteDados;
  }

  public Collection<FonteDados> getFontes() {
    return FachadaBuscador.getInstancia().buscarFontes();
  }
}
