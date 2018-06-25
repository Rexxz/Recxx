package org.recxx.factory;

import org.apache.commons.beanutils.ConstructorUtils;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.destination.Destination;
import org.recxx.domain.DatabaseMetaData;

public class DatabaseDestinationFactory implements DestinationFactory {

	public Destination getDestination(Class<?> destinationClass, RecxxConfiguration configuration, String alias) {

		DatabaseMetaData databaseMetaData = configuration.configureDatabaseMetaData(alias);

		try {
			return (Destination) ConstructorUtils.invokeConstructor(destinationClass, new Object[] {databaseMetaData});
		} catch (Exception e) {
			throw new IllegalArgumentException("'" + alias + ".* in configuration " +
			"generated the following databaseMetaData '" + databaseMetaData + "'", e);
		}
	}	

}
