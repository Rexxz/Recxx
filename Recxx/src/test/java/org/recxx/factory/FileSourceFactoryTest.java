package org.recxx.factory;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.source.CachedFileSource;

public class FileSourceFactoryTest {

	private static final File FILE = new File(System.getProperty("user.dir"), "test.txt");
	
	private static final String TEST_DATA = "Id, Name" + System.getProperty("line.separator")
												+ "1,Name1" + System.getProperty("line.separator")
												+ "2,Name2" + System.getProperty("line.separator");
	
	private PropertiesConfiguration propertiesConfig;
	private RecxxConfiguration config;
	private SourceFactory factory = new FileSourceFactory();

	@Before
	public void setup() throws IOException {
		FileUtils.writeStringToFile(FILE, TEST_DATA);
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
		config.setProperty("source1.filePath", FILE.getPath());
		factory.getSource("source1", CachedFileSource.class, config);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetSourceWithNoColumns() {
		config.setProperty("source1.filePath", FILE.getPath());
		config.setProperty("source1.keyColumns", new String[]{"Id"});
		factory.getSource("source1", CachedFileSource.class, config);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetSourceWithIncorrectColumnTypes() {
		config.setProperty("source1.filePath", FILE.getPath());
		config.setProperty("source1.keyColumns", new String[]{"Id"});
		config.setProperty("source1.columns", new String[]{"Id|Integer", "Name|NonExistentType"});
		factory.getSource("source1", CachedFileSource.class, config);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetSourceWithIncorrectColumnTypesSeparator() {
		config.setProperty("source1.filePath", FILE.getPath());
		config.setProperty("source1.keyColumns", new String[]{"Id"});
		config.setProperty("source1.columns", new String[]{"Id|Integer", "Name:String"});
		factory.getSource("source1", CachedFileSource.class, config);
	}
	
	@Test
	public void testGetSourceWithEnoughParams() {
		config.setProperty("source1.filePath", FILE.getPath());
		config.setProperty("source1.keyColumns", new String[]{"Id"});
		config.setProperty("source1.columns", new String[]{"Id|Integer", "Name|String"});
		factory.getSource("source1", CachedFileSource.class, config);
	}

	@After
	public void tearDown() {
		if (FILE.exists()) FILE.delete();
	}
	

}
