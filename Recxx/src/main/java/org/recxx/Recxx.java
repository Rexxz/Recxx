package org.recxx;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.sql.DataSource;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.destination.Destination;
import org.recxx.domain.Column;
import org.recxx.domain.ComparisonResult;
import org.recxx.domain.Default;
import org.recxx.domain.Difference;
import org.recxx.domain.Key;
import org.recxx.domain.Summary;
import org.recxx.factory.AbstractDestinationFactory;
import org.recxx.factory.AbstractSourceFactory;
import org.recxx.source.Source;
import org.recxx.utils.ComparisonUtils;
import org.recxx.utils.SystemUtils;

public class Recxx {

	private static Logger LOGGER = Logger.getLogger(Recxx.class);

	private static final String PROPS_FILE_ENDING = ".props";
	private static final String PROPERTIES_FILE_ENDING = ".properties";

	private String configName;
	private RecxxConfiguration configuration;

	private int totalDifferences = 0;

	public enum ConfigType { FILE, DATABASE } ;

	public Recxx ()  {}

	public Recxx (RecxxConfiguration configuration)  {
		this.configuration = configuration;
	}

	public int execute(String filePath) throws Exception {
		File file = new File(filePath);
		if (file.isFile()) {
			RecxxConfiguration configuration = new RecxxConfiguration(filePath);
			this.configName = ConfigType.FILE + ":" + file.getName();
			execute(configuration);
		}
		else if (file.isDirectory()) {
			for (File tempFile : file.listFiles()) {
				if (tempFile.getAbsolutePath().endsWith(PROPERTIES_FILE_ENDING) || tempFile.getAbsolutePath().endsWith(PROPS_FILE_ENDING)) {
					this.configName = ConfigType.FILE + ":" + tempFile.getName();
					RecxxConfiguration configuration = new RecxxConfiguration(tempFile.getAbsolutePath());
					execute(configuration);
				}
			}
		}
		return totalDifferences;
	}

	public int execute(String filePath, String alias, String configName) throws Exception {
		this.configName = configName;
		LOGGER.info("Executing config '" + configName + "' using Alias '" + alias + "' from Properties in '" + filePath + "'");
		PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(filePath);
		RecxxConfiguration configuration = new RecxxConfiguration(configName, alias, propertiesConfiguration);
		configuration.configureSubject();
		execute(configuration);
		return totalDifferences;
	}

	public void execute(Properties properties, String alias, String configName) throws Exception {
		this.configName = configName;
		LOGGER.info("Executing config '" + configName + "' using Alias '" + alias + "' from Properties '" + properties + "'");
		RecxxConfiguration configuration = new RecxxConfiguration(configName, alias, generatePropertiesConfiguration(properties));
		execute(configuration);
	}

	public void execute(DataSource dataSource, String configName) throws Exception {
		execute(dataSource, configName, null);
	}

	public void execute(DataSource dataSource, String configName, Properties properties) throws Exception {
		this.configName = configName;
		Connection connection = dataSource.getConnection();
		DatabaseMetaData metaData = connection.getMetaData();
		if (properties == null) {
			LOGGER.info("Executing config '" + configName + "' using DataSource '" + metaData.getURL() + "'");
		}
		else {
			LOGGER.info("Executing config '" + configName + "' using DataSource '" + metaData.getURL() + "' with Properties '" + properties + "'");
		}
		if (!connection.isClosed()) connection.close();
		RecxxConfiguration configuration = new RecxxConfiguration(dataSource, configName, generatePropertiesConfiguration(properties));
		execute(configuration);
	}

	private PropertiesConfiguration generatePropertiesConfiguration(Properties properties) {
		PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
		for (Object key : properties.keySet()) {
			propertiesConfiguration.setProperty(key.toString(), properties.get(key));
		}
		return null;
	}
	
	public List<Destination> execute() throws Exception {
		return execute(this.getConfiguration());
	}

	public List<Destination> execute(RecxxConfiguration configuration) throws Exception {

		List<Source<Key>> sources = new AbstractSourceFactory().getSources(configuration);

		FutureTask <Source<Key>> task1 = new FutureTask<Source<Key>>(sources.get(0));
		FutureTask <Source<Key>> task2 = new FutureTask<Source<Key>>(sources.get(1));

		List <Destination> destinations = new AbstractDestinationFactory().getDestinations(configuration);

		LOGGER.info(configuration.getProperty("toleranceLevelPercent"));

		long t = System.currentTimeMillis();
		LOGGER.info("Starting sources");

		ExecutorService executor = configuration.configureConcurrency() ? Executors.newFixedThreadPool(2) : Executors.newSingleThreadExecutor();
        executor.execute(task1);
        executor.execute(task2);

        while (!task1.isDone() && !task2.isDone()) {
        	try {
                Thread.sleep(500);
            }
        	catch (InterruptedException ie) {
				ie.printStackTrace();
            }
        }
        LOGGER.info("Sources complete " + ((System.currentTimeMillis() - t) / 1000d) +"s");
		LOGGER.info("Memory used: " + SystemUtils.memoryUsed() + "%");
		executor.shutdown();

		return compare(task1.get(), task2.get(), destinations, configuration);
    }

