package org.recxx.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.recxx.domain.Coordinates;
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
		
		LOGGER.info("Processing file: " + fileMetaData.getFilePath());
		
		int i = 0;
		while (byteBuffer.hasRemaining()) {
			char c = (char) byteBuffer.get();
			if ( c == fileMetaData.getLineDelimiter().charAt(0) || !byteBuffer.hasRemaining() ) {
				if (fileMetaData.isIgnoreHederRow() && isFirstRow) {
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
			} else {
				line.append(c);
			}
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
		return parseRow(builder.toString());
	}
	
	private Key createKey(String line) {
		List<?> fields = parseRow(line);
		List<String> keys =  new ArrayList<String>();
		for (Integer index : fileMetaData.getKeyColumnIndexes()) {
			keys.add(fields.get(index).toString());
		}
		return new Key(keys);
	}
}
