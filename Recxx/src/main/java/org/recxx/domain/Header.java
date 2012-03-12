package org.recxx.domain;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.recxx.source.Source;

public class Header {
	
	private final Source<Key> source1;
	private final Source<Key> source2;

	public Header(Source<Key> source1, Source<Key> source2) {
		this.source1 = source1;
		this.source2 = source2;
	}

	public String toOutputString() {
		return toOutputString(Default.COMMA, Default.LINE_DELIMITER);
	}

	public String toOutputString(String delimiter, String lineDelimiter) {
		StringBuilder sb = new StringBuilder();
		List<String> keyColumns = source1.getKeyColumns();
		for (String column : keyColumns) {
			sb.append("Key: ").append(column).append(delimiter);
		}
		sb.append("column").append(delimiter)
		.append(source1.getAlias()).append(".value").append(delimiter)
		.append(source2.getAlias()).append(".value").append(delimiter)
		.append("% Diff").append(delimiter).append("ABS Diff");
		return sb.toString();
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
