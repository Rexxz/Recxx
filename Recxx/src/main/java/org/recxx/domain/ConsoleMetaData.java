package org.recxx.domain;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ConsoleMetaData {

	private final List<String> dateFormats;
	private final String delimiter;
	private final String lineDelimiter;

	public static class Builder {
		
		 List<String> dateFormats;
		 String delimiter;
		 String lineDelimiter;
		 
		 public Builder dateFormats(List<String> dateFormats) {
			 this.dateFormats = dateFormats;
			 return this;
		 }
		 
		 public Builder delimiter(String delimiter) {
			 this.delimiter = delimiter;
			 return this;
		 }
		 
		 public Builder lineDelimiter(String lineDelimiter) {
			 this.lineDelimiter = lineDelimiter;
			 return this;
		 }

		 public ConsoleMetaData build() {
			 return new ConsoleMetaData(this);
		 }
	}
	
	private ConsoleMetaData(Builder builder) {
		this.dateFormats = builder.dateFormats;
		this.delimiter = builder.delimiter;
		this.lineDelimiter = builder.lineDelimiter;
	}
		
	public List<String> getDateFormats() {
		return dateFormats;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public String getLineDelimiter() {
		return lineDelimiter;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,	ToStringStyle.SHORT_PREFIX_STYLE);
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
