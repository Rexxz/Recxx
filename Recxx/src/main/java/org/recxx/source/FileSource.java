package org.recxx.source;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;
import org.apache.log4j.Logger;
import org.recxx.domain.Column;
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;

import au.com.bytecode.opencsv.CSVParser;

public abstract class FileSource implements Source<Key> {

	public static final Logger LOGGER = Logger.getLogger(FileSource.class);
	
	public static final String DEFAULT_DELIMITER = ",";
	public static final String DEFAULT_COLUMN_NAME_TYPE_SEPARATOR = "|";
	private static final String READ_ONLY = "r";

	protected FileMetaData fileMetaData;
	protected final RandomAccessFile randomAccessFile;
	protected final MappedByteBuffer byteBuffer;
	protected final ConvertUtilsBean convertUtilsBean;
	private final String alias;
	
	protected FileSource(String alias, FileMetaData metaData) {
		this.alias = alias;
		this.fileMetaData = metaData;
		try {
			this.randomAccessFile = new RandomAccessFile(fileMetaData.getFilePath(), READ_ONLY);
			this.byteBuffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile.length());
			this.convertUtilsBean = new ConvertUtilsBean();
			DateTimeConverter dtConverter = new DateConverter();
			dtConverter.setPatterns(fileMetaData.getDateFormats().toArray(new String[fileMetaData.getDateFormats().size()]));
			convertUtilsBean.register(dtConverter, Date.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getAlias() {
		return alias;
	}
	
	protected List<Class<?>> getHeaderColumnTypes(int size) {
		List<Class<?>> columnTypes = new ArrayList<Class<?>>();
		for (int i = 0; i < size; i++) {
			columnTypes.add(String.class);			
		}
		return columnTypes;
	}
	
	protected List<?> parseRow(String line, List<Class<?>> columnTypes) {
		CSVParser parser = new CSVParser(fileMetaData.getDelimiter().charAt(0), CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, false);
		String[] fields;
		List<Object> row = null;
		try {
			fields = parser.parseLine(line);
			row = new ArrayList<Object>(fields.length);
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].isEmpty()) {
					row.add(null);
				}
				else {
					try {
						row.add(convertUtilsBean.convert(fields[i], columnTypes.get(i)));
					} catch (ConversionException ce) {
						throw new RuntimeException("Source '" + this.getAlias() + "' - Error attempting to convert value '" + fields[i] + 
								"' which should be type '" + columnTypes.get(i) + "', please correct the configuration or data", ce);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Source '" + this.getAlias() + "' - Error attempting to parse '" + line + 
					"', please correct the configuration or data", e);
		}
		return row;
	}
	
	protected boolean isCurrentLineDelimiter(String delimiter, Character... characters) {
		StringBuilder sb = new StringBuilder(2);
		if (delimiter.length() == characters.length) {
			for (Character c : characters) {
				sb.append(c);
			}
			return delimiter.equals(sb.toString());
		}
		return false;
	}

	public List<Column> getColumns() {
		return fileMetaData.getColumns();
	}
	
	public List<String> getKeyColumns() {
		return fileMetaData.getKeyColumns();
	}
	
	public List<String> getCompareColumns() {
		return fileMetaData.getColumnsToCompare();
	}
	
	
}
