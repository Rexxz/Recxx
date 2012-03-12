package org.recxx.destination;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.ConsoleMetaData;
import org.recxx.domain.Difference;
import org.recxx.domain.Header;
import org.recxx.domain.Summary;

public class ConsoleDestination extends AbstractDestination {
	
	public ConsoleDestination (ConsoleMetaData consoleMetaData) {
		setDelimiter(consoleMetaData.getDelimiter());
		setLineDelimiter(consoleMetaData.getLineDelimiter());
		List<String> dateFormats = consoleMetaData.getDateFormats();
		if (dateFormats != null && dateFormats.size() != 0) {
			setDateFormatter(new SimpleDateFormat(dateFormats.get(0)));
		}
	}

	public void open() throws IOException {
		return;
	}

	public void writeHeader(RecxxConfiguration configuration) {
		Header header = new Header(configuration);
		System.err.println(header.toOutputString(getDelimiter(), getLineDelimiter()));
	}
	
	public void writeDifference(Difference difference) {
		System.err.println(difference.toOutputString(getDelimiter(), getDateFormatter(), getPercentFormatter()));
	}

	public void writeSummary(Summary summary) {
		setSummary(summary);
		System.err.println(summary.toOutputString(getDelimiter(), getLineDelimiter(), getPercentFormatter()));
	}

	public void close() throws IOException {
		return;
	}

}
