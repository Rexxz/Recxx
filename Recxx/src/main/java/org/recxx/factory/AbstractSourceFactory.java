package org.recxx.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.Key;
import org.recxx.source.CachedFileSource;
import org.recxx.source.DatabaseSource;
import org.recxx.source.ExcelSource;
import org.recxx.source.RandomAccessFileSource;
import org.recxx.source.Source;

public class AbstractSourceFactory {

	private static final int INVALID_EXCEL_SOURCE_COUNT = 1;
	private static final int VALID_SOURCE_COUNT = 2;
	
	private Map<Class<?>, SourceFactory> sourceFactoryMap = new HashMap<Class<?>, SourceFactory>();	

	public AbstractSourceFactory() {
		addSourceFactory(CachedFileSource.class, new FileSourceFactory());
		addSourceFactory(RandomAccessFileSource.class, new FileSourceFactory());
		addSourceFactory(DatabaseSource.class, new DatabaseSourceFactory());
		addSourceFactory(ExcelSource.class, new ExcelSourceFactory());
	}

	public List<Source<Key>> getSources(RecxxConfiguration configuration) {
		List<Source<Key>> sources = new ArrayList<Source<Key>>();
		int sourceCount = 0;
		int excelSourceCount = 0;
		List<String> sourceAliases = configuration.configureSourceAliases();
		for (String alias : sourceAliases) {
			String sourceType = configuration.configureSourceType(alias, sourceFactoryMap);
			Class<?> sourceClass;
			try {
				sourceClass = Class.forName(sourceType);
				if (sourceFactoryMap.containsKey(sourceClass)) {
					SourceFactory sourceFactory = sourceFactoryMap.get(sourceClass);
					sources.add(sourceFactory.getSource(alias, sourceClass, configuration));
					if (sourceClass.equals(ExcelSource.class)) excelSourceCount++;
				}
				else {
					throw new IllegalArgumentException("'" + alias + ".type' specified incorrectly in configuration, " +
							"configuration requires one of the following values: " + sourceFactoryMap.keySet() + ". " +
							sourceType + " has not been added to the AbstractSourceFactory, this can be achieved by calling " +
							"addSourceFactory(Class<?> source, SourceFactory sourceFactory)");
				}
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("'" + alias + ".type' specified incorrectly in configuration, " +
						"configuration requires one of the following values: " + sourceFactoryMap.keySet(), e);
			} 
			sourceCount++;
		}
		if (excelSourceCount == INVALID_EXCEL_SOURCE_COUNT) {
			throw new IllegalArgumentException("Excel sources can only be compared with other Excel sources, please correct the configuration");
		}
		if (sourceCount != VALID_SOURCE_COUNT) {
			throw new IllegalArgumentException("Recxx only supports two sources currently, maybe in a later release this could be expanded ;)");
		}
		return sources;
	}

	public void addSourceFactory (Class<?> sourceClass, SourceFactory sourceFactory) {
		sourceFactoryMap.put(sourceClass, sourceFactory);
	}
	
}
