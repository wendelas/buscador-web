package net.visualizacao.apresentacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.TopDocs;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import net.indexador.entidades.FonteDados;
import net.indexador.negocio.FachadaBuscador;
import net.visualizacao.util.StringUtils;
import net.visualizacao.util.UtilBusca;

public class LuceneLazyDataModel extends LazyDataModel<Document> {

	private static final long serialVersionUID = 1153244287993412470L;
	private String q;
	private int idFonteDados;
	private long duracaoBusca;

	public LuceneLazyDataModel(String q, int idFonteDados) {
		this.q = q;
		this.idFonteDados = idFonteDados;
	}

	@Override
	public List<Document> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		long inicio = System.currentTimeMillis();
		if (StringUtils.vazia(String.valueOf(idFonteDados)) || StringUtils.vazia(q)) {
			return null;
		}
		FonteDados fonte = FachadaBuscador.getInstancia().buscarFontePeloId(idFonteDados);
		UtilBusca buscador = new UtilBusca(fonte.getDiretorioIndice());
		try {
			TopDocs hits = buscador.buscar(q);

			List<Document> lista = new ArrayList<Document>();
			for (int i = first; i < first + pageSize; i++) {
				if (i >= hits.totalHits) {
					break;
				}
				int idDoc = hits.scoreDocs[i].doc;
				Document documento = buscador.doc(idDoc);
				lista.add(documento);
			}
			setRowCount(hits.totalHits);
			duracaoBusca = System.currentTimeMillis() - inicio;
			return lista;
		} catch (Exception e) {
			throw new RuntimeException(e);

		} finally {
			buscador.fechar();
		}
	}

	public long getDuracaoBusca() {
		return duracaoBusca;
	}

}
