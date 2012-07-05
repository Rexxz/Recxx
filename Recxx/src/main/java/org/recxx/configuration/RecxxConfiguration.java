package org.recxx.configuration;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.recxx.destination.ConsoleDestination;
import org.recxx.domain.Column;
import org.recxx.domain.Default;
import org.recxx.factory.DestinationFactory;
import org.recxx.factory.SourceFactory;
import org.recxx.source.FileSource;
import org.recxx.utils.ComparisonUtils;

public class RecxxConfiguration extends AbstractConfiguration {

	private static Logger LOGGER = Logger.getLogger(RecxxConfiguration.class);

	private CombinedConfiguration config;
	
	public RecxxConfiguration() throws ConfigurationException {
		this(new PropertiesConfiguration());
	}

	public RecxxConfiguration(String filePath) throws ConfigurationException {
		this(new PropertiesConfiguration(filePath));
	}

	public RecxxConfiguration(AbstractConfiguration propertiesConfig) {
		SystemConfiguration systemConfig = new SystemConfiguration();
		config = new CombinedConfiguration();
		config.addConfiguration(systemConfig);
		config.addConfiguration(propertiesConfig);
	}

	public boolean containsKey(String key) {
		return config.containsKey(key);
	}
	
	public Iterator<String> getKeys() {
		return config.getKeys();
	}
	
	public Object getProperty(String property) {
		return config.getProperty(property);
	}
	
	public boolean isEmpty() {
		return config.isEmpty();
	}
	
	@Override
	public String[] getStringArray(String key) {
		return config.getStringArray(key);
	}
	
	public List<String> getStrings(String key) {
		String[] stringArray = getStringArray(key);
		if (stringArray == null || stringArray.length == 0) {
			return new ArrayList<String>();
		}
		else {
			return Arrays.asList(stringArray);
		}
	}
	
	public List<String> getStrings(String key, String defaultKey) {
		List<String> strings = getStrings(key);
		if (strings.isEmpty()) {
			strings = getStrings(defaultKey);
		}
		return strings;
	}
	
	public String configureDelimiter(String alias) {
		return getString(alias + ".delimiter", FileSource.DEFAULT_DELIMITER);
	}

	public List<String> configureDateFormats(String alias) {
		List<String> dateFormats = getStrings(alias + ".dateFormats", "dateFormats");
		if (dateFormats.isEmpty()) {
			LOGGER.warn("'" + alias + ".dateFormats' does not exist in configuration, " +
			" and no overall 'dateFormats' is specified, conversion errors may occur");
		}
		return dateFormats;
	}
	
	public String configureLineDelimiter(String alias) {
		String lineDelimiterString = getString(alias + ".lineDelimiter");
		String lineDelimiter;
		if (lineDelimiterString == null || lineDelimiterString.equals("")) {
			lineDelimiter = System.getProperty("line.separator");
		}
		else {
			lineDelimiter = lineDelimiterString;
		}
		return lineDelimiter;
	}
	
	public String configureFilePath(String alias) {
		String filePath = getString(alias + ".filePath");
		if (filePath == null) {
			throw new IllegalArgumentException("'" + alias + ".filePath' not specified in configuration, " +
					"this component must have a filePath, configured using '<alias>.filePath=<value>'");
		}
		return filePath;
	}
	
	public String configureFilePathCheckExists(String alias) {
		String filePath = configureFilePath(alias);
		File file = new File(filePath);
		if (!file.exists() || !file.canRead()) {
			throw new IllegalArgumentException("'" + alias + ".filePath' specified in configuration " +
					"does not exist, or is not readable, please check '" + filePath + "'");
		}
		return filePath;
	}
	
	public boolean configureIgnoreCase() {
		return getBoolean("ignoreCase", false);
	}

	public BigDecimal configureToleranceLevel(){
		return getBigDecimal("toleranceLevelPercent", ComparisonUtils.DEFAULT_TOLERANCE_PERCENTAGE)
			.divide(new BigDecimal(100));
	}
	
	public BigDecimal configureSmallestAbsoluteValue(){
		return getBigDecimal("smallestAbsoluteValue", ComparisonUtils.DEFAULT_SMALLEST_ABSOLUTE_VALUE);
	}
	
	public boolean configureIgnoreHeaderRow(String alias) {
		return getBoolean(alias + ".ignoreHeaderRow", true);
	}

	public List<String> configureColumnsToCompare(String alias) {
		List<String> columnsToCompare = getStrings(alias + ".columnsToCompare");
		if (columnsToCompare.isEmpty()) {
			columnsToCompare.add(Default.ALL_COLUMNS);
		}
		return columnsToCompare;
	}
	
	public List<String> configureKeyColumns(String alias) {
		List<String> keyColumns = getStrings(alias + ".keyColumns");
		if (keyColumns.isEmpty()) {
			throw new IllegalArgumentException("'" + alias + ".keyColumns' not specified in configuration, " +
					"this component must have keyColumns, configured using '<alias>.keyColumns=<value>, <value>...'");
		}
		return keyColumns;
	}

