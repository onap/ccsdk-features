/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.onap.ccsdk.features.sdnr.wt.common.database.Portstatus;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.AliasesEntry;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.AliasesEntryList;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DatabaseVersion;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.IndicesEntryList;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlDBIndicesEntry;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper.UnableToMapClassException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlDBClient {

    private static final Logger LOG = LoggerFactory.getLogger(SqlDBClient.class);

    // matches:
    //  1=>type, e.g. mariadb, mysql, ...
    //  2=>host
    //  3=>port
    //  4=>dbname
    private static final String DBURL_REGEX = "^jdbc:([^:]+):\\/\\/([^:]+):([0-9]+)\\/(.+)$";
    private static final Pattern DBURL_PATTERN = Pattern.compile(DBURL_REGEX);
    private static final String DBVERSION_REGEX = "^([\\d]+\\.[\\d]+\\.[\\d]+)";
    private static final Pattern DBVERSION_PATTERN = Pattern.compile(DBVERSION_REGEX);
    private static final String SELECT_VERSION_QUERY = "SELECT @@version as version";

    private static final String DBNAME_DEFAULT = "sdnrdb";
    private final String dbConnectionString;
    private final String dbName;
    private final String dbHost;
    private final int dbPort;

    /**
     *
     * @param dbUrl e.g. jdbc:mysql://sdnrdb:3306/sdnrdb
     * @param username
     * @param password
     */
    public SqlDBClient(String dbUrl, String username, String password) throws IllegalArgumentException {
        this.dbConnectionString = String.format("%s?user=%s&password=%s", dbUrl, username, password);
        final Matcher matcher = DBURL_PATTERN.matcher(dbUrl);
        if(!matcher.find()) {
            throw new IllegalArgumentException("unable to parse databaseUrl "+dbUrl);
        }
        this.dbHost = matcher.group(2);
        this.dbPort = Integer.parseInt(matcher.group(3));
        this.dbName = matcher.group(4);
    }

    public AliasesEntryList readViews() {
        return this.readViews(DBNAME_DEFAULT);
    }

    public AliasesEntryList readViews(String dbName) {
        AliasesEntryList list = new AliasesEntryList();
        final String query = "SELECT v.`TABLE_NAME` AS vn, t.`TABLE_NAME` AS tn\n"
                + "FROM `information_schema`.`TABLES` AS v\n"
                + "LEFT JOIN `information_schema`.`TABLES` AS t ON t.`TABLE_NAME` LIKE CONCAT(v.`TABLE_NAME`,'%')"
                + " AND t.`TABLE_TYPE`='BASE TABLE'\n" + "WHERE v.`TABLE_SCHEMA`='" + dbName
                + "' AND v.`TABLE_TYPE`='VIEW'";
        ResultSet data = this.read(query);
        try {
            while (data.next()) {
                list.add(new AliasesEntry(data.getString(2), data.getString(1)));
            }
        } catch (SQLException e) {
            LOG.warn("problem reading views: ", e);
        }
        return list;
    }

    public IndicesEntryList readTables() {
        final String query = "SHOW FULL TABLES WHERE `Table_type` = 'BASE TABLE'";
        IndicesEntryList list = new IndicesEntryList();
        ResultSet data = this.read(query);
        try {
            while (data.next()) {
                list.add(new SqlDBIndicesEntry(data.getString(1)));
            }
        } catch (SQLException e) {
            LOG.warn("problem reading tables: ", e);
        }
        return list;
    }

    public void waitForYellowStatus(long timeoutms) {
        Portstatus.waitSecondsTillAvailable(timeoutms/1000, this.dbHost, this.dbPort);
    }

    public DatabaseVersion readActualVersion() throws SQLException, ParseException {
        ResultSet data;
        try {
            data = this.read(SELECT_VERSION_QUERY);
            if (data.next()) {
                final String s = data.getString(1);
                final Matcher matcher = DBVERSION_PATTERN.matcher(s);
                data.afterLast();
                data.close();
                if (matcher.find()) {
                    return new DatabaseVersion(matcher.group(1));
                } else {
                    throw new ParseException(String.format("unable to extract version out of string '%s'", s), 0);
                }
            }
        } catch (SQLException e) {
            LOG.warn("problem reading tables: ", e);
        }
        throw new SQLException("unable to read version from database");
    }

    public boolean createTable(Entity entity, Class<?> clazz, String suffix) throws UnableToMapClassException {
        String createStatement = SqlDBMapper.createTable(clazz, entity, suffix);
        return this.createTable(createStatement);
    }

    public boolean createTable(String tableName, String tableMappings) {
        final String createStatement = String.format("CREATE TABLE IF NOT EXISTS `%s` (%s)", tableName, tableMappings);
        return this.createTable(createStatement);
    }

    public boolean createTable(String query) {
        try {
            Connection connection = this.getConnection();
            PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.execute();
            connection.close();
            return true;
        } catch (SQLException e) {
            LOG.warn("problem creating table:", e);
        }
        return false;
    }

    public boolean createView(String tableName, String viewName) throws SQLException {
        try {
            this.write(String.format("CREATE VIEW IF NOT EXISTS `%s` AS SELECT * FROM `%s`", viewName, tableName));
            return true;
        } catch (SQLException e) {
            LOG.warn("problem deleting table:", e);
        }
        return false;
    }

    public boolean deleteView(String viewName) throws SQLException {
        try {
            this.write(String.format("DROP VIEW IF EXISTS `%s`", viewName));
            return true;
        } catch (SQLException e) {
            LOG.warn("problem deleting view:", e);
        }
        return false;
    }

    public boolean update(String query) throws SQLException {
        boolean result = false;
        Connection connection = null;
        connection = DriverManager.getConnection(this.dbConnectionString);
        Statement stmt = connection.createStatement();
        result = stmt.execute(query);
        return stmt.getUpdateCount() > 0 ? stmt.getUpdateCount() > 0 : result;
    }

    public boolean write(String query) throws SQLException {
        Connection connection = this.getConnection();
        PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        boolean result = stmt.execute();
        connection.close();
        return stmt.getUpdateCount() > 0 ? stmt.getUpdateCount() > 0 : result;
    }

    public String writeAndReturnId(String query) throws SQLException {
        Connection connection = this.getConnection();
        PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        stmt.execute();
        ResultSet generatedKeys = stmt.getGeneratedKeys();
        connection.close();
        if (generatedKeys.next()) {
            return String.valueOf(generatedKeys.getLong(1));
        }
        return null;
    }

    public boolean deleteTable(String tableName) throws SQLException {
        try {
            this.write(String.format("DROP TABLE IF EXISTS `%s`", tableName));
            return true;
        } catch (SQLException e) {
            LOG.warn("problem deleting table:", e);
        }
        return false;
    }

    public String getDatabaseName() {
        return this.dbName;
    }

    public ResultSet read(String query) {
        ResultSet data = null;
        Connection connection = null;
        Statement stmt = null;
        try {
            connection = DriverManager.getConnection(this.dbConnectionString);
            stmt = connection.createStatement();
            data = stmt.executeQuery(query);
        } catch (SQLException e) {
            LOG.warn("problem reading tables: ", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.warn("problem closing connection: ", e);
            }
        }

        return data;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.dbConnectionString);
    }

    public boolean delete(String query) throws SQLException {
        this.write(query);
        return true;
    }



}
