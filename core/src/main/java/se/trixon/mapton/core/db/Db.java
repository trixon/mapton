/* 
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.core.db;

import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.custom.mysql.MysObjects;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public class Db {

    private static final String DB_POSTFIX = ".mv.db";
    private static final Logger LOGGER = Logger.getLogger(Db.class.getName());
    private Connection mAutoCommitConnection = null;
    private final String mConnString;
//    private Connection mConnection = null;
    private final File mDbFile;
    private DbSchema mSchema;
    private final DbSpec mSpec;

    public static Db getInstance() {
        return Holder.INSTANCE;
    }

    private Db() {
        mDbFile = new File(System.getProperty("netbeans.user"), "mapton" + DB_POSTFIX);
        mConnString = String.format("jdbc:h2:%s;DEFRAG_ALWAYS=true", StringUtils.removeEnd(mDbFile.getAbsolutePath(), DB_POSTFIX));
        mSpec = new DbSpec();
        init();
    }

//    public void connectionCommit() throws ClassNotFoundException, SQLException {
//        getConnection().commit();
//    }
//    public void connectionOpen() throws ClassNotFoundException, SQLException {
//        if (mConnection != null && !mConnection.isClosed()) {
//            connectionCommit();
//            //mConnection.close();
//        }
//
//        Class.forName("org.h2.Driver");
//        mConnection = DriverManager.getConnection(mConnString);
//        mConnection.setAutoCommit(false);
//        //LOGGER.log(Level.INFO, "JDBC Connect: {0}", mConnString);
//    }
//    public boolean connectionRollback() {
//        try {
//            getConnection().rollback();
//            LOGGER.fine("JDBC Rollback");
//        } catch (SQLException ex) {
//            LOGGER.log(Level.SEVERE, "JDBC Rollback failed: {0}", ex.getMessage());
//            return false;
//        }
//
//        return true;
//    }
//
    public boolean create(DbTable table, boolean replace, DbConstraint... constraints) {
        boolean tableCreated;

        try (Statement statement = getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            if (table.getConstraints().isEmpty()) {
                for (DbConstraint constraint : constraints) {
                    table.addConstraint(constraint);
                }
            }

            CreateTableQuery createTableQuery = new CreateTableQuery(table, true);
            if (!replace) {
                createTableQuery.addCustomization(MysObjects.IF_NOT_EXISTS_TABLE);
            }

            String sql = createTableQuery.validate().toString();
            //System.out.println(sql);

            tableCreated = statement.execute(sql);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Table creation failed. {0}", table.getName());
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            tableCreated = false;
        }

        return tableCreated;
    }

    public void delete(DbTable table, DbColumn column, Long id) throws ClassNotFoundException, SQLException {
        try (Statement statement = getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String sql = String.format("DELETE FROM %s WHERE %s=%d;", table.getName(), column.getName(), id);
            System.out.println(sql);
            statement.execute(sql);
        }
    }

    public void drop(DbTable table, boolean cascade) throws ClassNotFoundException, SQLException {
        try (Statement statement = getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String sql = String.format("DROP TABLE IF EXISTS %s %s;", table.getName(), cascade ? "CASCADE" : "");
            //System.out.println(sql);
            statement.execute(sql);
        }
    }

    public void dropAllObjects() throws ClassNotFoundException, SQLException {
        try (Statement statement = getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String sql = "DROP ALL OBJECTS;";
            System.out.println(sql);
            statement.execute(sql);
        }
    }

    public Connection getAutoCommitConnection() {
        try {
            if (mAutoCommitConnection == null || mAutoCommitConnection.isClosed()) {
                Class.forName("org.h2.Driver");
                mAutoCommitConnection = DriverManager.getConnection(mConnString);
            } else {
                if (!mAutoCommitConnection.isValid(2)) {
                    mAutoCommitConnection = null;
                    LOGGER.severe("Database connection lost");
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.severe("Database may be already in use: Possible solutions: close all other connection(s); use the server mode [90020-196]");
        }

        return mAutoCommitConnection;
    }

//    public Connection getConnection() {
//        return mConnection;
//    }
    public File getDbFile() {
        return mDbFile;
    }

    public DbSpec getSpec() {
        return mSpec;
    }

    public void truncate(DbTable table) throws ClassNotFoundException, SQLException {
        try (Statement statement = getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String sql = String.format("TRUNCATE TABLE %s;", table.getName());
            //System.out.println(sql);
            statement.execute(sql);
        }
    }

    private void init() {
        mSpec.addDefaultSchema();
    }

    private static class Holder {

        private static final Db INSTANCE = new Db();
    }
}
