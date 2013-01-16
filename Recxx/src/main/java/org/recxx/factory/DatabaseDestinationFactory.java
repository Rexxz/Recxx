package org.recxx.factory;

import org.apache.commons.beanutils.ConstructorUtils;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.destination.Destination;
import org.recxx.domain.DatabaseMetaData;
import org.recxx.domain.Default;

public class DatabaseDestinationFactory implements DestinationFactory {

	public Destination getDestination(Class<?> destinationClass, RecxxConfiguration configuration, String alias) {

		String overrideAlias = configuration.getString(Default.DATABASE_PREFIX);
		
		DatabaseMetaData databaseMetaData = new DatabaseMetaData.Builder()
			.databaseDriver(configuration.configureDatabaseDriver(overrideAlias, alias))
			.databaseUrl(configuration.configureDatabaseUrl(overrideAlias, alias))
			.databaseUserId(configuration.configureDatabaseUserId(overrideAlias, alias))
			.databasePassword(configuration.configureDatabasePassword(overrideAlias, alias))
			.build();

		try {
			return (Destination) ConstructorUtils.invokeConstructor(destinationClass, new Object[] {databaseMetaData});
		} catch (Exception e) {
			throw new IllegalArgumentException("'" + alias + ".* in configuration " +
			"generated the following databaseMetaData '" + databaseMetaData + "'", e);
		}
	}	

}
