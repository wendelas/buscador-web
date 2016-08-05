package net.visualizacao.apresentacao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.faces.model.ListDataModel;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import net.indexador.entidades.FonteDados;
import net.indexador.negocio.FachadaBuscador;
import net.visualizacao.util.StringUtils;
import net.visualizacao.util.UtilBusca;

public class LuceneDataModel extends ListDataModel<Document> {

	public LuceneDataModel(String q, int idFonteDados) {
		if (StringUtils.vazia(String.valueOf(idFonteDados)) || StringUtils.vazia(q)) {
			return;
		}

		//

		FonteDados fonte = FachadaBuscador.getInstancia().buscarFontePeloId(idFonteDados);
		UtilBusca buscador = new UtilBusca(fonte.getDiretorioIndice());
		try {
			TopDocs hits = buscador.buscar(q);

			Collection<Document> lista = new ArrayList<Document>();
			for (ScoreDoc sc : hits.scoreDocs) {
				lista.add(buscador.doc(sc.doc));
			}
			setWrappedData(lista);
		} catch (Exception e) {
			throw new RuntimeException(e);

		} finally {
			buscador.fechar();
		}
	}

	public void consultar() {
		// if (StringUtils.vazia(getQ()) ||
		// StringUtils.vazia(String.valueOf(getIdFonteDados()))) {
		// return;
		// }
		// UtilBusca buscador = criarBuscador();
		// try {
		//
		// setItens(hits.scoreDocs);
		// totalHits = hits.totalHits;
		// // if (totalHits == 0) {
		// // setItens(null);
		// // }
		// duracaoBusca = buscador.getDuracaoBusca();
		// } catch (Exception e) {
		// errorMsg(e);
		// } finally {
		// buscador.fechar();
		// }
	}
}