	public RecxxConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(RecxxConfiguration configuration) {
		this.configuration = configuration;
	}

	private List<Destination> compare(Source<Key> source1,
							Source<Key> source2,
							List<Destination> destinations,
							RecxxConfiguration configuration) {

        	Set<Key> keySet1 = source1.getKeySet();
            Set<Key> keySet2 = source2.getKeySet();

        open(destinations);
        writeHeader(destinations, source1, source2);

        Long t = System.currentTimeMillis();
        LOGGER.info("Starting compare");

		int matchCount = 0;
		boolean ignoreCase = configuration.configureIgnoreCase();
		BigDecimal smallestAbsoluteValue = configuration.configureSmallestAbsoluteValue();
		BigDecimal toleranceLevel = configuration.configureToleranceLevel();
		BigDecimal toleranceAbsolute = configuration.configureToleranceAbsolute();
		List<String> compareColumns1 = source1.getCompareColumns();
		List<String> compareColumns2 = source2.getCompareColumns();
		List<String> ignoreColumns1 = source1.getIgnoreColumns();
		List<String> ignoreColumns2 = source2.getIgnoreColumns();

		for (Key key : keySet1) {

			List<?> row1 = source1.getRow(key);
			List<?> row2 = null;
			boolean keyExistsInBothSources = keySet2.contains(key);
			boolean matchedRow = true;

			if (keyExistsInBothSources) {
				row2 = source2.getRow(key);

				for (int i = 0; i < row1.size(); i++) {
					String columnName = source1.getColumns().get(i).getName();

					if((compareColumns1.contains(Default.ALL_COLUMNS) || compareColumns1.contains(columnName))
							&& (!ignoreColumns1.contains(columnName))
							&& !source1.getKeyColumns().contains(columnName)
							&& !source2.getKeyColumns().contains(columnName)) {

						Object field1 = row1.get(i);
						int source2ColumnIndex = source2.getColumnIndex(columnName);

						if (keyExistsInBothSources && source2ColumnIndex >= 0
								&& ((compareColumns2.contains(Default.ALL_COLUMNS) || compareColumns2.contains(columnName))
								&& !ignoreColumns2.contains(columnName) )
								&& row2.size() >  source2ColumnIndex ) {

							Object field2 = row2.get(source2ColumnIndex);
								ComparisonResult comparison = ComparisonUtils.compare(field1,
										field2,
										smallestAbsoluteValue,
										toleranceLevel,
										toleranceAbsolute,
										ignoreCase);
								if (comparison.isDifferent()) {
									Difference difference = new Difference.Builder()
									.key(key)
									.alias1(source1.getAlias())
									.alias2(source2.getAlias())
									.column(source1.getColumns().get(i))
									.field1(field1)
									.field2(field2)
									.absoluteDifference(comparison.getAbsoluteDifference())
									.percentageDifference(comparison.getPercentageDifference())
									.build();
									writeDifference(destinations, difference);
									matchedRow = false;
								}
						}
						else  {
							Difference difference = new Difference.Builder()
								.key(key)
								.alias1(source1.getAlias())
								.alias2(source2.getAlias())
								.column(source1.getColumns().get(i))
								.field1(field1)
								.field2("Missing")
								.absoluteDifference(BigDecimal.ZERO)
								.percentageDifference(BigDecimal.ZERO)
								.build();
							writeDifference(destinations, difference);
							matchedRow = false;
						}
					}
				}


				for (int i = 0; i < row2.size(); i++) {

					boolean sourceColumn1Exists = false;
					String source2ColumnName = source2.getColumns().get(i).getName();

                    if((compareColumns2.contains(Default.ALL_COLUMNS) || compareColumns2.contains(source2ColumnName))
                            && !ignoreColumns2.contains(source2ColumnName)){
						for (Column source1Column : source1.getColumns()) {
							String source1ColumnName = source1Column.getName();

	                        if (source1ColumnName.equals(source2ColumnName)
	                                && (compareColumns1.contains(Default.ALL_COLUMNS) || compareColumns1.contains(source1ColumnName))
	                                && !ignoreColumns1.contains(source1ColumnName)) {
	                               sourceColumn1Exists = true;
								break;
							}
						}

						if( !sourceColumn1Exists ) {

							Object field2 = row2.get(i);
							Difference difference = new Difference.Builder()
								.key(key)
								.alias1(source1.getAlias())
								.alias2(source2.getAlias())
								.column(source2.getColumns().get(i))
								.field1("Missing")
								.field2(field2)
								.absoluteDifference(BigDecimal.ZERO)
								.percentageDifference(BigDecimal.ZERO)
								.build();
							writeDifference(destinations, difference);
							matchedRow = false;
						}
                    }
				}

				if (matchedRow) {
					matchCount++;
				}
			}

			else {
				Difference difference = new Difference.Builder()
					.key(key)
					.alias1(source1.getAlias())
					.alias2(source2.getAlias())
					.column(new Column("*", String.class))
					.field1("*")
					.field2("Missing")
					.absoluteDifference(BigDecimal.ZERO)
					.percentageDifference(BigDecimal.ZERO)
					.build();
				writeDifference(destinations, difference);
			}

        }

        for (Key key : keySet2) {

        	if (!keySet1.contains(key)) {
				Difference difference = new Difference.Builder()
					.key(key)
					.alias1(source1.getAlias())
					.alias2(source2.getAlias())
					.column(new Column("*", String.class))
					.field1("Missing")
					.field2("*")
					.absoluteDifference(BigDecimal.ZERO)
					.percentageDifference(BigDecimal.ZERO)
					.build();
				writeDifference(destinations, difference);
			}
		}

        Summary summary = new Summary.Builder()
        	.configName(configName)
        	.subject(configuration.configureSubject())
        	.businessDate(configuration.configureBusinessDate())
            .alias1Count(keySet1.size())
            .alias2Count(keySet2.size())
            .alias1(source1.getAlias())
            .alias2(source2.getAlias())
            .matchCount(matchCount)
            .alias1ExecutionTime(source1.getExecutionTimeMillis())
            .alias2ExecutionTime(source2.getExecutionTimeMillis())
            .build();
		writeSummary(destinations, summary);

        close(destinations);

        source1.close();
        source1 = null;
        source2.close();
        source2 = null;

        LOGGER.info("Memory used: " + SystemUtils.memoryUsed() + "%");
        LOGGER.info("Compare complete in " + ((System.currentTimeMillis() - t) / 1000d) +"s");

        return destinations;

	}

