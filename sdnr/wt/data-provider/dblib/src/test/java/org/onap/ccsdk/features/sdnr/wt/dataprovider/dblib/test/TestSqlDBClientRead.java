/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.dblib.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;

public class TestSqlDBClientRead {

    private static final String DERBY_URL = "jdbc:derby:memory:sqldbclientread;create=true";

    @BeforeClass
    public static void createTable() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DERBY_URL);
                Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE readtest (id INT, name VARCHAR(32))");
            stmt.executeUpdate("INSERT INTO readtest VALUES (1, 'first'), (2, 'second')");
        }
    }

    /**
     * A client configured for MariaDB takes the non-Derby code path, while its connections come
     * from Derby. Like MariaDB Connector/J 3.x, Derby closes a ResultSet when its Statement is
     * closed, so read() has to hand back rows that stay readable after it has released them.
     */
    @Test
    public void testReadResultIsUsableAfterStatementIsClosed() throws SQLException {
        SqlDBClient client = new SqlDBClient("jdbc:mariadb://localhost:3306/sdnrdb", "sdnrdb", "secret") {
            @Override
            public Connection getConnection() throws SQLException {
                return DriverManager.getConnection(DERBY_URL);
            }
        };
        assertFalse(client.isDerby());

        ResultSet data = client.read("SELECT id, name FROM readtest ORDER BY id");

        assertNotNull(data);
        assertTrue(data.next());
        assertEquals(1, data.getInt(1));
        assertEquals("first", data.getString(2));
        assertTrue(data.next());
        assertEquals("second", data.getString(2));
        assertFalse(data.next());
        data.close();
    }
}
