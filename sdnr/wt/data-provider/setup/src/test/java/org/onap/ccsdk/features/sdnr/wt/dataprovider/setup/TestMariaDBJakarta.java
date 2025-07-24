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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.vorburger.exec.ManagedProcessException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlDBDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.SdnrDbType;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;

public class TestMariaDBJakarta {

    private static final String MARIADB_USERNAME = "sdnrdb";
    private static final String MARIADB_PASSWORD = "sdnrdb";
    //    private static final String MARIADB_HOST = "10.20.11.159";
    private static final String MARIADB_HOST = "sdnrdb";
    private static final int MARIADB_PORT = 3306;
    private static final String MARIADB_DATABASENAME = "sdnrdb";

    private static SqlDBClient dbService;

    private static MariaDBTestBase testBase;
    private static SqlDBDataProvider dbProvider;

    @BeforeClass
    public static void init() throws Exception {

        //testBase = new MariaDBTestBase(MARIADB_HOST,MARIADB_PORT, MARIADB_DATABASENAME);
        testBase = new MariaDBTestBase();
        dbProvider = testBase.getDbProvider();
        dbProvider.waitForDatabaseReady(30, TimeUnit.SECONDS);
    //    dbProvider.setControllerId();

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
    public void testCreate() {
        DataMigrationProviderImpl provider = null;
        try {
            provider = new DataMigrationProviderImpl(SdnrDbType.MARIADB, testBase.getDBUrl(), testBase.getDBUsername(),
                    testBase.getDBPassword(), true, 30000);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        boolean success = provider.initDatabase(Release.JAKARTA_R1, 1, 1, "", false, 1000);
        assertTrue("init database failed",success);
        try {
            dbProvider.setControllerId();
        } catch (SQLException e) {
            fail(e.getMessage());
        }

    }
}
