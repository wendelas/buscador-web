package net.indexador.filtro;

import org.apache.log4j.Logger;

import net.indexador.entidades.FonteDados;
import net.indexador.negocio.FachadaBuscador;
import net.utilitarios.ExcecaoIndexador;

public class ProcessoIndexador extends Thread {
	private static final Logger logger = Logger
			.getLogger(IndexadorListener.class);
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
