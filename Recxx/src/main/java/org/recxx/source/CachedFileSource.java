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
		String delimiter = getRowDelimiter();
		
		LOGGER.info("Source '" + getAlias() + "': Loading file: " + fileMetaData.getFilePath());
		boolean columnNamesNotSupplied = fileMetaData.getColumnNames().contains(Default.UNKNOWN_COLUMN_NAME);
		if (columnNamesNotSupplied) {
			isIgnoreHeaderRow = true;
		}
			
		int i = 0;
		char p = ' ';
		while (byteBuffer.hasRemaining()) {
			char c = decodeSingleByteToChar(byteBuffer.get());
			if ( delimiter.length() == 1 && isCurrentLineDelimiter(delimiter, c)
					|| (delimiter.length() == 2 && isCurrentLineDelimiter(delimiter, p, c)) 
					|| !byteBuffer.hasRemaining() ) {
				if (isFirstRow && isIgnoreHeaderRow) {
					if (columnNamesNotSupplied) {
						List<?> columns = parseRow(line.toString(), getHeaderColumnTypes(fileMetaData.getColumns().size()));
						fileMetaData = FileMetaData.valueOf(fileMetaData, columns);
					}
					isFirstRow = false;
				}
				else {
					if (line.length() != 0) {
						// Key & List fields
						List<?> fields = parseRow(line.toString(), fileMetaData.getColumnTypes());
						dataMap.put(createKey(fields, line.toString()), fields);
						i++;
						if (i % 10000 == 0) {
							LOGGER.info("Source '" + getAlias() + "':Loaded " + i + " rows");
						}
					}
				}
				line.setLength(0);
			} else if (!delimiter.contains(String.valueOf(c))) {
				line.append(c);
			}
			p = c;
		}
		LOGGER.info("Source '" + getAlias() + "': Loaded " + i + " rows");
		return this;
	}

	public Set<Key> getKeySet() {
		return dataMap.keySet();
	}
	
	public List<?> getRow(Key key) {
		return dataMap.get(key);
	}
	
	private Key createKey(List<?> fields, String line) {
		List<String> keys =  new ArrayList<String>();
		Key key = null;
		try {
			for (Integer index : fileMetaData.getKeyColumnIndexes()) {
				keys.add(fields.get(index) == null ? Default.NULL : fields.get(index).toString());
			}
			key = new Key(keys);
			if (dataMap.containsKey(key)) {
				LOGGER.warn("Source '" + getAlias() + "': A duplicate key was found for: " + key.toOutputString(Default.COMMA) + " will suffix with a unique id");
				int i = 0;
				Key suffixedKey = new Key(key.toString() + "_" + i);
				while (dataMap.containsKey(suffixedKey)) {
					i++;
					suffixedKey = new Key(key.toString() + "_" + i);
				}
				key = suffixedKey;
			}
		} catch (Exception e) {
			LOGGER.error("Source '" + getAlias() + "': An error occurred trying to extract a key from the following line: '" + line + "'");
		}
		return key;
	}

}