	public List<Column> configureColumns(String alias, Map<String, Class<?>> classAbbreviationMap) {
		List<String> columns = getStrings(alias + ".columns");
		if (columns.isEmpty()) {
			throw new IllegalArgumentException("'" + alias + ".columns' not specified in configuration, " +
					"this component must have columns, configured using '<alias>.columns=<name>|<type>, <name>|<type>...'," +
					" and columns must match across sources for comparison to function");
		}
		List<Column> columnDefinitions = new ArrayList<Column>();
		for (String column : columns) {
			String[] split = column.split("\\" + FileSource.DEFAULT_COLUMN_NAME_TYPE_SEPARATOR);
			String columnName; String columnType; 
			switch (split.length) {
				case 2:	
					columnName = split[0];
					columnType = split[1];
					break;
				case 1:
					columnName = Default.UNKNOWN_COLUMN_NAME;
					columnType = split[0];
					break;
		
				case 0:
				default:
					throw new IllegalArgumentException("'" + alias + ".columns' incorrectly specified in configuration, " +
							"this component must have columns, configured using '<alias>.columns=<name>|<type>, <name>|<type>...'," +
							" column definition '" + column + "' cannot be split without separator '" + FileSource.DEFAULT_COLUMN_NAME_TYPE_SEPARATOR + "'");
			} 
			
			Class<?> clazz = classAbbreviationMap.get(columnType);
			if (clazz == null) {
				throw new IllegalArgumentException("'" + alias + ".columns' incorrectly specified in configuration, " +
						"this component must have columns, configured using '<alias>.columns=<name>|<type>, <name>|<type>...'," +
						" column definition '" + column + "' received and no matching class definition found for '" + columnType + 
						"' in " + classAbbreviationMap.toString());
			}
			columnDefinitions.add(new Column(columnName, clazz));
		}
		return columnDefinitions;
	}
	
	public String configureSourceType(String alias, Map<Class<?>, SourceFactory> sourceFactoryMap) {
		String sourceType = getString(alias + ".type");
		if (sourceType == null) {
			throw new IllegalArgumentException("'" + alias + ".type' not specified in configuration, " +
					"configuration requires one of the following values: " + sourceFactoryMap.keySet());
		}
		return sourceType;
	}
	
	public List<String> configureDestinationAliases() {
		List<String> destinationAliases = getStrings("destinations");
		if (destinationAliases == null || destinationAliases.isEmpty()) {
			destinationAliases = new ArrayList<String>();
			destinationAliases.add("console");
		}
		return destinationAliases;
	}

	public String configureDestinationType(String alias, Map<Class<?>, DestinationFactory> destinationFactoryMap) {
		String destinationType = getString(alias + ".type");
		if (destinationType == null) {
			destinationType = ConsoleDestination.class.getName();
		}
		return destinationType;
	}
	
	public List<String> configureSourceAliases() {
		List<String> sourceAliases = getStrings("sources");
		if (sourceAliases == null || sourceAliases.isEmpty()) {
			throw new IllegalArgumentException("'sources' specified incorrectly or missing in configuration, " +
					"configuration requires specification using 'sources=<alias1>, <alias2>'");
		}
		return sourceAliases;
	}
	
	public boolean configureFormatComparison(String alias) {
		return getBoolean(alias + ".formatComparison", false);
	}

	public List<String> configureOmitSheets(String alias) {
		List<String> omitSheets = getStrings(alias + ".omitSheets");
		if (omitSheets == null || omitSheets.isEmpty()) {
			omitSheets = new ArrayList<String>();
		}
		return omitSheets;
	}

	// Database configuration
	
	public String configureDatabaseDriver(String alias) {
		String databaseDriver = getString(alias + ".databaseDriver");
		if (databaseDriver == null) {
			throw new IllegalArgumentException("'" + alias + ".databaseDriver' not specified in configuration, " +
					"this must be configured and available in the classpath");
		}
		return databaseDriver;
	}	

	public String configureDatabaseUrl(String alias) {
		String databaseUrl = getString(alias + ".databaseUrl");
		if (databaseUrl == null) {
			throw new IllegalArgumentException("'" + alias + ".databaseUrl' not specified in configuration, " +
					"this must be configured according to the driver configuration");
		}
		return databaseUrl;
	}	
	
	public String configureDatabaseUserId(String alias) {
		String databaseUserId = getString(alias + ".databaseUserId");
		if (databaseUserId == null) {
			throw new IllegalArgumentException("'" + alias + ".databaseUserId' not specified in configuration");
		}
		return databaseUserId;
	}	

	public String configureDatabasePassword(String alias) {
		String databasePassword = getString(alias + ".databasePassword");
		if (databasePassword == null) {
			throw new IllegalArgumentException("'" + alias + ".databasePassword' not specified in configuration");
		}
		return databasePassword;
	}	
	
	public String configureSql(String alias) {
		String databasePassword = getString(alias + ".sql");
		if (databasePassword == null) {
			throw new IllegalArgumentException("'" + alias + ".sql' not specified in configuration");
		}
		return databasePassword;
	}	
	
	@Override
	protected void addPropertyDirect(String key, Object value) {
		config.addProperty(key, value);					
	}
	
	@Override
	public void setProperty(String key, Object value) {
		config.setProperty(key, value);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.reflectionEquals(this, that, false);
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}

}
