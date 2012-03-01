package org.recxx.source;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.log4j.Logger;
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;
import org.recxx.domain.Column;

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
				row.add(ConvertUtils.convert(fields.get(i).trim(), columnTypes.get(i)));
			} catch (ConversionException e) {
				throw new RuntimeException("Source '" + this.getAlias() + "' - Error attempting to convert value '" + fields.get(i).trim() + 
						"' which should be type '" + columnTypes.get(i) + "', please correct the configuration or data", e);
			}
		}
		return row;
	}
	
	protected List<String> splitLine(String fileLine) {
        String pattern = "\\" + fileMetaData.getDelimiter() + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
		return Arrays.asList(fileLine.split(pattern));
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
