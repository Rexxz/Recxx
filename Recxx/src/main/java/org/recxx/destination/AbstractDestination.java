package org.recxx.destination;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.recxx.domain.Default;
import org.recxx.domain.Summary;

public abstract class AbstractDestination implements Destination {
	
	private String delimiter = Default.COMMA;
	private String lineDelimiter = Default.LINE_DELIMITER;

	private SimpleDateFormat dateFormatter = Default.DATE_FORMAT;
	private DecimalFormat percentFormatter = Default.SIXDP_PERCENT_FORMAT;
	private Summary summary;

	public Summary getSummary() {
		return summary;
	}

	public void setSummary(Summary summary) {
		this.summary = summary;
	}	

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public String getLineDelimiter() {
		return lineDelimiter;
	}

	public void setLineDelimiter(String lineDelimiter) {
		this.lineDelimiter = lineDelimiter;
	}

	public SimpleDateFormat getDateFormatter() {
		return dateFormatter;
	}

	public void setDateFormatter(SimpleDateFormat dateFormatter) {
		this.dateFormatter = dateFormatter;
	}

	public void setPercentFormatter(DecimalFormat percentFormatter) {
		this.percentFormatter = percentFormatter;
	}

	public DecimalFormat getPercentFormatter() {
		return percentFormatter;
	}
	
}
