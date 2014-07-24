package org.recxx.utils;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.recxx.domain.DatabaseMetaData;

public class DriverManagerWrappedDataSource implements DataSource {

	private final DatabaseMetaData databaseMetaData;

	public DriverManagerWrappedDataSource(DatabaseMetaData databaseMetaData) {
		this.databaseMetaData = databaseMetaData;
    	try {
			Class.forName(databaseMetaData.getDatabaseDriver());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public PrintWriter getLogWriter() throws SQLException {
		return DriverManager.getLogWriter();
	}

	public int getLoginTimeout() throws SQLException {
		return DriverManager.getLoginTimeout();
	}

	public void setLogWriter(PrintWriter arg0) throws SQLException {
		DriverManager.setLogWriter(arg0);
	}

	public void setLoginTimeout(int arg0) throws SQLException {
		DriverManager.setLoginTimeout(arg0);
	}

	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		throw new AbstractMethodError();
	}

	public <T> T unwrap(Class<T> arg0) throws SQLException {
		throw new AbstractMethodError();
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(databaseMetaData.getDatabaseUrl(), databaseMetaData.getDatabaseUserId(), databaseMetaData.getDatabasePassword());
	}

	public Connection getConnection(String user, String password) throws SQLException {
		return DriverManager.getConnection(databaseMetaData.getDatabaseUrl(), user, password);
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}

}
