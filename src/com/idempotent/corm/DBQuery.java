/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.idempotent.corm;

import com.codename1.db.Cursor;
import com.codename1.db.Database;
import com.codename1.db.Row;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author aardvocate
 */
public class DBQuery {

    private final DBConnector dbConnector;
    private final Database db;

    /**
     *
     * @param dbConnector DBConnector instance
     */
    public DBQuery(DBConnector dbConnector) {
        this.dbConnector = dbConnector;
        db = this.dbConnector.db;
    }

    /**
     *
     * @param tableName Name of table
     * @param columnNames List of Column Names
     * @param columnTypes List of Column Types, must be same length as
     * columnNames. A RuntimeException is thrown if lengths don't match.
     * @param columnConstraints List of columnConstraints, must be same length
     * as columnNames. A RuntimeException is thrown if lengths don't match. Use
     * empty string for no-constraint
     * @param tableConstraint sql statement to add to end of the create
     * statement.
     * @param silent If true, exception is not thrown if error occurred.
     * @return true or false
     * @throws RuntimeException
     * @throws IOException
     */
    public boolean createTable(String tableName, String[] columnNames, String[] columnTypes, String[] columnConstraints, String tableConstraint, boolean silent) throws RuntimeException, IOException {
        if (columnNames.length != columnTypes.length) {
            throw new RuntimeException("Column names length did not match column types length: (" + columnNames.length + ":" + columnTypes.length + ")");
        }

        if (columnNames.length != columnConstraints.length) {
            throw new RuntimeException("Column names length did not match column constraints length: (" + columnNames.length + ":" + columnConstraints.length + ")");
        }

        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (";

        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            String columnType = columnTypes[i];
            String columnConstraint = columnConstraints[i];

            query += columnName + " " + columnType + " " + columnConstraint + ", ";
        }

        query = query.substring(0, query.length() - 2) + ")";
        query += tableConstraint;

        System.err.println(query);
        try {
            db.beginTransaction();
            db.execute(query);
            db.commitTransaction();
            return true;
        } catch (IOException ex) {
            db.rollbackTransaction();
            return throwEx(ex, silent);
        }
    }

    public boolean update(String tableName, String[] updateColumns, String updateValues[], String[] whereColumns, String[] whereValues, boolean silent) throws IOException {
        if (whereColumns != null) {
            if (whereColumns.length != whereValues.length) {
                throw new RuntimeException("Where column names length did not match where values length: (" + whereColumns.length + ":" + whereValues.length + ")");
            }
        } else {
            throw new RuntimeException("Where column names can not be null");
        }

        if (updateColumns != null) {
            if (updateColumns.length != updateValues.length) {
                throw new RuntimeException("Update column names length did not match update values length: (" + updateColumns.length + ":" + updateValues.length + ")");
            }
        } else {
            throw new RuntimeException("Update column names can not be null");
        }

        String query = "UPDATE " + tableName + " SET ";

        for (int i = 0; i < updateColumns.length; i++) {
            String updateColumn = updateColumns[i];
            String updateValue = updateValues[i];

            query += updateColumn + "=" + updateValue + ", ";
        }

        query = query.substring(0, query.length() - 2);

        query += " WHERE " + whereColumns[0] + " = " + whereValues[0] + " AND ";
        for (int i = 1; i < whereColumns.length; i++) {
            String whereCol = whereColumns[i];
            String whereVal = whereValues[i];

            query += whereCol + " = " + whereVal + " AND ";
        }

        query = query.substring(0, query.length() - 4);

        System.err.println(query);
        try {
            db.beginTransaction();
            db.execute(query);
            db.commitTransaction();
            return true;
        } catch (IOException ex) {
            db.rollbackTransaction();
            throwEx(ex, false);
        }
        return false;
    }

    public boolean delete(String tableName, String[] whereColumns, String[] whereValues, boolean silent) throws IOException {
        String query = "DELETE FROM " + tableName + " ";
        if (whereColumns != null) {
            if (whereColumns.length != whereValues.length) {
                throw new RuntimeException("Where column names length did not match where values length: (" + whereColumns.length + ":" + whereValues.length + ")");
            }

            query += "WHERE " + whereColumns[0] + " = " + whereValues[0] + " AND ";
            for (int i = 1; i < whereColumns.length; i++) {
                String whereCol = whereColumns[i];
                String whereVal = whereValues[i];

                query += whereCol + " = " + whereVal + " AND ";
            }

            query = query.substring(0, query.length() - 4);
        }
        System.err.println(query);
        try {
            db.beginTransaction();
            db.execute(query);
            db.commitTransaction();

            return true;
        } catch (IOException ex) {
            db.rollbackTransaction();
            throwEx(ex, false);
        }
        return false;
    }

