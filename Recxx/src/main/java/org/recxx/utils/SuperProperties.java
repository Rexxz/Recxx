package org.recxx.utils;

import java.util.Properties;

@SuppressWarnings("serial")
public class SuperProperties extends Properties {
	
	@Override
	public String getProperty(String key) {
		
		return SystemUtils.replaceSystemProperties(super.getProperty(key));
			
	}
	
}
