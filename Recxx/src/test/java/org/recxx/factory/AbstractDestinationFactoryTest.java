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
import org.recxx.destination.ConsoleDestination;
import org.recxx.destination.Destination;

@RunWith(MockitoJUnitRunner.class)
public class AbstractDestinationFactoryTest {

	private AbstractDestinationFactory factory = new AbstractDestinationFactory();
	private PropertiesConfiguration propertiesConfig;
	private RecxxConfiguration config;
	@Mock Destination destination;
	@Mock DestinationFactory destinationFactory;

	@Before
	public void setup() {
		propertiesConfig = new PropertiesConfiguration();
		config = new RecxxConfiguration(propertiesConfig);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetDestinationsWithNoConfig() {
		factory.getDestinations(config);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetDestinationsWithNoTypeConfig() {
		config.setProperty("destinations", new String[]{"Destination1"});
		factory.getDestinations(config);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetDestinationsWithInvalidDestinationTypes() {
		config.setProperty("destinations", new String[]{"Destination1"});
		config.setProperty("Destination1.type", "DestinationTypeDoesNotExist");
		factory.getDestinations(config);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetDestinationsWithMockDestinationAndDestinationFactoryButDestinationClassDoesntExist() {
		config.setProperty("destinations", new String[]{"Destination1"});
		config.setProperty("Destination1.type", "DestinationTypeDoesNotExist");
		when(destinationFactory.getDestination(destination.getClass(), config, "Destination1")).thenReturn(destination);
		assertEquals(1, factory.getDestinations(config).size());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetDestinationsWithMockDestinationAndDestinationFactoryButTypeNotAdded() {
		config.setProperty("destinations", new String[]{"Destination1"});
		config.setProperty("Destination1.type", destination.getClass().getName());
		when(destinationFactory.getDestination(destination.getClass(), config, "Destination1")).thenReturn(destination);
		assertEquals(1, factory.getDestinations(config).size());
	}
	
	@Test
	public void testGetDestinationsWithMockDestinationAndDestinationFactoryWithTypeAdded() {
		factory.addDestinationFactory(destination.getClass(), destinationFactory);
		config.setProperty("destinations", new String[]{"Destination1"});
		config.setProperty("Destination1.type", destination.getClass().getName());
		when(destinationFactory.getDestination(destination.getClass(), config, "Destination1")).thenReturn(destination);
		assertEquals(1, factory.getDestinations(config).size());
	}
	
	@Test
	public void testGetDestinationsWithMockDestinationAndDestinationFactoryWithExistingAdded() {
		config.setProperty("destinations", new String[]{"Destination1"});
		config.setProperty("Destination1.type", ConsoleDestination.class.getName());
		when(destinationFactory.getDestination(destination.getClass(), config, "Destination1")).thenReturn(destination);
		assertEquals(1, factory.getDestinations(config).size());
	}

}
