package org.recxx.factory;

import org.apache.commons.beanutils.ConstructorUtils;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.ExcelFileMetaData;
import org.recxx.domain.Key;
import org.recxx.source.Source;

public class ExcelSourceFactory implements SourceFactory {

	@SuppressWarnings("unchecked")
	public Source<Key> getSource(String alias, Class<?> sourceClass, RecxxConfiguration configuration) {

		ExcelFileMetaData fileMetaData = new ExcelFileMetaData.Builder()
			.filePath(configuration.configureFilePathCheckExists(alias))
			.formatComparison(configuration.configureFormatComparison(alias))
			.omitSheets(configuration.configureOmitSheets(alias))
			.build();		

		try {
			return (Source<Key>) ConstructorUtils.invokeConstructor(sourceClass, new Object[] { alias, fileMetaData });
		} catch (Exception e) {
			throw new IllegalArgumentException("'" + alias + ".* in configuration generated the " +
					"following FileMetaData '" + fileMetaData + "'", e);
		}
	}

}
