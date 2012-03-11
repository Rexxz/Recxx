package org.recxx;

import static org.recxx.utils.ReconciliationAssert.assertReconciles;
import static org.recxx.utils.ReconciliationAssert.failsToReconcile;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.destination.ConsoleDestination;
import org.recxx.destination.CsvDestination;
import org.recxx.source.CachedFileSource;
import org.recxx.utils.FileAssert;

public class Recxx2Test {

	private static final String TEMP_OUTPUT_FILE_CSV = "tempOutputFile.csv";
	
	private static RecxxConfiguration fileConfig;

	@Test
	public void filesDifferAndFailsReconcile() throws Exception {
		String expectedOutputFile = Recxx2Test.class.getResource("expected0.01PercentTolerance.csv").getPath();
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		fileConfig.setProperty("toleranceLevelPercent", "0.01");
		failsToReconcile(fileConfig);
		FileAssert.assertEquals(expectedOutputFile, actualOutputFile);
	}

	@Test
	public void filesDifferAndFailsReconcileZeroPercentTolerance() throws Exception {
		String expectedOutputFile = Recxx2Test.class.getResource("expected0.00PercentTolerance.csv").getPath();
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("toleranceLevelPercent", "0.00");
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		failsToReconcile(fileConfig);
		FileAssert.assertEquals(expectedOutputFile, actualOutputFile);
	}

	@Test
	public void filesDifferAndFailsReconcileOnePercentTolerance() throws Exception {
		String expectedOutputFile = Recxx2Test.class.getResource("expected1.00PercentTolerance.csv").getPath();
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("toleranceLevelPercent", "1.00");
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		failsToReconcile(fileConfig);
		FileAssert.assertEquals(expectedOutputFile, actualOutputFile);
	}
	
	@Test
	public void filesDifferAndFailsReconcileZeroPercentToleranceCaseDifference() throws Exception {
		String expectedOutputFile = Recxx2Test.class.getResource("expected0.00PercentToleranceCaseDiff.csv").getPath();
		String actualOutputFile = FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV;
		fileConfig.setProperty("source1.filePath", Recxx2Test.class.getResource("source1_10_upper.csv").getPath());
		fileConfig.setProperty("ignoreCase", "true");
		fileConfig.setProperty("toleranceLevelPercent", "0.00");
		fileConfig.setProperty("csvFile.filePath", actualOutputFile);
		failsToReconcile(fileConfig);
		FileAssert.assertEquals(expectedOutputFile, actualOutputFile);
	}
	
	@Test
	public void filesSourcesSameAndReconciles() throws Exception {
		fileConfig.setProperty("source2.filePath", Recxx2Test.class.getResource("source1_10.csv").getPath());
		assertReconciles(fileConfig);
	}

	@Test
	public void filesSourcesUtf16SameAndReconciles() throws Exception {
		fileConfig.setProperty("source1.filePath", Recxx2Test.class.getResource("source1_UTF16_10.csv").getPath());
		fileConfig.setProperty("source2.filePath", Recxx2Test.class.getResource("source1_UTF16_10.csv").getPath());
		assertReconciles(fileConfig);
	}

	@Test(expected=ExecutionException.class)
	public void filesDifferNoDateFormatSpecified() throws Exception {
		fileConfig.setProperty("dateFormats", null);
		assertReconciles(fileConfig);
	}
	
	@BeforeClass
	public static void setUp() throws Exception {
		
		fileConfig = new RecxxConfiguration();
		
		fileConfig.setProperty("dateFormats", "EEE MMM dd HH:mm:ss z yyyy");

		fileConfig.setProperty("sources", Arrays.asList("source1", "source2"));
		
		fileConfig.setProperty("source1.type", CachedFileSource.class.getName());
		fileConfig.setProperty("source1.filePath", Recxx2Test.class.getResource("source1_10.csv").getPath());
		fileConfig.setProperty("source1.delimiter", ",");
		fileConfig.setProperty("source1.columns", Arrays.asList("Id|Integer", "Name|String", "Balance|Double", "Date|Date"));
		fileConfig.setProperty("source1.keyColumns", "Id");

		fileConfig.setProperty("source2.type", CachedFileSource.class.getName());
		fileConfig.setProperty("source2.filePath", Recxx2Test.class.getResource("source2_10.csv").getPath());
		fileConfig.setProperty("source2.delimiter", ",");
		fileConfig.setProperty("source2.columns", Arrays.asList("Id|Integer", "Name|String", "Balance|Double", "Date|Date"));
		fileConfig.setProperty("source2.keyColumns", "Id");
		
		fileConfig.setProperty("destinations", "csvFile, console");
		fileConfig.setProperty("csvFile.type", CsvDestination.class.getName());
		fileConfig.setProperty("csvFile.filePath", "");
		fileConfig.setProperty("console.type", ConsoleDestination.class.getName());
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		FileUtils.deleteQuietly(new File(FileUtils.getTempDirectoryPath() + TEMP_OUTPUT_FILE_CSV));
	}
	
	// TODO Excel Test
	// TODO Database Test


	


}
