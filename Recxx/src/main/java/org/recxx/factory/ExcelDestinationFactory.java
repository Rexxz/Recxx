package org.recxx.factory;

import org.apache.commons.beanutils.ConstructorUtils;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.destination.Destination;
import org.recxx.domain.ExcelFileMetaData;

public class ExcelDestinationFactory implements DestinationFactory {

	public Destination getDestination(Class<?> destinationClass, RecxxConfiguration configuration, String alias) {

		ExcelFileMetaData fileMetaData = new ExcelFileMetaData.Builder()
			.filePath(configuration.configureFilePath(alias, false))
			.build();

		try {
			return (Destination) ConstructorUtils.invokeConstructor(destinationClass, new Object[] {fileMetaData});
		} catch (Exception e) {
			throw new IllegalArgumentException("'" + alias + ".* in configuration " +
			"generated the following fileMetaData '" + fileMetaData + "'", e);
		}
	}

}
