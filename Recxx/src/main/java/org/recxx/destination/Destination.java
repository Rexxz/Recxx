package org.recxx.destination;

import java.io.IOException;

import org.recxx.domain.Difference;
import org.recxx.domain.Key;
import org.recxx.domain.Summary;
import org.recxx.source.Source;

public interface Destination {

	abstract void open() throws IOException;
	
	abstract void writeHeader(Source<Key> source1, Source<Key> source2);

	abstract void writeDifference(Difference difference);
	
	abstract void writeSummary(Summary summary);
	
	abstract Summary getSummary();

	abstract void close() throws IOException;

}
