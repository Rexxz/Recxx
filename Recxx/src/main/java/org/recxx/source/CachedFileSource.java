package org.recxx.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.recxx.domain.Default;
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;

public class CachedFileSource extends FileSource {
	
	private static Logger LOGGER = Logger.getLogger(CachedFileSource.class); 

	private final Map<Key, List<?>> dataMap = new HashMap<Key, List<?>>();
	
	public CachedFileSource(String alias, FileMetaData metaData) {
		super(alias, metaData);
	}
	
	public Source<Key> call() {
		StringBuilder line = new StringBuilder();
		boolean isFirstRow = true;
		boolean isIgnoreHeaderRow = fileMetaData.isIgnoreHederRow();
		
		LOGGER.info("Loading file: " + fileMetaData.getFilePath());
		boolean columnNamesNotSupplied = fileMetaData.getColumnNames().contains(Default.UNKNOWN_COLUMN_NAME);
		if (columnNamesNotSupplied) {
			isIgnoreHeaderRow = true;
		}
			
		int i = 0;
		while (byteBuffer.hasRemaining()) {
			char c = (char) byteBuffer.get();
			if ( c == fileMetaData.getLineDelimiter().charAt(0) || !byteBuffer.hasRemaining() ) {
				if (isFirstRow && isIgnoreHeaderRow) {
					if (columnNamesNotSupplied) {
						List<?> columns = parseRow(line.toString(), getHeaderColumnTypes(fileMetaData.getColumns().size()));
						fileMetaData = FileMetaData.valueOf(fileMetaData, columns);
					}
					isFirstRow = false;
				}
				else {
					if (line.length() != 0) {
						List<?> fields = parseRow(line.toString(), fileMetaData.getColumnTypes());
						dataMap.put(createKey(fields), fields);
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
		return this;
	}

	public Set<Key> getKeySet() {
		return dataMap.keySet();
	}
	
	public List<?> getRow(Key key) {
		return dataMap.get(key);
	}
	
	private Key createKey(List<?> fields) {
		List<String> keys =  new ArrayList<String>();
		for (Integer index : fileMetaData.getKeyColumnIndexes()) {
			keys.add(fields.get(index) == null ? Default.NULL_STRING : fields.get(index).toString());
		}
		return new Key(keys);
	}
	
}
