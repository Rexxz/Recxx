package org.recxx.factory;

import org.apache.commons.beanutils.ConstructorUtils;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.destination.Destination;
import org.recxx.domain.FileMetaData;

public class CsvDestinationFactory implements DestinationFactory {

	public Destination getDestination(Class<?> destinationClass, RecxxConfiguration configuration, String alias) {

		FileMetaData fileMetaData = new FileMetaData.Builder()
			.filePath(configuration.configureFilePath(alias))
			.delimiter(configuration.configureDelimiter(alias))
			.lineDelimiter(configuration.configureLineDelimiter(alias))
			.dateFormats(configuration.configureDateFormats(alias))
			.build();		

		try {
			return (Destination) ConstructorUtils.invokeConstructor(destinationClass, new Object[] {fileMetaData});
		} catch (Exception e) {
			throw new IllegalArgumentException("'" + alias + "* in configuration " +
				"generated the following FileMetaData '" + fileMetaData + "'", e);
		}
	}
	
}
