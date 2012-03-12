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
	private final String lineDelimiter;

	private final List<String> keyColumns;
	private final List<String> columnsToCompare;
	private final List<String> dateFormats;
	private final List<Integer> keyColumnIndexes;
	private final List<Column> columns;

	public static class Builder {
		
		 String filePath;
		 boolean ignoreHeaderRow;

		 String delimiter;
		 String lineDelimiter;

		 List<String> keyColumns;
		 List<String> columnsToCompare;
		 List<String> dateFormats;
		 List<Column> columns;
		 
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
		 
		 public Builder columns(List<Column> columns) {
			 this.columns = columns;
			 return this;
		 }
		 
		 public Builder delimiter(String delimiter) {
			 this.delimiter = delimiter;
			 return this;
		 }
		 
		 public Builder lineDelimiter(String lineDelimiter) {
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
		this.columnsToCompare = generateColumnsToCompare(builder.columnsToCompare, builder.keyColumns, builder.columns);			
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

	public List<Column> getColumns() {
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

	public String getLineDelimiter() {
		return lineDelimiter;
	}

	public boolean isIgnoreHederRow() {
		return ignoreHeaderRow;
	}

	public List<String> getColumnNames() {
		List<String> columnNames = new ArrayList<String>();
		for (Column column : this.columns) {
			columnNames.add(column.getName());
		}
		return columnNames;
	}

	public List<Class<?>> getColumnTypes() {
		List<Class<?>> types = new ArrayList<Class<?>>();
		for (Column column : this.columns) {
			types.add(column.getType());
		}
		return types;
	}

	private List<Integer> generateKeyColumnIndexes(List<String> keyColumns,
													List<Column> columns) {
		List<Integer> keyColumnIndexes = new ArrayList<Integer>();
		if (keyColumns != null & columns != null) {
			for (int i = 0; i < columns.size(); i++) {
				if (keyColumns.contains(columns.get(i).getName())) {
					keyColumnIndexes.add(Integer.valueOf(i));
				}
			}
		}
		return keyColumnIndexes;
	}
	
	private List<String> generateColumnsToCompare(List<String> columnsToCompare,
												List<String> keyColumns, 
												List<Column> columns) {
		if (keyColumns != null & columns != null) {
			if (columnsToCompare == null || columnsToCompare.isEmpty()) {
				columnsToCompare = new ArrayList<String>();
				for (int i = 0; i < columns.size(); i++) {
					if (!keyColumns.contains(columns.get(i).getName())) {
						columnsToCompare.add(columns.get(i).getName());
					}
				}
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
