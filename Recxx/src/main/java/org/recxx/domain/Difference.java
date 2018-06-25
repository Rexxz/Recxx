package org.recxx.domain;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import static org.recxx.utils.SystemUtils.quote;

public class Difference {
	
	private final Key key;
	private final Column column;
	private final BigDecimal absoluteDifference;
	private final BigDecimal percentageDifference;
	private final Object field1;
	private final Object field2;
	private final String alias1;
	private final String alias2;
	
	private Difference(Builder builder) {
		this.key = builder.key;
		this.column = builder.column;
		this.absoluteDifference = builder.absoluteDifference;
		this.percentageDifference = builder.percentageDifference;
		this.field1 = builder.field1;
		this.field2 = builder.field2;
		this.alias1 = builder.alias1;
		this.alias2 = builder.alias2;
	}
	
	public static class Builder {
		
		Key key;
		Column column;
		Object field1;
		Object field2;
		BigDecimal absoluteDifference;
		BigDecimal percentageDifference;
		String alias1;
		String alias2;
		
		public Builder field1(Object value) {
			field1 = value;
			return this;
		}
		
		public Builder field2(Object value) {
			field2 = value;
			return this;
		}
		
		public Builder key(Key value) {
			key = value;
			return this;
		}
		
		public Builder absoluteDifference(BigDecimal value) {
			absoluteDifference = value;
			return this;
		}
		
		public Builder percentageDifference(BigDecimal value) {
			percentageDifference = value;
			return this;
		}
		
		public Builder column(Column value) {
			column = value;
			return this;
		}
		
		public Builder alias1(String value) {
			alias1 = value;
			return this;
		}
		
		public Builder alias2(String value) {
			alias2 = value;
			return this;
		}
		
		public Difference build() {
			return new Difference(this);
		}
	}

	
	public Key getKey() {
		return key;
	}

	public Column getColumn() {
		return column;
	}

	public Object getField1() {
		return field1;
	}

	public Object getField2() {
		return field2;
	}

	public BigDecimal getAbsoluteDifference() {
		return absoluteDifference;
	}

	public BigDecimal getPercentageDifference() {
		return percentageDifference;
	}

	public Object getAlias1() {
		return alias1;
	}
	
	public Object getAlias2() {
		return alias2;
	}
	
	public String toOutputString(){
		return toOutputString(Default.COMMA, Default.DATE_FORMAT, Default.TWENTY_FIVE_DP_PERCENT_FORMAT);
	}
	
	public String toOutputString(String delimiter, SimpleDateFormat dateFormatter, DecimalFormat percentFormat) {
		StringBuilder sb = new StringBuilder();
		sb.append(getKey().toOutputString(delimiter));
		sb.append(quote(getColumn().getName())).append(delimiter);
		if (getField1() != null && Date.class.isAssignableFrom(getField1().getClass())) {
			sb.append(quote(dateFormatter.format(getField1()))).append(delimiter);
		}
		else if (getField1() != null && Number.class.isAssignableFrom(getField1().getClass())) {
			sb.append(dateFormatter.format(getField1())).append(delimiter);
		}
		else {
			sb.append(quote(getField1() == null ? "" : getField1().toString())).append(delimiter);
		}
		if (getField2() != null && Date.class.isAssignableFrom(getField2().getClass())) {
			sb.append(quote(dateFormatter.format(getField2()))).append(delimiter);
		}
		else if (getField2() != null && Number.class.isAssignableFrom(getField2().getClass())) {
			sb.append(dateFormatter.format(getField2())).append(delimiter);
		}
		else {
			sb.append(quote(getField2() == null ? "" : getField2().toString())).append(delimiter);
		}
		if (getField1() != null && Number.class.isAssignableFrom(getField1().getClass()) && 
				getField2() != null && Number.class.isAssignableFrom(getField2().getClass())) {
			sb.append(percentFormat.format(getPercentageDifference()))
			.append(delimiter)
			.append(getAbsoluteDifference());
		}
		else {
			sb.append(delimiter);
		}		
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
