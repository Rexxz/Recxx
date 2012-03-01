package org.recxx.domain;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ComparisonResult {
	
	private final boolean different;
	private final BigDecimal absoluteDifference;
	private final BigDecimal percentageDifference;
	
	public ComparisonResult(boolean different, BigDecimal absoluteDifference, BigDecimal percentageDifference) {
		this.different = different;
		this.absoluteDifference = absoluteDifference;
		this.percentageDifference = percentageDifference;
	}

	public boolean isDifferent() {
		return different;
	}

	public BigDecimal getAbsoluteDifference() {
		return absoluteDifference;
	}

	public BigDecimal getPercentageDifference() {
		return percentageDifference;
	}

	public static final ComparisonResult valueOf(boolean different, BigDecimal absoluteDifference, BigDecimal percentageDifference) {
		return new ComparisonResult(different, absoluteDifference, percentageDifference);
	}

	public static final ComparisonResult valueOf(boolean different) {
		return new ComparisonResult(different, null, null);
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
