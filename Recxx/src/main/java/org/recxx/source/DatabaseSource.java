package org.recxx.source;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
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
import org.recxx.utils.csv.CSVWriter;

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
        LOGGER.info("Source '" + getAlias() + "': Persisting data to file");
        fileMetaData = configureFileMetaData(resultset.getMetaData(), databaseMetaData);
		writeFile(resultset);
		resultset = null;
		closeDB();
		fileSource = new RandomAccessFileSource(alias, fileMetaData);
		return fileSource.call();
	}

	private void writeFile(ResultSet resultset) throws IOException, SQLException {
		LOGGER.info("Source '" + getAlias() + "': Writing temporary data to " + fileMetaData.getFilePath());
		Writer fileWriter = new FileWriter(fileMetaData.getFilePath());
		CSVWriter writer = new CSVWriter(fileWriter, fileMetaData.getDelimiter().charAt(0), CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, fileMetaData.getLineDelimiter());
		writer.writeAll(resultset, true);
		writer.flush();
		writer.close();
	}
	
    private void closeDB() throws SQLException {
        if (statement != null) {
        	statement.close();
        	statement = null;
        }
        if (connection != null) {
        	connection.close();
        	connection = null;
        }
    }
    
    private void openDB() throws ClassNotFoundException, SQLException {
    	StringBuilder sb = new StringBuilder();
    	LOGGER.info(sb.append("Source '").append(getAlias())
    			.append("': Connecting to Server '").append(databaseMetaData.getDatabaseUrl())
    			.append("', User '").append(databaseMetaData.getDatabaseUserId()).toString());
        LOGGER.info("Connecting to DB using " + sb.toString());
    	Class.forName(databaseMetaData.getDatabaseDriver());
        connection = DriverManager.getConnection(databaseMetaData.getDatabaseUrl(), databaseMetaData.getDatabaseUserId(), databaseMetaData.getDatabasePassword());
        statement = connection.createStatement();
        LOGGER.info("Successfully initialised the Database connection");
    }

	private ResultSet getResultset() throws Exception {
		ResultSet rs = null;
		String sql = databaseMetaData.getSql();
		LOGGER.info("Attempting to use the following sql config:" + databaseMetaData.getSql());
		File file = new File(sql);
		if (file.exists()) {
			LOGGER.info("Source '" + getAlias() + "': File based SQL discovered, will attempt to load sql file: " + databaseMetaData.getSql());
			sql = SystemUtils.replaceSystemProperties(FileUtils.readFileToString(file));
		}
		LOGGER.info("Source '" + getAlias() + "': Running sql :" + sql);
		try {
			rs = statement.executeQuery(sql);
		} catch (Exception e) {
			throw new InterruptedException("Source '" + getAlias() + "' failed attempting to run the SQL statement, if this is a file, it doesnt exist: " + sql);
		}
		LOGGER.info("Source '" + getAlias() + "': Sql completed");
		return rs;
	}
	
	private FileMetaData configureFileMetaData(ResultSetMetaData resultSetMetaData, DatabaseMetaData databaseMetaData) throws SQLException {
		List<Column> columns = new ArrayList<Column>();
		for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
			Column column = new Column(resultSetMetaData.getColumnName(i+1), convert(resultSetMetaData.getColumnType(i+1)));
			columns.add(column);
		}
		boolean temporaryFile = true;
		String filePath;
		String delimiter;
		String lineDelimiter;
		if (databaseMetaData.getFilePath() != null) {
			filePath = databaseMetaData.getFilePath();
			delimiter = databaseMetaData.getDelimiter();
			lineDelimiter = databaseMetaData.getLineDelimiter();
			temporaryFile = false;
		}
		else {
			filePath = FileUtils.getTempDirectoryPath() + alias + Default.FILE_DATE_FORMAT.format(new Date()) + ".psv";
			delimiter = Default.PIPE_DELIMITER;
			lineDelimiter = Default.WINDOWS_LINE_DELIMITER;
		}
		return new FileMetaData.Builder()
							.filePath(filePath)
							.keyColumns(databaseMetaData.getKeyColumns())
							.columns(columns)
							.delimiter(delimiter)
							.lineDelimiter(lineDelimiter)
							.ignoreHeaderRow(true)
							.temporaryFile(temporaryFile)
							.columnsToIgnore(databaseMetaData.getColumnsToIgnore())
							.columnsToCompare(databaseMetaData.getColumnsToCompare())
							.dateFormats(databaseMetaData.getDateFormats())
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
			case Types.TIMESTAMP:
			case Types.TIME:
				result = java.util.Date.class;
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
		return alias;
	}

	public List<String> getKeyColumns() {
		return fileSource.getKeyColumns();
	}

	public List<String> getCompareColumns() {
		return fileSource.getCompareColumns();
	}

	public List<String> getIgnoreColumns() {
		return fileSource.getIgnoreColumns();
	}

	public int getColumnIndex(String columnName) {
		return fileSource.getColumnIndex(columnName);
	}

	public void close() {
		fileSource.close();
	}


}
