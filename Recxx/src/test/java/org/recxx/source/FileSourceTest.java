package org.recxx.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
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
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;

public class FileSourceTest {

	private static Logger LOGGER = Logger.getLogger(FileSourceTest.class); 

	private static final int FILE_ROW_COUNT = 100000;
	private static String filePath = System.getProperty("java.io.tmpdir") + 
								System.getProperty("file.separator") +
								"test.csv";
	private static List<String> keyColumns = Arrays.asList("Id","Name", "Balance", "Date");
	private static Column col1 = new Column("Id", Integer.class);
	private static Column col2 = new Column("Name", String.class);
	private static Column col3 = new Column("Balance", Double.class);
	private static Column col4 = new Column("Date", Date.class);
	
	private static List<Column> columns = Arrays.asList(col1, col2, col3, col4);
	private static String delimiter = ",";
	private static String lineDelimiter = System.getProperty("line.separator");
	private static boolean ignoreHeaderRow = true;
	private static List<String> dateFormats = Arrays.asList("EEE MMM dd HH:mm:ss z yyyy");

	private static FileMetaData fileMetaData = new FileMetaData.Builder()
												.filePath(filePath)
												.keyColumns(keyColumns)
												.columns(columns)
												.delimiter(delimiter)
												.lineDelimiter(lineDelimiter)
												.ignoreHeaderRow(ignoreHeaderRow)
												.dateFormats(dateFormats)
												.build();
	
	private FileSource mappedFileSource;
	private FileSource cachedFileSource;
	private Key key;

	@Before
	public void setup() throws Exception {
		cachedFileSource = new CachedFileSource("Name", fileMetaData);
		cachedFileSource.call();
		mappedFileSource = new RandomAccessFileSource("Name", fileMetaData);
		mappedFileSource.call();
	}
	
	@Test
	public void testGetKeySetSize() {
		assertEquals(FILE_ROW_COUNT, mappedFileSource.getKeySet().size());
		assertEquals(FILE_ROW_COUNT, cachedFileSource.getKeySet().size());
	}

	@Test
	public void testGetKeyRow() {
		key = cachedFileSource.getKeySet().iterator().next();
		assertNotNull((List<?>) cachedFileSource.getRow(key));
		key = mappedFileSource.getKeySet().iterator().next();
		assertNotNull((List<?>) mappedFileSource.getRow(key));
	}	
	
	@BeforeClass
	public static void setUp() throws Exception {
		
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

	@AfterClass
	public static void tearDown() throws Exception {
		File file = new File(fileMetaData.getFilePath());
		if (file.exists()) {
			LOGGER.debug("File exists " + file.getPath() + " deleting");
			file.delete();
		}
	}
}
