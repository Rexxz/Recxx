package org.recxx.destination;

import java.io.IOException;

import org.recxx.domain.Difference;
import org.recxx.domain.Summary;

public interface Destination {

	abstract void open() throws IOException;
	
	abstract void writeDifference(Difference difference);
	
	abstract void writeSummary(Summary summary);
	
	abstract Summary getSummary();
	
	abstract void close() throws IOException;

}
