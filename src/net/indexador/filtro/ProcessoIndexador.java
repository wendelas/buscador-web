package net.indexador.filtro;

import net.indexador.entidades.*;
import net.indexador.negocio.*;
import net.utilitarios.indexador.*;

import org.apache.log4j.*;

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
