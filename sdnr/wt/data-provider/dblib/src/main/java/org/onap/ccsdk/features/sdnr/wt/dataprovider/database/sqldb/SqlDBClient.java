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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.onap.ccsdk.features.sdnr.wt.common.database.Portstatus;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DatabaseVersion;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlTable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlView;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper.UnableToMapClassException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

public class SqlDBClient {

    private static final Logger LOG = LoggerFactory.getLogger(SqlDBClient.class);

    // matches:
    //  1=>type, e.g. mariadb, mysql, ...
    //  2=>host
    //  3=>port
    //  4=>dbname
    private static final String DBURL_REGEX = "^jdbc:([^:]+):\\/\\/([^:]+):([0-9]+)\\/(.+)$";
    private static final Pattern DBURL_PATTERN = Pattern.compile(DBURL_REGEX);
    // matches embedded Derby URLs like jdbc:derby:memory:testdb;create=true
    private static final String DBURL_EMBEDDED_REGEX = "^jdbc:(derby):memory:([^;]+)(.*)$";
    private static final Pattern DBURL_EMBEDDED_PATTERN = Pattern.compile(DBURL_EMBEDDED_REGEX);
    private static final String DBVERSION_REGEX = "^([\\d]+\\.[\\d]+\\.[\\d]+)";
    private static final Pattern DBVERSION_PATTERN = Pattern.compile(DBVERSION_REGEX);
    private static final String SELECT_VERSION_QUERY = "SELECT @@version as version";

    private static final String DBNAME_DEFAULT = "sdnrdb";
    private final String dbConnectionString;
    private final String dbName;
    private final String dbHost;
    private final int dbPort;
    private final String dbType;

    private final HikariDataSource connectionPool;

    /**
     * @param dbUrl    e.g. jdbc:mysql://sdnrdb:3306/sdnrdb
     * @param username
     * @param password
     */
    public SqlDBClient(String dbUrl, String username, String password) throws IllegalArgumentException {
        final Matcher matcher = DBURL_PATTERN.matcher(dbUrl);
        final Matcher embeddedMatcher = DBURL_EMBEDDED_PATTERN.matcher(dbUrl);
        if (matcher.find()) {
            this.dbType = matcher.group(1);
            this.dbHost = matcher.group(2);
            this.dbPort = Integer.parseInt(matcher.group(3));
            this.dbName = matcher.group(4);
            this.dbConnectionString = String.format("%s?user=%s&password=%s", dbUrl, username, password);
        } else if (embeddedMatcher.find()) {
            this.dbType = embeddedMatcher.group(1);
            this.dbHost = "localhost";
            this.dbPort = 0;
            this.dbName = embeddedMatcher.group(2);
            this.dbConnectionString = dbUrl;
        } else {
            throw new IllegalArgumentException("unable to parse databaseUrl " + dbUrl);
        }
        this.connectionPool = new HikariDataSource();
        this.connectionPool.setJdbcUrl(this.dbConnectionString);
        if (!isDerby()) {
            this.connectionPool.setUsername(username);
            this.connectionPool.setPassword(password);
        }
    }

    public boolean isDerby() {
        return "derby".equalsIgnoreCase(this.dbType);
    }

