package org.recxx;

import static org.recxx.utils.ReconciliationAssert.assertReconciles;
import static org.recxx.utils.ReconciliationAssert.failsToReconcile;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.destination.ConsoleDestination;
import org.recxx.destination.CsvDestination;
import org.recxx.domain.Default;
import org.recxx.source.CachedFileSource;
import org.recxx.source.RandomAccessFileSource;
import org.recxx.utils.FileAssert;

public class RecxxTest {

	private static final String TEMP_OUTPUT_FILE_CSV = "tempOutputFile.csv";
	
	private static RecxxConfiguration fileConfig;

	@Test
	public void filesDifferAndFailsToReconcile() throws Exception {
		String expectedOutputFile = RecxxTest.class.getResource("expected0.01PercentTolerance.csv").getPath();
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		fileConfig.setProperty("toleranceLevelPercent", "0.01");
		failsToReconcile(fileConfig);
		FileAssert.assertEquals(expectedOutputFile, actualOutputFile);
	}

	@Test
	public void filesDifferAndFailsReconcileZeroPercentTolerance() throws Exception {
		String expectedOutputFile = RecxxTest.class.getResource("expected0.00PercentTolerance.csv").getPath();
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("toleranceLevelPercent", "0.00");
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		failsToReconcile(fileConfig);
		FileAssert.assertEquals(expectedOutputFile, actualOutputFile);
	}

	@Test
	public void filesDifferAndFailsReconcileOnePercentTolerance() throws Exception {
		String expectedOutputFile = RecxxTest.class.getResource("expected1.00PercentTolerance.csv").getPath();
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("toleranceLevelPercent", "1.00");
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		failsToReconcile(fileConfig);
		FileAssert.assertEquals(expectedOutputFile, actualOutputFile);
	}
	
	@Test
	public void filesDifferAndFailsReconcileZeroPercentToleranceCaseDifference() throws Exception {
		String expectedOutputFile = RecxxTest.class.getResource("expected0.00PercentToleranceCaseDiff.csv").getPath();
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("source1.filePath", RecxxTest.class.getResource("source1_10_upper.csv").getPath());
		fileConfig.setProperty("ignoreCase", "true");
		fileConfig.setProperty("toleranceLevelPercent", "0.00");
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		failsToReconcile(fileConfig);
		FileAssert.assertEquals(expectedOutputFile, actualOutputFile);
	}

	@Test
	public void fileSourcesSource1HasExtraColumn() throws Exception {
		fileConfig.setProperty("source1.filePath", RecxxTest.class.getResource("source2ExtraColumn_10.csv").getPath());
		fileConfig.setProperty("source1.columns", Arrays.asList("Id|Integer", "Name|String", "Balance|Double", "Date|Date", "Address|String"));
		failsToReconcile(fileConfig);
	}
	
	@Test
	public void fileSourcesSource2HasExtraColumn() throws Exception {
		fileConfig.setProperty("source2.filePath", RecxxTest.class.getResource("source1ExtraColumn_10.csv").getPath());
		fileConfig.setProperty("source2.columns", Arrays.asList("Id|Integer", "Name|String", "Balance|Double", "Date|Date", "Address|String"));
		failsToReconcile(fileConfig);
	}

	@Test
	public void fileSourcesTheSameAndReconciles() throws Exception {
		fileConfig.setProperty("source2.filePath", RecxxTest.class.getResource("source1_10.csv").getPath());
		assertReconciles(fileConfig);
	}

	@Test
	public void fileSourcesTheSameUtf16AndReconciles() throws Exception {
		fileConfig.setProperty("source1.filePath", RecxxTest.class.getResource("source1_UTF16_10.csv").getPath());
		fileConfig.setProperty("source2.filePath", RecxxTest.class.getResource("source1_UTF16_10.csv").getPath());
		assertReconciles(fileConfig);
	}

	@Test
	public void fileSourcesSameWithColumnHeaderUsedAndReconciles() throws Exception {
		fileConfig.setProperty("source1.columns", Arrays.asList("Integer", "String", "Double", "Date"));
		fileConfig.setProperty("source2.columns", Arrays.asList("Integer", "String", "Double", "Date"));
		fileConfig.setProperty("source2.filePath", RecxxTest.class.getResource("source1_10.csv").getPath());
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		assertReconciles(fileConfig);
	}

	@Test
	public void fileRandomAccessSourcesSameWithColumnHeaderUsedAndReconciles() throws Exception {
		fileConfig.setProperty("source1.type", RandomAccessFileSource.class.getName());
		fileConfig.setProperty("source1.columns", Arrays.asList("Integer", "String", "Double", "Date"));
		fileConfig.setProperty("source2.type", RandomAccessFileSource.class.getName());
		fileConfig.setProperty("source2.columns", Arrays.asList("Integer", "String", "Double", "Date"));
		fileConfig.setProperty("source2.filePath", RecxxTest.class.getResource("source1_10.csv").getPath());
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		assertReconciles(fileConfig);
	}

