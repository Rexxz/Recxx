package org.recxx.source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;

public class CachedFileSource extends FileSource {

	private final Map<Key, List<?>> dataMap = new HashMap<Key, List<?>>();

	public CachedFileSource(String alias, FileMetaData metaData) {
		super(alias, metaData);
	}

	public Set<Key> getKeySet() {
		return dataMap.keySet();
	}

	public List<?> getRow(Key key) {
		return dataMap.get(key);
	}

	@Override
	protected Map<Key, ?> getSourceDataMap() {
		return dataMap;
	}

	protected void addRow(Key key, List<?> fields, int start, int end) {
		dataMap.put(key, fields);
	}

}
