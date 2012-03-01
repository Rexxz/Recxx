package org.recxx.destination;

import java.io.IOException;

import org.recxx.domain.Difference;
import org.recxx.domain.Summary;

public interface Destination {

	void open() throws IOException;
	
	void writeDifference(Difference difference);
	
	void writeSummary(Summary summary);
	
	void close() throws IOException;

}
