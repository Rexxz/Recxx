package org.recxx.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;
import org.recxx.domain.Column;

public class CachedFileSource extends FileSource {
	
	private static Logger LOGGER = Logger.getLogger(CachedFileSource.class); 

	private final Map<Key, List<?>> dataMap = new HashMap<Key, List<?>>();
	
	public CachedFileSource(String name, FileMetaData metaData) {
		super(name, metaData);
	}
	
	public Source<Key> call() {
		StringBuilder line = new StringBuilder();
		boolean isFirstRow = true;
		
		LOGGER.info("Loading file: " + fileMetaData.getFilePath());
		
		int i = 0;
		while (byteBuffer.hasRemaining()) {
			char c = (char) byteBuffer.get();
			if ( c == fileMetaData.getLineDelimiter() || !byteBuffer.hasRemaining() ) {
				if (fileMetaData.isIgnoreHederRow() && isFirstRow) {
					isFirstRow = false;
				}
				else {
					if (line.length() != 0) {
						Column<Key, List<?>> row = createKeyAndRow(line.toString());
						dataMap.put(row.getKey(), row.getValue());
						i++;
						if (i % 10000 == 0) {
							LOGGER.info("Loaded " + i + " rows");
						}
					}
				}
				line = new StringBuilder();
			} else {
				line.append(c);
			}
		}
		LOGGER.info("Loaded file: " + fileMetaData.getFilePath());
		return this;
	}

	public Set<Key> getKeySet() {
		return dataMap.keySet();
	}
	
	public List<?> getRow(Key key) {
		return dataMap.get(key);
	}
	
	private Column<Key, List<?>> createKeyAndRow(String line) {
		List<?> fields = parseRow(line);
		List<String> keys =  new ArrayList<String>();
		for (Integer index : fileMetaData.getKeyColumnIndexes()) {
			keys.add(fields.get(index).toString());
		}
		return new Column<Key, List<?>>(new Key(keys), fields);
	}
}
