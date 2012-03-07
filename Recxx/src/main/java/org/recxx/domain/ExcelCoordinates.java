package org.recxx.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ExcelCoordinates {

	private final String sheetName;
	private final int row;
	private final ExcelType type;
	
	public ExcelCoordinates(String sheetName, int row, ExcelType type) {
		this.sheetName = sheetName;
		this.row = row;
		this.type = type;
	}

	public String getSheetName() {
		return sheetName;
	}

	public int getRow() {
		return row;
	}

	public ExcelType getType() {
		return type;
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