    public String translateSql(String sql) {
        if (!isDerby()) {
            return sql;
        }
        // Replace `identifier` with "IDENTIFIER" so Derby's uppercase convention is matched
        Matcher backtickMatcher = Pattern.compile("`([^`]+)`").matcher(sql);
        StringBuilder sb = new StringBuilder();
        while (backtickMatcher.find()) {
            backtickMatcher.appendReplacement(sb,
                    "\"" + Matcher.quoteReplacement(backtickMatcher.group(1).toUpperCase()) + "\"");
        }
        backtickMatcher.appendTail(sb);
        sql = sb.toString();
        sql = sql.replaceAll("(?i)\\bIF\\s+NOT\\s+EXISTS\\s+", "");
        sql = sql.replaceAll("(?i)\\bIF\\s+EXISTS\\s+", "");
        sql = sql.replaceAll("(?i)AUTO_INCREMENT", "GENERATED ALWAYS AS IDENTITY");
        sql = sql.replaceAll("(?i)\\s*CHARACTER\\s+SET\\s+utf8", "");
        sql = sql.replaceAll("(?i)\\s*COLLATE\\s+\\w+", "");
        // Remove CHECK constraints with regexp (Derby doesn't support regexp in CHECK)
        sql = sql.replaceAll("(?i)\\s*CHECK\\s*\\([^)]*regexp[^)]*\\)", "");
        sql = sql.replaceAll("(?i)int\\(\\d+\\)", "INT");
        sql = sql.replaceAll("(?i)NVARCHAR", "VARCHAR");
        sql = sql.replaceAll("(?i)DATETIME\\(\\d+\\)", "TIMESTAMP");
        sql = sql.replaceAll("(?i)\\bDATETIME\\b", "TIMESTAMP");
        sql = sql.replaceAll("(?i)\\bJSON\\b", "VARCHAR(32672)");
        sql = sql.replaceAll("(?i)\\bTINYINT\\b", "SMALLINT");
        sql = sql.replaceAll("(?i)\\bBOOLEAN\\b", "SMALLINT");
        // RLIKE '^prefix.*$' → LIKE 'prefix%'
        sql = sql.replaceAll("(?i)RLIKE '\\^([^']*)\\.\\*\\$'", "LIKE '$1%'");
        // Boolean string comparisons for SMALLINT columns
        sql = sql.replaceAll("='false'", "=0");
        sql = sql.replaceAll("='true'", "=1");
        // Unquote numeric values in comparisons (Derby strict typing)
        sql = sql.replaceAll("='(\\d+)'", "=$1");
        // LIMIT offset,count -> OFFSET offset ROWS FETCH NEXT count ROWS ONLY
        sql = sql.replaceAll("(?i)\\bLIMIT\\s+(\\d+)\\s*,\\s*(\\d+)", "OFFSET $1 ROWS FETCH NEXT $2 ROWS ONLY");
        // LIMIT count -> FETCH FIRST count ROWS ONLY
        sql = sql.replaceAll("(?i)\\bLIMIT\\s+(\\d+)", "FETCH FIRST $1 ROWS ONLY");
        // Remove trailing semicolons - Derby PreparedStatement doesn't support them
        sql = sql.replaceAll(";\\s*$", "");
        // Uppercase unquoted identifiers in primary key(...) and references ...(...)
        sql = fixKeyIdentifiers(sql);
        return sql;
    }

    private String fixKeyIdentifiers(String sql) {
        // Match primary key(col) or references "TABLE"(col) where col is not already double-quoted
        // Convert unquoted column names inside (...) after key/references to uppercase double-quoted
        Matcher keyMatcher = Pattern.compile("(?i)(primary\\s+key|references\\s+\"[^\"]+\")\\(([^)]+)\\)").matcher(sql);
        StringBuilder sb2 = new StringBuilder();
        while (keyMatcher.find()) {
            String prefix = keyMatcher.group(1);
            String cols = keyMatcher.group(2);
            // Uppercase each unquoted identifier
            String fixedCols = cols.replaceAll("(?<!\")\\b([a-zA-Z_][a-zA-Z0-9_-]*)\\b(?!\")", "\"$1\"")
                    .toUpperCase();
            keyMatcher.appendReplacement(sb2, Matcher.quoteReplacement(prefix + "(" + fixedCols + ")"));
        }
        keyMatcher.appendTail(sb2);
        return sb2.toString();
    }

    public List<SqlView> readViews() {
        if (isDerby()) {
            return readViewsDerby();
        }
        return this.readViews(DBNAME_DEFAULT);
    }

