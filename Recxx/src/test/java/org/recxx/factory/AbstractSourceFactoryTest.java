package org.recxx.factory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.Key;
import org.recxx.source.ExcelSource;
import org.recxx.source.Source;

@RunWith(MockitoJUnitRunner.class)
public class AbstractSourceFactoryTest {

	private AbstractSourceFactory factory = new AbstractSourceFactory();
	private PropertiesConfiguration propertiesConfig;
	private RecxxConfiguration config;
	@Mock Source<Key> source;
	@Mock SourceFactory sourceFactory;

	@Before
	public void setup() {
		propertiesConfig = new PropertiesConfiguration();
		config = new RecxxConfiguration(propertiesConfig);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetSourcesWithNoConfig() {
		factory.getSources(config);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetSourcesWithNoTypeConfig() {
		config.setProperty("sources", new String[]{"source1"});
		factory.getSources(config);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetSourcesWithInvalidSourceTypes() {
		config.setProperty("sources", new String[]{"source1", "source2"});
		config.setProperty("source1.type", "DoesNotExist");
		config.setProperty("source2.type", "DoesNotExist");
		factory.getSources(config);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetSourcesWithMockSourceAndSourceFactoryButSourceDoesntExist() {
		config.setProperty("sources", new String[]{"source1", "source2"});
		config.setProperty("source1.type", source.getClass().getName());
		config.setProperty("source2.type", source.getClass().getName());
		when(sourceFactory.getSource("source1", source.getClass(), config)).thenReturn(source);
		when(sourceFactory.getSource("source2", source.getClass(), config)).thenReturn(source);
		assertEquals(2, factory.getSources(config).size());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetSourcesWithOneExcelSource() {
		factory.addSourceFactory(source.getClass(), sourceFactory);
		config.setProperty("sources", new String[]{"source1", "source2"});
		config.setProperty("source1.type", source.getClass().getName());
		config.setProperty("source2.type", ExcelSource.class.getName());
		assertEquals(2, factory.getSources(config).size());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetSourcesWithThreeSources() {
		factory.addSourceFactory(source.getClass(), sourceFactory);
		config.setProperty("sources", new String[]{"source1", "source2", "source3"});
		config.setProperty("source1.type", source.getClass().getName());
		config.setProperty("source2.type", ExcelSource.class.getName());
		config.setProperty("source3.type", ExcelSource.class.getName());
		assertEquals(2, factory.getSources(config).size());
	}
	
	@Test
	public void testGetSourcesWithMockSourceAndSourceFactory() {
		factory.addSourceFactory(source.getClass(), sourceFactory);
		config.setProperty("sources", new String[]{"source1", "source2"});
		config.setProperty("source1.type", source.getClass().getName());
		config.setProperty("source2.type", source.getClass().getName());
		when(sourceFactory.getSource("source1", source.getClass(), config)).thenReturn(source);
		when(sourceFactory.getSource("source2", source.getClass(), config)).thenReturn(source);
		assertEquals(2, factory.getSources(config).size());
	}
	
}
