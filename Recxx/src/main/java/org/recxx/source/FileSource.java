package org.recxx.source;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;
import org.apache.log4j.Logger;
import org.recxx.domain.Column;
import org.recxx.domain.Conversion;
import org.recxx.domain.Default;
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;
import org.recxx.utils.csv.CSVParser;

public abstract class FileSource implements Source<Key> {

	public static final Logger LOGGER = Logger.getLogger(FileSource.class);

	private static final String READ_ONLY = "r";

	protected static final int MAX_BYTE_BUFFER_SIZE = Integer.MAX_VALUE;	
	protected FileMetaData fileMetaData;
	protected RandomAccessFile randomAccessFile;
	protected List<MappedByteBuffer> byteBuffers = new ArrayList<MappedByteBuffer>();
	protected final ConvertUtilsBean convertUtilsBean;
	private final String alias;

	private FileChannel channel;
	private Charset charset;
	private Map<String, List<Conversion>> conversionsMap;

	private long executionTimeMillis = 0;

	protected FileSource(String alias, FileMetaData metaData) {
		this.alias = alias;
		this.fileMetaData = metaData;
		try {
			this.randomAccessFile = new RandomAccessFile(fileMetaData.getFilePath(), READ_ONLY);
			this.channel = randomAccessFile.getChannel();
			
			long length = randomAccessFile.length();
			long start = 0L;
			long size = length > MAX_BYTE_BUFFER_SIZE ? MAX_BYTE_BUFFER_SIZE: length;
			do {
				this.byteBuffers.add(channel.map(FileChannel.MapMode.READ_ONLY, start, size));
				start += MAX_BYTE_BUFFER_SIZE;
				size = (length > start + MAX_BYTE_BUFFER_SIZE ? MAX_BYTE_BUFFER_SIZE: length - start - 1);
			} while (start < length);
			
			this.convertUtilsBean = new ConvertUtilsBean();
			DateTimeConverter dtConverter = new DateConverter();
			dtConverter.setPatterns(fileMetaData.getDateFormats().toArray(new String[fileMetaData.getDateFormats().size()]));
			convertUtilsBean.register(dtConverter, Date.class);
			conversionsMap = new HashMap<String, List<Conversion>>();
			if (fileMetaData.getConversions() != null) {
				for (Conversion conversion : fileMetaData.getConversions()) {
					List<Conversion> conversionList;
					if (conversionsMap.containsKey(conversion.getFieldName())) {
						conversionList = conversionsMap.get(conversion.getFieldName());						
					}
					else {
						conversionList = new ArrayList<Conversion>();
					}
					conversionList.add(conversion);
					conversionsMap.put(conversion.getFieldName(), conversionList);
				}
			}
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
			fields = parser.parseLineMulti(line);
			row = new ArrayList<Object>(fields.length);
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].isEmpty()) {
					row.add(null);
				}
				else {
					try {
						if (!conversionsMap.isEmpty()) {
							row.add(convertUtilsBean.convert(applyRegexConversions(fields[i],i), columnTypes.get(i)));
						}
						else {
							row.add(convertUtilsBean.convert(fields[i], columnTypes.get(i)));
						}
					}
					catch (ConversionException ce) {
						String message = "Source '" + getAlias() + "': Error attempting to convert value '" + fields[i] +
								"' which should be type '" + columnTypes.get(i) + "' for field number <" + (i + 1)  +
								">, file column name '" + fileMetaData.getColumnNames().get(i) +
								"', please correct the configuration or data";
						LOGGER.error(message, ce);
						throw new RuntimeException(message, ce);
					}
					catch (RuntimeException e) {
						String message = "Source '" + getAlias() + "': Error attempting to parse the row '" + line +
								"' which should be parseable into '" + columnTypes.toString() + "', please correct the column configuration or data";
						LOGGER.error(message, e);
						throw new RuntimeException(message, e);
					}
				}
			}
		}
		catch (IOException e) {
			String message = "Source '" + getAlias() + "': Error attempting to parse '" + line +
					"', please correct the configuration or data";
			LOGGER.error(message, e);
			throw new RuntimeException(message, e);
		}
		return row;
	}

	private String applyRegexConversions(String value, int index) {
		String returnValue = value;
		String fieldName = fileMetaData.getColumns().get(index).getName();
		if (conversionsMap.containsKey(fieldName)) {
			List<Conversion> conversionsList = conversionsMap.get(fieldName);
			for (Conversion conversion : conversionsList) {
				
				Pattern pattern = Pattern.compile(conversion.getPattern());
				Matcher matcher = pattern.matcher(returnValue);
				switch (conversion.getRegexOperation()) {
				case REGEX_MATCH:
					if (matcher.find()) {
						returnValue = matcher.group(1);
					}
					break;
				case REGEX_REPLACE_FIRST:	
					if (matcher.find()) {
						returnValue = matcher.replaceFirst(conversion.getReplacement());
					}
					break;						
				case REGEX_REPLACE_ALL:	
					if (matcher.find()) {
						returnValue = matcher.replaceAll(conversion.getReplacement());
					}
					break;						
				default:
					break;
				}
			}
		}
		return null;
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

	protected char decodeSingleByteToChar(byte b) {
		if (fileMetaData.getEncoding() == null) {
			return (new String(new byte[] {b})).charAt(0);
		}
		else {
			if (charset == null)
				charset = Charset.forName(fileMetaData.getEncoding());
			return (new String(new byte[] {b}, charset)).charAt(0);
		}
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

	public List<String> getIgnoreColumns() {
		return fileMetaData.getColumnsToIgnore();
	}

	public int getColumnIndex(String name) {
		return fileMetaData.getColumnNames().indexOf(name);
	}

	public void close() {
		LOGGER.info("Closing file: " + fileMetaData.getFilePath());
		try {
			randomAccessFile.close(); 	
			randomAccessFile = null;
			channel.close(); 			
			channel = null;
			byteBuffers.clear();
			byteBuffers = null;
			File file = new File(fileMetaData.getFilePath());
			if (fileMetaData.isTemporaryFile() && file.exists() ) {
				System.gc();
				Thread.sleep(3000);
				file.delete();
			}
			fileMetaData = null;
			charset = null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected String getRowDelimiter() {
		int lineFeeds = 0;
		int carriageReturnLineFeed = 0;
		int carriageReturns = 0;
		String delimiter = Default.UNIX_LINE_DELIMITER;
		StringBuilder sb = new StringBuilder();

		for (MappedByteBuffer byteBuffer : byteBuffers) {
			byteBuffer.rewind();
			char pp = 'p';
			char p = ' ';
			while (byteBuffer.hasRemaining()) {
				char c = decodeSingleByteToChar(byteBuffer.get());
				if (p == Default.CARRIAGE_RETURN_CHAR && c == Default.LINE_FEED_CHAR) {
					carriageReturnLineFeed++;;
				}
				else if (p != Default.CARRIAGE_RETURN_CHAR && c == Default.LINE_FEED_CHAR) {
					lineFeeds++;
				}
				else if (pp != Default.LINE_FEED_CHAR && p == Default.CARRIAGE_RETURN_CHAR && c != Default.LINE_FEED_CHAR) {
					carriageReturns++;
				}
				pp = p;
				p = c;
			}
			byteBuffer.rewind();
		}

		sb.append("Source '").append(getAlias()).append("': Auto detected line delimiter as ");
		if (lineFeeds > carriageReturnLineFeed && lineFeeds > carriageReturns) {
			sb.append("LF (Unix)");
			delimiter = String.valueOf(Default.LINE_FEED_CHAR);
		}
		else if (carriageReturnLineFeed > lineFeeds && carriageReturnLineFeed > carriageReturns) {
			sb.append("CR + LF (Windows)");
			delimiter = Default.WINDOWS_LINE_DELIMITER;
		}
		else if (carriageReturns > lineFeeds && carriageReturns > carriageReturnLineFeed) {
			sb.append("CR (Mac)");
			delimiter = Default.MAC_LINE_DELIMITER;
		}
		else {
			delimiter = Default.NO_DELIMITER;
		}
		LOGGER.info(sb.toString());
		return delimiter;
	}

	protected Key createKey(List<?> fields, String line, int lineNumber) {
		List<String> keys =  new ArrayList<String>();
		Key key = null;
		try {
			if (fileMetaData.getKeyColumns().contains(Default.EMPTY_KEY_COLUMN_NAME) &&
					fileMetaData.getKeyColumns().size() == 1) {
				key = new Key(String.valueOf(lineNumber + 1));
			}
			else {
				if (fileMetaData.getKeyColumnIndexes().isEmpty()) {
					throw new RuntimeException("Source '" + getAlias() + "': Key fields config is  " + fileMetaData.getKeyColumns() + " and field defined are " + fileMetaData.getColumns());
				}
				for (Integer index : fileMetaData.getKeyColumnIndexes()) {
					keys.add(fields.get(index) == null ? Default.NULL : fields.get(index).toString());
				}
				key = new Key(keys);
			}
			if (getSourceDataMap().containsKey(key)) {
				LOGGER.warn("Source '" + getAlias() + "': A duplicate key was found for: " + key.toOutputString(Default.COMMA) + " will suffix with a unique id");
				int i = 0;
				Key suffixedKey = new Key(key.toString() + "_" + i);
				while (getSourceDataMap().containsKey(suffixedKey)) {
					i++;
					suffixedKey = new Key(key.toString() + "_" + i);
				}
				key = suffixedKey;
			}
		} catch (Exception e) {
			LOGGER.error("Source '" + getAlias() + "': An error occurred trying to extract a key from the following line: '" + line + "'");
		}
		return key;
	}

	protected abstract Map<Key, ?> getSourceDataMap();

	protected abstract void addRow(Key key, List<?> fields, int byteBufferStart, int start, int byteBufferEnd, int end);

	public Source<Key> call() {
		StringBuilder line = new StringBuilder();
		boolean isFirstRow = true;
		boolean isIgnoreHeaderRow = fileMetaData.isIgnoreHederRow();
		String delimiter = getRowDelimiter();

		LOGGER.info("Source '" + getAlias() + "': Processing file: " + fileMetaData.getFilePath());
		boolean columnNamesNotSupplied = fileMetaData.getColumnNames().contains(Default.UNKNOWN_COLUMN_NAME);
		if (columnNamesNotSupplied) {
			isIgnoreHeaderRow = true;
		}

		long startTimeMillis = System.currentTimeMillis();
		int count = 0;
		int byteBufferStartIndex = 0;
		int start = 0;
		char previousChar = ' ';
		
		for (int byteBufferIndex = 0; byteBufferIndex < byteBuffers.size(); byteBufferIndex++) {
			MappedByteBuffer byteBuffer = byteBuffers.get(byteBufferIndex);
			byteBuffer.rewind();
		
			while (byteBuffer.hasRemaining()) {
				char currentChar = decodeSingleByteToChar(byteBuffer.get());
				if ( delimiter.length() == 1 && isCurrentLineDelimiter(delimiter, currentChar)
						|| (delimiter.length() == 2 && isCurrentLineDelimiter(delimiter, previousChar, currentChar))
						|| !byteBuffer.hasRemaining() ) {
					if (isFirstRow && isIgnoreHeaderRow) {
						if (columnNamesNotSupplied  && !line.toString().isEmpty()) {
							List<?> columns = parseRow(line.toString(), getHeaderColumnTypes(fileMetaData.getColumns().size()));
							fileMetaData = FileMetaData.valueOf(fileMetaData, columns);
						}
						isFirstRow = false;
					}
					else {
						if (line.length() != 0) {
							List<?> fields = parseRow(line.toString(), fileMetaData.getColumnTypes());
							Key key = createKey(fields, line.toString(), count);
							addRow(key, fields, byteBufferStartIndex, start, byteBufferIndex, byteBuffer.position() - delimiter.length());
							count++;
							if (count % 10000 == 0) {
								LOGGER.info("Source '" + getAlias() + "': Processed " + count + " rows");
							}
						}
					}
					start = byteBuffer.position();
					line.setLength(0);
				} else if (!delimiter.contains(String.valueOf(currentChar))) {
					line.append(currentChar);
				}
				previousChar = currentChar;
			}
		}
		LOGGER.info("Source '" + getAlias() + "': Processed " + count + " rows");
		executionTimeMillis = System.currentTimeMillis() - startTimeMillis;
		return this;
	}

	public long getExecutionTimeMillis() {
		return executionTimeMillis;
	}

}
