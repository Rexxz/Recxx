package org.recxx.source;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.log4j.Logger;
import org.recxx.domain.Column;
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;

public abstract class FileSource implements Source<Key> {

	public static final Logger LOGGER = Logger.getLogger(FileSource.class);
	
	public static final String DEFAULT_DELIMITER = ",";
	public static final String DEFAULT_COLUMN_NAME_TYPE_SEPARATOR = "|";
	private static final String READ_ONLY = "r";

	protected final FileMetaData fileMetaData;
	protected final RandomAccessFile randomAccessFile;
	protected final MappedByteBuffer byteBuffer;
	private final String alias;
	
	protected FileSource(String alias, FileMetaData metaData) {
		this.alias = alias;
		this.fileMetaData = metaData;
		try {
			this.randomAccessFile = new RandomAccessFile(fileMetaData.getFilePath(), READ_ONLY);
			this.byteBuffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile.length());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getAlias() {
		return alias;
	}

	protected List<?> parseRow(String line) {
		List<String> fields = splitLine(line);
		List<Object> row = new ArrayList<Object>(fields.size());
		List<Class<?>> columnTypes = fileMetaData.getColumnTypes();		
		for (int i = 0; i < fields.size(); i++) {
			try {
				row.add(ConvertUtils.convert(fields.get(i), columnTypes.get(i)));
			} catch (ConversionException e) {
				throw new RuntimeException("Source '" + this.getAlias() + "' - Error attempting to convert value '" + fields.get(i).trim() + 
						"' which should be type '" + columnTypes.get(i) + "', please correct the configuration or data", e);
			}
		}
		return row;
	}
	
	protected List<String> splitLine(String fileLine) {
		fileLine = normaliseNullValues(fileLine);
		List<String> matchList = new ArrayList<String>();
		Pattern regex = Pattern.compile("[^\\" + fileMetaData.getDelimiter() + "\"']+|\"([^\"]*)\"|'([^']*)'");
		Matcher regexMatcher = regex.matcher(fileLine);
		while (regexMatcher.find()) {
		    if (regexMatcher.group(1) != null) {
		        matchList.add(regexMatcher.group(1)); 			// Add double-quoted string without the quotes
		    } else if (regexMatcher.group(2) != null) {
		        matchList.add(regexMatcher.group(2)); 			// Add single-quoted string without the quotes
		    } else {
		        matchList.add(regexMatcher.group());			// Add unquoted word
		    }
		}         
		return matchList;
	}

	private String normaliseNullValues(String fileLine) {
		String nullValue = fileMetaData.getDelimiter() + fileMetaData.getDelimiter();
		String replacement = fileMetaData.getDelimiter() + " " + fileMetaData.getDelimiter();
		while (fileLine.contains(nullValue)) {
			fileLine = fileLine.replaceAll(nullValue, replacement);
		}
		return fileLine;
	}

	public List<Column<String, Class<?>>> getColumns() {
		return fileMetaData.getColumns();
	}
	
	public List<String> getKeyColumns() {
		return fileMetaData.getKeyColumns();
	}
	
	public List<String> getCompareColumns() {
		return fileMetaData.getColumnsToCompare();
	}
	
	
}
