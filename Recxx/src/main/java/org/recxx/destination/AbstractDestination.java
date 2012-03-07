package org.recxx.destination;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.recxx.domain.Difference;
import org.recxx.domain.Key;
import org.recxx.domain.Summary;

public abstract class AbstractDestination implements Destination {

	public static final String QUOTE = "\"";
	public static final String COMMA = ",";
	public static final String NULL_STRING = "";
	public static final char LINE_DELIMITER = System.getProperty("line.separator").charAt(0);
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss.SSS");
	public static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#.00%");
	
	protected String delimiter = COMMA;
	protected char lineDelimiter = LINE_DELIMITER;
	protected String nullString = NULL_STRING;
	protected SimpleDateFormat dateFormatter = DATE_FORMAT;
	protected Summary summary;

	private boolean initialised = false;

	public String defaultHeader(Difference difference) {
		StringBuilder sb = new StringBuilder();
		for (String column : difference.getKeyColumns()) {
			sb.append("Key: ").append(column).append(delimiter);
		}
		sb.append("column").append(delimiter)
		.append(difference.getAlias1()).append(".value").append(delimiter)
		.append(difference.getAlias2()).append(".value").append(delimiter)
		.append("% Diff").append(delimiter).append("ABS Diff")
		.append(lineDelimiter);
		return sb.toString();
	}

	public String defaultDifference(Difference difference) {
		StringBuilder sb = new StringBuilder();
		if (!initialised) {
			sb.append(defaultHeader(difference));
			initialised = true;
		}
		sb.append(splitKeyValue(difference.getKey()));
		sb.append(difference.getColumn().getName()).append(delimiter);
		if (difference.getField1() != null && Date.class.isAssignableFrom(difference.getField1().getClass())) {
			sb.append(dateFormatter.format(difference.getField1())).append(delimiter);
		}
		else {
			sb.append(difference.getField1()).append(delimiter);
		}
		if (difference.getField2() != null && Date.class.isAssignableFrom(difference.getField2().getClass())) {
			sb.append(dateFormatter.format(difference.getField2())).append(delimiter);
		}
		else {
			sb.append(difference.getField2()).append(delimiter);
		}
		if (difference.getField1() != null && Number.class.isAssignableFrom(difference.getField1().getClass()) && 
				difference.getField2() != null && Number.class.isAssignableFrom(difference.getField2().getClass())) {
			sb.append(PERCENT_FORMAT.format(difference.getComparison().getPercentageDifference()))
			.append(delimiter)
			.append(difference.getComparison().getAbsoluteDifference());
		}
		else {
			sb.append(delimiter);
		}		
		return sb.toString();
	}
	
	public String defaultSummary(Summary summary) {
		StringBuilder sb = new StringBuilder();
		sb.append(lineDelimiter)
		.append(lineDelimiter)
		.append("======================").append(lineDelimiter)
		.append("Reconciliation Summary").append(lineDelimiter)
		.append("======================").append(lineDelimiter)
		.append(summary.getAlias1()).append(" rows: ").append(delimiter).append(summary.getAlias1Count()).append(lineDelimiter)
		.append(summary.getAlias2()).append(" rows: ").append(delimiter).append(summary.getAlias2Count()).append(lineDelimiter)
		.append(summary.getAlias1()).append(" matched ").append(summary.getAlias2()).append(" : ").append(delimiter).append(summary.getMatchCount()).append(lineDelimiter)
		.append(summary.getAlias1()).append(" matched ").append(summary.getAlias2()).append(" % : ").append(delimiter).append(PERCENT_FORMAT.format(summary.getAlias1MatchPercentage())).append(lineDelimiter)
		.append(summary.getAlias2()).append(" matched ").append(summary.getAlias1()).append(" % : ").append(delimiter).append(PERCENT_FORMAT.format(summary.getAlias2MatchPercentage())).append(lineDelimiter);
		return sb.toString();
	}

	protected StringBuilder splitKeyValue(Key key) {
		StringBuilder sb = new StringBuilder();
		for (String keyPart : key.asList()) {
			sb.append(keyPart).append(delimiter);
		}
		return sb;
	}
	
	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public char getLineDelimiter() {
		return lineDelimiter;
	}

	public void setLineDelimiter(char lineDelimiter) {
		this.lineDelimiter = lineDelimiter;
	}

	public String getNullString() {
		return nullString;
	}

	public void setNullString(String nullString) {
		this.nullString = nullString;
	}

	public SimpleDateFormat getDateFormatter() {
		return dateFormatter;
	}

	public void setDateFormatter(SimpleDateFormat dateFormatter) {
		this.dateFormatter = dateFormatter;
	}	
	
	public Summary getSummary() {
		return summary;
	}

	public void setSummary(Summary summary) {
		this.summary = summary;
	}	
}
