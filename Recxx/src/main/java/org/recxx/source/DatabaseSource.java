package org.recxx.source;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.recxx.domain.Column;
import org.recxx.domain.DatabaseMetaData;
import org.recxx.domain.Default;
import org.recxx.domain.FileMetaData;
import org.recxx.domain.Key;
import org.recxx.utils.SystemUtils;

import au.com.bytecode.opencsv.CSVWriter;

public class DatabaseSource implements Source<Key> {

	private static Logger LOGGER = Logger.getLogger(DatabaseSource.class);

	private final String alias;
	private final DatabaseMetaData databaseMetaData;

	private FileMetaData fileMetaData;
	private FileSource fileSource;
    private Connection connection;
    private Statement statement;

	public DatabaseSource(String alias, DatabaseMetaData databaseMetaData) {
		this.alias = alias;
		this.databaseMetaData = databaseMetaData;
	}

	public Source<Key> call() throws Exception {
		openDB();
		ResultSet resultset = getResultset();
        LOGGER.info("Persisting data to file");
        fileMetaData = configureFileMetaData(resultset.getMetaData(), databaseMetaData);
		LOGGER.info("Writing temporary data to " + fileMetaData.getFilePath());
		Writer fileWriter = new FileWriter(fileMetaData.getFilePath());
		CSVWriter writer = new CSVWriter(fileWriter, fileMetaData.getDelimiter().charAt(0), CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, fileMetaData.getLineDelimiter());
		writer.writeAll(resultset, true);
		writer.flush();
		writer.close();
		closeDB();
		fileSource = new RandomAccessFileSource(alias, fileMetaData);
		return fileSource.call();
	}
	
    private void closeDB() throws SQLException {
        if (statement != null) {
        	statement.close();
        }
        if (connection != null) {
        	connection.close();
        }
    }

    private void openDB() throws ClassNotFoundException, SQLException {
        LOGGER.info("Connected to DB using " + databaseMetaData.getDatabaseUrl());
    	Class.forName(databaseMetaData.getDatabaseDriver());
        connection = DriverManager.getConnection(databaseMetaData.getDatabaseUrl(), databaseMetaData.getDatabaseUserId(), databaseMetaData.getDatabasePassword());
        statement = connection.createStatement();
        LOGGER.info("Successfully initialised the DB connections");
    }

	private ResultSet getResultset() throws Exception {
		ResultSet rs = null;
		String sql = databaseMetaData.getSql();
		File file = new File(sql);
		if (file.exists()) {
			LOGGER.info("File based SQL discovered, will attempt to load sql file: " + databaseMetaData.getSql());
			sql = FileUtils.readFileToString(file);
			sql = SystemUtils.replaceSystemProperties(sql);
		}
		LOGGER.info("Running sql :" + sql);
		rs = statement.executeQuery(sql);
		return rs;
	}
	
	private FileMetaData configureFileMetaData(ResultSetMetaData resultSetMetaData, DatabaseMetaData databaseMetaData) throws SQLException {
		List<Column> columns = new ArrayList<Column>();
		for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
			Column column = new Column(resultSetMetaData.getColumnName(i+1), convert(resultSetMetaData.getColumnType(i+1)));
			columns.add(column);
		}
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		String generatedFileName = alias + Default.FILE_DATE_FORMAT.format(new Date()) + ".psv";
		return new FileMetaData.Builder()
							.filePath(new File(tmpdir, generatedFileName).getPath())
							.keyColumns(databaseMetaData.getKeyColumns())
							.columns(columns)
							.delimiter(Default.DELIMITER)
							.lineDelimiter(Default.LINE_DELIMITER)
							.ignoreHeaderRow(true)
							.columnsToCompare(databaseMetaData.getColumnsToCompare())
							.dateFormats(Arrays.asList(Default.ISO_DATE_FORMAT.toString()))
							.build();		
	}
	
	
	@SuppressWarnings("rawtypes")
	public Class convert( int type ) {
		Class result = java.lang.Object.class;

		switch( type ) {
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
				result = java.lang.String.class;
				break;
	
			case Types.NUMERIC:
			case Types.DECIMAL:
				result = java.math.BigDecimal.class;
				break;
	
			case Types.BIT:
				result = java.lang.Boolean.class;
				break;
	
			case Types.TINYINT:
				result = java.lang.Byte.class;
				break;
	
			case Types.SMALLINT:
				result = java.lang.Short.class;
				break;
	
			case Types.INTEGER:
				result = java.lang.Integer.class;
				break;
	
			case Types.BIGINT:
				result = java.lang.Long.class;
				break;
	
			case Types.FLOAT:
			case Types.DOUBLE:
				result = java.lang.Double.class;
				break;
	
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
				result = java.lang.Byte[].class;
				break;
	
			case Types.DATE:
				result = java.sql.Date.class;
				break;
	
			case Types.TIME:
				result = java.sql.Time.class;
				break;
	
			case Types.TIMESTAMP:
				result = java.sql.Timestamp.class;
				break;
		}

		return result;
	}
	
	public Set<Key> getKeySet() {
		return fileSource.getKeySet();
	}

	public List<?> getRow(Key key) {
		return fileSource.getRow(key);
	}

	public List<Column> getColumns() {
		return fileSource.getColumns();
	}

	public String getAlias() {
		return fileSource.getAlias();
	}

	public List<String> getKeyColumns() {
		return fileSource.getKeyColumns();
	}

	public List<String> getCompareColumns() {
		return fileSource.getCompareColumns();
	}

}
