package org.recxx.factory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConstructorUtils;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;
import org.recxx.source.Source;

public class FileSourceFactory implements SourceFactory {

	private Map<String, Class<?>> classAbbreviationMap = new HashMap<String, Class<?>>();	
	
	public FileSourceFactory() {
		classAbbreviationMap.put("String", String.class);
		classAbbreviationMap.put("Double", Double.class);
		classAbbreviationMap.put("Integer", Integer.class);
		classAbbreviationMap.put("Long", Long.class);
		classAbbreviationMap.put("Date", Date.class);
	}
	
	@SuppressWarnings("unchecked")
	public Source<Key> getSource(String alias, Class<?> sourceClass, RecxxConfiguration configuration) {

		FileMetaData fileMetaData = new FileMetaData.Builder()
										.filePath(configuration.configureFilePathCheckExists(alias))
										.keyColumns(configuration.configureKeyColumns(alias))
										.columns(configuration.configureColumns(alias, classAbbreviationMap))
										.delimiter(configuration.configureDelimiter(alias))
										.lineDelimiter(configuration.configureLineDelimiter(alias))
										.ignoreHeaderRow(configuration.configureIgnoreHeaderRow(alias))
										.columnsToCompare(configuration.configureColumnsToCompare(alias))
										.build();		
		
		try {
			return (Source<Key>) ConstructorUtils.invokeConstructor(sourceClass, new Object[] {alias, fileMetaData});
		} catch (Exception e) {
			throw new IllegalArgumentException("'" + alias + ".* in configuration " +
					"generated the following FileMetaData '" + fileMetaData + "'", e);
		}
	}

}
