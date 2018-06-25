package org.recxx.configuration;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DatabaseConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.recxx.destination.ConsoleDestination;
import org.recxx.domain.Column;
import org.recxx.domain.Conversion;
import org.recxx.domain.Conversion.Operation;
import org.recxx.domain.DatabaseMetaData;
import org.recxx.domain.Default;
import org.recxx.factory.DestinationFactory;
import org.recxx.factory.SourceFactory;
import org.recxx.utils.ComparisonUtils;
import org.recxx.utils.DriverManagerWrappedDataSource;

public class RecxxConfiguration extends AbstractConfiguration {

	private static Logger LOGGER = Logger.getLogger(RecxxConfiguration.class);

	private CombinedConfiguration config;

	public RecxxConfiguration() throws ConfigurationException {
		this(new PropertiesConfiguration());
	}

	public RecxxConfiguration(String filePath) throws ConfigurationException {
		this(new PropertiesConfiguration(filePath));
	}

	public RecxxConfiguration(AbstractConfiguration configuration) {
		config = new CombinedConfiguration();
		config.addConfiguration(new SystemConfiguration());
		config.addConfiguration(configuration);
	}

	public RecxxConfiguration(DataSource dataSource, String configName) throws ConfigurationException {
		config = new CombinedConfiguration();
		config.addConfiguration(new SystemConfiguration());
		DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(dataSource, Default.DATABASE_CONFIG_TABLE, Default.CONFIG_NAME, Default.CONFIG_KEY, Default.CONFIG_VALUE, configName);
		config.addConfiguration(databaseConfiguration);
	}

	public RecxxConfiguration(DataSource dataSource, String configName, PropertiesConfiguration generatePropertiesConfiguration) {
		config = new CombinedConfiguration();
		config.addConfiguration(new SystemConfiguration());
		config.addConfiguration(generatePropertiesConfiguration);
		DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(dataSource, Default.DATABASE_CONFIG_TABLE, Default.CONFIG_NAME, Default.CONFIG_KEY, Default.CONFIG_VALUE, configName);
		config.addConfiguration(databaseConfiguration);
	}

	public RecxxConfiguration(String configName, String alias, PropertiesConfiguration configuration) throws ConfigurationException {
		config = new CombinedConfiguration();
		config.addConfiguration(new SystemConfiguration());
		config.addConfiguration(configuration);
		DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(getDataSource(alias), Default.DATABASE_CONFIG_TABLE, Default.CONFIG_NAME, Default.CONFIG_KEY, Default.CONFIG_VALUE, configName);
		PropertiesConfiguration pc = new PropertiesConfiguration();
		pc.copy(databaseConfiguration);
		config.addConfiguration(pc);
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

	public String configureSubject() {
		return getString("SUBJECT");
	}

	public Date configureBusinessDate() {
		Date returnDate = null;
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
			String dateString = getString("BUSINESS_DATE");
			if (dateString != null) {
				returnDate = simpleDateFormat.parse(dateString);
			}
		} catch (ParseException e) {
			LOGGER.warn("'businessDate' should be supplied in the following format: 'yyyyMMdd'");
		}
		return returnDate;
	}

	public String configureDelimiter(String alias) {
		return getString(alias + ".delimiter", Default.COMMA);
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
		return configureFilePath(alias, true);
	}

	public String configureFilePath(String alias, boolean mandatory) {
		String filePath = getString(alias + ".filePath");
		if (filePath == null && mandatory) {
			throw new IllegalArgumentException("'" + alias + ".filePath' not specified in configuration, " +
					"this component must have a filePath, configured using '<alias>.filePath=<value>': " + toString());
		}
		return filePath;
	}

	public String configureFileEncoding(String alias) {
		return getString(alias + ".encoding");
	}

	public String configureFilePathCheckExists(String alias) {
		String filePath = configureFilePath(alias);
		File file = new File(filePath);
		if (!file.exists() || !file.canRead()) {
			throw new IllegalArgumentException("'" + alias + ".filePath' specified in configuration " +
					"does not exist, or is not readable, please check '" + filePath + "': " + toString());
		}
		return filePath;
	}

	public boolean configureIgnoreCase() {
		return getBoolean("ignoreCase", false);
	}

	public boolean configureConcurrency() {
		return getBoolean("concurrentExecution", true);
	}

	public BigDecimal configureToleranceLevel(){
		return getBigDecimal("toleranceLevelPercent", ComparisonUtils.DEFAULT_TOLERANCE_PERCENTAGE)
			.divide(new BigDecimal(100));
	}

