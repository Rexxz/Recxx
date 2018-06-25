package org.recxx.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Conversion {

	public enum Operation{
		REGEX_MATCH, REGEX_REPLACE_FIRST, REGEX_REPLACE_ALL
	}

	private String fieldName;
	private Operation regexOperation;
	private String pattern;
	private String replacement;
	
	public Conversion(String fieldName, Operation regexOperation, String pattern) {
		this(fieldName, regexOperation, pattern, null);
	}

	public Conversion(String fieldName, Operation regexOperation, String pattern, String replacement) {
		this.fieldName = fieldName;
		this.regexOperation = regexOperation;
		this.pattern = pattern;
		this.replacement = replacement;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Operation getRegexOperation() {
		return regexOperation;
	}

	public String getPattern() {
		return pattern;
	}

	public String getReplacement() {
		return replacement;
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
