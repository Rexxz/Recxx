package org.recxx.domain;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public interface Default {

	public static final String QUOTE = "\"";
	public static final String COMMA = ",";
	public static final String NULL_STRING = "";
	public static final String NULL = "null";
	public static final String UNKNOWN_COLUMN_NAME = "?";

	public static final char LINE_FEED_CHAR = '\n';
	public static final char CARRIAGE_RETURN_CHAR = '\r';

	public static final String LINE_DELIMITER = String.valueOf(LINE_FEED_CHAR);
	public static final String WINDOWS_LINE_DELIMITER = "\r\n";
	public static final String MAC_LINE_DELIMITER = "\r";
	public static final String UNIX_LINE_DELIMITER = LINE_DELIMITER;
	
	public static final String PILCRO_DELIMITER = "\u00B6";
	public static final String PIPE_DELIMITER = "|";

	public static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-dd_hhmmss.SSS");
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss.SSS");
	public static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	
	public static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#.00%");
	public static final DecimalFormat SIXDP_PERCENT_FORMAT = new DecimalFormat("#0.000000%");
	public static final String ALL_COLUMNS = "*";
	
	public static final String CONFIG_VALUE = "configValue";
	public static final String CONFIG_KEY = "configKey";
	public static final String CONFIG_NAME = "configName";
	public static final String DATABASE_CONFIG_TABLE = "RecxxConfig";
	public static final String DATABASE_PREFIX = "DatabasePrefix";
	
	public static final String EMPTY_KEY_COLUMN_NAME = "recxxGeneratedRowNumber";
	public static final String NO_DELIMITER = "";
	
}
