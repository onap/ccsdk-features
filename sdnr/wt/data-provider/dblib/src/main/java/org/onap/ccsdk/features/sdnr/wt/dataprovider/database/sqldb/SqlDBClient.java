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

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.onap.ccsdk.features.sdnr.wt.common.database.Portstatus;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DatabaseVersion;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlTable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlView;
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

    private final HikariDataSource connectionPool;

    /**
     * @param dbUrl    e.g. jdbc:mysql://sdnrdb:3306/sdnrdb
     * @param username
     * @param password
     */
    public SqlDBClient(String dbUrl, String username, String password) throws IllegalArgumentException {
        this.dbConnectionString = String.format("%s?user=%s&password=%s", dbUrl, username, password);
        final Matcher matcher = DBURL_PATTERN.matcher(dbUrl);
        if (!matcher.find()) {
            throw new IllegalArgumentException("unable to parse databaseUrl " + dbUrl);
        }
        this.dbHost = matcher.group(2);
        this.dbPort = Integer.parseInt(matcher.group(3));
        this.dbName = matcher.group(4);
        this.connectionPool = new HikariDataSource();
        this.connectionPool.setJdbcUrl(this.dbConnectionString);
        this.connectionPool.setUsername(username);
        this.connectionPool.setPassword(password);
    }

    public List<SqlView> readViews() {
        return this.readViews(DBNAME_DEFAULT);
    }

    public List<SqlView> readViews(String dbName) {
        List<SqlView> list = new ArrayList<>();
        final String query = "SELECT v.`TABLE_NAME` AS vn, t.`TABLE_NAME` AS tn\n"
                + "FROM `information_schema`.`TABLES` AS v\n"
                + "LEFT JOIN `information_schema`.`TABLES` AS t ON t.`TABLE_NAME` LIKE CONCAT(v.`TABLE_NAME`,'%')"
                + " AND t.`TABLE_TYPE`='BASE TABLE'\n" + "WHERE v.`TABLE_SCHEMA`='" + dbName
                + "' AND v.`TABLE_TYPE`='VIEW'";
        ResultSet data = this.read(query);
        try {
            while (data.next()) {
                list.add(new SqlView(data.getString(2), data.getString(1)));
            }
        } catch (SQLException e) {
            LOG.warn("problem reading views: ", e);
        }
        try {
            data.close();
        } catch (SQLException ignore) {
        }
        return list;
    }

    public List<SqlTable> readTables() {
        final String query = "SHOW FULL TABLES WHERE `Table_type` = 'BASE TABLE'";
        List<SqlTable> list = new ArrayList<>();
        ResultSet data = this.read(query);
        try {
            while (data.next()) {
                list.add(new SqlTable(data.getString(1)));
            }
        } catch (SQLException e) {
            LOG.warn("problem reading tables: ", e);
        }
        try {
            data.close();
        } catch (SQLException ignore) {
        }
        return list;
    }

    public void waitForYellowStatus(long timeoutms) {
        Portstatus.waitSecondsTillAvailable(timeoutms / 1000, this.dbHost, this.dbPort);
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
            LOG.warn("problem reading actual version: ", e);
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
        boolean result = false;
        PreparedStatement stmt = null;
        Connection connection = null;
        try {
            connection = this.getConnection();
            stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.execute();

            result = true;
        } catch (SQLException e) {
            LOG.warn("problem creating table:", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException logOrIgnore) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException logOrIgnore) {
                }
            }
        }
        return result;
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
        SQLException innerE = null;
        Statement stmt = null;
        Connection connection = null;
        try {
            connection = this.getConnection();
            stmt = connection.createStatement();
            result = stmt.execute(query);
            result = stmt.getUpdateCount() > 0 ? stmt.getUpdateCount() > 0 : result;
        } catch (SQLException e) {
            innerE = e;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignore) {
                }
            }
        }
        if (innerE != null) {
            throw innerE;
        }
        return result;
    }

    public boolean write(String query) throws SQLException {
        boolean result = false;
        SQLException innerE = null;
        PreparedStatement stmt = null;
        Connection connection = null;
        try {
            connection = this.getConnection();
            stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            result = stmt.execute();
            result = stmt.getUpdateCount() > 0 ? stmt.getUpdateCount() > 0 : result;
        } catch (SQLException e) {
            innerE = e;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignore) {
                }
            }
        }
        if (innerE != null) {
            throw innerE;
        }
        return result;
    }

    public String writeAndReturnId(String query) throws SQLException {
        String result = null;
        SQLException innerE = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        Connection connection = null;
        try {
            connection = this.getConnection();
            stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.execute();
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                result = String.valueOf(generatedKeys.getLong(1));
            }
        } catch (SQLException e) {
            innerE = e;
        } finally {
            if (generatedKeys != null) {
                try {
                    generatedKeys.close();
                } catch (SQLException ignore) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignore) {
                }
            }
        }
        if (innerE != null) {
            throw innerE;
        }
        return result;
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
        Statement stmt = null;
        Connection connection = null;
        try {
            connection = this.getConnection();
            stmt = connection.createStatement();
            data = stmt.executeQuery(query);
        } catch (SQLException e) {
            LOG.warn("problem reading db for query '{}': ", query, e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignore) {
                }
            }
        }
        return data;
    }

    public Connection getConnection() throws SQLException {
        return this.connectionPool.getConnection();
        //return DriverManager.getConnection(this.dbConnectionString);
    }

    public boolean delete(String query) throws SQLException {
        this.write(query);
        return true;
    }
}
