package org.recxx.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.recxx.configuration.RecxxConfiguration;
import org.recxx.destination.ConsoleDestination;
import org.recxx.destination.CsvDestination;
import org.recxx.destination.Destination;

public class AbstractDestinationFactory {

	private Map<Class<?>, DestinationFactory> destinationFactoryMap = new HashMap<Class<?>, DestinationFactory>();	

	public AbstractDestinationFactory() {
		addDestinationFactory(CsvDestination.class, new CsvDestinationFactory());
		addDestinationFactory(ConsoleDestination.class, new ConsoleDestinationFactory());
	}

	public List<Destination> getDestinations(RecxxConfiguration configuration) {
		List<Destination> destinations = new ArrayList<Destination>();
		List<String> destinationAliases = configuration.configureDestinationAliases();
		for (String destinationAlias : destinationAliases) {
			String destinationType = configuration.configureDestinationType(destinationAlias, destinationFactoryMap);
			Class<?> destinationClass;
			try {
				destinationClass = Class.forName(destinationType);
				if (destinationFactoryMap.containsKey(destinationClass)) {
					DestinationFactory destinationFactory = destinationFactoryMap.get(destinationClass);
					destinations.add(destinationFactory.getDestination(destinationClass, configuration, destinationAlias));
				}
				else {
					throw new IllegalArgumentException("'" + destinationAlias + ".type' specified incorrectly in configuration, " +
							"configuration requires one of the following values: " + destinationFactoryMap.keySet() + ". " +
							destinationType + " has not been added to the AbstractDestinationFactory, this can be achieved by calling " +
							"addDestinationFactory (Class<?> sourceClass, DestinationFactory destinationFactory)");
				}
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("'" + destinationAlias + ".type' specified incorrectly in configuration, " +
						"configuration requires one of the following values: " + destinationFactoryMap.keySet(), e);
			} 
		}
		return destinations;
	}
	
	public void addDestinationFactory (Class<?> sourceClass, DestinationFactory destinationFactory) {
		destinationFactoryMap.put(sourceClass, destinationFactory);
	}
	
}
