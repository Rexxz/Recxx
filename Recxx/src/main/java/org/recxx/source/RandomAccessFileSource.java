package org.recxx.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.recxx.domain.Coordinates;
import org.recxx.domain.Default;
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;

public class RandomAccessFileSource extends FileSource {
	
	private static Logger LOGGER = Logger.getLogger(RandomAccessFileSource.class); 

	private final Map<Key, Coordinates> keyMap = new HashMap<Key, Coordinates>();
	
	public RandomAccessFileSource(String name, FileMetaData metaData) {
		super(name, metaData);
	}
	
	public Source<Key> call() {
		StringBuilder line = new StringBuilder();
		int start = 0;
		boolean isFirstRow = true;
		boolean isIgnoreHeaderRow = fileMetaData.isIgnoreHederRow();
		String delimiter = fileMetaData.getLineDelimiter();
		
		LOGGER.info("Processing file: " + fileMetaData.getFilePath());
		boolean columnNamesNotSupplied = fileMetaData.getColumnNames().contains(Default.UNKNOWN_COLUMN_NAME);
		if (columnNamesNotSupplied) {
			isIgnoreHeaderRow = true;
		}
		
		int i = 0;
		char p = ' ';
		while (byteBuffer.hasRemaining()) {
			char c = (char) byteBuffer.get();
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
						Key key = createKey(line.toString());
						Coordinates coordinates = Coordinates.valueOf(start, byteBuffer.position() - 1);
						keyMap.put(key, coordinates);
						i++;
						if (i % 10000 == 0) {
							LOGGER.info("Processed " + i + " rows");
						}
					}
				}
				start = byteBuffer.position();
				line = new StringBuilder();
			} else if (!delimiter.contains(String.valueOf(c))) {
				line.append(c);
			}
			p = c;
		}
		return this;
	}

	public Set<Key> getKeySet() {
		return keyMap.keySet();
	}
	
	public List<?> getRow(Key key) {
		Coordinates coords = keyMap.get(key);
		StringBuilder builder = new StringBuilder(coords.end - coords.start);
		
		builder = new StringBuilder();
		for (int i = coords.start; i <= coords.end; i++) {
			builder.append((char) byteBuffer.get(i));
		}
		return parseRow(builder.toString(), fileMetaData.getColumnTypes());
	}
	
	private Key createKey(String line) {
		List<?> fields = parseRow(line, fileMetaData.getColumnTypes());
		List<String> keys =  new ArrayList<String>();
		for (Integer index : fileMetaData.getKeyColumnIndexes()) {
			keys.add(fields.get(index) == null ? Default.NULL : fields.get(index).toString());
		}
		return new Key(keys);
	}
}
