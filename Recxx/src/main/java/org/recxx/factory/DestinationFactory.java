package org.recxx.factory;

import org.recxx.destination.Destination;
import org.recxx.configuration.RecxxConfiguration;

public interface DestinationFactory {
	
	Destination getDestination(Class<?> sourceClass, RecxxConfiguration configuration, String alias);
	
}
