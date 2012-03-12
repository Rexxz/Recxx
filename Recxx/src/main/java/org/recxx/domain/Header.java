package org.recxx.domain;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.recxx.configuration.RecxxConfiguration;

public class Header {
	
	private final RecxxConfiguration configuration;

	public Header(RecxxConfiguration configuration) {
		this.configuration = configuration;
	}

	public String toOutputString() {
		return toOutputString(Default.COMMA, Default.LINE_DELIMITER);
	}

	public String toOutputString(String delimiter, String lineDelimiter) {
		StringBuilder sb = new StringBuilder();
		List<String> sources = configuration.configureSourceAliases();
		String source1Alias = sources.get(0);
		String source2Alias = sources.get(1);
		List<String> keyColumns = configuration.configureKeyColumns(source1Alias);
		for (String column : keyColumns) {
			sb.append("Key: ").append(column).append(delimiter);
		}
		sb.append("column").append(delimiter)
		.append(source1Alias).append(".value").append(delimiter)
		.append(source2Alias).append(".value").append(delimiter)
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