    public List<SqlView> readViews(String dbName) {
        if (isDerby()) {
            return readViewsDerby();
        }
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

    private List<SqlView> readViewsDerby() {
        List<SqlView> list = new ArrayList<>();
        List<String> tableNames = new ArrayList<>();
        try (Connection conn = this.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, "APP", "%", new String[] {"TABLE"});
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME").toLowerCase());
            }
            rs.close();
            rs = meta.getTables(null, "APP", "%", new String[] {"VIEW"});
            while (rs.next()) {
                String viewName = rs.getString("TABLE_NAME").toLowerCase();
                String tableRef = null;
                for (String table : tableNames) {
                    if (table.startsWith(viewName)) {
                        tableRef = table;
                        break;
                    }
                }
                list.add(new SqlView(tableRef, viewName));
            }
            rs.close();
        } catch (SQLException e) {
            LOG.warn("problem reading views: ", e);
        }
        return list;
    }

    public List<SqlTable> readTables() {
        if (isDerby()) {
            return readTablesDerby();
        }
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

    private List<SqlTable> readTablesDerby() {
        List<SqlTable> list = new ArrayList<>();
        try (Connection conn = this.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, "APP", "%", new String[] {"TABLE"});
            while (rs.next()) {
                list.add(new SqlTable(rs.getString("TABLE_NAME").toLowerCase()));
            }
            rs.close();
        } catch (SQLException e) {
            LOG.warn("problem reading tables: ", e);
        }
        return list;
    }

    public void waitForYellowStatus(long timeoutms) {
        if (isDerby()) {
            return;
        }
        Portstatus.waitSecondsTillAvailable(timeoutms / 1000, this.dbHost, this.dbPort);
    }

    public DatabaseVersion readActualVersion() throws SQLException, ParseException {
        if (isDerby()) {
            return new DatabaseVersion("10.16.1");
        }
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
        query = translateSql(query);
        boolean result = false;
        PreparedStatement stmt = null;
        Connection connection = null;
        try {
            connection = this.getConnection();
            stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.execute();

            result = true;
        } catch (SQLException e) {
            // Derby X0Y32 = object already exists; treat as success
            if (isDerby() && "X0Y32".equals(e.getSQLState())) {
                result = true;
            } else {
                LOG.warn("problem creating table:", e);
            }
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
            if (isDerby()) {
                this.write(String.format("CREATE VIEW \"%s\" AS SELECT * FROM \"%s\"",
                        viewName.toUpperCase(), tableName.toUpperCase()));
            } else {
                this.write(String.format("CREATE VIEW IF NOT EXISTS `%s` AS SELECT * FROM `%s`", viewName, tableName));
            }
            return true;
        } catch (SQLException e) {
            if (isDerby() && e.getSQLState() != null && "X0Y32".equals(e.getSQLState())) {
                return true;
            }
            LOG.warn("problem creating view:", e);
        }
        return false;
    }

    public boolean deleteView(String viewName) throws SQLException {
        try {
            this.write(translateSql(String.format("DROP VIEW IF EXISTS `%s`", viewName)));
            return true;
        } catch (SQLException e) {
            // Derby 42X05 = table/view does not exist; treat as success
            if (isDerby() && "42X05".equals(e.getSQLState())) {
                return true;
            }
            LOG.warn("problem deleting view:", e);
        }
        return false;
    }

    public boolean update(String query) throws SQLException {
        query = translateSql(query);
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
        query = translateSql(query);
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
        query = translateSql(query);
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
            this.write(translateSql(String.format("DROP TABLE IF EXISTS `%s`", tableName)));
            return true;
        } catch (SQLException e) {
            // Derby 42Y55 = table does not exist; treat as success
            if (isDerby() && "42Y55".equals(e.getSQLState())) {
                return true;
            }
            LOG.warn("problem deleting table:", e);
        }
        return false;
    }

    public String getDatabaseName() {
        return this.dbName;
    }

    public ResultSet read(String query) {
        query = translateSql(query);
        ResultSet data = null;
        Statement stmt = null;
        Connection connection = null;
        try {
            connection = this.getConnection();
            stmt = connection.createStatement();
            data = stmt.executeQuery(query);
            if (isDerby()) {
                CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
                crs.populate(data);
                data.close();
                data = crs;
            }
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
