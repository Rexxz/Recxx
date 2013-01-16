package org.recxx.destination;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
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
		StringBuilder sb = new StringBuilder();
		LOGGER.info(sb.append("': Connecting to Server '").append(databaseMetaData.getDatabaseUrl())
				.append("', User '").append(databaseMetaData.getDatabaseUserId()).toString());

		Configuration config = new Configuration()
	    	.setProperty("hibernate.mapping.precedence", "class")
	    	.setProperty("hibernate.connection.driver_class", databaseMetaData.getDatabaseDriver())
	    	.setProperty("hibernate.connection.url", databaseMetaData.getDatabaseUrl())
	    	.setProperty("hibernate.connection.username", databaseMetaData.getDatabaseUserId())
	    	.setProperty("hibernate.connection.password", databaseMetaData.getDatabasePassword())
	    	.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider")
		    .setProperty("hibernate.show_sql", "false")
		    .setProperty("hibernate.format_sql","true")
		    .setProperty("hibernate.use_sql_comments","true")
		    .setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory")
		    .setProperty("hibernate.current_session_context_class", "thread")
	    	.setProperty("org.hibernate.flushMode", "COMMIT")
	    	.setProperty("hibernate.generate_statistics", "true");
	    
	    config.addAnnotatedClass(org.recxx.domain.Summary.class);
       
	    ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();        
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
