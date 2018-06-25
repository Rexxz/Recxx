package org.recxx.domain;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static org.recxx.utils.SystemUtils.quote;

public class Key implements CharSequence, Serializable {

	static final long serialVersionUID = 1L;
	
	public static final String DELIMITER = "\u00B6";

	private static final String ENCODING = "UTF-8";
	private final int offset;
	private final int end;
	private final byte[] data;

	public Key(String str) {
		try {
			data = str.getBytes(ENCODING);
			offset = 0;
			end = data.length;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unexpected: " + ENCODING + " not supported!");
		}
	}

	public Key(List<String> keys) {
		StringBuilder builder = new StringBuilder();
		for (String string : keys) {
			if (builder.length() != 0) builder.append(Key.DELIMITER);
			builder.append(string);	
		}
		builder.trimToSize();
		try {
			data = builder.toString().getBytes(ENCODING);
			offset = 0;
			end = data.length;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unexpected: " + ENCODING + " not supported!");
		}
	}
	
	private Key(byte[] data, int offset, int end) {
		this.data = data;
		this.offset = offset;
		this.end = end;
	}

	public char charAt(int index) {
		int ix = index + offset;
		if (ix >= end) {
			throw new StringIndexOutOfBoundsException("Invalid index " + index + " length " + length());
		}
		return (char) (data[ix] & 0xff);
	}

	public int length() {
		return end - offset;
	}

	public CharSequence subSequence(int start, int end) {
		if (start < 0 || end >= (this.end - offset)) {
			throw new IllegalArgumentException("Illegal range " + start + "-" + end + " for sequence of length " + length());
		}
		return new Key(data, start + offset, end + offset);
	}

	public List<String> asList() {
		String key = this.toString();
		return Arrays.asList(key.split(DELIMITER));
	}
	
	public String toOutputString(String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (String keyPart : asList()) {
			sb.append(quote(keyPart)).append(delimiter);
		}
		return sb.toString();
	}

	public String toString() {
		try {
			return new String(data, offset, end - offset, ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unexpected: " + ENCODING + " not supported");
		}
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