	@Test
	public void fileRandomAccessSourcesSameWithDateInLastField() throws Exception {
		fileConfig.setProperty("source1.type", RandomAccessFileSource.class.getName());
		fileConfig.setProperty("source1.columns", Arrays.asList("Integer", "String", "Double", "Date"));
		fileConfig.setProperty("source1.filePath", RecxxTest.class.getResource("source1DateLastField_10.csv").getPath());
		fileConfig.setProperty("source2.type", RandomAccessFileSource.class.getName());
		fileConfig.setProperty("source2.columns", Arrays.asList("Integer", "String", "Double", "Date"));
		fileConfig.setProperty("source2.filePath", RecxxTest.class.getResource("source1DateLastField_10.csv").getPath());
		fileConfig.setProperty("dateFormats", "yyyy-MM-dd HH:mm:ss.S");
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		assertReconciles(fileConfig);
	}

	@Test
	public void fileCachedFileSourcesSameWithDateInLastField() throws Exception {
		fileConfig.setProperty("source1.type", CachedFileSource.class.getName());
		fileConfig.setProperty("source1.columns", Arrays.asList("Integer", "String", "Double", "Date"));
		fileConfig.setProperty("source1.filePath", RecxxTest.class.getResource("source1DateLastField_10.csv").getPath());
		fileConfig.setProperty("source2.type", CachedFileSource.class.getName());
		fileConfig.setProperty("source2.columns", Arrays.asList("Integer", "String", "Double", "Date"));
		fileConfig.setProperty("source2.filePath", RecxxTest.class.getResource("source1DateLastField_10.csv").getPath());
		fileConfig.setProperty("dateFormats", "yyyy-MM-dd HH:mm:ss.S");
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		assertReconciles(fileConfig);
	}

	@Test
	public void fileCachedFileSourcesDifferentByIgnoredColumn() throws Exception {
		fileConfig.setProperty("source1.type", CachedFileSource.class.getName());
		fileConfig.setProperty("source1.columns", Arrays.asList("Integer", "String", "Double", "Date"));
		fileConfig.setProperty("source1.filePath", RecxxTest.class.getResource("source1_10.csv").getPath());
		fileConfig.setProperty("source1.columnsToIgnore", Arrays.asList("Date"));
		fileConfig.setProperty("source2.type", CachedFileSource.class.getName());
		fileConfig.setProperty("source2.columns", Arrays.asList("Integer", "String", "Double", "Date"));
		fileConfig.setProperty("source2.filePath", RecxxTest.class.getResource("source2_10_DateDiffOnly.csv").getPath());
		fileConfig.setProperty("source2.columnsToIgnore", Arrays.asList("Date"));
		fileConfig.setProperty("dateFormats", Default.ISO_DATE_FORMAT.toLocalizedPattern());
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		assertReconciles(fileConfig);
	}
	
	@Test(expected=ExecutionException.class)
	public void filesDifferNoDateFormatSpecified() throws Exception {
		fileConfig.setProperty("dateFormats", null);
		assertReconciles(fileConfig);
	}
	
	@Before
	public void setUp() throws Exception {
		
		fileConfig = new RecxxConfiguration();
		fileConfig.setProperty("dateFormats", "EEE MMM dd HH:mm:ss z yyyy");

		fileConfig.setProperty("sources", Arrays.asList("source1", "source2"));
		
		fileConfig.setProperty("source1.type", CachedFileSource.class.getName());
		fileConfig.setProperty("source1.filePath", RecxxTest.class.getResource("source1_10.csv").getPath());
		fileConfig.setProperty("source1.delimiter", ",");
		fileConfig.setProperty("source1.columns", Arrays.asList("Id|Integer", "Name|String", "Balance|Double", "Date|Date"));
		fileConfig.setProperty("source1.keyColumns", "Id");
		fileConfig.setProperty("source1.lineDelimiter", "\n");

		fileConfig.setProperty("source2.type", CachedFileSource.class.getName());
		fileConfig.setProperty("source2.filePath", RecxxTest.class.getResource("source2_10.csv").getPath());
		fileConfig.setProperty("source2.delimiter", ",");
		fileConfig.setProperty("source2.columns", Arrays.asList("Id|Integer", "Name|String", "Balance|Double", "Date|Date"));
		fileConfig.setProperty("source2.keyColumns", "Id");
		fileConfig.setProperty("source2.lineDelimiter", "\n");
		
		fileConfig.setProperty("destinations", "csvFile, console");
		fileConfig.setProperty("csvFile.type", CsvDestination.class.getName());
		fileConfig.setProperty("csvFile.filePath", FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV);
		fileConfig.setProperty("console.type", ConsoleDestination.class.getName());
	}
	
	@After
	public void tearDown() throws Exception {
		FileUtils.deleteQuietly(new File(FileUtils.getTempDirectoryPath(), TEMP_OUTPUT_FILE_CSV));
	}
	
	// TODO Excel Test
	// TODO Database Test

}
