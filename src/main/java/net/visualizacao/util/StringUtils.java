package net.visualizacao.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class StringUtils {
	protected static final Logger logger = Logger.getLogger(StringUtils.class);
	public static String[] REPLACES = { "a", "e", "i", "o", "u", "c", "A", "E", "I", "O", "U", "C", "" };
	public static Pattern[] PATTERNS = null;

	private static void compilePatterns() {
		PATTERNS = new Pattern[REPLACES.length];
		PATTERNS = new Pattern[REPLACES.length];
		PATTERNS = new Pattern[REPLACES.length];
		PATTERNS[0] = Pattern.compile("[âãáàä]");
		PATTERNS[1] = Pattern.compile("[éèêë]");
		PATTERNS[2] = Pattern.compile("[íìîï]");
		PATTERNS[3] = Pattern.compile("[óòôõö]");
		PATTERNS[4] = Pattern.compile("[úùûü]");
		PATTERNS[5] = Pattern.compile("[ç]");
		PATTERNS[6] = Pattern.compile("[ÂÃÁÀÄ]");
		PATTERNS[7] = Pattern.compile("[ÉÈÊË]");
		PATTERNS[8] = Pattern.compile("[ÍÌÎÏ]");
		PATTERNS[9] = Pattern.compile("[ÓÒÔÕÖ]");
		PATTERNS[10] = Pattern.compile("[ÚÙÛÜ]");
		PATTERNS[11] = Pattern.compile("[Ç]");
		PATTERNS[12] = Pattern.compile("[^\\p{Print}]");
	}

	public static String limpacaracter(String str) {
		if ((str == null) || (str.length() < 1)) {
			return null;
		}
		if (PATTERNS == null) {
			compilePatterns();
		}
		String result = str;
		for (int i = 0; i < PATTERNS.length; i++) {
			Matcher matcher = PATTERNS[i].matcher(result);
			result = matcher.replaceAll(REPLACES[i]);
		}
		return result;
	}

	public static boolean vazia(String str) {
		return str == null || "".equals(str.trim());
	}

	public static String getCondicaoLike(String condicao) {
		return condicao == null ? "%"
				: "".equals(condicao.trim()) ? "%" : '%' + condicao.toLowerCase().replace('*', '%') + '%';
	}
}