    /**
     *
     * @param tableName Name of the table
     * @param columnNames List of Column Names
     * @param values List of Column Values. Must be same length as columnNames.
     * A RuntimeException is thrown if lengths don't match.
     * @param silent If true, exception is not thrown if error occurred.
     * @return true or false
     * @throws IOException
     */
    public boolean insert(String tableName, String[] columnNames, String[] values, boolean silent) throws IOException {
        if (columnNames.length != values.length) {
            throw new RuntimeException("Column names length did not match values length: (" + columnNames.length + ":" + values.length + ")");
        }

        String query = "INSERT INTO " + tableName + " (";
        for (String colName : columnNames) {
            query += colName + ", ";
        }

        query = query.substring(0, query.length() - 2) + ") VALUES (";

        for (String val : values) {
            query += val + ", ";
        }

        query = query.substring(0, query.length() - 2) + ")";

        System.err.println(query);
        try {
            db.beginTransaction();
            db.execute(query);
            db.commitTransaction();
            return true;
        } catch (IOException ex) {
            db.rollbackTransaction();
            return throwEx(ex, silent);
        }
    }

    /**
     *
     * @param tableName Name of the table
     * @param joinClause sql specifying the join if any. Empty space if no
     * joins. Can not be null.
     * @param columnName Name of column to select
     * @param whereColumns List of columns to use in where clause. Can be null
     * @param whereValues List of values to use in where clause. Must be same
     * length as whereColumns. A RuntimeException is thrown if lengths don't
     * match.
     * @param otherParams other sql statements to add to the end of the where
     * statement, e.g ORDER BY, GROUP BY and LIMIT statements.
     * @return
     */
    public String selectOne(String tableName, String columnName, String joinClause, String[] whereColumns, String[] whereValues, String otherParams) throws IOException {
        String query = "SELECT " + columnName + " FROM " + tableName + " " + joinClause + " ";
        if (whereColumns != null) {
            if (whereColumns.length != whereValues.length) {
                throw new RuntimeException("Where column names length did not match where values length: (" + whereColumns.length + ":" + whereValues.length + ")");
            }

            query += "WHERE " + whereColumns[0] + " = " + whereValues[0] + " AND ";
            for (int i = 1; i < whereColumns.length; i++) {
                String whereCol = whereColumns[i];
                String whereVal = whereValues[i];

                query += whereCol + " = " + whereVal + " AND ";
            }

            query = query.substring(0, query.length() - 4);

        }

        System.err.println(query);

        try {
            db.beginTransaction();
            Cursor cursor = db.executeQuery(query);
            db.commitTransaction();

            if (cursor.next()) {
                Row row = cursor.getRow();
                String data = row.getString(0);
                return data;
            }
        } catch (IOException ex) {
            db.rollbackTransaction();
            throwEx(ex, false);
        }
        return null;
    }

    /**
     *
     * @param tableName Name of the table
     * @param joinClause sql specifying the join if any. Empty space if no
     * joins. Can not be null.
     * @param columnNames List of Column Names.
     * @param whereColumns List of columns to use in where clause. Can be null
     * @param whereValues List of values to use in where clause. Must be same
     * length as whereColumns. A RuntimeException is thrown if lengths don't
     * match.
     * @param otherParams other sql statements to add to the end of the where
     * statement, e.g ORDER BY, GROUP BY and LIMIT statements.
     * @return
     */
    public List<SortedMap<String, String>> selectMany(String tableName, String[] columnNames, String joinClause, String[] whereColumns, String[] whereValues, String otherParams) throws IOException {
        String query = "SELECT ";
        for (String colName : columnNames) {
            query += colName + ", ";
        }

        query = query.substring(0, query.length() - 2) + " FROM " + tableName + " "
                + joinClause + " ";

        List<SortedMap<String, String>> rows = new ArrayList<SortedMap<String, String>>();

        if (whereColumns != null) {
            if (whereColumns.length != whereValues.length) {
                throw new RuntimeException("Where column names length did not match where values length: (" + whereColumns.length + ":" + whereValues.length + ")");
            }

            query += " WHERE "
                    + whereColumns[0] + " = " + whereValues[0] + " AND ";

            for (int i = 1; i < whereColumns.length; i++) {
                String whereCol = whereColumns[i];
                String whereVal = whereValues[i];

                query += whereCol + " = '" + whereVal + "' AND ";
            }
            query = query.substring(0, query.length() - 4);
        }

        query += otherParams;
        
        System.err.println(query);

        try {
            db.beginTransaction();
            Cursor cursor = db.executeQuery(query);
            db.commitTransaction();

            while (cursor.next()) {
                Row row = cursor.getRow();
                SortedMap<String, String> rowMap = (SortedMap< String, String>) new TreeMap<String, String>();
                for (int i = 0; i < columnNames.length; i++) {
                    String colName = columnNames[i];
                    String data = row.getString(i);
                    rowMap.put(colName, data);
                }

                rows.add(rowMap);
            }
        } catch (IOException ex) {
            db.rollbackTransaction();
            throwEx(ex, false);
        }
        return rows;
    }

    private boolean throwEx(IOException ex, boolean silent) throws IOException {
        if (silent) {
            return false;
        } else {
            throw ex;
        }
    }
}
