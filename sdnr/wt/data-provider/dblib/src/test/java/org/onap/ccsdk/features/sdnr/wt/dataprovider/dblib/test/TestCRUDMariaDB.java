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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.dblib.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.vorburger.exec.ManagedProcessException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DatabaseVersion;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlDBDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.dblib.test.util.MariaDBTestBase;

public class TestCRUDMariaDB {


    private static final String TABLE1_NAME = "table1";
    private static final String TABLE2_NAME = "table2-v6";
    private static final String TABLE3_NAME = "table3-v6";
    private static final String VIEW2_NAME = "table2";
    private static final String VIEW3_NAME = "table3";
    private static final String TABLE1_MAPPING = "col1 INT PRIMARY KEY, col2 NVARCHAR(30), col3 BOOLEAN";
    private static final String TABLE2_MAPPING = "col1 INT PRIMARY KEY, col2 NVARCHAR(30), col3 BOOLEAN";
    private static final String TABLE3_MAPPING = "col1 INT PRIMARY KEY, col2 NVARCHAR(30), col3 BOOLEAN";
    private static final String DELETE_ALL_FORMAT = "DELETE FROM `%s`";
    private static final String READ_ALL_FORMAT = "SELECT * FROM `%s`";
    private static final String TABLE1_INSERT_ENTRY_FORMAT =
            "INSERT INTO `" + TABLE1_NAME + "` (col1, col2, col3) VALUES(%d,'%s',%d);";
    private static final String TABLE1_UPDATE_ENTRY_FORMAT =
            "UPDATE `" + TABLE1_NAME + "` SET col2='%s', col3=%d WHERE col1=%d;";
    private static final String TABLE1_DELETE_ENTRY_FORMAT = "DELETE FROM `" + TABLE1_NAME + "` WHERE col1=%d;";
    private static String DBNAME = null;

    private static MariaDBTestBase testBase;
    private static SqlDBDataProvider dbProvider;
    private static SqlDBClient dbClient;

    @BeforeClass
    public static void init() throws Exception {

        testBase = new MariaDBTestBase();
        dbProvider = testBase.getDbProvider();
        dbProvider.waitForDatabaseReady(30, TimeUnit.SECONDS);
        dbClient = testBase.createRawClient();
        DBNAME = testBase.getDBName();
    }

    @AfterClass
    public static void close() {
        try {
            testBase.close();
        } catch (ManagedProcessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1() {
        ResultSet data;
        boolean success;
        String id = null;
        //create test1Table
        success = dbClient.createTable(TABLE1_NAME, TABLE1_MAPPING);
        assertTrue("failed to create table", success);
        //delete all
        try {
            success = dbClient.delete(String.format(DELETE_ALL_FORMAT, TABLE1_NAME));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assertTrue("failed to clear table", success);
        //test empty
        data = dbClient.read(String.format(READ_ALL_FORMAT, TABLE1_NAME));
        try {
            assertEquals(0, countRows(data));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("unable to read size");
        }
        try { data.close(); } catch (SQLException ignore) { }
        //create entry
        success = false;
        try {
            success = dbClient.write(String.format(TABLE1_INSERT_ENTRY_FORMAT, 10, "hello", 0));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assertTrue("failed to write data", success);
        //verify write
        data = dbClient.read(String.format(READ_ALL_FORMAT, TABLE1_NAME));
        try {
            assertEquals(1, countRows(data));
            assertTrue(data.next());
            assertEquals(10, data.getInt(1));
            assertEquals("hello", data.getString(2));
            assertEquals(false, data.getBoolean(3));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("unable to verify write");
        }
        try { data.close(); } catch (SQLException ignore) { }
        //update entry
        success = false;
        try {
            success = dbClient.update(String.format(TABLE1_UPDATE_ENTRY_FORMAT, "hello2", 1, 10));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assertTrue("failed to update data", success);
        //verify update
        data = dbClient.read(String.format(READ_ALL_FORMAT, TABLE1_NAME));
        try {
            assertEquals(1, countRows(data));
            assertTrue(data.next());
            assertEquals(10, data.getInt(1));
            assertEquals("hello2", data.getString(2));
            assertEquals(true, data.getBoolean(3));
        } catch (SQLException e) {
            e.printStackTrace();
            fail("unable to verify write");
        }
        try { data.close(); } catch (SQLException ignore) { }
        //delete entry
        success = false;
        try {
            success = dbClient.delete(String.format(TABLE1_DELETE_ENTRY_FORMAT, 10));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assertTrue("failed to delete data", success);
        //verify delete
        data = dbClient.read(String.format(READ_ALL_FORMAT, TABLE1_NAME));
        try {
            assertEquals(0, data.getFetchSize());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("unable to verify delete. size>0");
        }
        try { data.close(); } catch (SQLException ignore) { }
    }

    @Test
    public void testDBVersion() {
        DatabaseVersion version = null;
        try {
            version = dbClient.readActualVersion();
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(version.getMajor() >= 10);
    }

    @Test
    public void testTableStuff() {

        boolean success;
        //create Tables/Views
        success = dbClient.createTable(TABLE2_NAME, TABLE2_MAPPING);
        assertTrue(success);
        success = dbClient.createTable(TABLE3_NAME, TABLE3_MAPPING);
        assertTrue(success);
        try {
            success = dbClient.createView(TABLE2_NAME, VIEW2_NAME);
            assertTrue(success);
            success = dbClient.createView(TABLE3_NAME, VIEW3_NAME);
            assertTrue(success);
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        //read Tables
        var tables = dbClient.readTables();
        assertTrue(tables.stream().filter(t -> t.getName().equals(TABLE2_NAME)).count() == 1);
        assertTrue(tables.stream().filter(t -> t.getName().equals(TABLE3_NAME)).count() == 1);
        var views = dbClient.readViews(DBNAME);
        assertTrue(views.stream().filter(t -> t.getTableReference().equals(TABLE2_NAME) && t.getName().equals(VIEW2_NAME))
                .count() == 1);
        assertTrue(views.stream().filter(t -> t.getTableReference().equals(TABLE3_NAME) && t.getName().equals(VIEW3_NAME))
                .count() == 1);

        //delete Tables/Views
        try {
            success = dbClient.deleteView(VIEW2_NAME);
            assertTrue(success);
            success = dbClient.deleteView(VIEW3_NAME);
            assertTrue(success);
            success = dbClient.deleteTable(TABLE2_NAME);
            assertTrue(success);
            success = dbClient.deleteTable(TABLE3_NAME);
            assertTrue(success);
        } catch (SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        //verify
        tables = dbClient.readTables();
        assertTrue(tables.stream().filter(t->t.getName().equals(TABLE2_NAME)).count()==0);
        assertTrue(tables.stream().filter(t->t.getName().equals(TABLE3_NAME)).count()==0);
        views = dbClient.readViews(DBNAME);
        assertEquals(0,views.size());
    }

    public static int countRows(ResultSet data) throws SQLException {
        int rows = 0;
        while (data.next()) {
            rows++;
        }
        data.beforeFirst();
        return rows;
    }
}
