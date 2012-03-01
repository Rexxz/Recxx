package org.recxx.factory;

import org.apache.commons.beanutils.ConstructorUtils;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.destination.Destination;
import org.recxx.domain.ConsoleMetaData;

public class ConsoleDestinationFactory implements DestinationFactory {

	public Destination getDestination(Class<?> destinationClass, RecxxConfiguration configuration, String alias) {

		ConsoleMetaData consoleMetaData = new ConsoleMetaData.Builder()
			.delimiter(configuration.configureDelimiter(alias))
			.lineDelimiter(configuration.configureLineDelimiter(alias))
			.dateFormats(configuration.configureDateFormats(alias))
			.build();		

		try {
			return (Destination) ConstructorUtils.invokeConstructor(destinationClass, new Object[] {consoleMetaData});
		} catch (Exception e) {
			throw new IllegalArgumentException("'" + alias + ".* in configuration " +
				"generated the following ConsoleMetaData '" + consoleMetaData + "'", e);
		}
	}	

}
