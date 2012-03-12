package org.recxx.destination;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.domain.Default;
import org.recxx.domain.Difference;
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Header;
import org.recxx.domain.Summary;

public class CsvDestination extends AbstractDestination {

	private static final Logger LOGGER = Logger.getLogger(CsvDestination.class);
	private File file;
	private FileWriter fileWriter;
	private BufferedWriter bufferedWriter;
	
	public CsvDestination (FileMetaData fileMetaData) {
		file = new File(fileMetaData.getFilePath());
		if (file.exists()) {
			LOGGER.warn("File '" + fileMetaData.getFilePath() + "' exists already, it will be deleted first!");
			FileUtils.deleteQuietly(file);
		}
		setDelimiter(fileMetaData.getDelimiter());
		setLineDelimiter(fileMetaData.getLineDelimiter());
		List<String> dateFormats = fileMetaData.getDateFormats();
		if (dateFormats != null && dateFormats.size() != 0) {
			setDateFormatter(new SimpleDateFormat(dateFormats.get(0)));
		}
	}
	
	public void writeHeader(RecxxConfiguration configuration) {
		Header header = new Header(configuration);
		try {
			writeLine(header.toOutputString(getDelimiter(), getLineDelimiter()));
		} catch (IOException e) {
			throw new RuntimeException("Error while attempting to write header for configuration '" 
					+ configuration + "'", e) ;
		}
	}

	public void writeDifference(Difference difference) {
		try {
			writeLine(difference.toOutputString(getDelimiter(), getDateFormatter(), getPercentFormatter()));
		} catch (IOException e) {
			throw new RuntimeException("Error while attempting to write difference for key '" 
				+ difference.getKey() + "'", e) ;
		}
	}

	public void writeSummary(Summary summary) {
		setSummary(summary);
		try {
			writeLine(summary.toOutputString(getDelimiter(), getLineDelimiter(), getPercentFormatter()));
		} catch (IOException e) {
			throw new RuntimeException("Error while attempting to write summary for the reconciliation", e) ;
		}
	}

	public void write(Object object) throws IOException {
			write(object == null ? null : object.toString());		
	}
	
	public void write(String string) throws IOException {
		if (isEmpty(string)) {
			string = Default.NULL_STRING;
		}
		bufferedWriter.write(string);
	}

	public void writeLine(Object value) throws IOException {
		write(value);
		write(getLineDelimiter());
	}
	
	public void open() throws IOException {
		fileWriter = new FileWriter(file);
		bufferedWriter = new BufferedWriter(fileWriter);
	}
	
	public void close() throws IOException {
		bufferedWriter.flush();
		bufferedWriter.close();
		fileWriter.close();
	}


}
