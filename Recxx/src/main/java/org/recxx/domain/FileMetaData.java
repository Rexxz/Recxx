package org.recxx.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class FileMetaData {

	private final String filePath;
	private final boolean ignoreHeaderRow;

	private final String delimiter;
	private final char lineDelimiter;

	private final List<String> keyColumns;
	private final List<String> columnsToCompare;
	private final List<String> dateFormats;
	private final List<Integer> keyColumnIndexes;
	private final List<Column<String, Class<?>>> columns;

	public static class Builder {
		
		 String filePath;
		 boolean ignoreHeaderRow;

		 String delimiter;
		 char lineDelimiter;

		 List<String> keyColumns;
		 List<String> columnsToCompare;
		 List<String> dateFormats;
		 List<Column<String, Class<?>>> columns;
		 
		 public Builder filePath(String filePath) {
			 this.filePath = filePath;
			 return this;
		 }

		 public Builder keyColumns(List<String> keyColumns) {
			 this.keyColumns = keyColumns;
			 return this;
		 }
		 
		 public Builder columnsToCompare(List<String> columnsToCompare) {
			 this.columnsToCompare = columnsToCompare;
			 return this;
		 }
		 
		 public Builder dateFormats(List<String> dateFormats) {
			 this.dateFormats = dateFormats;
			 return this;
		 }
		 
		 public Builder columns(List<Column<String, Class<?>>> columns) {
			 this.columns = columns;
			 return this;
		 }
		 
		 public Builder delimiter(String delimiter) {
			 this.delimiter = delimiter;
			 return this;
		 }
		 
		 public Builder lineDelimiter(char lineDelimiter) {
			 this.lineDelimiter = lineDelimiter;
			 return this;
		 }

		 public Builder ignoreHeaderRow(boolean ignoreHeaderRow) {
			 this.ignoreHeaderRow = ignoreHeaderRow;
			 return this;
		 }
		 
		 public FileMetaData build() {
			 return new FileMetaData(this);
		 }
	}
	
	private FileMetaData(Builder builder) {
		this.filePath = builder.filePath;
		this.keyColumns = builder.keyColumns;
		this.columns = builder.columns;
		this.dateFormats = builder.dateFormats;
		this.delimiter = builder.delimiter;
		this.lineDelimiter = builder.lineDelimiter;
		this.ignoreHeaderRow = builder.ignoreHeaderRow;
		this.keyColumnIndexes = generateKeyColumnIndexes(builder.keyColumns, builder.columns);
		//TODO Reconsider this
		if (builder.columnsToCompare == null && builder.keyColumns != null) {
			this.columnsToCompare = generateColumnsCompare(builder.keyColumns, builder.columns);			
		}
		else {
			this.columnsToCompare = builder.columnsToCompare;
		}
	}
		
	public String getFilePath() {
		return filePath;
	}

	public List<String> getKeyColumns() {
		return keyColumns;
	}

	public List<Integer> getKeyColumnIndexes() {
		return keyColumnIndexes;
	}

	public List<Column<String, Class<?>>> getColumns() {
		return columns;
	}

	public List<String> getColumnsToCompare() {
		return columnsToCompare;
	}

	public List<String> getDateFormats() {
		return dateFormats;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public char getLineDelimiter() {
		return lineDelimiter;
	}

	public boolean isIgnoreHederRow() {
		return ignoreHeaderRow;
	}

	public List<String> getColumnNames() {
		List<String> columnNames = new ArrayList<String>();
		for (Column<String, Class<?>> column : this.columns) {
			columnNames.add(column.getKey());
		}
		return columnNames;
	}

	public List<Class<?>> getColumnTypes() {
		List<Class<?>> types = new ArrayList<Class<?>>();
		for (Column<String, Class<?>> column : this.columns) {
			types.add(column.getValue());
		}
		return types;
	}

	private List<Integer> generateKeyColumnIndexes(List<String> keyColumns,
			List<Column<String, Class<?>>> columns) {
		List<Integer> keyColumnIndexes = new ArrayList<Integer>();
		if (keyColumns != null & columns != null) {
			for (int i = 0; i < columns.size(); i++) {
				if (keyColumns.contains(columns.get(i).getKey())) {
					keyColumnIndexes.add(Integer.valueOf(i));
				}
			}
		}
		return keyColumnIndexes;
	}
	
	private List<String> generateColumnsCompare(List<String> keyColumns,
			List<Column<String, Class<?>>> columns) {
		List<String> columnsToCompare = new ArrayList<String>();
		for (int i = 0; i < columns.size(); i++) {
			if (!keyColumns.contains(columns.get(i).getKey())) {
				columnsToCompare.add(columns.get(i).getKey());
			}
		}
		return columnsToCompare;
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
