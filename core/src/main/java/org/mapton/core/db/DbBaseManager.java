/* 
 * Copyright 2019 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mapton.core.db;

import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 *
 * @author Patrik Karlström
 */
public abstract class DbBaseManager {

    protected static final String SQL_BIGINT = "BIGINT";
    protected static final String SQL_BIGINT_NOT_NULL = "BIGINT not null";
    protected static final String SQL_BOOLEAN = "BOOLEAN";
    protected static final String SQL_DATE = "DATE";
    protected static final String SQL_DOUBLE = "DOUBLE";
    protected static final String SQL_IDENTITY = "IDENTITY";
    protected static final String SQL_INTEGER = "INTEGER";
    protected static final String SQL_TIMESTAMP = "TIMESTAMP";
    protected static final String SQL_VARCHAR = "VARCHAR";

    protected final Db mDb;
    protected DbColumn mId;
    protected PlaceHolderController mInsertPlaceHolders = new PlaceHolderController();
    protected PreparedStatement mInsertPreparedStatement;
    protected PlaceHolderController mSelectPlaceHolders = new PlaceHolderController();
    protected PreparedStatement mSelectPreparedStatement;
    protected DbTable mTable;
    protected PlaceHolderController mUpdatePlaceHolders = new PlaceHolderController();
    protected PreparedStatement mUpdatePreparedStatement;

    public DbBaseManager() {
        mDb = Db.getInstance();
    }

    public abstract void create();

    public DbColumn getId() {
        return mId;
    }

    public String getIndexName(DbColumn[] dbColumns, String suffix) {
        StringBuilder builder = new StringBuilder(mTable.getName()).append("_");

        if (dbColumns != null) {
            for (DbColumn dbColumn : dbColumns) {
                builder.append(dbColumn.getName()).append("_");
            }
        }

        if (builder.lastIndexOf("_") != builder.length() - 1) {
            builder.append("_");
        }

        builder.append(suffix);

        return builder.toString();
    }

    public DbSchema getSchema() {
        return mDb.getSpec().getDefaultSchema();

    }

    public DbTable getTable() {
        return mTable;
    }

    protected void addNotNullConstraints(DbColumn... columns) {
        for (DbColumn column : columns) {
            column.addConstraint(new DbConstraint(column, "nnc_" + column.getName(), Constraint.Type.NOT_NULL));
        }
    }

    protected Boolean getBoolean(ResultSet rs, DbColumn column) throws SQLException {
        return getBoolean(rs, column.getName());
    }

    protected Boolean getBoolean(ResultSet rs, String column) throws SQLException {
        return rs.getBoolean(column);
    }

    protected Double getDouble(ResultSet rs, DbColumn column) throws SQLException {
        return getDouble(rs, column.getName());
    }

    protected Double getDouble(ResultSet rs, String columnName) throws SQLException {
        return rs.getObject(columnName, Double.class);
    }

    protected String getFilterPattern(String filter) {
        filter = filter.replaceAll("\\*", "%");
        filter = filter.replaceAll("\\?", "_");

        if (filter.length() == 0) {
            filter = "%";
        }

        if (!filter.contains("%")) {
            filter = String.format("%%%s%%", filter);
        }

        return filter;
    }

    protected Integer getInteger(ResultSet rs, DbColumn column) throws SQLException {
        return getInteger(rs, column.getName());
    }

    protected Integer getInteger(ResultSet rs, String columnName) throws SQLException {
        return rs.getObject(columnName, Integer.class);
    }

    protected Long getLong(ResultSet rs, DbColumn column) throws SQLException {
        return getLong(rs, column.getName());
    }

    protected Long getLong(ResultSet rs, String columnName) throws SQLException {
        return rs.getObject(columnName, Long.class);
    }

    protected String getString(ResultSet rs, DbColumn column) throws SQLException {
        return getString(rs, column.getName());
    }

    protected String getString(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    protected Timestamp getTimestamp(ResultSet rs, DbColumn column) throws SQLException {
        return rs.getTimestamp(column.getName());

    }

    public class Columns {

        public DbColumn getId() {
            return mId;
        }
    }
}