	public BigDecimal configureToleranceAbsolute() {
		return getBigDecimal("toleranceLevelAbsolute", ComparisonUtils.DEFAULT_TOLERANCE_ABSOLUTE);
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

	public List<String> configureColumnsToIgnore(String alias) {
		List<String> columnsToIgnore = getStrings(alias + ".columnsToIgnore");
		return columnsToIgnore;
	}

	public List<String> configureKeyColumns(String alias) {
		List<String> keyColumns = getStrings(alias + ".keyColumns");
		if (keyColumns.isEmpty()) {
			LOGGER.warn("'keyColumns' not supplied, row numbers will be used to key the data for the reconciliation");
			keyColumns.add(Default.EMPTY_KEY_COLUMN_NAME);
		}
		return keyColumns;
	}

	public List<Column> configureColumns(String alias, Map<String, Class<?>> classAbbreviationMap) {
		List<String> columns = getStrings(alias + ".columns");
		if (columns.isEmpty()) {
			throw new IllegalArgumentException("'" + alias + ".columns' not specified in configuration, " +
					"this component must have columns, configured using '<alias>.columns=<name>|<type>, <name>|<type>...'," +
					" and columns must match across sources for comparison to function: " + toString());
		}
		List<Column> columnDefinitions = new ArrayList<Column>();
		for (String column : columns) {
			String[] split = column.split("\\" + Default.PIPE_DELIMITER);
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
							" column definition '" + column + "' cannot be split without separator '" + Default.PIPE_DELIMITER + "'");
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

	public List<Conversion> configureConversions(String alias) {
		List<String> conversionStrings = getStrings(alias + ".conversions");
		List<Conversion> conversions = new ArrayList<Conversion>();
		for (String conversionString : conversionStrings) {
			String[] split = conversionString.split("\\" + Default.PIPE_DELIMITER);
			String fieldName; String expression; Operation operation;
			switch (split.length) {
			case 2:
				fieldName = split[0];
				expression = split[1];
				String[] splitExpression = expression.split(Default.REGEX_DELIMITER);
					switch (splitExpression.length) {
					case 3:
						operation = Conversion.Operation.valueOf(splitExpression[0]);
						conversions.add(new Conversion(fieldName, operation, splitExpression[1], splitExpression[2]));
						break;
					case 2:
						operation = Conversion.Operation.valueOf(splitExpression[0]);
						if (operation.equals(Conversion.Operation.REGEX_REPLACE_ALL) || operation.equals(Conversion.Operation.REGEX_REPLACE_FIRST)) {
							conversions.add(new Conversion(fieldName, operation, splitExpression[1], ""));
						}
						else {
							conversions.add(new Conversion(fieldName, operation, splitExpression[1]));
						}
					break;
					default:
						throw new IllegalArgumentException("'" + alias + ".conversions' incorrectly specified in configuration, " +
								"this component must have conversions, configured using '<alias>.conversions=<fieldName>|REGEX_MATCH¬<regexPattern> " +
								"OR <fieldName>|REGEX_REPLACE¬<regexPattern>¬<replacementString> OR <fieldName>|REGEX_REPLACE_ALL¬<regexPattern>¬<replacementString>', " +
								" regex expression definition '" + expression + "' cannot be split with separator '" + Default.REGEX_DELIMITER + "'");
					}
				break;
			default:
				throw new IllegalArgumentException("'" + alias + ".conversions' incorrectly specified in configuration, " +
						"this component must have conversions, configured using '<alias>.conversions=<fieldName>|REGEX_MATCH¬<regexPattern> " +
						"OR <fieldName>|REGEX_REPLACE¬<regexPattern>¬<replacementString> OR <fieldName>|REGEX_REPLACE_ALL¬<regexPattern>¬<replacementString>', " +
						" conversion definition '" + conversionString + "' cannot be split with separator '" + Default.PIPE_DELIMITER + "'");
			}
		}
		return conversions;
	}
	
	public String configureSourceType(String alias, Map<Class<?>, SourceFactory> sourceFactoryMap) {
		String sourceType = getString(alias + ".type");
		if (sourceType == null) {
			throw new IllegalArgumentException("'" + alias + ".type' not specified in configuration, " +
					"configuration requires one of the following values: " + sourceFactoryMap.keySet() + ": " + toString());
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
					"configuration requires specification using 'sources=<alias1>, <alias2>'" + ": " + toString());
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

	public String configureDatabaseDriver(String... aliases) {
		String databaseDriver = null;
		for (String alias : aliases) {
			databaseDriver = getString(alias + ".driverClassName");
			if (databaseDriver != null) break;
		}
		if (databaseDriver == null) {
			StringBuilder sb = new StringBuilder();
			for (String alias : aliases) {
				sb.append(sb.length() != 0 ? " OR " : "");
				sb.append("'").append(alias).append(".driverClassName' not specified in configuration");
			}
			sb.append(", this must be configured and available in the classpath");
			throw new IllegalArgumentException(sb.toString() + ": " + toString());
		}
		return databaseDriver;
	}

	public String configureDatabaseUrl(String... aliases) {
		String databaseUrl = null;
		for (String alias : aliases) {
			databaseUrl = getString(alias + ".url");
			if (databaseUrl != null) break;
		}
		if (databaseUrl == null) {
			StringBuilder sb = new StringBuilder();
			for (String alias : aliases) {
				sb.append(sb.length() != 0 ? " OR " : "");
				sb.append("'").append(alias).append(".url' not specified in configuration");
			}
			sb.append(", this must be configured according to the driver configuration");
			throw new IllegalArgumentException(sb.toString() + ": " + toString());
		}
		return databaseUrl;
	}

	public String configureDatabaseUserId(String... aliases) {
		String databaseUserId = null;
		for (String alias : aliases) {
			databaseUserId = getString(alias + ".username");
			if (databaseUserId != null) break;
		}
		if (databaseUserId == null) {
			StringBuilder sb = new StringBuilder();
			for (String alias : aliases) {
				sb.append(sb.length() != 0 ? " OR " : "");
				sb.append("'").append(alias).append(".username' not specified in configuration");
			}
			throw new IllegalArgumentException(sb.toString());
		}
		return databaseUserId;
	}

	public String configureDatabasePassword(String... aliases) {
		String databasePassword = null;
		for (String alias : aliases) {
			databasePassword = getString(alias + ".password");
			if (databasePassword != null) break;
		}
		if (databasePassword == null) {
			StringBuilder sb = new StringBuilder();
			for (String alias : aliases) {
				sb.append(sb.length() != 0 ? " OR " : "");
				sb.append("'").append(alias).append(".password' not specified in configuration");
			}
			throw new IllegalArgumentException(sb.toString() + ": " + toString());
		}
		return databasePassword;
	}

	public DatabaseMetaData configureDatabaseMetaData(String alias) {
		String databaseDriver = getString(alias + ".driverClassName");
		String databaseUrl = getString(alias + ".url");
		String databaseUserId = getString(alias + ".username");
		String databasePassword = getString(alias + ".password");
		if (databaseDriver == null && databaseUrl == null && databaseUserId == null && databasePassword == null) {
			for (Configuration config : this.config.getConfigurations()) {
				if (DatabaseConfiguration.class.isAssignableFrom(config.getClass())) {
					return new DatabaseMetaData.Builder()
						.dataSource(((DatabaseConfiguration)config).getDatasource())
						.build();
				}
			}
		}
		else {
			return new DatabaseMetaData.Builder()
				.databaseDriver(configureDatabaseDriver(alias))
				.databaseUrl(configureDatabaseUrl(alias))
				.databaseUserId(configureDatabaseUserId(alias))
				.databasePassword(configureDatabasePassword(alias))
				.build();
		}
		return null;
	}
	
	public String configureSql(String alias) {
		String databasePassword = getString(alias + ".sql");
		if (databasePassword == null) {
			throw new IllegalArgumentException("'" + alias + ".sql' not specified in configuration" + ": " + toString());
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
		StringBuilder stringBuilder = new StringBuilder(10000);
		String lineSeparator = System.getProperty("line.separator");
		
		for (Configuration config: this.config.getConfigurations()) {
			SortedSet<String> keys = new TreeSet<String>();
			stringBuilder.append(lineSeparator).append(config.getClass().getName()).append(": ").append(lineSeparator);
			for (Iterator<String> iterator = config.getKeys(); iterator.hasNext();) {
				keys.add(iterator.next());
			}
			for (String key : keys) {
				stringBuilder.append(key)
				.append(" = ")
				.append(config.getProperty(key).toString().replace("[","").replace("]",""))
				.append(lineSeparator);
			}
		}
		
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).concat(stringBuilder.toString());
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.reflectionEquals(this, that, false);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}

	public DataSource getDataSource(String alias) {
		DatabaseMetaData databaseMetaData = new DatabaseMetaData.Builder()
			.databaseDriver(configureDatabaseDriver(alias))
			.databaseUrl(configureDatabaseUrl(alias))
			.databaseUserId(configureDatabaseUserId(alias))
			.databasePassword(configureDatabasePassword(alias))
			.build();
		return new DriverManagerWrappedDataSource(databaseMetaData);
	}

}
