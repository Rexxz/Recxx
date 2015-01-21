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

	private long executionTimeMillis = 0;

	public RandomAccessFileSource(String name, FileMetaData metaData) {
		super(name, metaData);
	}

	public Source<Key> call() {
		StringBuilder line = new StringBuilder();
		int start = 0;
		boolean isFirstRow = true;
		boolean isIgnoreHeaderRow = fileMetaData.isIgnoreHederRow();
		String delimiter = getRowDelimiter();

		LOGGER.info("Source '" + getAlias() + "': Processing file: " + fileMetaData.getFilePath());
		boolean columnNamesNotSupplied = fileMetaData.getColumnNames().contains(Default.UNKNOWN_COLUMN_NAME);
		if (columnNamesNotSupplied) {
			isIgnoreHeaderRow = true;
		}

		long startTimeMillis = System.currentTimeMillis();
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
						// Key
						Key key = createKey(line.toString(), i);
						Coordinates coordinates = Coordinates.valueOf(start, byteBuffer.position() - delimiter.length());
						keyMap.put(key, coordinates);
						i++;
						if (i % 10000 == 0) {
							LOGGER.info("Source '" + getAlias() + "': Processed " + i + " rows");
						}
					}
				}
				start = byteBuffer.position();
				line.setLength(0);
			} else if (!delimiter.contains(String.valueOf(c))) {
				line.append(c);
			}
			p = c;
		}
		LOGGER.info("Source '" + getAlias() + "': Processed " + i + " rows");
		executionTimeMillis = System.currentTimeMillis() - startTimeMillis;
		return this;
	}

	public Set<Key> getKeySet() {
		return keyMap.keySet();
	}

	public List<?> getRow(Key key) {
		Coordinates coords = keyMap.get(key);
		StringBuilder builder = new StringBuilder(coords.end - coords.start);

		builder = new StringBuilder();
		for (int i = coords.start; i < coords.end; i++) {
			builder.append(decodeSingleByteToChar(byteBuffer.get(i)));
		}
		return parseRow(builder.toString(), fileMetaData.getColumnTypes());
	}

	private Key createKey(String line, int lineNumber) {
		List<?> fields = parseRow(line, fileMetaData.getColumnTypes());
		List<String> keys =  new ArrayList<String>();
		Key key = null;
		try {
			if (fileMetaData.getKeyColumns().contains(Default.EMPTY_KEY_COLUMN_NAME) &&
					fileMetaData.getKeyColumns().size() == 1) {
				key = new Key(String.valueOf(lineNumber + 1));
			}
			else {
				for (Integer index : fileMetaData.getKeyColumnIndexes()) {
					keys.add(fields.get(index) == null ? Default.NULL : fields.get(index).toString());
				}
				key = new Key(keys);
			}
			if (keyMap.containsKey(key)) {
				LOGGER.warn("Source '" + getAlias() + "': A duplicate key was found for: " + key.toOutputString(Default.COMMA) + " will suffix with a unique id");
				int i = 0;
				Key suffixedKey = new Key(key.toString() + "_" + i);
				while (keyMap.containsKey(suffixedKey)) {
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

	@Override
	public long getExecutionTimeMillis() {
		return executionTimeMillis;
	}
}
