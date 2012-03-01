package org.recxx.destination;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.recxx.domain.Difference;
import org.recxx.domain.FileMetaData;
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
			file.delete();
		}
		setDelimiter(fileMetaData.getDelimiter());
		setLineDelimiter(fileMetaData.getLineDelimiter());
		List<String> dateFormats = fileMetaData.getDateFormats();
		if (dateFormats != null && dateFormats.size() != 0) {
			setDateFormatter(new SimpleDateFormat(dateFormats.get(0)));
		}
	}
	
	public void writeDifference(Difference difference) {
		try {
			writeLine(defaultDifference(difference));
		} catch (IOException e) {
			throw new RuntimeException("Error while attempting to write difference for key '" 
				+ difference.getKey() + "'", e) ;
		}
	}

	public void writeSummary(Summary summary) {
		try {
			writeLine(defaultSummary(summary));
		} catch (IOException e) {
			throw new RuntimeException("Error while attempting to write summary for the reconciliation", e) ;
		}
	}

	public void write(Object object) throws IOException {
		if (object instanceof Date) {
			write((Date) object);
		}
		else {
			write(object == null ? null : object.toString());		
		}
	}
	
	public void write(Date date) throws IOException {
		write(date == null ? null : dateFormatter.format(date));		
	}
	
	public void write(String string) throws IOException {
		if (isEmpty(string)) {
			string = nullString;
		}
		bufferedWriter.write(string);
	}

	public void writeLine(List<?> values) throws IOException {
		int idx = 0;
		for (Object value : values) {
			if (values.size() == idx + 1) {
				writeLine(value);
			} else {
				write(value);
				write(delimiter);
			}
			idx++;
		}
	}

	public void writeLine(Object value) throws IOException {
		write(value);
		bufferedWriter.newLine();
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
