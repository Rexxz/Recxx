package org.recxx.factory;

import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.Key;
import org.recxx.source.Source;

public interface SourceFactory {
	
	Source<Key> getSource(String alias, Class<?> sourceClass, RecxxConfiguration configuration);
	
}
