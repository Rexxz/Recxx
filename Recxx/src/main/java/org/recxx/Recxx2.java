package org.recxx;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.recxx.configuration.RecxxConfiguration;
import org.recxx.destination.Destination;

/**
 * Supported only for backwards compatibility, and wraps {@link Recxx} internally.
 *
 * @deprecated use {@link Recxx} instead.  
 */
@Deprecated
public class Recxx2 {
	
	public void execute(String filePath) throws Exception {
		new Recxx().execute(filePath);
	}

	public void execute(String filePath, String alias, String configName) throws Exception {
		new Recxx().execute(filePath, alias, configName);
	}

	public void execute(Map <String,String> configMap, String alias, String configName) throws Exception {
		new Recxx().execute(configMap, alias, configName);
	}

	public void execute(DataSource dataSource, String configName) throws Exception {
		new Recxx().execute(dataSource, configName);
	}
	
	public List<Destination> execute(RecxxConfiguration configuration) throws Exception {
		return new Recxx().execute(configuration);
    }

    public static void main(String args[]) {
    	Recxx.main(args);
    }
}
