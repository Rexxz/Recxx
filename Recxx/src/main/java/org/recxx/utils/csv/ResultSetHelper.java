package org.recxx.utils.csv;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetHelper {
    public String[] getColumnNames(ResultSet rs) throws SQLException;

    public String[] getColumnValues(ResultSet rs) throws SQLException, IOException;
}

