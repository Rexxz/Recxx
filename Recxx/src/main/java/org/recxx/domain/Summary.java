package org.recxx.domain;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.recxx.utils.ComparisonUtils;

public class Summary {
	
	private final String alias1;
	private final String alias2;
	private final Integer alias1Count;
	private final Integer alias2Count;
	private final Integer matchCount;
	
	private Summary(Builder builder) {
		this.alias1 = builder.alias1;
		this.alias2 = builder.alias2;
		this.alias1Count = builder.alias1Count;
		this.alias2Count = builder.alias2Count;
		this.matchCount = builder.matchCount;
	}
	
	public static class Builder {
		
		Integer alias1Count;
		Integer alias2Count;
		Integer matchCount;
		String alias1;
		String alias2;
		
		public Builder alias1(String alias) {
			alias1 = alias;
			return this;
		}
		
		public Builder alias2(String alias) {
			alias2 = alias;
			return this;
		}
		
		public Builder alias1Count(Integer count) {
			alias1Count = count;
			return this;
		}
		
		public Builder alias2Count(Integer count) {
			alias2Count = count;
			return this;
		}
		
		public Builder matchCount(Integer count) {
			matchCount = count;
			return this;
		}
		
		public Summary build() {
			return new Summary(this);
		}
	}

	public Integer getAlias1Count() {
		return alias1Count;
	}
	
	public Integer getAlias2Count() {
		return alias2Count;
	}

	public Integer getMatchCount() {
		return matchCount;
	}

	public String getAlias1() {
		return alias1;
	}

	public String getAlias2() {
		return alias2;
	}
	
	public BigDecimal getAlias1MatchPercentage() {
		return ComparisonUtils.percentageMatch(matchCount, alias1Count);
	}

	public BigDecimal getAlias2MatchPercentage() {
		return ComparisonUtils.percentageMatch(matchCount, alias2Count);
	}

	public String toOutputString(){
		return toOutputString(Default.COMMA, Default.LINE_DELIMITER, Default.PERCENT_FORMAT);
	}

	public String toOutputString(String delimiter, String lineDelimiter, DecimalFormat percentFormat) {
		StringBuilder sb = new StringBuilder();
		sb.append(lineDelimiter)
		.append(lineDelimiter)
		.append("======================").append(lineDelimiter)
		.append("Reconciliation Summary").append(lineDelimiter)
		.append("======================").append(lineDelimiter)
		.append(getAlias1()).append(" rows").append(delimiter).append(getAlias1Count()).append(lineDelimiter)
		.append(getAlias2()).append(" rows").append(delimiter).append(getAlias2Count()).append(lineDelimiter)
		.append(getAlias1()).append(" matched ").append(getAlias2()).append(delimiter).append(getMatchCount()).append(lineDelimiter)
		.append(getAlias1()).append(" matched ").append(getAlias2()).append(delimiter).append(Default.PERCENT_FORMAT.format(getAlias1MatchPercentage())).append(lineDelimiter)
		.append(getAlias2()).append(" matched ").append(getAlias1()).append(delimiter).append(Default.PERCENT_FORMAT.format(getAlias2MatchPercentage())).append(lineDelimiter);
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
