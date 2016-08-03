package net.visualizacao.util;

import java.text.*;
import java.util.*;

public final class DateUtils {
	public static final String FORMATO_AMERICANO_COMPLETO = "yyyy-MM-dd HH:mm:ss";
	private static final SimpleDateFormat sdf;

	private DateUtils() {
		super();
	}

	static {
		sdf = new SimpleDateFormat();
		sdf.setLenient(false);
	}

	public static String getDateFormatoAmericano(Date d) {
		sdf.applyPattern(FORMATO_AMERICANO_COMPLETO);
		return sdf.format(d);
	}

	public static String getYear(Date d) {
		sdf.applyPattern("yyyy");
		return sdf.format(d);
	}
}
