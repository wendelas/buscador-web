package net.visualizacao.apresentacao;

import java.io.*;
import java.math.*;
import java.util.*;

import javax.persistence.*;

import net.indexador.entidades.*;
import net.indexador.negocio.*;
import net.utilitarios.*;

import org.ajax4jsf.model.*;
import org.apache.lucene.document.*;
import org.apache.lucene.search.*;

@KeepAlive
public class BuscaLivreBean extends BaseBean {
  private static final long serialVersionUID = -7508553590263034662L;
  private String consulta;
  private long duracaoBusca;
  private ScoreDoc[] itens;
  private int idFonteDados;
  private FonteDados fonte;

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

  public String[] getCamposSelecionados() {
    return new String[] {"Texto"};
  }

  public void consultar() {
    try {
      fonte = FachadaBuscador.getInstancia().buscarFontePeloId(
          getIdFonteDados());
      UtilBusca buscador = new UtilBusca(fonte.getDiretorioIndice());
      TopDocs hits = buscador.busca(getCamposSelecionados(), getConsulta());
      setItens(hits.scoreDocs);
      duracaoBusca = buscador.getDuracaoBusca();
      infoMsg("mensagem", duracaoBusca + "ms");
    } catch (Exception e) {
      errorMsg(Constantes.ERRO_BUSCA_LUCENE, e);
      logger.error(e);
    }
  }

  public Collection<String> getCamposDisponiveis() {
    return UtilBusca.getCampos();
  }

  public void setItens(ScoreDoc[] itens) {
    this.itens = itens;
  }

  public ScoreDoc[] getItens() {
    return itens;
  }

  public Document doc(ScoreDoc doc) {
    try {
      UtilBusca buscador = new UtilBusca(getFonte().getDiretorioIndice());
      return buscador.doc(doc.doc);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String mostraDados(ScoreDoc doc) {
    StringBuilder saida = new StringBuilder();
    Document documento = doc(doc);
    String conteudo = documento.get("Texto");
    if (conteudo.length() > 400) {
      conteudo = conteudo.substring(0, 400) + " (...)";
    }
    //    conteudo = conteudo.replaceAll("\n", "<br />");
    saida.append(conteudo);
    return saida.toString();
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
}