	private void writeHeader(List<Destination> destinations, Source<Key> source1, Source<Key> source2) {
		for (Destination destination : destinations) {
			destination.writeHeader(source1, source2);
		}
	}

	private void writeDifference(List<Destination> destinations, Difference difference) {
		totalDifferences++;
		for (Destination destination : destinations) {
			destination.writeDifference(difference);
		}
	}

	private void writeSummary(List<Destination> destinations, Summary summary) {
		for (Destination destination : destinations) {
			destination.writeSummary(summary);
		}
	}

	private void open(List<Destination> destinations) {
		int failCount = 0;
		for (Destination destination : destinations) {
			try {
				destination.open();
			} catch (Exception e) {
				if (failCount == destinations.size()) {
					throw new RuntimeException("All configured destinations failed on opening, reconciliation failed!", e);
				}
				else {
					failCount++;
					LOGGER.error("A problem occurred with '" + destination.getClass().getName() +
							"' will attempt to continue with remaining desintations");
					e.printStackTrace();
				}
			}
		}
	}

	private void close(List<Destination> destinations) {
		for (Destination destination : destinations) {
			try {
				destination.close();
			} catch (Exception e) {
				LOGGER.error("A problem occurred with '" + destination.getClass().getName() +
						"' will attempt to continue with remaining desintations");
				e.printStackTrace();
			}
		}
	}

    public static void main(String args[]) {
    	int reconciliationDifferences = 0;
    	if (args.length < 1) {
    		LOGGER.error("Configuration is required for running Recxx");
    		LOGGER.info("Please supply one of the following configuration options: ");
    		LOGGER.info("1: <properties file path>");
    		LOGGER.info("2: FILE <properties file path>");
    		LOGGER.info("3: DATABASE <database source properties file path> <property prefix> <database config name>");
    	}
    	else {
    		try {
    			ConfigType configType = ConfigType.valueOf(args[0]);
    			Recxx recxx = new Recxx();

    			switch (configType) {
				case DATABASE:

		    		String filePath = args[1];
		    		String prefix = args[2];
		    		String configName = args[3];
		    		LOGGER.info("Running Recxx with " + configType + " config defined in '" + filePath + "' for the prefix '" + prefix + "'");
		    		reconciliationDifferences = recxx.execute(filePath, prefix, configName);

					break;

				case FILE:

		    		String configFilePath = args[1];
					LOGGER.info("Running Recxx with " + configType + " config defined in '" + configFilePath + "'");
					reconciliationDifferences = recxx.execute(configFilePath);
					break;

				default:

		    		String defaultConfigFilePath = args[0];
	    			LOGGER.info("Running Recxx with " + configType + " config defined in '" + defaultConfigFilePath + "'");
	    			reconciliationDifferences = recxx.execute(defaultConfigFilePath);
					break;
				}

    		} catch (Exception e) {
    			LOGGER.error("A problem has occurred, using the configuration options supplied: "
    				 + Arrays.toString(args) + "', please recheck your configuration");
    			e.printStackTrace();
    			reconciliationDifferences = -1;
    		}
    	}
    	System.exit(reconciliationDifferences);
    }

}
