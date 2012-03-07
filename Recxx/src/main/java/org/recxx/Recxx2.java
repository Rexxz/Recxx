package org.recxx;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.recxx.configuration.RecxxConfiguration;
import org.recxx.destination.Destination;
import org.recxx.domain.ComparisonResult;
import org.recxx.domain.Difference;
import org.recxx.domain.Key;
import org.recxx.domain.Summary;
import org.recxx.factory.AbstractDestinationFactory;
import org.recxx.factory.AbstractSourceFactory;
import org.recxx.source.Source;
import org.recxx.utils.ComparisonUtils;
import org.recxx.utils.SystemUtils;

public class Recxx2 {
	
	private static Logger LOGGER = Logger.getLogger(Recxx2.class);
	
	private final RecxxConfiguration configuration;
	private final ExecutorService executor = Executors.newFixedThreadPool(2);

	public Recxx2(String filePath) throws ConfigurationException {
		this.configuration = new RecxxConfiguration(filePath);
	}

	public Recxx2(RecxxConfiguration config) throws ConfigurationException {
		this.configuration = config;
	}

	public Summary execute() {
		
		//TODO Move to ConvertUtilsBean to allow per source specific conversions
		DateTimeConverter dtConverter = new DateConverter(null);
		dtConverter.setPatterns(configuration.getStringArray("dateFormats"));
		ConvertUtils.register(dtConverter, Date.class);
		
		List<Source<Key>> sources = new AbstractSourceFactory().getSources(configuration);
		
		FutureTask <Source<Key>> task1 = new FutureTask<Source<Key>>(sources.get(0));
		FutureTask <Source<Key>> task2 = new FutureTask<Source<Key>>(sources.get(1));
		
		List <Destination> destinations = new AbstractDestinationFactory().getDestinations(configuration);

		long t = System.currentTimeMillis();
		LOGGER.info("Starting sources");
        executor.execute(task1);
        executor.execute(task2);

        while (!task1.isDone() && !task2.isDone()) {
        	try {
                Thread.sleep(2000);
            } 
        	catch (InterruptedException ie) {
				ie.printStackTrace();
            }
        }
        LOGGER.info("Sources complete " + ((System.currentTimeMillis() - t) / 1000d) +"s");
		LOGGER.info("Memory used: " + SystemUtils.memoryUsed() + "%");
		executor.shutdown();
	    
		Summary summary = null;
        try {
			summary = compare(task1.get(), task2.get(), destinations);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return summary;
    }

	private Summary compare(Source<Key> source1,
						Source<Key> source2, 
						List<Destination> destinations) {

        	Set<Key> keySet1 = source1.getKeySet();
            Set<Key> keySet2 = source2.getKeySet();
            
        open(destinations);

        Long t = System.currentTimeMillis();
        LOGGER.info("Starting compare");
        
		int compareCount = 0;
		int matchCount = 0;
		boolean ignoreCase = configuration.configureIgnoreCase();
		BigDecimal smallestAbsoluteValue = configuration.configureSmallestAbsoluteValue();
		BigDecimal toleranceLevel = configuration.configureToleranceLevel();
		List<String> compareColumns1 = source1.getCompareColumns();
		List<String> compareColumns2 = source2.getCompareColumns();

		for (Key key : keySet1) {

			List<?> row1 = source1.getRow(key);
			List<?> row2 = null;
			boolean keyExistsInBothSources = keySet2.contains(key);
			boolean matchedRow = true;

			if (keyExistsInBothSources) {
				row2 = source2.getRow(key);					
			}
			
			for (int i = 0; i < row1.size(); i++) {
				
				if(compareColumns1.contains(RecxxConfiguration.ALL_COLUMNS) ||
						compareColumns1.contains(source1.getColumns().get(i).getName())) {
					
					Object field1 = row1.get(i);
					
					if (keyExistsInBothSources  && i < row2.size() &&
							(compareColumns2.contains(RecxxConfiguration.ALL_COLUMNS) ||
								compareColumns2.contains(source1.getColumns().get(i).getName()))) {
						
						Object field2 = row2.get(i);
						ComparisonResult comparison = ComparisonUtils.compare(field1, 
																				field2,
																				smallestAbsoluteValue,
																				toleranceLevel,
																				ignoreCase); 
						if (comparison.isDifferent()) {
							Difference difference = new Difference.Builder()
								.alias1(source1.getAlias())
								.alias2(source2.getAlias())
								.column(source1.getColumns().get(i))
								.comparison(comparison)
								.field1(field1)
								.field2(field2)
								.key(key)
								.keyColumns(source1.getKeyColumns())
								.build();
							writeDifference(destinations, difference);
							matchedRow = false;
						}
					}
					else  {
						Difference difference = new Difference.Builder()
							.alias1(source1.getAlias())
							.alias2(source2.getAlias())
							.column(source1.getColumns().get(i))
							.field1(field1)
							.field2("Missing")
							.key(key)
							.keyColumns(source1.getKeyColumns())
							.build();
						writeDifference(destinations, difference);
						matchedRow = false;
					}
				}
            }
			
			if (matchedRow) {
				matchCount++;
			}
			compareCount++;
        }
        
        for (Key key : keySet2) {
			
        	if (!keySet1.contains(key)) {
				List<?> row2 = source2.getRow(key);					
				
				for (int i = 0; i < row2.size(); i++) {
					Object columnName = source2.getColumns().get(i).getName();
					
					if (!source2.getKeyColumns().contains(columnName)
							&& (compareColumns2.contains(columnName)  ||
								compareColumns2.contains(RecxxConfiguration.ALL_COLUMNS))) {
						
						Object field2 = row2.get(i);
						Difference difference = new Difference.Builder()
							.alias1(source1.getAlias())
							.alias2(source2.getAlias())
							.column(source2.getColumns().get(i))
							.field1("Missing")
							.field2(field2)
							.key(key)
							.keyColumns(source2.getKeyColumns())
							.build();
						writeDifference(destinations, difference);
					}
				}
			}
		}
        
        Summary summary = new Summary.Builder()
            .alias1Count(keySet1.size())
            .alias2Count(keySet2.size())
            .alias1(source1.getAlias())
            .alias2(source2.getAlias())
            .matchCount(matchCount)
            .build();
		writeSummary(destinations, summary);
        
        close(destinations);

        LOGGER.info("Memory used: " + SystemUtils.memoryUsed() + "%");
        LOGGER.info("Compare complete in " + ((System.currentTimeMillis() - t) / 1000d) +"s");
        
        return summary;
	            
	}

	private void writeDifference(List<Destination> destinations, Difference difference) {
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
    	if (args.length == 0) {
    		LOGGER.error("Properties configuration file required, please provide a valid properties filepath");
    	} 
    	else {
    		try {
	    		LOGGER.info("Running Recxx with the following config: '" + args[0] + "'");
	    		Recxx2 recxx = new Recxx2(args[0]);
    			recxx.execute();
    		} catch (Exception e) {
    			LOGGER.error("A problem has occurred, using the configuration file supplied: "
    				 + args[0] + "', please recheck your configuration");
    			e.printStackTrace();
    		}
    	}
    }

}
