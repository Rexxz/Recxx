package org.recxx.domain;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Difference {
	
	private final Key key;
	private final Column column;
	private final ComparisonResult comparison;
	private final Object field1;
	private final Object field2;
	private final List<String> keyColumns;
	private final String alias1;
	private final String alias2;
	
	private Difference(Builder builder) {
		this.key = builder.key;
		this.column = builder.column;
		this.comparison = builder.comparison;
		this.field1 = builder.field1;
		this.field2 = builder.field2;
		this.keyColumns = builder.keyColumns;
		this.alias1 = builder.alias1;
		this.alias2 = builder.alias2;
	}
	
	public static class Builder {
		
		Key key;
		Column column;
		ComparisonResult comparison;
		Object field1;
		Object field2;
		List<String> keyColumns;
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
		
		public Builder comparison(ComparisonResult value) {
			comparison = value;
			return this;
		}
		
		public Builder column(Column value) {
			column = value;
			return this;
		}
		
		public Builder keyColumns(List<String> value) {
			keyColumns = value;
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

	public ComparisonResult getComparison() {
		return comparison;
	}

	public Object getField1() {
		return field1;
	}

	public Object getField2() {
		return field2;
	}

	public List<String> getKeyColumns() {
		return keyColumns;
	}
	
	public Object getAlias1() {
		return alias1;
	}
	
	public Object getAlias2() {
		return alias2;
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
