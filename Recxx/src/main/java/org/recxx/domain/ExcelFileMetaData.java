package org.recxx.domain;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ExcelFileMetaData {

	private final String filePath;
	private final boolean formatComparison;
	private final List<String> omitSheets;

	private final List<String> dateFormats;

	public static class Builder {
		
		 String filePath;
		 boolean formatComparison;
		 List<String> omitSheets;

		 List<String> dateFormats;
		 
		 public Builder filePath(String filePath) {
			 this.filePath = filePath;
			 return this;
		 }

		 public Builder formatComparison(boolean formatComparison) {
			 this.formatComparison = formatComparison;
			 return this;
		 }
		 
		 public Builder omitSheets(List<String> omitSheets) {
			 this.omitSheets = omitSheets;
			 return this;
		 }
		 
		 public Builder dateFormats(List<String> dateFormats) {
			 this.dateFormats = dateFormats;
			 return this;
		 }
		 
		 public ExcelFileMetaData build() {
			 return new ExcelFileMetaData(this);
		 }
	}
	
	private ExcelFileMetaData(Builder builder) {
		this.filePath = builder.filePath;
		this.formatComparison = builder.formatComparison;
		this.omitSheets = builder.omitSheets;
		
		this.dateFormats = builder.dateFormats;
	}
		
	public String getFilePath() {
		return filePath;
	}

	public boolean isFormatComparison() {
		return formatComparison;
	}

	public List<String> getOmitSheets() {
		return omitSheets;
	}

	public List<String> getDateFormats() {
		return dateFormats;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
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
