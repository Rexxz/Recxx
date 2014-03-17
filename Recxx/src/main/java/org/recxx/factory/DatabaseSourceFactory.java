package org.recxx.factory;

import org.apache.commons.beanutils.ConstructorUtils;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.DatabaseMetaData;
import org.recxx.domain.Key;
import org.recxx.source.Source;

public class DatabaseSourceFactory implements SourceFactory {

	@SuppressWarnings("unchecked")
	public Source<Key> getSource(String alias, Class<?> sourceClass, RecxxConfiguration configuration) {


		DatabaseMetaData databaseMetaData = new DatabaseMetaData.Builder()
										.databaseDriver(configuration.configureDatabaseDriver(alias))
										.databaseUrl(configuration.configureDatabaseUrl(alias))
										.databaseUserId(configuration.configureDatabaseUserId(alias))
										.databasePassword(configuration.configureDatabasePassword(alias))
										.sql(configuration.configureSql(alias))
										.filePath(configuration.configureFilePath(alias, false))
										.keyColumns(configuration.configureKeyColumns(alias))
										.columnsToCompare(configuration.configureColumnsToCompare(alias))
										.columnsToIgnore(configuration.configureColumnsToIgnore(alias))
										.dateFormats(configuration.configureDateFormats(alias))
										.build();

		try {
			return (Source<Key>) ConstructorUtils.invokeConstructor(sourceClass, new Object[] {alias, databaseMetaData});
		} catch (Exception e) {
			throw new IllegalArgumentException("'" + alias + ".* in configuration " +
					"generated the following databaseMetaData '" + databaseMetaData + "'", e);
		}
	}
}
