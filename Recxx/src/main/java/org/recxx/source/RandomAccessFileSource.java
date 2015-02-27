package org.recxx.source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.recxx.domain.Coordinates;
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;

public class RandomAccessFileSource extends FileSource {

	final Map<Key, Coordinates> dataMap = new HashMap<Key, Coordinates>();

	public RandomAccessFileSource(String alias, FileMetaData metaData) {
		super(alias, metaData);
	}

	public Set<Key> getKeySet() {
		return dataMap.keySet();
	}

	public List<?> getRow(Key key) {
		Coordinates coords = dataMap.get(key);
		StringBuilder builder = new StringBuilder(coords.end - coords.start);

		builder = new StringBuilder();
		for (int i = coords.start; i < coords.end; i++) {
			builder.append(decodeSingleByteToChar(byteBuffer.get(i)));
		}
		return parseRow(builder.toString(), fileMetaData.getColumnTypes());
	}

	@Override
	protected Map<Key, ?> getSourceDataMap() {
		return dataMap;
	}

	@Override
	protected void addRow(Key key, List<?> fields, int start, int end) {
		Coordinates coordinates = Coordinates.valueOf(start, end);
		dataMap.put(key, coordinates);
	}

}
