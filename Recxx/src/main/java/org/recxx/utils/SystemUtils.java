package org.recxx.utils;

import java.math.BigDecimal;

import org.apache.commons.beanutils.ConvertUtilsBean;

public class SystemUtils {

	public static final String ENV_VARIABLE_START = "${";
	public static final String ENV_VARIABLE_END = "}";

	public static String replaceSystemProperties(String string) {
		while (string != null && string.contains(ENV_VARIABLE_START) && string.contains(ENV_VARIABLE_END)) {
			string = replaceSystemProperty(string);
		}
		return string;
	}

	private static String replaceSystemProperty(String property) {
		String systemPropertyName = property.substring(property.indexOf(ENV_VARIABLE_START) + 2, property.indexOf(ENV_VARIABLE_END));
		String systemPropertyRequired = System.getProperty(systemPropertyName);
		if (systemPropertyRequired == null) throw new RuntimeException("Property " + systemPropertyName + " not set, please set VM Arguments with -D" + systemPropertyName + "=xxxxxxxx");
		return property.replace(ENV_VARIABLE_START + systemPropertyName + ENV_VARIABLE_END, systemPropertyRequired);
	}
	
	public static BigDecimal memoryUsed() {
		ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
		BigDecimal used = (BigDecimal)convertUtilsBean.convert(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(), BigDecimal.class);
		BigDecimal max = (BigDecimal)convertUtilsBean.convert(Runtime.getRuntime().maxMemory(), BigDecimal.class);
		return used.divide(max, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));		
	}

	
	
}
