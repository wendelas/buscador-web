package net.indexador.filtro;

import net.indexador.entidades.FonteDados;
import net.indexador.negocio.FachadaBuscador;
import net.visualizacao.util.ExcecaoIndexador;

import org.apache.log4j.Logger;

public class ProcessoIndexador extends Thread {
	private static final Logger logger = Logger.getLogger(IndexadorListener.class);
	private FonteDados fonte;

	public ProcessoIndexador(FonteDados fonte) {
		this.fonte = fonte;
	}

	public void run() {
		try {
			FachadaBuscador.getInstancia().indexar(fonte.getId());
		} catch (ExcecaoIndexador e) {
			logger.error(e);
		}
	}
}
