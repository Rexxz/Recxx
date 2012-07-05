package org.recxx.domain;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public interface Default {

	public static final String QUOTE = "\"";
	public static final String COMMA = ",";
	public static final String NULL_STRING = "";
	public static final String NULL = "null";
	public static final String UNKNOWN_COLUMN_NAME = "?";
	public static final String LINE_DELIMITER = "\n";
	//	public static final String DELIMITER = "\u00B6";
	public static final String DELIMITER = "|";

	public static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-dd_hhmmss.SSS");
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss.SSS");
	public static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	
	public static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#.00%");
	public static final DecimalFormat SIXDP_PERCENT_FORMAT = new DecimalFormat("#0.000000%");
	public static final String ALL_COLUMNS = "*";
	
}
