package net.visualizacao.util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class UtilFrequenciaDosTermos {
	private String diretorioIndice;

	public UtilFrequenciaDosTermos(String diretorioIndice) {
		this.diretorioIndice = diretorioIndice;
	}

	public static void main(String[] pArgs) throws Exception {
		UtilFrequenciaDosTermos util = new UtilFrequenciaDosTermos("");
		String limite = JOptionPane.showInputDialog("Limite");
		System.out.println("Threshold (limite): " + limite);
		Integer threshold = new Integer(limite);
		//
		Collection<String> campos = new ArrayList<String>();
		campos.add("TermoIndexacao");
		List<FrequenciaDoTermo> termList = util.getFrequencia(threshold, campos);
		//
		System.out.println("Frequencia | Termo");
		Iterator<FrequenciaDoTermo> iterator = termList.iterator();
		while (iterator.hasNext()) {
			FrequenciaDoTermo freq = (FrequenciaDoTermo) iterator.next();
			System.out.print(freq.getFrequencia());
			System.out.println(" | " + freq.getTermo());
		}
	}

	public List<FrequenciaDoTermo> getFrequencia(Integer threshold, Collection<String> campos) throws IOException {
		IndexReader reader = null;
		try {
			// reader =
			// IndexReader.open(FSDirectory.open(Paths.get(diretorioIndice)));
			// TermEnum enumeracao = reader.terms();
			// List<FrequenciaDoTermo> termList = new
			// ArrayList<FrequenciaDoTermo>();
			// while (enumeracao.next()) {
			// if (enumeracao.docFreq() >= threshold.intValue() &&
			// campos.contains(enumeracao.term().field())) {
			// FrequenciaDoTermo freq = new FrequenciaDoTermo(enumeracao.term()
			// .text(), enumeracao.docFreq());
			// termList.add(freq);
			// }
			// }
			// Collections.sort(termList);
			// Collections.reverse(termList);
			// return termList;
			return null;
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
	}
}
