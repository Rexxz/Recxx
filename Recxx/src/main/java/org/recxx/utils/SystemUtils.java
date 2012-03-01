package org.recxx.utils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.converters.BigDecimalConverter;

public class SystemUtils {

	public static final String ENV_VARIABLE_START = "${";
	public static final String ENV_VARIABLE_END = "}";

	public static String replaceSystemProperties(String property) {
		while (property != null && property.contains(SystemUtils.ENV_VARIABLE_START) && property.contains(SystemUtils.ENV_VARIABLE_END)) {
			property = replaceSystemProperty(property);
		}
		return property;
	}

	public static List<String> replaceSystemProperties(List<String> properties) {
		for (String property : properties) {
			property = SystemUtils.replaceSystemProperties(property);
		}
 		return properties;
	}
	
	private static String replaceSystemProperty(String property) {
		String systemPropertyName = property.substring(property.indexOf(ENV_VARIABLE_START) + 2, property.indexOf(ENV_VARIABLE_END));
		String systemPropertyRequired = System.getProperty(systemPropertyName);
		if (systemPropertyRequired == null) throw new RuntimeException("Property " + systemPropertyName + " not set, please set VM Arguments with -D" + systemPropertyName + "=xxxxxxxx");
		return property.replace(ENV_VARIABLE_START + systemPropertyName + ENV_VARIABLE_END, systemPropertyRequired);
	}
	
	public static BigDecimal memoryUsed() {
		BigDecimalConverter bdc = new BigDecimalConverter();
		BigDecimal use = (BigDecimal)bdc.convert(BigDecimal.class, Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		BigDecimal max = (BigDecimal)bdc.convert(BigDecimal.class, Runtime.getRuntime().maxMemory());
		return use.divide(max, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));		
	}

	
	
}
