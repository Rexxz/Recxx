package org.recxx.source;

import gnu.trove.map.hash.THashMap;

import java.nio.MappedByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;

public class RandomAccessFileSource extends FileSource {

	final THashMap<Key, Key> dataMap = new THashMap<Key, Key>();

	public RandomAccessFileSource(String alias, FileMetaData metaData) {
		super(alias, metaData);
	}

	public Set<Key> getKeySet() {
		return dataMap.keySet();
	}

	public List<?> getRow(Key key) {
		Key dataCoordinates = dataMap.get(key);
		int byteBufferStartIndex = Integer.valueOf(dataCoordinates.asList().get(0)).intValue();		
		int start = Integer.valueOf(dataCoordinates.asList().get(1)).intValue();		
		int byteBufferEndIndex = Integer.valueOf(dataCoordinates.asList().get(2)).intValue();		
		int end = Integer.valueOf(dataCoordinates.asList().get(3)).intValue();		
		int currentByteBufferIndex = byteBufferStartIndex;
		int currentStart = start;
		int currentEnd = end;
		int length = byteBufferStartIndex == byteBufferEndIndex ? end - start : (MAX_BYTE_BUFFER_SIZE - start) + end;
		StringBuilder builder = new StringBuilder(length);
		
		do {
			MappedByteBuffer byteBuffer = byteBuffers.get(currentByteBufferIndex);
			if (byteBufferStartIndex != byteBufferEndIndex) {
				currentEnd = MAX_BYTE_BUFFER_SIZE;
			}
			if (currentByteBufferIndex != byteBufferStartIndex) {
				currentStart = 0;
				currentEnd = 0;
			}
			
			for (int i = currentStart; i < currentEnd; i++) {
				builder.append(decodeSingleByteToChar(byteBuffer.get(i)));
			}
			currentByteBufferIndex++;
		} while (currentByteBufferIndex <= byteBufferEndIndex);
		return parseRow(builder.toString(), fileMetaData.getColumnTypes());
	}

	@Override
	protected Map<Key, ?> getSourceDataMap() {
		return dataMap;
	}

	@Override
	protected void addRow(Key key, List<?> fields, int byteBufferStartIndex, int start, int byteBufferEndIndex, int end) {
		List<String> coordinates = Arrays.asList(String.valueOf(byteBufferStartIndex),
												String.valueOf(start),
												String.valueOf(byteBufferEndIndex), 
												String.valueOf(end));
		dataMap.put(key, new Key(coordinates));
	}

}
