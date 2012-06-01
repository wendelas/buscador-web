package net.visualizacao.apresentacao;

import java.io.*;
import java.math.*;
import java.util.*;
import java.util.regex.*;

import net.indexador.entidades.*;
import net.indexador.negocio.*;
import net.utilitarios.*;
import net.visualizacao.util.*;

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
  private VOMetaDados metaDados;
  private Document documento;

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
      metaDados = FachadaBuscador.getInstancia().buscarMetaData(fonte);
      return metaDados.getColunas();
    } catch (Exception e) {
      logger.error(e);
    }
    Collection<String> listaCamposArquivo = new ArrayList<String>();
    listaCamposArquivo.add("Texto");
    return listaCamposArquivo;
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
    return doc(doc.doc);
  }

  public Document doc(int doc) {
    try {
      UtilBusca buscador = new UtilBusca(getFonte().getDiretorioIndice());
      return buscador.doc(doc);
    } catch (IOException e) {
      logger.error(e);
    }
    return null;
  }

  public String visualizarDetalhe() {
    try {
      //solucao tosca para recuperar o id do documento
      String scoreDoc = JSFUtils.getParametro("scoreDoc");
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
      Document documento = doc(doc);
      //Arquivo no disco
      if (metaDados == null) {
        return limitaTamanho(documento.get("Texto"));
      }
      //Registro do banco
      for (String campo : metaDados.getColunas()) {
        String conteudo = documento.get(campo);
        conteudo = limitaTamanho(conteudo);
        saida.append(conteudo + " - ");
      }
    } catch (Exception e) {
      saida.append("Erro: " + e);
    }
    //    conteudo = conteudo.replaceAll("\n", "<br />");
    return saida.toString();
  }

  private String limitaTamanho(String conteudo) {
    if (conteudo != null && conteudo.length() > 400) {
      conteudo = conteudo.substring(0, 400) + " (...)";
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
    //    System.out.println(matcher.find());
    System.out.println(matcher.group(1));
  }
}
