package org.recxx.factory;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.source.CachedFileSource;
import org.recxx.utils.FileUtils;

public class FileSourceFactoryTest {

	private static final String FILE_NAME = System.getProperty("user.dir") 
												+ System.getProperty("file.separator") 
												+ "test.txt";

	private static final File FILE = new File(FILE_NAME);
	
	private static final String TEST_DATA = "Id, Name" + System.getProperty("line.separator")
												+ "1,Name1" + System.getProperty("line.separator")
												+ "2,Name2" + System.getProperty("line.separator");
	
	private PropertiesConfiguration propertiesConfig;
	private RecxxConfiguration config;
	private SourceFactory factory = new FileSourceFactory();

	@Before
	public void setup() throws IOException {
		FileUtils.writeFile(TEST_DATA, FILE);
		propertiesConfig = new PropertiesConfiguration();
		config = new RecxxConfiguration(propertiesConfig);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetSourceWithNoFilePath() {
		factory.getSource("source1", CachedFileSource.class, config);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetSourceWithNonExistentFilePath() {
		config.setProperty("source1.filePath", "DoesNotExist");
		factory.getSource("source1", CachedFileSource.class, config);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetSourceWithNoKeyColumns() {
		config.setProperty("source1.filePath", FILE_NAME);
		factory.getSource("source1", CachedFileSource.class, config);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetSourceWithNoColumns() {
		config.setProperty("source1.filePath", FILE_NAME);
		config.setProperty("source1.keyColumns", new String[]{"Id"});
		factory.getSource("source1", CachedFileSource.class, config);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetSourceWithIncorrectColumnTypes() {
		config.setProperty("source1.filePath", FILE_NAME);
		config.setProperty("source1.keyColumns", new String[]{"Id"});
		config.setProperty("source1.columns", new String[]{"Id|Integer", "Name|NonExistentType"});
		factory.getSource("source1", CachedFileSource.class, config);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetSourceWithIncorrectColumnTypesSeparator() {
		config.setProperty("source1.filePath", FILE_NAME);
		config.setProperty("source1.keyColumns", new String[]{"Id"});
		config.setProperty("source1.columns", new String[]{"Id|Integer", "Name:String"});
		factory.getSource("source1", CachedFileSource.class, config);
	}
	
	@Test
	public void testGetSourceWithEnoughParams() {
		config.setProperty("source1.filePath", FILE_NAME);
		config.setProperty("source1.keyColumns", new String[]{"Id"});
		config.setProperty("source1.columns", new String[]{"Id|Integer", "Name|String"});
		factory.getSource("source1", CachedFileSource.class, config);
	}

	@After
	public void tearDown() {
		if (FILE.exists()) FILE.delete();
	}
	

}
