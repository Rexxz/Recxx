package org.recxx.domain;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Coordinates {

	private final Key internalKey;
	private List<String> internalKeyAsList;
	
	public Coordinates (int byteBufferStartIndex, int start, int byteBufferEndIndex, int end) {
		internalKey = new Key(Arrays.asList(String.valueOf(byteBufferStartIndex), String.valueOf(start), String.valueOf(byteBufferEndIndex), String.valueOf(end)));
	}

	public static Coordinates valueOf(int byteBufferStartIndex, int start, int byteBufferEndIndex, int end) {
		return new Coordinates(byteBufferStartIndex, start, byteBufferEndIndex, end);
	}
	
	private void populateList() {
		if (internalKeyAsList == null) {
			internalKeyAsList = internalKey.asList();
		}
	}
	
	public int getByteBufferStartIndex() {
		populateList();
		return Integer.valueOf(internalKeyAsList.get(0)).intValue();
	}
	
	public int getStart() {
		populateList();
		return Integer.valueOf(internalKeyAsList.get(1)).intValue();
	}
	
	public int getByteBufferEndIndex() {
		populateList();
		return Integer.valueOf(internalKeyAsList.get(2)).intValue();
	}
	
	public int getEnd() {
		populateList();
		return Integer.valueOf(internalKeyAsList.get(3)).intValue();
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.reflectionEquals(this, that, false);
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}

}
