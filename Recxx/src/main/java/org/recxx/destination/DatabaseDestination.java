package org.recxx.destination;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.recxx.domain.DatabaseMetaData;
import org.recxx.domain.Difference;
import org.recxx.domain.Key;
import org.recxx.domain.Summary;
import org.recxx.source.Source;

public class DatabaseDestination extends AbstractDestination {

	private static Logger LOGGER = Logger.getLogger(DatabaseDestination.class);

	private final DatabaseMetaData databaseMetaData;
	private Session currentSession;
	private SessionFactory sessionFactory;

	public DatabaseDestination (DatabaseMetaData databaseMetaData) {
		this.databaseMetaData = databaseMetaData;
	}

	public void open() throws IOException {

		Configuration config = new Configuration()
	    	.setProperty("hibernate.mapping.precedence", "class")
	    	.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider")
		    .setProperty("hibernate.show_sql", "false")
		    .setProperty("hibernate.format_sql","true")
		    .setProperty("hibernate.use_sql_comments","true")
		    .setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory")
		    .setProperty("hibernate.current_session_context_class", "thread")
	    	.setProperty("org.hibernate.flushMode", "COMMIT")
	    	.setProperty("hibernate.generate_statistics", "true");
	    
	    config.addAnnotatedClass(org.recxx.domain.Summary.class);
	    
	    ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder();
	    serviceRegistryBuilder.applySettings(config.getProperties());
	    
	    StringBuilder sb = new StringBuilder();
	    if (databaseMetaData.getDataSource() != null) {
	    	serviceRegistryBuilder.applySetting(Environment.DATASOURCE, databaseMetaData.getDataSource());
			LOGGER.info(sb.append("': DataSource '").append(databaseMetaData.getDataSource().toString()));
			try {
				Connection connection = databaseMetaData.getDataSource().getConnection();
				java.sql.DatabaseMetaData metaData = connection.getMetaData();
				LOGGER.info(sb.append("': DataSource '").append(metaData.getURL()).append(" ").append(metaData.getUserName()));
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	    else {
	    	serviceRegistryBuilder.applySetting(Environment.DRIVER, databaseMetaData.getDatabaseDriver());
	    	serviceRegistryBuilder.applySetting(Environment.URL, databaseMetaData.getDatabaseUrl());
	    	serviceRegistryBuilder.applySetting(Environment.USER, databaseMetaData.getDatabaseUserId());
	    	serviceRegistryBuilder.applySetting(Environment.PASS, databaseMetaData.getDatabasePassword());
			LOGGER.info(sb.append("': DataSource '").append(databaseMetaData.getDatabaseUrl()).append(" ").append(databaseMetaData.getDatabaseUserId()));
	    }
	    
	    ServiceRegistry serviceRegistry = serviceRegistryBuilder.buildServiceRegistry();        
	    sessionFactory = config.buildSessionFactory(serviceRegistry);
	    currentSession = sessionFactory.getCurrentSession();

	    LOGGER.info("Successfully initialised the Database destination");
	}

	public void writeHeader(Source<Key> source1, Source<Key> source2) {
		return; // Not required as columns are defined in the database
	}
	
	public void writeDifference(Difference difference) {
		return; // To be implemented soon
	}

	public void writeSummary(Summary summary) {
		Transaction transaction = currentSession.beginTransaction();
		currentSession.persist(summary);
		transaction.commit();
	}

	public void close() throws IOException {
		try {
			if (currentSession != null && currentSession.isOpen()) currentSession.close();
			if (sessionFactory != null && !sessionFactory.isClosed()) sessionFactory.close();
		} catch (Exception e) {
			 e.printStackTrace();
		}
	}

}
