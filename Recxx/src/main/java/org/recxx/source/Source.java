package org.recxx.source;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.recxx.domain.Column;
import org.recxx.domain.Key;

public interface Source<K> extends Callable<Source<Key>> {

	public abstract String getAlias();
	
	public abstract Set<K> getKeySet();

	public abstract List<?> getRow(K key);

	public abstract List<Column> getColumns();
	
	public abstract List<String> getKeyColumns();
	
	public abstract List<String> getCompareColumns();

}