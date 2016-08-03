package net.visualizacao.util;

import java.io.*;

public class FiltroArquivoSuportado implements FileFilter {
	public boolean accept(File file) {
		String name = file.getName();
		if (name.startsWith(".")) {
			return false;
		}
		if (!file.isFile()) {
			return true;
		}
		boolean retorno = name.toLowerCase().endsWith("pdf") || name.toLowerCase().endsWith("doc")
				|| name.toLowerCase().endsWith("docx") || name.toLowerCase().endsWith("xls")
				|| name.toLowerCase().endsWith("xlsx") || name.toLowerCase().endsWith("ppt")
				|| name.toLowerCase().endsWith("pptx");
		return retorno;
	}
}
