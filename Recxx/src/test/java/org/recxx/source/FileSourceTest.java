package org.recxx.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.recxx.domain.Column;
import org.recxx.domain.Default;
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;

public class FileSourceTest {

	private static Logger LOGGER = Logger.getLogger(FileSourceTest.class); 

	private static final int FILE_ROW_COUNT = 10000;
	private static String unixFilePath = System.getProperty("java.io.tmpdir") + 
			System.getProperty("file.separator") +
			"unixFile.csv";
	private static String macFilePath = System.getProperty("java.io.tmpdir") + 
			System.getProperty("file.separator") +
			"macFile.csv";
	private static String windowsFilePath = System.getProperty("java.io.tmpdir") + 
			System.getProperty("file.separator") +
			"windowsFile.csv";
	private static List<String> keyColumns = Arrays.asList("Id","Name", "Balance", "Date");
	private static List<String> compareColumnNames = Arrays.asList("Name", "Balance");

	private static Column col1 = new Column("Id", Integer.class);
	private static Column col2 = new Column("Name", String.class);
	private static Column col3 = new Column("Balance", Double.class);
	private static Column col4 = new Column("Date", Date.class);
	
	private static List<Column> columns = Arrays.asList(col1, col2, col3, col4);
	private static String delimiter = ",";
	private static boolean ignoreHeaderRow = true;
	private static List<String> dateFormats = Arrays.asList("EEE MMM dd HH:mm:ss z yyyy");

	private static FileMetaData unixFileMetaData = new FileMetaData.Builder()
													.filePath(unixFilePath)
													.keyColumns(keyColumns)
													.columns(columns)
													.delimiter(delimiter)
													.lineDelimiter(Default.UNIX_LINE_DELIMITER)
													.columnsToCompare(compareColumnNames)
													.ignoreHeaderRow(ignoreHeaderRow)
													.dateFormats(dateFormats)
													.build();
	
	private static FileMetaData macFileMetaData = new FileMetaData.Builder()
													.filePath(macFilePath)
													.keyColumns(keyColumns)
													.columns(columns)
													.delimiter(delimiter)
													.lineDelimiter(Default.MAC_LINE_DELIMITER)
													.ignoreHeaderRow(ignoreHeaderRow)
													.dateFormats(dateFormats)
													.build();

	private static FileMetaData windowsFileMetaData = new FileMetaData.Builder()
													.filePath(windowsFilePath)
													.keyColumns(keyColumns)
													.columns(columns)
													.delimiter(delimiter)
													.lineDelimiter(Default.WINDOWS_LINE_DELIMITER)
													.ignoreHeaderRow(ignoreHeaderRow)
													.dateFormats(dateFormats)
													.build();

	private FileSource unixRandomAccessFileSource;
	private FileSource unixCachedFileSource;
	private FileSource macCachedFileSource;
	private FileSource windowsCachedFileSource;
	private Key key;

	@Before
	public void setup() throws Exception {
		unixCachedFileSource = new CachedFileSource("CachedFileSourceAlias", unixFileMetaData);
		unixCachedFileSource.call();
		unixRandomAccessFileSource = new RandomAccessFileSource("RandomAccessFileSourceAlias", unixFileMetaData);
		unixRandomAccessFileSource.call();
		macCachedFileSource = new CachedFileSource("CachedFileSourceAlias", macFileMetaData);
		macCachedFileSource.call();
		windowsCachedFileSource = new CachedFileSource("CachedFileSourceAlias", windowsFileMetaData);
		windowsCachedFileSource.call();
	}
	
	@Test
	public void testGetAlias() {
		assertEquals("CachedFileSourceAlias", unixCachedFileSource.getAlias());
		assertEquals("RandomAccessFileSourceAlias", unixRandomAccessFileSource.getAlias());
	}
	
	@Test
	public void testGetColumnIndex() {
		assertEquals(0, unixCachedFileSource.getColumnIndex(columns.get(0).getName()));
	}

	@Test
	public void testGetColumns() {
		assertEquals(columns, unixCachedFileSource.getColumns());
	}

	@Test
	public void testGetCompareColumns() {
		assertEquals(compareColumnNames, unixCachedFileSource.getCompareColumns());
	}
	
	@Test
	public void testGetKeySetSize() {
		assertEquals(FILE_ROW_COUNT, unixRandomAccessFileSource.getKeySet().size());
		assertEquals(FILE_ROW_COUNT, unixCachedFileSource.getKeySet().size());
	}

	@Test
	public void testGetKeyRow() {
		key = unixCachedFileSource.getKeySet().iterator().next();
		assertNotNull((List<?>) unixCachedFileSource.getRow(key));
		key = unixRandomAccessFileSource.getKeySet().iterator().next();
		assertNotNull((List<?>) unixRandomAccessFileSource.getRow(key));
	}
	
	@Test
	public void testGetRowDelimiter() {
		assertEquals(Default.UNIX_LINE_DELIMITER, unixCachedFileSource.getRowDelimiter());
		assertEquals(Default.MAC_LINE_DELIMITER, macCachedFileSource.getRowDelimiter());
		assertEquals(Default.WINDOWS_LINE_DELIMITER, windowsCachedFileSource.getRowDelimiter());
	}
	
	@Test(expected=NullPointerException.class)
	public void testClose() throws Exception {
		unixCachedFileSource.close();
		unixCachedFileSource.call();		
	}

	@BeforeClass
	public static void setUp() throws Exception {
		buildFile(unixFileMetaData);
		buildFile(macFileMetaData);
		buildFile(windowsFileMetaData);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		deleteFile(unixFileMetaData);
		deleteFile(macFileMetaData);
		deleteFile(windowsFileMetaData);
	}

	private static void buildFile(FileMetaData fileMetaData) throws FileNotFoundException, IOException {
		File file = new File(fileMetaData.getFilePath());
		if (file.exists()) file.delete();
	
		RandomAccessFile randomAccessFile = new RandomAccessFile(fileMetaData.getFilePath(), "rw");
		StringBuilder builder = new StringBuilder();
		for (Column column : fileMetaData.getColumns()) {
			if (builder.length() > 0) builder.append(fileMetaData.getDelimiter());
			builder.append(column.getName()); 
		}
		builder.append(fileMetaData.getLineDelimiter());
		randomAccessFile.writeBytes(builder.toString());
		
		int totalRows = FILE_ROW_COUNT;
		for (int i = 1; i <= totalRows; i++) {
			randomAccessFile.writeBytes("" + 
										i +
										fileMetaData.getDelimiter() +
										"Name" + i +
										fileMetaData.getDelimiter() +
										i * 100d +
										fileMetaData.getDelimiter() +
										new GregorianCalendar(1900, Calendar.JANUARY, 1).getTime().toString() +
										fileMetaData.getLineDelimiter());
			if (i % 1000 == 0) {
				LOGGER.debug("Written " + i + " of " + totalRows);
			} 
		}
		
		randomAccessFile.close();
	}

	private static void deleteFile(FileMetaData fileMetaData) {
		File file = new File(fileMetaData.getFilePath());
		if (file.exists()) {
			LOGGER.debug("File exists " + file.getPath() + " deleting");
			file.delete();
		}
	}
}
