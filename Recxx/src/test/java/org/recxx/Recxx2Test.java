package org.recxx;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.Summary;
import org.recxx.utils.FileAssert;

public class Recxx2Test {

	private static File sourceFile1 = new File(System.getProperty("java.io.tmpdir"), "test1.csv");
	private static File sourceFile2 = new File(System.getProperty("java.io.tmpdir"), "test2.csv");
	private static File configFile = new File(System.getProperty("java.io.tmpdir"), "testConfig.properties");
	private static File outputFile = new File(System.getProperty("java.io.tmpdir"), "testOutput.csv");
	private static File compareFile = new File(System.getProperty("java.io.tmpdir"), "testOutputCompare.csv");
	private static String eol = "\n";

	@Test
	public void testRecxxFromPropertiesFile() throws Exception {
		Recxx2 recxx = new Recxx2(configFile.getPath());
		Summary summary = new Summary.Builder()
	        .alias1Count(10)
	        .alias2Count(10)
	        .alias1("prod")
	        .alias2("uat")
	        .matchCount(7)
	        .build();
		assertEquals(summary, recxx.execute());
		FileAssert.assertEquals(outputFile, compareFile);
	}

	@Test
	public void testRecxxFromConfiguration() throws Exception {
		RecxxConfiguration configuration = new RecxxConfiguration(configFile.getPath());
		Recxx2 recxx = new Recxx2(configuration);
		Summary summary = new Summary.Builder()
	        .alias1Count(10)
	        .alias2Count(10)
	        .alias1("prod")
	        .alias2("uat")
	        .matchCount(7)
	        .build();
		assertEquals(summary, recxx.execute());
		FileAssert.assertEquals(outputFile, compareFile);
	}

	@Test
	public void testMain() {
		
	}
	
	@BeforeClass
	public static void setUp() throws Exception {
		
		StringBuilder sb1 = new StringBuilder();
		sb1.append("Id,Name,Balance,Date").append(eol)
		.append("1,Name1,100.0,Tue Jan 02 00:00:00 GMT 2007").append(eol)
		.append("2,Name2,199.999,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("3,Name3,,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("4,Name4,400.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("5,Name5,500.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("6,Name6,600.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("7,Name7,700.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("8,Name8,800.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("9,Name9,900.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("100,Name10,1000.0,Mon Jan 01 00:00:00 GMT 2007").append(eol);

		FileUtils.writeStringToFile(sourceFile1, sb1.toString(), false);

		StringBuilder sb2 = new StringBuilder();
		sb2.append("Id,Name,Balance,Date").append(eol)
		.append("1,Name1,101.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("2,Name2,200.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("3,Name3,300.0,").append(eol)
		.append("4,Name4,400.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("5,Name5,500.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("6,Name6,600.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("7,Name7,700.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("8,Name8,800.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("9,Name9,900.0,Mon Jan 01 00:00:00 GMT 2007").append(eol)
		.append("10,Name10,1000.0,Mon Jan 01 00:00:00 GMT 2007").append(eol);		

		FileUtils.writeStringToFile(sourceFile2, sb2.toString(), false);
		
		StringBuilder sb3 = new StringBuilder();
		sb3.append("toleranceLevelPercent=1").append(eol)
		.append("smallestAbsoluteValue=0.00001").append(eol)
		.append("dateFormats=EEE MMM dd HH:mm:ss z yyyy").append(eol)
		
		.append("sources=prod, uat").append(eol)
		
		.append("prod.type=org.recxx.source.CachedFileSource").append(eol)
		.append("prod.filePath=").append(sourceFile1).append(eol)
		.append("prod.delimiter=,").append(eol)
		.append("prod.lineDelimiter=\\n").append(eol)
		.append("prod.columns=Id|Integer, Name|String, Balance|Double, Date|Date").append(eol)
		.append("prod.keyColumns=Id").append(eol)

		.append("uat.type=org.recxx.source.CachedFileSource").append(eol)
		.append("uat.filePath=").append(sourceFile2).append(eol)
		.append("uat.delimiter=,").append(eol)
		.append("uat.lineDelimiter=\\n").append(eol)
		.append("uat.columns=Id|Integer, Name|String, Balance|Double, Date|Date").append(eol)
		.append("uat.keyColumns=Id").append(eol)

		.append("destinations=csvFile").append(eol)
		
		.append("csvFile.type=org.recxx.destination.CsvDestination").append(eol)
		.append("csvFile.filePath=").append(outputFile).append(eol)
		.append("csvFile.dateFormat=dd-MMM-yyyy").append(eol)
		.append("csvFile.delimiter=,").append(eol)
		
		.append("retainer.type=org.recxx.destination.RetainingDestination").append(eol);
		
		FileUtils.writeStringToFile(configFile, sb3.toString(), false);
		
		StringBuilder sb4 = new StringBuilder();
		sb4.append("Key: Id,column,prod.value,uat.value,% Diff,ABS Diff").append(eol)
		.append("100,Id,100,Missing,,").append(eol)
		.append("100,Name,Name10,Missing,,").append(eol)
		.append("100,Balance,1000.0,Missing,,").append(eol)
		.append("100,Date,01-Jan-2007,Missing,,").append(eol)
		.append("3,Balance,0.0,300.0,100.00%,300.0").append(eol)
		.append("3,Date,01-Jan-2007,null,,").append(eol)
		.append("1,Date,02-Jan-2007,01-Jan-2007,,").append(eol)
		.append("10,Name,Missing,Name10,,").append(eol)
		.append("10,Balance,Missing,1000.0,,").append(eol)
		.append("10,Date,Missing,01-Jan-2007,,").append(eol)
		.append("").append(eol)
		.append("").append(eol)
		.append("======================").append(eol)
		.append("Reconciliation Summary").append(eol)
		.append("======================").append(eol)
		.append("prod rows: ,10").append(eol)
		.append("uat rows: ,10").append(eol)
		.append("prod matched uat : ,7").append(eol)
		.append("prod matched uat % : ,70.00%").append(eol)
		.append("uat matched prod % : ,70.00%").append(eol)
		.append("").append(eol);

		FileUtils.writeStringToFile(compareFile, sb4.toString(), false);

	}

	@AfterClass
	public static void tearDown() throws Exception {
		FileUtils.deleteQuietly(sourceFile1);
		FileUtils.deleteQuietly(sourceFile2);
		FileUtils.deleteQuietly(configFile);
		FileUtils.deleteQuietly(outputFile);
		FileUtils.deleteQuietly(compareFile);
	}
	


}
