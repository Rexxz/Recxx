package org.recxx.domain;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DatabaseMetaData {

	private final String databaseUrl;
	private final String databaseDriver;
	private final String databaseUserId;
	private final String databasePassword;
	private final String sql;

	private final List<String> keyColumns;
	private final List<String> columnsToCompare;

	private final List<String> dateFormats;

	public static class Builder {
		
		 String databaseUrl;
		 String databaseDriver;
		 String databaseUserId;
		 String databasePassword;
		 String sql;

		 List<String> keyColumns;
		 List<String> columnsToCompare;

		 List<String> dateFormats;

		 public Builder databaseUrl(String databaseUrl) {
			 this.databaseUrl = databaseUrl;
			 return this;
		 }

		 public Builder databaseDriver(String databaseDriver) {
			 this.databaseDriver = databaseDriver;
			 return this;
		 }
		 
		 public Builder databaseUserId(String databaseUserId) {
			 this.databaseUserId = databaseUserId;
			 return this;
		 }
		 
		 public Builder databasePassword(String databasePassword) {
			 this.databasePassword = databasePassword;
			 return this;
		 }
		 
		 public Builder sql(String sql) {
			 this.sql = sql;
			 return this;
		 }
		 
		 public Builder keyColumns(List<String> keyColumns) {
			 this.keyColumns = keyColumns;
			 return this;
		 }

		 public Builder dateFormats(List<String> dateFormats) {
			 this.dateFormats = dateFormats;
			 return this;
		 }

		 public Builder columnsToCompare(List<String> columnsToCompare) {
			 this.columnsToCompare = columnsToCompare;
			 return this;
		 }
		 
		 public DatabaseMetaData build() {
			 return new DatabaseMetaData(this);
		 }
		 
	}
	
	private DatabaseMetaData(Builder builder) {
		this.databaseUrl = builder.databaseUrl;
		this.databaseDriver = builder.databaseDriver;
		this.databaseUserId = builder.databaseUserId;
		this.databasePassword = builder.databasePassword;
		this.sql = builder.sql;

		this.keyColumns = builder.keyColumns;
		this.columnsToCompare = builder.columnsToCompare;
		this.dateFormats = builder.dateFormats;
	}
		
	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public String getDatabaseDriver() {
		return databaseDriver;
	}

	public String getDatabaseUserId() {
		return databaseUserId;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public String getSql() {
		return sql;
	}

	public List<String> getKeyColumns() {
		return keyColumns;
	}

	public List<String> getColumnsToCompare() {
		return columnsToCompare;
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
