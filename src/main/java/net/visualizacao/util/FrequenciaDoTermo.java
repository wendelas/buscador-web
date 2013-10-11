package net.visualizacao.util;

import java.io.Serializable;

public class FrequenciaDoTermo implements Comparable, Serializable {
	private String termo;
	private int frequencia;

	public FrequenciaDoTermo(String termo, int frequencia) {
		this.termo = termo;
		this.frequencia = frequencia;
	}

	public int compareTo(Object o) {
		// if (o instanceof FrequenciaDoTermo) {
		// FrequenciaDoTermo oFreq = (FrequenciaDoTermo) o;
		// return new CompareToBuilder().append(frequencia, oFreq.frequencia)
		// .append(termo, oFreq.termo).toComparison();
		// } else {
		return 0;
		// }
	}

	public String getTermo() {
		return termo;
	}

	public int getFrequencia() {
		return frequencia;
	}
}